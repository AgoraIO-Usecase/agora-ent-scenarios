//
//  VoiceRoomAudiencesEntity.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/9/18.
//

import Foundation
import KakaJSON

@objc public class SAAudiencesEntity: NSObject, Convertible {
    var total: Int?

    var cursor: String?

    var members: [SAUser]?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc public class SAApplyEntity: NSObject, Convertible {
    /*
     "total": 0,
     "cursor": null,
     "apply_list": [
     {
     "index": null,
     "member": {},
     "created_at": 0
     }
     ]
     */
    var total: Int?

    var cursor: String?

    var apply_list: [SAApply]?

    var members: [SAUser]?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc public class SAApply: NSObject, Convertible {
    
    var index: Int?

    var member: SAUser?

    var created_at: UInt64?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}
