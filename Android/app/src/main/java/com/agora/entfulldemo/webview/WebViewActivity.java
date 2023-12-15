package com.agora.entfulldemo.webview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppActivityWebviewBinding;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.utils.UiUtil;
import kotlin.jvm.JvmField;

@Route(path = PagePathConstant.pageWebView)
public class WebViewActivity extends BaseViewBindingActivity<AppActivityWebviewBinding> {
    /**
     * h5地址
     */
    @JvmField
    @Autowired(name = Constant.URL)
    String url = "https://www.agora.io/cn/about-us/";

    @JvmField
    @Autowired(name = Constant.PARAMS_WITH_BROWSER)
    Boolean withBrowser = false;

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
        } else if (url.contains("ent-scenarios/pages/manifest")) {
            getBinding().titleView.setTitle(getString(R.string.app_personal_info_collection_checklist));
        }
        getBinding().webView.loadUrl(url);


        if (withBrowser) {
            getBinding().titleView.getLeftIcon().setImageResource(R.drawable.app_icon_back_black);
            ImageView right = getBinding().titleView.getRightIcon();
            right.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = right.getLayoutParams();
            layoutParams.width = UiUtil.dp2px(22);
            layoutParams.height = UiUtil.dp2px(22);
            right.setLayoutParams(layoutParams);
            right.setImageResource(R.drawable.app_icon_find_earth);
            getBinding().titleView.setRightIconClick(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            });
        }

        getBinding().titleView.setLeftClick(v -> {
            if (getBinding().webView.canGoBack()) {
                getBinding().webView.goBack();
            } else {
                finish();
            }
        });
        getBinding().webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Log.d("zhangw", "WebChromeClient webView title：");
                if (!TextUtils.isEmpty(title)&&!view.getUrl().contains(title)){
                    getBinding().titleView.setTitle(title);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getBinding().webView.canGoBack()) {
            getBinding().webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
