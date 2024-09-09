package io.agora.scene.aichat.list.event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData

class AIChatEventViewModel constructor(val app: Application) : AndroidViewModel(app) {
    var unreadConversationLiveData = UnPeekLiveData<UnreadMessageEvent>()
    var unreadMessageLiveData = UnPeekLiveData<Boolean>()
}
