//
//  AppContext+Extension.swift
//  Cantata
//
//  Created by CP on 2023/8/29.
//

import Foundation
import AgoraCommon
import UIKit

@objc
extension AppContext {
    private static let dServiceImpKey = "ServiceImpKey"
    private static let dAgoraKTVAPIKey = "kAgoraKTVAPIKey"
    
    var ktvAPI: KTVApiImpl? {
        get {
            return extDic[AppContext.dAgoraKTVAPIKey] as? KTVApiImpl
        }
        set {
            extDic[AppContext.dAgoraKTVAPIKey] = newValue
        }
    }
    
    class func setupKtvConfig() {
        shared.sceneImageBundleName = "KtvResource"
        shared.sceneLocalizeBundleName = "KtvResource"
    }
    
    class func ktvServiceImp() -> KTVServiceProtocol? {
        if let ktvServiceImp = shared.extDic[dServiceImpKey] as? KTVServiceProtocol {
            return ktvServiceImp
        } else {
            let ktvServiceImp = KTVSyncManagerServiceImp()
            shared.extDic[dServiceImpKey] = ktvServiceImp
            return ktvServiceImp
        }
    }
    
    class func unloadServiceImp() {
        shared.extDic.removeAllObjects()
    }
}
