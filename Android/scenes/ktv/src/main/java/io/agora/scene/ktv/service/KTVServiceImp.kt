package io.agora.scene.ktv.service

import io.agora.scene.base.api.ApiException
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.ApiSubscriber
import io.agora.scene.base.api.apiutils.SchedulersUtil.applyApiSchedulers
import io.agora.scene.base.api.base.BaseResponse
import io.agora.scene.base.bean.RoomListModel
import io.agora.scene.base.data.model.AgoraRoom
import io.agora.scene.base.manager.UserManager
import io.reactivex.disposables.Disposable

class KTVServiceImp : KTVServiceProtocol {

    override fun getRoomListWithPage(
        completion: (error: Exception?, list: List<VLRoomListModel>?) -> Unit
    ) {
        ApiManager.getInstance().requestRoomList(1, 100)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<RoomListModel>>() {

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
                }
            )
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
        ).compose(applyApiSchedulers()).subscribe(
            object : ApiSubscriber<BaseResponse<String?>>() {
                override fun onSubscribe(d: Disposable) {
                    // addDispose(d);
                }

                override fun onSuccess(data: BaseResponse<String?>) {
                    val room = AgoraRoom()
                    room.roomNo = data.data
                    room.creatorNo = UserManager.getInstance().user.userNo
                    room.isPrivate = inputModel.isPrivate
                    room.name = inputModel.name
                    room.belCanto = "0"
                    room.icon = inputModel.icon
                    ApiManager.getInstance().requestGetRoomInfo(room.roomNo, inputModel.password)
                        .compose(applyApiSchedulers()).subscribe(
                            object : ApiSubscriber<BaseResponse<AgoraRoom>>() {
                                override fun onSubscribe(d: Disposable) {
                                    // addDispose(d);
                                }

                                override fun onSuccess(data: BaseResponse<AgoraRoom>) {
                                    completion.invoke(
                                        null, KTVCreateRoomOutputModel(
                                            room.name,
                                            room.roomNo,
                                            null,
                                            // if null then crash for the value must be not null!
                                            data.data!!.agoraRTMToken,
                                            data.data!!.agoraRTCToken,
                                            data.data!!.agoraPlayerRTCToken
                                        )
                                    )
                                }

                                override fun onFailure(t: ApiException?) {
                                    completion.invoke(t, null)
                                }
                            }
                        )

                }

                override fun onFailure(t: ApiException?) {
                    completion.invoke(t, null)
                }
            }
        )
    }

    override fun joinRoomWithInput(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
    ) {
        ApiManager.getInstance().requestGetRoomInfo(inputModel.roomNo, inputModel.password)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<AgoraRoom>>() {
                    override fun onSubscribe(d: Disposable) {
                        // addDispose(d);
                    }

                    override fun onSuccess(data: BaseResponse<AgoraRoom>) {
                        completion.invoke(
                            null, KTVJoinRoomOutputModel(
                                data.data!!.creatorNo,
                                null,
                                // if null then crash for the value must be not null!
                                data.data!!.agoraRTMToken,
                                data.data!!.agoraRTCToken,
                                data.data!!.agoraPlayerRTCToken
                            )
                        )
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t, null)
                    }
                }
            )
    }

    override fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun outSeatWithInput(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun leaveRoomWithCompletion(completion: (error: Exception?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeRoomWithCompletion(completion: (error: Exception?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getChoosedSongsListWithCompletion(completion: (error: Exception?, list: List<VLRoomSelSongModel>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun markSongDidPlayWithInput(
        inputModel: VLRoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun chooseSongWithInput(
        inputModel: KTVChooseSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun makeSongTopWithInput(
        inputModel: KTVMakeSongTopInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun subscribeUserListCountWithChanged(changedBlock: (count: Int) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun subscribeSeatListWithChanged(changedBlock: (Int, VLRoomSeatModel) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun subscribeRoomStatusWithChanged(changedBlock: (Int, VLRoomListModel) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun subscribeChooseSongWithChanged(changedBlock: (Int, VLRoomSelSongModel) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun muteWithMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun openVideoStatusWithStatus(
        openStatus: Boolean,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun publishChooseSongEvent() {
        TODO("Not yet implemented")
    }

    override fun leaveChannel() {
        TODO("Not yet implemented")
    }

    override fun publishMuteEventWithMuteStatus(
        muteStatus: Boolean,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun publishVideoOpenEventWithOpenStatus(
        openStatus: Boolean,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun publishSongDidChangedEventWithOwnerStatus(isMaster: Boolean) {
        TODO("Not yet implemented")
    }

    override fun publishToSoloEvent() {
        TODO("Not yet implemented")
    }

    override fun publishJoinToChorusWithCompletion(completion: (error: Exception?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun publishSongOwnerWithOwnerId(userNo: String) {
        TODO("Not yet implemented")
    }

    override fun publishSingingScoreWithTotalVolume(totalVolume: Double) {
        TODO("Not yet implemented")
    }


}