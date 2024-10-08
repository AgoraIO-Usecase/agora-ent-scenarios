//
//  AppContext+SpatialAudio.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/9.
//

import Foundation
import AgoraCommon

extension AppContext {
    static private var _saServiceImp: SpatialAudioServiceProtocol?
    
    //TODO: need to remove
    static func saTmpServiceImp() -> SpatialAudioSyncSerciceImp {
        return saServiceImp() as! SpatialAudioSyncSerciceImp
    }
    
    static func saServiceImp() -> SpatialAudioServiceProtocol {
        if let imp = _saServiceImp {
            return imp
        }
        _saServiceImp = SpatialAudioSyncSerciceImp()
        return _saServiceImp!
    }
    
    static func unloadSaServiceImp() {
        _saServiceImp = nil
    }
}
