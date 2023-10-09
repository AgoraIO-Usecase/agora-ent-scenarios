package com.agora.entfulldemo.welcome;

import android.content.Context;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.agora.entfulldemo.databinding.AppDialogUserAgreementBinding;
import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.widget.R;

public class UserAgreementDialog extends BaseDialog<AppDialogUserAgreementBinding> {
    public UserAgreementDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected AppDialogUserAgreementBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return AppDialogUserAgreementBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        setCancelable(false);
        getBinding().btnDisagree.setOnClickListener(view -> {
            getOnButtonClickListener().onLeftButtonClick();
        });

        ClickableSpan protocolClickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/service.html");
            }
        };
        ClickableSpan protocolClickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/privacy.html");
            }
        };
        ClickableSpan protocolClickableSpan3 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/service.html");
            }
        };
        ClickableSpan protocolClickableSpan4 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/privacy.html");
            }
        };

        ForegroundColorSpan spanColor = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_9F));
        SpannableStringBuilder spannable = new SpannableStringBuilder(getContext().getString(R.string.privacy_protection_tip1));
        spannable.setSpan();

        spannable.setSpan(protocolClickableSpan1, 72, 76, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(spanColor, 72, 76, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(protocolClickableSpan2, 79, 83, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(spanColor, 79, 83, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(protocolClickableSpan3, 202, 207, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(spanColor, 202, 207, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(protocolClickableSpan4, 210, 214, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannable.setSpan(spanColor, 210, 214, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        getBinding().tvProtection.setText(spannable);
        getBinding().btnAgree.setOnClickListener(view -> {
            getOnButtonClickListener().onRightButtonClick();
        });
        getBinding().tvProtection.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void setGravity() {
        Window window = getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
