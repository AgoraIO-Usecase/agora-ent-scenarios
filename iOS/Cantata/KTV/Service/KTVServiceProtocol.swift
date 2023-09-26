//
//  KTVServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import AgoraCommon

@objc public enum KTVServiceNetworkStatus: Int {
    case connecting = 0 // 连接中
    case open // 已打开
    case fail // 失败
    case closed // 已关闭
}

@objc public enum KTVSubscribe: Int {
    case created // 创建
    case deleted // 删除
    case updated // 更新
}

@objc public protocol KTVServiceProtocol: NSObjectProtocol {
    
    // room info
    
    /// 获取房间列表
    /// - Parameters:
    ///   - page: 页码
    ///   - completion: 完成回调
    func getRoomList(with page: UInt, completion: @escaping (Error?, [VLRoomListModel]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func createRoom(with inputModel: KTVCreateRoomInputModel, completion: @escaping (Error?, KTVCreateRoomOutputModel?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func joinRoom(with inputModel: KTVJoinRoomInputModel, completion: @escaping (Error?, KTVJoinRoomOutputModel?) -> Void)
    
    /// 离开房间
    /// - Parameter completion: 完成回调
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    // mic seat
    
    /// 上麦
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func enterSeat(with inputModel: KTVOnSeatInputModel, completion: @escaping (Error?) -> Void)
    
    /// 下麦删除所有歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func leaveSeat(with inputModel: KTVOutSeatInputModel, completion: @escaping (Error?) -> Void)
    
    /// 下麦但是只删除当前歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func leaveSeatWithoutRemoveSong(with inputModel: KTVOutSeatInputModel, completion: @escaping (Error?) -> Void)
    
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
    
    /// 修改麦位对应的分数
    /// - Parameters:
    ///   - score: 对应麦位的实时分数
    ///   - completion: 完成回调
    func updateSeatScoreStatus(with score: Int, completion: @escaping (Error?) -> Void)
    
    /// 修改歌曲结束状态
    /// - Parameters:
    ///   - isEnd: 歌曲结束状态
    ///   - completion: 完成回调
    func updateSongEndStatus(with musicEnd: Bool, inputModel: KTVRemoveSongInputModel, completion: @escaping (Error?) -> Void)
    
    /// 删除选中歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func removeSong(with inputModel: KTVRemoveSongInputModel, completion: @escaping (Error?) -> Void)
    
    /// 获取选择歌曲列表
    /// - Parameter completion: 完成回调
    func getChoosedSongsList(completion: @escaping (Error?, [VLRoomSelSongModel]?) -> Void)
    
    /// 主唱告诉后台当前播放的歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func markSongDidPlay(with inputModel: VLRoomSelSongModel, completion: @escaping (Error?) -> Void)
    
    /// 点歌
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func chooseSong(with inputModel: KTVChooseSongInputModel, completion: @escaping (Error?) -> Void)
    
    /// 置顶歌曲
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func pinSong(with inputModel: KTVMakeSongTopInputModel, completion: @escaping (Error?) -> Void)
    
    // lyrics
    
    /// 加入合唱
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func joinChorus(with inputModel: KTVJoinChorusInputModel, completion: @escaping (Error?) -> Void)
    
    /// 伴唱取消合唱
    /// - Parameter completion: 完成回调
    func coSingerLeaveChorus(completion: @escaping (Error?) -> Void)
    
    /// 当前歌曲合唱改为独唱
    func enterSoloMode()
    
    /// 切换MV封面
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func changeMVCover(with inputModel: KTVChangeMVCoverInputModel, completion: @escaping (Error?) -> Void)
    
    // subscribe
    
    /// 订阅用户变化
    /// - Parameter changedBlock: 变化回调
    func subscribeUserListCountChanged(with changedBlock: @escaping (UInt) -> Void)
    
    /// 用户属性变化
    /// - Parameter changedBlock: 变化回调
    func subscribeUserChanged(with changedBlock: @escaping (KTVSubscribe, VLLoginModel) -> Void)
    
    /// 订阅麦位变化
    /// - Parameter changedBlock: 变化回调
    func subscribeSeatListChanged(with changedBlock: @escaping (KTVSubscribe, VLRoomSeatModel) -> Void)
    
    /// 订阅房间状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeRoomStatusChanged(with changedBlock: @escaping (KTVSubscribe, VLRoomListModel) -> Void)
    
    /// 订阅选中歌曲变化
    /// - Parameter changedBlock: 变化回调
    func subscribeChooseSongChanged(with changedBlock: @escaping (KTVSubscribe, VLRoomSelSongModel, [VLRoomSelSongModel]) -> Void)
    
    /// 订阅网络状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (KTVServiceNetworkStatus) -> Void)
    
    /// 订阅房间过期
    /// - Parameter changedBlock: 变化回调
    func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void)
    
    /// 取消全部订阅
    func unsubscribeAll()
}
