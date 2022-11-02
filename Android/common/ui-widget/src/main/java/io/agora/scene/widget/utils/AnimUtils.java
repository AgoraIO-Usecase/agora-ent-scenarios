package io.agora.scene.widget.utils;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;

public class AnimUtils {
    /**
     * 线的位移动画 在view的正下方
     */
    public static void radioGroupLineAnim(View superView, View viewLine) {
        if (superView.getMeasuredWidth() == 0) {
            int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            superView.measure(width, height);
        }
        if (viewLine.getMeasuredWidth() == 0) {
            int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            viewLine.measure(width, height);
        }
        if (viewLine.getMeasuredWidth() == 0) {
            viewLine.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            viewLine.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int offset = superView.getMeasuredWidth() / 2 - viewLine.getWidth() / 2;
                            float fromX = viewLine.getX();
                            float toX = superView.getX() + offset;
                            ObjectAnimator animatorTransY = ObjectAnimator.ofFloat(
                                    viewLine,
                                    "translationX",
                                    fromX,
                                    toX
                            );
                            animatorTransY.setDuration(200);
                            animatorTransY.start();
                        }
                    });
        } else {
            int offset = superView.getMeasuredWidth() / 2 - viewLine.getMeasuredWidth() / (2);
            float fromX = viewLine.getX();
            float toX = superView.getX() + offset;
            ObjectAnimator animatorTransY = ObjectAnimator.ofFloat(viewLine, "translationX", fromX, toX);
            animatorTransY.setDuration(200);
            animatorTransY.start();
        }
    }
}
