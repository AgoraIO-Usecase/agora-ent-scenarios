package io.agora.scene.base.component;

import android.view.View;

import androidx.annotation.NonNull;

public interface OnItemClickListener<T> {
    /**
     * For item data not null
     */
    default void onItemClick(@NonNull T data, View view, int position, long viewType){

    }

    /**
     * For the null data item
     */
    default void onItemClick(View view, int position, long viewType) {

    }
}
