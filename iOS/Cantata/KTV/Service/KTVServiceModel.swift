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

public class KTVCreateRoomOutputModel: NSObject {
    @objc public var name: String?
    @objc public var roomNo: String?
    @objc public var seatsArray: [VLRoomSeatModel]?
}

public class KTVJoinRoomInputModel: NSObject {
    @objc public var roomNo: String?
    //@objc var userNo: String?
    @objc public var password: String?
}

public class KTVJoinRoomOutputModel: NSObject {
    @objc public var creatorNo: String?
    @objc public var seatsArray: [VLRoomSeatModel]?
}

public class KTVChangeMVCoverInputModel: NSObject {
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

public class KTVOnSeatInputModel: NSObject {
    @objc public var seatIndex: UInt = 0
}

public class KTVOutSeatInputModel: NSObject {
    @objc public var userNo: String?
    @objc public var userId: String?
    @objc public var userName: String?
    @objc public var userHeadUrl: String?
    @objc public var seatIndex: NSInteger = 0
}

public class KTVRemoveSongInputModel: NSObject {
    @objc public var songNo: String?
    @objc public var objectId: String?
}

public class KTVJoinChorusInputModel: NSObject {
    @objc public var isChorus: Bool = false
    @objc public var songNo: String?
}

public class KTVChooseSongInputModel: NSObject {
    @objc public var isChorus: Bool = false
    @objc public var songName: String?
    @objc public var songNo: String?
    //@objc var songUrl: String?
    @objc public var singer: String?
    @objc public var imageUrl: String?
}

public class KTVMakeSongTopInputModel: NSObject {
    @objc public var songNo: String?
    @objc public var sort: NSNumber?
    @objc public var objectId: String?
}
