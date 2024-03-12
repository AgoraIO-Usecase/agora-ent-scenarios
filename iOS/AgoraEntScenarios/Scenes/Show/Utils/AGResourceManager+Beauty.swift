//
//  ResourceManager+Beauty.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

import AGResourceManager

extension AGResourceManager {
    static func checkAndSetupBeautyPath(_ types: [BeautyFactoryType]) -> Bool {
        
        //TODO(wst): test code
        let manager = AGResourceManager.shared
        let loadingToastStr = "show_beauty_resource_downloading".show_localized
        for type in types {
            switch type {
            case .sense:
                //setup senseme path
                guard let stLicResource = manager.getResource(uri: "beauty1/SenseLib_lic"),
                      manager.getStatus(resource: stLicResource) == .downloaded,
                      let stResource = manager.getResource(uri: "beauty1/SenseLib"),
                      manager.getStatus(resource: stResource) == .downloaded else {
                    AGResourceManager.autoDownload()
                    ToastView.show(text: loadingToastStr)
                    return false
                }
                let stLicPath = manager.getFolderPath(resource: stLicResource) + "/SENSEME.lic"
                let stResourcePath = manager.getFolderPath(resource: stResource) + "/SenseLib"
                STDynmicResourceConfig.shareInstance().licFilePath = stLicPath
                STDynmicResourceConfig.shareInstance().resourceFolderPath = stResourcePath
            case .byte:
                //setup byte effect path
                guard let beLicResource = manager.getResource(uri: "beauty1/ByteEffectLib_lic"),
                      manager.getStatus(resource: beLicResource) == .downloaded,
                      let beResource = manager.getResource(uri: "beauty1/ByteEffectLib"),
                      manager.getStatus(resource: beResource) == .downloaded else {
                    AGResourceManager.autoDownload()
                    ToastView.show(text: loadingToastStr)
                    return false
                }
                let beLicPath = manager.getFolderPath(resource: beLicResource) + "/LicenseBag.bundle/Agora_test_20240111_20240411_io.agora.test.entfull_4.5.0_1111.licbag"
                let beResourcePath = manager.getFolderPath(resource: beResource) + "/ByteEffectLib"
                BEDynmicResourceConfig.shareInstance().licFilePath = beLicPath
                BEDynmicResourceConfig.shareInstance().resourceFolderPath = beResourcePath
            case .fu:
                //setup fu effect path
                guard let fuLicResource = manager.getResource(uri: "beauty2/FULib_lic"),
                      manager.getStatus(resource: fuLicResource) == .downloaded,
                      let fuResource = manager.getResource(uri: "beauty2/FULib"),
                      manager.getStatus(resource: fuResource) == .downloaded else {
                    AGResourceManager.autoDownload()
                    ToastView.show(text: loadingToastStr)
                    return false
                }
                let beLicPath = manager.getFolderPath(resource: fuLicResource) + "/authpack.h"
                let beResourcePath = manager.getFolderPath(resource: fuResource) + "/FULib"
                FUDynmicResourceConfig.shareInstance().licFilePath = beLicPath
                FUDynmicResourceConfig.shareInstance().resourceFolderPath = beResourcePath
                
                break
            default:
                break
            }
        }
        return true
    }
}
