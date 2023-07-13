//
//  KTVServiceModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
class KTVCreateRoomInputModel: NSObject {
    @objc var belCanto: String?
    @objc var icon: String?
    @objc var isPrivate: NSNumber?
    @objc var name: String?
    @objc var password: String?
    @objc var soundEffect: String?
    //@objc var userNo: String?
}

class KTVCreateRoomOutputModel: NSObject {
    @objc var name: String?
    @objc var roomNo: String?
    @objc var seatsArray: [VLRoomSeatModel]?
}

class KTVJoinRoomInputModel: NSObject {
    @objc var roomNo: String?
    //@objc var userNo: String?
    @objc var password: String?
}

class KTVJoinRoomOutputModel: NSObject {
    @objc var creatorNo: String?
    @objc var seatsArray: [VLRoomSeatModel]?
}

class KTVChangeMVCoverInputModel: NSObject {
    //@objc var roomNo: String?
    @objc var mvIndex: UInt = 0
    //@objc var userNo: String?
}

// Uncomment the following code block if KTVChangeMVCoverOutputModel is required
/*
class KTVChangeMVCoverOutputModel: NSObject {
    @objc var name: String?
    @objc var roomNo: String?
    @objc var seatsArray: [VLRoomSeatModel]?
}
*/

class KTVOnSeatInputModel: NSObject {
    @objc var seatIndex: UInt = 0
}

class KTVOutSeatInputModel: NSObject {
    @objc var userNo: String?
    @objc var userId: String?
    @objc var userName: String?
    @objc var userHeadUrl: String?
    @objc var seatIndex: NSInteger = 0
}

class KTVRemoveSongInputModel: NSObject {
    @objc var songNo: String?
    @objc var objectId: String?
}

class KTVJoinChorusInputModel: NSObject {
    @objc var isChorus: Bool = false
    @objc var songNo: String?
}

class KTVChooseSongInputModel: NSObject {
    @objc var isChorus: Bool = false
    @objc var songName: String?
    @objc var songNo: String?
    //@objc var songUrl: String?
    @objc var singer: String?
    @objc var imageUrl: String?
}

class KTVMakeSongTopInputModel: NSObject {
    @objc var songNo: String?
    @objc var sort: NSNumber?
    @objc var objectId: String?
}
