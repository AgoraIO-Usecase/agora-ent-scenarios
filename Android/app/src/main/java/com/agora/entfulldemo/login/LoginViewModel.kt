package com.agora.entfulldemo.login

import com.agora.entfulldemo.R
import io.agora.scene.base.Constant
import io.agora.scene.base.api.ApiException
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.ApiSubscriber
import io.agora.scene.base.api.apiutils.SchedulersUtil.applyApiSchedulers
import io.agora.scene.base.api.base.BaseResponse
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.BaseRequestViewModel
import io.agora.scene.base.manager.UserManager
import io.agora.scene.widget.toast.CustomToast
import io.reactivex.disposables.Disposable

class LoginViewModel : BaseRequestViewModel() {

    /**
     * Request login
     *
     * @param account
     * @param vCode
     */
    fun requestLogin(account: String, vCode: String) {
        if (account != phone) {
            iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL,null)
            CustomToast.show(R.string.app_vcode_wrong_tip)
            return
        }
        ApiManager.getInstance().requestLogin(account, vCode)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<User>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
                    }

                    override fun onSuccess(data: BaseResponse<User>) {
                        if (data.isSuccess && data.data != null) {
                            CustomToast.show(R.string.app_login_success_tip)
                            val user: User = data.data!!
                            user.setRealNameVerifyStatus(user.realNameVerifyStatus)
                            ApiManager.token = user.token
                            UserManager.getInstance().saveUserInfo(user,true)
                            iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS,null)
                        }else{
                            CustomToast.show(R.string.app_login_failed_tip)
                            iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL,null)
                        }

                    }


                    override fun onFailure(t: ApiException?) {
                        if (t?.message != null) {
                            CustomToast.show(t.message!!)
                        } else {
                            CustomToast.show(R.string.app_login_failed_tip)
                        }
                        iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL,null)
                    }
                }
            )
    }

    private var phone: String? = null

    fun getPhone(): String? {
        return phone
    }

    fun setPhone(phone:String){
        this.phone = phone
    }

    /**
     * Request send v code
     *
     * @param phone
     */
    fun requestSendVCode(phone: String?) {
        this.phone = phone
        ApiManager.getInstance().requestSendVerCode(phone)
            .compose(applyApiSchedulers())
            .subscribe(
                object : ApiSubscriber<BaseResponse<String>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
                    }

                    override fun onSuccess(t: BaseResponse<String>?) {
                        CustomToast.show(R.string.app_vcode_send_success_tip)
                        iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_CODE_SUCCESS,null)
                    }

                    override fun onFailure(t: ApiException?) {
                        if (t?.message != null) {
                            CustomToast.show(t.message!!)
                        } else {
                            CustomToast.show(R.string.app_vcode_send_failed_tip)
                        }
                        iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_CODE_FAIL,null)
                    }
                }
            )
    }

    override fun clearDispose() {
        super.clearDispose()
    }
}