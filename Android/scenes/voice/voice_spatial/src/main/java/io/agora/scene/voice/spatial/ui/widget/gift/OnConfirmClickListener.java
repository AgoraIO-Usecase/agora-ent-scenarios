package io.agora.scene.voice.spatial.ui.widget.gift;

import android.view.View;

import io.agora.scene.voice.spatial.model.GiftBean;

public interface OnConfirmClickListener {
    void onConfirmClick(View view, Object bean);
    void onFirstItem(GiftBean firstBean);
}
