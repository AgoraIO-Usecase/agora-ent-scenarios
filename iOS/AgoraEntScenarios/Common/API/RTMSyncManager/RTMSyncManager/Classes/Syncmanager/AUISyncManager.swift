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
    deinit {
        aui_info("deinit AUISyncManager")
    }
    
    public required init(rtmClient: AgoraRtmClientKit?, commonConfig: AUICommonConfig) {
        aui_info("init AUISyncManager")
        AUIRoomContext.shared.commonConfig = commonConfig
        let _rtmClient = rtmClient ?? AUISyncManager.createRtmClient()
        self.rtmManager = AUIRtmManager(rtmClient: _rtmClient,
                                        rtmChannelType: .message,
                                        isExternalLogin: _rtmClient == rtmClient)
        super.init()
    }
    
    public func login(with token: String, completion: @escaping (NSError?) -> ()) {
        rtmManager.login(token: token, completion: completion)
    }
    
    public func logout() {
        rtmManager.logout()
    }
    
    public func renew(token: String) {
        rtmManager.renew(token: token)
    }
    
    public func createScene(channelName: String) -> AUIScene {
        aui_info("createScene: \(channelName)")
        if let scene = getScene(channelName: channelName) {
            return scene
        }
        
        let scene = AUIScene(channelName: channelName, rtmManager: rtmManager) { [weak self] in
            self?.sceneMap.removeValue(forKey: channelName)
        }
        sceneMap[channelName] = scene
        return scene
    }
    
    public func getScene(channelName: String) -> AUIScene? {
        aui_info("getScene: \(channelName)")
        if let scene = sceneMap[channelName] {
            return scene
        }
        
        return nil
    }
}

extension AUISyncManager {
    private static func createRtmClient() -> AgoraRtmClientKit {
        let commonConfig = AUIRoomContext.shared.commonConfig!
        let userInfo = AUIRoomContext.shared.currentUserInfo
        let rtmConfig = AgoraRtmClientConfig(appId: commonConfig.appId, userId: userInfo.userId)
        rtmConfig.presenceTimeout = 30
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
