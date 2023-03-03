//
//  VoiceServiceModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2022/11/15.
//
import Foundation
import KakaJSON

class VoiceBaseRoomModel: NSObject, Convertible  {
    var roomName: String?
    var isPrivate: Bool = false
    var password: String = ""
    var soundEffect: Int = 0
    
    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

class VoiceUserModel: NSObject, Convertible  {
    var uid: String?
    var chatUid: String?
    var name: String?
    var portrait: String?
    var rtcUid: Int = 0
    var micIndex: Int = 0
    
    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
    
}

class VoiceUserRankModel: NSObject, Convertible  {
    var name: String?
    var portrait: String = ""
    var amount: Int = 0
    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
    
}

class VoiceRoomDetailModel: VoiceBaseRoomModel {
    
    var owner: VoiceUserModel?
    var roomId: String = ""
    var clickCount: Int = 0
    var roomType: Int = 0
    var channelId: String = ""
    var chatroomId: String = ""
    var createdAt: UInt64  = 0
    var memberCount: Int = 0 // 房间人数
    var giftAmount: Int = 0 // 礼物数
    var announcement: String?
    var useRobot: Bool = false
    var robotVolume: UInt = 50
    
}

