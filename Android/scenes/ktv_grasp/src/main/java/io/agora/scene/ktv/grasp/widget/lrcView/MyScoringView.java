package io.agora.scene.ktv.grasp.widget.lrcView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import io.agora.karaoke_view.v11.ScoringView;
import io.agora.scene.ktv.grasp.R;

public class MyScoringView extends ScoringView {
    private LinearGradient mStartLineLinearGradient;

    public MyScoringView(Context context) {
        super(context);
    }

    public MyScoringView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScoringView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyScoringView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            int colorWhite35 = getResources().getColor(R.color.white_35_percent);
            int colorWhite = getResources().getColor(R.color.white);
            mStartLineLinearGradient = new LinearGradient(0, 0, 0, bottom - top, new int[]{colorWhite35, colorWhite, colorWhite35}, null, Shader.TileMode.CLAMP);
        }
    }

    @Override
    protected void drawOverpastWallAndStartLine(Canvas canvas) {
        drawOverpastWall(canvas);

        // Same as drawStartLine, but with extra gradient color on it
        mStartLinePaint.setShader(mStartLineLinearGradient);
        mStartLinePaint.setColor(mLocalPitchIndicatorColor);
        mStartLinePaint.setAntiAlias(true);
        mStartLinePaint.setStrokeWidth(3);
        canvas.drawLine(mCenterXOfStartPoint, 0, mCenterXOfStartPoint, getHeight(), mStartLinePaint);
    }
}
