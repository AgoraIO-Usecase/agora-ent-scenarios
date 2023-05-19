package com.agora.entfulldemo.webview;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppActivityWebviewBinding;
import com.agora.entfulldemo.webview.constructor.DeviceInfo;
import com.agora.entfulldemo.webview.constructor.UserModel;
import com.agora.entfulldemo.webview.constructor.WebUsage;
import com.agora.entfulldemo.webview.constructor.WebUsageModel;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.manager.UserManager;
import kotlin.jvm.JvmField;

@Route(path = PagePathConstant.pageWebView)
public class WebViewActivity extends BaseViewBindingActivity<AppActivityWebviewBinding> {
    /**
     * h5地址
     */
    @JvmField
    @Autowired(name = Constant.URL)
    String url = "https://www.agora.io/cn/about-us/";


    @Override
    protected AppActivityWebviewBinding getViewBinding(@NonNull LayoutInflater layoutInflater) {
        return AppActivityWebviewBinding.inflate(layoutInflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        setOnApplyWindowInsetsListener(getBinding().superLayout);
        ARouter.getInstance().inject(this);
        if (url.contains("privacy/service")) {
            getBinding().titleView.setTitle(getString(R.string.app_user_agreement));
        } else if (url.contains("about-us")) {
            getBinding().titleView.setTitle(getString(R.string.app_about_us));
        } else if (url.contains("privacy/privacy")) {
            getBinding().titleView.setTitle(getString(R.string.app_privacy_agreement));
        } else if (url.contains("privacy/libraries")) {
            getBinding().titleView.setTitle(getString(R.string.app_third_party_info_data_sharing));
        }else if (url.contains("agora-ent-scenarios")){
            getBinding().titleView.setTitle(getString(R.string.app_personal_info_collection_checklist));
        }
        getBinding().webView.loadUrl(url);
    }

    private void addJavascriptInterface() {
        getBinding().webView.addJavascriptInterface(new Object() {
            /**
             * js 向App请求表单
             * @param day 获取多长跨度的时间,单位：天
             * @param callbackFuncName 告知app端获取到数据后往js哪个方法里传递,可选，也可约定好方法名后不带改参数
             */
            @JavascriptInterface
            public void fetchUsage(final int day, final String callbackFuncName) {
                runOnUiThread(() -> {
                    // TODO: 2023/5/10
                });
            }

            /**
             * App向js注入数据
             */
            @JavascriptInterface
            public void updateUsage() {
                runOnUiThread(() -> {
                    // TODO: 2023/5/10
                    User user = UserManager.getInstance().getUser();
                    UserModel userModel = new UserModel(user.headUrl, user.name, user.mobile);
                    String androidId= Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    String type = "model:" + Build.MODEL + "\n"
                            + "manufacturer：" + Build.MANUFACTURER + "\n"
                            + "os_version：" + Build.VERSION.RELEASE + "\n"
                            + "android_id：" + androidId + "\n"
                            + "imsi：" + "";
                    DeviceInfo deviceInfo = new DeviceInfo(type, "content", 111);
                    WebUsageModel webUsageModel = new WebUsageModel(userModel, deviceInfo);
                    String usage = GsonUtils.Companion.covertToString(new WebUsage(webUsageModel));
                    // 将系统信息返回给 H5 页面
                    getBinding().webView.loadUrl("javascript:updateUsage('" + usage + "')");
                });
            }
        }, "android");
    }
}
