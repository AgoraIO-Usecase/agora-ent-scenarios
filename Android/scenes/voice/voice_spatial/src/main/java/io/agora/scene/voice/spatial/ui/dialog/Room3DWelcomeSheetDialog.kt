package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomWelcomeBinding
import io.agora.voice.common.ui.dialog.BaseSheetDialog

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述: 3D音频空间欢迎页
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/2/4
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 **/
class Room3DWelcomeSheetDialog : BaseSheetDialog<VoiceSpatialDialogRoomWelcomeBinding>() {

    companion object {
        // 是否需要展示
        var needShow = true
    }

    // 绑定ViewBinding
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceSpatialDialogRoomWelcomeBinding? {
        return VoiceSpatialDialogRoomWelcomeBinding.inflate(inflater, container, false)
    }

    // xxx
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            // 事件监听
            mbNext.setOnClickListener {
                needShow = false
                dismiss()
            }
        }

    }
}