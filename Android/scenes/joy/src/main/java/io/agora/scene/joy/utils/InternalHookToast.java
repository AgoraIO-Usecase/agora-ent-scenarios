package io.agora.scene.joy.utils;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.RestrictTo;

import java.lang.reflect.Field;

@RestrictTo({RestrictTo.Scope.LIBRARY})
public final class InternalHookToast {

    private static Field sField_TN;
    private static Field sField_TN_Handler;

    static {
        try {
            Class<?> clazz = Toast.class;
            //通过反射拿到，获取class对象的指定属性，拿到tn对象
            sField_TN = clazz.getDeclaredField("mTN");
            sField_TN.setAccessible(true);
            //然后通过反射拿到Toast中内部类TN的mHandler
            sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
            sField_TN_Handler.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hook(Toast toast) {
        try {
            Object tn = sField_TN.get(toast);
            Handler preHandler = (Handler) sField_TN_Handler.get(tn);
            sField_TN_Handler.set(tn, new SafelyHandler(preHandler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SafelyHandler extends Handler {

        private final Handler impl;

        public SafelyHandler(Handler impl) {
            this.impl = impl;
        }

        @Override
        public void dispatchMessage(Message msg) {
            try {
                // 捕获这个异常，避免程序崩溃
                super.dispatchMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            //需要委托给原Handler执行
            impl.handleMessage(msg);
        }
    }

}
