package io.agora.scene.playzone.hall

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.agora.scene.base.GlideApp
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.dp
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityGameHallLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameBannerLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameHeaderLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemGameTypeLayoutBinding
import io.agora.scene.playzone.service.api.PlayApiManager
import io.agora.scene.playzone.service.api.PlayGameInfoModel
import io.agora.scene.playzone.service.api.PlayGameType
import io.agora.scene.playzone.service.api.PlayZoneGameBanner
import java.util.Timer
import java.util.TimerTask

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

    init {
        PlayApiManager.setBaseURL(ServerConfig.toolBoxUrl)
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

        binding.vpGame.adapter = mGameInfoAdapter
        binding.dotIndicator.setViewPager2(binding.vpGame, true)
        binding.vpGame.registerOnPageChangeCallback(onPageCallback)
        binding.vpGame.getChildAt(0).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> stopAutoScroll()
                MotionEvent.ACTION_UP -> startAutoScroll()
            }
            return@setOnTouchListener false
        }

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
        mPlayZoneViewModel.gameConfig()
        mPlayZoneViewModel.mGameConfigLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                // 头尾各添加一个数据，无缝循环播放
                val first = it.first()
                val last = it.last()
                val dataList = mutableListOf<PlayZoneGameBanner>()
                dataList.add(last)
                dataList.addAll(it)
                dataList.add(first)
                mGameInfoAdapter.setDataList(dataList)

                binding.vpGame.currentItem = 1
                startAutoScroll()
            }
        }

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
                }, GameInfoeHolder::class.java)
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
        binding.vpGame.unregisterOnPageChangeCallback(onPageCallback)
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

    private var mCurrentPos = 1
    private var mTimer: Timer? = null

    private val mGameInfoAdapter by lazy {
        GameBannerAdapter(emptyList(), itemClick = {
            ToastUtils.showToast("click $it")
        })
    }


    private val onPageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            mCurrentPos = position
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            //只有在空闲状态，才让自动滚动
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                if (mCurrentPos == 0) {
                    binding.vpGame.setCurrentItem(mGameInfoAdapter.itemCount - 2, false)
                }
                if (mCurrentPos == mGameInfoAdapter.itemCount - 1) {
                    binding.vpGame.setCurrentItem(1, false)
                }
            }
        }
    }

    private fun startAutoScroll() {
        mTimer?.cancel()
        mTimer = Timer()
        mTimer?.schedule(object : TimerTask() {
            override fun run() {
                mMainHandler.post {
                    binding.vpGame.apply {
                        if (currentItem + 1 == mGameInfoAdapter.itemCount - 1) {
                            setCurrentItem(1, false)
                        } else if (currentItem + 1 < mGameInfoAdapter.itemCount - 1) {
                            setCurrentItem(currentItem + 1, true)
                        }
                    }
                }
            }
        }, 3000, 3000)
    }

    private fun stopAutoScroll() {
        mTimer?.cancel()
    }


    /**
     * 轮播图
     *
     * @property bannerList
     * @property itemClick
     * @constructor Create empty Game banner adapter
     */
    class GameBannerAdapter constructor(
        var bannerList: List<PlayZoneGameBanner>,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.Adapter<GameBannerAdapter.GameViewHolder>() {

        inner class GameViewHolder(val binding: PlayZoneItemGameBannerLayoutBinding) :
            RecyclerView.ViewHolder(binding.root)

        fun setDataList(list: List<PlayZoneGameBanner>) {
            bannerList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
            return GameViewHolder(
                PlayZoneItemGameBannerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int = bannerList.size

        override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
            val data = bannerList[position]
            GlideApp.with(holder.binding.ivGuide)
                .load(data.url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.binding.ivGuide)
            holder.binding.root.setOnClickListener {
                itemClick.invoke(position)
            }
        }
    }

    /**
     * 具体游戏 viewHolder
     *
     * @constructor
     *
     * @param mBinding
     */
    class GameInfoeHolder constructor(mBinding: PlayZoneItemGameLayoutBinding) :
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