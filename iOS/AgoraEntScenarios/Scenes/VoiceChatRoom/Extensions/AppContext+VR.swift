//
//  AppContext+VR.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/12/7.
//

private let kvrIsApmOn = "kvrIsApmOn"

extension AppContext {
    
    var isVRApmOn: Bool {
        set{
            UserDefaults.standard.setValue(newValue, forKey: kvrIsApmOn)
        }
        get{
            guard isDebugMode else { return false }
            return UserDefaults.standard.bool(forKey: kvrIsApmOn)
        }
    }
}
