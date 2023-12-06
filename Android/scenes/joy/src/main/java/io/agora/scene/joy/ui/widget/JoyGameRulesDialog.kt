package io.agora.scene.joy.ui.widget

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.joy.R
import io.agora.scene.joy.databinding.JoyDialogGameRulesLayoutBinding
import io.agora.scene.joy.network.JoyGameDetailResult

class JoyGameRulesDialog : BaseBottomSheetDialogFragment<JoyDialogGameRulesLayoutBinding>() {

    companion object {
        const val Key_Game = "key_game"
        const val Key_IsOwner = "key_is_owner"
    }

    private val mGameInfo by lazy {
        arguments?.getSerializable(Key_Game) as JoyGameDetailResult
    }

    private val mIsOwner by lazy {
        arguments?.getBoolean(Key_IsOwner) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvIntroduceContent.text = mGameInfo.introduce
        if (mIsOwner) {
            mBinding.mtBottomSheetTitle.text = getText(R.string.joy_tips_for_launching_anchors)
        } else {
            mBinding.mtBottomSheetTitle.text = mGameInfo.name + getText(R.string.joy_rules_of_play)
        }
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }

    }
}