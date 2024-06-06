//
//  VLRoomSelSongModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import RTMSyncManager

@objc enum VLSongPlayStatus: Int {
    case idle = 0 //未播放
    case playing = 1 //正在播放
}


class VLRoomSelSongModel: VLBaseModel {
    @objc var songName: String?
    @objc var songNo: String?
    @objc var singer: String?
    @objc var imageUrl: String?
    
    @objc var owner: AUIUserThumbnailInfo?
    
    /// 创建时间
    @objc var createAt: UInt64 = 0
    // 置顶时间
    @objc var pinAt: UInt64 = 0
    
    /// 0 未开始 1.已唱 2.正在唱 3. match完毕
    @objc var status: VLSongPlayStatus = .idle
}
