package io.agora.scene.base

import android.util.Log
import android.widget.Toast
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
import io.reactivex.disposables.Disposable
import java.io.File

object LogUploader {

    private const val tag = "LogUploader"
    private val logFolder = AgoraApplication.the().getExternalFilesDir("")!!.absolutePath

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
        FileUtils.compressFiles(logPaths, sdkLogZipPath, object : FileUtils.ZipCallback {
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
                    }
                }
            )
    }
}