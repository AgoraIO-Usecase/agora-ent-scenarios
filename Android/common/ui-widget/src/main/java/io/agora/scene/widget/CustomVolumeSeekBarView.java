package io.agora.scene.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.agora.scene.base.utils.UiUtil;

public class CustomVolumeSeekBarView extends View {
    /**
     * 总条目 左右 各五个
     */
    private final int totalPitch = 11;

    /**
     * 当前选中的音调
     */
    public int currentPitch = 5;

    private int paddingBottom = UiUtil.dp2px(3);

    private final int mSelectBlueColor = ContextCompat.getColor(getContext(), R.color.blue_9F);
    private final int mUnSelectBlueColor = ContextCompat.getColor(getContext(), R.color.white);

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public CustomVolumeSeekBarView(Context context) {
        super(context);
        initView();
    }

    public CustomVolumeSeekBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomVolumeSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPaint.setStrokeWidth(UiUtil.dp2px(3));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawStartLine(canvas);
    }

    private void drawStartLine(Canvas canvas) {
        for (int i = 0; i < totalPitch; i++) {
            if (i <= currentPitch) {
                mPaint.setColor(mSelectBlueColor);
            } else {
                mPaint.setColor(mUnSelectBlueColor);
            }
            int left = i * UiUtil.dp2px(17);
            int top = getBottom() - paddingBottom - UiUtil.dp2px((i + 1) * 2);
            int right = left + paddingBottom;
            int bottom = getBottom() - paddingBottom;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    public void currentPitchPlus() {
        if (currentPitch == 11) {
            return;
        }
        currentPitch++;
        invalidate();
    }

    public void currentPitchMinus() {
        if (currentPitch < 0) {
            return;
        }
        currentPitch--;
        invalidate();
    }

}
