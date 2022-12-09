package io.agora.scene.ktv.service

import android.util.Log
import io.agora.scene.base.KtvConstant
import io.agora.scene.base.api.ApiException
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.ApiSubscriber
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.api.apiutils.GsonUtils.Companion.gson
import io.agora.scene.base.api.apiutils.SchedulersUtil.applyApiSchedulers
import io.agora.scene.base.api.base.BaseResponse
import io.agora.scene.base.bean.MemberMusicModel
import io.agora.scene.base.bean.RoomListModel
import io.agora.scene.base.data.model.AgoraRoom
import io.agora.scene.base.event.ReceivedMessageEvent
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.ktv.manager.RTMManager
import io.agora.scene.ktv.manager.RoomManager
import io.agora.scene.ktv.manager.bean.RTMMessageBean
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 使用Restful API和RTM进行数据交互
 *
 * 注：只能包含Restful api请求和RTM信令
 */
class KTVServiceImp : KTVServiceProtocol {
    val API_ERROR_PASSWORD = 20009

    enum class VLSendMessageType(val value: String) {
        VLSendMessageTypeOnSeat("0"),         // 上麦
        VLSendMessageTypeDropSeat("1"),       // 下麦
        VLSendMessageTypeChooseSong("2"),     // 点歌
        VLSendMessageTypeChangeSong("3"),     // 切歌
        VLSendMessageTypeCloseRoom("4"),      // 关闭房间
        VLSendMessageTypeChangeMVBg("5"),     // 切换MV背景

        VLSendMessageTypeAudioMute("9"),       // 静音
        VLSendMessageTypeVideoIfOpen("10"),    // 摄像头
        VLSendMessageTypeTellSingerSomeBodyJoin("11"),     //通知主唱有人加入合唱
        VLSendMessageTypeTellJoinUID("12"), //通知合唱者 主唱UID
        VLSendMessageTypeSoloSong("13"),  //独唱
        VLSendMessageTypeSeeScore("14"),   //观众看到分数

        VLSendMessageAuditFail("20");
    }

    @Volatile
    private var roomNo: String? = null
    private var creatorNo: String? = null
    private var chorusSongNo: String? = null
    private var localSeatModel: VLRoomSeatModel? = null
    private var selSongModel: VLRoomSelSongModel? = null

    private var roomStatusSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit)? =
        null
    private var roomUserCountSubscriber: ((Int) -> Unit)? =
        null
    private var seatListChangeSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit)? =
        null
    private var chooseSongSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel?) -> Unit)? =
        null

    // ================== 房间相关 ===========================

    override fun getRoomList(
        completion: (error: Exception?, list: List<VLRoomListModel>?) -> Unit
    ) {
        ApiManager.getInstance().requestRoomList(1, 100).compose(applyApiSchedulers())
            .subscribe(object : ApiSubscriber<BaseResponse<RoomListModel>>() {

                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(data: BaseResponse<RoomListModel>) {
                    val ret = ArrayList<VLRoomListModel>()
                    data.data?.records?.forEach {
                        ret.add(
                            VLRoomListModel(
                                it.name,
                                it.isPrivate == 1,
                                it.password,
                                it.creatorNo,
                                it.roomNo,
                                it.isChorus,
                                it.bgOption,
                                it.soundEffect,
                                it.belCanto,
                                it.createdAt,
                                it.updatedAt,
                                it.status,
                                it.deletedAt,
                                it.roomPeopleNum,
                                it.icon,
                                it.creatorNo
                            )
                        )
                    }
                    completion(null, ret)
                }

                override fun onFailure(t: ApiException?) {
                    completion.invoke(t, null)
                }
            })
    }

    override fun createRoomWithInput(
        inputModel: KTVCreateRoomInputModel,
        completion: (error: Exception?, out: KTVCreateRoomOutputModel?) -> Unit
    ) {
        ApiManager.getInstance().requestCreateRoom(
            inputModel.isPrivate,
            inputModel.name,
            inputModel.password,
            inputModel.userNo,
            inputModel.icon
        ).compose(applyApiSchedulers()).subscribe(object : ApiSubscriber<BaseResponse<String?>>() {
            override fun onSubscribe(d: Disposable) {
                // addDispose(d);
            }

            override fun onSuccess(data: BaseResponse<String?>) {
                completion.invoke(
                    null, KTVCreateRoomOutputModel(
                        data.data, inputModel.password
                    )
                )
            }

            override fun onFailure(t: ApiException?) {
                completion.invoke(t, null)
            }
        })
    }

    override fun joinRoomWithInput(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
    ) {
        roomNo = inputModel.roomNo;
        ApiManager.getInstance().requestGetRoomInfo(inputModel.roomNo, inputModel.password)
            .compose(applyApiSchedulers())
            .subscribe(object : ApiSubscriber<BaseResponse<AgoraRoom>>() {
                override fun onSubscribe(d: Disposable) {
                    // addDispose(d);
                }

                override fun onSuccess(data: BaseResponse<AgoraRoom>) {
                    val seatsArray = ArrayList<VLRoomSeatModel>()
                    data.data?.roomUserInfoDTOList?.forEach {
                        val element = VLRoomSeatModel(
                            it.isMaster,
                            it.headUrl,
                            it.userNo,
                            it.id.toString(),
                            it.name,
                            it.onSeat,
                            false,
                            it.isSelfMuted,
                            it.isVideoMuted,
                            false,
                            false
                        )
                        if (element.userNo.equals(UserManager.getInstance().user.userNo)) {
                            localSeatModel = element
                        }
                        seatsArray.add(element)
                    }


                    // save roomNo
                    roomNo = data.data!!.roomNo
                    creatorNo = data.data!!.creatorNo

                    // login RTM to get real message
                    KtvConstant.RTM_TOKEN = data.data!!.agoraRTMToken
                    RTMManager.getInstance().doLoginRTM()
                    RTMManager.getInstance().joinRTMRoom(roomNo)
                    try {
                        EventBus.getDefault().register(this@KTVServiceImp)
                    } catch (e: Exception) {
                        // do nothing
                    }


                    data.data!!.apply {
                        completion.invoke(
                            null, KTVJoinRoomOutputModel(
                                name,
                                roomNo,
                                creatorNo,
                                bgOption,
                                seatsArray,

                                agoraRTMToken,
                                agoraRTCToken,
                                agoraPlayerRTCToken,
                            )
                        )
                    }

                }

                override fun onFailure(t: ApiException?) {
                    roomNo = null
                    if (t != null && t.errCode == API_ERROR_PASSWORD) {
                        completion.invoke(t, null)
                    }
                }
            })
    }

    override fun leaveRoomWithCompletion(completion: (error: Exception?) -> Unit) {
        if (roomNo == null) {
            completion.invoke(RuntimeException("No room joined!"))
            return
        }

        val resetRun: () -> Unit = {
            this@KTVServiceImp.roomNo = null
            this@KTVServiceImp.creatorNo = null
            this@KTVServiceImp.chorusSongNo = null
            this@KTVServiceImp.localSeatModel = null

            // logout rtm
            RTMManager.getInstance().levelRTMRoom();
            RTMManager.getInstance().doLogoutRTM()
            EventBus.getDefault().unregister(this@KTVServiceImp)
        }

        if (UserManager.getInstance().user.userNo.equals(creatorNo)) {
            // creator: remove room
            ApiManager.getInstance().requestCloseRoom(roomNo).compose(applyApiSchedulers())
                .subscribe(object : ApiSubscriber<BaseResponse<String>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<String>) {
                        val bean = RTMMessageBean()
                        bean.messageType = VLSendMessageType.VLSendMessageTypeCloseRoom.value
                        bean.roomNo = roomNo
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))

                        resetRun.invoke()
                        completion.invoke(null)
                    }

                    override fun onFailure(t: ApiException?) {
                        if (t!!.message == "无法关闭房间") {
                            val bean = RTMMessageBean()
                            bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_CREATOR_EXIT
                            bean.roomNo = roomNo
                            RTMManager.getInstance().sendMessage(gson.toJson(bean))

                            resetRun.invoke()
                            completion.invoke(null)
                        } else {
                            completion.invoke(t)
                            ToastUtils.showToast(t.message)
                        }
                    }
                })
        } else {
            // other: leave room
            ApiManager.getInstance().requestExitRoom(roomNo).compose(applyApiSchedulers())
                .subscribe(object : ApiSubscriber<BaseResponse<String>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<String>) {
                        // 发送RTM消息通知他人
                        val bean = RTMMessageBean()
                        bean.headUrl = UserManager.getInstance().user.headUrl
                        bean.roomNo = roomNo
                        bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_LEAVE_SEAT
                        bean.userNo = UserManager.getInstance().user.userNo
                        bean.onSeat = RoomManager.getInstance().mine!!.onSeat
                        bean.name = UserManager.getInstance().user.name
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))

                        resetRun.invoke()

                        completion.invoke(null)
                        // TODO 切歌给其他人
                        // if (RoomManager.mMine.userNo == RoomManager.getInstance().mMusicModel.userNo) {
                        //     changeMusic()
                        // }
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                })
        }
    }

    override fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel, completion: (error: Exception?) -> Unit
    ) {
        if (roomNo == null) {
            completion.invoke(RuntimeException("No room joined!"))
            return
        }
        ApiManager.getInstance()
            .requestRoomInfoEdit(roomNo, null, inputModel.mvIndex.toString(), null).compose(
                applyApiSchedulers()
            ).subscribe(object : ApiSubscriber<BaseResponse<String>>() {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(data: BaseResponse<String>) {
                    val bean = RTMMessageBean()
                    bean.headUrl = UserManager.getInstance().user.headUrl
                    bean.messageType = VLSendMessageType.VLSendMessageTypeChangeMVBg.value
                    bean.userNo = UserManager.getInstance().user.userNo
                    bean.roomNo = roomNo
                    bean.bgOption = inputModel.mvIndex.toString()
                    RTMManager.getInstance().sendMessage(gson.toJson(bean))
                    completion.invoke(null)

                    roomStatusSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated, VLRoomListModel(
                            "",
                            false,
                            "",
                            bean.userNo,
                            bean.roomNo,
                            0,
                            bean.bgOption,
                            "",
                            "",
                            "",
                            "",
                            0,
                            "",
                            0,
                            "",
                            ""
                        )
                    )
                }

                override fun onFailure(t: ApiException?) {
                    completion.invoke(t)
                }
            })
    }

    override fun subscribeRoomStatusWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit) {
        roomStatusSubscriber = changedBlock
    }

    override fun subscribeUserListCountWithChanged(changedBlock: (count: Int) -> Unit) {
        roomUserCountSubscriber = changedBlock
    }


    // =============== 麦位相关 ===========================

    override fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel, completion: (error: Exception?) -> Unit
    ) {
        if (localSeatModel != null) {
            completion.invoke(RuntimeException("The user has been on seat!"))
            return
        }
        ApiManager.getInstance()
            .requestRoomHaveSeatRoomInfo(
                roomNo,
                inputModel.seatIndex,
                UserManager.getInstance().user.userNo
            )
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<AgoraRoom>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<AgoraRoom>) {
                        Log.d("cwtsw", "haveSeat onSuccess")
                        //上麦成功 推送
                        val bean = RTMMessageBean()
                        bean.headUrl = UserManager.getInstance().user.headUrl
                        bean.messageType = VLSendMessageType.VLSendMessageTypeOnSeat.value
                        bean.userNo = UserManager.getInstance().user.userNo
                        bean.roomNo = roomNo
                        bean.onSeat = inputModel.seatIndex
                        bean.id = UserManager.getInstance().user.id
                        bean.name = UserManager.getInstance().user.name
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))

                        completion.invoke(null)
                        localSeatModel = VLRoomSeatModel(
                            bean.userNo.equals(creatorNo),
                            bean.headUrl,
                            bean.userNo,
                            bean.id.toString(),
                            bean.name,
                            bean.onSeat,
                            false,
                            bean.isSelfMuted,
                            bean.isVideoMuted,
                            false,
                            false
                        )
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            localSeatModel
                        )
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                })
    }

    override fun outSeatWithInput(
        inputModel: KTVOutSeatInputModel, completion: (error: Exception?) -> Unit
    ) {
        if (localSeatModel == null) {
            completion.invoke(RuntimeException("The user has been out seat!"))
            return
        }
        ApiManager.getInstance().requestRoomLeaveSeatRoomInfo(roomNo, inputModel.userNo)
            .compose(
                applyApiSchedulers()
            ).subscribe(
                object : ApiSubscriber<BaseResponse<AgoraRoom>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<AgoraRoom>) {
                        //下麦成功 推送通知其他人
                        val bean = RTMMessageBean()
                        bean.headUrl = UserManager.getInstance().user.headUrl
                        bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_LEAVE_SEAT
                        bean.roomNo = roomNo
                        bean.userNo = inputModel.userNo
                        bean.onSeat = inputModel.userOnSeat
                        bean.name = inputModel.userName
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))

                        completion.invoke(null)
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                            VLRoomSeatModel(
                                bean.userNo.equals(creatorNo),
                                bean.headUrl,
                                bean.userNo,
                                bean.id.toString(),
                                bean.name,
                                bean.onSeat,
                                false,
                                bean.isSelfMuted,
                                bean.isVideoMuted,
                                false,
                                false
                            )
                        )

                        localSeatModel = null
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                })
    }

    override fun muteWithMuteStatus(isSelfMuted: Int, completion: (error: Exception?) -> Unit) {
        if (localSeatModel == null) {
            completion.invoke(RuntimeException("The user has been out seat!"))
            return
        }
        val localSeatModel = localSeatModel ?: return
        //同步静音状态
        ApiManager.getInstance()
            .requestToggleMic(isSelfMuted, UserManager.getInstance().user.userNo, roomNo)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<String>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<String>) {
                        this@KTVServiceImp.localSeatModel = VLRoomSeatModel(
                            localSeatModel.isMaster,
                            localSeatModel.headUrl,
                            localSeatModel.userNo,
                            localSeatModel.id,
                            localSeatModel.name,
                            localSeatModel.onSeat,
                            localSeatModel.joinSing,
                            isSelfMuted,
                            localSeatModel.isVideoMuted,
                            localSeatModel.ifSelTheSingSong,
                            localSeatModel.ifJoinedChorus
                        )

                        //发送通知
                        val bean = RTMMessageBean()
                        bean.messageType = VLSendMessageType.VLSendMessageTypeAudioMute.value
                        bean.userNo = UserManager.getInstance().user.userNo
                        bean.roomNo = roomNo
                        bean.isSelfMuted = isSelfMuted
                        bean.isVideoMuted = localSeatModel.isVideoMuted
                        bean.id = UserManager.getInstance().user.id
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))

                        completion.invoke(null)
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                            this@KTVServiceImp.localSeatModel
                        )
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                }
            )
    }

    override fun openVideoStatusWithStatus(
        isVideoMuted: Int, completion: (error: Exception?) -> Unit
    ) {
        if (localSeatModel == null) {
            completion.invoke(RuntimeException("The user has been out seat!"))
            return
        }
        val localSeatModel = localSeatModel ?: return
        ApiManager.getInstance()
            .requestOpenCamera(isVideoMuted, UserManager.getInstance().user.userNo, roomNo)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<String>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<String>) {
                        this@KTVServiceImp.localSeatModel = VLRoomSeatModel(
                            localSeatModel.isMaster,
                            localSeatModel.headUrl,
                            localSeatModel.userNo,
                            localSeatModel.id,
                            localSeatModel.name,
                            localSeatModel.onSeat,
                            localSeatModel.joinSing,
                            localSeatModel.isSelfMuted,
                            isVideoMuted,
                            localSeatModel.ifSelTheSingSong,
                            localSeatModel.ifJoinedChorus
                        )

                        //发送通知
                        val bean = RTMMessageBean()
                        bean.messageType = VLSendMessageType.VLSendMessageTypeVideoIfOpen.value
                        bean.userNo = UserManager.getInstance().user.userNo
                        bean.id = UserManager.getInstance().user.id
                        bean.roomNo = roomNo
                        bean.isVideoMuted = isVideoMuted
                        bean.isSelfMuted = localSeatModel.isSelfMuted
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))
                        completion.invoke(null)

                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                            this@KTVServiceImp.localSeatModel
                        )
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(null)
                    }
                }
            )
    }


    override fun subscribeSeatListWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit) {
        seatListChangeSubscriber = changedBlock
    }


    // =============== 歌词相关 ================================


    override fun getChoosedSongsListWithCompletion(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        ApiManager.getInstance().requestGetSongsOrderedList(roomNo)
            .compose(applyApiSchedulers())
            .subscribe(object : ApiSubscriber<BaseResponse<List<MemberMusicModel>?>>() {
                override fun onSubscribe(d: Disposable) {
                    // addDispose(d);
                }

                override fun onSuccess(data: BaseResponse<List<MemberMusicModel>?>) {
                    val out = ArrayList<VLRoomSelSongModel>()
                    data.data!!.forEach {
                        out.add(
                            VLRoomSelSongModel(
                                it.songName,
                                it.songNo,
                                it.songUrl,
                                it.singer,
                                it.lyric,
                                it.status,
                                it.imageUrl,

                                it.userNo,
                                it.userId,
                                it.name,
                                it.chorusNo,
                                it.isChorus,
                                it.isOriginal,
                                it.sort
                            )
                        )
                    }
                    completion.invoke(null, out)
                }

                override fun onFailure(t: ApiException?) {
                    completion.invoke(t, null)
                }
            })
    }

    override fun switchSongWithInput(
        inputModel: KTVSwitchSongInputModel, completion: (error: Exception?) -> Unit
    ) {
        ApiManager.getInstance().requestSwitchSong(
            inputModel.userNo, inputModel.songNo, inputModel.roomNo
        ).compose(applyApiSchedulers()).subscribe(object : ApiSubscriber<BaseResponse<String?>?>() {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(data: BaseResponse<String?>?) {
                //通过RTM通知其他人切歌
                val bean = RTMMessageBean()
                bean.headUrl = UserManager.getInstance().user.headUrl
                bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_SWITCH_SONGS
                bean.roomNo = inputModel.roomNo
                bean.userNo = UserManager.getInstance().user.userNo
                RTMManager.getInstance().sendMessage(gson.toJson(bean))

                completion.invoke(null)
            }

            override fun onFailure(t: ApiException?) {
                if (t != null && "歌曲不存在" == t.message) {
                    Log.d("cwtsw", "歌曲不存在 切歌")

                    //通过RTM通知其他人切歌
                    val bean = RTMMessageBean()
                    bean.headUrl = UserManager.getInstance().user.headUrl
                    bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_SWITCH_SONGS
                    bean.roomNo = inputModel.roomNo
                    bean.userNo = UserManager.getInstance().user.userNo
                    RTMManager.getInstance().sendMessage(gson.toJson(bean))

                    completion.invoke(null)
                } else {
                    completion.invoke(t)
                }

            }
        })
    }

    override fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel, completion: (error: Exception?) -> Unit
    ) {
        ApiManager.getInstance().requestDeleteSong(
            inputModel.sort.toInt(), inputModel.songNo, UserManager.getInstance().user.userNo, roomNo)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<String?>?>() {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(data: BaseResponse<String?>?) {
                        val bean = RTMMessageBean();
                        bean.headUrl = UserManager.getInstance().getUser().headUrl;
                        bean.messageType = VLSendMessageType.VLSendMessageTypeChangeSong.value
                        bean.userNo = UserManager.getInstance().user.userNo;
                        bean.name = UserManager.getInstance().user.name;
                        RTMManager.getInstance().sendMessage(GsonUtils.Companion.gson.toJson(bean));

                        completion.invoke(null)
                        chooseSongSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                            null
                        )
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                }
            );
    }

    override fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun markSongDidPlayWithInput(
        inputModel: VLRoomSelSongModel, completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }


    override fun chooseSongWithInput(
        inputModel: KTVChooseSongInputModel, completion: (error: Exception?) -> Unit
    ) {
        val isChorus : Int = inputModel.isChorus
        ApiManager.getInstance().requestChooseSong(inputModel.imageUrl, isChorus, 0, inputModel.singer, inputModel.songName,
                inputModel.songNo, inputModel.imageUrl, UserManager.getInstance().user.userNo, roomNo)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<String?>?>() {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(data: BaseResponse<String?>?) {
                        val bean = RTMMessageBean()
                        bean.messageType = VLSendMessageType.VLSendMessageTypeChooseSong.value
                        bean.roomNo = roomNo
                        val json = gson.toJson(bean)
                        RTMManager.getInstance().sendMessage(json)

                        completion.invoke(null)

                        chooseSongSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            null
                        )
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                }
            );
    }

    override fun makeSongTopWithInput(
        inputModel: KTVMakeSongTopInputModel, completion: (error: Exception?) -> Unit
    ) {
        ApiManager.getInstance().requestTopSong(inputModel.sort, inputModel.songNo, UserManager.getInstance().user.userNo, roomNo)
        .compose(applyApiSchedulers()).subscribe(
            object : ApiSubscriber<BaseResponse<String?>?>() {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onSuccess(data: BaseResponse<String?>?) {
                    val bean = RTMMessageBean();
                    bean.headUrl = UserManager.getInstance().getUser().headUrl;
                    bean.messageType = VLSendMessageType.VLSendMessageTypeChangeSong.value
                    bean.userNo = UserManager.getInstance().getUser().userNo;
                    bean.name = UserManager.getInstance().getUser().name;
                    RTMManager.getInstance().sendMessage(gson.toJson(bean))

                    completion.invoke(null)
                    chooseSongSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                        null
                    )
                }

                override fun onFailure(t: ApiException?) {
                    completion.invoke(t)
                }
            }
        );
    }


    override fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel, completion: (error: Exception?) -> Unit
    ) {
        ApiManager.getInstance()
            .requestJoinChorus(inputModel.songNo, UserManager.getInstance().user.userNo, roomNo)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<String>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<String>) {
                        chorusSongNo = inputModel.songNo

                        val bgUid: Long = UserManager.getInstance().user.id * 10 + 1
                        //通知主唱 有人加入了合唱
                        val bean = RTMMessageBean()
                        bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_APPLY_JOIN_CHORUS
                        bean.userNo = UserManager.getInstance().user.userNo
                        bean.name = UserManager.getInstance().user.name
                        bean.roomNo = roomNo
                        bean.bgUid = bgUid
                        RTMManager.getInstance().sendMessage(gson.toJson(bean))
                        Log.d("cwtsw", "发送消息11 加入合唱")

                        completion.invoke(null)
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                }
            )
    }

    override fun becomeSolo() {
        val bean = RTMMessageBean()
        bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_NO_JOIN_CHORUS
        bean.userNo = UserManager.getInstance().user.userNo
        bean.roomNo = roomNo
        bean.songNo = chorusSongNo
        RTMManager.getInstance().sendMessage(gson.toJson(bean))

        ApiManager.getInstance().requestRoomCancelChorus(
            UserManager.getInstance().user.userNo,
            chorusSongNo,
            roomNo
        ).compose(
            applyApiSchedulers()
        ).subscribe(
            object : ApiSubscriber<BaseResponse<String>>() {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(data: BaseResponse<String>) {}
                override fun onFailure(t: ApiException?) {
                }
            })
    }


    override fun subscribeChooseSongWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel?) -> Unit) {
        chooseSongSubscriber = changedBlock
    }


    //======推送消息处理=======

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ReceivedMessageEvent?) {
        val bean = gson.fromJson(
            event!!.message, RTMMessageBean::class.java
        )
        if (roomNo != bean.roomNo) return
        if (bean.messageType != "14") {
            Log.d("cwtsw", "收到消息" + event.message)
        }
        when (bean.messageType) {
            VLSendMessageType.VLSendMessageTypeCloseRoom.value -> {
                roomStatusSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted, null
                )
            }
            VLSendMessageType.VLSendMessageTypeChangeMVBg.value -> {
                roomStatusSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated, VLRoomListModel(
                        "",
                        false,
                        "",
                        bean.userNo,
                        bean.roomNo,
                        0,
                        bean.bgOption,
                        "",
                        "",
                        "",
                        "",
                        0,
                        "",
                        0,
                        "",
                        ""
                    )
                )
            }
            VLSendMessageType.VLSendMessageTypeOnSeat.value -> {
                seatListChangeSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                    VLRoomSeatModel(
                        bean.userNo.equals(creatorNo),
                        bean.headUrl,
                        bean.userNo,
                        bean.id.toString(),
                        bean.name,
                        bean.onSeat,
                        false,
                        bean.isSelfMuted,
                        bean.isVideoMuted,
                        false,
                        false
                    )
                )
            }
            VLSendMessageType.VLSendMessageTypeDropSeat.value -> {
                seatListChangeSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                    VLRoomSeatModel(
                        bean.userNo.equals(creatorNo),
                        bean.headUrl,
                        bean.userNo,
                        bean.id.toString(),
                        bean.name,
                        bean.onSeat,
                        false,
                        bean.isSelfMuted,
                        bean.isVideoMuted,
                        false,
                        false
                    )
                )
            }
            VLSendMessageType.VLSendMessageTypeAudioMute.value -> {
                seatListChangeSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    VLRoomSeatModel(
                        bean.userNo.equals(creatorNo),
                        bean.headUrl,
                        bean.userNo,
                        bean.id.toString(),
                        bean.name,
                        bean.onSeat,
                        false,
                        bean.isSelfMuted,
                        bean.isVideoMuted,
                        false,
                        false
                    )
                )
            }
            VLSendMessageType.VLSendMessageTypeChooseSong.value -> {
                chooseSongSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                    null
                )
            }
            VLSendMessageType.VLSendMessageTypeChangeSong.value -> {
                chooseSongSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    null
                )
            }
            VLSendMessageType.VLSendMessageTypeVideoIfOpen.value -> {
                seatListChangeSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    VLRoomSeatModel(
                        bean.userNo.equals(creatorNo),
                        bean.headUrl,
                        bean.userNo,
                        bean.id.toString(),
                        bean.name,
                        bean.onSeat,
                        false,
                        bean.isSelfMuted,
                        bean.isVideoMuted,
                        false,
                        false
                    )
                )
            }
        }
    }
}