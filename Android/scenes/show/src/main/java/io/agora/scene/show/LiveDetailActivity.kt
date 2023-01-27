package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.service.ROOM_AVAILABLE_DURATION
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.widget.utils.StatusBarUtil


class LiveDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ROOM_DETAIL_INFO_LIST = "roomDetailInfoList"
        private const val EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX =
            "roomDetailInfoListSelectedIndex"
        private const val EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE = "roomDetailInfoListScrollable"


        fun launch(context: Context, roomDetail: ShowRoomDetailModel) {
            launch(context, arrayListOf(roomDetail), 0, false)
        }

        fun launch(
            context: Context,
            roomDetail: ArrayList<ShowRoomDetailModel>,
            selectedIndex: Int,
            scrollable: Boolean
        ) {
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST, roomDetail)
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, selectedIndex)
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE, scrollable)
            })
        }
    }

    internal lateinit var mPermissionHelp: PermissionHelp

    private val mRoomInfoList by lazy {
        intent.getParcelableArrayListExtra<ShowRoomDetailModel>(
            EXTRA_ROOM_DETAIL_INFO_LIST
        )!!
    }
    private val mScrollable by lazy {
        intent.getBooleanExtra(
            EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE,
            true
        )
    }
    private val mBinding by lazy { ShowLiveDetailActivityBinding.inflate(LayoutInflater.from(this)) }

    private val mVideoSwitcher by lazy { RtcEngineInstance.videoSwitcher }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPermissionHelp = PermissionHelp(this)
        val selectedRoomIndex = intent.getIntExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, 0)

        // 设置token有效期为房间存活时长，到期后关闭并退出房间
        TokenGenerator.expireSecond =
            ROOM_AVAILABLE_DURATION / 1000 + 10 // 20min + 10s，加10s防止临界条件下报token无效

        // 设置预加载
        val preloadCount = 3
        mVideoSwitcher.setPreloadCount(preloadCount)
        mVideoSwitcher.preloadConnections(mRoomInfoList.map {
            RtcConnection(
                it.roomId,
                UserManager.getInstance().user.id.toInt()
            )
        })

        // 设置vp当前页面外的页面数
        mBinding.viewPager2.offscreenPageLimit = preloadCount - 1
        val fragmentAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = if (mScrollable) Int.MAX_VALUE else 1

            override fun createFragment(position: Int): Fragment {
                val roomInfo = if (mScrollable) {
                    mRoomInfoList[position % mRoomInfoList.size]
                } else {
                    mRoomInfoList[selectedRoomIndex]
                }
                return LiveDetailFragment.newInstance(roomInfo)
            }
        }
        mBinding.viewPager2.adapter = fragmentAdapter
        mBinding.viewPager2.isUserInputEnabled = mScrollable
        if (mScrollable) {
            mBinding.viewPager2.setCurrentItem(
                Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % mRoomInfoList.size + selectedRoomIndex,
                false
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        TokenGenerator.expireSecond = -1
        RtcEngineInstance.destroy()
    }
}