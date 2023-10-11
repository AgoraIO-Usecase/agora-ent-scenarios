//
//  CallApi+Security.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/10.
//

import Foundation
import CallAPI
import AgoraRtcKit

var showTo1v1AppId: String?
var showTo1v1AppCertificate: String?
extension CallApiImpl {
    func setupContentInspectConfig(rtcEngine: AgoraRtcEngineKit,
                                   enable: Bool,
                                   uid: String,
                                   channelId: String) {
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": "\(uid)",
            "sceneName": "ShowTo1v1",
            "userNo": "\(uid)"
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            showTo1v1Warn("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 30
        module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspect(enable, config: config)
        showTo1v1Print("setupContentInspectConfig[\(enable)]: uid:\(uid) channelId: \(channelId) ret:\(ret)")
    }
    
    func setupContentInspectExConfig(rtcEngine: AgoraRtcEngineKit,
                                     enable: Bool,
                                     connection: AgoraRtcConnection) {
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": "\(connection.localUid)",
            "sceneName": "ShowTo1v1",
            "userNo": "\(connection.localUid)"
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            showTo1v1Warn("setupContentInspectExConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 30
        module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspectEx(enable, config: config, connection: connection)
        showTo1v1Print("setupContentInspectExConfig[\(enable)]: uid:\(connection.localUid) channelId: \(connection.channelId) ret:\(ret)")
    }
    
    /// 语音审核
    func moderationAudio(appId: String, channelName: String, user: ShowTo1v1UserInfo) {
        let userInfo = ["id": user.userId ?? "",
                        "sceneName": "showTo1v1",
                        "userNo": user.userId,
                        "userName": user.userName] as NSDictionary
        let parasm: [String: Any] = ["appId": appId,
                                     "channelName": channelName,
                                     "channelType": AgoraChannelProfile.liveBroadcasting.rawValue,
                                     "traceId": NSString.withUUID().md5(),
                                     "src": "iOS",
                                     "payload": userInfo.yy_modelToJSONString()]
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.agoralab.co/v1/moderation/audio",
                                          params: parasm) { response in
            showTo1v1Print("moderationAudio response === \(response)")
        } failure: { errr in
            showTo1v1Warn(errr)
        }
    }
}

extension CallApiImpl: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, contentInspectResult result: AgoraContentInspectResult) {
        showTo1v1Warn("contentInspectResult: \(result.rawValue)")
        guard result != .neutral else { return }
        AUIToast.show(text: "call_content_inspect_warning".showTo1v1Localization())
    }
}
