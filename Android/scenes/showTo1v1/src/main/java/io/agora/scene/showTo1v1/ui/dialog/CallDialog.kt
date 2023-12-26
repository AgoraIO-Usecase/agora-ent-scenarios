package io.agora.scene.showTo1v1.ui.dialog

import android.app.Dialog
import android.content.Context
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo

enum class CallDialogState {
    None,
    Calling,
    Connecting
}

abstract class CallDialog constructor(
    private val context: Context,
    private val userInfo: ShowTo1v1UserInfo
) : Dialog(context, R.style.Show_to1v1Theme_Dialog_Bottom) {

    abstract fun updateCallState(state: CallDialogState)

}