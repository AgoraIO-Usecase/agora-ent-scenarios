package io.agora.scene.ktv.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback

/**
 * 使用SyncManager进行数据交互
 *
 *
 */
class KTVSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : KTVServiceProtocol {
    private val kSceneId = "scene_ktv"


    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }


    // ========= 房间相关 =====================

    override fun getRoomList(
        completion: (error: Exception?, list: List<VLRoomListModel>?) -> Unit
    ) {
        initScene {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val ret = ArrayList<VLRoomListModel>()
                    result?.forEach {
                        val obj = it.toObject(VLRoomListModel::class.java)
                        obj.objectId = it.id
                        ret.add(obj)
                    }
                    runOnMainThread { completion.invoke(null, ret) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    override fun createRoomWithInput(
        inputModel: KTVCreateRoomInputModel,
        completion: (error: Exception?, out: KTVCreateRoomOutputModel?) -> Unit
    ) {
        initScene {
            val scene = Scene()
            scene.id = inputModel.name
            scene.userId = inputModel.userNo
            Sync.Instance().createScene(scene, object: Sync.Callback{
                override fun onSuccess() {

                }

                override fun onFail(exception: SyncManagerException?) {

                }
            })
        }
    }

    override fun joinRoomWithInput(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
    ) {
        initScene {
            Sync.Instance().joinScene(inputModel.roomNo, object: Sync.JoinSceneCallback{
                override fun onSuccess(sceneReference: SceneReference?) {

                }

                override fun onFail(exception: SyncManagerException?) {

                }
            })
        }
    }

    override fun leaveRoomWithCompletion(completion: (error: Exception?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun subscribeRoomStatusWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun subscribeUserListCountWithChanged(changedBlock: (count: Int) -> Unit) {
        TODO("Not yet implemented")
    }

    // =================== 麦位相关 ===============================

    override fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun outSeatWithInput(
        inputModel: KTVOutSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun muteWithMuteStatus(isSelfMuted: Int, completion: (error: Exception?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun openVideoStatusWithStatus(
        isVideoMuted: Int,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun subscribeSeatListWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit) {
        TODO("Not yet implemented")
    }


    // ============= 歌曲相关 =============================

    override fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getChoosedSongsListWithCompletion(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun switchSongWithInput(
        inputModel: KTVSwitchSongInputModel,
        completion: (error: Exception?) -> Unit
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

    override fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun becomeSolo() {
        TODO("Not yet implemented")
    }

    override fun subscribeChooseSongWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel) -> Unit) {
        TODO("Not yet implemented")
    }


    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInited) {
            complete.invoke()
            return
        }

        Sync.Instance().init(context,
            mapOf(
                Pair("appid", BuildConfig.AGORA_APP_ID),
                Pair("defaultChannel", kSceneId),
            ),
            object : Sync.Callback {
                override fun onSuccess() {
                    syncUtilsInited = true
                    runOnMainThread { complete.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread{ errorHandler?.invoke(exception) }
                }
            }
        )
    }


}