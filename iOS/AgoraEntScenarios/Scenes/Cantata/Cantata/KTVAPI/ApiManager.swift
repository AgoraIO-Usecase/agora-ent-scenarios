import Foundation
import AgoraCommon
class ApiManager {
    static let shared = ApiManager()
    
    private let TAG = "ApiManager"
    
    private var tokenName = ""
    private var taskId = ""
    
    private lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 30
        
        return URLSession(configuration: configuration)
    }()
    
    
    func fetchStartCloud(mainChannel: String, cloudRtcUid: Int, completion: @escaping ((Bool)->Void)) {
        do {
            let inputRetObj: [String: Any] = [
                "rtcUid": 0,
                "rtcChannel": mainChannel
            ]
            let outputRetObj: [String: Any] = [
                "rtcUid": cloudRtcUid,
                "rtcChannel": "\(mainChannel)_ad"
            ]
            var transcoderObj: [String: Any] = [
                "src": "iOS",
                "traceId": "12345",
                "instanceId": "\(Int(Date().timeIntervalSince1970 * 1000))",
                "audioInputsRtc": inputRetObj,
                "outputsRtc": outputRetObj
            ]
            if let appId = AppContext.shared.sceneConfig?.cantataAppId {
                transcoderObj.updateValue(appId, forKey: "appId")
                transcoderObj.updateValue("", forKey: "appCert")
                transcoderObj.updateValue("", forKey: "basicAuth")
            } else {
                transcoderObj.updateValue(AppContext.shared.appId, forKey: "appId")
                transcoderObj.updateValue(AppContext.shared.certificate, forKey: "appCert")
                transcoderObj.updateValue(getBasicAuth(), forKey: "basicAuth")
            }
            
            guard let requestUrl = URL(string: startTaskUrl()) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
            request.httpBody = try JSONSerialization.data(withJSONObject: transcoderObj, options: [])
            
         //   let semaphore = DispatchSemaphore(value: 0)
            
            let task = session.dataTask(with: request) { (data, response, error) in
                if let error = error {
                    print("云端合流uid 请求报错: \(error.localizedDescription)")
                    completion(false)
                    VLToast.toast("ktv_merge_failed_and create".toSceneLocalization() as String)
                } else if let data = data {
                    do {
                        guard let responseDict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                              let dataDict = responseDict["data"] as? [String: Any],
                              let taskId = dataDict["taskId"] as? String,
                              let tokenName = dataDict["builderToken"] as? String
                        else {
                            completion(false)
                            return
                        }
                        self.taskId = taskId
                        self.tokenName = tokenName
                        completion(true)
                        VLToast.toast("ktv_merge_success".toSceneLocalization() as String)
                    } catch {
                        print("云端合流uid 请求报错: \(error.localizedDescription)")
                        completion(false)
                        VLToast.toast("ktv_merge_failed_and create".toSceneLocalization() as String)
                    }
                }
                
             //   semaphore.signal()
            }
            
            task.resume()
           // semaphore.wait()
            
        } catch {
            print("云端合流uid 请求报错: \(error.localizedDescription)")
            completion(false)
            VLToast.toast("ktv_merge_failed_and create".toSceneLocalization() as String)
        }
    }
    
    func fetchStopCloud() {
        if taskId.isEmpty || tokenName.isEmpty {
            print("云端合流任务停止失败 taskId || tokenName is null")
            return
        }
        
        var body: [String: Any] = [
            "src": "iOS",
            "traceId": "12345",
            "taskId": taskId,
            "builderToken": tokenName,
        ]
        if let appId = AppContext.shared.sceneConfig?.cantataAppId {
            body.updateValue(appId, forKey: "appId")
            body.updateValue("", forKey: "appCert")
            body.updateValue("", forKey: "basicAuth")
        } else {
            body.updateValue(AppContext.shared.appId, forKey: "appId")
            body.updateValue(AppContext.shared.certificate, forKey: "appCert")
            body.updateValue(getBasicAuth(), forKey: "basicAuth")
        }
        do {
            guard let requestUrl = URL(string: deleteTaskUrl()) else {return}
            var request = URLRequest(url: requestUrl)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(getBasicAuth(), forHTTPHeaderField: "Authorization")
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
            
            let task = session.dataTask(with: request) { (data, response, error) in
                // Handle response
                
             //   semaphore.signal()
            }
            
            task.resume()
           // semaphore.wait()
            
        } catch {
            print("云端合流任务停止失败: \(error.localizedDescription)")
        }
    }

    private func startTaskUrl() -> String {
        let domain = AppContext.shared.baseServerUrl + "toolbox"
        return String(format: "%@/v1/cloud-transcoder/start", domain)
    }

    private func deleteTaskUrl() -> String {
        let domain = AppContext.shared.baseServerUrl + "toolbox"
        return String(format: "%@/v1/cloud-transcoder/stop", domain)
    }

    private func getBasicAuth() -> String {
        // 拼接客户 ID 和客户密钥并使用 base64 编码
        let plainCredentials = "\(AppContext.shared.RestfulApiKey):\(AppContext.shared.RestfulApiSecret)"
        guard let base64Credentials = plainCredentials.data(using: .utf8)?.base64EncodedString() else {
            return ""
        }
        // 创建 authorization header
        return "Basic \(base64Credentials)"
    }
    
}
