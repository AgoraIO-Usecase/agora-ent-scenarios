//
//  VLRoomSelSongModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import AgoraCommon
@objc public enum VLSongPlayStatus: Int {
    case idle = 0 //未播放
    case playing = 2 //正在播放
}


@objc public class VLRoomSelSongModel: VLBaseModel {
    @objc public var imageUrl: String?
    
    /// 是否原唱
    @objc public var isOriginal: String?
    @objc public var singer: String?
    @objc public var songName: String?
    @objc public var songNo: String?
    //@property (nonatomic, copy) NSString *songUrl;
    /// 歌词
    //@property (nonatomic, copy) NSString *lyric;
    /// 创建时间
    @objc public var createAt: Int64 = 0
    // 置顶时间
    @objc public var pinAt: Int64 = 0
    
    /// 0 未开始 1.已唱 2.正在唱 3. match完毕
    @objc public var status: VLSongPlayStatus = .idle
    // 是谁点的歌
    @objc public var userNo: String?
    
    /// 点歌人昵称
    @objc public var name: String?
    
    @objc public var objectId: String?
    
    @objc public var musicEnded: Bool = false
    
    @objc public func isSongOwner() -> Bool {
        if VLUserCenter.user.id == self.userNo {
            return true
        }
        return false
    }

    @objc public func chorusSongId() -> String {
        return "\(self.songNo ?? "")\(self.createAt)"
    }
}
