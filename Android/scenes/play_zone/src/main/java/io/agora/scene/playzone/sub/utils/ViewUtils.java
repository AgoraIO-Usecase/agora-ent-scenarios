package io.agora.scene.playzone.sub.utils;

import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ViewUtils {

    /**
     * 对view的MarginTop增加设定的值
     *
     * @param view  操作的View
     * @param value 需要增加的值
     */
    public static void addMarginTop(View view, int value) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            marginLayoutParams.topMargin = marginLayoutParams.topMargin + value;
            view.setLayoutParams(marginLayoutParams);
        }
    }

    /** 设置MarginTop */
    public static void setMarginTop(View view, int value) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            marginLayoutParams.topMargin = value;
            view.setLayoutParams(marginLayoutParams);
        }
    }

    /** 获取MarginTop */
    public static int getMarginTop(View view) {
        if (view == null) return 0;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            return marginLayoutParams.topMargin;
        }
        return 0;
    }

    /** 设置Margin */
    public static void setMargin(View view, int value) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            marginLayoutParams.topMargin = value;
            marginLayoutParams.bottomMargin = value;
            marginLayoutParams.leftMargin = value;
            marginLayoutParams.rightMargin = value;
            view.setLayoutParams(marginLayoutParams);
        }
    }

    /** 获取Margin */
    public static int getMargin(View view) {
        if (view == null) return 0;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            return marginLayoutParams.topMargin;
        }
        return 0;
    }

    /** 设置宽高 */
    public static void setSize(View view, int size) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.width = size;
            params.height = size;
            view.setLayoutParams(params);
        }
    }

    /** 设置宽度 */
    public static void setWidth(View view, int width) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.width = width;
            view.setLayoutParams(params);
        }
    }

    /** 设置高度 */
    public static void setHeight(View view, int height) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.height = height;
            view.setLayoutParams(params);
        }
    }

    /** 设置paddingStart */
    public static void setPaddingStart(View view, int value) {
        if (view == null) return;
        view.setPaddingRelative(value, view.getPaddingTop(), view.getPaddingEnd(), view.getPaddingBottom());
    }

    /** 设置paddingEnd */
    public static void setPaddingEnd(View view, int value) {
        if (view == null) return;
        view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(), value, view.getPaddingBottom());
    }

    /** 设置paddingHorizontal */
    public static void setPaddingHorizontal(View view, int value) {
        if (view == null) return;
        view.setPaddingRelative(value, view.getPaddingTop(), value, view.getPaddingBottom());
    }

    public static void setTranslationX(View view, int value) {
        if (view == null) return;
        view.setTranslationX(value);
    }

    public static String getEditTextText(EditText editText) {
        Editable text = editText.getText();
        if (text == null) return null;
        return text.toString();
    }

}
