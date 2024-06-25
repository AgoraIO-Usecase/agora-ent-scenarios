//  RttApiManager.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/25.
//

import Foundation
import AgoraCommon

class RttApiManager {
    static let shared = RttApiManager()
    
    private let domain = "https://api.agora.io"
    
    private let TAG = "RttApiManager"
    
    private var tokenName = ""
    private var taskId = ""
    
    private lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 30
        return URLSession(configuration: configuration)
    }()
    
    func fetchCloudToken() -> String? {
        var token: String? = nil
        
        do {
            let timeInterval: TimeInterval = Date().timeIntervalSince1970
            let millisecond = CLongLong(round(timeInterval*1000))
            let acquireOjb = try JSONSerialization.data(withJSONObject: [
                "instanceId": "\(Int(millisecond))"
            ])

            let url = getTokenUrl(domain: domain, appId: AppContext.shared.appId)
            guard let requestUrl = URL(string: url) else {return ""}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
            request.httpBody = acquireOjb
            
            let semaphore = DispatchSemaphore(value: 0)
            
            let task = session.dataTask(with: request) { (data, response, error) in
                if let error = error {
                    print("RttApiManager getToken error: \(error.localizedDescription)")
                    token = nil
                } else if let data = data {
                    do {
                        guard let responseDict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any], let tokenName = responseDict["tokenName"] as? String else {
                            semaphore.signal()
                            return
                        }
                        token = tokenName
                        print("RttApiManager getToken success")
                    } catch {
                        print("RttApiManager getToken error: \(error.localizedDescription)")
                        token = nil
                    }
                }
                
                semaphore.signal()
            }
            
            task.resume()
            semaphore.wait()
            
        } catch {
            print("RttApiManager getToken error: \(error.localizedDescription)")
            token = nil
        }
        
        return token
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
            
        let token = fetchCloudToken()
        if token == nil {
            print("RttApiManager fetchStartRtt failed token is null")
            completion(false)
            return
        } else {
            tokenName = token!
        }
        
        do {
            // 根据提供的 JSON 结构定义字段
            let languages = languages
            let sourceLanguage = sourceLanguage
            let targetLanguages = targetLanguages
            
            // 配置字典
            let rtcConfig = [
                "channelName": channelName,
                "subBotUid": subBotUid,
                "subBotToken": subBotToken,
                "pubBotUid": pubBotUid,
                "pubBotToken": pubBotToken
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
                    // 根据需要添加其他 storage 字段
                ],
                // 根据需要添加其他 captionConfig 字段
            ]
            
            let translateConfig: [String: Any] = [
                "forceTranslateInterval": 2,
                "languages": [
                    [
                        "source": sourceLanguage,
                        "target": targetLanguages
                    ]
                ]
                // 根据需要添加其他 translateConfig 字段
            ]
            
            // 构建顶级字典
            let postBody: [String: Any] = [
                "languages": languages,
                "maxIdleTime": 50,
                "rtcConfig": rtcConfig,
                //"captionConfig": captionConfig,
                "translateConfig": translateConfig
            ]
            
            let url = startTaskUrl(domain: domain, appId: AppContext.shared.appId, tokenName: tokenName)
            guard let requestUrl = URL(string: url) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
            request.httpBody = try JSONSerialization.data(withJSONObject: postBody, options: [])
            print("RttApiManager fetchStartRtt url: \(request.httpBody)")
            
         //   let semaphore = DispatchSemaphore(value: 0)
            
            let task = session.dataTask(with: request) { (data, response, error) in
                if let error = error {
                    print("RttApiManager fetchStartRtt failed: \(error.localizedDescription)")
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
                        print("RttApiManager fetchStartRtt success taskId: \(taskId)")
                        DispatchQueue.main.async {
                            completion(true)
                        }
                    } catch {
                        print("RttApiManager fetchStartRtt failed: \(error.localizedDescription)")
                        DispatchQueue.main.async {
                            completion(false)
                        }
                    }
                }
            }
            
            task.resume()
           // semaphore.wait()
            
        } catch {
            print("RttApiManager fetchStartRtt failed: \(error.localizedDescription)")
            DispatchQueue.main.async {
                completion(false)
            }
        }
    }
    
    func fetchStopRtt(completion: @escaping ((Bool)->Void)) {
        if taskId.isEmpty || tokenName.isEmpty {
            print("RttApiManager fetchStopRtt failed taskId || tokenName is null")
            return
        }
        
        do {
            let url = deleteTaskUrl(domain: domain, appid: AppContext.shared.appId, taskid: taskId, tokenName: tokenName)
            guard let requestUrl = URL(string: url) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "DELETE"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
                        
            let task = session.dataTask(with: request) { (data, response, error) in
                // Handle response
                print("RttApiManager fetchStopRtt: \(error)")
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
            print("云端合流任务停止失败: \(error.localizedDescription)")
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

    private func getBasicAuth() -> String {
        // 拼接客户 ID 和客户密钥并使用 base64 编码
        let plainCredentials = "\(AppContext.shared.appId):\(AppContext.shared.certificate)"
        guard let base64Credentials = plainCredentials.data(using: .utf8)?.base64EncodedString() else {
            return ""
        }
        // 创建 authorization header
        return "Basic *****************************="
    }
}
