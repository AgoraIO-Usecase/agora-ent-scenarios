//
//  AppContext+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import Foundation
import AgoraCommon
import SwiftyBeaver

let kShowLogBaseContext = "AgoraKit"

private let kShowRoomListKey = "kShowRoomListKey"
private let kRtcTokenMapKey = "kRtcTokenMapKey"
private let kRtcToken = "kRtcToken"
private let kRtcTokenDate = "kRtcTokenDate"
private let kDebugModeKey = "kDebugModeKey"

func showLogger() -> SwiftyBeaver.Type {
    AgoraEntLog.getSceneLogger(with: "Show")
}

func showPrint(_ message: String, context: String? = nil) {
    agoraDoMainThreadTask {
        showLogger().info(message, context: context)
    }
}

func showWarn(_ message: String, context: String? = nil) {
    agoraDoMainThreadTask {
        showLogger().warning(message, context: context)
    }
}

func showError(_ message: String, context: String? = nil) {
    agoraDoMainThreadTask {
        showLogger().error(message, context: context)
    }
}

extension AppContext {
    static private var _showServiceImp: ShowSyncManagerServiceImp?
    static func showServiceImp() -> ShowServiceProtocol? {
        if let service = _showServiceImp {
            return service
        }
        
        _showServiceImp = ShowSyncManagerServiceImp()
        
        return _showServiceImp
    }
    
    static func unloadShowServiceImp() {
        _showServiceImp = nil
    }
    
    public var rtcToken: String? {
        set {
            self.extDic[kRtcToken] = newValue
            self.tokenDate = Date()
        }
        get {
            return self.extDic[kRtcToken] as? String
        }
    }
    
    public var tokenDate: Date? {
        set {
            self.extDic[kRtcTokenDate] = newValue
        }
        get {
            return self.extDic[kRtcTokenDate] as? Date
        }
    }
}

