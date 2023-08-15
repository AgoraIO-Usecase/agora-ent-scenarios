package io.agora.scene.showTo1v1.ui

import android.os.Bundle
import android.view.LayoutInflater
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding

class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomListActivityBinding {
        return ShowTo1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


}