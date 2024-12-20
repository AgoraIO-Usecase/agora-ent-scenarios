package io.agora.scene.base.manager;

import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;

public class PagePilotManager {
    public static void pageWelcomeAndExit() {
        ARouter.getInstance()
                .build(PagePathConstant.pageWelcome)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .withInt(Constant.KEY_CODE, Constant.PARAMS_EXIT)
                .navigation();
    }

    public static void pageWelcomeClear() {
        ARouter.getInstance()
                .build(PagePathConstant.pageWelcome)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation();
    }

    public static void pageLogin() {
        ARouter.getInstance()
                .build(PagePathConstant.pageLogin)
                .navigation();
    }

    public static void pageMainHome() {
        ARouter.getInstance()
                .build(PagePathConstant.pageMainHome)
                .navigation();
    }

    public static void pageWebView(String url) {
        ARouter.getInstance()
                .build(PagePathConstant.pageWebView)
                .withString(Constant.URL, url)
                .navigation();
    }

    public static void pageWebViewWithBrowser(String url) {
        ARouter.getInstance()
                .build(PagePathConstant.pageWebView)
                .withString(Constant.URL, url)
                .withBoolean(Constant.PARAMS_WITH_BROWSER, true)
                .navigation();
    }

    public static void pageAboutUs() {
        ARouter.getInstance()
                .build(PagePathConstant.pageAboutUs)
                .navigation();
    }

    public static void pageMineAccount() {
        ARouter.getInstance()
                .build(PagePathConstant.pageMineAccount)
                .navigation();
    }

    public static void pageFeedback() {
        ARouter.getInstance()
                .build(PagePathConstant.pageFeedback)
                .navigation();
    }

    /**
     * 房间列表
     */
    public static void pageKTVRoomList() {
        ARouter.getInstance()
                .build(PagePathConstant.pageKTVRoomList)
                .navigation();
    }

}
