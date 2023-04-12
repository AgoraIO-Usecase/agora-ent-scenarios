package io.agora.scene.voice.ui.widget.gift

import io.agora.voice.common.ui.BaseUiFragment
import io.agora.scene.voice.model.GiftBean
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import io.agora.scene.voice.ui.widget.recyclerview.PagingScrollHelper
import io.agora.scene.voice.ui.widget.recyclerview.HorizontalPageLayoutManager
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import io.agora.scene.voice.databinding.VoiceFragmentGiftListLayoutBinding
import io.agora.voice.common.ui.adapter.listener.OnAdapterItemClickListener
import io.agora.voice.common.utils.DeviceTools

class LiveGiftListFragment : BaseUiFragment<VoiceFragmentGiftListLayoutBinding>(),
    OnAdapterItemClickListener {
    private var adapter: GiftListAdapter? = null
    private var giftBean: GiftBean? = null
    private var listener: OnConfirmClickListener? = null
    private var position = 0

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentGiftListLayoutBinding {
        return VoiceFragmentGiftListLayoutBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = arguments
        if (null != data) position = data.getInt("position")
        initView()
    }

    private fun initView() {
        binding?.apply {
            val snapHelper =
                PagingScrollHelper()
            val manager =
                HorizontalPageLayoutManager(
                    1,
                    4)
            rvList.setHasFixedSize(true)
            rvList.layoutManager = manager

            //设置item 间距
            val itemDecoration = DividerItemDecoration(root.context, DividerItemDecoration.VERTICAL)
            val drawable = GradientDrawable()
            drawable.setSize(DeviceTools.dp2px(root.context, 3f), 0)
            itemDecoration.setDrawable(drawable)
            rvList.addItemDecoration(itemDecoration)
            adapter = GiftListAdapter()
            rvList.adapter = adapter
            snapHelper.setUpRecycleView(rvList)
            snapHelper.updateLayoutManger()
            snapHelper.scrollToPosition(0)
            rvList.isHorizontalScrollBarEnabled = true
        }
        adapter?.setOnItemClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        adapter?.data = GiftRepository.getGiftsByPage(context, position)
        if (!adapter?.data.isNullOrEmpty()){
            adapter?.setSelectedPosition(0)
            listener?.onFirstItem(adapter?.getItem(0))
        }
    }

    override fun onItemClick(view: View, position: Int) {
        giftBean = adapter?.getItem(position)
        adapter?.getItem(position)?.let {
            it.isChecked = !it.isChecked
            if (it.isChecked) {
                adapter?.setSelectedPosition(position)
            } else {
                adapter?.setSelectedPosition(-1)
            }
            listener?.onConfirmClick(view, it)
        }

    }

    fun setOnItemSelectClickListener(listener: OnConfirmClickListener?) {
        this.listener = listener
    }
}