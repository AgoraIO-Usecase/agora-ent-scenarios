package io.agora.voice.common.ui.adapter.listener;

import android.view.View;

import androidx.annotation.NonNull;

public interface OnItemClickListener<T> {
    /**
     * For item data not null
     */
    default void onItemClick(@NonNull T data, @NonNull View view, int position, long viewType){

    }

    /**
     * For the null data item
     */
    default void onItemClick(@NonNull View view, int position, long viewType) {

    }
}
