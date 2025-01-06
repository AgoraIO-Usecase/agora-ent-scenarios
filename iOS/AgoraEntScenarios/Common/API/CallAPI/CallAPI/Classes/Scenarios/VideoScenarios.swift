//
//  VideoScenarios.swift
//  CallAPI
//
//  Created by wushengtao on 2024/3/20.
//

import AgoraRtcKit

public func update1v1VideoEncoder(engine: AgoraRtcEngineKit, roomId: String, userId: Int) {
    //1.API 设置 VideoEncoderConfiguration：
    let config = AgoraVideoEncoderConfiguration()
    config.dimensions = CGSize(width: 720, height: 1280)
    config.frameRate = .fps24
    config.codecType = .H265
    config.degradationPreference = .balanced
    let connection = AgoraRtcConnection(channelId: roomId, localUid: userId)
    engine.setVideoEncoderConfigurationEx(config, connection: connection)
    engine.setParameters("\"che.video.videoCodecIndex\": 2")
}

public func optimize1v1Video(engine: AgoraRtcEngineKit) {
    engine.setVideoScenario(.application1V1Scenario)
}
