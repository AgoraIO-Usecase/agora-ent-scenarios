package io.agora.scene.voice;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

public class ChatroomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.init(this);
        VoiceConfigManager.initMain();
    }
}
