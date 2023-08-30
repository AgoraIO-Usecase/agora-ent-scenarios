package io.agora.scene.showTo1v1.ui

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import io.agora.scene.base.GlideApp
import io.agora.scene.base.GlideOptions
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.RtcEngineInstance
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.ui.fragment.RoomListFragment
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.BlurTransformation
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>() {

    companion object {
        private const val TAG = "RoomListActivity"
        private const val kRoomListSwipeGuide = "showTo1v1_SwipeGuide"

        private const val POSITION_NONE = -1
    }

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }

    private var fragmentAdapter: FragmentStateAdapter? = null
    private val mRoomInfoList = mutableListOf<ShowTo1v1RoomInfo>()

    private val vpFragments = SparseArray<RoomListFragment>()
    private var currLoadPosition = POSITION_NONE

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomListActivityBinding {
        return ShowTo1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        fetchService()
    }

    private fun setOnApplyWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(0, 0, 0, inset.bottom)
            binding.titleView.setPaddingRelative(0, inset.top, 0, 0)
            binding.emptyInclude.root.setPaddingRelative(0, inset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    private var guided = SPUtil.getBoolean(kRoomListSwipeGuide, false)
    private fun mayShowGuideView() {
        if (mRoomInfoList.isEmpty() || guided) {
            return
        }
        binding.vGuidance.visibility = View.VISIBLE
        binding.vGuidance.setOnClickListener {
            SPUtil.putBoolean(kRoomListSwipeGuide, true)
            guided = true
            binding.vGuidance.visibility = View.GONE
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleView.setLeftClick { finish() }
        binding.titleView.setRightIconClick {
            fetchRoomList()
        }
        binding.emptyInclude.layoutCreateRoom.setOnClickListener {
            RoomCreateActivity.launch(this)
        }
    }

    private fun initViewPage() {
        if (fragmentAdapter == null) {
            // 设置预加载
            val preloadCount = 3
            binding.viewPager2.offscreenPageLimit = preloadCount - 2
            fragmentAdapter = object : FragmentStateAdapter(this) {
                override fun getItemCount() = Int.MAX_VALUE

                override fun createFragment(position: Int): Fragment {
                    val roomInfo = mRoomInfoList[position % mRoomInfoList.size]

                    return RoomListFragment.newInstance(roomInfo).also {
                        vpFragments.put(position, it)
                        if (position == binding.viewPager2.currentItem) {
                            it.startLoadPageSafely()
                        }
                    }
                }
            }
            binding.viewPager2.adapter = fragmentAdapter
            binding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
            binding.viewPager2.setCurrentItem(Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % mRoomInfoList.size, false)
        }
    }

    private val onPageChangeCallback = object : OnPageChangeCallback() {

        private val PRE_LOAD_OFFSET = 0.01f
        private var preLoadPosition = POSITION_NONE
        private var lastOffset = 0f
        private var scrollStatus: Int = ViewPager2.SCROLL_STATE_IDLE

        private var hasPageSelected = false

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            Log.d(TAG, "PageChange onPageScrollStateChanged state=$state hasPageSelected=$hasPageSelected")
            when (state) {
                ViewPager2.SCROLL_STATE_SETTLING -> binding.viewPager2.isUserInputEnabled = false
                ViewPager2.SCROLL_STATE_IDLE -> {
                    binding.viewPager2.isUserInputEnabled = true
                    if (!hasPageSelected) {
                        if (preLoadPosition != POSITION_NONE) {
                            vpFragments[preLoadPosition]?.stopLoadPage(true)
                        }
                        preLoadPosition = POSITION_NONE
                        lastOffset = 0f
                    }
                    hasPageSelected = false
                }

                else -> {
                    // nothing
                }
            }
            scrollStatus = state
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            Log.d(TAG, "PageChange onPageScrolled positionOffset=$positionOffset")
            if (scrollStatus == ViewPager2.SCROLL_STATE_DRAGGING) {
                if (lastOffset > 0f) {
                    val isMoveUp = (positionOffset - lastOffset) > 0
                    if (isMoveUp && positionOffset >= PRE_LOAD_OFFSET && preLoadPosition == POSITION_NONE) {
                        preLoadPosition = currLoadPosition + 1
                        vpFragments[preLoadPosition]?.startLoadPageSafely()
                    } else if (!isMoveUp && positionOffset <= (1 - PRE_LOAD_OFFSET) && preLoadPosition == POSITION_NONE) {
                        preLoadPosition = currLoadPosition - 1
                        vpFragments[preLoadPosition]?.startLoadPageSafely()
                    }
                }
                lastOffset = positionOffset
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(
                TAG,
                "PageChange onPageSelected position=$position, currLoadPosition=$currLoadPosition, preLoadPosition=$preLoadPosition"
            )
            if (currLoadPosition != POSITION_NONE) {
                if (preLoadPosition != POSITION_NONE) {
                    if (position == preLoadPosition) {
                        vpFragments[currLoadPosition]?.stopLoadPage(true)
                    } else {
                        vpFragments[preLoadPosition]?.stopLoadPage(true)
                    }
                } else if (currLoadPosition != position) {
                    vpFragments[currLoadPosition]?.stopLoadPage(true)
                    vpFragments[position]?.startLoadPageSafely()
                }
            }
            currLoadPosition = position
            preLoadPosition = POSITION_NONE
            lastOffset = 0f
            hasPageSelected = true
        }
    }

    private fun fetchService() {
        //获取token
        val localUId = UserManager.getInstance().user.id.toInt()
        TokenGenerator.generateToken("", localUId.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            TokenGenerator.AgoraTokenType.rtc,
            success = {
                RtcEngineInstance.setupGeneralToken(it)
                ShowTo1v1Logger.d(TAG, "generateToken success：$it， uid：$localUId")
                fetchRoomList()
            },
            failure = {
                ShowTo1v1Logger.e(TAG, it, "generateToken failure：$it")
                ToastUtils.showToast(it?.message ?: "generate token failure")
            })
    }

    private fun fetchRoomList() {
        mService.getRoomList(completion = { error, roomList ->
            if (error == null) { // success
                mRoomInfoList.clear()
                mRoomInfoList.addAll(roomList)
                vpFragments[currLoadPosition]?.stopLoadPage(false)
                updateListView()
                initViewPage()
                ToastUtils.showToast(R.string.show_to1v1_room_list_refreshed)
            } else {
                updateListView()
            }
            mayShowGuideView()

        })
    }

    private fun updateListView() {
        if (mRoomInfoList.isEmpty()) {
            StatusBarUtil.hideStatusBar(window, true)
            binding.emptyInclude.root.isVisible = true
            binding.viewPager2.isVisible = false
        } else {
            StatusBarUtil.hideStatusBar(window, false)
            binding.emptyInclude.root.isVisible = false
            binding.viewPager2.isVisible = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpFragments[currLoadPosition]?.stopLoadPage(false)
        RtcEngineInstance.cleanCache()
    }
}