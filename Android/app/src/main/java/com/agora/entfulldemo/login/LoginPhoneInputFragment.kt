package com.agora.entfulldemo.login;

import android.os.Bundle
import android.view.*
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentLoginPhoneInputBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.manager.PagePilotManager

class LoginPhoneInputFragment : BaseViewBindingFragment<AppFragmentLoginPhoneInputBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentLoginPhoneInputBinding {
        return AppFragmentLoginPhoneInputBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVerify.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.fragmentVerify)
        }

    }
}
