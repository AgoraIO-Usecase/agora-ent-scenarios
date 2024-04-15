package io.agora.scene.ktv.ktvapi

import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import org.json.JSONObject
import java.util.HashMap

enum class ApiEventType(val value: Int) {
    API(0),
    COST(1),
    CUSTOM(2)
}

object ApiEventKey {
    const val TYPE = "type"
    const val DESC = "desc"
    const val API_VALUE = "apiValue"
    const val TIMESTAMP = "ts"
    const val EXT = "ext"
}

object ApiCostEvent {
    const val CHANNEL_USAGE = "channelUsage"                 //频道使用耗时
    const val FIRST_FRAME_ACTUAL = "firstFrameActual"        //首帧实际耗时
    const val FIRST_FRAME_PERCEIVED = "firstFramePerceived"  //首帧感官耗时
}

class APIReporter(
    private val category: String,
    private val rtcEngine: RtcEngine
) {
    private val tag = "APIReporter"
    private val messageId = "agora:scenarioAPI"
    private val durationEventStartMap = HashMap<String, Long>()

    init {
        configParameters()
    }

    // 上报普通场景化API
    fun reportFuncEvent(name: String, value: Map<String, Any>, ext: Map<String, Any>) {
        Log.d(tag, "reportFuncEvent: $name value: $value ext: $ext")
        val eventMap = mapOf(ApiEventKey.TYPE to ApiEventType.API.value, ApiEventKey.DESC to name)
        val labelMap = mapOf(ApiEventKey.API_VALUE to value, ApiEventKey.TIMESTAMP to getCurrentTs(), ApiEventKey.EXT to ext)
        val event = convertToJSONString(eventMap) ?: ""
        val label = convertToJSONString(labelMap) ?: ""
        rtcEngine.sendCustomReportMessage(messageId, category, event, label, 0)
    }

    fun startDurationEvent(name: String) {
        Log.d(tag, "startDurationEvent: $name")
        durationEventStartMap[name] = getCurrentTs()
    }

    fun endDurationEvent(name: String) {
        Log.d(tag, "endDurationEvent: $name")
        val beginTs = durationEventStartMap[name] ?: return
        durationEventStartMap.remove(name)
        val ts = getCurrentTs()
        val cost = (ts - beginTs).toInt()

        innerReportCostEvent(ts, name, cost)
    }

    // 上报耗时打点信息
    fun reportCostEvent(name: String, cost: Int) {
        durationEventStartMap.remove(name)
        innerReportCostEvent(
            ts = getCurrentTs(),
            name = name,
            cost = cost
        )
    }

    // 上报自定义信息
    fun reportCustomEvent(name: String, ext: Map<String, Any>) {
        Log.d(tag, "reportCustomEvent: $name ext: $ext")
        val eventMap = mapOf(ApiEventKey.TYPE to ApiEventType.CUSTOM.value, ApiEventKey.DESC to name)
        val labelMap = mapOf(ApiEventKey.TIMESTAMP to getCurrentTs(), ApiEventKey.EXT to ext)
        val event = convertToJSONString(eventMap) ?: ""
        val label = convertToJSONString(labelMap) ?: ""
        rtcEngine.sendCustomReportMessage(messageId, category, event, label, 0)
    }

    fun writeLog(content: String, level: Int) {
        rtcEngine.writeLog(level, content)
    }

    fun cleanCache() {
        durationEventStartMap.clear()
    }

    // ---------------------- private ----------------------

    private fun configParameters() {
        //rtcEngine.setParameters("{\"rtc.qos_for_test_purpose\": true}") //测试环境使用
        // 数据上报
        rtcEngine.setParameters("{\"rtc.direct_send_custom_event\": true}")
        // 日志写入
        rtcEngine.setParameters("{\"rtc.log_external_input\": true}")
    }

    private fun getCurrentTs(): Long {
        return System.currentTimeMillis()
    }

    private fun innerReportCostEvent(ts: Long, name: String, cost: Int) {
        Log.d(tag, "reportCostEvent: $name cost: $cost ms")
        writeLog("reportCostEvent: $name cost: $cost ms", Constants.LOG_LEVEL_INFO)
        val eventMap = mapOf(ApiEventKey.TYPE to ApiEventType.COST.value, ApiEventKey.DESC to name)
        val labelMap = mapOf(ApiEventKey.TIMESTAMP to ts)
        val event = convertToJSONString(eventMap) ?: ""
        val label = convertToJSONString(labelMap) ?: ""
        rtcEngine.sendCustomReportMessage(messageId, category, event, label, cost)
    }

    private fun convertToJSONString(dictionary: Map<String, Any>): String? {
        return try {
            JSONObject(dictionary).toString()
        } catch (e: Exception) {
            writeLog("[$tag]convert to json fail: $e dictionary: $dictionary", Constants.LOG_LEVEL_WARNING)
            null
        }
    }
}