package io.agora.scene.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import io.agora.scene.widget.databinding.ViewCommonTitleBinding;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/**
 * 代码内kotlin转回java
 */
public final class CommonTitleView extends ConstraintLayout {
    private ViewCommonTitleBinding binding;

    public ImageView getRightIcon(){
        return binding.ivRightIcon;
    }
    private final void initView(AttributeSet attrs, int defStyleAttr) {
        Context var10000 = this.getContext();
        Intrinsics.checkNotNullExpressionValue(var10000, "context");
        TypedArray var11 = var10000.getTheme().obtainStyledAttributes(attrs, R.styleable.titleView, defStyleAttr, 0);
        Intrinsics.checkNotNullExpressionValue(var11, "context.theme.obtainStyl…defStyleAttr, 0\n        )");
        TypedArray typedArray = var11;
        Drawable drawable = typedArray.getDrawable(R.styleable.titleView_ctv_leftImageSrc);
        AppCompatImageView var12;
        if (drawable != null) {
            this.binding.ivBackIcon.setImageDrawable(drawable);
            var12 = this.binding.ivBackIcon;
            Intrinsics.checkNotNullExpressionValue(var12, "binding.ivBackIcon");
            var12.setVisibility(View.VISIBLE);
        }

        this.binding.ivBackIcon.setOnClickListener((View.OnClickListener) (new View.OnClickListener() {
            public final void onClick(View it) {
                Context var10000 = CommonTitleView.this.getContext();
                if (var10000 == null) {
                    throw new NullPointerException("null cannot be cast to non-null type android.app.Activity");
                } else {
                    ((Activity) var10000).finish();
                }
            }
        }));
        boolean isHideLeftImage = typedArray.getBoolean(R.styleable.titleView_ctv_hideLeftImage, false);
        if (isHideLeftImage) {
            var12 = this.binding.ivBackIcon;
            Intrinsics.checkNotNullExpressionValue(var12, "binding.ivBackIcon");
            var12.setVisibility(View.GONE);
        }

        String leftText = typedArray.getString(R.styleable.titleView_ctv_leftText);
        AppCompatTextView var13;
        if (!TextUtils.isEmpty((CharSequence) leftText)) {
            var13 = this.binding.tvBackTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvBackTitle");
            var13.setText((CharSequence) leftText);
            var13 = this.binding.tvBackTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvBackTitle");
            var13.setVisibility(View.VISIBLE);
            var12 = this.binding.ivBackIcon;
            Intrinsics.checkNotNullExpressionValue(var12, "binding.ivBackIcon");
            var12.setVisibility(View.GONE);
        } else {
            var13 = this.binding.tvBackTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvBackTitle");
            var13.setVisibility(View.GONE);
        }

        String centerText = typedArray.getString(R.styleable.titleView_ctv_centerText);
        if (!TextUtils.isEmpty((CharSequence) centerText)) {
            var13 = this.binding.tvCenterTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvCenterTitle");
            var13.setText((CharSequence) centerText);
            var13 = this.binding.tvCenterTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvCenterTitle");
            var13.setVisibility(View.VISIBLE);
        } else {
            var13 = this.binding.tvCenterTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvCenterTitle");
            var13.setVisibility(View.GONE);
        }

        int centerTextColor = typedArray.getColor(R.styleable.titleView_ctv_centerTextColor, ContextCompat.getColor(this.getContext(), R.color.def_text_color_040));
        this.binding.tvCenterTitle.setTextColor(centerTextColor);
        String tvRightTitle = typedArray.getString(R.styleable.titleView_ctv_rightText);
        if (!TextUtils.isEmpty((CharSequence) tvRightTitle)) {
            var13 = this.binding.tvRightTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvRightTitle");
            var13.setText((CharSequence) tvRightTitle);
            var13 = this.binding.tvRightTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvRightTitle");
            var13.setVisibility(View.VISIBLE);
        } else {
            var13 = this.binding.tvRightTitle;
            Intrinsics.checkNotNullExpressionValue(var13, "binding.tvRightTitle");
            var13.setVisibility(View.GONE);
        }

        this.binding.tvCenterTitle.setTypeface(Typeface.DEFAULT_BOLD);

        Drawable drawableRight = typedArray.getDrawable(R.styleable.titleView_ctv_rightImageSrc);
        if (drawableRight != null) {
            this.binding.ivRightIcon.setImageDrawable(drawableRight);
            var12 = this.binding.ivRightIcon;
            Intrinsics.checkNotNullExpressionValue(var12, "binding.ivRightIcon");
            var12.setVisibility(View.VISIBLE);
        } else {
            var12 = this.binding.ivRightIcon;
            Intrinsics.checkNotNullExpressionValue(var12, "binding.ivRightIcon");
            var12.setVisibility(View.GONE);
        }

        this.binding.ivBackIcon.setOnClickListener((View.OnClickListener) (new View.OnClickListener() {
            public final void onClick(View it) {
                Context var10000 = CommonTitleView.this.getContext();
                if (var10000 == null) {
                    throw new NullPointerException("null cannot be cast to non-null type android.app.Activity");
                } else {
                    ((Activity) var10000).finish();
                }
            }
        }));
    }

    public final void hideLeftImage() {
        AppCompatImageView var10000 = this.binding.ivBackIcon;
        Intrinsics.checkNotNullExpressionValue(var10000, "binding.ivBackIcon");
        var10000.setVisibility(View.GONE);
    }

    public final void setTitle(@NotNull String title) {
        Intrinsics.checkNotNullParameter(title, "title");
        AppCompatTextView var10000 = this.binding.tvCenterTitle;
        Intrinsics.checkNotNullExpressionValue(var10000, "binding.tvCenterTitle");
        var10000.setText((CharSequence) title);
    }

    public final void setLeftClick(@Nullable View.OnClickListener onClickListener) {
        if (onClickListener != null) {
            this.binding.tvBackTitle.setOnClickListener(onClickListener);
            this.binding.ivBackIcon.setOnClickListener(onClickListener);
        }

    }

    public final void setRightText(@NotNull String text) {
        Intrinsics.checkNotNullParameter(text, "text");
        AppCompatTextView var10000 = this.binding.tvRightTitle;
        Intrinsics.checkNotNullExpressionValue(var10000, "binding.tvRightTitle");
        var10000.setText((CharSequence) text);
        var10000 = this.binding.tvRightTitle;
        Intrinsics.checkNotNullExpressionValue(var10000, "binding.tvRightTitle");
        var10000.setVisibility(View.VISIBLE);
    }

    public final void setRightIconClick(@Nullable View.OnClickListener onClickListener) {
        if (onClickListener != null) {
            this.binding.ivRightIcon.setOnClickListener(onClickListener);
            this.binding.tvRightTitle.setOnClickListener(onClickListener);
        }

    }

    @JvmOverloads
    public CommonTitleView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Intrinsics.checkNotNullParameter(context, "context");
        ViewCommonTitleBinding var10001 = ViewCommonTitleBinding.inflate(LayoutInflater.from(context), (ViewGroup) this);
        Intrinsics.checkNotNullExpressionValue(var10001, "ViewCommonTitleBinding.i…ater.from(context), this)");
        this.binding = var10001;
        this.initView(attrs, defStyleAttr);
    }

    // $FF: synthetic method
    public CommonTitleView(Context var1, AttributeSet var2, int var3, int var4, DefaultConstructorMarker var5) {
        this(var1, var2, var3);
        if ((var4 & 2) != 0) {
            var2 = (AttributeSet) null;
        }
        if ((var4 & 4) != 0) {
            var3 = 0;
        }
    }

    @JvmOverloads
    public CommonTitleView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 4, (DefaultConstructorMarker) null);
    }

    @JvmOverloads
    public CommonTitleView(@NotNull Context context) {
        this(context, (AttributeSet) null, 0, 6, (DefaultConstructorMarker) null);
    }
}
