package io.agora.scene.voice.ui.debugSettings

import android.content.Context
import android.content.SharedPreferences
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.voice.imkit.manager.ChatroomConfigManager

object VoiceDebugSettingModel {

    // NS
    const val CHE_AUDIO_SF_NSENABLE = "che.audio.sf.nsEnable" // value取值0/1 default 0
    const val CHE_AUDIO_SF_AINSTOLOADFLAG = "che.audio.sf.ainsToLoadFlag" // value取值0/1 default 0
    const val CHE_AUDIO_SF_NSNGALGROUTE = "che.audio.sf.nsngAlgRoute" // value取值10/11/12, default 10
    const val CHE_AUDIO_SF_NSNGPREDEFAGG = "che.audio.sf.nsngPredefAgg" // value取值-1/10/11, default 11
    const val CHE_AUDIO_SF_NSNGMAPINMASKMIN = "che.audio.sf.nsngMapInMaskMin" // value取值[0,1000], defalut 80
    const val CHE_AUDIO_SF_NSNGMAPOUTMASKMIN = "che.audio.sf.nsngMapOutMaskMin" // value取值[0,1000], default 50
    const val CHE_AUDIO_SF_STATNSLOWERBOUND = "che.audio.sf.statNsLowerBound" // value取值[0,1000]， default 5
    const val CHE_AUDIO_SF_NSNGFINALMASKLOWERBOUND =
        "che.audio.sf.nsngFinalMaskLowerBound" // value取值[0,1000], default 30
    const val CHE_AUDIO_SF_STATNSENHFACTOR = "che.audio.sf.statNsEnhFactor" // value取值[100,200], default 200
    const val CHE_AUDIO_SF_STATNSFASTNSSPEECHTRIGTHRESHOLD =
        "che.audio.sf.statNsFastNsSpeechTrigThreshold" // value取值 [0,100], default 0

    // 音乐保护
    const val CHE_AUDIO_AED_ENABLE = "che.audio.aed.enable" // value取值范围0/1, default 1
    const val CHE_AUDIO_SF_NSNGMUSICPROBTHR = "che.audio.sf.nsngMusicProbThr" // value取值范围[0,100], default 85
    const val CHE_AUDIO_SF_STATNSMUSICMODEBACKOFFDB =
        "che.audio.sf.statNsMusicModeBackoffDB" // value取值[0-100], default 200
    const val CHE_AUDIO_SF_AINSMUSICMODEBACKOFFDB =
        "che.audio.sf.ainsMusicModeBackoffDB" // value取值[0-100], default 270

    // 人声保护
    const val CHE_AUDIO_SF_AINSSPEECHPROTECTTHRESHOLD =
        "che.audio.sf.ainsSpeechProtectThreshold" // value取值[0,100], default 100

    var callback: OnDebugSettingCallback? = null

    private val mSharedPreferences: SharedPreferences by lazy {
        AgoraApplication.the().getSharedPreferences("sp_voice_debugg_profile", Context.MODE_PRIVATE)
    }

    private fun putInt(key: String, value: Int) {
        mSharedPreferences.edit().putInt(key, value).apply()
    }

    var nsEnable = mSharedPreferences.getInt(CHE_AUDIO_SF_NSENABLE, 0)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsEnable(newValue)
                putInt(CHE_AUDIO_SF_NSENABLE, newValue)
            }
        }

    var ainsToLoadFlag = mSharedPreferences.getInt(CHE_AUDIO_SF_AINSTOLOADFLAG, 0)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onAinsToLoadFlag(newValue)
                putInt(CHE_AUDIO_SF_AINSTOLOADFLAG, newValue)
            }
        }

    var nsngAlgRoute = mSharedPreferences.getInt(CHE_AUDIO_SF_NSNGALGROUTE, 10)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsngAlgRoute(newValue)
                putInt(CHE_AUDIO_SF_NSNGALGROUTE, newValue)
            }
        }

    var nsngPredefAgg = mSharedPreferences.getInt(CHE_AUDIO_SF_NSNGPREDEFAGG, 11)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsngPredefAgg(newValue)
                putInt(CHE_AUDIO_SF_NSNGPREDEFAGG, newValue)
            }
        }

    var nsngMapInMaskMin = mSharedPreferences.getInt(CHE_AUDIO_SF_NSNGMAPINMASKMIN, 80)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsngMapInMaskMin(newValue)
                putInt(CHE_AUDIO_SF_NSNGMAPINMASKMIN, newValue)
            }
        }

    var nsngMapOutMaskMin = mSharedPreferences.getInt(CHE_AUDIO_SF_NSNGMAPOUTMASKMIN, 50)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsngMapOutMaskMin(newValue)
                putInt(CHE_AUDIO_SF_NSNGMAPOUTMASKMIN, newValue)
            }
        }

    var statNsLowerBound = mSharedPreferences.getInt(CHE_AUDIO_SF_STATNSLOWERBOUND, 5)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onStatNsLowerBound(newValue)
                putInt(CHE_AUDIO_SF_STATNSLOWERBOUND, newValue)
            }
        }

    var nsngFinalMaskLowerBound = mSharedPreferences.getInt(CHE_AUDIO_SF_NSNGFINALMASKLOWERBOUND, 30)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsngFinalMaskLowerBound(newValue)
                putInt(CHE_AUDIO_SF_NSNGFINALMASKLOWERBOUND, newValue)
            }
        }

    var statNsEnhFactor = mSharedPreferences.getInt(CHE_AUDIO_SF_STATNSENHFACTOR, 200)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onStatNsEnhFactor(newValue)
                putInt(CHE_AUDIO_SF_STATNSENHFACTOR, newValue)
            }
        }

    var statNsFastNsSpeechTrigThreshold = mSharedPreferences.getInt(CHE_AUDIO_SF_STATNSFASTNSSPEECHTRIGTHRESHOLD, 0)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onStatNsFastNsSpeechTrigThreshold(newValue)
                putInt(CHE_AUDIO_SF_STATNSFASTNSSPEECHTRIGTHRESHOLD, newValue)
            }
        }

    var aedEnable = mSharedPreferences.getInt(CHE_AUDIO_AED_ENABLE, 1)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onAedEnable(newValue)
                putInt(CHE_AUDIO_AED_ENABLE, newValue)
            }
        }

    var nsngMusicProbThr = mSharedPreferences.getInt(CHE_AUDIO_SF_NSNGMUSICPROBTHR, 85)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onNsngMusicProbThr(newValue)
                putInt(CHE_AUDIO_SF_NSNGMUSICPROBTHR, newValue)
            }
        }

    var statNsMusicModeBackoffDB = mSharedPreferences.getInt(CHE_AUDIO_SF_STATNSMUSICMODEBACKOFFDB, 200)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onStatNsMusicModeBackoffDB(newValue)
                putInt(CHE_AUDIO_SF_STATNSMUSICMODEBACKOFFDB, newValue)
            }
        }

    var ainsMusicModeBackoffDB = mSharedPreferences.getInt(CHE_AUDIO_SF_AINSMUSICMODEBACKOFFDB, 270)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onAinsMusicModeBackoffDB(newValue)
                putInt(CHE_AUDIO_SF_AINSMUSICMODEBACKOFFDB, newValue)
            }
        }

    var ainsSpeechProtectThreshold = mSharedPreferences.getInt(CHE_AUDIO_SF_AINSSPEECHPROTECTTHRESHOLD, 100)
        set(newValue) {
            if (field != newValue) {
                field = newValue
                callback?.onAinsSpeechProtectThreshold(newValue)
                putInt(CHE_AUDIO_SF_AINSSPEECHPROTECTTHRESHOLD, newValue)
            }
        }

}

interface OnDebugSettingCallback {
    fun onNsEnable(newValue: Int)
    fun onAinsToLoadFlag(newValue: Int)
    fun onNsngAlgRoute(newValue: Int)
    fun onNsngPredefAgg(newValue: Int)
    fun onNsngMapInMaskMin(newValue: Int)
    fun onNsngMapOutMaskMin(newValue: Int)
    fun onStatNsLowerBound(newValue: Int)
    fun onNsngFinalMaskLowerBound(newValue: Int)
    fun onStatNsEnhFactor(newValue: Int)
    fun onStatNsFastNsSpeechTrigThreshold(newValue: Int)
    fun onAedEnable(newValue: Int)
    fun onNsngMusicProbThr(newValue: Int)
    fun onStatNsMusicModeBackoffDB(newValue: Int)
    fun onAinsMusicModeBackoffDB(newValue: Int)
    fun onAinsSpeechProtectThreshold(newValue: Int)
}