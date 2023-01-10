package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener
import io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog
import io.agora.voice.common.utils.ToastTools
import io.agora.voice.common.constant.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.model.AINSModeBean
import io.agora.scene.voice.model.AINSSoundsBean
import io.agora.scene.voice.databinding.VoiceDialogChatroomAinsBinding
import io.agora.scene.voice.databinding.VoiceItemRoomAgoraAinsBinding
import io.agora.scene.voice.databinding.VoiceItemRoomAinsAuditionBinding
import io.agora.scene.voice.model.constructor.RoomAINSConstructor
import io.agora.scene.voice.ui.ainoise.*

class RoomAINSSheetDialog constructor() : BaseFixedHeightSheetDialog<VoiceDialogChatroomAinsBinding>() {

    companion object {
        const val KEY_AINS_MODE = "ains_mode"
        const val KEY_IS_ENABLE = "is_Enable"
    }

    private var anisModeAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAgoraAinsBinding, AINSModeBean, RoomAINSModeViewHolder>? =
        null
    private var anisSoundsAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAinsAuditionBinding, AINSSoundsBean, RoomAINSSoundsViewHolder>? =
        null

    private val anisModeList = mutableListOf<AINSModeBean>()

    private val anisSoundsList = mutableListOf<AINSSoundsBean>()

    private val anisMode by lazy {
        arguments?.getInt(KEY_AINS_MODE, ConfigConstants.AINSMode.AINS_Medium) ?: ConfigConstants.AINSMode.AINS_Medium
    }

    private val isEnable by lazy {
        arguments?.getBoolean(KEY_IS_ENABLE, true) ?: true
    }

    var anisModeCallback: ((ainsModeBean: io.agora.scene.voice.model.AINSModeBean) -> Unit)? = null

    var anisSoundCallback: ((position: Int, anisSoundBean: io.agora.scene.voice.model.AINSSoundsBean) -> Unit)? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAinsBinding {
        return VoiceDialogChatroomAinsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        dialog?.setCanceledOnTouchOutside(false)
        anisModeList.addAll(RoomAINSConstructor.builderDefaultAINSList(view.context, anisMode))
        anisSoundsList.addAll(RoomAINSConstructor.builderDefaultSoundList(view.context))
        binding?.apply {
            setOnApplyWindowInsets(root)
            ivBottomSheetBack.setOnClickListener {
                onHandleOnBackPressed()
            }
            initAdapter(rvNoiseSuppression)
        }
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        val anisModeHeaderAdapter = BaseRecyclerViewAdapter(
            mutableListOf(getString(R.string.voice_chatroom_ains_settings)),
            ChatroomAINSTitleViewHolder::class.java
        )
        anisModeAdapter = BaseRecyclerViewAdapter(
            anisModeList,
            null,
            object :
                OnItemChildClickListener<AINSModeBean> {

                override fun onItemChildClick(
                    data: AINSModeBean?,
                    extData: Any?,
                    view: View,
                    position: Int,
                    itemViewType: Long
                ) {
                    data?.let { anisMode ->
                        if (extData is Int) {
                            if (anisMode.anisMode == extData) {
                                return
                            } else {
                                anisMode.anisMode = extData
                                anisModeAdapter?.notifyItemChanged(position)
                                anisModeCallback?.invoke(anisMode)
                            }
                        }
                    }
                }
            },
            RoomAINSModeViewHolder::class.java
        )
        val anisIntroduceGap1Adapter = BaseRecyclerViewAdapter(
            mutableListOf(""), ChatroomAINSGapViewHolder::class.java
        )
        val anisIntroduceHeaderAdapter = BaseRecyclerViewAdapter(
            mutableListOf(getString(R.string.voice_chatroom_agora_ains)),
            ChatroomAINSTitleViewHolder::class.java
        )
        val anisIntroduceContentAdapter = BaseRecyclerViewAdapter(
            mutableListOf(
                getString(R.string.voice_chatroom_ains_introduce)
            ), ChatroomAINSContentViewHolder::class.java
        )
        val anisIntroduceGap2Adapter = BaseRecyclerViewAdapter(
            mutableListOf(""), ChatroomAINSGapViewHolder::class.java
        )
        val anisSoundsHeaderAdapter = BaseRecyclerViewAdapter(
            mutableListOf(getString(R.string.voice_chatroom_agora_ains_supports_sounds)),
            ChatroomAINSTitleViewHolder::class.java
        )
        anisSoundsAdapter = BaseRecyclerViewAdapter(
            anisSoundsList,
            null,
            object :
                OnItemChildClickListener<AINSSoundsBean> {

                override fun onItemChildClick(
                    data: AINSSoundsBean?,
                    extData: Any?,
                    view: View,
                    position: Int,
                    itemViewType: Long
                ) {
                    data?.let { anisSound ->
                        if (isEnable) {
                            if (extData is Int) {
                                if (anisSound.soundMode == extData) {
                                    return
                                } else {
                                    val selectedIndex = anisSoundsAdapter?.selectedIndex ?: -1
                                    if (selectedIndex >= 0) {
                                        anisSoundsAdapter?.dataList?.get(selectedIndex)?.soundMode =
                                            ConfigConstants.AINSMode.AINS_Unknown
                                        anisSoundsAdapter?.notifyItemChanged(selectedIndex)
                                    }
                                    anisSoundsAdapter?.selectedIndex = position
                                    anisSound.soundMode = extData
//                                    anisSoundsAdapter?.notifyItemChanged(position)
                                    anisSoundCallback?.invoke(position, anisSound)
                                }
                            } else {
                                // nothing
                            }
                        } else {
                            activity?.let {
                                ToastTools.showTips(it, getString(R.string.voice_room_only_host_can_change_anis))
                            }
                        }
                    }
                }
            },
            RoomAINSSoundsViewHolder::class.java
        )
        val config = ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build()
        val concatAdapter = ConcatAdapter(
            config,
            anisModeHeaderAdapter, anisModeAdapter,
            anisIntroduceGap1Adapter,
            anisIntroduceHeaderAdapter, anisIntroduceContentAdapter,
            anisIntroduceGap2Adapter,
            anisSoundsHeaderAdapter, anisSoundsAdapter,
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
//        context?.let {
//            recyclerView.addItemDecoration(
//                MaterialDividerItemDecoration(it, MaterialDividerItemDecoration.VERTICAL).apply {
//                    dividerColor = ResourcesTools.getColor(it.resources, R.color.divider_color_1F979797)
//                    dividerThickness = 1.dp.toInt()
//                    dividerInsetStart = 15.dp.toInt()
//                    dividerInsetEnd = 15.dp.toInt()
//                }
//            )
//        }
        recyclerView.adapter = concatAdapter
    }

    /**
     * 播放时候更新ui
     */
    fun updateAnisSoundsAdapter(position: Int, update: Boolean = true) {
        if (update) {
            anisSoundsAdapter?.notifyItemChanged(position)
        } else {
            anisSoundsAdapter?.selectedIndex = -1
            anisSoundsList[position].soundMode = ConfigConstants.AINSMode.AINS_Unknown
        }
    }
}