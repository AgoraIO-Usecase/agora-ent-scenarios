package io.agora.scene.base.manager;

import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.KtvConstant;
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
                .withString(KtvConstant.URL, url)
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
     * 创建房间
     */
    public static void pageCreateRoomStep1() {
        ARouter.getInstance()
                .build(PagePathConstant.pageRoomCreate)
                .navigation();
    }

    /**
     * 房间列表
     */
    public static void pageRoomList() {
        ARouter.getInstance()
                .build(PagePathConstant.pageRoomList)
                .navigation();
    }

    /**
     * 房间主页
     */
    public static void pageRoomLiving() {
        ARouter.getInstance()
                .build(PagePathConstant.pageRoomLiving)
                .navigation();
    }

}
