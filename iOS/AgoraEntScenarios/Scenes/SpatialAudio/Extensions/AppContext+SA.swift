//
//  AppContext+SA.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/12/5.
//

private let kIsApmOn = "kIsApmOn"
import AgoraCommon
extension AppContext {
    
    var isApmOn: Bool {
        set{
            UserDefaults.standard.setValue(newValue, forKey: kIsApmOn)
        }
        get{
            guard isDebugMode else { return false }
            return UserDefaults.standard.bool(forKey: kIsApmOn)
        }
    }
}
