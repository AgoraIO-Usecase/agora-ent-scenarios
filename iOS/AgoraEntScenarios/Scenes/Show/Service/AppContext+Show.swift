//
//  AppContext+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import Foundation

let kShowLogBaseContext = "AgoraKit"
let showLogger = AgoraEntLog.createLog(config: AgoraEntLogConfig(sceneName: "Show"))

private let kShowRoomListKey = "kShowRoomListKey"
private let kRtcTokenMapKey = "kRtcTokenMapKey"
private let kRtcToken = "kRtcToken"
private let kDebugModeKey = "kDebugModeKey"

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
    
    public var rtcToken: String? {
        set {
            self.extDic[kRtcToken] = newValue
        }
        get {
            return self.extDic[kRtcToken] as? String
        }
    }
    
//    @objc public var isDebugMode: Bool{
//        set{
//            UserDefaults.standard.set(newValue, forKey: kDebugModeKey)
//        }
//        
//        get {
//            return UserDefaults.standard.bool(forKey: kDebugModeKey)
//        }
//    }
}

