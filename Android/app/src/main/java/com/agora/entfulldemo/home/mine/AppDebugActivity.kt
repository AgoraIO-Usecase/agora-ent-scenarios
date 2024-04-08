package com.agora.entfulldemo.home.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityDebugModeBinding
import com.agora.entfulldemo.home.MainActivity
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.dialog.CommonDialog

class AppDebugActivity : BaseViewBindingActivity<AppActivityDebugModeBinding>() {

    companion object {
        fun startActivity(context: Activity) {
            val intent = Intent(context, AppDebugActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var tempEnvRelease = true

    private var debugModeDialog: CommonDialog? = null
    override fun getViewBinding(inflater: LayoutInflater): AppActivityDebugModeBinding {
        return AppActivityDebugModeBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
        tempEnvRelease = ServerConfig.envRelease
        if (tempEnvRelease) {
            binding.rgSwitchEnv.check(R.id.rbEnvRelease)
        } else {
            binding.rgSwitchEnv.check(R.id.rbEnvDev)
        }
        binding.rgSwitchEnv.setOnCheckedChangeListener { group, checkedId ->
            tempEnvRelease = checkedId == R.id.rbEnvRelease
        }
        binding.btnEnvSwitch.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                ServerConfig.envRelease = tempEnvRelease
                MainActivity.startActivity(this@AppDebugActivity, MainActivity.PARAMS_EXIT)
            }
        })
        binding.btnExitDebug.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                showDebugModeCloseDialog()
            }
        })
    }

    private fun showDebugModeCloseDialog() {
        if (debugModeDialog == null) {
            debugModeDialog = CommonDialog(this)
            debugModeDialog?.setDialogTitle(getString(R.string.app_exit_debug_title))
            debugModeDialog?.setDescText(getString(R.string.app_exit_debug_tip))
            debugModeDialog?.setDialogBtnText(getString(R.string.cancel), getString(R.string.app_exit))
            debugModeDialog?.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {}
                override fun onRightButtonClick() {
                    AgoraApplication.the().enableDebugMode(false)
                    ToastUtils.showToast(R.string.app_debug_off)
                    finish()
                }
            }
        }
        debugModeDialog?.show()
    }
}