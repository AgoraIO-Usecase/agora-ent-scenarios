package io.agora.scene.pure1v1.ui

import android.app.Dialog
import android.content.Context
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.service.UserInfo

enum class CallDialogState {
    None,
    Calling,
    Connecting
}

abstract class CallDialog(
    private val context: Context,
    private val userInfo: UserInfo
) : Dialog(context, R.style.Pure1v1Theme_Dialog_Bottom) {

    abstract fun updateCallState(state: CallDialogState)

}