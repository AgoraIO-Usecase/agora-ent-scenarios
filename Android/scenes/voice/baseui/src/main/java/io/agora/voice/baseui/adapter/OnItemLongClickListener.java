package io.agora.voice.baseui.adapter;

import android.view.View;

/**
 * Item long click listener
 */
public interface OnItemLongClickListener {
    /**
     * Item long click
     * @param view
     * @param position
     */
    boolean onItemLongClick(View view, int position);
}
