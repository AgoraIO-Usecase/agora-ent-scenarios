package com.agora.entfulldemo.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentLoginAdvertiseBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.manager.PagePilotManager

class LoginAdvertiseFragment: BaseViewBindingFragment<AppFragmentLoginAdvertiseBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentLoginAdvertiseBinding {
        return AppFragmentLoginAdvertiseBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.fragmentPhoneInput)
        }
        binding.cvIAgree.setOnClickListener {

        }
        binding.tvUserAgreement.setOnClickListener {
            PagePilotManager.pageWebView(URLStatics.userAgreementURL)
        }
        binding.tvPrivacyAgreement.setOnClickListener {
            PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
        }
    }
}