package com.agora.entfulldemo.welcome

import android.os.Build
import android.os.Bundle
import android.os.Process
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
import kotlin.system.exitProcess

@Route(path = PagePathConstant.pageWelcome)
class WelcomeActivity : BaseViewBindingActivity<AppActivityWelcomeBinding>() {
    private var userAgreementDialog: UserAgreementDialog? = null
    private var userAgreementDialog2: UserAgreementDialog2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            val code = intent.getIntExtra(Constant.KEY_CODE, -1)
            if (code == Constant.PARAMS_EXIT) {
                binding.root.postDelayed({
                    Process.killProcess(Process.myPid())
                    exitProcess(0)
                }, 500)
            }
        }
    }

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
     * Show user agreement dialog
     *
     */
    private fun showUserAgreementDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (userAgreementDialog == null) {
            userAgreementDialog = UserAgreementDialog(this).apply {
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        dismiss()
                        showUserAgreementDialog2()
                    }

                    override fun onRightButtonClick() {
                        dismiss()
                        finish()
                        PagePilotManager.pageLogin()
                    }
                }
            }
        }
        userAgreementDialog?.show()
    }

    /**
     * Show user agreement dialog2
     *
     */
    private fun showUserAgreementDialog2(){
        if (isFinishing || isDestroyed) {
            return
        }
        if (userAgreementDialog2 == null) {
            userAgreementDialog2 = UserAgreementDialog2(this).apply {
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        dismiss()
                        finish()
                    }

                    override fun onRightButtonClick() {
                        dismiss()
                        finish()
                        PagePilotManager.pageLogin()
                    }
                }
            }
        }
        userAgreementDialog2?.show()
    }

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
                finish()
                PagePilotManager.pageLogin()
            }
        }
    }
}
