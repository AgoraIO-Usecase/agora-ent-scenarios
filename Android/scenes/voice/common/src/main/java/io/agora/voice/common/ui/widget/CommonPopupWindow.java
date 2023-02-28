package io.agora.voice.common.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;


public abstract class CommonPopupWindow<V extends View, Binding extends ViewBinding> extends PopupWindow {
    private Context context;
    private OnDismissListener onDismissListener;


    private CommonPopupWindow(Context context, ViewDataBindingBuilder<Binding> builder) {
        getIntercept().intercept();
        setContentView(builder.mBinding.getRoot());
        setWidth(builder.mWidth);
        setHeight(builder.mHeight);
        setOutsideTouchable(builder.mOutsideTouchable);
        setBackgroundDrawable(builder.mBackground);
        setFocusable(builder.mFocusable);
        /*
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */
        setClippingEnabled(builder.mClippingEnabled);
        setAnimationStyle(builder.mAnimationStyle);
        onDismissListener = builder.onDismissListener;
        super.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (context instanceof Activity) {
                    WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
                    if (lp != null && lp.alpha != 1.0f) {
                        lp.alpha = 1.0f; /* 0.0~1.0*/
                        ((Activity) context).getWindow().setAttributes(lp);
                    }
                }
                if (onDismissListener != null) {
                    onDismissListener.onDismiss();
                }
            }
        });
    }


    private CommonPopupWindow(Context context, ViewBuilder<V> builder) {
        super(context);
        getIntercept().intercept();
        setContentView(builder.view);
        setWidth(builder.mWidth);
        setHeight(builder.mHeight);
        setOutsideTouchable(builder.mOutsideTouchable);
        setBackgroundDrawable(builder.mBackground);
        setFocusable(builder.mFocusable);
        /*
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */
        setClippingEnabled(builder.mClippingEnabled);
        setAnimationStyle(builder.mAnimationStyle);
        onDismissListener = builder.onDismissListener;
        super.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (context instanceof Activity) {
                    WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
                    if (lp != null && lp.alpha != 1.0f) {
                        lp.alpha = 1.0f; /* 0.0~1.0*/
                        ((Activity) context).getWindow().setAttributes(lp);
                    }
                }
                if (onDismissListener != null) {
                    onDismissListener.onDismiss();
                }
            }
        });
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (getContentView() != null) {
            Context context = getContentView().getContext();
            if (context instanceof Activity) {
                WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
                if (lp != null && lp.alpha != getAlpha()) {
                    lp.alpha = getAlpha();
                    ((Activity) context).getWindow().setAttributes(lp);
                }
            }
        }
        getIntercept().showBefore();
        super.showAsDropDown(anchor, xoff, yoff);
        getIntercept().showAfter();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (getContentView() != null) {
            Context context = getContentView().getContext();
            if (context instanceof Activity) {
                WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
                if (lp != null && lp.alpha != getAlpha()) {
                    lp.alpha = getAlpha();
                    ((Activity) context).getWindow().setAttributes(lp);
                }
            }
        }
        getIntercept().showBefore();
        super.showAtLocation(parent, gravity, x, y);
        getIntercept().showAfter();
    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    abstract float getAlpha();

    abstract CommonPopupWindow getInstance();

    abstract InterceptTransform getIntercept();

    public static class ViewDataBindingBuilder<Binding extends ViewBinding> {
        private Binding mBinding;
        private int mWidth;
        private int mHeight;
        private boolean mOutsideTouchable;
        private ViewEvent<Binding> mEvent;
        private Drawable mBackground;
        private boolean mFocusable;
        /**
         * 设置窗口弹出时背景的透明度
         * 0f（透明）~1.0f（正常）
         * 设置了alpha时需要在onDismiss恢复窗口的alpha至默认值即1.0f
         */
        private float alpha = 1.0f;
        /**
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */
        private boolean mClippingEnabled = true;
        private OnShowBefore<Binding> mOnShowBefore;
        private OnShowAfter<Binding> mOnShowAfter;
        private int mAnimationStyle = -1;
        private OnDismissListener onDismissListener;

        public ViewDataBindingBuilder<Binding> viewDataBinding(Binding mBinding) {
            this.mBinding = mBinding;
            return this;
        }

        public ViewDataBindingBuilder<Binding> width(int width) {
            this.mWidth = width;
            return this;
        }

        public ViewDataBindingBuilder<Binding> height(int height) {
            this.mHeight = height;
            return this;
        }

        public ViewDataBindingBuilder<Binding> outsideTouchable(boolean outsideTouchable) {
            this.mOutsideTouchable = outsideTouchable;
            return this;
        }

        public ViewDataBindingBuilder<Binding> backgroundDrawable(Drawable background) {
            this.mBackground = background;
            return this;
        }

        public ViewDataBindingBuilder<Binding> focusable(boolean focusable) {
            this.mFocusable = focusable;
            return this;
        }

        public ViewDataBindingBuilder<Binding> alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public ViewDataBindingBuilder<Binding> clippingEnabled(boolean clippingEnabled) {
            this.mClippingEnabled = clippingEnabled;
            return this;
        }

        public ViewDataBindingBuilder<Binding> onShowBefore(OnShowBefore<Binding> showBefore) {
            this.mOnShowBefore = showBefore;
            return this;
        }

        public ViewDataBindingBuilder<Binding> onShowAfter(OnShowAfter<Binding> showAfter) {
            this.mOnShowAfter = showAfter;
            return this;
        }

        public ViewDataBindingBuilder<Binding> animationStyle(int animationStyle) {
            this.mAnimationStyle = animationStyle;
            return this;
        }

        public ViewDataBindingBuilder<Binding> intercept(ViewEvent<Binding> event) {
            this.mEvent = event;
            return this;
        }

        public ViewDataBindingBuilder<Binding> onDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        public <V extends View> CommonPopupWindow<V, Binding> build(Context context) {
            return new CommonPopupWindow<V, Binding>(context, this) {
                @Override
                public float getAlpha() {
                    return alpha;
                }

                @Override
                CommonPopupWindow getInstance() {
                    return this;
                }

                @Override
                InterceptTransform getIntercept() {
                    return new InterceptTransform<Binding>() {
                        @Override
                        public void showBefore() {
                            if (mOnShowBefore != null) {
                                mOnShowBefore.showBefore(getInstance(), mBinding);
                            }
                        }

                        @Override
                        public void showAfter() {
                            if (mOnShowAfter != null) {
                                mOnShowAfter.showAfter(getInstance(), mBinding);
                            }
                        }

                        @Override
                        public void intercept() {
                            if (mEvent != null) {
                                mEvent.getView(getInstance(), mBinding);
                            }
                        }
                    };
                }
            };
        }
    }

    public static class ViewBuilder<V extends View> {
        private V view;
        private int mWidth;
        private int mHeight;
        private boolean mOutsideTouchable;
        private ViewEvent<V> mEvent;
        private Drawable mBackground;
        private boolean mFocusable;
        /**
         * 设置窗口弹出时背景的透明度
         * 0f（透明）~1.0f（正常）
         * 设置了alpha时需要在onDismiss恢复窗口的alpha至默认值即1.0f
         */
        private float alpha = 1.0f;
        /**
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */
        private boolean mClippingEnabled = true;
        private OnShowBefore<V> mOnShowBefore;
        private OnShowAfter<V> mOnShowAfter;
        private int mAnimationStyle = -1;
        private OnDismissListener onDismissListener;

        public ViewBuilder<V> view(@NonNull V view) {
            this.view = view;
            return this;
        }

        public ViewBuilder<V> width(int width) {
            this.mWidth = width;
            return this;
        }

        public ViewBuilder<V> height(int height) {
            this.mHeight = height;
            return this;
        }

        public ViewBuilder<V> outsideTouchable(boolean outsideTouchable) {
            this.mOutsideTouchable = outsideTouchable;
            return this;
        }

        public ViewBuilder<V> backgroundDrawable(Drawable background) {
            this.mBackground = background;
            return this;
        }

        public ViewBuilder<V> focusable(boolean focusable) {
            this.mFocusable = focusable;
            return this;
        }

        public ViewBuilder<V> alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public ViewBuilder<V> clippingEnabled(boolean clippingEnabled) {
            this.mClippingEnabled = clippingEnabled;
            return this;
        }

        public ViewBuilder<V> onShowBefore(OnShowBefore<V> showBefore) {
            this.mOnShowBefore = showBefore;
            return this;
        }

        public ViewBuilder<V> onShowAfter(OnShowAfter<V> showAfter) {
            this.mOnShowAfter = showAfter;
            return this;
        }

        public ViewBuilder<V> animationStyle(int animationStyle) {
            this.mAnimationStyle = animationStyle;
            return this;
        }

        public ViewBuilder<V> intercept(ViewEvent<V> event) {
            this.mEvent = event;
            return this;
        }

        public ViewBuilder<V> onDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        public <T extends ViewBinding> CommonPopupWindow<V, T> build(Context context) {
            return new CommonPopupWindow<V, T>(context, this) {

                @Override
                public float getAlpha() {
                    return alpha;
                }

                @Override
                CommonPopupWindow getInstance() {
                    return this;
                }

                @Override
                InterceptTransform getIntercept() {
                    return new InterceptTransform<V>() {
                        @Override
                        public void showBefore() {
                            if (mOnShowBefore != null) {
                                mOnShowBefore.showBefore(getInstance(), view);
                            }
                        }

                        @Override
                        public void showAfter() {
                            if (mOnShowAfter != null) {
                                mOnShowAfter.showAfter(getInstance(), view);
                            }
                        }

                        @Override
                        public void intercept() {
                            if (mEvent != null) {
                                mEvent.getView(getInstance(), view);
                            }
                        }
                    };
                }
            };
        }
    }

    public interface ViewEvent<T> {
        void getView(CommonPopupWindow popupWindow, T view);
    }

    public static abstract class InterceptTransform<V> {
        public abstract void showBefore();

        public abstract void showAfter();

        public abstract void intercept();
    }

    public interface OnShowBefore<V> {
        void showBefore(CommonPopupWindow popupWindow, V view);
    }

    public interface OnShowAfter<V> {
        void showAfter(CommonPopupWindow popupWindow, V view);
    }

}









