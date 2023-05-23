package io.agora.scene.voice.ui.dialog

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiagcBinding
import io.agora.scene.voice.databinding.VoiceDialogChatroomBgmSettingBinding
import io.agora.scene.voice.databinding.VoiceDialogChatroomEarbackSettingBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import java.util.*

class RoomEarBackSettingSheetDialog: BaseSheetDialog<VoiceDialogChatroomEarbackSettingBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val isOn by lazy {
        arguments?.getBoolean(RoomAIAECSheetDialog.KEY_IS_ON, true) ?: true
    }

    var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    private var beforeDrawable: APNGDrawable? = null
    private var beforeTimer: Timer? = null

    private var afterDrawable: APNGDrawable? = null
    private var afterTimer: Timer? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomEarbackSettingBinding? {
        return VoiceDialogChatroomEarbackSettingBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        beforeDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_agc_sample_before.png")
        beforeDrawable?.registerAnimationCallback(object: Animatable2Compat.AnimationCallback(){
            var firstStart = true
            override fun onAnimationStart(drawable: Drawable?) {
                super.onAnimationStart(drawable)
                if(firstStart){
                    beforeDrawable?.pause()
                    firstStart = false
                }
            }
        })

    }

}
