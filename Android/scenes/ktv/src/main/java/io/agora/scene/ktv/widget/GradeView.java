package io.agora.scene.ktv.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import io.agora.scene.ktv.R;

public class GradeView extends View {
    public GradeView(Context context) {
        super(context);
    }

    public GradeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private final RectF mDefaultBackgroundRectF = new RectF();

    private final Paint mDefaultBackgroundPaint = new Paint();

    /**
     * We separator this view by 5 parts(->C, C->B, B->A, A->S, S->)
     * 0.3, 0.55, 0.725, 0.9
     */
    private final Paint mGradeSeparatorIndicatorPaint = new Paint();
    private final Paint mGradeSeparatorLabelIndicatorPaint = new Paint();

    private float mWidth = 0;
    private float mHeight = 0;

    private final RectF mCumulativeScoreBarRectF = new RectF();
    private final Paint mCumulativeScoreBar = new Paint();

    private LinearGradient mCumulativeLinearGradient;

    private long mCumulativeScore;
    private long mPerfectScore;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mDefaultBackgroundRectF.top = 0;
            mDefaultBackgroundRectF.left = 0;
            mDefaultBackgroundRectF.right = right;
            mDefaultBackgroundRectF.bottom = bottom;

            mWidth = right - left;
            mHeight = bottom - top;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mGradeSeparatorIndicatorPaint.setShader(null);
        mGradeSeparatorIndicatorPaint.setAntiAlias(true);

        mGradeSeparatorLabelIndicatorPaint.setShader(null);
        mGradeSeparatorLabelIndicatorPaint.setAntiAlias(true);
        mGradeSeparatorLabelIndicatorPaint.setTextSize(32);
        mGradeSeparatorLabelIndicatorPaint.setStyle(Paint.Style.FILL);
        mGradeSeparatorLabelIndicatorPaint.setTextAlign(Paint.Align.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDefaultBackgroundPaint.setColor(Color.argb(1f, 0f, 0f, 0.3f));

            mGradeSeparatorIndicatorPaint.setColor(Color.argb(0.3f, 1f, 1f, 1f));
            mGradeSeparatorLabelIndicatorPaint.setColor(Color.argb(0.3f, 1f, 1f, 1f));
        } else {
            mDefaultBackgroundPaint.setColor(Color.LTGRAY);

            mGradeSeparatorIndicatorPaint.setColor(Color.LTGRAY);
            mGradeSeparatorLabelIndicatorPaint.setColor(Color.LTGRAY);
        }

        Paint.FontMetrics fontMetrics = mGradeSeparatorLabelIndicatorPaint.getFontMetrics();
        float offsetForLabelX = mGradeSeparatorLabelIndicatorPaint.measureText("S");
        float baseLineForLabel = mHeight / 2 - fontMetrics.top / 2 - fontMetrics.bottom / 2;

        canvas.drawRoundRect(mDefaultBackgroundRectF, mHeight / 2, mHeight / 2, mDefaultBackgroundPaint);

        canvas.drawLine((float) (mWidth * 0.3), 0, (float) (mWidth * 0.3), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("C", (float) ((mWidth * 0.3) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * 0.55), 0, (float) (mWidth * 0.55), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("B", (float) ((mWidth * 0.55) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * 0.725), 0, (float) (mWidth * 0.725), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("A", (float) ((mWidth * 0.725) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * 0.9), 0, (float) (mWidth * 0.9), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("S", (float) ((mWidth * 0.9) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);

        if (mCumulativeLinearGradient == null) {
            buildDefaultCumulativeScoreBarStyle(Color.parseColor("#FF99f5FF"), Color.parseColor("#FF1B6FFF"));
        }
        mCumulativeScoreBar.setShader(mCumulativeLinearGradient);
        mCumulativeScoreBar.setAntiAlias(true);
        canvas.drawRoundRect(mCumulativeScoreBarRectF, mHeight / 2, mHeight / 2, mCumulativeScoreBar);
    }

    public void setScore(long score, long cumulativeScore, long perfectScore) {
        mCumulativeScore = cumulativeScore;
        mPerfectScore = perfectScore;

        if (mCumulativeScore < 500) {
            int fromColor = Color.parseColor("#FF99f5FF");
            int toColor = Color.parseColor("#FF1B6FFF");
            buildDefaultCumulativeScoreBarStyle(fromColor, toColor);
        } else {
            int fromColor = Color.parseColor("#FF99F5FF");
            int toColor = Color.parseColor("#FFFFEB6E");
            mCumulativeLinearGradient = new LinearGradient(0, 0, mWidth, mHeight, fromColor, toColor, Shader.TileMode.CLAMP);

            mCumulativeScoreBarRectF.top = 0;
            mCumulativeScoreBarRectF.bottom = mHeight;
            mCumulativeScoreBarRectF.left = 0;
            mCumulativeScoreBarRectF.right = mWidth * cumulativeScore / perfectScore;
        }

        invalidate();
    }

    protected int getCumulativeDrawable() {
        int res = R.drawable.ktv_ic_grade_c;

        if (mCumulativeScore >= mPerfectScore * 0.9) {
            res = R.drawable.ktv_ic_grade_s;
        } else if (mCumulativeScore >= mPerfectScore * 0.7) {
            res = R.drawable.ktv_ic_grade_a;
        } else if (mCumulativeScore >= mPerfectScore * 0.55) {
            res = R.drawable.ktv_ic_grade_b;
        }

        return res;
    }

    private void buildDefaultCumulativeScoreBarStyle(int fromColor, int toColor) {
        if (mHeight <= 0) {
            return;
        }

        mCumulativeLinearGradient = new LinearGradient(0, 0, mHeight, mHeight, fromColor, toColor, Shader.TileMode.CLAMP);

        mCumulativeScoreBarRectF.top = 0;
        mCumulativeScoreBarRectF.bottom = mHeight;
        mCumulativeScoreBarRectF.left = 0;
        mCumulativeScoreBarRectF.right = mHeight;
    }
}
