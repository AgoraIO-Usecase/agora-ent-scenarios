package io.agora.scene.aichat.create

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatCreateAgentDialogBinding
import io.agora.scene.aichat.list.logic.AIUserViewModel
import io.agora.scene.base.component.BaseBottomFullDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.dp
import io.agora.scene.widget.toast.CustomToast
import kotlin.random.Random

/**
 * Ai chat create agent dialog
 *
 * @constructor Create empty Ai chat create agent dialog
 */
class AIChatCreateAgentDialog constructor(
    private val createCount: Int
) : BaseBottomFullDialogFragment<AichatCreateAgentDialogBinding>() {

    //viewModel
    private val aiUserViewModel: AIUserViewModel by viewModels()

    private val kNameMaxLength = 32
    private val kBriefMaxLength = 32
    private val kDescriptionMaxLength = 1024
    private var onClickSubmit: ((String, String, String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false) // 禁用点击外部关闭对话框

        dialog.setOnKeyListener { _, keyCode, event ->
            // 屏蔽返回键
            keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        }
        return dialog
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatCreateAgentDialogBinding {
        return AichatCreateAgentDialogBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            ivBackIcon.setOnClickListener {
                dismiss()
            }
            ivAichatCreateAvatar.setOnClickListener {
                onClickExchangeAvatar()
            }
            btnAichatCreate.setOnClickListener {
                onClickCreate()
            }
            val createInfo = getString(R.string.aichat_create_count) + "$createCount/3"
            tvAichatCreateInfo.text = createInfo
            val nameCountStr = "0/$kNameMaxLength"
            tvAichatCreateNameCount.text = nameCountStr
            val briefCountStr = "0/$kBriefMaxLength"
            tvAichatCreateBriefCount.text = briefCountStr
            val descriptionCountStr = "0/$kDescriptionMaxLength"
            tvAichatCreateDescriptionCount.text = descriptionCountStr
            etAichatCreateName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.length > kNameMaxLength) {
                        s.delete(kNameMaxLength, s.length)
                        etAichatCreateName.setSelection(kNameMaxLength)
                    }
                    val countStr = s.length.toString() + "/" + kNameMaxLength.toString()
                    tvAichatCreateNameCount.text = countStr
                }
            })
            etAichatCreateBrief.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.length > kBriefMaxLength) {
                        s.delete(kBriefMaxLength, s.length)
                        etAichatCreateBrief.setSelection(kBriefMaxLength)
                    }
                    val countStr = s.length.toString() + "/" + kBriefMaxLength.toString()
                    tvAichatCreateBriefCount.text = countStr
                }
            })
            etAichatCreateDescription.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.length > kDescriptionMaxLength) {
                        s.delete(kDescriptionMaxLength, s.length)
                        etAichatCreateDescription.setSelection(kDescriptionMaxLength)
                    }
                    val countStr = s.length.toString() + "/" + kDescriptionMaxLength.toString()
                    tvAichatCreateDescriptionCount.text = countStr
                }
            })
            etAichatCreateDescription.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    val layoutParams = mBinding?.vAichatCreateBottom?.layoutParams
                    layoutParams?.height = 200.dp.toInt()
                    mBinding?.vAichatCreateBottom?.layoutParams = layoutParams
                    Handler(Looper.getMainLooper()).postDelayed({
                        svAichatCreate.fullScroll(View.FOCUS_DOWN)
                    }, 300)
                } else {
                    val layoutParams = mBinding?.vAichatCreateBottom?.layoutParams
                    layoutParams?.height = 40.dp.toInt()
                    mBinding?.vAichatCreateBottom?.layoutParams = layoutParams
                    Handler(Looper.getMainLooper()).postDelayed({
                        svAichatCreate.fullScroll(View.FOCUS_UP)
                    }, 300)
                }
            }
            etAichatCreateDescription.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
            svAichatCreate.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
        }
        activity?.window?.let { window ->
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            mBinding?.root?.viewTreeObserver?.addOnGlobalLayoutListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val currentWindowHeight =
                        Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                    if (currentWindowHeight < initialWindowHeight) {
                    } else {
                        mBinding?.etAichatCreateName?.clearFocus()
                        mBinding?.etAichatCreateBrief?.clearFocus()
                        mBinding?.etAichatCreateDescription?.clearFocus()
                    }
                }, 300)
            }
        }

        onViewModelObserve()
    }

    private fun onViewModelObserve() {
        aiUserViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        aiUserViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }
        aiUserViewModel.createAgentLiveData.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()){
                CustomToast.show("创建智能体成功 $it")
                dismiss()
            }
        }
    }

    fun setOnClickSubmit(listener: ((String, String, String) -> Unit)?) {
        onClickSubmit = listener
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
            mBinding?.ivAichatCreateAvatar?.let {
                Glide.with(context).load(drawable).into(it)
            }
        }
    }

    private fun onClickCreate() {
        val name = mBinding?.etAichatCreateName?.text.toString()
        val brief = mBinding?.etAichatCreateBrief?.text.toString()
        val description = mBinding?.etAichatCreateDescription?.text.toString()
        if (name.isEmpty() || brief.isEmpty() || description.isEmpty()) {
            ToastUtils.showToast(R.string.aichat_create_submit_toast)
            return
        }
        onClickSubmit?.invoke(name, brief, description)
        val avatarUrl = "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/aichat/avatar/assistant_avatar.png"
        aiUserViewModel.createAgent(avatarUrl, name, brief, description)
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = mBinding?.root
        if (v != null) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}