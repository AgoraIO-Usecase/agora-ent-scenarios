package io.agora.scene.joy.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.joy.databinding.JoyLiveDetailActivityBinding
import io.agora.scene.joy.service.JoyRoomInfo

class RoomLivingActivity : BaseViewBindingActivity<JoyLiveDetailActivityBinding>() {

    companion object {
        private const val TAG = "RoomLivingActivity"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun launch(context: Context,roomInfo: JoyRoomInfo) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_DETAIL_INFO,roomInfo)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): JoyLiveDetailActivityBinding {
        return JoyLiveDetailActivityBinding.inflate(inflater)
    }
}