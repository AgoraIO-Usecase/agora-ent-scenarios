package io.agora.scene.base.utils;

import android.widget.Toast;

import io.agora.scene.base.component.AgoraApplication;

public class ToastUtils {

    public static void showToast(int resStringId) {
        Toast.makeText(AgoraApplication.the(), resStringId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(String str) {
        Toast.makeText(AgoraApplication.the(), str, Toast.LENGTH_SHORT).show();
    }
}
