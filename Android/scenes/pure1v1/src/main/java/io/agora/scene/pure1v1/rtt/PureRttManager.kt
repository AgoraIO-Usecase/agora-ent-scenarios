package io.agora.scene.pure1v1.rtt

import android.os.Handler
import android.os.Looper
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.pure1v1.Pure1v1Logger
import io.agora.scene.pure1v1.R

interface RttEventListener {
    fun onRttStart()
    fun onRttStop()
}

object PureRttManager {

    private val TAG = "PureRttManager"

    val mRttLanguages by lazy {
        AgoraApplication.the().resources.getStringArray(R.array.pure1v1_rtt_language)
    }

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    var isRttEnabled: Boolean = false
        private set(newValue) {
            field = newValue
        }

    private val rttListenerList: MutableList<RttEventListener> = mutableListOf()

    var selectedSourceLanguageIndex: Int = 29
        set(newValue) {
            if (newValue in mRttLanguages.indices) {
                field = newValue
            } else {
                Pure1v1Logger.d(TAG, "Index out of range")
            }
        }
    var selectedTargetLanguageIndex: Int = 21
        set(newValue) {
            if (newValue in mRttLanguages.indices) {
                field = newValue
            } else {
                Pure1v1Logger.d(TAG, "Index out of range")
            }
        }

    // 当前语言
    private val currentSourceLanguage get() = mRttLanguages[selectedSourceLanguageIndex].split(":")
    val currentSourceLanguageCode: String get() = currentSourceLanguage[0].trim()
    val currentSourceLanguageName: String get() = currentSourceLanguage[1].trim()

    // 目标语言
    private val currentTargetLanguage get() = mRttLanguages[selectedTargetLanguageIndex].split(":")
    val currentTargetLanguageCode: String get() = currentTargetLanguage[0].trim()
    val currentTargetLanguageName: String get() = currentTargetLanguage[1].trim()

    // 被叫 10000，20000, 主叫叫 3000，4000
    var subBotUid: String = "3000" // 订阅音频
    var pubBotUid: String = "4000" // 推字幕
    var targetUid: String = ""
    var subBotToken: String = ""
    var pubBotToken: String = ""

    fun enableRtt(channelName: String, completion: (Boolean) -> Unit) {
        Pure1v1Logger.d(
            TAG,
            "enableRtt[$channelName] subBotUid[$subBotUid] pubBotUid[$pubBotUid] targetUid[$targetUid] isRttEnabled[$isRttEnabled]"
        )
        if (isRttEnabled) {
            completion(false)
            return
        }
        PureRttApiManager.fetchStartRtt(
            languages = listOf(currentSourceLanguageCode),
            sourceLanguage = currentSourceLanguageCode,
            targetLanguages = listOf(currentTargetLanguageCode),
            channelName = channelName,
            subBotUid = subBotUid,
            subBotToken = subBotToken,
            pubBotUid = pubBotUid,
            pubBotToken = pubBotToken
        ) { success ->
            mMainHandler.post {
                if (success) {
                    isRttEnabled = true
                    rttListenerList.forEach { it.onRttStart() }
                }
                completion(success)
            }
        }
    }

    fun disableRtt(force: Boolean, completion: (Boolean) -> Unit) {
        Pure1v1Logger.d(TAG, "RttManager disableRtt force[$force] isRttEnabled[$isRttEnabled]")
        if (!isRttEnabled) {
            completion(false)
            return
        }
        if (force) {
            isRttEnabled = false
            rttListenerList.forEach { it.onRttStop() }
        }
        PureRttApiManager.fetchStopRtt { success ->
            mMainHandler.post {
                if (success) {
                    isRttEnabled = false
                    rttListenerList.forEach { it.onRttStop() }
                }
                completion(success)
            }
        }
    }

    fun resetRttSettings(isCaller: Boolean) {
        if (isCaller) {
            subBotUid = "30000"
            pubBotUid = "40000"
        } else {
            subBotUid = "10000"
            pubBotUid = "20000"
        }
        subBotToken = ""
        pubBotToken = ""
        selectedSourceLanguageIndex = 29
        selectedTargetLanguageIndex = 21
        disableRtt(true) {  }
    }

    fun addListener(listener: RttEventListener) {
        if (!rttListenerList.contains(listener)) {
            rttListenerList.add(listener)
        }
    }

    fun removeListener(listener: RttEventListener) {
        rttListenerList.remove(listener)
    }
}