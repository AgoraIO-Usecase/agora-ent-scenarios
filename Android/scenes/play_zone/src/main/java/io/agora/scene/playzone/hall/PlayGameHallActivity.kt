package io.agora.scene.playzone.hall

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.dp
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityGameHallLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameHeaderLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameTypeLayoutBinding
import io.agora.scene.playzone.service.api.PlayGameInfoModel
import io.agora.scene.playzone.service.api.PlayGameType

class PlayGameHallActivity : BaseViewBindingActivity<PlayZoneActivityGameHallLayoutBinding>() {

    companion object {
        private const val TAG = "RoomGameHallActivity"
    }

    private val mPlayZoneViewModel: PlayHallViewModel by lazy {
        ViewModelProvider(this)[PlayHallViewModel::class.java]
    }

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun getViewBinding(inflater: LayoutInflater): PlayZoneActivityGameHallLayoutBinding {
        return PlayZoneActivityGameHallLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleView.setLeftClick { finish() }

        binding.tvSwitchVendor.setOnClickListener {
            showVendorDialog()
        }
        binding.tvRoomList.setOnClickListener {
            startActivity(Intent(this, PlayRoomListActivity::class.java))
        }

        binding.currentlyHottest.setOnClickListener {
            finish()
        }

        val itemDecoration = object : DividerItemDecoration(this, HORIZONTAL) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                if (position != RecyclerView.NO_POSITION) {
                    var adapterStartPosition = 0
                    concatAdapter?.let { cAdapter ->
                        val totalWidth = binding.rvLeisureGame.measuredWidth
                        val space = (totalWidth - 4 * 66.dp.toInt() - 24.dp.toInt()) / 3

                        for (adapter in cAdapter.adapters) {
                            val itemCount = adapter.itemCount
                            Log.d("xxxxx", "$adapterStartPosition $itemCount")
                            if (position in adapterStartPosition until (adapterStartPosition + itemCount)) {
                                if ((position - adapterStartPosition) % 4 == 0) { // 最左边 item
                                    outRect.left = 12.dp.toInt()
                                    outRect.right = space / 2
                                } else if ((position - adapterStartPosition) % 4 == 3) { // 最右边 item
                                    outRect.right = 12.dp.toInt()
                                    outRect.left = space / 2
                                } else {
                                    outRect.left = space / 2
                                    outRect.right = space / 2
                                }
                                break
                            }
                            adapterStartPosition += itemCount
                        }
                    }
                }
            }
        }
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.play_zone_shape_transparent)!!)
        binding.rvLeisureGame.addItemDecoration(itemDecoration)
    }

    private var concatAdapter: ConcatAdapter? = null

    override fun requestData() {
        super.requestData()
        mPlayZoneViewModel.getGameList(mCurrentVendor)
        mPlayZoneViewModel.mGameListLiveData.observe(this) {

            val listAdapter = mutableListOf<RecyclerView.Adapter<*>>()
            val gameTypeAdapter = GameTypeAdapter(mutableListOf(""), GameTypeHolder::class.java)
            listAdapter.add(gameTypeAdapter)

            var headTitle = ""
            it?.forEach { subGameListModel ->
                headTitle = when (subGameListModel.gameType) {
                    PlayGameType.leisure_and_entertainment -> getString(R.string.play_zone_leisure_and_entertainment)
                    PlayGameType.voice_interaction -> getString(R.string.play_zone_voice_interaction)
                    PlayGameType.realtime_competition -> getString(R.string.play_zone_realtime_competition)
                    PlayGameType.classic_board_games -> getString(R.string.play_zone_classic_board_games)
                    PlayGameType.party_games -> getString(R.string.play_zone_party_games)
                }
                val headAdapter = GameHeadAdapter(mutableListOf(headTitle), GameHeadHolder::class.java)
                listAdapter.add(headAdapter)
                val gameAdapter = BaseRecyclerViewAdapter(subGameListModel.gameList, object :
                    OnItemClickListener<PlayGameInfoModel?> {
                    override fun onItemClick(
                        subGameInfo: PlayGameInfoModel,
                        view: View,
                        position: Int,
                        viewType: Long
                    ) {
                        if (!subGameInfo.gameUrl.isNullOrEmpty()) {
                            PagePilotManager.pageWebViewWithBrowser(subGameInfo.gameUrl)
                        } else {
                            showCreateRoomDialog(subGameInfo)
                        }
                    }
                }, GameInfoHolder::class.java)
                listAdapter.add(gameAdapter)
            }
            concatAdapter = ConcatAdapter(listAdapter)
            binding.rvLeisureGame.adapter = concatAdapter
        }
    }

    private fun showCreateRoomDialog(subGameInfo: PlayGameInfoModel) {
        val bundle = Bundle().apply {
            putSerializable(PlayCreateRoomDialog.Key_GameInfo, subGameInfo)
        }
        val dialog = PlayCreateRoomDialog(this).apply {
            setBundleArgs(bundle)
        }
        dialog.show(supportFragmentManager, "createDialog")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private var mCurrentVendor = GameVendor.Sub

    private fun showVendorDialog() {
        val bundle = Bundle().apply {
            putSerializable(PlayZoneVendorDialog.Key_Vendor, mCurrentVendor.value)
        }
        val dialog = PlayZoneVendorDialog().apply {
            setBundleArgs(bundle)
        }
        dialog.vendorCallback = {
            if (mCurrentVendor != it) {
                mCurrentVendor = it
                mPlayZoneViewModel.getGameList(mCurrentVendor)
                ToastUtils.showToast("切换到${it}")
            }
        }
        dialog.show(supportFragmentManager, "VendorDialog")
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    /**
     * 具体游戏 viewHolder
     *
     * @constructor
     *
     * @param mBinding
     */
    class GameInfoHolder constructor(mBinding: PlayZoneItemGameLayoutBinding) :
        BaseRecyclerViewAdapter.BaseViewHolder<PlayZoneItemGameLayoutBinding, PlayGameInfoModel?>(mBinding) {

        override fun binding(subGame: PlayGameInfoModel?, selectedIndex: Int) {
            subGame ?: return
            mBinding.tvGameName.text = subGame.gameName
            mBinding.ivGameIcon.setImageResource(subGame.gamePic)
        }
    }

    // 休闲玩法 adapter
    class GameTypeAdapter<B : ViewBinding, T, H : BaseRecyclerViewAdapter.BaseViewHolder<B, T>>(
        dataList: List<T>, viewHolderClass: Class<H>
    ) : BaseRecyclerViewAdapter<B, T, H>(dataList, viewHolderClass) {
        override fun onViewAttachedToWindow(holder: H) {
            super.onViewAttachedToWindow(holder)
            (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }
    }

    class GameTypeHolder constructor(mBinding: PlayZoneItemGameTypeLayoutBinding) :
        BaseRecyclerViewAdapter.BaseViewHolder<PlayZoneItemGameTypeLayoutBinding, String>(mBinding) {
        override fun binding(data: String?, selectedIndex: Int) {
            data ?: return
        }
    }

    // 游戏分类标题 adapter
    class GameHeadAdapter<B : ViewBinding, T, H : BaseRecyclerViewAdapter.BaseViewHolder<B, T>>(
        dataList: List<T>, viewHolderClass: Class<H>
    ) : BaseRecyclerViewAdapter<B, T, H>(dataList, viewHolderClass) {
        override fun onViewAttachedToWindow(holder: H) {
            super.onViewAttachedToWindow(holder)
            (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }
    }

    // 游戏分类标题 viewHolder
    class GameHeadHolder constructor(mBinding: PlayZoneItemGameHeaderLayoutBinding) :
        BaseRecyclerViewAdapter.BaseViewHolder<PlayZoneItemGameHeaderLayoutBinding, String>(mBinding) {
        override fun binding(data: String?, selectedIndex: Int) {
            data ?: return
            mBinding.tvGameTypeTitle.text = data
        }
    }
}