package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogRoomSoundSelectionBinding
import io.agora.scene.voice.databinding.VoiceItemHandsRaisedBinding
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.ui.adapter.viewholder.RoomMemberCountViewHolder
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener
import io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog
import io.agora.voice.common.utils.ThreadManager

class RoomMemberCountDialog constructor(
    private val onClickKickListener:OnClickKickMemberListener
): BaseFixedHeightSheetDialog<VoiceDialogRoomSoundSelectionBinding>(){

    private var roomMemberList = mutableListOf<VoiceMemberModel>()
    private var adapter: BaseRecyclerViewAdapter<VoiceItemHandsRaisedBinding,VoiceMemberModel, RoomMemberCountViewHolder>? = null
    private var currentMemberCount:Int = 0
    private var isFirst:Boolean = true

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogRoomSoundSelectionBinding {
        return VoiceDialogRoomSoundSelectionBinding.inflate(inflater, container, false)
    }

    interface OnClickKickMemberListener {
        fun onKickMember(member: VoiceMemberModel,index: Int)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        dialog?.setCanceledOnTouchOutside(true)
        binding?.apply {
            mtBottomSheetTitle.text = context?.resources?.getString(R.string.voice_room_invite_audience_title)
            setOnApplyWindowInsets(root)
            ivBottomSheetBack.visibility = View.GONE
            binding?.noDataLayout?.contentItem?.text = context?.resources?.getString(R.string.voice_empty_invite_hands)

            roomMemberList = ChatroomIMManager.getInstance().fetchRoomMembers()
            currentMemberCount = roomMemberList.size
            checkData()

            initAdapter(rvBottomSheetSoundSelection)
        }
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        adapter =
            BaseRecyclerViewAdapter(
                roomMemberList,
                null,
                object :
                    OnItemChildClickListener<VoiceMemberModel> {
                    override fun onItemChildClick(
                        data: VoiceMemberModel?,
                        extData: Any?,
                        view: View,
                        position: Int,
                        itemViewType: Long
                    ) {
                        data?.let { onClickKickListener.onKickMember(it,position) }
                    }
                },
                RoomMemberCountViewHolder::class.java
            )
        val config = ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build()
        val concatAdapter = ConcatAdapter(config, adapter)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = concatAdapter

    }

    fun notifyItemRemovedRefresh(index:Int){
        ThreadManager.getInstance().runOnMainThread {
            adapter?.deleteItem(index)
            if (currentMemberCount > 0){
                currentMemberCount --
            }
            checkData()
        }
    }

    fun notifyItemAddRefresh(){
        ThreadManager.getInstance().runOnMainThread {
            currentMemberCount ++
            checkData()
            if (currentMemberCount  > 0){
                if (isFirst){
                    adapter?.submitListAndPurge(roomMemberList)
                    isFirst = false
                } else{
                    adapter?.notifyItemInserted(roomMemberList.size - 1)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setDimAmount(0f) //设置布局
        val heightRadio = 0.7
        val h = (heightRadio * resources.displayMetrics.heightPixels).toInt()
        val viewRoot: FrameLayout? = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        viewRoot?.apply {
            layoutParams.width = -1
            layoutParams.height = h
        }
    }

    private fun checkData(){
        if (currentMemberCount == 0){
            binding?.noDataLayout?.baseLayout?.visibility = View.VISIBLE
            adapter?.notifyDataSetChanged()
        }else{
            binding?.noDataLayout?.baseLayout?.visibility = View.GONE
        }
    }
}