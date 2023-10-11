package com.agora.entfulldemo.welcome

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.view.*
import androidx.core.content.ContextCompat
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppDialogUserAgreementBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.base.manager.PagePilotManager

class UserAgreementDialog(context: Context) : BaseDialog<AppDialogUserAgreementBinding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): AppDialogUserAgreementBinding {
        return AppDialogUserAgreementBinding.inflate(inflater)
    }

    override fun initView() {
        setCancelable(false)
        binding.btnDisagree.setOnClickListener { view -> getOnButtonClickListener().onLeftButtonClick() }
        val spanColor = ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue_9F))
        // 1
        val spannable1 =
            SpannableStringBuilder(context.getString(R.string.app_agreement_protection_tip1))
        // 2
        val spannable2 =
            SpannableStringBuilder(context.getString(R.string.app_agreement_protection_tip2))
        val protocolClickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.userAgreementURL)
            }
        }
        val protocolClickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
            }
        }
        spannable2.setSpan(protocolClickableSpan1, 7, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(spanColor, 7, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(protocolClickableSpan2, 14, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(spanColor, 14, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(
            LeadingMarginSpan.Standard(0, 32),
            0,
            spannable2.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 3
        val spannable3 =
            SpannableStringBuilder(context.getString(R.string.app_agreement_protection_tip3))
        spannable3.setSpan(
            LeadingMarginSpan.Standard(0, 32),
            0,
            spannable3.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 4
        val spannable4 =
            SpannableStringBuilder(context.getString(R.string.app_agreement_protection_tip4))
        val protocolClickableSpan3: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.userAgreementURL)
            }
        }
        val protocolClickableSpan4: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
            }
        }
        spannable4.setSpan(protocolClickableSpan3, 9, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable4.setSpan(spanColor, 9, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable4.setSpan(protocolClickableSpan4, 16, 22, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable4.setSpan(spanColor, 16, 22, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        // 5
        val spannable5 =
            SpannableStringBuilder(context.getString(R.string.app_agreement_protection_tip5))
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable5.setSpan(boldSpan, 0, spannable5.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val finalText = TextUtils.concat(
            spannable1,
            "\n\n",
            spannable2,
            "\n",
            spannable3,
            "\n\n",
            spannable4,
            "\n\n",
            spannable5
        )
        binding.tvProtection.text = finalText
        binding.btnAgree.setOnClickListener { view -> getOnButtonClickListener().onRightButtonClick() }
        binding.tvProtection.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun setGravity() {
        window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            window.attributes.gravity = Gravity.CENTER
        }
    }
}