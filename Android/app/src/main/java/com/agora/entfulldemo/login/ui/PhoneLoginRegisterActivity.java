package com.agora.entfulldemo.login.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppActivityPhoneLoginBinding;
import com.agora.entfulldemo.login.LoginViewModel;
import com.alibaba.android.arouter.facade.annotation.Route;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.utils.CountDownTimerUtils;
import io.agora.scene.base.utils.StringUtils;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.widget.dialog.SwipeCaptchaDialog;

/**
 * 登录注册
 */
@Route(path = PagePathConstant.pagePhoneLoginRegister)
public class PhoneLoginRegisterActivity extends BaseViewBindingActivity<AppActivityPhoneLoginBinding> {

    /**
     * 登录模块统一ViewModel
     */
    private LoginViewModel phoneLoginViewModel;
    private SwipeCaptchaDialog swipeCaptchaDialog;

    /**
     * 记时器
     */
    private CountDownTimerUtils countDownTimerUtils;

    @Override
    protected AppActivityPhoneLoginBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return AppActivityPhoneLoginBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        phoneLoginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        phoneLoginViewModel.setLifecycleOwner(this);
        phoneLoginViewModel.setISingleCallback((var1, var2) -> {
            if (var1 == Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS) {
                // RoomManager.getInstance().loginOut();
                PagePilotManager.pageMainHome();
                finish();
            } else if (var1 == Constant.CALLBACK_TYPE_LOGIN_REQUEST_CODE_SUCCESS) {
                if (countDownTimerUtils != null) countDownTimerUtils.start();
            }
            hideLoadingView();
        });
        setAccountStatus();
//        String account = SPUtil.getString(KtvConstant.ACCOUNT, null);
//        if (!TextUtils.isEmpty(account)) {
//            getBinding().etAccounts.setText(account);
//            String password = SPUtil.getString(KtvConstant.V_CODE, null);
//            if (!TextUtils.isEmpty(password)) {
//                getBinding().etVCode.setText(password);
//            }
//        }
        countDownTimerUtils = new CountDownTimerUtils(getBinding().tvSendVCode, 60000, 1000);
    }

    /**
     * 设置帐号输入框输入状态
     */
    private void setAccountStatus() {
        //手机号登录
        getBinding().etAccounts.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
    }

    @Override
    protected boolean isCanExit() {
        return true;
    }

    @Override
    public void initListener() {
        getBinding().tvUserAgreement.setOnClickListener(view -> {
            PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/service.html");
        });
        getBinding().tvPrivacyAgreement.setOnClickListener(view -> {
            PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/privacy.html");
        });
        getBinding().btnLogin.setOnClickListener(view -> {
            if (getBinding().cvIAgree.isChecked()) {
                if (checkAccount()) {
                    showSwipeCaptchaDialog();
                }
            } else {
                ToastUtils.showToast(R.string.app_agree_tip);
            }
        });
        getBinding().tvSendVCode.setOnClickListener(view -> {
            String account = getBinding().etAccounts.getText().toString();
            if (!StringUtils.checkPhoneNum(account)) {
                ToastUtils.showToast(R.string.app_input_phonenum_tip);
            } else {
                phoneLoginViewModel.requestSendVCode(account);
            }
        });
        getBinding().iBtnClearAccount.setOnClickListener(view -> {
            getBinding().etAccounts.setText("");
        });
        getBinding().etAccounts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.length() > 0) {
                    getBinding().iBtnClearAccount.setVisibility(View.VISIBLE);
                } else {
                    getBinding().iBtnClearAccount.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showSwipeCaptchaDialog() {
        if (swipeCaptchaDialog == null) {
            swipeCaptchaDialog = new SwipeCaptchaDialog(this);
            swipeCaptchaDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {

                }

                @Override
                public void onRightButtonClick() {
                    //验证成功
                    //showLoadingView();
                    String account = getBinding().etAccounts.getText().toString();
                    String vCode = getBinding().etVCode.getText().toString();
                    phoneLoginViewModel.requestLogin(account, vCode);
//                    SPUtil.putString(KtvConstant.ACCOUNT, account);
                }
            });
        }
        swipeCaptchaDialog.show();

    }

    private boolean checkAccount() {
        String account = getBinding().etAccounts.getText().toString();
        if (!StringUtils.checkPhoneNum(account)) {
            ToastUtils.showToast(R.string.app_input_phonenum_tip);
            return false;
        } else if (TextUtils.isEmpty(getBinding().etVCode.getText().toString())) {
            ToastUtils.showToast(R.string.app_input_vcode_tip);
        }
        return true;
    }
}
