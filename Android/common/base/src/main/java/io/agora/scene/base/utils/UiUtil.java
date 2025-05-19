package io.agora.scene.base.utils;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Keep;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Keep
public class UiUtil {

    public static <T> Class<T> getGenericClass(Class<?> clz, int index) {
        Type type = clz.getGenericSuperclass();
        if (type == null) return null;
        return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    public static <T> T getViewBinding(Class<T> bindingClass, LayoutInflater inflater, ViewGroup container) {
        try {
            Method inflateMethod = bindingClass.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, Boolean.TYPE);
            return (T) inflateMethod.invoke(null, inflater, container, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}