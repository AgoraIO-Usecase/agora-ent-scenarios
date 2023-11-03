package com.agora.entfulldemo.home.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
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
        binding.layoutLogoff.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                showLogoffAccountDialog()
            }
        })
        binding.btnLogout.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                showLogoutDialog()
            }
        })
        mainViewModel.setISingleCallback { type, data ->
            if (type == Constant.CALLBACK_TYPE_USER_LOGOUT) {
                finish()
                PagePilotManager.pageWelcomeClear()
            } else if (type == Constant.CALLBACK_TYPE_USER_LOGOFF) {
                finish()
                PagePilotManager.pageWelcomeClear()
            }
        }
    }

    // 注销账号
    private fun showLogoffAccountDialog() {
        if (logoffAccountDialog == null) {
            logoffAccountDialog = CommonDialog(this).apply {
                setDialogTitle(getString(R.string.app_logoff_account))
                setDescText(getString(R.string.app_logoff_account_tips))
                setDialogBtnText(getString(R.string.app_logoff_confirm), getString(R.string.app_logoff_cancel))
                setDialogLeftBtnTextColor(ResourcesCompat.getColor(resources, R.color.red_f3, null))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        mainViewModel.requestCancellation(UserManager.getInstance().user.userNo)
                    }

                    override fun onRightButtonClick() {
                        SPUtil.putBoolean(Constant.IS_AGREE, true)
                    }
                }
            }
        }
        logoffAccountDialog?.show()
    }

    //退出登录
    private fun showLogoutDialog() {
        if (logoutDialog == null) {
            logoutDialog = CommonDialog(this).apply {
                setDialogTitle(getString(R.string.app_logout_account))
                setDescText(getString(R.string.app_logout_account_tips))
                setDialogBtnText(getString(R.string.app_logout_cancel), getString(R.string.app_logout_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                    }

                    override fun onRightButtonClick() {
                        UserManager.getInstance().logout()
                        mainViewModel.iSingleCallback?.onSingleCallback(Constant.CALLBACK_TYPE_USER_LOGOUT, null)
                    }
                }
            }
        }
        logoutDialog?.show()
    }
}