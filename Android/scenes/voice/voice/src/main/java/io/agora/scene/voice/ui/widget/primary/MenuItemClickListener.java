package io.agora.scene.voice.ui.widget.primary;

import android.view.View;

public interface MenuItemClickListener {
    void onChatExtendMenuItemClick(int itemId, View view);
    void onInputLayoutClick();
    void onSendMessage(String content);
}
