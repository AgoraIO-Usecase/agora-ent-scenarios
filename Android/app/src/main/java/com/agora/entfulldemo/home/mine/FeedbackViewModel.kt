package com.agora.entfulldemo.home.mine

import io.agora.scene.base.api.ApiException
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.ApiSubscriber
import io.agora.scene.base.api.ErrorCode
import io.agora.scene.base.api.apiutils.SchedulersUtil.applyApiSchedulers
import io.agora.scene.base.api.base.BaseResponse
import io.agora.scene.base.bean.CommonBean
import io.agora.scene.base.bean.FeedbackUploadResBean
import io.agora.scene.base.component.BaseRequestViewModel
import io.agora.scene.base.utils.ToastUtils
import io.reactivex.disposables.Disposable
import java.io.File

class FeedbackViewModel : BaseRequestViewModel() {

    fun updatePhoto(file: File, completion: ((error: Exception?, url: String) -> Unit)) {
        ApiManager.getInstance().requestUploadPhoto(file)
            .compose(applyApiSchedulers<BaseResponse<CommonBean>>()).subscribe(
                object : ApiSubscriber<BaseResponse<CommonBean>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
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

    fun requestUploadLog(file: File, completion: ((error: Exception?, url: String) -> Unit)) {
        ApiManager.getInstance().requestUploadLog(file)
            .compose(applyApiSchedulers<BaseResponse<CommonBean>>()).subscribe(
                object : ApiSubscriber<BaseResponse<CommonBean>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
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

    fun requestFeedbackUpload(
        screenshotURLs: Map<String, String>, tags: Array<String>, description: String, logURL: String,
        completion: ((error: Exception?, feedbackRes: FeedbackUploadResBean?) -> Unit)
    ) {
        ApiManager.getInstance().requestFeedbackUpload(screenshotURLs, tags, description, logURL)
            .compose(applyApiSchedulers<BaseResponse<FeedbackUploadResBean>>()).subscribe(
                object : ApiSubscriber<BaseResponse<FeedbackUploadResBean>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
                    }

                    override fun onSuccess(data: BaseResponse<FeedbackUploadResBean>) {
                        if (data.isSuccess) {
                            completion.invoke(null, data.data)
                        } else {
                            completion.invoke(ApiException(data.code ?: ErrorCode.API_ERROR,
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