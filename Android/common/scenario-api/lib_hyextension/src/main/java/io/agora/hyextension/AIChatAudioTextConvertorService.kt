package io.agora.hyextension

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import io.agora.hy.extension.ExtensionManager
//import io.agora.mediaplayer.Constants.MediaPlayerError
import io.agora.rtc2.Constants.LOG_LEVEL_ERROR
import io.agora.rtc2.Constants.LOG_LEVEL_INFO
import io.agora.rtc2.RtcEngineEx
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

    fun onTimeoutHandler()

    fun onLogHandler(log: String, isError: Boolean)
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
     *
     * 调用此方法以启动音频到文本转换服务，并配置必要的参数。
     */
    fun startService(
        appId: String,
        apiKey: String,
        apiSecret: String,
        convertType: LanguageConvertType,
    )

    fun stopService()

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

enum class ConvertorStatusType {
    Idle,
    Start,
    Flush
}

class AIChatAudioTextConvertorService constructor(private val rtcEngine: RtcEngineEx) : AIChatAudioTextConvertor,
    AIChatAudioTextConvertEvent {

    companion object {
        const val tag = "HY_API_LOG"
        const val version = "1.0.0"
        const val maxDuration = 60 * 1000 // 最长 60ms
    }

    private val observableHelper = ObservableHelper<AIChatAudioTextConvertorDelegate>()

    private var convertType: LanguageConvertType = LanguageConvertType.NORMAL
    private var convertorStatus: ConvertorStatusType = ConvertorStatusType.Idle

    private var mHyUtil: HyUtil? = null

    private val mMainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var startTime = 0L
    private fun startTimer() {
        startTime = 0L
        mMainHandler.removeCallbacks(timeoutDownTask)
        mMainHandler.postDelayed(timeoutDownTask, 1000)
    }

    private val timeoutDownTask = object : Runnable {
        override fun run() {
            if (startTime >= maxDuration) {
                mMainHandler.removeCallbacks(this)
                observableHelper.notifyEventHandlers {
                    it.onTimeoutHandler()
                }
            } else {
                startTime += 1000
                mMainHandler.postDelayed(this, 1000)
            }
        }
    }

    private fun stopTimer() {
        mMainHandler.removeCallbacks(timeoutDownTask)
    }

    fun onEvent(key: String?, value: String?) {
        mHyUtil?.onEvent(key, value)
    }


    override fun startService(appId: String, apiKey: String, apiSecret: String, convertType: LanguageConvertType) {
        mHyUtil = HyUtil(appId, apiKey, apiSecret, mHyUtilListener, rtcEngine)
        this.convertType = convertType
        // 设置日志配置。最多设置1次，若不设置则不打日志。
        rtcEngine.setExtensionProviderProperty(
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
                .put("lvl", 5)
                .toString()
        )

    }

    override fun stopService() {
        rtcEngine.enableExtension(
            ExtensionManager.EXTENSION_VENDOR_NAME,
            ExtensionManager.EXTENSION_AUDIO_FILTER_NAME, false
        )
    }

    override fun setAudioVolumeIndication(interval: Int, smooth: Int) {
        rtcEngine.enableAudioVolumeIndication(interval, smooth, true)
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
        startTimer()
        val paramWrap: HyUtil.ParamWrap =
            if (convertType == LanguageConvertType.EN) hyUtil.paramWraps[1] else hyUtil.paramWraps[0]
        convertorStatus = ConvertorStatusType.Start
        hyUtil.start(paramWrap, false)
    }

    override fun flushConvertor() {
        val hyUtil = mHyUtil ?: return
        stopTimer()
        convertorStatus = ConvertorStatusType.Flush
        hyUtil.flush()
    }

    override fun stopConvertor() {
        val hyUtil = mHyUtil ?: return
        stopTimer()
        convertorStatus = ConvertorStatusType.Idle
        hyUtil.stop()
    }

    fun writeLog(content: String, level: Int) {
        rtcEngine.writeLog(level, content)
    }

//    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
//        super.onAudioVolumeIndication(speakers, totalVolume)
//        observableHelper.notifyEventHandlers { it.convertAudioVolumeHandler(totalVolume) }
//    }


    private val mHyUtilListener: HyUtil.IListener = object : HyUtil.IListener {
        override fun onLogI(tip: String?) {
            observableHelper.notifyEventHandlers { it.onLogHandler(tip ?: "", false) }
        }

        override fun onLogE(tip: String?) {
            observableHelper.notifyEventHandlers { it.onLogHandler(tip ?: "", true) }
        }

        override fun onIstText(text: String?, exception: Exception?) {
            exception?.let { error ->
                observableHelper.notifyEventHandlers { it.convertResultHandler(text, error) }
            }
            Log.d("AudioTextConvertor", "convertorStatus:$convertorStatus, onIstText: $text")
            if (convertorStatus == ConvertorStatusType.Flush) {
                observableHelper.notifyEventHandlers { it.convertResultHandler(text?:"", exception) }
                convertorStatus = ConvertorStatusType.Idle
            }
        }

        override fun onItsText(text: String?) {
            // nothing
        }
    }
}