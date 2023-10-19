package com.agora.entfulldemo.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentLoginAdvertiseBinding
import com.agora.entfulldemo.databinding.AppItemGuideLayoutBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import com.agora.entfulldemo.login.constructor.AdvertiseConstructor
import com.agora.entfulldemo.login.constructor.AdvertiseModel
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager

class LoginAdvertiseFragment : BaseViewBindingFragment<AppFragmentLoginAdvertiseBinding>() {

    private val mCloseAgreementTipsTask: Runnable = Runnable {
        binding.tvAgreementTips.isVisible = false
    }

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentLoginAdvertiseBinding {
        return AppFragmentLoginAdvertiseBinding.inflate(inflater)
    }

    override fun initListener() {
        super.initListener()
        binding.btnLogin.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                findNavController().navigate(R.id.action_fragmentAdvertise_to_fragmentPhoneInput)
            }
        })
        binding.tvUserAgreement.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.userAgreementURL)
            }
        })
        binding.tvPrivacyAgreement.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
            }
        })
        binding.cvIAgree.setOnCheckedChangeListener { _, isChecked ->
            mMainHandler.removeCallbacks(mCloseAgreementTipsTask)
            if (isChecked) {
                binding.btnLogin.isEnabled = true
                binding.tvAgreementTips.isVisible = false
                binding.btnLogin.alpha = 1.0f
            } else {
                binding.btnLogin.isEnabled = false
                binding.tvAgreementTips.isVisible = true
                binding.btnLogin.alpha = 0.6f
                mMainHandler.postDelayed(mCloseAgreementTipsTask, 3000L)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnApplyWindowInsetsListener(binding.root)
        val advertiseModels = AdvertiseConstructor.buildData()
        val mAdvertiseAdapter: BaseRecyclerViewAdapter<AppItemGuideLayoutBinding, AdvertiseModel, AdvertiseHolder> =
            BaseRecyclerViewAdapter(advertiseModels, AdvertiseHolder::class.java)
        binding.vpGuide.adapter = mAdvertiseAdapter
        binding.dotIndicator.setViewPager2(binding.vpGuide)
    }

    override fun onResume() {
        super.onResume()
        binding.dotIndicator.refreshDots()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMainHandler.removeCallbacks(mCloseAgreementTipsTask)
    }
}

class AdvertiseHolder constructor(mBinding: AppItemGuideLayoutBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemGuideLayoutBinding, AdvertiseModel>(mBinding) {
    override fun binding(
        data: AdvertiseModel?,
        selectedIndex: Int
    ) {
        data ?: return
        mBinding.ivGuide.setImageResource(data.drawableId)
        mBinding.tvGuideTitle.setText(data.titleId)
        mBinding.tvGuideIntroduce.setText(data.contentId)
    }
}