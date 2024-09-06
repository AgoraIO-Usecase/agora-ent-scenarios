package io.agora.scene.ktv.widget.lrcView;

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

/**
 * 评分 S A B C
 */
public class GradeView extends View {
    /**
     * Instantiates a new Grade view.
     *
     * @param context the context
     */
    public GradeView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Grade view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public GradeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new Grade view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public GradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Instantiates a new Grade view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     * @param defStyleRes  the def style res
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private final RectF mDefaultBackgroundRectF = new RectF();

    private final Paint mDefaultBackgroundPaint = new Paint();

    /**
     * Separate this view by 5 parts(->C, C->B, B->A, A->S, S->)
     * 0.6, 0.7, 0.8, 0.9 by PRD
     */
    private static final float xRadioOfGradeC = 0.6f;
    private static final float xRadioOfGradeB = 0.7f;
    private static final float xRadioOfGradeA = 0.8f;
    private static final float xRadioOfGradeS = 0.9f;

    private final Paint mGradeSeparatorIndicatorPaint = new Paint();
    private final Paint mGradeSeparatorLabelIndicatorPaint = new Paint();

    private float mWidth = 0;
    private float mHeight = 0;

    private final RectF mCumulativeScoreBarRectF = new RectF();
    private final Paint mCumulativeScoreBarPaint = new Paint();

    private LinearGradient mCumulativeLinearGradient;

    private int mCumulativeScore;
    private int mPerfectScore;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = right - left;
            mHeight = bottom - top;

            mDefaultBackgroundRectF.top = 0;
            mDefaultBackgroundRectF.left = 0;
            mDefaultBackgroundRectF.right = mWidth;
            mDefaultBackgroundRectF.bottom = mHeight;
        }
    }

    private boolean isCanvasNotReady() {
        return mWidth <= 0 && mHeight <= 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isCanvasNotReady()) { // Fail fast
            return;
        }

        mGradeSeparatorIndicatorPaint.setShader(null);
        mGradeSeparatorIndicatorPaint.setAntiAlias(true);

        mGradeSeparatorLabelIndicatorPaint.setShader(null);
        mGradeSeparatorLabelIndicatorPaint.setAntiAlias(true);
        mGradeSeparatorLabelIndicatorPaint.setTextSize(32);
        mGradeSeparatorLabelIndicatorPaint.setStyle(Paint.Style.FILL);
        mGradeSeparatorLabelIndicatorPaint.setTextAlign(Paint.Align.CENTER);

        mDefaultBackgroundPaint.setShader(null);
        int colorOfBackground = 0;
        int colorOfContentGray = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorOfBackground = getResources().getColor(R.color.ktv_grade_view_bg, null);
            colorOfContentGray = getResources().getColor(R.color.ktv_grade_view_content_gray, null);
        } else {
            colorOfBackground = getResources().getColor(R.color.ktv_grade_view_bg);
            colorOfContentGray = getResources().getColor(R.color.ktv_grade_view_content_gray);
        }
        mDefaultBackgroundPaint.setColor(colorOfBackground);
        mGradeSeparatorIndicatorPaint.setColor(colorOfContentGray);
        mGradeSeparatorLabelIndicatorPaint.setColor(colorOfContentGray);

        Paint.FontMetrics fontMetrics = mGradeSeparatorLabelIndicatorPaint.getFontMetrics();
        float offsetForLabelX = mGradeSeparatorLabelIndicatorPaint.measureText("S");
        float baseLineForLabel = mHeight / 2 - fontMetrics.top / 2 - fontMetrics.bottom / 2;

        mDefaultBackgroundPaint.setAntiAlias(true);
        canvas.drawRoundRect(mDefaultBackgroundRectF, mHeight / 2, mHeight / 2, mDefaultBackgroundPaint);

        canvas.drawLine((float) (mWidth * xRadioOfGradeC), 0, (float) (mWidth * xRadioOfGradeC), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("C", (float) ((mWidth * xRadioOfGradeC) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * xRadioOfGradeB), 0, (float) (mWidth * xRadioOfGradeB), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("B", (float) ((mWidth * xRadioOfGradeB) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * xRadioOfGradeA), 0, (float) (mWidth * xRadioOfGradeA), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("A", (float) ((mWidth * xRadioOfGradeA) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * xRadioOfGradeS), 0, (float) (mWidth * xRadioOfGradeS), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("S", (float) ((mWidth * xRadioOfGradeS) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);

        if (mCumulativeLinearGradient == null) {
            buildDefaultCumulativeScoreBarStyle(Color.parseColor("#FF99f5FF"), Color.parseColor("#FF1B6FFF"));
        }
        mCumulativeScoreBarPaint.setShader(mCumulativeLinearGradient);
        mCumulativeScoreBarPaint.setAntiAlias(true);
        canvas.drawRoundRect(mCumulativeScoreBarRectF, mHeight / 2, mHeight / 2, mCumulativeScoreBarPaint);
    }

    /**
     * Sets score.
     *
     * @param score           the score
     * @param cumulativeScore the cumulative score
     * @param perfectScore    the perfect score
     */
    public void setScore(int score, int cumulativeScore, int perfectScore) {
        mCumulativeScore = cumulativeScore;
        mPerfectScore = perfectScore;

        int startColor = Color.parseColor("#FF99F5FF");
        if (mCumulativeScore <= perfectScore * 0.1) {
            buildDefaultCumulativeScoreBarStyle(startColor, startColor);
        } else {
            float currentWidthOfScoreBar = mWidth * cumulativeScore / perfectScore;
            int middleColor = Color.parseColor("#FF1B6FFF");

            if (mCumulativeScore > perfectScore * 0.1 && mCumulativeScore < perfectScore * 0.8) {
                mCumulativeLinearGradient = new LinearGradient(0, 0, currentWidthOfScoreBar, mHeight, startColor, middleColor, Shader.TileMode.CLAMP);
            } else {
                int endColor = Color.parseColor("#FFD598FF");
                mCumulativeLinearGradient = new LinearGradient(0, 0, currentWidthOfScoreBar, mHeight, new int[]{startColor, middleColor, endColor}, null, Shader.TileMode.CLAMP);
            }

            mCumulativeScoreBarRectF.top = 0;
            mCumulativeScoreBarRectF.bottom = mHeight;
            mCumulativeScoreBarRectF.left = 0;
            mCumulativeScoreBarRectF.right = currentWidthOfScoreBar;
        }

        invalidate();
    }

    /**
     * Gets cumulative drawable.
     *
     * @return the cumulative drawable
     */
    protected int getCumulativeDrawable() {
        int res = 0;

        if (mCumulativeScore >= mPerfectScore * xRadioOfGradeS) {
            res = R.drawable.ktv_ic_grade_s;
        } else if (mCumulativeScore >= mPerfectScore * xRadioOfGradeA) {
            res = R.drawable.ktv_ic_grade_a;
        } else if (mCumulativeScore >= mPerfectScore * xRadioOfGradeB) {
            res = R.drawable.ktv_ic_grade_b;
        } else if (mCumulativeScore >= mPerfectScore * xRadioOfGradeC) {
            res = R.drawable.ktv_ic_grade_c;
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
