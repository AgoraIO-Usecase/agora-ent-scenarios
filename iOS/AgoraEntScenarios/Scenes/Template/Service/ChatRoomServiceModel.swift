//
//  TemplateServiceModel.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

struct TemplateScene {
    enum SubscribeStatus {
        case created
        case deleted
        case updated
    }

    struct JoinResponse {
        var channelName: String
        var userId: String
    }

    struct UsersModel: Codable {
        var userName: String = "User-\(UserInfo.userId)"
        var avatar: String = .init(format: "portrait%02d", Int.random(in: 1...14))
        var userId: String = UserInfo.userId
        //    var status: PKApplyInfoStatus? = .end
        var timestamp: String = "".timeStamp16
        var isEnableVideo: Bool? = false
        var isEnableAudio: Bool? = false
        var objectId: String?
    }

    struct LiveRoomInfo: Codable {
        var roomName: String = ""
        var roomId: String = "\(arc4random_uniform(899999) + 100000)"
        var userId: String = "\(UserInfo.userId)"
        var backgroundId: String = .init(format: "portrait%02d", Int.random(in: 1...2))
        var objectId: String?
        var videoUrl: String? = "https://webdemo-pull-hdl.agora.io/lbhd/sample1.flv"
    }
}
