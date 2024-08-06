//  RttApiManager.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/25.
//

import Foundation
import AgoraCommon

class RttApiManager {
    static let shared = RttApiManager()
    
    private let domain = "https://api.agora.io/"
    
    private let TAG = "RttApiManager"
    private var auth = ""
    private var tokenName = ""
    private var taskId = ""
    
    private lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 30
        return URLSession(configuration: configuration)
    }()
    
    func setBasicAuth(token: String) {
        self.auth = "agora token=" + token
    }
    
    func fetchCloudToken(completion: @escaping ((String?)->Void)) {
        
        do {
            let timeInterval: TimeInterval = Date().timeIntervalSince1970
            let millisecond = CLongLong(round(timeInterval*1000))
            let acquireOjb = try JSONSerialization.data(withJSONObject: [
                "instanceId": "\(Int(millisecond))"
//                "testIp" : "218.205.37.49",
//                "testPort": 4447
            ])

            let url = getTokenUrl(domain: domain, appId: AppContext.shared.appId)
            guard let requestUrl = URL(string: url) else {
                DispatchQueue.main.async {
                    completion(nil)
                }
                return
            }
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(auth, forHTTPHeaderField: "Authorization")
            request.httpBody = acquireOjb
                        
            let task = session.dataTask(with: request) { (data, response, error) in
                if let error = error {
                    pure1v1Print("RttApiManager getToken error: \(error.localizedDescription)")
                    DispatchQueue.main.async {
                        completion(nil)
                    }
                } else if let data = data {
                    do {
                        guard let responseDict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any], let tokenName = responseDict["tokenName"] as? String else {
                            pure1v1Print("RttApiManager getToken error: \(data)")
                            return
                        }
                        DispatchQueue.main.async {
                            completion(tokenName)
                        }
                        pure1v1Print("RttApiManager getToken success")
                    } catch {
                        pure1v1Print("RttApiManager getToken error: \(error.localizedDescription)")
                        DispatchQueue.main.async {
                            completion(nil)
                        }
                    }
                }
            }
            
            task.resume()
            
        } catch {
            pure1v1Print("RttApiManager getToken error: \(error.localizedDescription)")
            DispatchQueue.main.async {
                completion(nil)
            }
        }
    }
    
    func fetchStartRtt(
        languages: [String], // 请替换为实际语言列表
        sourceLanguage: String, // 请替换为实际源语言
        targetLanguages: [String],  // 请替换为实际目标语言列表
        channelName: String,
        subBotUid: String,
        subBotToken: String,
        pubBotUid: String,
        pubBotToken: String,
        completion: @escaping ((Bool)->Void)) {
            
        self.fetchCloudToken { token in
            if token == nil {
                pure1v1Print("RttApiManager fetchStartRtt failed token is null")
                completion(false)
                return
            } else {
                self.tokenName = token!
            }
            
            do {
                // 根据提供的 JSON 结构定义字段
                let languages = languages
                let sourceLanguage = sourceLanguage
                let targetLanguages = targetLanguages
                
                // 配置字典
                let rtcConfig: [String: Any] = [
                    "channelName": channelName,
                    "subBotUid": subBotUid,
                    "subBotToken": subBotToken,
                    "pubBotUid": pubBotUid,
                    "pubBotToken": pubBotToken,
                    "subscribeAudioUids": [RttManager.shared.targetUid]
                    // 根据需要添加其他 rtcConfig 字段
                ]
                
                let captionConfig: [String: Any] = [
                    "sliceDuration": 60,
                    "storage": [
                        "accessKey": "<YourOssAccessKey>",
                        "secretKey": "<YourOssSecretKey>",
                        "bucket": "<YourOssBucketName>",
                        "vendor": "<YourOssVendor>",
                        "region": "<YourOssRegion>",
                        "fileNamePrefix": ["<YourOssPrefix>"]
                    ],
                ]
                
                let translateConfig: [String: Any] = [
                    "forceTranslateInterval": 2,
                    "languages": [
                        [
                            "source": sourceLanguage,
                            "target": targetLanguages
                        ]
                    ]
                ]
                
                // 构建顶级字典
                let postBody: [String: Any] = [
                    "languages": languages,
                    "maxIdleTime": 50,
                    "rtcConfig": rtcConfig,
                    //"captionConfig": captionConfig,
                    "translateConfig": translateConfig
                ]
                
                let url = self.startTaskUrl(domain: self.domain, appId: AppContext.shared.appId, tokenName: self.tokenName)
                guard let requestUrl = URL(string: url) else {return}
                var request = URLRequest(url: requestUrl)
                request.httpMethod = "POST"
                request.setValue("application/json", forHTTPHeaderField: "Content-Type")
                request.setValue(self.auth, forHTTPHeaderField: "Authorization")
                request.httpBody = try JSONSerialization.data(withJSONObject: postBody, options: [])
                pure1v1Print("RttApiManager fetchStartRtt url: \(request.httpBody)")
                
             //   let semaphore = DispatchSemaphore(value: 0)
                
                let task = self.session.dataTask(with: request) { (data, response, error) in
                    if let error = error {
                        pure1v1Print("RttApiManager fetchStartRtt failed: \(error.localizedDescription)")
                        DispatchQueue.main.async {
                            completion(false)
                        }
                    } else if let data = data {
                        do {
                            guard let responseDict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any], let taskId = responseDict["taskId"] as? String else {
                                DispatchQueue.main.async {
                                    completion(false)
                                }
                                return
                            }
                            
                            self.taskId = taskId
                            pure1v1Print("RttApiManager fetchStartRtt success taskId: \(taskId)")
                            DispatchQueue.main.async {
                                completion(true)
                            }
                        } catch {
                            pure1v1Print("RttApiManager fetchStartRtt failed: \(error.localizedDescription)")
                            DispatchQueue.main.async {
                                completion(false)
                            }
                        }
                    }
                }
                
                task.resume()
               // semaphore.wait()
                
            } catch {
                pure1v1Print("RttApiManager fetchStartRtt failed: \(error.localizedDescription)")
                DispatchQueue.main.async {
                    completion(false)
                }
            }
        }
        
    }
    
    func fetchStopRtt(completion: @escaping ((Bool)->Void)) {
        if taskId.isEmpty || tokenName.isEmpty {
            pure1v1Print("RttApiManager fetchStopRtt failed taskId || tokenName is null")
            return
        }
        
        do {
            let url = deleteTaskUrl(domain: domain, appid: AppContext.shared.appId, taskid: taskId, tokenName: tokenName)
            guard let requestUrl = URL(string: url) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "DELETE"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(auth, forHTTPHeaderField: "Authorization")
                        
            let task = session.dataTask(with: request) { (data, response, error) in
                // Handle response
                pure1v1Print("RttApiManager fetchStopRtt: \(error)")
                if let error = error {
                    DispatchQueue.main.async {
                        completion(false)
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(true)
                    }
                }
            }
            
            task.resume()
        } catch {
            pure1v1Print("RttApiManager fetchStopRtt failed: \(error.localizedDescription)")
            DispatchQueue.main.async {
                completion(false)
            }
        }
    }
    
    // 获取 Token
    private func getTokenUrl(domain: String, appId: String) -> String {
        return String(format: "%@/v1/projects/%@/rtsc/speech-to-text/builderTokens", domain, appId)
    }

    // 开启 RTT 任务
    private func startTaskUrl(domain: String, appId: String, tokenName: String) -> String {
        return String(format: "%@/v1/projects/%@/rtsc/speech-to-text/tasks?builderToken=%@", domain, appId, tokenName)
    }

    // 结束 RTT 任务
    private func deleteTaskUrl(domain: String, appid: String, taskid: String, tokenName: String) -> String {
        return String(format: "%@/v1/projects/%@/rtsc/speech-to-text/tasks/%@?builderToken=%@", domain, appid, taskid, tokenName)
    }
}
