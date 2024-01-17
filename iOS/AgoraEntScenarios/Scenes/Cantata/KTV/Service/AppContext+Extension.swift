//
//  AppContext+Extension.swift
//  Cantata
//
//  Created by CP on 2023/8/28.
//

import Foundation
import AgoraCommon
extension AppContext {
    private static let kServiceImpKey = "ServiceImpKey"
    private static let kAgoraKTVAPIKey = "kAgoraKTVAPIKey"
    
    var ktvAPI: KTVApiImpl? {
        get {
            return self.extDic[kAgoraKTVAPIKey] as? KTVApiImpl
        }
        set {
            self.extDic[kAgoraKTVAPIKey] = newValue
        }
    }
    
    static func setupKtvConfig() {
        AppContext.shared.sceneImageBundleName = "KtvResource"
        AppContext.shared.sceneLocalizeBundleName = "KtvResource"
    }
    
    static func ktvServiceImp() -> KTVServiceProtocol {
        if let ktvServiceImp = AppContext.shared.extDic[kServiceImpKey] as? KTVServiceProtocol {
            return ktvServiceImp
        } else {
            let ktvServiceImp = KTVSyncManagerServiceImp()
            AppContext.shared.extDic[kServiceImpKey] = ktvServiceImp
            return ktvServiceImp
        }
    }
    
    static func unloadServiceImp() {
        AppContext.shared.extDic.removeAllObjects()
    }
}
