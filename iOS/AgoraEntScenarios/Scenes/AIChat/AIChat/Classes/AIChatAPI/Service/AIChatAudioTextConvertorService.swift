//
//  AIChatAudioTextConvertorService.swift
//  TestHy
//
//  Created by wushengtao on 2024/8/28.
//

import Foundation
import AgoraRtcKit

/// `AIChatAudioTextConvertorDelegate` 协议定义了处理音频到文本转换结果的方法。
/// 任何实现此协议的类都必须实现 `convertResultHandler(result:error:)` 方法。
///
/// 实现此协议的类可以通过实现 `convertResultHandler(result:error:)` 方法来接收音频到文本的转换结果。
/// 例如，当音频转换成功时，可以处理转换后的文本；当转换失败时，可以处理错误信息。
@objc protocol AIChatAudioTextConvertorDelegate: NSObjectProtocol {
    
    /// 当音频转换为文本时会回调此方法。
    /// - Parameters
    ///  - result: 转换后的文本结果。
    ///  - error: 如果转换过程中发生错误，则包含错误信息；否则为 `nil`。
    func convertResultHandler(result: String, error: Error?)
}

/// `AIChatAudioTextConvertEvent` 协议定义了音频转换过程中的事件处理方法。
/// 实现此协议的类可以通过这些方法来控制音频获取和处理的生命周期。
protocol AIChatAudioTextConvertEvent {
    /// 开始音频获取。
    ///
    /// 调用此方法以开始捕获音频数据并进行转换处理。
    func startConvertor()
    
    /// 结束音频获取。
    ///
    /// 调用此方法后，会先处理完当前捕获的音频数据并生成转换结果，然后进入空闲状态。
    /// 适用于正常结束音频捕获的场景。
    func flushConvertor()
    
    /// 停止音频获取。
    ///
    /// 调用此方法后，会丢弃当前捕获的音频数据和转换结果，然后进入空闲状态。
    /// 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
    func stopConvertor()
}

/// `AIChatAudioTextConvertor` 协议定义了音频到文本转换服务的基本操作。
/// 实现此协议的类可以启动服务并管理转换过程中的委托。
protocol AIChatAudioTextConvertor {
    
    /// 启动音频到文本转换服务。
    ///
    /// - Parameters:
    ///   - appId: 服务商分配的appId。
    ///   - apiKey: 服务商分配的apiKey。
    ///   - apiSecret: 服务商分配的apiSecret。
    ///   - convertType: 指定的语言转换类型。
    ///   - agoraRtcKit: 用于音频处理的 Agora RTC 引擎实例。
    ///
    /// 调用此方法以启动音频到文本转换服务，并配置必要的参数。
    func run(appId: String, apiKey: String, apiSecret: String, convertType: LanguageConvertType, agoraRtcKit: AgoraRtcEngineKit?)
    
    /// 设置启音量指示器回调，以报告哪些用户在讲话以及讲话者的音量。
    ///
    /// - Parameters:
    ///   - interval: 设置两个连续音量指示之间的时间间隔,两个连续音量指示之间的时间间隔（毫秒），应为 200 的整数倍（小于 200 将被设置为 200)
    ///   - smooth 设置音量指示器灵敏度的平滑因子。取值范围为 [0, 10]。值越大，指示器越灵敏。推荐值为 3。
    ///
    ///   调用此方法以启动音量指示器回调
    func setAudioVolumeIndication(interval: Int, smooth: Int)
    
    /// 添加一个委托以接收转换结果。
    ///
    /// - Parameter delegate: 实现 `AIChatAudioTextConvertorDelegate` 协议的对象。
    ///
    /// 调用此方法以添加一个委托，该委托将接收音频到文本转换的结果。
    func addDelegate(_ delegate: AIChatAudioTextConvertorDelegate)
    
    /// 移除一个委托。
    ///
    /// - Parameter delegate: 实现 `AIChatAudioTextConvertorDelegate` 协议的对象。
    ///
    /// 调用此方法以移除先前添加的委托。
    func removeDelegate(_ delegate: AIChatAudioTextConvertorDelegate)
    
    /// 移除所有委托。
    ///
    /// 调用此方法以移除所有先前添加的委托。
    func removeAllDelegates()
    
    /// 清理service
    func destory()
}

/// `LanguageConvertType` 枚举定义了音频转换过程中支持的语言模式。
/// 通过选择不同的模式，可以控制音频转换器识别的语言类型。
enum LanguageConvertType {
    
    /// 中英文模式：中文和英文均可识别。
    ///
    /// 适用于需要同时识别中文和英文的场景。
    case normal
    
    /// 英文模式：只能识别出英文。
    ///
    /// 适用于只需要识别英文的场景。
    case en
}

enum AIChatConvertorType {
    case idle
    case start
    case flush
}

class AIChatAudioTextConvertorService: NSObject {
    private var commonDict: [String: String]!
    private weak var engine: AgoraRtcEngineKit?
    private var convertType: LanguageConvertType = .normal
    private let delegates: NSHashTable<AIChatAudioTextConvertorDelegate> = NSHashTable<AIChatAudioTextConvertorDelegate>.weakObjects()
    //maximum recording duration
    private var maxDuration: Int = 60
    private var timer: Timer?
    weak var delegate: AIChatAudioTextConvertorDelegate?
    
    private var result: String = ""
    private var state: AIChatConvertorType = .idle {
        didSet {
            aichatPrint("state: '\(state)'", context: "AIChatAudioTextConvertorService")
        }
    }
    
    private func parseIstResult(dict: [String: Any]) {
        var str = ""
        guard let code = dict["code"] as? Int, code == 0 else {
            let errorMsg = "转写失败"
            aichatWarn(errorMsg, context: "AIChatAudioTextConvertorService")
            DispatchQueue.main.async {
                for delegate in self.delegates.allObjects {
                    delegate.convertResultHandler(result: "", error: NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey : errorMsg]))
                }
            }
            return
        }
        
        guard let dataDict = dict["data"] as? [String: Any], !dataDict.isEmpty else { return }
        guard let resultDict = dataDict["result"] as? [String: Any], !resultDict.isEmpty else { return }
        guard let wsArray = resultDict["ws"] as? [[String: Any]] else { return }
        
        for wsItemDict in wsArray {
            guard let cwArray = wsItemDict["cw"] as? [[String: Any]], let cwItemDict = cwArray.first else {
                continue
            }
            
            if let w = cwItemDict["w"] as? String, !isBlankString(string: w) {
                str += w
            }
        }
        
        aichatPrint("parseIstResult: '\(str)'", context: "AIChatAudioTextConvertorService")
        DispatchQueue.main.async {
            self.result += " \(str)"
            aichatPrint("total Result: '\(self.result)'", context: "AIChatAudioTextConvertorService")
            if self.state == .flush {
                for delegate in self.delegates.allObjects {
                     delegate.convertResultHandler(result: self.result, error: nil)
                }
            }
        }
    }

    private func isBlankString(string: String?) -> Bool {
        guard let string = string else { return true }
        if string.isEmpty { return true }
        if string.trimmingCharacters(in: .whitespaces).isEmpty { return true }
        return false
    }
    
    private func logPath() -> String {
        let documentsPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
        let logDir = "\(documentsPath)/log"
        
        return logDir
    }
    
    private func startTimer() {
        stopTimer()
        timer = Timer.scheduledTimer(withTimeInterval: TimeInterval(maxDuration), repeats: false, block: {[weak self] t in
            aichatPrint("recording timeout", context: "AIChatAudioTextConvertorService")
            self?.flushConvertor()
        })
    }
    
    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
}

//MARK: AIChatAudioTextConvertor
extension AIChatAudioTextConvertorService: AIChatAudioTextConvertor {
    func setAudioVolumeIndication(interval: Int, smooth: Int) {
        guard let engine = engine else { return }
        
        engine.enableAudioVolumeIndication(interval, smooth: smooth, reportVad: true)
    }
    
    func run(appId: String, apiKey: String, apiSecret: String, convertType: LanguageConvertType, agoraRtcKit: AgoraRtcEngineKit?) {
        self.engine = agoraRtcKit
        self.commonDict = ["app_id": appId, "api_key": apiKey, "api_secret": apiSecret]
        self.convertType = convertType
        guard let engine = engine else { return }
        engine.setRecordingAudioFrameParametersWithSampleRate(44100, channel: 1, mode: .readWrite, samplesPerCall: 4410)
        engine.setMixedAudioFrameParametersWithSampleRate(44100, channel: 1, samplesPerCall: 4410)
        engine.setPlaybackAudioFrameParametersWithSampleRate(44100, channel: 1, mode: .readWrite, samplesPerCall: 4410)
        engine.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)

        engine.enableExtension(withVendor: "Hy", extension: "IstIts", enabled: true)
        let logDir = logPath()
        var dictionary = [String: Any]()
        dictionary["dir"] = logDir
        dictionary["lvl"] = 0
        
        if let data = try? JSONSerialization.data(withJSONObject: dictionary, options: .prettyPrinted),
           let str = String(data: data, encoding: .utf8) {
            engine.setExtensionProviderPropertyWithVendor("Hy", key: "log_cfg", value: str)
        }

        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = true
        
//        engine.joinChannel(byToken: nil, channelId: "agora_extension", uid: 0, mediaOptions: option)
        engine.setEnableSpeakerphone(true)
    }
    
    func addDelegate(_ delegate: any AIChatAudioTextConvertorDelegate) {
        delegates.add(delegate)
    }
    
    func removeDelegate(_ delegate: any AIChatAudioTextConvertorDelegate) {
        delegates.remove(delegate)
    }
    
    func removeAllDelegates() {
        delegates.removeAllObjects()
    }
    
    func destory() {
        removeAllDelegates()
    }
}

//MARK: AIChatAudioTextConvertEvent
extension AIChatAudioTextConvertorService: AIChatAudioTextConvertEvent {
    func startConvertor() {
        guard let engine = engine, state != .start else { return }
        result = ""
        startTimer()
        state = .start
        engine.enableLocalAudio(true)
        engine.muteLocalAudioStream(false)
        
        var rootDict = [String: Any]()
        rootDict["common"] = commonDict
        
        var istDict = [String: Any]()
        istDict["uri"] = "wss://ist-api.xfyun.cn/v2/ist"
        
        var itsDict = [String: Any]()
        itsDict["uri"] = "https://itrans.xfyun.cn/v2/its"
        
        if convertType == .normal {
            let istBusinessDict: [String: Any] = ["language": "zh_cn", "accent": "mandarin", "domain": "ist_ed_open", "language_type": 1]
            let istReqDict = ["business": istBusinessDict]
            istDict["req"] = istReqDict
            rootDict["ist"] = istDict
            
            let itsBusinessDict = ["from": "cn", "to": "en"]
            let itsReqDict = ["business": itsBusinessDict]
            itsDict["req"] = itsReqDict
            rootDict["its"] = itsDict
        } else {
            let businessDict: [String: Any] = ["language": "zh_cn", "accent": "mandarin", "domain": "ist_ed_open", "language_type": 3]
            let reqDict = ["business": businessDict]
            istDict["req"] = reqDict
            rootDict["ist"] = istDict
            
            let itsBusinessDict = ["from": "en", "to": "cn"]
            let itsReqDict = ["business": itsBusinessDict]
            itsDict["req"] = itsReqDict
            rootDict["its"] = itsDict
        }
        
        if let data = try? JSONSerialization.data(withJSONObject: rootDict, options: .prettyPrinted),
           let str = String(data: data, encoding: .utf8) {
            engine.setExtensionPropertyWithVendor("Hy", extension: "IstIts", key: "start_listening", value: str)
        }
        
        aichatPrint("startConvertor", context: "AIChatAudioTextConvertorService")
    }
    
    func flushConvertor() {
        guard let engine = self.engine else { return }
        stopTimer()
        state = .flush
        
        engine.setExtensionPropertyWithVendor("Hy", extension: "IstIts", key: "flush_listening", value: "{}")
        engine.muteLocalAudioStream(true)
        
        aichatPrint("flushConvertor", context: "AIChatAudioTextConvertorService")
    }
    
    func stopConvertor() {
        guard let engine = self.engine, state != .idle else { return }
        stopTimer()
        state = .idle
        
        engine.setExtensionPropertyWithVendor("Hy", extension: "IstIts", key: "stop_listening", value: "{}")
        engine.muteLocalAudioStream(true)
        engine.enableLocalAudio(false)
        
        aichatPrint("stopConvertor", context: "AIChatAudioTextConvertorService")
    }
}

//MARK: AgoraMediaFilterEventDelegate
extension AIChatAudioTextConvertorService: AgoraMediaFilterEventDelegate {
    func onEvent(_ provider: String?, extension scene: String?, key: String?, value: String?) {
        guard let scene = scene else { return }
        aichatPrint("onEvent scene: \(scene) key: \(key ?? "")", context: "AIChatAudioTextConvertorService")
        
        if scene == "IstIts" {
            guard let key = key else { return }
            
            if key == "ist_result" { //语音转写结果
                guard let value = value, let jsonData = value.data(using: .utf8) else { return }
                
                do {
                    guard let dict = try JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers) as? [String: Any] else { return }
                    
                    parseIstResult(dict: dict)
                } catch {
                    aichatError("JSON parsing error: \(error)", context: "AIChatAudioTextConvertorService")
                }
            } else if key == "error" {
            } else if key == "end" {
            }
        }
    }
}

