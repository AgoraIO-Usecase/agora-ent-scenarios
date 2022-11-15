//
//  AppContext+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import Foundation

extension AppContext {
    static private var _showServiceImp: ShowSyncManagerServiceImp?
    
    static private (set) var showServiceImp: ShowSyncManagerServiceImp = {
        if _showServiceImp == nil {
            _showServiceImp = ShowSyncManagerServiceImp()
        }
        return _showServiceImp!
    }()
    
    static func unloadShowServiceImp() {
        _showServiceImp = nil
    }
}

