package com.agora.entfulldemo.welcome

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppDialogUserAgreement2Binding
import com.agora.entfulldemo.home.constructor.URLStatics
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.UiUtil.*

class UserAgreementDialog2(context: Context) : BaseDialog<AppDialogUserAgreement2Binding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): AppDialogUserAgreement2Binding {
        return AppDialogUserAgreement2Binding.inflate(inflater)
    }

    override fun initView() {
        setCancelable(false)
        binding.root.setBackgroundResource(io.agora.scene.widget.R.drawable.bg_full_white_r16)
        binding.btnDisagree.setOnClickListener { view: View? -> getOnButtonClickListener().onLeftButtonClick() }
        binding.btnAgree.text = context.getString(io.agora.scene.widget.R.string.agree1)
        binding.btnDisagree.text =  context.getString(io.agora.scene.widget.R.string.disagree1)
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
        val spanColor = ForegroundColorSpan(ContextCompat.getColor(context, io.agora.scene.widget.R.color.blue_9F))
        val spannable = SpannableStringBuilder(context.getString(R.string.app_agreement_protection_alert))
        spannable.setSpan(protocolClickableSpan1, 3, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(spanColor, 3, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(protocolClickableSpan2, 10, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(spanColor, 10, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvProtection.setText(spannable)
        binding.btnAgree.setOnClickListener { view: View? -> getOnButtonClickListener().onRightButtonClick() }
        binding.tvProtection.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun setGravity() {
        window?.apply {
            setLayout(dp2px(320), dp2px(240))
//            attributes.horizontalMargin = 30.dp
            attributes.gravity = Gravity.CENTER
        }
    }
}