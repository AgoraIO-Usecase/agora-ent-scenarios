package com.agora.entfulldemo.home.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityMineAccountBinding
import com.agora.entfulldemo.home.MainViewModel
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.Constant
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.widget.dialog.CommonDialog

@Route(path = PagePathConstant.pageMineAccount)
class MineAccountActivity : BaseViewBindingActivity<AppActivityMineAccountBinding>() {


    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private var logoutDialog: CommonDialog? = null
    private var logoffAccountDialog: CommonDialog? = null
    override fun getViewBinding(inflater: LayoutInflater): AppActivityMineAccountBinding {
        return AppActivityMineAccountBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
        binding.layoutLogoff.setOnClickListener(object :OnFastClickListener(){
            override fun onClickJacking(view: View) {
                showLogoffAccountDialog()
            }
        })
        binding.btnLogout.setOnClickListener(object :OnFastClickListener(){
            override fun onClickJacking(view: View) {
                showLogoutDialog()
            }
        })
    }

    private fun showLogoffAccountDialog() {
        if (logoffAccountDialog == null) {
            logoffAccountDialog = CommonDialog(this)
            logoffAccountDialog?.setDialogTitle("确定注销账号？")
            logoffAccountDialog?.setDescText("注销账号后，您将暂时无法使用该账号体验我们的服务，真的要注销吗？")
            logoffAccountDialog?.setDialogBtnText(getString(R.string.app_logoff), getString(R.string.cancel))
            logoffAccountDialog?.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {
                    SPUtil.putBoolean(Constant.IS_AGREE, false)
                    mainViewModel.requestCancellation(UserManager.getInstance().user.userNo)
                }

                override fun onRightButtonClick() {
                    SPUtil.putBoolean(Constant.IS_AGREE, true)
                }
            }
        }
        logoffAccountDialog?.show()
    }

    private fun showLogoutDialog() {
        if (logoutDialog == null) {
            logoutDialog = CommonDialog(this)
            logoutDialog?.setDialogTitle("确定退出登录吗？")
            logoutDialog?.setDescText("退出登录后，我们还会继续保留您的账户数据，记得再来体验哦～")
            logoutDialog?.setDialogBtnText(getString(R.string.app_exit), getString(R.string.cancel))
            logoutDialog?.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {
                    SPUtil.putBoolean(Constant.IS_AGREE, false)
                    UserManager.getInstance().logout()
                    finish()
                    PagePilotManager.pageWelcomeClear()
                }

                override fun onRightButtonClick() {}
            }
        }
        logoutDialog?.show()
    }
}