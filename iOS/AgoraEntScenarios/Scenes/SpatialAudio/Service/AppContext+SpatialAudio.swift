//
//  AppContext+SpatialAudio.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/9.
//

import Foundation

let saLogger = AgoraEntLog.createLog(config: AgoraEntLogConfig.init(sceneName: "SpatialAudio"))
extension AppContext {
    static private var _saServiceImp: SpatialAudioServiceProtocol?
    
    static func saServiceImp() -> SpatialAudioServiceProtocol {
        if let imp = _saServiceImp {
            return imp
        }
        _saServiceImp = SpatialAudioSyncSerciceImp()
        return _saServiceImp!
    }
    
    static func unloadShowServiceImp() {
        _saServiceImp = nil
    }
}
