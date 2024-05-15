//
//  APIReporter.swift
//  CallAPI
//
//  Created by wushengtao on 2024/4/8.
//

import AgoraRtcKit


/// 场景化类型
public enum APIType: Int {
    case ktv = 1             //K歌
    case call = 2            //呼叫
    case beauty = 3          //美颜
    case videoLoader = 4     //秒开/秒切
    case pk = 5              //团战
    case vitualSpace = 6     //
    case screenSpace = 7     //屏幕共享
    case audioScenario = 8   //音频scenario
}

enum APIEventType: Int {
    case api = 0       //api事件
    case cost          //耗时事件
    case custom        //自定义事件
}

struct ApiEventKey {
    static let type = "type"
    static let desc = "desc"
    static let apiValue = "apiValue"
    static let ts = "ts"
    static let ext = "ext"
}

struct APICostEvent {
    static let channelUsage = "channelUsage"                  //频道使用耗时
    static let firstFrameActual = "firstFrameActual"          //首帧实际耗时
    static let firstFramePerceived = "firstFramePerceived"    //首帧感官耗时
}

let formatter = DateFormatter()
func debugApiPrint(_ message: String) {
#if DEBUG
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    print("\(timeString) \(message)")
#endif
}

@objcMembers
public class APIReporter: NSObject {
    private var engine: AgoraRtcEngineKit
    private let messsageId: String = "agora:scenarioAPI"
    private var category: String
    private var durationEventStartMap: [String: Int64] = [:]
    
    //MARK: public
    public init(type: APIType, version: String, engine: AgoraRtcEngineKit) {
        self.category = "\(type.rawValue)_iOS_\(version)"
        self.engine = engine
        super.init()
        
        configParameters()
    }
    
    public func reportFuncEvent(name: String, value: [String: Any], ext: [String: Any]) {
        let content = "[APIReporter]reportFuncEvent: \(name) value: \(value) ext: \(ext)"
        debugApiPrint(content)
        let eventMap: [String: Any] = [ApiEventKey.type: APIEventType.api.rawValue, ApiEventKey.desc: name]
        let labelMap: [String: Any] = [ApiEventKey.apiValue: value, ApiEventKey.ts: getCurrentTs(), ApiEventKey.ext: ext]
        let event = convertToJSONString(eventMap) ?? ""
        let label = convertToJSONString(labelMap) ?? ""
        engine.sendCustomReportMessage(messsageId,
                                       category: category,
                                       event: event,
                                       label: label,
                                       value: 0)
    }
    
    public func startDurationEvent(name: String) {
        durationEventStartMap[name] = getCurrentTs()
    }
    
    public func endDurationEvent(name: String, ext: [String: Any]) {
        guard let beginTs = durationEventStartMap[name] else {return}
        durationEventStartMap.removeValue(forKey: name)
        let ts = getCurrentTs()
        let cost = Int(ts - beginTs)
        
        reportCostEvent(ts: ts, name: name, cost: cost, ext: ext)
    }
    
    public func reportCostEvent(name: String, cost: Int, ext: [String: Any]) {
        durationEventStartMap.removeValue(forKey: name)
        reportCostEvent(ts: getCurrentTs(), name: name, cost: cost, ext: ext)
    }
    
    public func reportCustomEvent(name: String, ext: [String: Any]) {
        let content = "[APIReporter]reportCustomEvent: \(name) ext: \(ext)"
        debugApiPrint(content)
        let eventMap: [String: Any] = [ApiEventKey.type: APIEventType.custom.rawValue, ApiEventKey.desc: name]
        let labelMap: [String: Any] = [ApiEventKey.ts: getCurrentTs(), ApiEventKey.ext: ext]
        let event = convertToJSONString(eventMap) ?? ""
        let label = convertToJSONString(labelMap) ?? ""
        engine.sendCustomReportMessage(messsageId,
                                       category: category,
                                       event: event,
                                       label: label,
                                       value: 0)
    }
    
    public func writeLog(content: String, level: AgoraLogLevel) {
        engine.writeLog(level, content: content)
    }
    
    public func cleanCache() {
        durationEventStartMap.removeAll()
    }
    
    //MARK: private
    private func reportCostEvent(ts: Int64, name: String, cost: Int, ext: [String: Any]) {
        let content = "[APIReporter]reportCostEvent: \(name) cost: \(cost) ms ext: \(ext)"
        debugApiPrint(content)
        writeLog(content: content, level: .info)
        let eventMap: [String: Any] = [ApiEventKey.type: APIEventType.cost.rawValue, ApiEventKey.desc: name]
        let labelMap: [String: Any] = [ApiEventKey.ts: ts, ApiEventKey.ext: ext]
        let event = convertToJSONString(eventMap) ?? ""
        let label = convertToJSONString(labelMap) ?? ""
        engine.sendCustomReportMessage(messsageId,
                                       category: category,
                                       event: event,
                                       label: label,
                                       value: cost)
    }
    
    private func configParameters() {
//        engine.setParameters("{\"rtc.qos_for_test_purpose\": true}")
        engine.setParameters("{\"rtc.direct_send_custom_event\": true}")
        engine.setParameters("{\"rtc.log_external_input\": true}")
    }
    
    private func getCurrentTs() -> Int64 {
        return Int64(round(Date().timeIntervalSince1970 * 1000.0))
    }
    
    private func convertToJSONString(_ dictionary: [String: Any]) -> String? {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dictionary, options: [])
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                return jsonString
            }
        } catch {
            writeLog(content: "[APIReporter]convert to json fail: \(error) dictionary: \(dictionary)", level: .warn)
        }
        return nil
    }
}
