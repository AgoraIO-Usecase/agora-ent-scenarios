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
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

class AiChatAgentCreateFragment : BaseViewBindingFragment<AichatFragmentCreateAgentBinding>() {
    companion object {
        private const val kNameMaxLength = 32
        private const val kBriefMaxLength = 32
        private const val kDescriptionMaxLength = 1024

        private const val maxCreateCount = 3
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
//        ViewCompat.setOnApplyWindowInsetsListener(binding.ivBackIcon) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(
//                binding.ivBackIcon.left,
//                binding.ivBackIcon.top + systemBars.top,
//                binding.ivBackIcon.right,
//                binding.ivBackIcon.bottom
//            )
//            insets
//        }

        binding.ivBackIcon.setOnClickListener {
            activity?.finish()
        }
        binding.ivAichatCreateAvatar.setOnClickListener {
            onClickExchangeAvatar()
        }
        binding.btnAichatCreate.setOnClickListener {
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
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.svAichatCreate.fullScroll(View.FOCUS_DOWN)
                }, 300)
            } else {
                val layoutParams = binding?.vAichatCreateBottom?.layoutParams
                layoutParams?.height = 40.dp.toInt()
                binding?.vAichatCreateBottom?.layoutParams = layoutParams
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.svAichatCreate.fullScroll(View.FOCUS_UP)
                }, 300)
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
                Handler(Looper.getMainLooper()).postDelayed({
                    val currentWindowHeight =
                        Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                    if (currentWindowHeight < initialWindowHeight) {
                    } else {
                        binding?.etAichatCreateName?.clearFocus()
                        binding?.etAichatCreateBrief?.clearFocus()
                        binding?.etAichatCreateDescription?.clearFocus()
                    }
                }, 300)
            }
        }
    }

    override fun initListener() {
        aiAgentViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        aiAgentViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }
        aiAgentViewModel.createAgentLiveData.observe(viewLifecycleOwner) { agentUsername ->
            if (!agentUsername.isNullOrEmpty()) {
                activity?.let { activity ->
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
                    val createInfo = getString(R.string.aichat_create_count) + "$count/$maxCreateCount"
                    binding.tvAichatCreateInfo.text = createInfo
                }
            }
        }
        aiAgentViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        aiAgentViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }
    }

    private var avatarIndex = 1
    private fun onClickExchangeAvatar() {
        val randomInt = Random.nextInt(1, 3)
        if (randomInt == avatarIndex) {
            onClickExchangeAvatar()
            return
        }
        avatarIndex = randomInt
        context?.let { context ->
            var resourceId: Int
            try {
                val resourceName = "aichat_agent_avatar_$randomInt"
                resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            } catch (e: Exception) {
                resourceId = R.drawable.aichat_agent_avatar_1
            }
            val drawable = ContextCompat.getDrawable(context, resourceId)
            binding?.ivAichatCreateAvatar?.let {
                Glide.with(context).load(drawable).into(it)
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