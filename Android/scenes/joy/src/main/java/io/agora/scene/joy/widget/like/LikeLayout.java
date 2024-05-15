/*
 * Copyright (C) 2015 tyrantgit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agora.scene.joy.widget.like;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.Random;

import io.agora.scene.joy.R;

/**
 * like动画
 */
public class LikeLayout extends RelativeLayout implements View.OnClickListener {

    private AbstractPathAnimator mAnimator;
    private AttributeSet attrs = null;
    private int defStyleAttr = 0;
    private OnHearLayoutListener onHearLayoutListener;
    private static HeartHandler heartHandler;
    private static HeartThread heartThread;
    private ImageView imageView;

    public void setOnHearLayoutListener(OnHearLayoutListener onHearLayoutListener) {
        this.onHearLayoutListener = onHearLayoutListener;
    }

    public interface OnHearLayoutListener {
        boolean onAddFavor();
        void onLikeClick(View view);
    }

    public LikeLayout(Context context) {
        super(context);
        findViewById(context);
    }

    public LikeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        findViewById(context);
    }

    public LikeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        findViewById(context);
    }

    private Bitmap bitmap;

    private void findViewById(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.joy_widget_gift_periscope, this);
        imageView = view.findViewById(R.id.img);
        imageView.setOnClickListener(this);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.joy_icon_like);
        dHeight = bitmap.getWidth();
        dWidth = bitmap.getHeight();
        textHight = sp2px(getContext(), 20) + dHeight / 2;

        pointx = dWidth;//随机上浮方向的x坐标

        bitmap.recycle();
    }

    public ImageView getLikeView(){
        return imageView;
    }

    private int mHeight;
    private int mWidth;
    private int textHight;
    private int dHeight;
    private int dWidth;
    private int initX;
    private int pointx;

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    public class HeartHandler extends Handler {
        public final static int MSG_SHOW = 1;
        WeakReference<LikeLayout> wf;

        public HeartHandler(LikeLayout layout) {
            wf = new WeakReference<LikeLayout>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LikeLayout layout = wf.get();
            if (layout == null) return;
            switch (msg.what) {
                case MSG_SHOW:
                    addFavor();
                    break;
                default:
                    break;
            }
        }
    }

    public class HeartThread implements Runnable {

        private long time = 0;
        private int allSize = 0;

        public void addTask(long time, int size) {
            this.time = time;
            allSize += size;
        }

        public void clean() {
            allSize = 0;
        }

        @Override
        public void run() {
            if (heartHandler == null) return;

            if (allSize > 0) {
                heartHandler.sendEmptyMessage(HeartHandler.MSG_SHOW);
                allSize--;
            }
            postDelayed(this, time);
        }
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.joy_LikeLayout, defStyleAttr, 0);

        if (pointx <= initX && pointx >= 0) {
            pointx -= 10;
        } else if (pointx >= -initX && pointx <= 0) {
            pointx += 10;
        } else pointx = initX;


        mAnimator = new PathAnimator(AbstractPathAnimator.Config.fromTypeArray(a, initX, textHight, pointx, dWidth, dHeight));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取本身的宽高 这里要注意,测量之后才有宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        initX = mWidth / 2 - dWidth / 2;

    }

    public AbstractPathAnimator getAnimator() {
        return mAnimator;
    }

    public void setAnimator(AbstractPathAnimator animator) {
        clearAnimation();
        mAnimator = animator;
    }

    public void clearAnimation() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).clearAnimation();
        }
        removeAllViews();
    }

    private static int[] drawableIds = new int[]{R.drawable.joy_icon_like_1, R.drawable.joy_icon_like_2, R.drawable.joy_icon_like_3, R.drawable.joy_icon_like_4, R.drawable.joy_icon_like_5, R.drawable.joy_icon_like_6};
    private Random random = new Random();

    public void addFavor() {
        LikeView likeView = new LikeView(getContext());
        likeView.setDrawable(drawableIds[random.nextInt(5)]);
        init(attrs, defStyleAttr);
        mAnimator.start(likeView, this);
    }

    private long nowTime, lastTime;
    final static int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999,
            99999999, 999999999, Integer.MAX_VALUE};

    public static int sizeOfInt(int x) {
        for (int i = 0; ; i++)
            if (x <= sizeTable[i])
                return i + 1;
    }

    public void addFavor(int size) {
        switch (sizeOfInt(size)) {
            case 1:
                size = size % 10;
                break;
            default:
                size = size % 100;
        }
        if (size == 0) return;
        nowTime = System.currentTimeMillis();
        long time = nowTime - lastTime;
        if (lastTime == 0)
            time = 2 * 1000;//第一次分为2秒显示完

        time = time / (size + 15);
        if (heartThread == null) {
            heartThread = new HeartThread();
        }
        if (heartHandler == null) {
            heartHandler = new HeartHandler(this);
            heartHandler.post(heartThread);
        }
        heartThread.addTask(time, size);
        lastTime = nowTime;
    }

    public void addHeart(int color) {
        LikeView likeView = new LikeView(getContext());
        likeView.setColor(color);
        init(attrs, defStyleAttr);
        mAnimator.start(likeView, this);
    }

    public void addHeart(int color, int heartResId, int heartBorderResId) {
        LikeView likeView = new LikeView(getContext());
        likeView.setColorAndDrawables(color, heartResId, heartBorderResId);
        init(attrs, defStyleAttr);
        mAnimator.start(likeView, this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.img) {
            if (onHearLayoutListener != null) {
                Log.d("setOnHearLayoutListener","onClick");
                onHearLayoutListener.onLikeClick(v);
                boolean isAdd = onHearLayoutListener.onAddFavor();
                if (isAdd) addFavor();
            }
        }
    }

    public void clean() {
        if (heartThread != null) {
            heartThread.clean();
        }
    }

    public void release() {
        if (heartHandler != null) {
            heartHandler.removeCallbacks(heartThread);
            heartThread = null;
            heartHandler = null;
        }
    }
}
