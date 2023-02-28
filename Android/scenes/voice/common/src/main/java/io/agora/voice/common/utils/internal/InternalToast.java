package io.agora.voice.common.utils.internal;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.lang.ref.SoftReference;

import io.agora.voice.common.R;
import io.agora.voice.common.utils.DeviceTools;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2017/06/4
 *     desc  : Toast工具类
 *     revise: 具体看GitHub开源项目：https://github.com/yangchong211/YCDialog
 * </pre>
 */
@RestrictTo({RestrictTo.Scope.LIBRARY})
public final class InternalToast {

    public static final int COMMON = 0;
    public static final int TIPS = 1;
    public static final int ERROR = 2;

    @SuppressLint("StaticFieldLeak")
    private static Application mApp;

    /**
     * 初始化吐司工具类
     *
     * @param app 应用
     */
    public static void init(@NonNull final Application app) {
        if (mApp == null) {
            mApp = app;
        }
    }

    public static Application getApp() {
        return mApp;
    }

    /**
     * 私有构造
     */
    private InternalToast() {
        //避免初始化
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 检查上下文不能为空，必须先进性初始化操作
     */
    private static void checkContext() {
        if (mApp == null) {
            throw new NullPointerException("ToastUtils context is not null，please first init");
        }
    }

    /**
     * 某些系统可能屏蔽通知
     * 1:检查 SystemUtils.isEnableNotification(BaseApplication.getApplication());
     * 2:替代方案 SnackBarUtils.showSnack(topActivity, noticeStr);
     * 圆角
     * 屏幕中间
     *
     * @param notice 内容
     */
    public static void show(CharSequence notice, int toastType,int duration) {
        checkMainThread();
        checkContext();
        if (TextUtils.isEmpty(notice)) {
            return;
        }
        new Builder(mApp)
                .setDuration(duration)
                .setGravity(Gravity.BOTTOM)
                .setOffset((int) DeviceTools.getDp(200))
                .setToastTYpe(toastType)
                .setTitle(notice)
                .build()
                .show();
    }


    public static final class Builder {

        private final Context context;
        private CharSequence title;
        private int gravity = Gravity.TOP;
        private int yOffset;
        private int duration = Toast.LENGTH_SHORT;
        private int toastType;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setToastTYpe(int toastType) {
            this.toastType = toastType;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setOffset(int yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * 采用软引用管理toast对象
         * 如果一个对象只具有软引用，那么如果内存空间足够，垃圾回收器就不会回收它；
         * 如果内存空间不足了，就会回收这些对象的内存。只要垃圾回收器没有回收它，该对象就可以被程序使用。
         */
        private SoftReference<Toast> mToast;

        public Toast build() {
            if (!checkNull(mToast)) {
                mToast.get().cancel();
            }
            Toast toast = new Toast(context);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                //android 7.1.1 版本
                InternalHookToast.hook(toast);
            }

//            toast.setMargin(0, 0);
            View rootView = LayoutInflater.from(context).inflate(R.layout.voice_view_toast_custom, null);
            TextView textView = rootView.findViewById(R.id.tvContent);
            ImageView imageView = rootView.findViewById(R.id.ivToast);

            textView.setText(title);
            if (toastType == COMMON) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                if (toastType == TIPS) {
                    imageView.setImageResource(R.drawable.voice_icon_toast_hint);
                } else {
                    imageView.setImageResource(R.drawable.voice_icon_toast_error);
                }
            }

            toast.setView(rootView);
            toast.setGravity(gravity, 0, yOffset);
            toast.setDuration(duration);
            mToast = new SoftReference<>(toast);
            return toast;
        }
    }

    private static boolean checkNull(SoftReference softReference) {
        if (softReference == null || softReference.get() == null) {
            return true;
        }
        return false;
    }

    public static void checkMainThread() {
        if (!isMainThread()) {
            throw new IllegalStateException("请不要在子线程中做弹窗操作");
        }
    }

    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
