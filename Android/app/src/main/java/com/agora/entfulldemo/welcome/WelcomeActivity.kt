package com.agora.entfulldemo.welcome

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import com.agora.entfulldemo.databinding.AppActivityWelcomeBinding
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.Constant
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil

@Route(path = PagePathConstant.pageWelcome)
class WelcomeActivity : BaseViewBindingActivity<AppActivityWelcomeBinding>() {
    private var userAgreementDialog: UserAgreementDialog? = null
    private var userAgreementDialog2: UserAgreementDialog2? = null
    override fun getViewBinding(inflater: LayoutInflater): AppActivityWelcomeBinding {
        return AppActivityWelcomeBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.ivAppLogo.postDelayed({ checkStatusToStart() }, 500)
    }

    override fun initListener() {}
    override fun isBlackDarkStatus(): Boolean {
        return false
    }

    /**
     * 显示用户协议 隐私政策对话框
     */
    private fun showUserAgreementDialog() {
        if (userAgreementDialog == null) {
            userAgreementDialog = UserAgreementDialog(this)
            userAgreementDialog!!.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {
                    showUserAgreementDialog2()
                    userAgreementDialog!!.dismiss()
                }

                override fun onRightButtonClick() {
                    PagePilotManager.pageLogin()
                    userAgreementDialog!!.dismiss()
                    finish()
                }
            }
        }
        userAgreementDialog!!.show()
    }

    /**
     * 显示用户协议 隐私政策对话框
     */
    private fun showUserAgreementDialog2() {
        if (userAgreementDialog2 == null) {
            userAgreementDialog2 = UserAgreementDialog2(this)
            userAgreementDialog2!!.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {
                    userAgreementDialog2!!.dismiss()
                    finish()
                }

                override fun onRightButtonClick() {
                    PagePilotManager.pageLogin()
                    userAgreementDialog2!!.dismiss()
                    finish()
                }
            }
        }
        userAgreementDialog2!!.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun getPermissions() {
    }

    private fun checkStatusToStart() {
        startMainActivity()
    }

    private fun startMainActivity() {
        if (UserManager.getInstance().isLogin) {
            PagePilotManager.pageMainHome()
            finish()
        } else {
            if (!SPUtil.getBoolean(Constant.IS_AGREE, false)) {
                showUserAgreementDialog()
            } else {
                PagePilotManager.pageLogin()
                finish()
            }
        }
    }
}
