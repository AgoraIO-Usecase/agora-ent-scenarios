//
//  VLRoomSeatModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import RTMSyncManager

@objcMembers
class VLRoomSeatModel: VLBaseModel {
    /// 在哪个座位
    @objc var seatIndex: Int = 0
    //上麦用户信息
    @objc var owner: AUIUserThumbnailInfo = AUIUserThumbnailInfo()
    /// 麦位是否静音
    @objc var isAudioMuted: Bool = true
    /// 麦位是否禁视频
    @objc var isVideoMuted: Bool = true
    
    
    /// 新增, 判断当前歌曲是否是自己点的
    @objc var isSongOwner: Bool = false
    
    override init() {
        super.init()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func reset(seatInfo: VLRoomSeatModel) {
        self.owner = seatInfo.owner
        self.seatIndex = seatInfo.seatIndex
        self.isAudioMuted = seatInfo.isAudioMuted
        self.isVideoMuted = seatInfo.isVideoMuted
    }
    
    override var description: String {
        return "seatIndex: \(seatIndex), userNo: \(owner.userId), isAudioMuted: \(isAudioMuted)"
    }

}
