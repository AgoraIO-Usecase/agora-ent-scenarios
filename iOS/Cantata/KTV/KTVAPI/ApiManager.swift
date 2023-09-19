import Foundation
import AgoraCommon
class ApiManager {
    static let shared = ApiManager()
    
    private let domain = "http://218.205.37.50:16000"
    private let testIp = "218.205.37.50"
    
    private let TAG = "ApiManager"
    
    private var tokenName = ""
    private var taskId = ""
    
    private lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 30
        
        return URLSession(configuration: configuration)
    }()
    
    func fetchCloudToken() -> String {
        var token = ""
        
        do {
            let acquireOjb = try JSONSerialization.data(withJSONObject: [
                "instanceId": String(Date().timeIntervalSince1970),
                "testIp": testIp
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
                    print("getToken error: \(error.localizedDescription)")
                } else if let data = data {
                    do {
                        guard let responseDict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any], let tokenName = responseDict["tokenName"] as? String else {
                            return
                        }
                        
                        token = tokenName
                    } catch {
                        print("getToken error: \(error.localizedDescription)")
                    }
                }
                
                semaphore.signal()
            }
            
            task.resume()
            semaphore.wait()
            
        } catch {
            print("getToken error: \(error.localizedDescription)")
        }
        
        return token
    }
    
    func fetchStartCloud(mainChannel: String, cloudRtcUid: Int) {
        let token = fetchCloudToken()
        
        if token.isEmpty {
            print("云端合流uid 请求报错 token is null")
            return
        } else {
            tokenName = token
        }
        
        do {
            let inputRetObj: [String: Any] = [
                "rtcUid": 0,
                "rtcToken": AppContext.shared.appId,
                "rtcChannel": mainChannel
            ]
            
            let intObj: [String: Any] = ["rtc": inputRetObj]
            
            let audioOptionObj: [String: Any] = [
                "profileType": "AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO",
                "fullChannelMixer": "native-mixer-weighted"
            ]
            
            let outputRetObj: [String: Any] = [
                "rtcUid": cloudRtcUid,
                "rtcToken": AppContext.shared.appId,
                "rtcChannel": "\(mainChannel)_ad"
            ]
            
            let dataStreamObj: [String: Any] = [
                "source": ["dataStream": true],
                "sink": [:]
            ]
            
            let outputsObj: [String: Any] = [
                "audioOption": audioOptionObj,
                "rtc": outputRetObj,
                "dataStreamOption": dataStreamObj
            ]
            
            let transcoderObj: [String: Any] = [
                "audioInputs": [intObj],
                "idleTimeout": 30,
                "outputs": [outputsObj]
            ]
            
            let postBody: [String: Any] = [
                "services": [
                    "cloudTranscoder": [
                        "serviceType": "cloudTranscoderV2",
                        "config": [
                            "transcoder": transcoderObj
                        ]
                    ]
                ]
            ]
            
            let url = startTaskUrl(domain: domain, appId: AppContext.shared.appId, tokenName: tokenName)
            guard let requestUrl = URL(string: url) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
            request.httpBody = try JSONSerialization.data(withJSONObject: postBody, options: [])
            
            let semaphore = DispatchSemaphore(value: 0)
            
            let task = session.dataTask(with: request) { (data, response, error) in
                if let error = error {
                    print("云端合流uid 请求报错: \(error.localizedDescription)")
                } else if let data = data {
                    do {
                        guard let responseDict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any], let taskId = responseDict["taskId"] as? String else {
                            return
                        }
                        
                        self.taskId = taskId
                    } catch {
                        print("云端合流uid 请求报错: \(error.localizedDescription)")
                    }
                }
                
                semaphore.signal()
            }
            
            task.resume()
            semaphore.wait()
            
        } catch {
            print("云端合流uid 请求报错: \(error.localizedDescription)")
        }
    }
    
    func fetchStopCloud() {
        if taskId.isEmpty || tokenName.isEmpty {
            print("云端合流任务停止失败 taskId || tokenName is null")
            return
        }
        
        do {
            let url = deleteTaskUrl(domain: domain, appid: AppContext.shared.appId, taskid: taskId, tokenName: tokenName)
            guard let requestUrl = URL(string: url) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "DELETE"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
            
            let semaphore = DispatchSemaphore(value: 0)
            
            let task = session.dataTask(with: request) { (data, response, error) in
                // Handle response
                
                semaphore.signal()
            }
            
            task.resume()
            semaphore.wait()
            
        } catch {
            print("云端合流任务停止失败: \(error.localizedDescription)")
        }
    }
    
    private func getTokenUrl(domain: String, appId: String) -> String {
        return String(format: "%@/v1/projects/%@/rtsc/cloud-transcoder/builderTokens", domain, appId)
    }

    private func startTaskUrl(domain: String, appId: String, tokenName: String) -> String {
        return String(format: "%@/v1/projects/%@/rtsc/cloud-transcoder/tasks?builderToken=%@", domain, appId, tokenName)
    }

    private func deleteTaskUrl(domain: String, appid: String, taskid: String, tokenName: String) -> String {
        return String(format: "%@/v1/projects/%@/rtsc/cloud-transcoder/tasks/%@?builderToken=%@", domain, appid, taskid, tokenName)
    }

    private func getBasicAuth() -> String {
        // 拼接客户 ID 和客户密钥并使用 base64 编码
        let plainCredentials = "\(AppContext.shared.appId):\(AppContext.shared.certificate)"
        guard let base64Credentials = plainCredentials.data(using: .utf8)?.base64EncodedString() else {
            return ""
        }
        // 创建 authorization header
        return "Basic \(base64Credentials)"
    }
    
}
