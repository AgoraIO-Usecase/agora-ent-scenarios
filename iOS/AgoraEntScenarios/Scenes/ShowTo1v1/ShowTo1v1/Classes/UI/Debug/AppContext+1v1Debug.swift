//
//  AppContext+1v1Debug.swift
//  Pure1v1
//
//  Created by wushengtao on 2024/3/25.
//

import AgoraCommon
import AgoraRtcKit

extension AppContext {
    func resetDebugConfig(engine: AgoraRtcEngineKit) {
        let info = settingInfoList[0]
        if info.selectedIdx() == 1 {
            for param in kSelectedDumpParam {
                engine.setParameters(param)
            }
        } else {
            for param in kUnselectedDumpParam {
                engine.setParameters(param)
            }
        }
    }
}
