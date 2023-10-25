package com.agora.entfulldemo.login

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentLoginVerifyBinding
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager

class LoginVerifyFragment : BaseViewBindingFragment<AppFragmentLoginVerifyBinding>() {

    private val mLoginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    private var mAreaCode = ""
    private var mAccounts = ""

    private var mCountDownTimerUtils: CountDownTimer? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentLoginVerifyBinding {
        return AppFragmentLoginVerifyBinding.inflate(inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("zhangw", "LoginVerifyFragment onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("zhangw", "LoginVerifyFragment onViewCreated")
        setOnApplyWindowInsetsListener(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context?.mainLooper?.queue?.addIdleHandler {
                Log.d("addIdleHandler", "showKeyboard -- queueIdle -- 1")
                binding.etCode.isFocusable = true
                binding.etCode.isFocusableInTouchMode = true
                binding.etCode.requestFocus()
                showKeyboard(binding.etCode)
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("zhangw", "LoginVerifyFragment onDestroyView")
        mLoginViewModel.clearDispose()
    }

    override fun initListener() {
        super.initListener()
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mCountDownTimerUtils?.cancel()
                mLoginViewModel.mRequestCodeLiveData.postValue(false)
                findNavController().popBackStack()
            }
        })
        binding.etCode.setOnTextChangeListener {
            if (it.length >= binding.etCode.textLength) {
                hideKeyboard(binding.etCode)
                mLoginViewModel.requestLogin(mAccounts, it)
            }
        }
        binding.btnBack.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                mCountDownTimerUtils?.cancel()
                mLoginViewModel.mRequestCodeLiveData.postValue(false)
                findNavController().popBackStack()
            }
        })
        binding.tvRegainCode.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                binding.tvCodeError.isVisible = false
                mLoginViewModel.requestSendVCode(mAccounts)
            }
        })
    }

    override fun requestData() {
        super.requestData()
        mAreaCode = arguments?.getString(LoginPhoneInputFragment.Key_Area_Code, "86") ?: ""
        mAccounts = arguments?.getString(LoginPhoneInputFragment.Key_Account, "") ?: ""
        binding.tvPageInfo.text = getString(R.string.app_login_phone_input_code_info, mAreaCode, mAccounts)
        mLoginViewModel.setPhone(mAccounts)
        mLoginViewModel.mRequestLoginLiveData.removeObservers(this)
        mLoginViewModel.mRequestLoginLiveData.observe(this) {
            if (it) {
                mCountDownTimerUtils?.cancel()
                activity?.finish()
                PagePilotManager.pageMainHome()
            } else {
                binding.tvCodeError.isVisible = true
                binding.etCode.setText("")
            }
        }
        mLoginViewModel.mRequestCodeLiveData.removeObservers(this)
        mLoginViewModel.mRequestCodeLiveData.observe(this) {
            if (it) {
                Log.d("zhangw", "LoginVerifyFragment mRequestCodeLiveData true")
                enableRegainCodeView(false)
                binding.tvCodeError.isVisible = false
                mCountDownTimerUtils?.cancel()
                mCountDownTimerUtils?.start()
            }
        }
        mCountDownTimerUtils = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountDown.text = getString(R.string.app_login_count_down, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                enableRegainCodeView(true)
                binding.tvCodeError.isVisible = false
            }

        }
        enableRegainCodeView(false)
        mCountDownTimerUtils?.start()
    }

    private fun enableRegainCodeView(enable: Boolean) {
        if (enable) {
            binding.tvRegainCode.isClickable = true
            binding.tvRegainCode.setTextColor(ResourcesCompat.getColor(resources, R.color.blue_2e, null))
            binding.tvCountDown.isVisible = false
        } else {
            binding.tvRegainCode.setTextColor(ResourcesCompat.getColor(resources, R.color.def_text_grey_979, null))
            binding.tvCountDown.isVisible = true
            binding.tvRegainCode.isClickable = false
        }
    }
}