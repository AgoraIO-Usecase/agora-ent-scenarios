//
//  VLRoomSelSongModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation

@objc enum VLSongPlayStatus: Int {
    case idle = 0 //未播放
    case playing = 2 //正在播放
}


class VLRoomSelSongModel: VLBaseModel {
    @objc var imageUrl: String?
    
    /// 是否原唱
    @objc var isOriginal: String?
    @objc var singer: String?
    @objc var songName: String?
    @objc var songNo: String?
    //@property (nonatomic, copy) NSString *songUrl;
    /// 歌词
    //@property (nonatomic, copy) NSString *lyric;
    /// 创建时间
    @objc var createAt: Int64 = 0
    // 置顶时间
    @objc var pinAt: Int64 = 0
    
    /// 0 未开始 1.已唱 2.正在唱 3. match完毕
    @objc var status: VLSongPlayStatus = .idle
    // 是谁点的歌
    @objc var userNo: String?
    
    /// 点歌人昵称
    @objc var name: String?
    
    @objc var objectId: String?
    
    @objc func isSongOwner() -> Bool {
        if VLUserCenter.user.id == self.userNo {
            return true
        }
        return false
    }

    @objc func chorusSongId() -> String {
        return "\(self.songNo ?? "")\(self.createAt)"
    }
}
