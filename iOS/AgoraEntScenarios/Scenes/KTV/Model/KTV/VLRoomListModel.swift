//
//  VLRoomListModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation

class VLRoomListModel: VLBaseModel {
    
    //@property (nonatomic, copy) NSString *id;
    @objc var name: String?
    @objc var isPrivate: Bool = false
    @objc var password: String?
    @objc var creator: String?
    @objc var roomNo: String?
    @objc var isChorus: String?
    @objc var bgOption: Int = 0
    @objc var soundEffect: String?
    @objc var belCanto: String?
    @objc var createdAt: Int64 = 0
    @objc var updatedAt: Int64 = 0
    @objc var status: String?
    @objc var deletedAt: String?
    @objc var roomPeopleNum: String?
    @objc var icon: String?
    
    ///新加字段 当前房间的创建者
    @objc var creatorNo: String?
    @objc var creatorAvatar: String?
    @objc var creatorName: String?
    
    @objc var objectId: String?
}
