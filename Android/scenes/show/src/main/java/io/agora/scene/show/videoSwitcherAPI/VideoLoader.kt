package io.agora.scene.show.videoSwitcherAPI

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import io.agora.rtc2.*
import io.agora.scene.base.manager.UserManager
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.VideoSwitcher
import io.agora.scene.show.VideoSwitcherImpl

open class VideoLoader constructor(
    private val rtcEngine: RtcEngineEx,
    private val channelId: String
): ViewPager2.OnPageChangeCallback() {

    private var innerVideoSwitcher: VideoSwitcher? = null
    private val videoSwitcher: VideoSwitcher
        get() {
            if (innerVideoSwitcher == null) {
                innerVideoSwitcher = VideoSwitcherImpl(
                    rtcEngine, VideoSwitcherAPIImpl(
                        rtcEngine
                    ))
            }
            return innerVideoSwitcher!!
        }

    val onRoomItemTouchListener: View.OnTouchListener
        @SuppressLint("ClickableViewAccessibility")
        get() {
            return View.OnTouchListener { v, event ->
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val rtcConnection =
                            RtcConnection(channelId, UserManager.getInstance().user.id.toInt())
                        val channelMediaOptions = ChannelMediaOptions()
                        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        channelMediaOptions.autoSubscribeVideo = false
                        channelMediaOptions.autoSubscribeAudio = false
                        channelMediaOptions.publishCameraTrack = false
                        channelMediaOptions.publishMicrophoneTrack = false
                        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                        channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        videoSwitcher.preJoinChannel(
                            rtcConnection,
                            channelMediaOptions,
                            null
                        )
                    }
                    MotionEvent.ACTION_MOVE -> {}
                    MotionEvent.ACTION_UP -> {
                        val rtcConnection =
                            RtcConnection(channelId, UserManager.getInstance().user.id.toInt())
                        val channelMediaOptions = ChannelMediaOptions()
                        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        channelMediaOptions.autoSubscribeVideo = true
                        channelMediaOptions.autoSubscribeAudio = true
                        channelMediaOptions.publishCameraTrack = false
                        channelMediaOptions.publishMicrophoneTrack = false
                        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                        channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        ShowLogger.d("hugo", "click up join channel: $rtcConnection")
                        videoSwitcher.joinChannel(
                            rtcConnection,
                            channelMediaOptions,
                            null
                        )
                    }
                }
                true
            }
        }

    val onViewPagerItemScrollListener: ViewPager.OnPageChangeListener
        get() {
            return object : ViewPager.OnPageChangeListener{
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    TODO("Not yet implemented")
                }

                override fun onPageSelected(position: Int) {
                    TODO("Not yet implemented")
                }

                override fun onPageScrollStateChanged(state: Int) {
                    TODO("Not yet implemented")
                }

            }
        }

    val onViewPager2ItemScrollListener: ViewPager2.OnPageChangeCallback
        get() {
            return object: ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            }
        }
}