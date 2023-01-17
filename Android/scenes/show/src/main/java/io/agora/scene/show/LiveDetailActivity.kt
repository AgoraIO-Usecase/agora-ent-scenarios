package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.keyIterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.widget.utils.StatusBarUtil


class LiveDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ROOM_DETAIL_INFO_LIST = "roomDetailInfoList"
        private const val EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX = "roomDetailInfoListSelectedIndex"
        private const val EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE = "roomDetailInfoListScrollable"


        fun launch(context: Context, roomDetail: ShowRoomDetailModel) {
            launch(context, arrayListOf(roomDetail), 0, false)
        }

        fun launch(context: Context, roomDetail: ArrayList<ShowRoomDetailModel>, selectedIndex: Int, scrollable: Boolean) {
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST, roomDetail)
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, selectedIndex)
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE, scrollable)
            })
        }
    }

    internal lateinit var mPermissionHelp: PermissionHelp

    private val mRoomInfoList by lazy { intent.getParcelableArrayListExtra<ShowRoomDetailModel>(EXTRA_ROOM_DETAIL_INFO_LIST)!! }
    private val mScrollable by lazy { intent.getBooleanExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE, true) }
    private val mBinding by lazy { ShowLiveDetailActivityBinding.inflate(LayoutInflater.from(this)) }

    private var mSelectedRoomIndex = 0
    private val mRtcConnectionList = SparseArray<RtcConnection>()
    private val mRtcEngine = RtcEngineInstance.rtcEngine

    private var mCurrResumedFragment: LiveDetailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPermissionHelp = PermissionHelp(this)
        mSelectedRoomIndex = intent.getIntExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, 0)

        // 设置token有效期为房间存活时长，到期后关闭并退出房间
        TokenGenerator.expireSecond =
            LiveDetailFragment.ROOM_AVAILABLE_DURATION / 1000 + 10 // 20min + 10s，加10s防止临界条件下报token无效

        mBinding.viewPager2.offscreenPageLimit = 2
        val fragmentAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = if (mScrollable) Int.MAX_VALUE else 1

            override fun createFragment(position: Int) : Fragment{
                val roomInfo = if(mScrollable){
                    mRoomInfoList[position % mRoomInfoList.size]
                }else{
                    mRoomInfoList[mSelectedRoomIndex]
                }
                return LiveDetailFragment.newInstance(roomInfo)
            }
        }
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentResumed(fm, f)
                    if(f is LiveDetailFragment){
                        mCurrResumedFragment = f
                        mRtcConnectionList.get(mRoomInfoList.indexOfFirst { it.roomId == f.mRoomInfo.roomId })?.let { connection ->
                            f.onRtcConnectionConnected(connection)
                        }
                    }
                }
            },
            false
        )
        mBinding.viewPager2.adapter = fragmentAdapter
        mBinding.viewPager2.isUserInputEnabled = mScrollable
        if (mScrollable) {
            mBinding.viewPager2.setCurrentItem(Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % mRoomInfoList.size + mSelectedRoomIndex, false)
            mBinding.viewPager2.registerOnPageChangeCallback(object: OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mSelectedRoomIndex = position % mRoomInfoList.size
                    ShowLogger.d("", "Fragment Lifecycle: onPageSelected")
                    preJoinChannel()
                }
            })
        }
        preJoinChannel()
    }

    private fun preJoinChannel() {
        val roomsForJoining = mutableListOf<RtcConnection>()
        val roomsForLeaving = mutableListOf<RtcConnection>()

        mRoomInfoList.getOrNull(mSelectedRoomIndex)?.let { roomsForJoining.add(RtcConnection(it.roomId, UserManager.getInstance().user.id.toInt())) }
        mRoomInfoList.getOrNull(mSelectedRoomIndex - 1)?.let { roomsForJoining.add(RtcConnection(it.roomId, UserManager.getInstance().user.id.toInt())) }
        mRoomInfoList.getOrNull(mSelectedRoomIndex + 1)?.let { roomsForJoining.add(RtcConnection(it.roomId, UserManager.getInstance().user.id.toInt())) }


        val keyIterator = mRtcConnectionList.keyIterator()
        while (keyIterator.hasNext()) {
            val index = keyIterator.nextInt()
            if ((index < mSelectedRoomIndex - 1) or (index > mSelectedRoomIndex + 1)) {
                roomsForLeaving.add(mRtcConnectionList[index])
            } else {
                roomsForJoining.firstOrNull { it.channelId == mRtcConnectionList[index].channelId }
                    ?.let {
                        roomsForJoining.remove(it)
                    }
            }
        }

        roomsForLeaving.forEach {
            mRtcEngine.leaveChannelEx(it)
            mRtcConnectionList.remove(mRtcConnectionList.indexOfValue(it))
        }

        roomsForJoining.forEach { connection ->
            TokenGenerator.generateToken(connection.channelId,
                connection.localUid.toString(),
                TokenGenerator.TokenGeneratorType.token006,
                TokenGenerator.AgoraTokenType.rtc,
                success = { token ->
                    mRtcEngine.joinChannelEx(
                        token,
                        connection,
                        ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeAudio = false
                            autoSubscribeVideo = false
                        },
                        object : IRtcEngineEventHandler() {})
                    val indexOfFirst =
                        mRoomInfoList.indexOfFirst { it.roomId == connection.channelId }
                    mRtcConnectionList.put(indexOfFirst, connection)
                    mCurrResumedFragment?.let { fragment ->
                        if (fragment.mRoomInfo.roomId == connection.channelId) {
                            fragment.onRtcConnectionConnected(connection)
                        }
                    }
                },
                failure = {
                    ToastUtils.showToast(it.toString())
                })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        TokenGenerator.expireSecond = -1
        for (i in 0 until mRtcConnectionList.size()){
            mRtcEngine.leaveChannelEx(mRtcConnectionList.valueAt(i))
        }
        mRtcConnectionList.clear()
        RtcEngineInstance.destroy()
    }
}