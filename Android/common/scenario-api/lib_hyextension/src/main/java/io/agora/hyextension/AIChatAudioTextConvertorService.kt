package io.agora.hyextension

import android.util.Log
import io.agora.hy.extension.ExtensionManager
import io.agora.rtc2.Constants.LOG_LEVEL_ERROR
import io.agora.rtc2.Constants.LOG_LEVEL_INFO
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.scene.base.component.AgoraApplication
import org.json.JSONObject

// 定义音频到文本转换的协议
interface AIChatAudioTextConvertorDelegate {
    /**
     * 当音频转换为文本时会回调此方法。
     *
     * @param result 转换后的文本结果。
     * @param error 如果转换过程中发生错误，则包含错误信息；否则为 `nil`。
     */
    fun convertResultHandler(result: String?, error: Exception?)

    /**
     * 当设置了音量指示器-setAudioVolumeIndications时，会回调此方法来同步音频音量。
     *
     * @param totalVolume
     */
    fun convertAudioVolumeHandler(totalVolume: Int)
}

interface AIChatAudioTextConvertEvent {
    /**
     * 开始音频获取。
     *
     * 调用此方法以开始捕获音频数据并进行转换处理。
     */
    fun startConvertor()

    /**
     * 结束音频获取。
     *
     * 调用此方法后，会先处理完当前捕获的音频数据并生成转换结果，然后进入空闲状态。
     * 适用于正常结束音频捕获的场景。
     */
    fun flushConvertor()

    /**
     * 停止音频获取。
     *
     * 调用此方法后，会丢弃当前捕获的音频数据和转换结果，然后进入空闲状态。
     * 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
     */
    fun stopConvertor()
}

interface AIChatAudioTextConvertor {
    /**
     * 启动音频到文本转换服务。
     *
     * @param  服务商分配的appId。
     * @param  服务商分配的apiKey。
     * @param apiSecret 服务商分配的apiSecret。
     * @param convertType 指定的语言转换类型。
     * @param agoraRtcKit 用于音频处理的 Agora RTC 引擎实例。
     *
     * 调用此方法以启动音频到文本转换服务，并配置必要的参数。
     */
    fun run(appId: String, apiKey: String, apiSecret: String, convertType: LanguageConvertType, agoraRtcKit: RtcEngine)

    /**
     * 设置启音量指示器回调，以报告哪些用户在讲话以及讲话者的音量。
     *
     * @param interval 设置两个连续音量指示之间的时间间隔,两个连续音量指示之间的时间间隔（毫秒），应为 200 的整数倍（小于 200 将被设置为 200)
     * @param smooth 设置音量指示器灵敏度的平滑因子。取值范围为 [0, 10]。值越大，指示器越灵敏。推荐值为 3。
     *
     * 调用此方法以启动音量指示器回调
     */
    fun setAudioVolumeIndication(interval: Int, smooth: Int)

    /**
     * 添加一个委托以接收转换结果。
     *
     * @param delegate 实现 [AIChatAudioTextConvertorDelegate] 协议的对象。
     *
     * 调用此方法以添加一个委托，该委托将接收音频到文本转换的结果。
     */
    fun addDelegate(delegate: AIChatAudioTextConvertorDelegate)

    /**
     * 移除一个委托。
     *
     * @param delegate 实现 [AIChatAudioTextConvertorDelegate] 协议的对象。
     *
     * 调用此方法以移除先前添加的委托。
     */
    fun removeDelegate(delegate: AIChatAudioTextConvertorDelegate)

    /**
     * 移除所有委托。
     *
     * 调用此方法以移除所有先前添加的委托。
     */
    fun removeAllDelegates()
}

/**
 * 枚举定义了音频转换过程中支持的语言模式。
 *
 * 通过选择不同的模式，可以控制音频转换器识别的语言类型。
 */
enum class LanguageConvertType {
    /**
     * 中英文模式：中文和英文均可识别。
     *
     * 适用于需要同时识别中文和英文的场景。
     */
    NORMAL,

    /**
     * 英文模式：只能识别出英文。
     *
     * 适用于只需要识别英文的场景。
     */
    EN
}

class AIChatAudioTextConvertorService : AIChatAudioTextConvertor, AIChatAudioTextConvertEvent,
    IRtcEngineEventHandler() {

    companion object {
        const val tag = "HY_API_LOG"
        const val version = "1.0.0"
    }

    private val observableHelper = ObservableHelper<AIChatAudioTextConvertorDelegate>()


    private var mRtcEngine: RtcEngine? = null
    private var convertType: LanguageConvertType = LanguageConvertType.NORMAL

    private var mHyUtil: HyUtil? = null


    fun onEvent(key: String?, value: String?) {
        mHyUtil?.onEvent(key, value)
    }

    override fun run(
        appId: String, apiKey: String, apiSecret: String, convertType: LanguageConvertType, agoraRtcKit: RtcEngine
    ) {
        mHyUtil = HyUtil(appId, apiKey, apiSecret, mHyUtilListener, agoraRtcKit)
        this.convertType = convertType
        this.mRtcEngine = agoraRtcKit
        // 设置日志配置。最多设置1次，若不设置则不打日志。
        agoraRtcKit.setExtensionProviderProperty(
            ExtensionManager.EXTENSION_VENDOR_NAME,
            "log_cfg", JSONObject() // 目录路径。必选。值类型：String。
                .put("dir", AgoraApplication.the().getExternalFilesDir(null)!!.getCanonicalPath() + "/log")
                // 等级
                // 值类型：int
                // 值范围：
                // LOG_LVL_UNKNOWN：0；
                // LOG_LVL_DEFAULT：1；
                // LOG_LVL_VERBOSE：2；
                // LOG_LVL_DEBUG：3；
                // LOG_LVL_INFO：4；
                // LOG_LVL_WARN：5；
                // LOG_LVL_ERROR：6；
                // LOG_LVL_FATAL：7；
                // LOG_LVL_SILENT：8。
                // 值默认：LOG_LVL_WARN
                .put("lvl", 0)
                .toString()
        )

    }

    override fun setAudioVolumeIndication(interval: Int, smooth: Int) {
        mRtcEngine?.enableAudioVolumeIndication(interval, smooth, true)
    }

    override fun addDelegate(delegate: AIChatAudioTextConvertorDelegate) {
        observableHelper.subscribeEvent(delegate)
    }

    override fun removeDelegate(delegate: AIChatAudioTextConvertorDelegate) {
        observableHelper.unSubscribeEvent(delegate)
    }

    override fun removeAllDelegates() {
        observableHelper.unSubscribeAll()
    }

    override fun startConvertor() {
        val hyUtil = mHyUtil ?: return
        val paramWrap: HyUtil.ParamWrap =
            if (convertType == LanguageConvertType.EN) hyUtil.paramWraps[1] else hyUtil.paramWraps[0]
        hyUtil.start(paramWrap)
    }

    override fun flushConvertor() {
        val hyUtil = mHyUtil ?: return
        hyUtil.flush()
    }

    override fun stopConvertor() {
        val hyUtil = mHyUtil ?: return
        hyUtil.stop()
    }

    fun writeLog(content: String, level: Int) {
        Log.d("zhangw", content)
        mRtcEngine?.writeLog(level, content)
    }

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        observableHelper.notifyEventHandlers { it.convertAudioVolumeHandler(totalVolume) }
    }


    private val mHyUtilListener: HyUtil.IListener = object : HyUtil.IListener {
        override fun onLogI(tip: String?) {
            writeLog("[$tag][${convertType.name}] $tip", LOG_LEVEL_INFO)
        }

        override fun onLogE(tip: String?) {
            writeLog("[$tag][${convertType.name}] $tip", LOG_LEVEL_ERROR)
        }

        override fun onLogE(tip: String?, tr: Throwable?) {
            writeLog("[$tag][${convertType.name}] $tip", LOG_LEVEL_ERROR)
        }

        override fun onIstText(text: String?, exception: Exception?) {
            observableHelper.notifyEventHandlers { it.convertResultHandler(text, exception) }
        }

        override fun onItsText(text: String?) {
            // nothing
        }
    }
}