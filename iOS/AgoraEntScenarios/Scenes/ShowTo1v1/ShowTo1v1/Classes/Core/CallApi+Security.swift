//
//  CallApi+Security.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/10.
//

import Foundation
import CallAPI
import AgoraRtcKit
import AgoraCommon

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
            ShowTo1v1Logger.warn("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 60
      //  module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspect(enable, config: config)
        ShowTo1v1Logger.info("setupContentInspectConfig[\(enable)]: uid:\(uid) channelId: \(channelId) ret:\(ret)")
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
            ShowTo1v1Logger.warn("setupContentInspectExConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 60
        module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspectEx(enable, config: config, connection: connection)
        ShowTo1v1Logger.info("setupContentInspectExConfig[\(enable)]: uid:\(connection.localUid) channelId: \(connection.channelId) ret:\(ret)")
    }
    
    /// 语音审核
    func moderationAudio(channelName: String) {
        NetworkManager.shared.voiceIdentify(channelName: channelName, 
                                            channelType: AgoraChannelProfile.liveBroadcasting.rawValue,
                                            sceneType: "showTo1v1") { errStr in
            guard let errStr = errStr else {return}
            ShowTo1v1Logger.warn("moderationAudio response === \(errStr)")
        }
//        let userInfo = ["id": user.uid ?? "",
//                        "sceneName": "showTo1v1",
//                        "userNo": user.uid,
//                        "userName": user.userName] as NSDictionary
//        let parasm: [String: Any] = ["appId": appId,
//                                     "channelName": channelName,
//                                     "channelType": AgoraChannelProfile.liveBroadcasting.rawValue,
//                                     "traceId": NSString.withUUID().md5(),
//                                     "src": "iOS",
//                                     "payload": userInfo.yy_modelToJSONString()]
//        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.shengwang.cn/v1/moderation/audio",
//                                          params: parasm) { response in
//            ShowTo1v1Logger.info("moderationAudio response === \(response)")
//        } failure: { errr in
//            ShowTo1v1Logger.warn(errr)
//        }
    }
}

extension CallApiImpl: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, contentInspectResult result: AgoraContentInspectResult) {
        ShowTo1v1Logger.warn("contentInspectResult: \(result.rawValue)")
        guard result != .neutral else { return }
        AUIToast.show(text: "call_content_inspect_warning".showTo1v1Localization())
    }
}
