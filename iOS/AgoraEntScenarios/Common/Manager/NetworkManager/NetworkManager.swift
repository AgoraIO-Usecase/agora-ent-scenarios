//
//  NetworkManager.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/19.
//
import UIKit
import YYCategories

public let kAppProjectName = "appProject"
public let kAppProjectValue = "agora_ent_demo"
public let kAppOS = "appOs"
public let kAppOSValue = "iOS"
public let kAppVersion = "versionName"

@objc
public class NetworkManager:NSObject {
    @objc public enum AgoraTokenType: Int {
        case rtc = 1
        case rtm = 2
        case chat = 3
    }

    enum HTTPMethods: String {
        case GET
        case POST
    }

    var gameToken: String = ""

    public typealias SuccessClosure = ([String: Any]) -> Void
    public typealias FailClosure = (String) -> Void

    private var sessionConfig: URLSessionConfiguration = {
        let config = URLSessionConfiguration.default
        config.httpAdditionalHeaders = [
            "Content-Type": "application/json",
            "X-LC-Id": "fkUjxadPMmvYF3F3BI4uvmjo-gzGzoHsz",
            "X-LC-Key": "QAvFS62IOR28GfSFQO5ze45s",
            "X-LC-Session": "qmdj8pdidnmyzp0c7yqil91oc",
            kAppProjectName: kAppProjectValue,
            kAppOS: kAppOSValue,
            kAppVersion: UIApplication.shared.appVersion ?? ""
        ]
        if !VLUserCenter.user.token.isEmpty {
            config.httpAdditionalHeaders?["Authorization"] = VLUserCenter.user.token
        }
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        return config
    }()

    @objc public static let shared = NetworkManager()
    private let baseUrl = "https://agoraktv.xyz/1.1/functions/"
    private var baseServerUrl: String {
        return AppContext.shared.baseServerUrl + "toolbox/"
    }
    
    /// get tokens
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - uid: <#uid description#>
    ///   - tokenGeneratorType: token types
    ///   - tokenTypes: [token type :  token string]
    public func generateToken(channelName: String,
                              appId: String? = nil,
                              uid: String,
                              tokenTypes: [AgoraTokenType],
                              expire: UInt = 24 * 60 * 60,
                              success: @escaping (String?) -> Void) {
        generateToken(channelName: channelName,
                      appId: appId,
                      uid: uid,
                      types: tokenTypes.map({NSNumber(value: $0.rawValue)}),
                      expire: expire, success: success)
    }

    @objc
    public func generateToken(channelName: String,
                              appId: String? = nil,
                              uid: String,
                              types: [NSNumber],
                              expire: UInt = 24 * 60 * 60,
                              success: @escaping (String?) -> Void) {
        let model: NMGenerateTokennNetworkModel = NMGenerate007TokennNetworkModel()
        model.appId = appId ?? AppContext.shared.appId
        model.expire = NSNumber(value: expire)
        model.channelName = channelName
        model.types = types
        model.uid = uid
        model.request { error, token in
            success(token as? String)
        }
    }
    
    /// generator easemob im token & uid
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - nickName: <#nickName description#>
    ///   - password: <#password description#>
    ///   - uid: <#uid description#>
    ///   - type: 0: 同时处理用户注册/返回用户token和创建聊天室 1: 只处理用户注册/返回用户token 2: 只处理创建聊天室

    ///   - success: success description {roomid, uid}
    public func generateIMConfig(type: Int,
                                 channelName: String,
                                 nickName: String,
                                 chatId: String?,
                                 imUid: String?,
                                 password: String,
                                 uid: String,
                                 success: @escaping (String?, String?, String?) -> Void) {
        let chatParamsModel = NMGenerateIMConfigNetworkModelChatParams()
        chatParamsModel.name = channelName
        chatParamsModel.desc = "test"
        chatParamsModel.owner = uid
        if let chatId = chatId {
            chatParamsModel.chatId = chatId
        }
        
        let userParamsModel = NMGenerateIMConfigNetworkModelUserParmas()
        userParamsModel.username = uid
        userParamsModel.password = password
        userParamsModel.nickname = nickName
      
        let imConfigModel = NMGenerateIMConfigNetworkModelIMParmas()
        
        let payload: String = getPlayloadWithSceneType("voice_chat") ?? ""
        
        let networkModel = NMGenerateIMConfigNetworkModel()
        networkModel.chat = chatParamsModel
        networkModel.im = imConfigModel
        networkModel.payload = payload
        networkModel.user = userParamsModel
        networkModel.type = NSNumber(value: type)
        
        networkModel.request { error, data in
            let data = data as? [String: String]
            let uid = data?["userName"]
            let chatId = data?["chatId"]
            let token = data?["chatToken"]
            success(uid, chatId, token)
        }
        
    }
    
    @objc public func voiceIdentify(channelName: String,
                                    channelType: Int,
                                    sceneType: String, //SceneType
                                    success: @escaping (String?) -> Void) {
        let payload: String = getPlayloadWithSceneType(sceneType) ?? ""
        let model = NMVoiceIdentifyNetworkModel()
        model.channelName = channelName
        model.channelType = NSNumber(value: channelType)
        model.payload = payload
        model.request { error, data in
            let data = data as? [String: Any]
            let code = data?["code"] as? Int
            let msg = data?["msg"] as? String
            success(code == 0 ? nil : msg)
        }
    }
    
    func getPlayloadWithSceneType(_ type: String) -> String? {
        let userInfo: [String: Any] = [
            "id": VLUserCenter.user.id,     //用户id
            "sceneName": type,
            "userNo": VLUserCenter.user.userNo
        ]
                 
        guard let jsonData = try? JSONSerialization.data(withJSONObject: userInfo, options: .prettyPrinted) else {
            print("setupContentInspectConfig fail")
            return nil
        }
        let payload: String? = String(data: jsonData, encoding: .utf8) ?? nil
        return payload
    }
    
    public func startCloudPlayer(channelName: String,
                                 uid: String,
                                 robotUid: UInt,
                                 streamUrl: String,
                                 success: @escaping (String?) -> Void) {
        let model = NMStartCloudPlayerNetworkModel()
        model.channelName = channelName
        model.uid = uid
        model.robotUid = NSNumber(value: robotUid)
        model.streamUrl = streamUrl
        
        model.request { error, data in
            let data = data as? [String: Any]
            let code = data?["code"] as? Int
            let msg = data?["msg"] as? String
            success(code == 0 ? nil : msg)
        }
    }
    
    public func cloudPlayerHeartbeat(channelName: String,
                              uid: String,
                              success: @escaping (String?) -> Void) {
        let model = NMCloudPlayerHeartbeatNetworkModel()
        model.channelName = channelName
        model.uid = uid
        model.request { error, data in
            let data = data as? [String: Any]
            let code = data?["code"] as? Int
            let msg = data?["msg"] as? String
            success(code == 0 ? nil : msg)
        }
    }

    func getRequest(urlString: String, success: SuccessClosure?, failure: FailClosure?) {
        DispatchQueue.global().async {
            self.request(urlString: urlString, params: nil, method: .GET, success: success, failure: failure)
        }
    }

    public func postRequest(urlString: String, params: [String: Any]?, success: SuccessClosure?, failure: FailClosure?) {
        DispatchQueue.global().async {
            self.request(urlString: urlString, params: params, method: .POST, success: success, failure: failure)
        }
    }

    /// 生成签名
    func generateSignature(params: [String: Any]?, token: String) -> String {
        guard let params = params else { return "" }
        var value = Array(params.keys)
            .sorted()
            .compactMap({ String(format: "%@", "\(params[$0] ?? "")") })
            .joined(separator: "")
        value += token
        return value.md5Encrypt
    }

    private func request(urlString: String,
                         params: [String: Any]?,
                         method: HTTPMethods,
                         success: SuccessClosure?,
                         failure: FailClosure?)
    {
        let session = URLSession(configuration: sessionConfig)
        guard let request = getRequest(urlString: urlString,
                                       params: params,
                                       method: method,
                                       success: success,
                                       failure: failure) else { return }
        session.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                self.checkResponse(response: response, data: data, success: success, failure: failure)
            }
        }.resume()
    }

    private func getRequest(urlString: String,
                            params: [String: Any]?,
                            method: HTTPMethods,
                            success: SuccessClosure?,
                            failure: FailClosure?) -> URLRequest?
    {
        let string = urlString.hasPrefix("http") ? urlString : baseUrl.appending(urlString)
        guard let url = URL(string: string) else {
            return nil
        }
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        if method == .POST {
            request.httpBody = try? JSONSerialization.data(withJSONObject: params ?? [],
                                                           options: .sortedKeys) // convertParams(params: params).data(using: .utf8)
        }
        let curl = request.cURL(pretty: true)
        #if DEBUG
        debugPrint("curl == \(curl)")
        #endif
        return request
    }

    private func convertParams(params: [String: Any]?) -> String {
        guard let params = params else { return "" }
        let value = params.map({ String(format: "%@=%@", $0.key, "\($0.value)") }).joined(separator: "&")
        return value
    }

    private func checkResponse(response: URLResponse?, data: Data?, success: SuccessClosure?, failure: FailClosure?) {
        if let httpResponse = response as? HTTPURLResponse {
            switch httpResponse.statusCode {
            case 200...201:
                if let resultData = data {
                    let result = String(data: resultData, encoding: .utf8)
                    print(result ?? "")
                    success?(JSONObject.toDictionary(jsonString: result ?? ""))
                } else {
                    failure?("Error in the request status code \(httpResponse.statusCode), response: \(String(describing: response))")
                }
            default:
                failure?("Error in the request status code \(httpResponse.statusCode), response: \(String(describing: response))")
            }
        } else {
            failure?("Error in the request status code \(400), response: \(String(describing: response))")
        }
    }
}

public extension URLRequest {
    func cURL(pretty: Bool = false) -> String {
        let newLine = pretty ? "\\\n" : ""
        let method = (pretty ? "--request " : "-X ") + "\(httpMethod ?? "GET") \(newLine)"
        let url: String = (pretty ? "--url " : "") + "\'\(url?.absoluteString ?? "")\' \(newLine)"

        var cURL = "curl "
        var header = ""
        var data = ""

        if let httpHeaders = allHTTPHeaderFields, httpHeaders.keys.count > 0 {
            for (key, value) in httpHeaders {
                header += (pretty ? "--header " : "-H ") + "\'\(key): \(value)\' \(newLine)"
            }
        }

        if let bodyData = httpBody, let bodyString = String(data: bodyData, encoding: .utf8), !bodyString.isEmpty {
            data = "--data '\(bodyString)'"
        }

        cURL += method + url + header + data

        return cURL
    }
}

//event report
extension NetworkManager {
    @objc public func reportSceneClick(sceneName: String) {
        let model = NMReportSceneClickNetworkModel()
        model.setProject(sceneName)
        model.request { error, data in

        }
    }
    
    @objc
    public func reportDeviceInfo(sceneName: String) {
        let model = NMReportDeviceInfoNetworkModel(sceneId: sceneName, userNo: VLUserCenter.user.userNo, appId: AppContext.shared.appId)
        model.request { error, data in

        }
    }
    
    @objc
    public func reportUserBehavior(sceneName: String) {
        let model = NMReportUserBehaviorNetworkModel(sceneId: sceneName, userNo: VLUserCenter.user.userNo, appId: AppContext.shared.appId)
        model.request { error, data in

        }
    }
}

// sbg and sr
extension NetworkManager {
    //发起抢唱
    @objc public func startSongGrab(_ appid: String, sceneId: String, roomId: String, headUrl: String, userId: String, userName: String, songCode: String, success: @escaping (Bool) -> Void) {
        let params = [
            "appId": appid,
            "sceneId": sceneId,
            "roomId": roomId,
            
            "userId": userId,
            "userName": userName,
            "songCode": songCode,
            "src": "postman",
            "headUrl":headUrl
        ]
        
        let baseUrl = self.baseServerUrl
        
        NetworkTools().request("\(baseUrl)/v1/ktv/song/grab", method: .post, parameters: params) {[weak self] result in
            switch result{
                case .success(let data):
                    let obj = self?.data2Dict(with: data)
                    guard let code: Int = obj?["code"] as? Int else {return}
                    success(code == 0 ? true : false)
                case .failure(let error):
                    print(error)
                    success(false)
            }
        }
        
    }
    
    //抢唱结果查询
    @objc public func songGrabQuery(_ appid: String, sceneId: String, roomId: String, songCode: String, src: String, success: @escaping (String?, String?,Bool) -> Void) {
        let params = [
            "appId": appid,
            "sceneId": sceneId,
            "roomId": roomId,
            "songCode": songCode,
            "src": "postman"
        ]
        
        let baseUrl = self.baseServerUrl
        
        NetworkTools().request("\(baseUrl)/v1/ktv/song/grab/query".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "", method: .get, parameters: params) { result in
            switch result {
            case .success(let data):
                do {
                    if let obj = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                       let code = obj["code"] as? Int{
                        if code == 0 {
                            let userData = obj["data"] as? [String: Any]
                            let userId = userData?["userId"]
                            let userName = userData?["userName"]
                            success(userId as? String, userName as? String, true)
                        } else if code == 961 {
                            success(nil, nil, false)
                        }
                    } else {
                       // success(nil,nil, false)
                    }
                } catch {
//                    print(error)
//                    success(nil, nil, false)
                }
            case .failure(let error):
                print(error)
             //   success(nil, nil, false)
            }
        }
    }
    
    private func data2Dict(with data: Data) -> [String: Any]? {
        do {
            if let jsonObject = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [String: Any] {
                return jsonObject
            } else {
                // data 转换为字典失败，处理错误
                return nil
            }
        } catch {
            // 发生异常，处理错误
            return nil
        }
    }
}
