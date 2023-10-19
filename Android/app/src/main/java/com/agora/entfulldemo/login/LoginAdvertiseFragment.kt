package com.agora.entfulldemo.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentLoginAdvertiseBinding
import com.agora.entfulldemo.databinding.AppItemGuideLayoutBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import com.agora.entfulldemo.login.constructor.AdvertiseModel
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.ToastUtils
import java.util.Timer
import java.util.TimerTask

class LoginAdvertiseFragment : BaseViewBindingFragment<AppFragmentLoginAdvertiseBinding>() {

    private val mCloseAgreementTipsTask: Runnable = Runnable {
        binding.tvAgreementTips.isVisible = false
    }

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val mAdvertiseModels: List<AdvertiseModel> by lazy {
        mutableListOf(
            AdvertiseModel(
                R.drawable.app_guide_live,
                R.string.app_guide_live,
                R.string.app_guide_live_introduce
            ),
            AdvertiseModel(
                R.drawable.app_guide_chatroom,
                R.string.app_guide_chatroom,
                R.string.app_guide_chatroom_introduce
            ),
            AdvertiseModel(
                R.drawable.app_guide_ktv,
                R.string.app_guide_ktv,
                R.string.app_guide_ktv_introduce
            ),
            AdvertiseModel(
                R.drawable.app_guide_live,
                R.string.app_guide_live,
                R.string.app_guide_live_introduce
            ),
            AdvertiseModel(
                R.drawable.app_guide_chatroom,
                R.string.app_guide_chatroom,
                R.string.app_guide_chatroom_introduce
            ),
        )
    }

    private var mCurrentPos = 1
    private var mTimer: Timer? = null

    private val mAdvertiseAdapter by lazy {
        AdvertiseAdapter(mAdvertiseModels, itemClick = {
            ToastUtils.showToast("click $it")
        })
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
        binding.cvIAgree.isChecked = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnApplyWindowInsetsListener(binding.root)
        binding.vpGuide.adapter = mAdvertiseAdapter
        binding.dotIndicator.setViewPager2(binding.vpGuide, true)
        binding.vpGuide.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mCurrentPos = position
                Log.e("zhangw", "onPageSelected-1: pos:$position")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("zhangw", "onPageScrollStateChanged-3: state:$state")
                //只有在空闲状态，才让自动滚动
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (mCurrentPos == 0) {
                        binding.vpGuide.setCurrentItem(mAdvertiseModels.size - 2, false)
                    }
                    if (mCurrentPos == mAdvertiseModels.size - 1) {
                        binding.vpGuide.setCurrentItem(1, false)
                    }
                }
            }
        })
        binding.vpGuide.getChildAt(0).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> stopAutoScroll()
                MotionEvent.ACTION_UP -> startAutoScroll()
            }
            return@setOnTouchListener false
        }
        binding.vpGuide.currentItem = 1
        startAutoScroll()
    }

    private fun startAutoScroll() {
        mTimer?.cancel()
        mTimer = Timer()
        mTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mMainHandler.post {
                    binding.vpGuide.apply {
                        if (currentItem + 1 == mAdvertiseAdapter.itemCount - 1) {
                            setCurrentItem(1, false)
                        } else if (currentItem + 1 < mAdvertiseAdapter.itemCount - 1) {
                            setCurrentItem(currentItem + 1, false)
                        }
                    }
                }
            }
        }, 3000, 3000)
    }

    private fun stopAutoScroll() {
        mTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        binding.dotIndicator.refreshDots()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mTimer?.cancel()
        mMainHandler.removeCallbacks(mCloseAgreementTipsTask)
    }
}

class AdvertiseAdapter constructor(
    private val advertiseModels: List<AdvertiseModel>,
    private val itemClick: (position: Int) -> Unit
) :
    RecyclerView.Adapter<AdvertiseAdapter.AdvertiseViewHolder>() {

    inner class AdvertiseViewHolder(val binding: AppItemGuideLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertiseViewHolder {
        return AdvertiseViewHolder(
            AppItemGuideLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return advertiseModels.size
    }

    override fun onBindViewHolder(holder: AdvertiseViewHolder, position: Int) {
        val data = advertiseModels[position]
        holder.binding.ivGuide.setImageResource(data.drawableId)
        holder.binding.tvGuideTitle.setText(data.titleId)
        holder.binding.tvGuideIntroduce.setText(data.contentId)
        holder.binding.root.setOnClickListener {
            itemClick.invoke(position)
        }
    }
}

class CustomPageTransformer : ViewPager2.PageTransformer {
    private val minScale = 0.85f
    private val minAlpha = 0.5f

    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        page.apply {
            val scaleFactor = minScale.coerceAtLeast(1 - kotlin.math.abs(position))
            val verticalMargin = pageHeight * (1 - scaleFactor) / 2
            val horizontalMargin = pageWidth * (1 - scaleFactor) / 2

            translationX = if (position < 0) {
                horizontalMargin - verticalMargin / 2
            } else {
                -horizontalMargin + verticalMargin / 2
            }

            scaleX = scaleFactor
            scaleY = scaleFactor

            alpha = minAlpha + (scaleFactor - minScale) / (1 - minScale) * (1 - minAlpha)
        }
    }
}