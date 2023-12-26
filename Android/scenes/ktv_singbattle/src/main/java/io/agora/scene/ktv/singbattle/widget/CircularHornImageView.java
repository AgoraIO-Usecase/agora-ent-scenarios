package io.agora.scene.ktv.singbattle.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import io.agora.scene.base.utils.UiUtil;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述:圆角ImageView
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2019/3/23
 * ---------------------------------------------------------------------------------------------
 * 代码创建: 刘桂安
 * ---------------------------------------------------------------------------------------------
 * 代码备注:CircularHornImageView
 * ---------------------------------------------------------------------------------------------
 **/
public class CircularHornImageView extends AppCompatImageView {
    private float width, height;
    //圆角
    private int leftTop = UiUtil.dp2px(12);
    private int leftBottom = UiUtil.dp2px(12);
    private int rightTop = UiUtil.dp2px(12);
    private int rightBottom = UiUtil.dp2px(12);


    public CircularHornImageView(Context context) {
        this(context, null);
        init(context, null);
    }

    public CircularHornImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public CircularHornImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width >= 12 && height > 12) {
            Path path = new Path();
            //右上角
            {
                path.moveTo(rightTop, 0);
                path.lineTo(width - rightTop, 0);
                path.quadTo(width, 0, width, rightTop);
            }

            //右下角
            {
                path.lineTo(width, height - rightBottom);
                path.quadTo(width, height, width - rightBottom, height);
            }


            //左下角
            {
                path.lineTo(leftBottom, height);
                path.quadTo(0, height, 0, height - leftBottom);
            }


            //左上角
            {
                path.lineTo(0, leftTop);
                path.quadTo(0, 0, leftTop, 0);
            }


            //绘制
            canvas.clipPath(path);
        }
        super.onDraw(canvas);
    }

    public void setFillet(int fillet) {
        leftTop = fillet;
        leftBottom = fillet;
        rightTop = fillet;
        rightBottom = fillet;
    }

    public void setFillet(int leftTop, int leftBottom, int rightTop, int rightBottom) {
        this.leftTop = leftTop;
        this.leftBottom = leftBottom;
        this.rightTop = rightTop;
        this.rightBottom = rightBottom;
    }
}
