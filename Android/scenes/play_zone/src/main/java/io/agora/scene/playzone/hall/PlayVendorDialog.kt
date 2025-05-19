package io.agora.scene.playzone.hall

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneDialogGameVendorLayoutBinding

enum class GameVendor constructor(val value: Int) {
    Sub(0),
    YYGame(1),
    GroupPlay(2)
}

class PlayZoneVendorDialog : BaseBottomSheetDialogFragment<PlayZoneDialogGameVendorLayoutBinding>() {

    companion object {
        const val Key_Vendor = "key_vendor"
    }

    private val mVendor by lazy {
        arguments?.getInt(Key_Vendor, GameVendor.Sub.value) ?: GameVendor.Sub.value
    }

    var vendorCallback: ((GameVendor) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (mVendor) {
            GameVendor.YYGame.value -> {
                mBinding.tvVendorYY.setTextColor(ResourcesCompat.getColor(resources, R.color.play_zone_684, null))
            }

            GameVendor.GroupPlay.value -> {
                mBinding.tvVendorGroupPlay.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.play_zone_684,
                        null
                    )
                )
            }

            else -> {
                mBinding.tvVendorSub.setTextColor(ResourcesCompat.getColor(resources, R.color.play_zone_684, null))
            }
        }

        mBinding.tvVendorSub.setOnClickListener {
            vendorCallback?.invoke(GameVendor.Sub)
            dismiss()
        }
        mBinding.tvVendorYY.setOnClickListener {
            vendorCallback?.invoke(GameVendor.YYGame)
            dismiss()
        }
        mBinding.tvVendorGroupPlay.setOnClickListener {
            vendorCallback?.invoke(GameVendor.GroupPlay)
            dismiss()
        }
        mBinding.btnCancel.setOnClickListener {
            dismiss()
        }

    }
}