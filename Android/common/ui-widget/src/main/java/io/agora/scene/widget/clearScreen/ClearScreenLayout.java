package io.agora.scene.widget.clearScreen;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import io.agora.scene.widget.R;


/**
 * 作者：wangjianxiong 创建时间：2021/4/27 一个支持拖动顶部遮罩层的ViewGroup
 */
public class ClearScreenLayout extends ViewGroup {

    private static final String TAG = "ClearScreenLayout";

    private final ViewDragHelper mViewDragHelper;

    private List<DragListener> mListeners;

    private int mDragState;

    private float mInitialMotionX;

    private float mInitialMotionY;

    private float mLastMotionX;

    private float mLastMotionY;

    private int mTouchSlop;

    private int mActivePointerId = INVALID_POINTER;

    private static final float RIGHT_RANG_SIZE = 0.2f;

    private static final float LEFT_RANG_SIZE = 0.8f;

    /**
     * 负值表示当前无活动指针
     */
    private static final int INVALID_POINTER = -1;

    /**
     * 表示当前滑动闲置
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * 表示当前正被用户拖动
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * 表示当前正被放置在最终位置
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {

    }

    public interface DragListener {

        /**
         * 当遮罩层拖动时调用
         *
         * @param dragView    被拖动的View
         * @param slideOffset 滑动的偏移量
         */
        void onDragging(@NonNull View dragView, float slideOffset);

        /**
         * 当遮罩层被拖动至屏幕外时调用
         *
         * @param dragView 被拖动的View
         */
        void onDragToOut(@NonNull View dragView);

        /**
         * 当遮罩层被拖动至屏幕内时调用
         *
         * @param dragView 被拖动的View
         */
        void onDragToIn(@NonNull View dragView);

        /**
         * 当拖动状态改变时回调
         *
         * @param newState 被拖动的View
         */
        void onDragStateChanged(@State int newState);
    }

    public ClearScreenLayout(Context context) {
        this(context, null);
    }

    public ClearScreenLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearScreenLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setFocusable(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClearScreenLayout);
        float sensitivity = a.getFloat(R.styleable.ClearScreenLayout_touch_slop_sensitivity, 2.0f);
        a.recycle();

        int edgeSize = ClearScreenUtils.getScreenWidth(context);
        ViewDragCallback viewDragCallback = new ViewDragCallback();
        mViewDragHelper = ViewDragHelper.create(this, sensitivity, edgeSize, viewDragCallback);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
        mTouchSlop = mViewDragHelper.getTouchSlop();
        viewDragCallback.setDragger(mViewDragHelper);
    }

    /**
     * 添加拖拽的添加事件
     *
     * @param listener 在发生拖拽事件时通知的监听器。
     */
    public void addDragListener(@NonNull DragListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    /**
     * 移除拖拽的添加事件
     *
     * @param listener 在发生拖拽事件时通知的监听器。
     */
    public void removeDragListener(@NonNull DragListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    public void open() {
        View draggerChild = findDraggerChild();
        if (!isSlideOut(draggerChild)) {
            return;
        }
        mViewDragHelper.smoothSlideViewTo(draggerChild, getWidth() - draggerChild.getWidth(),
                draggerChild.getTop());
        invalidate();
    }

    public void close() {
        View draggerChild = findDraggerChild();
        if (isSlideOut(draggerChild)) {
            return;
        }
        mViewDragHelper.smoothSlideViewTo(draggerChild, getWidth(), draggerChild.getTop());
        invalidate();
    }

    public boolean isOpen() {
        View draggerChild = findDraggerChild();
        return isSlideOut(draggerChild);
    }

    void setDragViewOffset(View dragView, float slideOffset) {
        final LayoutParams lp = (LayoutParams) dragView.getLayoutParams();
        if (slideOffset == lp.onScreen) {
            return;
        }
        lp.onScreen = slideOffset;
        dispatchOnDragging(dragView, slideOffset);
    }

    float getDragViewOffset(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        return lp.onScreen;
    }

    public void updateDragState(int activeState, View activeView) {
        final int rightState = mViewDragHelper.getViewDragState();
        final int state;
        if (rightState == STATE_DRAGGING) {
            state = STATE_DRAGGING;
        } else if (rightState == STATE_SETTLING) {
            state = STATE_SETTLING;
        } else {
            state = STATE_IDLE;
        }

        if (activeView != null && activeState == STATE_IDLE) {
            final LayoutParams lp = (LayoutParams) activeView.getLayoutParams();
            if (lp.onScreen == 0) {
                dispatchDragToOutState(activeView);
            } else if (lp.onScreen == 1) {
                dispatchDragToInState(activeView);
            }
        }

        if (state != mDragState) {
            mDragState = state;

            if (mListeners != null) {
                int listenerCount = mListeners.size();
                for (int i = listenerCount - 1; i >= 0; i--) {
                    mListeners.get(i).onDragStateChanged(state);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
                getDefaultSize(0, heightMeasureSpec));
        boolean hasDraggerView = false;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.isDrag && hasDraggerView) {
                throw new IllegalStateException("暂不支持添加多个可拖动的child");
            }
            hasDraggerView = lp.isDrag;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (isContentView(child)) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
            } else {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                int childLeft;

                final float newOffset;
                childLeft = width - (int) (childWidth * lp.onScreen);
                newOffset = (float) (width - childLeft) / childWidth;

                child.layout(childLeft, lp.topMargin,
                        childLeft + childWidth,
                        lp.topMargin + childHeight);

                boolean changeOffset = newOffset != lp.onScreen;
                if (changeOffset) {
                    setDragViewOffset(child, newOffset);
                }
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        boolean interceptTouchEvent = mViewDragHelper.shouldInterceptTouchEvent(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final float x = ev.getX(pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float y = ev.getY(pointerIndex);
                final float yDiff = Math.abs(y - mInitialMotionY);
                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                    requestParentDisallowInterceptTouchEvent(true);
                    mLastMotionX = dx > 0
                            ? mInitialMotionX + mTouchSlop : mInitialMotionX - mTouchSlop;
                    mLastMotionY = y;
                }
                break;
        }
        return interceptTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragState == ViewDragHelper.STATE_SETTLING) {
            return false;
        }
        mViewDragHelper.processTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                final float xDiff = Math.abs(x - mLastMotionX);
                float y = ev.getY();
                final float yDiff = Math.abs(y - mLastMotionY);
                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    requestParentDisallowInterceptTouchEvent(true);
                    mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    mLastMotionY = y;
                }
                break;
        }
        return true;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof MarginLayoutParams
                        ? new LayoutParams((MarginLayoutParams) p)
                        : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    View findDraggerChild() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (((LayoutParams) child.getLayoutParams()).isDrag) {
                return child;
            }
        }
        return null;
    }

    void dispatchDragToOutState(View dragView) {
        ((LayoutParams) dragView.getLayoutParams()).isSlideOut = true;
        if (mListeners != null) {
            // 发出通知。从列表的末尾开始，这样，如果监听器由于被调用而将自己删除，则不会干扰我们的迭代
            int listenerCount = mListeners.size();
            for (int i = listenerCount - 1; i >= 0; i--) {
                mListeners.get(i).onDragToOut(dragView);
            }
        }
    }

    void dispatchDragToInState(View dragView) {
        ((LayoutParams) dragView.getLayoutParams()).isSlideOut = false;
        if (mListeners != null) {
            // 发出通知。从列表的末尾开始，这样，如果监听器由于被调用而将自己删除，则不会干扰我们的迭代
            int listenerCount = mListeners.size();
            for (int i = listenerCount - 1; i >= 0; i--) {
                mListeners.get(i).onDragToIn(dragView);
            }
        }
    }

    void dispatchOnDragging(View dragView, float slideOffset) {
        if (mListeners != null) {
            // 发出通知。从列表的末尾开始，这样，如果监听器由于被调用而将自己删除，则不会干扰我们的迭代
            int listenerCount = mListeners.size();
            for (int i = listenerCount - 1; i >= 0; i--) {
                mListeners.get(i).onDragging(dragView, slideOffset);
            }
        }
    }

    boolean isContentView(View child) {
        return !((LayoutParams) child.getLayoutParams()).isDrag;
    }

    boolean isSlideOut(View dragView) {
        return ((LayoutParams) dragView.getLayoutParams()).isSlideOut;
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private ViewDragHelper mDragger;

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            return layoutParams.isDrag && mDragState != ViewDragHelper.STATE_SETTLING;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            int width = getWidth();
            int childWidth = releasedChild.getWidth();
            float offset = getDragViewOffset(releasedChild);
            float v = isSlideOut(releasedChild) ? RIGHT_RANG_SIZE : LEFT_RANG_SIZE;
            int left = xvel < 0 || (xvel == 0 && offset > v) ? width - childWidth : width;
            mDragger.settleCapturedViewAt(left, releasedChild.getTop());
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            //设置左侧边界
            final int width = getWidth();
            return Math.max(width - child.getWidth(), Math.min(left, width));
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return child.getTop();
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return !isContentView(child) ? child.getWidth() : 0;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
            if (edgeFlags == ViewDragHelper.EDGE_RIGHT) {
                View child = findDraggerChild();
                mDragger.captureChildView(child, pointerId);
            }
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx,
                int dy) {
            final int childWidth = changedView.getWidth();
            final int width = getWidth();
            float offset = (float) (width - left) / childWidth;
            if (offset >= 0) {
                setDragViewOffset(changedView, offset);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            updateDragState(state, mDragger.getCapturedView());
        }
    }

    public static class LayoutParams extends MarginLayoutParams {

        public boolean isDrag;

        public boolean isSlideOut;

        public float onScreen = 1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = null;
            try {
                a = c.obtainStyledAttributes(attrs, R.styleable.ClearScreenLayout_Layout);
                isDrag = a.getBoolean(
                        R.styleable.ClearScreenLayout_Layout_layout_dragEnable,
                        false
                );
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (a != null) {
                    a.recycle();
                }
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

    }
}
