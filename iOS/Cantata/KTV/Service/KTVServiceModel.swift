//
//  KTVServiceModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
@objc public class KTVCreateRoomInputModel: NSObject {
    @objc public var belCanto: String?
    @objc public var icon: String?
    @objc public var isPrivate: NSNumber?
    @objc public var name: String?
    @objc public var password: String?
    @objc public var soundEffect: String?
    //@objc var userNo: String?
}

@objc public class KTVCreateRoomOutputModel: NSObject {
    @objc public var name: String?
    @objc public var roomNo: String?
    @objc public var seatsArray: [VLRoomSeatModel]?
}

@objc public class KTVJoinRoomInputModel: NSObject {
    @objc public var roomNo: String?
    //@objc var userNo: String?
    @objc public var password: String?
}

@objc public class KTVJoinRoomOutputModel: NSObject {
    @objc public var creatorNo: String?
    @objc public var seatsArray: [VLRoomSeatModel]?
}

@objc public class KTVChangeMVCoverInputModel: NSObject {
    //@objc var roomNo: String?
    @objc public var mvIndex: UInt = 0
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

@objc public class KTVOnSeatInputModel: NSObject {
    @objc public var seatIndex: UInt = 0
}

@objc public class KTVOutSeatInputModel: NSObject {
    @objc public var userNo: String?
    @objc public var userId: String?
    @objc public var userName: String?
    @objc public var userHeadUrl: String?
    @objc public var seatIndex: NSInteger = 0
}

@objc public class KTVRemoveSongInputModel: NSObject {
    @objc public var songNo: String?
    @objc public var objectId: String?
}

@objc public class KTVJoinChorusInputModel: NSObject {
    @objc var isChorus: Bool = false
    @objc var songNo: String?
}

@objc public class KTVChooseSongInputModel: NSObject {
    @objc var isChorus: Bool = false
    @objc var songName: String?
    @objc var songNo: String?
    //@objc var songUrl: String?
    @objc var singer: String?
    @objc var imageUrl: String?
}

@objc public class KTVMakeSongTopInputModel: NSObject {
    @objc var songNo: String?
    @objc var sort: NSNumber?
    @objc var objectId: String?
}
