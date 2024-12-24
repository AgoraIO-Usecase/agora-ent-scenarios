package io.agora.scene.base.api

import io.agora.scene.base.api.apiutils.SchedulersUtil.applyApiSchedulers
import io.agora.scene.base.api.base.BaseResponse
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.BaseRequestViewModel
import io.agora.scene.base.manager.UserManager
import io.reactivex.disposables.Disposable

class RealNameViewModel : BaseRequestViewModel() {

    fun requestUserInfo(userNo: String,completion: ((error: Exception?) -> Unit)) {
        ApiManager.getInstance().requestUserInfo(userNo)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<User>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
                    }

                    override fun onSuccess(data: BaseResponse<User>) {
                        UserManager.getInstance().saveUserInfo(data.data,false)
                        completion.invoke(null)
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                }
            )
    }

    fun requestRealNameAuth(realName: String, idCard: String, completion: ((error: Exception?) -> Unit)) {
        ApiManager.getInstance().requestRealNameAuth(realName, idCard)
            .compose(applyApiSchedulers<BaseResponse<Void>>()).subscribe(
                object : ApiSubscriber<BaseResponse<Void>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
                    }

                    override fun onSuccess(data: BaseResponse<Void>) {
                        if (data.isSuccess) {
                            completion.invoke(null)
                        } else {
                            completion.invoke(
                                ApiException(data.code ?: ErrorCode.API_ERROR, data.message)
                            )
                        }
                    }

                    override fun onFailure(t: ApiException?) {
                        completion.invoke(t)
                    }
                }
            )
    }
}