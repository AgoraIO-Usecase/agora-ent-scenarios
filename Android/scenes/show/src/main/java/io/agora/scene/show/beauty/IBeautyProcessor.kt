package io.agora.scene.show.beauty

import io.agora.base.VideoFrame
import io.agora.rtc2.video.IVideoFrameObserver


abstract class IBeautyProcessor: IVideoFrameObserver {

    @Volatile
    private var isEnable = true

    protected abstract fun setFaceBeautifyAfterCached(itemId: Int, intensity: Float)

    protected abstract fun setFilterAfterCached(itemId: Int, intensity: Float)

    protected abstract fun setEffectAfterCached(itemId: Int, intensity: Float)

    protected abstract fun setStickerAfterCached(itemId: Int)

    protected fun restore(){
        BeautyCache.restoreByOperation(this)
    }


    // Publish Functions

    abstract fun release()

    fun setEnable(enable: Boolean){
        isEnable = enable
    }

    fun isEnable() = isEnable

    fun setFaceBeautify(itemId: Int, intensity: Float){
        BeautyCache.cacheItemValue(GROUP_ID_BEAUTY, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_BEAUTY, itemId)
        setFaceBeautifyAfterCached(itemId, intensity)
    }

    fun setFilter(itemId: Int, intensity: Float){
        BeautyCache.cacheItemValue(GROUP_ID_FILTER, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_FILTER, itemId)
        setFilterAfterCached(itemId, intensity)
    }

    fun setEffect(itemId: Int, intensity: Float){
        BeautyCache.cacheItemValue(GROUP_ID_EFFECT, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_EFFECT, itemId)
        setEffectAfterCached(itemId, intensity)
    }

    fun setSticker(itemId: Int){
        BeautyCache.cacheOperation(GROUP_ID_STICKER, itemId)
        setStickerAfterCached(itemId)
    }


    // IVideoFrameObserver implement

    override fun onPreEncodeVideoFrame(videoFrame: VideoFrame?) = false

    override fun onScreenCaptureVideoFrame(videoFrame: VideoFrame?) = false

    override fun onPreEncodeScreenVideoFrame(videoFrame: VideoFrame?) = false

    override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame?, mediaPlayerId: Int) = false

    override fun onRenderVideoFrame(
        channelId: String?,
        uid: Int,
        videoFrame: VideoFrame?
    ) = false

    override fun getVideoFrameProcessMode() = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

    override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

    override fun getRotationApplied() = false

    override fun getMirrorApplied() =  false

    override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

}