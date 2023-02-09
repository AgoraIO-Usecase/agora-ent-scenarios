package io.agora.scene.voice.ui.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class RoomAIAECSheetDialog: BaseSheetDialog<VoiceDialogChatroomAiaecBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val beforeSoundId = 201001
    private val afterSoundId = 201002

    private val isOn by lazy {
        arguments?.getBoolean(KEY_IS_ON, true) ?: true
    }

    var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    private var beforeDrawable: APNGDrawable? = null

    private var afterDrawable: APNGDrawable? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAiaecBinding? {
        return VoiceDialogChatroomAiaecBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        setupOnClickPlayButton()
        beforeDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_aec_sample_before.png")
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
        binding?.ivBefore?.setImageDrawable(beforeDrawable)

        afterDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_aec_sample_after.png")
        afterDrawable?.registerAnimationCallback(object: Animatable2Compat.AnimationCallback(){
            var firstStart = true
            override fun onAnimationStart(drawable: Drawable?) {
                super.onAnimationStart(drawable)
                if(firstStart){
                    afterDrawable?.pause()
                    firstStart = false
                }
            }
        })
        binding?.ivAfter?.setImageDrawable(afterDrawable)

        binding?.accbAEC?.isChecked = isOn
        binding?.accbAEC?.setOnCheckedChangeListener { _, isChecked ->
            onClickCheckBox?.invoke(isChecked)
        }
    }

    fun setupOnClickPlayButton() {
        binding?.btnBefore?.setOnClickListener {
            if (it.isSelected) { // stop play
                AgoraRtcEngineController.get().resetMediaPlayer()
                beforeDrawable?.stop()
                afterDrawable?.stop()
                it.isSelected = false
            } else { // start play
                val file = "sounds/voice_sample_aec_before.m4a"
                copyFileFromAssets(this.context!!, file, context?.externalCacheDir!!.absolutePath)?.apply {
                    AgoraRtcEngineController.get().playMusic(afterSoundId, this, 0)
                }
                it.isSelected = true
                binding?.btnAfter?.isSelected = false
                beforeDrawable?.start()
                beforeDrawable?.resume()
                afterDrawable?.stop()
            }
        }
        binding?.btnAfter?.setOnClickListener {
            if (it.isSelected) { // stop play
                AgoraRtcEngineController.get().resetMediaPlayer()
                beforeDrawable?.stop()
                afterDrawable?.stop()
                it.isSelected = false
            } else { // start play
                val file = "sounds/voice_sample_aec_after.m4a"
                copyFileFromAssets(this.context!!, file, context?.externalCacheDir!!.absolutePath)?.apply {
                    AgoraRtcEngineController.get().playMusic(afterSoundId, this, 0)
                }
                it.isSelected = true
                binding?.btnBefore?.isSelected = false
                afterDrawable?.start()
                afterDrawable?.resume()
                beforeDrawable?.stop()
            }
        }
    }

    fun copyFileFromAssets(context: Context, assetsFilePath: String, storagePath: String) : String? {
        var sPath = storagePath
        var aPath = assetsFilePath
        if (sPath.endsWith(File.separator)) {
            sPath = sPath.substring(0, storagePath.length - 1)
        }
        if (aPath.endsWith(File.separator)) {
            return null
        }
        sPath = sPath + File.separator + assetsFilePath
        val file = File(sPath)
        if (file.exists()) {
            return sPath
        }
        file.parentFile.mkdirs()
        val input = context.assets.open(aPath)
        readInputStream(sPath, input)
        return sPath
    }

    fun readInputStream(storagePath: String, inputStream: InputStream) {
        try {
            val file = File(storagePath)
            if (!file.exists()) {
                val fos = FileOutputStream(file)
                val buffer = ByteArray(inputStream.available())
                var lenth = -1
                while (((inputStream.read(buffer)).also { lenth = it }) != -1) {
                    fos.write(buffer, 0, lenth)
                }
                fos.flush()
                fos.close()
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
