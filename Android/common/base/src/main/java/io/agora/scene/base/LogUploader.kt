package io.agora.scene.base

import android.os.HandlerThread
import android.util.Log
import io.agora.scene.base.api.ApiException
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.ApiSubscriber
import io.agora.scene.base.api.ErrorCode
import io.agora.scene.base.api.apiutils.SchedulersUtil
import io.agora.scene.base.api.base.BaseResponse
import io.agora.scene.base.bean.CommonBean
import io.agora.scene.base.bean.FeedbackUploadResBean
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.FileUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.ZipUtils
import io.reactivex.disposables.Disposable
import java.io.File

object LogUploader {

//    enum class SceneType(val value: String) {
//        KTV("ktv_log"),
//        KTV_BATTLE("ktv_battle"),
//        KTV_REALY("ktv_relay"),
//        KTV_CANTATA("ktv_cantata"),
//        CHAT("Voice_log"),
//        CHAT_SPATIAL("Voice_spatial"),
//        SHOW("showlive"),
//        PURE1V1("pure"),
//        SHOW_TO_1V1("showto1v1"),
//        JOY("joy"),
//    }


    private const val tag = "LogUploader"
    private val logFolder = AgoraApplication.the().getExternalFilesDir("")!!.absolutePath
    private val logFileWriteThread by lazy {
        HandlerThread("AgoraFeedback.$logFolder").apply {
            start()
        }
    }

    private const val feedbackTag = "autoUploadLog"
    private const val rtcSdkPrefix = "agorasdk"
    private const val rtcApiPrefix = "agoraapi"
    private const val rtmSdkPrefix = "agorartmsdk"

    private var mUploadLogUrl: String = ""

    private fun getAgoraSDKPaths(): List<String> {
        val paths = mutableListOf<String>()
        File(logFolder).listFiles()?.forEach { file ->
            if (file.isFile) {
                if (file.name.startsWith(rtcSdkPrefix) ||
                    file.name.startsWith(rtcApiPrefix) ||
                    file.name.startsWith(rtmSdkPrefix)
                ) {
                    paths.add(file.path)
                }
            }
        }
        return paths
    }

    private fun getScenePaths(type: AgoraScenes): List<String> {
        val paths = mutableListOf<String>()
        File(logFolder + File.separator + "ent").listFiles()?.forEach { file ->
            if (file.isFile) {
                if (file.name.contains(type.name,true)) {
                    paths.add(file.path)
                }
            }
        }
        return paths
    }

     fun uploadLog(type: AgoraScenes) {
        mUploadLogUrl = ""
        val sdkLogZipPath = logFolder + File.separator + "agoraSdkLog.zip"

        val sdkPaths = getAgoraSDKPaths()
        val scenePaths = getScenePaths(type)
        val logPaths = mutableListOf<String>().apply {
            addAll(sdkPaths)
            addAll(scenePaths)
        }
        ZipUtils.compressFiles(logPaths, sdkLogZipPath, object : ZipUtils.ZipCallback {
            override fun onFileZipped(destinationFilePath: String) {
                requestUploadLog(File(destinationFilePath), completion = { error, url ->
                    if (error == null) { // success
                        mUploadLogUrl = url
                        Log.d(tag,"upload log success: $mUploadLogUrl")
                    } else {
                        Log.e(tag, "upload log failed:${error.message}")
                    }
                    FileUtils.deleteFile(sdkLogZipPath)
                    requestFeedbackUpload(mUploadLogUrl) { err, _ ->
                        if (error == null) {
                            Log.d(tag, "upload feedback success")
                        } else {
                            Log.e(tag, "upload feedback failed: ${err?.message}")
                        }

                    }
                })
            }

            override fun onError(e: java.lang.Exception?) {

            }
        })
    }

    fun requestUploadLog(file: File, completion: ((error: Exception?, url: String) -> Unit)) {
        ApiManager.getInstance().requestUploadLog(file)
            .compose(SchedulersUtil.applyApiSchedulers<BaseResponse<CommonBean>>()).subscribe(
                object : ApiSubscriber<BaseResponse<CommonBean>>() {
                    override fun onSubscribe(d: Disposable) {
                        //addDispose(d)
                    }

                    override fun onSuccess(data: BaseResponse<CommonBean>) {
                        completion.invoke(null, data.data!!.url)
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t, "")
                        t?.let {
                            ToastUtils.showToast(it.message)
                        }
                    }
                }
            )
    }

    fun requestFeedbackUpload(logURL: String, completion: ((error: Exception?, feedbackRes: FeedbackUploadResBean?) -> Unit)
    ) {
        ApiManager.getInstance().requestFeedbackUpload(mapOf(), arrayOf(), feedbackTag, logURL)
            .compose(SchedulersUtil.applyApiSchedulers<BaseResponse<FeedbackUploadResBean>>()).subscribe(
                object : ApiSubscriber<BaseResponse<FeedbackUploadResBean>>() {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(data: BaseResponse<FeedbackUploadResBean>) {
                        if (data.isSuccess) {
                            completion.invoke(null, data.data)
                        } else {
                            completion.invoke(
                                ApiException(data.code ?: ErrorCode.API_ERROR,
                                "request feedback upload failed"),null)
                        }
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t, null)
                        t?.let {
                            ToastUtils.showToast(it.message)
                        }
                    }
                }
            )
    }
}