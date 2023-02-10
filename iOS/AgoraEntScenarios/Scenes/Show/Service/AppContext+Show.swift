//
//  AppContext+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import Foundation

let kShowLogBaseContext = "AgoraKit"
let showLogger = AgoraEntLog.createLog(config: AgoraEntLogConfig.init(sceneName: "Show"))

private let kShowRoomListKey = "kShowRoomListKey"
private let kRtcTokenMapKey = "kRtcTokenMapKey"

extension AppContext {
    static private var _showServiceImpMap: [String: ShowSyncManagerServiceImp] = [String: ShowSyncManagerServiceImp]()
    
    static func showServiceImp(_ roomId: String) -> ShowServiceProtocol {
        let showServiceImp = _showServiceImpMap[roomId]
        guard let showServiceImp = showServiceImp else {
            let imp = roomId.count == 6 ? ShowSyncManagerServiceImp() : ShowRobotSyncManagerServiceImp()
            _showServiceImpMap[roomId] = imp
            return imp
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
    
    public var showRoomList: [ShowRoomListModel]? {
        set {
            self.extDic[kShowRoomListKey] = newValue
        }
        get {
            return self.extDic[kShowRoomListKey] as? [ShowRoomListModel]
        }
    }
    
    public var rtcTokenMap: [String: String]? {
        set {
            self.extDic[kRtcTokenMapKey] = newValue
        }
        get {
            return self.extDic[kRtcTokenMapKey] as? [String: String]
        }
    }
}

