package io.agora.scene.aichat.create

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.AiChatActivity
import io.agora.scene.aichat.create.logic.AiChatAgentCreateViewModel
import io.agora.scene.aichat.databinding.AichatFragmentCreateAgentBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.mainScope
import io.agora.scene.aichat.imkit.EaseFlowBus
import io.agora.scene.aichat.imkit.model.EaseEvent
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class AiChatAgentCreateFragment : BaseViewBindingFragment<AichatFragmentCreateAgentBinding>() {
    companion object {
        private const val kNameMaxLength = 32
        private const val kBriefMaxLength = 32
        private const val kDescriptionMaxLength = 512

        const val maxCreateCount = 3
        const val maxCreateCountStr = "三"
    }

    //viewModel
    private val aiAgentViewModel: AiChatAgentCreateViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // nothing 屏蔽返回键
            }
        })
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatFragmentCreateAgentBinding {
        return AichatFragmentCreateAgentBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.ivBackIcon.setOnClickListener {
            activity?.finish()
        }
        binding.cvAichatCreate.setOnClickListener {
            onClickCreate()
        }

        val nameCountStr = "0/$kNameMaxLength"
        binding.tvAichatCreateNameCount.text = nameCountStr
        val briefCountStr = "0/$kBriefMaxLength"
        binding.tvAichatCreateBriefCount.text = briefCountStr
        val descriptionCountStr = "0/$kDescriptionMaxLength"
        binding.tvAichatCreateDescriptionCount.text = descriptionCountStr
        binding.etAichatCreateName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length > kNameMaxLength) {
                    s.delete(kNameMaxLength, s.length)
                    binding.etAichatCreateName.setSelection(kNameMaxLength)
                }
                val countStr = s.length.toString() + "/" + kNameMaxLength.toString()
                binding.tvAichatCreateNameCount.text = countStr
            }
        })
        binding.etAichatCreateBrief.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length > kBriefMaxLength) {
                    s.delete(kBriefMaxLength, s.length)
                    binding.etAichatCreateBrief.setSelection(kBriefMaxLength)
                }
                val countStr = s.length.toString() + "/" + kBriefMaxLength.toString()
                binding.tvAichatCreateBriefCount.text = countStr
            }
        })
        binding.etAichatCreateDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length > kDescriptionMaxLength) {
                    s.delete(kDescriptionMaxLength, s.length)
                    binding.etAichatCreateDescription.setSelection(kDescriptionMaxLength)
                }
                val countStr = s.length.toString() + "/" + kDescriptionMaxLength.toString()
                binding.tvAichatCreateDescriptionCount.text = countStr
            }
        })
        binding.etAichatCreateDescription.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val layoutParams = binding?.vAichatCreateBottom?.layoutParams
                layoutParams?.height = 200.dp.toInt()
                binding?.vAichatCreateBottom?.layoutParams = layoutParams
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    if (isActive) {
                        binding.svAichatCreate.fullScroll(View.FOCUS_DOWN)
                    }
                }
            } else {
                val layoutParams = binding?.vAichatCreateBottom?.layoutParams
                layoutParams?.height = 40.dp.toInt()
                binding?.vAichatCreateBottom?.layoutParams = layoutParams
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    if (isActive) {
                        binding.svAichatCreate.fullScroll(View.FOCUS_UP)
                    }
                }
            }
        }
        binding.etAichatCreateDescription.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.svAichatCreate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
        binding.cvAichatCreateExchange.setOnClickListener {
            aiAgentViewModel.randomAvatar()
        }
        binding.ivAichatCreateAvatar.setOnClickListener {
            findNavController().navigate(AiChatAgentCreateActivity.PREVIEW_TYPE)
        }
        activity?.window?.let { window ->
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            binding?.root?.viewTreeObserver?.addOnGlobalLayoutListener {
                if(isRemoving) return@addOnGlobalLayoutListener
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    if (isActive) {
                        val currentWindowHeight =
                            Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                        if (currentWindowHeight >= initialWindowHeight) {
                            binding?.etAichatCreateName?.clearFocus()
                            binding?.etAichatCreateBrief?.clearFocus()
                            binding?.etAichatCreateDescription?.clearFocus()
                        }
                    }
                }
            }
        }
    }

    override fun initListener() {
        aiAgentViewModel.loadingChange.showDialog.observe(viewLifecycleOwner) {
            showLoadingView()
        }
        aiAgentViewModel.loadingChange.dismissDialog.observe(viewLifecycleOwner) {
            hideLoadingView()
        }
        aiAgentViewModel.createAgentLiveData.observe(viewLifecycleOwner) { agentUsername ->
            if (!agentUsername.isNullOrEmpty()) {
                activity?.let { activity ->
                    // 发送事件通知添加好友
                    EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.ADD.name)
                        .post(activity.mainScope(), EaseEvent(EaseEvent.EVENT.ADD.name, EaseEvent.TYPE.CONTACT))

                    AiChatActivity.start(activity, agentUsername)
                    activity.finish()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                aiAgentViewModel.curPreviewAvatar.collectLatest { preview ->
                    binding.ivAichatCreateAvatar.loadCircleImage(preview.avatar)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                aiAgentViewModel.mineCreateAgentLiveData.collectLatest { count ->
                    val createInfo = getString(R.string.aichat_create_count) + "${count + 1}/$maxCreateCount"
                    binding.tvAichatCreateInfo.text = createInfo
                    val isEnable = count < maxCreateCount
                    binding.cvAichatCreate.isEnabled = isEnable
                    binding.cvAichatCreate.alpha = if (isEnable) 1f else 0.3f
                    if (!isEnable) {
                        CustomToast.show(getString(R.string.aichat_create_agent_limit, maxCreateCount))
                    }
                }
            }
        }
    }

    private fun onClickCreate() {
        val name = binding?.etAichatCreateName?.text.toString()
        val brief = binding?.etAichatCreateBrief?.text.toString()
        val description = binding?.etAichatCreateDescription?.text.toString()
        if (name.isEmpty() || brief.isEmpty() || description.isEmpty()) {
            CustomToast.show(R.string.aichat_create_submit_toast)
            return
        }
        val previewAvatar = aiAgentViewModel.curPreviewAvatar.value
        aiAgentViewModel.createAgent(previewAvatar, name, brief, description)
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = binding?.root
        if (v != null) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}