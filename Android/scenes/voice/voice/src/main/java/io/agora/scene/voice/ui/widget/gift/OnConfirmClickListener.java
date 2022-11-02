package io.agora.scene.voice.ui.widget.gift;

import android.view.View;

import io.agora.scene.voice.bean.GiftBean;

public interface OnConfirmClickListener {
    void onConfirmClick(View view, Object bean);
    void onFirstItem(GiftBean firstBean);
}
