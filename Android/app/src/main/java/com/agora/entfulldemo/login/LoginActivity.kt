package com.agora.entfulldemo.login

import android.view.LayoutInflater
import com.agora.entfulldemo.databinding.AppActivityLoginBinding
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseViewBindingActivity

@Route(path = PagePathConstant.pageLogin)
class LoginActivity: BaseViewBindingActivity<AppActivityLoginBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): AppActivityLoginBinding {
        return AppActivityLoginBinding.inflate(inflater)
    }

}