package io.agora.scene.cantata.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.cantata.databinding.CantataActivityRoomLivingBinding
import io.agora.scene.cantata.service.JoinRoomOutputModel

class RoomLivingActivity : BaseViewBindingActivity<CantataActivityRoomLivingBinding>() {

    companion object{
        private const val EXTRA_ROOM_INFO = "roomInfo"
        fun launch(context: Context, roomInfo: JoinRoomOutputModel) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): CantataActivityRoomLivingBinding {
        return CantataActivityRoomLivingBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (savedInstanceState != null) {
            finish()
            return
        }
        window.decorView.keepScreenOn = true
        setOnApplyWindowInsetsListener(binding.superLayout)
    }
}