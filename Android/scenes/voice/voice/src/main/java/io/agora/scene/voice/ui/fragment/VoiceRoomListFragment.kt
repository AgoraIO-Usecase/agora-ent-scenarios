package io.agora.scene.voice.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentRoomListLayoutBinding
import io.agora.scene.voice.model.VoiceRoomViewModel
import io.agora.scene.voice.service.VoiceRoomModel
import io.agora.scene.voice.ui.adapter.VoiceRoomListAdapter
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.voice.baseui.BaseUiFragment
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.config.RouterParams
import io.agora.voice.buddy.config.RouterPath
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.ToastTools.show

class VoiceRoomListFragment : BaseUiFragment<VoiceFragmentRoomListLayoutBinding>() {
    private lateinit var voiceRoomViewModel: VoiceRoomViewModel
    private var listAdapter: VoiceRoomListAdapter? = null

    private var curVoiceRoomModel: VoiceRoomModel? = null

    private var total = 0
    private var isEnd = false

    var itemCountListener: ((count: Int) -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentRoomListLayoutBinding {
        return VoiceFragmentRoomListLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceRoomViewModel::class.java]
        binding?.apply {
            initAdapter(recycler)
        }
        voiceRoomObservable()
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        val offsetPx = resources.getDimension(R.dimen.space_84dp)
        recyclerView.addItemDecoration(BottomOffsetDecoration(offsetPx.toInt()))
        listAdapter = VoiceRoomListAdapter(null, object : OnItemClickListener<VoiceRoomModel> {
            override fun onItemClick(voiceRoomModel: VoiceRoomModel, view: View, position: Int, viewType: Long) {
                onItemClick(voiceRoomModel)
            }
        }, VoiceRoomListAdapter.VoiceRoomListViewHolder::class.java)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listAdapter
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.roomListObservable().observe(requireActivity()) { response: Resource<List<VoiceRoomModel>> ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceRoomModel>>() {
                override fun onSuccess(dataList: List<VoiceRoomModel>?) {
                    binding?.swipeLayout?.isRefreshing = false
                    total = dataList?.size ?: 0
                    "Voice room list total：${total}".logD()
                    if (dataList == null || dataList.isEmpty()) return
                    isEnd = true
                    listAdapter?.submitListAndPurge(dataList)
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.swipeLayout?.isRefreshing = false
                }
            })
        }
        voiceRoomViewModel.checkPasswordObservable().observe(requireActivity()) { response ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(value: Boolean?) {
                    if (value == true) {
                        curVoiceRoomModel?.let {
                            goChatroomPage(it)
                        }
                    } else {
                        dismissLoading()
                        show(requireActivity(), getString(R.string.voice_room_check_password), Toast.LENGTH_SHORT)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.swipeLayout?.isRefreshing = false
                }
            })
        }
        voiceRoomViewModel.getRoomList(0,0)
    }

    private fun onItemClick(voiceRoomModel: VoiceRoomModel) {
        if (voiceRoomModel.isPrivate) {
            showInputDialog(voiceRoomModel)
        } else {
            goChatroomPage(voiceRoomModel)
        }
    }

    private fun goChatroomPage(voiceRoomModel: VoiceRoomModel) {
        ARouter.getInstance()
            .build(RouterPath.ChatroomPath)
            .withSerializable(RouterParams.KEY_CHATROOM_INFO, voiceRoomModel)
            .navigation()
        dismissLoading()
    }

    private fun showInputDialog(voiceRoomModel: VoiceRoomModel) {
        RoomEncryptionInputDialog()
            .leftText(activity!!.getString(R.string.voice_room_cancel))
            .rightText(activity!!.getString(R.string.voice_room_confirm))
            .setOnClickListener(object : RoomEncryptionInputDialog.OnClickBottomListener {
                override fun onCancelClick() {}
                override fun onConfirmClick(password: String) {
                    curVoiceRoomModel = voiceRoomModel
                    voiceRoomViewModel.checkPassword(voiceRoomModel.roomId, voiceRoomModel.roomId, password)
                    showLoading(false)
                }
            })
            .show(requireFragmentManager(), "encryptionInputDialog")
    }

    internal class BottomOffsetDecoration(private val mBottomOffset: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val dataSize = state.itemCount
            val position = parent.getChildAdapterPosition(view)
            if (dataSize > 0 && position == dataSize - 1) {
                outRect[0, 0, 0] = mBottomOffset
            } else {
                outRect[0, 0, 0] = 0
            }
        }
    }
}