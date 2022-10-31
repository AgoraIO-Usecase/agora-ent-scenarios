//
//  VoiceRoomRequest.swift
//  VoiceRoomRequest
//
//  Created by 朱继超 on 2022/8/29.
//

import UIKit

public struct VoiceRoomRequestHTTPMethod: RawRepresentable, Equatable, Hashable {
    /// `CONNECT` method.
    public static let connect = VoiceRoomRequestHTTPMethod(rawValue: "CONNECT")
    /// `DELETE` method.
    public static let delete = VoiceRoomRequestHTTPMethod(rawValue: "DELETE")
    /// `GET` method.
    public static let get = VoiceRoomRequestHTTPMethod(rawValue: "GET")
    /// `HEAD` method.
    public static let head = VoiceRoomRequestHTTPMethod(rawValue: "HEAD")
    /// `OPTIONS` method.
    public static let options = VoiceRoomRequestHTTPMethod(rawValue: "OPTIONS")
    /// `PATCH` method.
    public static let patch = VoiceRoomRequestHTTPMethod(rawValue: "PATCH")
    /// `POST` method.
    public static let post = VoiceRoomRequestHTTPMethod(rawValue: "POST")
    /// `PUT` method.
    public static let put = VoiceRoomRequestHTTPMethod(rawValue: "PUT")
    /// `TRACE` method.
    public static let trace = VoiceRoomRequestHTTPMethod(rawValue: "TRACE")
    
    public let rawValue: String
    
    public init(rawValue: String) {
        self.rawValue = rawValue
    }
}

@objcMembers public class VoiceRoomRequest: NSObject, URLSessionDelegate {
    
    @objc public static var shared = VoiceRoomRequest()
    
   // var host: String =  "https://gateway-fulldemo-staging.agoralab.co"
    var host = "http://ad-fulldemo-gateway-chat-staging.sh2.agoralab.co"
    private lazy var config: URLSessionConfiguration = {
        //MARK: - session config
        let config = URLSessionConfiguration.default
        config.httpAdditionalHeaders = ["Content-Type":"application/json"]
        config.timeoutIntervalForRequest = 10
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        return config
    }()
    
    private var session: URLSession?
    
    override init() {
        super.init()
        self.session = URLSession(configuration: self.config, delegate: self, delegateQueue: .main)
    }
    
    public func constructRequest(method: VoiceRoomRequestHTTPMethod,
                                 uri: String,
                                 params: Dictionary<String,Any>,
                                 headers:[String : String],
                                 callBack:@escaping ((Data?,HTTPURLResponse?,Error?) -> Void)) -> URLSessionTask? {
        guard let url = URL(string: self.host+uri) else { return nil }
        //MARK: - request
        var urlRequest = URLRequest(url: url)
        if method == .put || method == .post {
            do {
                urlRequest.httpBody = try JSONSerialization.data(withJSONObject: params, options: [])
            } catch {
                assert(false, "\(error.localizedDescription)")
            }
        }
        urlRequest.allHTTPHeaderFields = headers
        urlRequest.httpMethod = method.rawValue
        let task = self.session?.dataTask(with: urlRequest){
            if $2 == nil {
                callBack($0,($1 as? HTTPURLResponse),$2)
            } else {
                callBack(nil,nil,$2)
            }
        }
        task?.resume()
        return task
    }
    
    @objc public func sendRequest(method: String,
                                  uri: String,
                                  params: Dictionary<String,Any>,
                                  headers:[String : String],
                                  callBack:@escaping ((Data?,HTTPURLResponse?,Error?) -> Void)) -> URLSessionTask? {
        guard let url = URL(string: self.host+uri) else { return nil }
        //MARK: - request
        var urlRequest = URLRequest(url: url)
        do {
            urlRequest.httpBody = try JSONSerialization.data(withJSONObject: params, options: [])
        } catch {
            assert(false, "\(error.localizedDescription)")
        }
        urlRequest.allHTTPHeaderFields = headers
        urlRequest.httpMethod = method
        let task = self.session?.dataTask(with: urlRequest){
            if $2 == nil {
                callBack($0,($1 as? HTTPURLResponse),$2)
            } else {
                callBack(nil,nil,$2)
            }
        }
        task?.resume()
        return task
    }
    
    @objc public func configHost(url: String) {
        self.host = url
    }
    
    //MARK: - URLSessionDelegate
    public func urlSession(_ session: URLSession, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
        if challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust {
            let credential = URLCredential(trust: challenge.protectionSpace.serverTrust!)
            completionHandler(.useCredential,credential)
        }
    }
    
}

