package io.agora.scene.show.beauty

import android.util.Log
import io.agora.base.VideoFrame
import io.agora.rtc2.video.IVideoFrameObserver
import java.util.concurrent.Executors


abstract class IBeautyProcessor: IVideoFrameObserver {
    private val workerExecutor = Executors.newSingleThreadExecutor()

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

    open fun release(){
        if(workerExecutor.isShutdown){
            workerExecutor.shutdownNow()
        }
    }

    fun reset(){
        BeautyCache.reset()
        BeautyCache.restoreByOperation(this)
    }

    fun setEnable(enable: Boolean){
        isEnable = enable
    }

    fun isEnable() = isEnable

    // 设置绿幕强度（0 ～ 1）
    fun setBg(intensity: Float) {
        Log.e("liu0208", "setBg    intensity = $intensity")
        // 保存绿幕强度值
        BeautyCache.cacheItemValue(GROUP_ID_VIRTUAL_BG, ITEM_ID_VIRTUAL_BG_GREEN_SCREENSTRENGTH, intensity)
    }

    // 获取绿幕强度（0 ～ 1）
    fun getGreenScreenStrength(): Float {
        return BeautyCache.getItemValue(ITEM_ID_VIRTUAL_BG_GREEN_SCREENSTRENGTH, 0.5f)
    }

    // 设置绿幕开关
    fun setGreenScreen(greenScreen: Boolean) {
        BeautyCache.cacheItemValue(GROUP_ID_VIRTUAL_BG, ITEM_ID_VIRTUAL_BG_GREEN_SCREEN, if (greenScreen) 1f else 0f)
    }

    // 获取绿幕开关
    fun greenScreen(): Boolean {
        return BeautyCache.getItemValue(ITEM_ID_VIRTUAL_BG_GREEN_SCREEN) == 1f
    }

    fun setFaceBeautify(itemId: Int, intensity: Float){
        BeautyCache.cacheItemValue(GROUP_ID_BEAUTY, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_BEAUTY, itemId)
        workerExecutor.execute {
            setFaceBeautifyAfterCached(itemId, intensity)
        }
    }

    fun setFilter(itemId: Int, intensity: Float){
        BeautyCache.cacheItemValue(GROUP_ID_FILTER, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_FILTER, itemId)
        workerExecutor.execute {
            setFilterAfterCached(itemId, intensity)
        }
    }

    fun setEffect(itemId: Int, intensity: Float){
        BeautyCache.resetGroupValue(GROUP_ID_EFFECT)
        BeautyCache.cacheItemValue(GROUP_ID_EFFECT, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_EFFECT, itemId)
        workerExecutor.execute{
            setEffectAfterCached(itemId, intensity)
        }
    }

    fun setSticker(itemId: Int){
        BeautyCache.cacheOperation(GROUP_ID_STICKER, itemId)
        workerExecutor.execute{
            setStickerAfterCached(itemId)
        }
    }

    fun setAdjust(itemId: Int, intensity: Float){
        BeautyCache.cacheItemValue(GROUP_ID_ADJUST, itemId, intensity)
        BeautyCache.cacheOperation(GROUP_ID_ADJUST, itemId)
        workerExecutor.execute {
            setFaceBeautifyAfterCached(itemId, intensity)
        }
    }


    // IVideoFrameObserver implement
    override fun onPreEncodeVideoFrame(type: Int, videoFrame: VideoFrame?): Boolean = false

    override fun onCaptureVideoFrame(type: Int, videoFrame: VideoFrame?): Boolean = false

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