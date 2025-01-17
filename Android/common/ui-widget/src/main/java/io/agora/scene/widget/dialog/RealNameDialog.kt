package io.agora.scene.widget.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.scene.base.URLStatics
import io.agora.scene.base.api.RealNameViewModel
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager
import io.agora.scene.widget.R
import io.agora.scene.widget.databinding.DialogRealNameBinding
import io.agora.scene.widget.toast.CustomToast
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import com.google.android.material.textfield.TextInputLayout

@JvmOverloads
fun FragmentActivity.checkRealName(): Boolean {
    if (UserManager.getInstance().isRealNameAuth) {
        return true
    }
    RealNameDialog().show(this.supportFragmentManager, "RealNameDialog")
    return false
}

class RealNameDialog : BaseBottomSheetDialogFragment<DialogRealNameBinding>() {

    private var window: Window? = null
    private var loadingView: View? = null

    private val realNameViewModel: RealNameViewModel by lazy {
        ViewModelProvider(this)[RealNameViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        window = dialog.window
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPrivacyText()
        updateConfirmButtonState()

        mBinding.etIdNumber.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                source.filter { it.isDigit() || it.equals('x', true) }
            },
            LengthFilter(18)
        )

        mBinding.etRealname.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                source.filter { it.isChineseCharacter() }
            },
            LengthFilter(15)
        )

        mBinding.etRealname.doAfterTextChanged {
            updateConfirmButtonState()
        }

        mBinding.etIdNumber.doAfterTextChanged {
            updateConfirmButtonState()
        }

        mBinding.btnCancel.setOnClickListener {
            dismiss()
        }
        mBinding.btnConfirm.setOnClickListener {
            if (!mBinding.cvIAgree.isChecked) {
                CustomToast.show(R.string.comm_realname_review_and_check_the_agreement)
                return@setOnClickListener
            }

            val name = mBinding.etRealname.text.toString().trim()
            if (!isValidName(name)) {
                CustomToast.show(R.string.comm_realname_invalid_name)
                return@setOnClickListener
            }

            val idNumber = mBinding.etIdNumber.text.toString().trim()

            if (!isValidIdNumber(idNumber)) {
                CustomToast.show(R.string.comm_realname_invalid_id)
                return@setOnClickListener
            }
            showLoadingView()
            realNameAuth(name, idNumber)
        }
    }

    private fun realNameAuth(name: String, idNumber: String) {
        realNameViewModel.requestRealNameAuth(name, idNumber, completion = {
            if (it == null) {
                realNameViewModel.requestUserInfo(UserManager.getInstance().user.userNo, completion = {
                    hideLoadingView()
                    CustomToast.show(R.string.comm_realname_success)
                    dismiss()
                })
            } else {
                hideLoadingView()
                CustomToast.show(it.message ?: getString(R.string.comm_realname_error))
            }
        })
    }

    private fun setPrivacyText() {
        val privacyText = getString(R.string.comm_realname_privacy_policy_text)
        val spannableString = SpannableString(Html.fromHtml(privacyText))

        val userAgreementSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.blue_2e)
                ds.isUnderlineText = false
            }
        }

        spannableString.setSpan(userAgreementSpan, 7, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacyAgreementSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.userAgreementURL)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.blue_2e)
                ds.isUnderlineText = false
            }
        }

        spannableString.setSpan(privacyAgreementSpan, 14, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        mBinding.tvPrivacyAgreement.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun updateConfirmButtonState() {
        val name = mBinding.etRealname.text.toString().trim()
        val idNumber = mBinding.etIdNumber.text.toString().trim()

        val nameValid = name.length >= 2
        val idNumberValid = idNumber.length == 18

        mBinding.btnConfirm.isEnabled = nameValid && idNumberValid
        mBinding.btnConfirm.alpha = if (mBinding.btnConfirm.isEnabled) 1.0f else 0.6f
    }

    private fun isValidIdNumber(idNumber: String): Boolean {
        if (TextUtils.isEmpty(idNumber) || idNumber.length != 18) {
            return false
        }
        val idCardRegex = """^[1-6]\d{5}((?:19|20)\d{2})(0[1-9]|1[0-2])([0-2][1-9]|3[0-1])\d{3}(\d|X|x)$"""
        return idNumber.matches(Regex(idCardRegex))
    }

    private fun isValidName(name: String): Boolean {
        if (TextUtils.isEmpty(name) || name.length < 2) {
            return false
        }
        val nameRegex = """^[\u4e00-\u9fa5][\u4e00-\u9fa5·]*[\u4e00-\u9fa5]$"""
        val regex = Regex(nameRegex)
        return regex.matches(name)
    }

    private fun showLoadingView() {
        window?.apply {
            decorView.post { addLoadingView() }
            decorView.postDelayed({ hideLoadingView() }, 5000)
        }
    }

    private fun addLoadingView() {
        if (this.loadingView == null) {
            val rootView = window?.decorView?.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0) as ViewGroup
            this.loadingView =
                LayoutInflater.from(context).inflate(io.agora.scene.base.R.layout.view_base_loading, rootView, false)
            rootView.addView(this.loadingView, ViewGroup.LayoutParams(-1, -1))
        }
        this.loadingView?.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        if (loadingView == null) {
            return
        }
        window?.apply {
            decorView.post {
                loadingView?.visibility = View.GONE
            }
        }
    }

    private fun Char.isChineseCharacter(): Boolean {
        val unicodeBlock = Character.UnicodeBlock.of(this)
        return unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
    }

} 