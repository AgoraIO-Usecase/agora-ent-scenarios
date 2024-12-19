package io.agora.scene.widget.dialog

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.TextPaint
import android.view.View
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

@JvmOverloads
fun FragmentActivity.checkRealName(): Boolean {
    if (UserManager.getInstance().isRealNameAuth) {
        return true
    }
    RealNameDialog().show(this.supportFragmentManager, "RealNameDialog")
    return false
}

class RealNameDialog : BaseBottomSheetDialogFragment<DialogRealNameBinding>() {

    private val realNameViewModel: RealNameViewModel by lazy {
        ViewModelProvider(this)[RealNameViewModel::class.java]
    }

    private var onConfirmClick: ((name: String, idNumber: String) -> Unit)? = null

    fun setOnConfirmClickListener(listener: (name: String, idNumber: String) -> Unit) {
        onConfirmClick = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPrivacyText()
        updateConfirmButtonState()
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
            onConfirmClick?.invoke(name, idNumber)

            realNameAuth(name, idNumber)
        }
    }

    private fun realNameAuth(name: String, idNumber: String) {
        realNameViewModel.requestRealNameAuth(name, idNumber, completion = {
            if (it == null) {
                realNameViewModel.requestUserInfo(UserManager.getInstance().user.userNo, completion = {
                    CustomToast.show(R.string.comm_realname_success)
                    dismiss()
                })
            } else {
                CustomToast.show(it.message ?: getString(R.string.comm_realname_error))
                dismiss()
            }
        })
    }

    private fun setPrivacyText() {
        val privacyText = getString(R.string.comm_realname_privacy_policy_text)
        val spannableString = SpannableString(Html.fromHtml(privacyText))

        val userAgreementSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.userAgreementURL)
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
                PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
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

        mBinding.btnConfirm.isEnabled = name.isNotEmpty() && idNumber.isNotEmpty()
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
} 