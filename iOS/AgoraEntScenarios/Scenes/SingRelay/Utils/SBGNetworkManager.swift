//
//  SRNetworkManager.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/16.
//

import Foundation

class SRNetworkManager: NSObject {
    @objc static let shared = SRNetworkManager()
    let testUrl = "https://test-toolbox.bj2.agoralab.co/v1/ktv/song/grab"
    let url = "https://toolbox.bj2.agoralab.co/v1/ktv/song/grab"
    let networkTool = NetworkTools()
    //发起抢唱
    @objc func startSongGrab(_ appid: String, sceneId: String, roomId: String, headUrl: String, userId: String, userName: String, songCode: String, success: @escaping (Bool) -> Void) {
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
        
        networkTool.request(url, method: .post, parameters: params) {[weak self] result in
            switch result{
                case .success(let data):
                    let obj = self?.data2Dict(with: data)
                    print("obj:\(String(describing: obj))")
                    guard let code: Int = obj?["code"] as? Int else {return}
                    success(code == 0 ? true : false)
                case .failure(let error):
                    print(error)
                    success(false)
            }
        }
        
    }
    
    //抢唱结果查询
    @objc func songGrabQuery(_ appid: String, sceneId: String, roomId: String, songCode: String, src: String, success: @escaping (String?, String?,Bool) -> Void) {
        let params = [
            "appId": appid,
            "sceneId": sceneId,
            "roomId": roomId,
            "songCode": songCode,
            "src": "postman"
        ]
        
        networkTool.request("\(url)/query".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "", method: .get, parameters: params) { result in
            switch result {
            case .success(let data):
                do {
                    if let obj = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                       let code = obj["code"] as? Int,
                       code == 0 {
                        let userData = obj["data"] as? [String: Any]
                        let userId = userData?["userId"]
                        let userName = userData?["userName"]
                        success(userId as? String, userName as? String, true)
                    } else {
                        success(nil,nil, false)
                    }
                } catch {
                    print(error)
                    success(nil, nil, false)
                }
            case .failure(let error):
                print(error)
                success(nil, nil, false)
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
