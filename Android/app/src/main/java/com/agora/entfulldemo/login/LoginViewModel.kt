package com.agora.entfulldemo.login

import androidx.lifecycle.MutableLiveData
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
import io.agora.scene.base.utils.ToastUtils
import io.reactivex.disposables.Disposable

class LoginViewModel : BaseRequestViewModel() {

    /**
     * 登录
     *
     * @param account 账号
     * @param vCode   验证码
     */
    fun requestLogin(account: String, vCode: String?) {
        if (account != phone) {
            iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL,null)
            ToastUtils.showToast(R.string.app_vcode_wrong_tip)
            return
        }
        ApiManager.getInstance().requestLogin(account, vCode)
            .compose(applyApiSchedulers()).subscribe(
                object : ApiSubscriber<BaseResponse<User>>() {
                    override fun onSubscribe(d: Disposable) {
                        addDispose(d)
                    }

                    override fun onSuccess(data: BaseResponse<User>) {
                        ToastUtils.showToast(R.string.app_login_success_tip)
                        ApiManager.token = data.data!!.token
                        UserManager.getInstance().saveUserInfo(data.data)
                        iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS,null)
                    }


                    override fun onFailure(t: ApiException?) {
                        if (t != null) {
                            ToastUtils.showToast(t.message)
                        } else {
                            ToastUtils.showToast(R.string.app_login_failed_tip)
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
     * 发送验证码
     *
     * @param phone 手机号
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
                        ToastUtils.showToast(R.string.app_vcode_send_success_tip)
                        iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_CODE_SUCCESS,null)
                    }

                    override fun onFailure(t: ApiException?) {
                        if (t != null) {
                            ToastUtils.showToast(t.message)
                        } else {
                            ToastUtils.showToast(R.string.app_vcode_send_failed_tip)
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