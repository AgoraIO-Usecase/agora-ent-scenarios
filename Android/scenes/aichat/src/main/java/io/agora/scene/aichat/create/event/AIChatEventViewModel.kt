package io.agora.scene.aichat.create.event

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData

class AIChatEventViewModel : ViewModel() {
    var unreadConversationLiveData = UnPeekLiveData<UnreadMessageEvent>()
    var unreadMessageLiveData = UnPeekLiveData<Boolean>()
}
