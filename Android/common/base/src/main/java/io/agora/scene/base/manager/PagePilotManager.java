package io.agora.scene.base.manager;

import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;

public class PagePilotManager {
    public static void pageWelcome() {
        ARouter.getInstance()
                .build(PagePathConstant.pageWelcome)
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

    public static void pageAboutUs() {
        ARouter.getInstance()
                .build(PagePathConstant.pageAboutUs)
                .navigation();
    }

    /**
     * 手机号登录注册
     */
    public static void pagePhoneLoginRegister() {
        ARouter.getInstance()
                .build(PagePathConstant.pagePhoneLoginRegister)
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
