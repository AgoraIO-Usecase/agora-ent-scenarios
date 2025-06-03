package io.agora.scene.playzone.live

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneDialogGameRulesLayoutBinding
import io.agora.scene.playzone.service.api.PlayGameInfoModel

class PlayGameRulesDialog : BaseBottomSheetDialogFragment<PlayZoneDialogGameRulesLayoutBinding>() {

    companion object {
        const val Key_Game = "key_game"
        const val Key_IsOwner = "key_is_owner"
    }

    private val mGameInfo by lazy {
        arguments?.getSerializable(Key_Game) as PlayGameInfoModel
    }

    private val mIsOwner by lazy {
        arguments?.getBoolean(Key_IsOwner) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvIntroduceContent.text = mGameInfo.gameName
            mBinding.mtBottomSheetTitle.text = mGameInfo.gameName + getText(R.string.play_zone_game_rules)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }
}