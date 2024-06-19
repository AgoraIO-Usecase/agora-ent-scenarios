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
import io.agora.scene.playzone.service.api.PlayZoneGameBanner
import io.agora.scene.playzone.sub.api.SubGameInfoModel
import io.agora.scene.playzone.sub.api.SubGameType
import io.agora.scene.playzone.sub.quickstart.QuickStartActivity
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
            ToastUtils.showToast("房间列表")
            startActivity(Intent(this, QuickStartActivity::class.java))
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
        mPlayZoneViewModel.createRoomInfoLiveData.observe(this) { roomInfo->
            if (roomInfo == null) {
                setDarkStatusIcon(isBlackDarkStatus)
            } else {
               ToastUtils.showToast("创建房间成功")
            }
        }

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

        mPlayZoneViewModel.subGameList(mCurrentVendor)

        mPlayZoneViewModel.mGameListLiveData.observe(this) {

            val listAdapter = mutableListOf<RecyclerView.Adapter<*>>()
            val gameTypeAdapter = GameTypeAdapter(mutableListOf(""), GameTypeHolder::class.java)
            listAdapter.add(gameTypeAdapter)

            var headTitle = ""
            it.forEach { subGameListModel ->
                when (subGameListModel.gameType) {
                    SubGameType.leisure_and_entertainment -> {
                        headTitle = getString(R.string.play_zone_leisure_and_entertainment)
                    }

                    SubGameType.voice_interaction -> {
                        headTitle = getString(R.string.play_zone_voice_interaction)
                    }

                    SubGameType.realtime_competition -> {
                        headTitle = getString(R.string.play_zone_realtime_competition)
                    }

                    SubGameType.classic_board_games -> {
                        headTitle = getString(R.string.play_zone_classic_board_games)
                    }

                    SubGameType.party_games -> {
                        headTitle = getString(R.string.play_zone_party_games)
                    }
                }
                val headAdapter = GameHeadAdapter(mutableListOf(headTitle), GameHeadHolder::class.java)
                listAdapter.add(headAdapter)
                val gameAdapter = BaseRecyclerViewAdapter(subGameListModel.gameList, object :
                    OnItemClickListener<SubGameInfoModel?> {
                    override fun onItemClick(subGameInfo: SubGameInfoModel, view: View, position: Int, viewType: Long) {
                        ToastUtils.showToast("点击了${subGameInfo.gameName}")
                        if (!subGameInfo.gameUrl.isNullOrEmpty()) {
                            PagePilotManager.pageWebViewWithBrowser(subGameInfo.gameUrl)
                        }else{
                            showCreateRoomDialog(subGameInfo)
                        }
                    }
                }, GameSubHolder::class.java)
                listAdapter.add(gameAdapter)
            }
            concatAdapter = ConcatAdapter(listAdapter)
            binding.rvLeisureGame.adapter = concatAdapter
        }
    }

    private fun showCreateRoomDialog(subGameInfo: SubGameInfoModel) {
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
                mPlayZoneViewModel.subGameList(mCurrentVendor)
                ToastUtils.showToast("切换到${it}")
            }
        }
        dialog.show(supportFragmentManager, "VendorDialog")
    }

    private var mCurrentPos = 1
    private var mTimer: Timer? = null

    private val mGameInfoAdapter by lazy {
        GameInfoAdapter(emptyList(), itemClick = {
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


    class GameInfoAdapter constructor(
        var gameInfoList: List<PlayZoneGameBanner>,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.Adapter<GameInfoAdapter.GameViewHolder>() {

        inner class GameViewHolder(val binding: PlayZoneItemGameBannerLayoutBinding) :
            RecyclerView.ViewHolder(binding.root)

        fun setDataList(list: List<PlayZoneGameBanner>) {
            gameInfoList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
            return GameViewHolder(
                PlayZoneItemGameBannerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int {
            return gameInfoList.size
        }

        override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
            val data = gameInfoList[position]
            GlideApp.with(holder.binding.ivGuide)
                .load(data.url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.binding.ivGuide)
            holder.binding.root.setOnClickListener {
                itemClick.invoke(position)
            }
        }
    }

    class GameSubHolder constructor(mBinding: PlayZoneItemGameLayoutBinding) :
        BaseRecyclerViewAdapter.BaseViewHolder<PlayZoneItemGameLayoutBinding, SubGameInfoModel?>(mBinding) {

        override fun binding(subGame: SubGameInfoModel?, selectedIndex: Int) {
            subGame ?: return
            mBinding.tvGameName.text = subGame.gameName
            mBinding.ivGameIcon.setImageResource(subGame.gamePic)
        }
    }

    // 休闲玩法
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

    // 游戏分类标题
    class GameHeadAdapter<B : ViewBinding, T, H : BaseRecyclerViewAdapter.BaseViewHolder<B, T>>(
        dataList: List<T>, viewHolderClass: Class<H>
    ) : BaseRecyclerViewAdapter<B, T, H>(dataList, viewHolderClass) {
        override fun onViewAttachedToWindow(holder: H) {
            super.onViewAttachedToWindow(holder)
            (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }
    }

    class GameHeadHolder constructor(mBinding: PlayZoneItemGameHeaderLayoutBinding) :
        BaseRecyclerViewAdapter.BaseViewHolder<PlayZoneItemGameHeaderLayoutBinding, String>(mBinding) {
        override fun binding(data: String?, selectedIndex: Int) {
            data ?: return
            mBinding.tvGameTypeTitle.text = data
        }
    }
}