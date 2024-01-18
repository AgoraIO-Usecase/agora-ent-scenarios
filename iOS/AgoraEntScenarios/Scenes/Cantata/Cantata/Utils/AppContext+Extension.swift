//
//  AppContext+Extension.swift
//  Cantata
//
//  Created by CP on 2023/8/29.
//

import Foundation
import AgoraCommon
//
//extension AppContext {
//    @objc(dhcAPI)
//    var dhcAPI: KTVApiImpl? {
//        get {
//            return AppContext.extDic.value(forKey: "dAgoraKTVAPIKey") as? KTVApiImpl
//        }
//        set {
//            AppContext.extDic.setValue(newValue, forKey: "dAgoraKTVAPIKey")
//        }
//    }
//    
//    @objc(setupDhcConfig)
//    static func setupDhcConfig() {
//        AppContext.shared.sceneImageBundleName = "DHCResource"
//        AppContext.shared.sceneLocalizeBundleName = "DHCResource"
//    }
//    
//    @objc(dhcServiceImp)
//    static func dhcServiceImp() -> KTVServiceProtocol {
//        var ktvServiceImp = extDic.value(forKey: "dServiceImpKey") as? KTVServiceProtocol
//        if ktvServiceImp == nil {
//            ktvServiceImp = KTVSyncManagerServiceImp()
//            extDic.setValue(ktvServiceImp, forKey: "dServiceImpKey")
//        }
//        
//        return ktvServiceImp!
//    }
//    
//    @objc(unloadServiceImp)
//    static func unloadServiceImp() {
//        extDic.removeAllObjects()
//    }
//}
