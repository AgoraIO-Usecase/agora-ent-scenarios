package com.agora.entfulldemo.welcome;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.agora.entfulldemo.databinding.AppActivityWelcomeBinding;
import com.alibaba.android.arouter.facade.annotation.Route;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.SPUtil;

@Route(path = PagePathConstant.pageWelcome)
public class WelcomeActivity extends BaseViewBindingActivity<AppActivityWelcomeBinding> {
    private UserAgreementDialog userAgreementDialog;
    private UserAgreementDialog2 userAgreementDialog2;

    @Override
    protected AppActivityWelcomeBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return AppActivityWelcomeBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        getBinding().ivAppLogo.postDelayed(this::checkStatusToStart, 500);

    }

    @Override
    public void initListener() {
    }

    @Override
    public boolean isBlackDarkStatus() {
        return false;
    }

    /**
     * 显示用户协议 隐私政策对话框
     */
    private void showUserAgreementDialog() {
        if (userAgreementDialog == null) {
            userAgreementDialog = new UserAgreementDialog(this);
            userAgreementDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    showUserAgreementDialog2();
                    userAgreementDialog.dismiss();
                }

                @Override
                public void onRightButtonClick() {
                    PagePilotManager.pagePhoneLoginRegister();
                    userAgreementDialog.dismiss();
                    finish();
                }
            });
        }
        userAgreementDialog.show();
    }

    /**
     * 显示用户协议 隐私政策对话框
     */
    private void showUserAgreementDialog2() {
        if (userAgreementDialog2 == null) {
            userAgreementDialog2 = new UserAgreementDialog2(this);
            userAgreementDialog2.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    userAgreementDialog2.dismiss();
                    finish();
                }

                @Override
                public void onRightButtonClick() {
                    PagePilotManager.pagePhoneLoginRegister();
                    userAgreementDialog2.dismiss();
                    finish();
                }
            });
        }
        userAgreementDialog2.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void getPermissions() {

    }

    private void checkStatusToStart() {
        startMainActivity();
    }

    private void startMainActivity() {
        if (UserManager.getInstance().isLogin()) {
            PagePilotManager.pageMainHome();
            finish();
        } else {
            if (!SPUtil.getBoolean(Constant.IS_AGREE, false)) {
                showUserAgreementDialog();
            } else {
                PagePilotManager.pagePhoneLoginRegister();
                finish();
            }
        }
    }
}
