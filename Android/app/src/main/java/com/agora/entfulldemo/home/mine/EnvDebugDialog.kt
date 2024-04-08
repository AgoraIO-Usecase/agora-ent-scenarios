package com.agora.entfulldemo.home.mine

import android.content.Context
import android.view.LayoutInflater
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppDialogDebugBinding
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.base.component.OnButtonClickListener


interface EnvDebugClickListener : OnButtonClickListener {
    fun onSwitchEnvClick(envRelease: Boolean)
}

class EnvDebugDialog constructor(context: Context) : BaseDialog<AppDialogDebugBinding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): AppDialogDebugBinding {
        return AppDialogDebugBinding.inflate(inflater)
    }

    var tempEnvRelease = true

    override fun initView() {
        tempEnvRelease = ServerConfig.envRelease
        if (tempEnvRelease) {
            binding.rgSwitchEnv.check(R.id.rbEnvRelease)
        } else {
            binding.rgSwitchEnv.check(R.id.rbEnvDev)
        }
        binding.rgSwitchEnv.setOnCheckedChangeListener { group, checkedId ->
            tempEnvRelease = checkedId == R.id.rbEnvRelease
        }
        binding.btnLeft.setOnClickListener {
            dismiss()
            onEnvDebugClickListener?.onLeftButtonClick()
        }
        binding.btnRight.setOnClickListener {
            dismiss()
            onEnvDebugClickListener?.onRightButtonClick()
        }
        binding.btnEnvSwitch.setOnClickListener {
            dismiss()
            onEnvDebugClickListener?.onSwitchEnvClick(tempEnvRelease)
        }
    }

    var onEnvDebugClickListener: EnvDebugClickListener? = null
}