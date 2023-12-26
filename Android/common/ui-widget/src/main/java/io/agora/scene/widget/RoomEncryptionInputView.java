package io.agora.scene.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class RoomEncryptionInputView extends TextInputEditText {
    private static String TAG = RoomEncryptionInputView.class.getName();
    private Paint mSidePaint, mBackPaint, mTextPaint;
    private Context mC;
    private String mText;
    private List<RectF> mRectFS;
    private int mStrokeWidth, mSpaceX, mTextSize;
    private int mCheckedColor, mDefaultColor, mBackColor, mTextColor, mWaitInputColor;
    private int mTextLength;
    private int mCircle, mRound;
    private boolean mIsPwd, mIsWaitInput;
    private Paint l;

    public RoomEncryptionInputView(Context context) {
        super(context);

        mC = context;
        setAttrs(null);
        init();
    }

    public RoomEncryptionInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mC = context;
        setAttrs(attrs);
        init();
    }

    public RoomEncryptionInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mC = context;
        setAttrs(attrs);
        init();
    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray t = mC.obtainStyledAttributes(attrs, R.styleable.ui_widget_encryption_input_style);
        if (t != null) {
            mTextLength = t.getInt(R.styleable.ui_widget_encryption_input_style_ui_widget_textLength, 6);
            mStrokeWidth = t.getDimensionPixelSize(R.styleable.ui_widget_encryption_input_style_ui_widget_strokeWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
            mRound = t.getDimensionPixelSize(R.styleable.ui_widget_encryption_input_style_ui_widget_round, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
            mCircle = t.getDimensionPixelSize(R.styleable.ui_widget_encryption_input_style_ui_widget_circle, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics()));
            mTextSize = t.getDimensionPixelSize(R.styleable.ui_widget_encryption_input_style_ui_widget_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            mCheckedColor = t.getColor(R.styleable.ui_widget_encryption_input_style_ui_widget_checkedColor, 0xff44ce61);
            mDefaultColor = t.getColor(R.styleable.ui_widget_encryption_input_style_ui_widget_defaultColor, 0xffd0d0d0);
            mBackColor = t.getColor(R.styleable.ui_widget_encryption_input_style_ui_widget_backColor, 0xfff1f1f1);
            mTextColor = t.getColor(R.styleable.ui_widget_encryption_input_style_ui_widget_textColor, 0xFF444444);
            mWaitInputColor = t.getColor(R.styleable.ui_widget_encryption_input_style_ui_widget_waitInputColor, 0xFF444444);
            mIsPwd = t.getBoolean(R.styleable.ui_widget_encryption_input_style_ui_widget_isPwd, true);
            mIsWaitInput = t.getBoolean(R.styleable.ui_widget_encryption_input_style_ui_widget_isWaitInput, true);
            t.recycle();
        }
    }

    private void init() {
        setTextColor(0X00ffffff); //把用户输入的内容设置为透明
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        mSidePaint = new Paint();
        mBackPaint = new Paint();
        mTextPaint = new Paint();


        mRectFS = new ArrayList<>();
        mText = "";

        this.setBackgroundDrawable(null);
        setLongClickable(false);
        setTextIsSelectable(false);
        setCursorVisible(false);

    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mText == null) {
            return;
        }
        //如果字数不超过用户设置的总字数，就赋值给成员变量mText；
        // 如果字数大于用户设置的总字数，就只保留用户设置的几位数字，并把光标制动到最后，让用户可以删除；
        if (text.toString().length() <= mTextLength) {
            mText = text.toString();
        } else {
            setText(mText);
            setSelection(getText().toString().length());  //光标制动到最后
            //调用setText(mText)之后键盘会还原，再次把键盘设置为数字键盘；
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }
        if (OnTextChangeListener != null) OnTextChangeListener.onTextChange(mText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                heightSize = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                heightSize = widthSize / mTextLength;
                break;
            default:
                break;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //边框画笔
        mSidePaint.setAntiAlias(true);//消除锯齿
        mSidePaint.setStrokeWidth(mStrokeWidth);//设置画笔的宽度
        mSidePaint.setStyle(Paint.Style.STROKE);//设置绘制轮廓
        mSidePaint.setColor(mDefaultColor);
        //背景色画笔
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setColor(mBackColor);
        //文字的画笔
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mTextColor);

        // 方型大小
        int singleSize = getMeasuredHeight();
        // 左右间距
        mSpaceX = (getMeasuredWidth() - singleSize * mTextLength) / (mTextLength - 1);
        RectF rectBg = null;
        for (int i = 0; i < mTextLength; i++) {
            //区分已输入和未输入的边框颜色
            if (mText.length() >= i) {
                mSidePaint.setColor(mCheckedColor);
            } else {
                mSidePaint.setColor(mDefaultColor);
            }
            //RectF的参数(left,  top,  right,  bottom); 画出每个矩形框并设置间距，间距其实是增加左边框距离，缩小上下右边框距离；
            rectBg = new RectF(i * singleSize + mSpaceX * i + mStrokeWidth, mStrokeWidth,
                    (i + 1) * singleSize + mSpaceX * i - mStrokeWidth,
                    singleSize - mStrokeWidth);
            //四个值，分别代表4条线，距离起点位置的线
            canvas.drawRoundRect(rectBg, mRound, mRound, mBackPaint); //绘制背景色
            canvas.drawRoundRect(rectBg, mRound, mRound, mSidePaint); //绘制边框
            mRectFS.add(rectBg);

            if (mIsWaitInput && i == mText.length()) {  //显示待输入的线
                l = new Paint();
                l.setStrokeWidth(3);
                l.setStyle(Paint.Style.FILL);
                l.setColor(mWaitInputColor);
                canvas.drawLine(i * singleSize + singleSize / 2 + (mSpaceX * i),
                        singleSize / 2 - singleSize / 5,
                        i * singleSize + singleSize / 2 + (mSpaceX * i),
                        singleSize / 2 + singleSize / 5, l);
            }
        }
        //画密码圆点
        for (int j = 0; j < mText.length(); j++) {
            if (mIsPwd) {
                canvas.drawCircle(mRectFS.get(j).centerX(), mRectFS.get(j).centerY(), mCircle, mTextPaint);
            } else {
                canvas.drawText(mText.substring(j, j + 1), mRectFS.get(j).centerX() - mTextSize / 2 + mStrokeWidth,
                        mRectFS.get(j).centerY() + mTextSize / 2 - mStrokeWidth, mTextPaint);
            }
        }
    }

    /**
     * 输入监听
     */
    public interface OnTextChangeListener {
        void onTextChange(String pwd);
    }

    private OnTextChangeListener OnTextChangeListener;

    public void setOnTextChangeListener(OnTextChangeListener OnTextChangeListener) {
        this.OnTextChangeListener = OnTextChangeListener;
    }

    /**
     * 清空所有输入的内容
     */
    public void clearText() {
        setText("");
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
    }

    /**
     * 设置密码框间距
     */
    public void setXSpace(int space) {
        mSpaceX = space;
    }

    /**
     * 设置密码框个数
     */
    public void setTextLength(int mTextLength) {
        this.mTextLength = mTextLength;
    }

    /**
     * 获得密码框个数
     */
    public int getTextLength() {
        return this.mTextLength;
    }

    /**
     * 设置已输入密码框颜色
     */
    public void setCheckedColorColor(int checkedColor) {
        this.mCheckedColor = checkedColor;
    }

    /**
     * 设置未输入密码框颜色
     */
    public void setDefaultColorColor(int defaultColor) {
        this.mDefaultColor = defaultColor;
    }

    /**
     * 设置密码框背景色
     */
    public void setBackColor(int mBackColor) {
        this.mBackColor = mBackColor;
    }

    /**
     * 设置密码圆点的颜色
     */
    public void setPwdTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    /**
     * 设置密码框 边框的宽度
     */
    public void setStrokeWidth(int width) {
        mStrokeWidth = width;
    }

    /**
     * 密码的圆点大小
     */
    public void setCircle(int Circle) {
        this.mCircle = Circle;
    }

    /**
     * 密码边框的圆角大小
     */
    public void setRound(int Round) {
        this.mRound = Round;
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public int getSpaceX() {
        return mSpaceX;
    }

    public int getCheckedColor() {
        return mCheckedColor;
    }

    public int getDefaultColor() {
        return mDefaultColor;
    }

    public int getBackColor() {
        return mBackColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getCircle() {
        return mCircle;
    }

    public int getRound() {
        return mRound;
    }

    public int geTextSize() {
        return mTextSize;
    }

    public void setTextSize(int mTextSize) {
        this.mTextSize = mTextSize;
    }

    public boolean getIsPwd() {
        return mIsPwd;
    }

    /**
     * 是否密文输入
     *
     * @param mIsPwd
     */
    public void setIsPwd(boolean mIsPwd) {
        this.mIsPwd = mIsPwd;
    }

    public int getWaitInputColor() {
        return mWaitInputColor;
    }

    /**
     * \
     * 待输入线的颜色
     *
     * @param mWaitInputColor
     */
    public void setWaitInputColor(int mWaitInputColor) {
        this.mWaitInputColor = mWaitInputColor;
    }

    public boolean isIsWaitInput() {
        return mIsWaitInput;
    }

    /**
     * 是否显示待输入的线
     *
     * @param mIsWaitInput
     */
    public void setIsWaitInput(boolean mIsWaitInput) {
        this.mIsWaitInput = mIsWaitInput;

    }
}
