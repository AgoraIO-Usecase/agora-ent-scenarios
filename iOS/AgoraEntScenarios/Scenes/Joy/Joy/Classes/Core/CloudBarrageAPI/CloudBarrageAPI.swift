//
//  CloudBarrageAPI.swift
//  Joy
//
//  Created by wushengtao on 2023/11/29.
//

import Foundation
import AgoraRtcKit
import YYCategories

public enum UserGameStatus: String {
    case unknown = "unknown"
    case schedule = "schedule"
    case scheduled = "scheduled"
    case starting = "starting"
    case startFailed = "start_failed"
    case started = "started"
    case stopping = "stopping"
    case stopped = "stopped"
}

public struct CloudBarrageConfig {
    var appId: String?
    var basicAuth: String?
    var host: String? = "https://service-staging.agora.io/toolbox/v1/"
    var engine: AgoraRtcEngineKit?
    var rtmToken: String?
}

public class CloudBarrageAPI: NSObject {
    public private(set) var apiConfig: CloudBarrageConfig?
    public static let shared: CloudBarrageAPI = CloudBarrageAPI()
    
    private lazy var throttler: AUIThrottler = AUIThrottler()
    private var msgId: Int = 0
    private var streamId: Int = 0
    private var msgArray = [Agora_Pb_Rctrl_RctrlMsg]()
    
    public func setup(apiConfig: CloudBarrageConfig) {
        self.apiConfig = apiConfig
        createDataStream()
    }
    
}

// MARK: public
extension CloudBarrageAPI {
    
    /// 获取游戏列表信息
    /// - Parameters:
    ///   - completion: <#completion description#>
    public func getGameList(completion: @escaping (NSError?, [CloudGameInfo]?) -> Void) {
        let interfaceName = "cloud-bullet-game/games"
        postRequest(interface: interfaceName) { err, result  in
            if let result = result?["list"] as? [[String: Any]] {
                let model: [CloudGameInfo]? = self.decodeModelArray(result)
                completion(err, model)
                return
            }
            completion(err, nil)
        }
    }
    
    
    /// 获取游戏详情信息
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - completion: <#completion description#>
    public func getGameInfo(gameId: String, completion: @escaping (NSError?, CloudGameDetailInfo?) -> Void) {
        let interfaceName = "cloud-bullet-game/games/game"
        postRequest(interface: interfaceName, params: ["gameId": gameId]) { err, result  in
            if let result = result {
                let model: CloudGameDetailInfo? = self.decodeModel(result)
                completion(err, model)
                return
            }
            completion(err, nil)
        }
    }
    
    /// 发送评论
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - commentConfig: 评论内容
    ///   - completion: <#completion description#>
    public func sendComment(gameId: String,
                            commentConfig: CloudGameSendCommentConfig,
                            completion: @escaping (NSError?) -> Void) {
        guard let params = encodeModel(commentConfig) else {
            completion(NSError(domain: "parse model fail", code: -1))
            return
        }
        let interfaceName = "cloud-bullet-game/gameid/\(gameId)/comment"
        postRequest(interface: interfaceName, params: params) { err, _  in
            completion(err)
        }
    }
    
    
    /// 发送点赞
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - likeConfig: 点赞内容
    ///   - completion: <#completion description#>
    public func sendLike(gameId: String,
                         likeConfig: CloudGameSendLikeConfig,
                         completion: @escaping (NSError?) -> Void) {
        guard let params = encodeModel(likeConfig) else {
            completion(NSError(domain: "parse model fail", code: -1))
            return
        }
        let interfaceName = "cloud-bullet-game/gameid/\(gameId)/like"
        postRequest(interface: interfaceName, params: params) { err, _  in
            completion(err)
        }
    }
    
    
    /// 开始游戏
    /// - Parameters:
    ///   - config: 开始内容
    ///   - completion: <#completion description#>
    public func startGame(config: CloudGameStartConfig,
                          completion: @escaping ((NSError?, String?)->Void)) {
        guard let params = encodeModel(config) else {
            completion(NSError(domain: "parse model fail", code: -1), nil)
            return
        }
        let interfaceName = "cloud-bullet-game/games/start"
        postRequest(interface: interfaceName, params: params) { err, result  in
            let taskId = result?["task_id"] as? String
            completion(err, taskId)
        }
    }
    
    
    /// 结束游戏
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - vid: agora app对应的vid
    ///   - roomId: 主播房间ID
    ///   - openId: 主播ID
    ///   - taskId:  启动游戏的任务ID，通过startGame返回
    ///   - completion: <#completion description#>
    public func endGame(gameId:String,
                        taskId: String,
                        completion: @escaping ((NSError?)->Void)) {
        let params = ["gameId": gameId, "taskId": taskId]
        let interfaceName = "cloud-bullet-game/games/stop"
        postRequest(interface: interfaceName, params: params) { err, result  in
            completion(err)
        }
    }
    
    /// 查询游戏状态
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - taskId: 启动游戏的任务ID，通过startGame返回
    ///   - completion: <#completion description#>
    public func getGameStatus(gameId:String,
                              taskId: String,
                              completion: @escaping ((_ status: UserGameStatus?)->Void)) {
        let params = ["gameId": gameId, "taskId": taskId]
        let interfaceName = "cloud-bullet-game/games/status"
        postRequest(interface: interfaceName, params: params) { err, result  in
            let status = UserGameStatus(rawValue: result?["status"] as? String ?? "") ?? .unknown
            completion(status)
        }
    }
    
    public func renewGameToken(gameId:String,
                               tokenConfig: CloudGameTokenConfig,
                               completion: @escaping ((NSError?)->Void)) {
        guard let params = encodeModel(tokenConfig) else {
            completion(NSError(domain: "parse model fail", code: -1))
            return
        }
        let interfaceName = "cloud-bullet-game/gameid/\(gameId)/renew-token"
        postRequest(interface: interfaceName, params: params) { err, result  in
            completion(err)
        }
    }
    
    
     func sendKeyboardEvent(type: Agora_Pb_Rctrl_KeyboardEventType, key:Character) {
        msgId += 1
        let currentDate = Date()
        var event = Agora_Pb_Rctrl_KeyboardEventMsg()
        event.vkey = UInt32(key.asciiValue ?? 0)
        event.keyboardEvent = UInt32(type.rawValue)
        event.state = type == .keyboardEventKeyDown ? 1 : 0xC0000001
        
        var msg = Agora_Pb_Rctrl_RctrlMsg()
        msg.type = .keyboardEventType
        msg.msgID = UInt32(msgId)
        msg.timestamp = UInt64(currentDate.timeIntervalSince1970)
        if let eventData = try? event.serializedData() {
            msg.payload = eventData
        }
        
        sendEventMessage(msg: msg)
    }
    
     func sendMouseEvent(type: Agora_Pb_Rctrl_MouseEventType,
                               point: CGPoint,
                               gameViewSize: CGSize) {
        msgId += 1
        let currentDate = Date()
        
        let x = (Int(point.x) << 16) / Int(gameViewSize.width)
        let y = (Int(point.y) << 16) / Int(gameViewSize.height)
        var event = Agora_Pb_Rctrl_MouseEventMsg()
        event.mouseEvent = UInt32(type.rawValue)
        event.x = Int32(x)
        event.y = Int32(y)
        event.extData = 1
        
        var msg = Agora_Pb_Rctrl_RctrlMsg()
        msg.type = .mouseEventType
        msg.msgID = UInt32(msgId)
        msg.timestamp = UInt64(currentDate.timeIntervalSince1970)
        if let eventData = try? event.serializedData() {
            msg.payload = eventData
        }
        
        sendEventMessage(msg: msg)
    }
}

extension CloudBarrageAPI {
    private func decodeModel<T: Codable>(_ dictionary: [String: Any]) -> T? {
        let decoder = JSONDecoder()
        do {
            let data = try JSONSerialization.data(withJSONObject: dictionary, options: .prettyPrinted)
            let model = try decoder.decode(T.self, from: data)
            return model
        } catch {
            joyWarn("decode model fail: \(error)")
        }
        return nil
    }
    
    private func decodeModelArray<T: Codable>(_ array: [[String: Any]]) -> [T]? {
        var modelArray: [T] = []
        for dic in array {
            if let model: T = decodeModel(dic) {
                modelArray.append(model)
            }
        }
        if modelArray.count > 0 {
            return modelArray
        }
        return nil
    }
    
    private func encodeModel(_ model: Codable) -> [String: Any]? {
        let encoder = JSONEncoder()
        encoder.keyEncodingStrategy = .useDefaultKeys
        var dictionary: [String: Any]?
        do {
            let data = try encoder.encode(model)
            dictionary = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
        } catch {
            joyWarn("encode model fail: \(error.localizedDescription)")
            return nil
        }
        
        return dictionary
    }
    private func sendEventMessage(msg: Agora_Pb_Rctrl_RctrlMsg) {
        msgArray.append(msg)
        
        throttler.triggerLastEvent(after: 0.03) {
            self.sendDataStream()
        }
    }
    
    private func createData() -> Data? {
        guard msgArray.count > 0 else { return nil }
        var msgs = Agora_Pb_Rctrl_RctrlMsges()
        msgs.msges = msgArray
        let data = try? msgs.serializedData()
        return data
    }
    
    private func createDataStream() {
        guard let engine = self.apiConfig?.engine else {
            joyError("createDataStream fail: engine == nil")
            return
        }
        let config = AgoraDataStreamConfig()
        config.ordered = true
        config.syncWithAudio = true
        let ret = engine.createDataStream(&streamId, config: config)
        if ret == 0 { return }
        joyWarn("createStream fail! ret:\(ret), streamId: \(streamId)")
    }
    
    private func sendDataStream() {
        guard let engine = self.apiConfig?.engine else {
            joyError("sendDataStream fail: engine == nil")
            return
        }
        guard let data = self.createData() else { return }
        self.msgArray.removeAll()
        let ret = engine.sendStreamMessage(streamId, data: data)
        if ret == 0 { return }
        joyWarn("sendStreamMessage fail! ret:\(ret), streamId: \(streamId)")
    }
}

public let kAppProjectName = "appProject"
public let kAppProjectValue = "agora_ent_demo"
public let kAppOS = "appOs"
public let kAppOSValue = "iOS"
public let kAppVersion = "versionName"
extension CloudBarrageAPI {
    private func getRequest(interface: String,
                            params:[String: Any]? = nil,
                            completion: @escaping (NSError?, [String: Any]?)->()) {
        httpRequest(interface: interface, httpMethod: "GET", params: params, completion: completion)
    }
    
    private func postRequest(interface: String,
                             params:[String: Any]? = nil,
                             completion: @escaping (NSError?, [String: Any]?)->()) {
        httpRequest(interface: interface, httpMethod: "POST", params: params, completion: completion)
    }
    
    private func httpRequest(interface: String,
                             httpMethod:String,
                             params:[String: Any]? = nil,
                             completion: @escaping (NSError?, [String: Any]?)->()) {
        guard let apiConfig = apiConfig,
              let host = apiConfig.host,
              let appId = apiConfig.appId,
              let rtmToken = apiConfig.rtmToken else {
            completion(NSError(domain: "api config == nil", code: -1), nil)
            return
        }
        var params: [String: Any] = params ?? [:]
        params["appId"] = appId
        if let basicAuth = apiConfig.basicAuth {
            params["basicAuth"] = basicAuth
        }
        params["src"] = "iOS"
        params["traceId"] = NSString.withUUID().md5() ?? ""
        
        var url = URL(string: "\(host)\(interface)")!
        if httpMethod == "GET" {
            url = appendQueryParams(to: url, queryParams: params) ?? url
//            joyPrint(" GET url = \(url)")
        }
       
        var request = getHeaderRequest(url: url)
        request.httpMethod = httpMethod
        if httpMethod == "POST" {
            let jsonBody = try? JSONSerialization.data(withJSONObject: params)
            request.httpBody = jsonBody
//            joyPrint(" POST url = \(url), params = \(params.debugDescription)")
        }
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                joyError("Error: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            joyPrint(" httpRequest request:\n \(request.cURL(pretty: true)) \nresp:\n \(NSString(data: data, encoding: NSUTF8StringEncoding)) ")
            if let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                let result = dic["data"] as? [String: Any]
                let code = dic["code"] as? Int ?? 0
                let msg = dic["msg"] as? String ?? ""
                let error = code == 0 ? nil : NSError(domain: msg, code: code)
//                joyPrint("result = \(String(describing: result)), code = \(code)")
                DispatchQueue.main.async {
                    completion(error, result)
                }
            }
        }
        task.resume()
    }
    
    private func getHeaderRequest(url: URL) -> URLRequest {
        var request = URLRequest(url: url)
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("fkUjxadPMmvYF3F3BI4uvmjo-gzGzoHsz", forHTTPHeaderField: "X-LC-Id")
        request.addValue("QAvFS62IOR28GfSFQO5ze45s", forHTTPHeaderField: "X-LC-Key")
        request.addValue("qmdj8pdidnmyzp0c7yqil91oc", forHTTPHeaderField: "X-LC-Session")
        request.addValue(kAppProjectValue, forHTTPHeaderField: kAppProjectName)
        request.addValue(kAppOSValue, forHTTPHeaderField: kAppOS)
        request.addValue(UIApplication.shared.appVersion ?? "", forHTTPHeaderField: kAppVersion)
        if let token = self.apiConfig?.rtmToken {
            request.addValue(token, forHTTPHeaderField: "Authorization")
        }
        return request
    }
    
    private func appendQueryParams(to url: URL, queryParams: [String: Any]) -> URL? {
        var urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: true)
        var urlParams = [String]()
        
        queryParams.forEach { (key, value) in
            urlParams.append("\(key)=\(value)")
        }
        
        let paramsString = urlParams.joined(separator: "&")
        
        if var query = urlComponents?.query {
            query.append("&" + paramsString)
            urlComponents?.query = query
        } else {
            urlComponents?.query = paramsString
        }
        return urlComponents?.url
    }
}
