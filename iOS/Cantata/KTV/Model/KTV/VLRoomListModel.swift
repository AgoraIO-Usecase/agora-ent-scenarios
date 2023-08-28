//
//  VLRoomListModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import AgoraCommon
@objc public class VLRoomListModel: VLBaseModel {
    
    //@property (nonatomic, copy) NSString *id;
    @objc public var name: String?
    @objc public var isPrivate: Bool = false
    @objc public var password: String?
    @objc public var creator: String?
    @objc public var roomNo: String?
    @objc public var isChorus: String?
    @objc public var bgOption: Int = 0
    @objc public var soundEffect: String?
    @objc public var belCanto: String?
    @objc public var createdAt: Int64 = 0
    @objc public var updatedAt: Int64 = 0
    @objc public var status: String?
    @objc public var deletedAt: String?
    @objc public var roomPeopleNum: String?
    @objc public var icon: String?
    
    ///新加字段 当前房间的创建者
    @objc public var creatorNo: String?
    
    @objc var objectId: String?
}
