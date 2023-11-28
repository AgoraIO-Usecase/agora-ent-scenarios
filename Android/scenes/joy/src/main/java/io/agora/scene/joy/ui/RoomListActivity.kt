package io.agora.scene.joy.ui

import android.view.LayoutInflater
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.joy.databinding.JoyActivityRoomListBinding

class RoomListActivity : BaseViewBindingActivity<JoyActivityRoomListBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityRoomListBinding {
        return JoyActivityRoomListBinding.inflate(inflater)
    }
}