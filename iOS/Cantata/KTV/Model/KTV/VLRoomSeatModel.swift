//
//  VLRoomSeatModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import AgoraCommon
@objc public class VLRoomSeatModel: VLBaseModel {
    /// 是否是房主
    @objc public var isMaster: Bool = false
    /// 头像
    @objc public var headUrl: String?
    /// userNO
    @objc public var userNo: String?
    // rtc uid(rtc join with uid)
    @objc public var rtcUid: String?
    /// 昵称
    @objc public var name: String?
    /// 在哪个座位
    @objc public var seatIndex: Int = 0
    /// 合唱歌曲code
    @objc public var chorusSongCode: String?
    /// 是否自己静音
    @objc public var isAudioMuted: Int = 0
    /// 是否开启视频
    @objc public var isVideoMuted: Int = 0
    /// 新增, 判断当前歌曲是否是自己点的
    @objc public var isOwner: Bool = false

    /// for sync manager
    @objc public var objectId: String?
    
    override init() {
        super.init()
        self.chorusSongCode = ""
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc public func reset(with seatInfo: VLRoomSeatModel?) {
        self.isMaster = seatInfo?.isMaster ?? false
        self.headUrl = seatInfo?.headUrl ?? ""
        self.name = seatInfo?.name ?? ""
        self.userNo = seatInfo?.userNo ?? ""
        self.rtcUid = seatInfo?.rtcUid
        self.isAudioMuted = seatInfo?.isAudioMuted ?? 0
        self.isVideoMuted = seatInfo?.isVideoMuted ?? 0
        self.chorusSongCode = seatInfo?.chorusSongCode
    }

}
