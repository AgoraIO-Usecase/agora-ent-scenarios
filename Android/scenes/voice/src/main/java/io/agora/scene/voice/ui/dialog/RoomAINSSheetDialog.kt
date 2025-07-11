package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.OnItemChildClickListener
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.model.AINSModeBean
import io.agora.scene.voice.model.AINSSoundsBean
import io.agora.scene.voice.databinding.VoiceDialogChatroomAinsBinding
import io.agora.scene.voice.databinding.VoiceItemRoomAgoraAinsBinding
import io.agora.scene.voice.databinding.VoiceItemRoomAinsAuditionBinding
import io.agora.scene.voice.model.AINSType
import io.agora.scene.voice.model.constructor.RoomAINSConstructor
import io.agora.scene.voice.ui.adapter.viewholder.ChatroomAINSContentViewHolder
import io.agora.scene.voice.ui.adapter.viewholder.ChatroomAINSGapViewHolder
import io.agora.scene.voice.ui.adapter.viewholder.ChatroomAINSTitleViewHolder
import io.agora.scene.voice.ui.adapter.viewholder.RoomAINSModeViewHolder
import io.agora.scene.voice.ui.adapter.viewholder.RoomAINSSoundsViewHolder
import io.agora.scene.widget.toast.CustomToast

class RoomAINSSheetDialog constructor() : BaseBottomSheetDialogFragment<VoiceDialogChatroomAinsBinding>() {

    companion object {
        const val KEY_AINS_MODE = "ains_mode"
        const val KEY_AINS_MUSIC_MODE = "ains_music_mode"
        const val KEY_AINS_MIC_MODE = "ains_mic_mode"
        const val KEY_IS_ENABLE = "is_Enable"
    }

    private var anisModeAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAgoraAinsBinding, AINSModeBean, RoomAINSModeViewHolder>? =
        null
    private var anisSoundsAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAinsAuditionBinding, AINSSoundsBean, RoomAINSSoundsViewHolder>? =
        null

    private val anisModeList = mutableListOf<AINSModeBean>()

    private val anisSoundsList = mutableListOf<AINSSoundsBean>()

    private val anisMode by lazy {
        arguments?.getInt(KEY_AINS_MODE, ConfigConstants.AINSMode.AINS_Tradition_Weakness) ?: ConfigConstants.AINSMode.AINS_Tradition_Weakness
    }

    private val anisMusicMode by lazy {
        arguments?.getInt(KEY_AINS_MUSIC_MODE, ConfigConstants.AINSMode.AINS_Off) ?: ConfigConstants.AINSMode.AINS_Off
    }

    private val anisMicMode by lazy {
        arguments?.getInt(KEY_AINS_MIC_MODE, ConfigConstants.AINSMode.AINS_Off) ?: ConfigConstants.AINSMode.AINS_Off
    }

    private val isEnable by lazy {
        arguments?.getBoolean(KEY_IS_ENABLE, true) ?: true
    }

    var anisModeCallback: ((ainsModeBean: io.agora.scene.voice.model.AINSModeBean) -> Unit)? = null

    var anisSoundCallback: ((position: Int, anisSoundBean: io.agora.scene.voice.model.AINSSoundsBean) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        dialog?.setCanceledOnTouchOutside(false)
        anisModeList.addAll(
            RoomAINSConstructor.builderDefaultAINSList(
                view.context,
                anisMode,
                anisMusicMode,
                anisMicMode
            )
        )
        anisSoundsList.addAll(RoomAINSConstructor.builderDefaultSoundList(view.context))
        mBinding?.apply {
            ivBottomSheetBack.setOnClickListener {
                dismiss()
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
                    val anisMode = data ?: return
                    if (extData !is Int) return
                    if (anisMode.anisMode == extData) return

                    anisMode.anisMode = extData
                    anisModeAdapter?.notifyItemChanged(position)
                    anisModeCallback?.invoke(anisMode)

                    if (anisMode.type == AINSType.AINS_Default) {
                        if (anisMode.anisMode == ConfigConstants.AINSMode.AINS_Off) {   // Noise reduction off
                            anisModeAdapter?.apply {
                                val removed = dataList.removeIf {
                                    it.type == AINSType.AINS_Music || it.type == AINSType.AINS_Mic
                                }
                                if (removed) notifyDataSetChanged()
                            }

                        } else {  // Noise reduction on
                            var needAdd = false
                            anisModeAdapter?.apply {

                                if (dataList.find { it.type == AINSType.AINS_Music } == null) {
                                    dataList.add(
                                        RoomAINSConstructor.buildAIBean(view.context, AINSType.AINS_Music, anisMusicMode)
                                    )
                                    needAdd = true
                                }
                                if (dataList.find { it.type == AINSType.AINS_Mic } == null) {
                                    dataList.add(
                                        RoomAINSConstructor.buildAIBean(view.context, AINSType.AINS_Mic, anisMicMode)
                                    )
                                    needAdd = true
                                }
                                if (needAdd) notifyDataSetChanged()
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
                            CustomToast.showTips(R.string.voice_room_only_host_can_change_anis)
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

    fun updateAnisSoundsAdapter(position: Int, update: Boolean = true) {
        if (update) {
            anisSoundsAdapter?.notifyItemChanged(position)
        } else {
            anisSoundsAdapter?.selectedIndex = -1
            anisSoundsList[position].soundMode = ConfigConstants.AINSMode.AINS_Unknown
        }
    }
}