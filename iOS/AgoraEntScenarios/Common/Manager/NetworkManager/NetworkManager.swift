//
//  NetworkManager.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/19.
//

import UIKit

class NetworkManager {
    enum AgoraTokenType: Int {
        case rtc = 1
        case rtm = 2
    }

    enum HTTPMethods: String {
        case GET
        case POST
    }

    var gameToken: String = ""

    typealias SuccessClosure = ([String: Any]) -> Void
    typealias FailClosure = (String) -> Void

    private lazy var sessionConfig: URLSessionConfiguration = {
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

    static let shared = NetworkManager()
    private init() {}
    private let baseUrl = "https://agoraktv.xyz/1.1/functions/"

    func generateRTCToken(channelName: String,
                          uid: String,
                          success: @escaping (String?) -> Void)
    {
        generateToken(channelName: channelName,
                      uid: uid,
                      type: .rtc,
                      success: success)
    }

    func generateRTMToken(channelName: String,
                          uid: String,
                          success: @escaping (String?) -> Void)
    {
        generateToken(channelName: channelName,
                      uid: uid,
                      type: .rtm,
                      success: success)
    }

    /// get rtc token & rtmtoken
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - uid: <#uid description#>
    ///   - success: {"rtc token", "rtm token"}
    func generateAllToken(channelName: String,
                          uid: String,
                          success: @escaping (String?, String?) -> Void)
    {
        let group = DispatchGroup()
        var token: (String?, String?) = (nil, nil)
        group.enter()
        generateRTMToken(channelName: channelName,
                         uid: uid) { rtmToken in
            token.1 = rtmToken
            group.leave()
        }
        group.enter()
        generateRTCToken(channelName: channelName,
                         uid: uid) { rtcToken in
            token.0 = rtcToken
            group.leave()
        }

        group.notify(queue: DispatchQueue.main) {
            success(token.0, token.1)
        }
    }

    func generateToken(channelName: String,
                       uid: String,
                       type: AgoraTokenType,
                       success: @escaping (String?) -> Void)
    {
        if KeyCenter.Certificate == nil || KeyCenter.Certificate?.isEmpty == true {
            success(nil)
            return
        }
        let params = ["appCertificate": KeyCenter.Certificate ?? "",
                      "appId": KeyCenter.AppId,
                      "channelName": channelName,
                      "expire": 900,
                      "src": "iOS",
                      "ts": "".timeStamp,
                      "type": type.rawValue,
                      "uid": uid] as [String: Any]
        ToastView.showWait(text: "loading...", view: nil)
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.agoralab.co/v1/token006/generate", params: params, success: { response in
            let data = response["data"] as? [String: String]
            let token = data?["token"]
            KeyCenter.Token = token
            print(response)
            success(token)
            ToastView.hidden()
        }, failure: { error in
            print(error)
            success(nil)
            ToastView.hidden()
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
            request.httpBody = try? JSONSerialization.data(withJSONObject: params ?? [], options: .sortedKeys) // convertParams(params: params).data(using: .utf8)
        }
        let curl = request.cURL(pretty: true)
        debugPrint("curl == \(curl)")
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
