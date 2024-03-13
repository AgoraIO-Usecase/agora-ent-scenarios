//
//  AUISyncManager.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/1/25.
//

import Foundation
import AgoraRtmKit

public class AUISyncManager: NSObject {
    public var sceneMap: [String: AUIScene] = [:]
    public private(set) var rtmManager: AUIRtmManager
    
    public required init(rtmClient: AgoraRtmClientKit?, commonConfig: AUICommonConfig) {
        AUIRoomContext.shared.commonConfig = commonConfig
        let _rtmClient = rtmClient ?? AUISyncManager.createRtmClient()
        self.rtmManager = AUIRtmManager(rtmClient: _rtmClient,
                                        rtmChannelType: .message,
                                        isExternalLogin: _rtmClient == rtmClient)
        super.init()
    }
    
    public func login(with token: String, completion: @escaping (NSError?) -> ()) {
        aui_info("login")
        rtmManager.login(token: token, completion: completion)
    }
    
    public func logout() {
        aui_info("logout")
        rtmManager.logout()
    }
    
    public func getScene(channelName: String) -> AUIScene {
        aui_info("getScene: \(channelName)")
        if let scene = sceneMap[channelName] {
            return scene
        }
        
        let scene = AUIScene(channelName: channelName, rtmManager: rtmManager)
        sceneMap[channelName] = scene
        return scene
    }
}

extension AUISyncManager {
    private static func createRtmClient() -> AgoraRtmClientKit {
        let commonConfig = AUIRoomContext.shared.commonConfig!
        let userInfo = AUIRoomContext.shared.currentUserInfo
        let rtmConfig = AgoraRtmClientConfig(appId: commonConfig.appId, userId: userInfo.userId)
        rtmConfig.presenceTimeout = 60
        if rtmConfig.userId.count == 0 {
            aui_error("userId is empty")
            assert(false, "userId is empty")
        }
        if rtmConfig.appId.count == 0 {
            aui_error("appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
            assert(false, "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
        }
        let rtmClient = try? AgoraRtmClientKit(rtmConfig, delegate: nil)
        return rtmClient!
    }
}
