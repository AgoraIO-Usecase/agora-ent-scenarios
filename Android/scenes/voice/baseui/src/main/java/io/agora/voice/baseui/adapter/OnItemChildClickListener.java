package io.agora.voice.baseui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnItemChildClickListener<T> {
    default void onItemChildClick(@Nullable T data, Object extData, @NonNull View view, int position, long itemViewType){

    }
}
