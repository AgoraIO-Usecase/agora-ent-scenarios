//
//  SRServiceModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
class SRCreateRoomInputModel: NSObject {
    @objc var belCanto: String?
    @objc var icon: String?
    @objc var isPrivate: NSNumber?
    @objc var name: String?
    @objc var password: String?
    @objc var soundEffect: String?
    //@objc var userNo: String?
}

class SRCreateRoomOutputModel: NSObject {
    @objc var name: String?
    @objc var roomNo: String?
    @objc var seatsArray: [VLSRRoomSeatModel]?
}

class SRJoinRoomInputModel: NSObject {
    @objc var roomNo: String?
    //@objc var userNo: String?
    @objc var password: String?
}

class SRJoinRoomOutputModel: NSObject {
    @objc var creatorNo: String?
    @objc var seatsArray: [VLSRRoomSeatModel]?
}

class SRChangeMVCoverInputModel: NSObject {
    //@objc var roomNo: String?
    @objc var mvIndex: UInt = 0
    //@objc var userNo: String?
}

// Uncomment the following code block if SRChangeMVCoverOutputModel is required
/*
class SRChangeMVCoverOutputModel: NSObject {
    @objc var name: String?
    @objc var roomNo: String?
    @objc var seatsArray: [VLRoomSeatModel]?
}
*/

class SROnSeatInputModel: NSObject {
    @objc var seatIndex: UInt = 0
}

class SROutSeatInputModel: NSObject {
    @objc var userNo: String?
    @objc var userId: String?
    @objc var userName: String?
    @objc var userHeadUrl: String?
    @objc var seatIndex: NSInteger = 0
}

class SRRemoveSongInputModel: NSObject {
    @objc var songNo: String?
    @objc var objectId: String?
}

class SRJoinChorusInputModel: NSObject {
    @objc var isChorus: Bool = false
    @objc var songNo: String?
}

class SRChooseSongInputModel: NSObject {
    @objc var isChorus: Bool = false
    @objc var songName: String?
    @objc var songNo: String?
    //@objc var songUrl: String?
    @objc var singer: String?
    @objc var imageUrl: String?
    @objc var playCounts: [Int] = [Int]()
}

class SRMakeSongTopInputModel: NSObject {
    @objc var songNo: String?
    @objc var sort: NSNumber?
    @objc var objectId: String?
}

class RankModel: NSObject {
    @objc var userName: String?
    @objc var poster: String?
    @objc var songNum: Int = 0
    @objc var score: Int = 0
}

class SubRankModel : RankModel {
    @objc var userId: String?
    @objc var index: Int = 0
    @objc var count: Int = 0
}

@objc enum SingRelayStatus: Int {
    case idle = 0
    case waiting = 1 // 等待中
    case started = 2 // 已开始
    case ended = 3 // 已结束
}

@objc class SingRelayModel: NSObject {
    @objc var status: SingRelayStatus = .idle
    @objc var objectId: String?
    @objc var name: String?
    @objc var rank: [String: Any] = [:]
}
