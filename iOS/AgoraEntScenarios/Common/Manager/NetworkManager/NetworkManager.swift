//
//  NetworkManager.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/19.
//
import UIKit
import YYCategories

@objc
class NetworkManager:NSObject {
    @objc public enum TokenGeneratorType: Int {
        case token006 = 0
        case token007 = 1
    }
    
    @objc public enum AgoraTokenType: Int {
        case rtc = 1
        case rtm = 2
        case chat = 3
    }

    enum HTTPMethods: String {
        case GET
        case POST
    }
    
    @objc enum SceneType: Int {
        case show = 0
        case voice = 1
        case ktv = 2

        func desc() ->String {
            switch self {
            case .show:
                return "show"
            case .voice:
                return "voice_chat"
            case .ktv:
                return "ktv"
            default:
                break
            }

            return "unknown"
        }
    }

    var gameToken: String = ""

    typealias SuccessClosure = ([String: Any]) -> Void
    typealias FailClosure = (String) -> Void

    private var sessionConfig: URLSessionConfiguration = {
        let config = URLSessionConfiguration.default
        config.httpAdditionalHeaders = ["Content-Type": "application/json",
                                        "X-LC-Id": "fkUjxadPMmvYF3F3BI4uvmjo-gzGzoHsz",
                                        "X-LC-Key": "QAvFS62IOR28GfSFQO5ze45s",
                                        "X-LC-Session": "qmdj8pdidnmyzp0c7yqil91oc"]
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        return config
    }()

    @objc static let shared = NetworkManager()
    private let baseUrl = "https://agoraktv.xyz/1.1/functions/"
    private let baseServerUrl: String = "https://toolbox.bj2.agoralab.co/v1/"
    
    private func basicAuth(key: String, password: String) -> String {
        let loginString = String(format: "%@:%@", key, password)
        guard let loginData = loginString.data(using: String.Encoding.utf8) else {
            return ""
        }
        let base64LoginString = loginData.base64EncodedString()
        return base64LoginString
    }
    
    /// get tokens
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - uid: <#uid description#>
    ///   - tokenGeneratorType: token types
    ///   - tokenTypes: [token type :  token string]
    func generateTokens(channelName: String,
                        uid: String,
                        tokenGeneratorType: TokenGeneratorType,
                        tokenTypes: [AgoraTokenType],
                        success: @escaping ([Int: String]) -> Void)
    {
        let group = DispatchGroup()
        var tokenMap: [Int: String] = [Int:String]()
        
        tokenTypes.forEach { type in
            group.enter()
            generateToken(channelName: channelName,
                          uid: uid,
                          tokenType: tokenGeneratorType,
                          type: type) { token in
                if let token = token, token.count > 0 {
                    tokenMap[type.rawValue] = token
                }
                group.leave()
            }
        }

        group.notify(queue: DispatchQueue.main) {
            success(tokenMap)
        }
    }

    @objc
    func generateToken(channelName: String,
                       uid: String,
                       tokenType: TokenGeneratorType,
                       type: AgoraTokenType,
                       success: @escaping (String?) -> Void)
    {
        let params = ["appCertificate": KeyCenter.Certificate ?? "",
                      "appId": KeyCenter.AppId,
                      "channelName": channelName,
                      "expire": 1500,
                      "src": "iOS",
                      "ts": "".timeStamp,
                      "type": type.rawValue,
                      "uid": uid] as [String: Any]
//        ToastView.showWait(text: "loading...", view: nil)
        let url = tokenType == .token006 ?
        "\(baseServerUrl)token006/generate"
        : "\(baseServerUrl)token/generate"
        NetworkManager.shared.postRequest(urlString: url,
                                          params: params,
                                          success: { response in
            let data = response["data"] as? [String: String]
            let token = data?["token"]
            print(response)
            success(token)
//            ToastView.hidden()
        }, failure: { error in
            print(error)
            success(nil)
//            ToastView.hidden()
        })
    }
    
    /// generator easemob im token & uid
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - nickName: <#nickName description#>
    ///   - password: <#password description#>
    ///   - uid: <#uid description#>
    ///   - type: 0: 同时处理用户注册/返回用户token和创建聊天室 1: 只处理用户注册/返回用户token 2: 只处理创建聊天室

    ///   - success: success description {roomid, uid}
    func generateIMConfig(type: Int,
                          channelName: String,
                          nickName: String,
                          chatId: String?,
                          imUid: String?,
                          password: String,
                          uid: String,
                          sceneType: SceneType,
                          success: @escaping (String?, String?, String?) -> Void) {
        var chatParams = [
            "name": channelName,
            "description": "test",
            "owner": uid,
        ]
        
        if let chatId = chatId {
            chatParams.updateValue(chatId, forKey: "id")
        }
        
        let userParams = [
            "username": uid,
            "password": password,
            "nickname": nickName,
        ]
        
        let imConfig = [
            "appKey":KeyCenter.IMAppKey,
            "clientId":KeyCenter.IMClientId,
            "clientSecret":KeyCenter.IMClientSecret,
        ]
        
        let payload: String = getPlayloadWithSceneType(.voice) ?? ""
        let traceId = UUID().uuidString.md5Encrypt
        let params = ["appId": KeyCenter.AppId,
                      "chat": chatParams,
                      "src": "iOS",
                      "im": imConfig,
                      "payload": payload,
                      "traceId": NSString.withUUID().md5() as Any,
                      "user": userParams,
                      "type":type] as [String: Any]
 
        NetworkManager.shared.postRequest(urlString: "\(baseServerUrl)webdemo/im/chat/create",
                                          params: params,
                                          success: { response in
            let data = response["data"] as? [String: String]
            let uid = data?["userName"]
            let chatId = data?["chatId"]
            let token = data?["chatToken"]
            print(response)
            success(uid, chatId, token)
//            ToastView.hidden()
        }, failure: { error in
            print(error)
            success(nil, nil, nil)
//            ToastView.hidden()
        })
    }
    
    @objc func voiceIdentify(channelName: String,
                             channelType: Int,
                             sceneType: SceneType,
                             success: @escaping (String?) -> Void) {
        let payload: String = getPlayloadWithSceneType(sceneType) ?? ""
        let params = ["appId": KeyCenter.AppId,
                      "channelName": channelName,
                      "channelType": channelType,
                      "src": "iOS",
                      "traceId": UUID().uuidString.md5Encrypt,
                      "payload": payload] as [String: Any]
                      
        NetworkManager.shared.postRequest(urlString: "\(baseServerUrl)moderation/audio",
                                          params: params,
                                          success: { response in
            let code = response["code"] as? Int
            let msg = response["msg"] as? String
            success(code == 0 ? nil : msg)
        }, failure: { error in
            print(error)
            success(error.description)
        })
    }
    
    func getPlayloadWithSceneType(_ type: SceneType) -> String? {
        let userInfo: [String: Any] = [
            "id": VLUserCenter.user.id,     //用户id
            "sceneName": type.desc()
        ]
                 
        guard let jsonData = try? JSONSerialization.data(withJSONObject: userInfo, options: .prettyPrinted) else {
            print("setupContentInspectConfig fail")
            return nil
        }
        let payload: String? = String(data: jsonData, encoding: .utf8) ?? nil
        return payload
    }
    
    func startCloudPlayer(channelName: String,
                          uid: String,
                          robotUid: UInt,
                          streamUrl: String,
                          success: @escaping (String?) -> Void) {
        let params: [String: Any] = ["appId": KeyCenter.AppId,
                                     "appCert": KeyCenter.Certificate ?? "",
                                     "basicAuth":basicAuth(key: KeyCenter.CloudPlayerKey ?? "", password: KeyCenter.CloudPlayerSecret ?? ""),
                                        "channelName": channelName,
                                        "uid": uid,
                                        "robotUid": robotUid,
                                        "region": "cn",
                                        "streamUrl": streamUrl,
                                        "src": "iOS",
                                        "traceId": NSString.withUUID().md5() ?? ""]
                      
        NetworkManager.shared.postRequest(urlString: "\(baseServerUrl)cloud-player/start",
                                          params: params,
                                          success: { response in
            let code = response["code"] as? Int
            let msg = response["msg"] as? String
            success(code == 0 ? nil : msg)
        }, failure: { error in
            print(error)
            success(error.description)
        })
    }
    
    func cloudPlayerHeartbeat(channelName: String,
                              uid: String,
                              success: @escaping (String?) -> Void) {
        let params: [String: String] = ["appId": KeyCenter.AppId,
                                        "channelName": channelName,
                                        "uid": uid,
                                        "src": "iOS",
                                        "traceId": NSString.withUUID().md5() ?? ""]
                      
        NetworkManager.shared.postRequest(urlString: "\(baseServerUrl)heartbeat",
                                          params: params,
                                          success: { response in
            let code = response["code"] as? Int
            let msg = response["msg"] as? String
            success(code == 0 ? nil : msg)
        }, failure: { error in
            print(error)
            success(error.description)
        })
    }

    func getRequest(urlString: String, success: SuccessClosure?, failure: FailClosure?) {
        DispatchQueue.global().async {
            self.request(urlString: urlString, params: nil, method: .GET, success: success, failure: failure)
        }
    }

    func postRequest(urlString: String, params: [String: Any]?, success: SuccessClosure?, failure: FailClosure?) {
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
        let src: String = "agora_ent_demo"
        let ts: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
        let params = ["pts": [["m": "event",
                              "ls": [
                                "name": "entryScene",
                                "project": sceneName,
                                "version": UIApplication.shared.appVersion ?? "",
                                "platform": "iOS",
                                "model": UIDevice.current.machineModel ?? ""
                              ],
                              "vs": ["count": 1]
                             ]],
                      "src": src,
                      "ts": ts,
                      "sign": "src=\(src)&ts=\(ts)".md5Encrypt] as [String: Any]
//        ToastView.showWait(text: "loading...", view: nil)
        let url = "https://report-ad.agoralab.co/v1/report"
        NetworkManager.shared.postRequest(urlString: url,
                                          params: params,
                                          success: { response in
//            let data = response["data"] as? [String: String]
            print(response)
//            success(token)
//            ToastView.hidden()
        }, failure: { error in
//            print(error)
//            success(nil)
//            ToastView.hidden()
        })
    }
}
