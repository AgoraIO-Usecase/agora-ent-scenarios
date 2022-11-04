package io.agora.scene.ktv.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback

class KTVSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : KTVServiceProtocol {
    private val kSceneId = "scene_ktv"


    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun getRoomListWithPage(
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