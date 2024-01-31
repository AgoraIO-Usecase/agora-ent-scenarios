//
//  AppContext+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import Foundation
import AgoraCommon
let kShowLogBaseContext = "AgoraKit"
let showLogger = AgoraEntLog.createLog(config: AgoraEntLogConfig(sceneName: "Show"))

private let kShowRoomListKey = "kShowRoomListKey"
private let kRtcTokenMapKey = "kRtcTokenMapKey"
private let kRtcToken = "kRtcToken"
private let kDebugModeKey = "kDebugModeKey"

extension AppContext {
    static private var _showServiceImpMap: [String: ShowSyncManagerServiceImp] = [String: ShowSyncManagerServiceImp]()
    static func showServiceImp(_ roomId: String) -> ShowServiceProtocol? {
        let showServiceImp = _showServiceImpMap[roomId]
        guard let showServiceImp = showServiceImp else {
            var serviceImp: ShowServiceProtocol? = _showServiceImpMap[roomId]
            if let imp = serviceImp {return imp}
            if roomId.count == 6 {
                serviceImp = ShowSyncManagerServiceImp()
            } else {
                serviceImp = ShowRobotSyncManagerServiceImp()
            }
            _showServiceImpMap[roomId] = serviceImp as? ShowSyncManagerServiceImp
            return serviceImp!
        }
        return showServiceImp
    }
    
    static func unloadShowServiceImp(_ roomId: String) {
        _showServiceImpMap[roomId] = nil
    }
    
    static func unloadShowServiceImp() {
        _showServiceImpMap = [String: ShowSyncManagerServiceImp]()
        SyncUtilsWrapper.cleanScene()
    }
    
    static func unloadShowServiceImpExcludeRoomList() {
        _showServiceImpMap.forEach { (key, value) in
            if key.count > 0 {
                _showServiceImpMap.removeValue(forKey: key)
            }
        }
        SyncUtilsWrapper.cleanScene()
    }
    
    var showRoomList: [ShowRoomListModel]? {
        set {
            self.extDic[kShowRoomListKey] = newValue
        }
        get {
            return self.extDic[kShowRoomListKey] as? [ShowRoomListModel]
        }
    }
    
    public var rtcToken: String? {
        set {
            self.extDic[kRtcToken] = newValue
        }
        get {
            return self.extDic[kRtcToken] as? String
        }
    }
}

