//
//  SRServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import AgoraRtmKit

@objc enum SRServiceNetworkStatus: Int {
    case connecting = 0 // 连接中
    case open // 已打开
    case fail // 失败
    case closed // 已关闭
}

@objc enum SRSubscribe: Int {
    case created // 创建
    case deleted // 删除
    case updated // 更新
    case failed //失败
}

@objc protocol SRServiceProtocol: NSObjectProtocol {
    
    // room info
    
    /// 获取房间列表
    /// - Parameters:
    ///   - page: 页码
    ///   - completion: 完成回调
    func getRoomList(with page: UInt, completion: @escaping (Error?, [VLSRRoomListModel]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func createRoom(with inputModel: SRCreateRoomInputModel, completion: @escaping (Error?, SRCreateRoomOutputModel?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func joinRoom(with inputModel: SRJoinRoomInputModel, completion: @escaping (Error?, SRJoinRoomOutputModel?) -> Void)
    
    /// 离开房间
    /// - Parameter completion: 完成回调
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    // mic seat
    
    /// 上麦
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func enterSeat(with inputModel: SROnSeatInputModel, completion: @escaping (Error?) -> Void)
    
    /// 下麦
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func leaveSeat(with inputModel: SROutSeatInputModel, completion: @escaping (Error?) -> Void)
    
    /// 设置麦位声音
    /// - Parameters:
    ///   - muted: 是否关闭声音，YES: 关闭声音，NO: 开启声音
    ///   - completion: 完成回调
    func updateSeatAudioMuteStatus(with muted: Bool, completion: @escaping (Error?) -> Void)
    
    /// 打开麦位摄像头
    /// - Parameters:
    ///   - muted: 是否关闭摄像头，YES: 关闭摄像头，NO: 开启摄像头
    ///   - completion: 完成回调
    func updateSeatVideoMuteStatus(with muted: Bool, completion: @escaping (Error?) -> Void)
    
    // choose songs
    
    /// 删除选中歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func removeSong(with inputModel: SRRemoveSongInputModel, completion: @escaping (Error?) -> Void)
    
    /// 获取选择歌曲列表
    /// - Parameter completion: 完成回调
    func getChoosedSongsList(completion: @escaping (Error?, [VLSRRoomSelSongModel]?) -> Void)
    
    /// 主唱告诉后台当前播放的歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func markSongDidPlay(with inputModel: VLSRRoomSelSongModel, completion: @escaping (Error?) -> Void)
    
    /// 点歌
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func chooseSong(with inputModel: SRChooseSongInputModel, completion: @escaping (Error?) -> Void)
    
    /// 置顶歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func pinSong(with inputModel: SRMakeSongTopInputModel, completion: @escaping (Error?) -> Void)
    
    // lyrics
    
    /// 加入合唱
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func joinChorus(with inputModel: SRJoinChorusInputModel, completion: @escaping (Error?) -> Void)
    
    /// 伴唱取消合唱
    /// - Parameter completion: 完成回调
    func coSingerLeaveChorus(completion: @escaping (Error?) -> Void)
    
    /// 当前歌曲合唱改为独唱
    func enterSoloMode()
    
    /// 切换MV封面
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func changeMVCover(with inputModel: SRChangeMVCoverInputModel, completion: @escaping (Error?) -> Void)
    
    // subscribe
    
    /// 订阅用户变化
    /// - Parameter changedBlock: 变化回调
    func subscribeUserListCountChanged(with changedBlock: @escaping (UInt) -> Void)
    
    /// 用户属性变化
    /// - Parameter changedBlock: 变化回调
    func subscribeUserChanged(with changedBlock: @escaping (SRSubscribe, VLLoginModel) -> Void)
    
    /// 订阅麦位变化
    /// - Parameter changedBlock: 变化回调
    func subscribeSeatListChanged(with changedBlock: @escaping (SRSubscribe, VLSRRoomSeatModel) -> Void)
    
    /// 订阅房间状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeRoomStatusChanged(with changedBlock: @escaping (SRSubscribe, VLSRRoomListModel) -> Void)
    
    /// 订阅选中歌曲变化
    /// - Parameter changedBlock: 变化回调
    func subscribeChooseSongChanged(with changedBlock: @escaping (SRSubscribe, VLSRRoomSelSongModel, [VLSRRoomSelSongModel]) -> Void)
    
    /// 订阅网络状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (SRServiceNetworkStatus) -> Void)
    
    /// 订阅房间过期
    /// - Parameter changedBlock: 变化回调
    func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void)
    
    /// 取消全部订阅
    func unsubscribeAll()
    
    func innerSingRelayInfo(_ completion: @escaping (Error?, SingRelayModel?) -> Void)
    
    func innerAddSingRelayInfo(_ model: SingRelayModel, completion: @escaping (Error?) -> Void)
    
    func innerUpdateSingRelayInfo(_ model: SingRelayModel, completion: @escaping (Error?) -> Void)
    
    func innerSubscribeSingRelayInfo(completion: @escaping (SRSubscribe, SingRelayModel?, Error?) -> Void)
}
