package io.agora.scene.base.utils;

import java.io.File;

import io.agora.scene.base.component.AgoraApplication;

public class FileUtils {
    public static String getBaseStrPath() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
//        } else {
        return AgoraApplication.the().getExternalFilesDir("media").getAbsolutePath() + File.separator;
//        }
    }

    public static String getTempSDPath() {
        return getBaseStrPath() + "ag" + File.separator;
    }

}
