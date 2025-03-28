package io.agora.scene.playzone.hall

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.dp
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityGameHallLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameHeaderLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameTypeLayoutBinding
import io.agora.scene.playzone.live.PlayWebViewActivity
import io.agora.scene.playzone.service.PlayChatRoomService
import io.agora.scene.playzone.service.PlayZoneServiceProtocol
import io.agora.scene.playzone.service.api.PlayApiManager
import io.agora.scene.playzone.service.api.PlayGameInfoModel
import io.agora.scene.playzone.service.api.PlayGameType
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.playzone.BuildConfig

class PlayGameHallActivity : BaseViewBindingActivity<PlayZoneActivityGameHallLayoutBinding>() {

    companion object {
        private const val TAG = "RoomGameHallActivity"
    }

    private val mPlayZoneViewModel: PlayCreateViewModel by lazy {
        ViewModelProvider(this)[PlayCreateViewModel::class.java]
    }

    override fun getViewBinding(inflater: LayoutInflater): PlayZoneActivityGameHallLayoutBinding {
        return PlayZoneActivityGameHallLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
        PlayZoneServiceProtocol.ROOM_AVAILABLE_DURATION = SceneConfigManager.joyExpireTime * 1000L
        if (BuildConfig.SUB_APP_KEY.isEmpty() || BuildConfig.IM_APP_KEY.isEmpty()) {
            CustomToast.show(R.string.play_zone_sub_key_empty)
            finish()
        }
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
                            if (position in adapterStartPosition until (adapterStartPosition + itemCount)) {
                                if ((position - adapterStartPosition) % 4 == 0) { // Leftmost item
                                    outRect.left = 12.dp.toInt()
                                    outRect.right = space / 2
                                } else if ((position - adapterStartPosition) % 4 == 3) { // Rightmost item
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
        mPlayZoneViewModel.getRoomList()
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
                            PlayWebViewActivity.startActivity(this@PlayGameHallActivity, subGameInfo.gameUrl)
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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mPlayZoneViewModel.checkLoginIm()
        showLoadingView()
        mPlayZoneViewModel.loginImLiveData.observe(this) {
            hideLoadingView()
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
        PlayChatRoomService.chatRoomService.imManagerService.logoutChat {
            PlayChatRoomService.reset()
        }
        PlayZoneServiceProtocol.destroy()
        PlayApiManager.reset()
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
     * Game item viewHolder
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

    // Leisure games adapter
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

    // Game category title adapter
    class GameHeadAdapter<B : ViewBinding, T, H : BaseRecyclerViewAdapter.BaseViewHolder<B, T>>(
        dataList: List<T>, viewHolderClass: Class<H>
    ) : BaseRecyclerViewAdapter<B, T, H>(dataList, viewHolderClass) {
        override fun onViewAttachedToWindow(holder: H) {
            super.onViewAttachedToWindow(holder)
            (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }
    }

    // Game category title viewHolder 
    class GameHeadHolder constructor(mBinding: PlayZoneItemGameHeaderLayoutBinding) :
        BaseRecyclerViewAdapter.BaseViewHolder<PlayZoneItemGameHeaderLayoutBinding, String>(mBinding) {
        override fun binding(data: String?, selectedIndex: Int) {
            data ?: return
            mBinding.tvGameTypeTitle.text = data
        }
    }
}