package io.agora.scene.widget.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.widget.R;
import io.agora.scene.widget.SwipeCaptchaView;
import io.agora.scene.widget.databinding.DialogSwipeCaptchaBinding;

public class SwipeCaptchaDialog extends BaseDialog<DialogSwipeCaptchaBinding> {
    public SwipeCaptchaDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected DialogSwipeCaptchaBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return DialogSwipeCaptchaBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        getBinding().iBtnRefresh.setOnClickListener(view -> {
            createCaptcha();
        });
        getBinding().swipeCaptchaView.setOnCaptchaMatchCallback(new SwipeCaptchaView.OnCaptchaMatchCallback() {
            @Override
            public void matchSuccess(SwipeCaptchaView swipeCaptchaView) {
                getOnButtonClickListener().onRightButtonClick();
                getBinding().dragBar.setEnabled(false);
                dismiss();
            }

            @Override
            public void matchFailed(SwipeCaptchaView swipeCaptchaView) {
                ToastUtils.showToast(R.string.comm_try_again);
                createCaptcha();
            }
        });
        getBinding().dragBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                getBinding().swipeCaptchaView.setCurrentSwipeValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //随便放这里是因为控件
                getBinding().dragBar.setMax(getBinding().swipeCaptchaView.getMaxSwipeValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getBinding().swipeCaptchaView.matchCaptcha();
            }
        });
        createCaptcha();
    }

    public static final List<Integer> exampleBackgrounds = new ArrayList<>(
            Arrays.asList(
                    R.mipmap.mvbg1,
                    R.mipmap.mvbg3,
                    R.mipmap.mvbg4,
                    R.mipmap.mvbg7,
                    R.mipmap.mvbg9));

    private void createCaptcha() {
        GlideApp.with(getContext()).asBitmap().load(exampleBackgrounds.get(new Random().nextInt(5))).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                getBinding().swipeCaptchaView.setImageBitmap(resource);
                try {
                    getBinding().swipeCaptchaView.createCaptcha();
                } catch (Exception e) {
                    getBinding().swipeCaptchaView.resetCaptcha();
                }
            }
        });
        getBinding().dragBar.setEnabled(true);
        getBinding().dragBar.setProgress(0);
    }

    @Override
    public void show() {
        super.show();
        createCaptcha();
    }

    @Override
    protected void setGravity() {
//        getWindow().setLayout(
//                UiUtil.dp2px(295),
//                UiUtil.dp2px(365)
//        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
