package io.agora.scene.widget.dialog;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.widget.R;
import io.agora.scene.widget.databinding.DialogUserAgreementBinding;

public class UserAgreementDialog extends BaseDialog<DialogUserAgreementBinding> {
    public UserAgreementDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected DialogUserAgreementBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return DialogUserAgreementBinding.inflate(inflater);
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
//        getWindow().setLayout(
//                UiUtil.dp2px(280),
//                UiUtil.dp2px(365)
//        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
