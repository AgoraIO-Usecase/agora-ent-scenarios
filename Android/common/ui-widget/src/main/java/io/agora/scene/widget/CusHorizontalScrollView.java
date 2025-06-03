package io.agora.scene.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Resolve the sliding conflict with ViewPager
 *
 */
public class CusHorizontalScrollView extends HorizontalScrollView {
    private final static String TAG = "CusHorizontalScrollView";

    public CusHorizontalScrollView(Context context) {
        super(context);
    }

    public CusHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 也可以在此处理冲突
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                getParent().getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // request parentView intercept event
                getParent().requestDisallowInterceptTouchEvent(false);
                break;

            default:
        }
        return super.onTouchEvent(ev);
    }

    /**
     * is scroll right
     *
     * @return
     */
    private boolean isScrollToRight() {
        return getChildAt(getChildCount() - 1).getRight() == getScrollX() + getWidth();
    }

    /**
     * is scroll left
     *
     * @return
     */
    private boolean isScrollToLeft() {
        return getScrollX() == 0;
    }
}