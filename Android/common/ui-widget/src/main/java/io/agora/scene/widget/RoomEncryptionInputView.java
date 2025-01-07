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
        setTextColor(0X00ffffff);
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
        if (text.toString().length() <= mTextLength) {
            mText = text.toString();
        } else {
            setText(mText);
            setSelection(getText().toString().length());
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
        mSidePaint.setAntiAlias(true);
        mSidePaint.setStrokeWidth(mStrokeWidth);
        mSidePaint.setStyle(Paint.Style.STROKE);
        mSidePaint.setColor(mDefaultColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setColor(mBackColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mTextColor);

        int singleSize = getMeasuredHeight();
        mSpaceX = (getMeasuredWidth() - singleSize * mTextLength) / (mTextLength - 1);
        RectF rectBg = null;
        for (int i = 0; i < mTextLength; i++) {
            //区分已输入和未输入的边框颜色
            if (mText.length() >= i) {
                mSidePaint.setColor(mCheckedColor);
            } else {
                mSidePaint.setColor(mDefaultColor);
            }
            rectBg = new RectF(i * singleSize + mSpaceX * i + mStrokeWidth, mStrokeWidth,
                    (i + 1) * singleSize + mSpaceX * i - mStrokeWidth,
                    singleSize - mStrokeWidth);
            canvas.drawRoundRect(rectBg, mRound, mRound, mBackPaint);
            canvas.drawRoundRect(rectBg, mRound, mRound, mSidePaint);
            mRectFS.add(rectBg);

            if (mIsWaitInput && i == mText.length()) {
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
        for (int j = 0; j < mText.length(); j++) {
            if (mIsPwd) {
                canvas.drawCircle(mRectFS.get(j).centerX(), mRectFS.get(j).centerY(), mCircle, mTextPaint);
            } else {
                canvas.drawText(mText.substring(j, j + 1), mRectFS.get(j).centerX() - mTextSize / 2 + mStrokeWidth,
                        mRectFS.get(j).centerY() + mTextSize / 2 - mStrokeWidth, mTextPaint);
            }
        }
    }

    public interface OnTextChangeListener {
        void onTextChange(String pwd);
    }

    private OnTextChangeListener OnTextChangeListener;

    public void setOnTextChangeListener(OnTextChangeListener OnTextChangeListener) {
        this.OnTextChangeListener = OnTextChangeListener;
    }

    public void clearText() {
        setText("");
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
    }

    public void setXSpace(int space) {
        mSpaceX = space;
    }

    public void setTextLength(int mTextLength) {
        this.mTextLength = mTextLength;
    }

    public int getTextLength() {
        return this.mTextLength;
    }

    public void setCheckedColorColor(int checkedColor) {
        this.mCheckedColor = checkedColor;
    }

    public void setDefaultColorColor(int defaultColor) {
        this.mDefaultColor = defaultColor;
    }

    public void setBackColor(int mBackColor) {
        this.mBackColor = mBackColor;
    }

    public void setPwdTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    public void setStrokeWidth(int width) {
        mStrokeWidth = width;
    }

    public void setCircle(int Circle) {
        this.mCircle = Circle;
    }

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

    public void setIsPwd(boolean mIsPwd) {
        this.mIsPwd = mIsPwd;
    }

    public int getWaitInputColor() {
        return mWaitInputColor;
    }

    public void setWaitInputColor(int mWaitInputColor) {
        this.mWaitInputColor = mWaitInputColor;
    }

    public boolean isIsWaitInput() {
        return mIsWaitInput;
    }

    public void setIsWaitInput(boolean mIsWaitInput) {
        this.mIsWaitInput = mIsWaitInput;

    }
}
