//
//  KTVServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import RTMSyncManager

@objc protocol KTVServiceListenerProtocol: NSObjectProtocol {
    
    /// 房间过期
    func onRoomDidExpire()
    
    /// 房间被销毁
    func onRoomDidDestroy()
    
    /// 房间用户数量变更
    /// - Parameter userCount: 用户数
    func onUserCountUpdate(userCount: UInt)
    
    /// 麦位更新
    /// - Parameter seat: 麦位对象
    func onMicSeatSnapshot(seat: [String: VLRoomSeatModel])
    
    /// 麦位更新
    /// - Parameter seat: 麦位对象
    func onUserSeatUpdate(seat: VLRoomSeatModel)
    
    /// 有成员上麦（主动上麦/房主抱人上麦）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - user: 用户信息
    func onUserEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo)
    
    /// 有成员下麦（主动下麦/房主踢人下麦）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - user: 用户信息
    func onUserLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo)
    
    /// 房主对麦位进行了静音/解禁
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isMute:麦克风开关状态
    func onSeatAudioMute(seatIndex: Int, isMute: Bool)

    /// 房主对麦位摄像头进行禁用/启用
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isMute: 摄像头开关状态
    func onSeatVideoMute(seatIndex: Int, isMute: Bool)
    
    /// 更新所有歌曲回调（例如pin）
    /// - Parameter song: 歌曲列表
    func onChosenSongListDidChanged(songs: [VLRoomSelSongModel])
    
    /// 合唱者加入
    /// - Parameter chorus: 加入的合唱者信息
    func onChoristerDidEnter(chorister: KTVChoristerModel)
    
    /// 合唱者离开
    /// - Parameter chorister: 离开的合唱者
    func onChoristerDidLeave(chorister: KTVChoristerModel)
    
}

@objc protocol KTVServiceProtocol: NSObjectProtocol {
    // room info
    
    /// 获取房间列表
    /// - Parameters:
    ///   - page: 页码
    ///   - completion: 完成回调
    func getRoomList(page: UInt, completion: @escaping (Error?, [AUIRoomInfo]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func createRoom(inputModel: KTVCreateRoomInfo, completion: @escaping (Error?, AUIRoomInfo?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - password: 密码
    ///   - completion: 完成回调
    func joinRoom(roomId: String, password: String, completion: @escaping (Error?) -> Void)
    
    /// 离开房间
    /// - Parameter completion: 完成回调
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    // mic seat
    
    /// 上麦
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - completion: 完成回调
    func enterSeat(seatIndex: NSNumber?, completion: @escaping (Error?) -> Void)
    
    /// 下麦
    /// - Parameters:
    ///   - completion: 完成回调
    func leaveSeat(completion: @escaping (Error?) -> Void)
    
    /// 踢人下麦
    /// - Parameters:
    ///   - seatIndex: 麦位index
    ///   - completion: 完成回调
    func kickSeat(seatIndex: Int, completion: @escaping (NSError?) -> ())
    
    /// 设置麦位声音
    /// - Parameters:
    ///   - muted: 是否关闭声音，YES: 关闭声音，NO: 开启声音
    ///   - completion: 完成回调
    func updateSeatAudioMuteStatus(muted: Bool, completion: @escaping (Error?) -> Void)
    
    /// 打开麦位摄像头
    /// - Parameters:
    ///   - muted: 是否关闭摄像头，YES: 关闭摄像头，NO: 开启摄像头
    ///   - completion: 完成回调
    func updateSeatVideoMuteStatus(muted: Bool, completion: @escaping (Error?) -> Void)
    
    // choose songs
    
    /// 删除选中歌曲
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 完成回调
    func removeSong(songCode: String, completion: @escaping (Error?) -> Void)
    
    /// 获取选择歌曲列表
    /// - Parameter completion: 完成回调
    func getChoosedSongsList(completion: @escaping (Error?, [VLRoomSelSongModel]?) -> Void)
    
    /// 主唱告诉后台当前播放的歌曲
    /// - Parameters:
    ///   - songCode: 房间id
    ///   - completion: 完成回调
    func markSongDidPlay(songCode: String, completion: @escaping (Error?) -> Void)
    
    /// 点歌
    /// - Parameters:
    ///   - inputModel: 输入模型
    ///   - completion: 完成回调
    func chooseSong(inputModel: KTVChooseSongInputModel, completion: @escaping (Error?) -> Void)
    
    /// 置顶歌曲
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 完成回调
    func pinSong(songCode: String, completion: @escaping (Error?) -> Void)
    
    /// 加入合唱
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 完成回调
    func joinChorus(songCode: String, completion: @escaping (Error?) -> Void)
    
    /// 伴唱取消合唱
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 完成回调
    func leaveChorus(songCode: String, completion: @escaping (Error?) -> Void)
    
    /// 订阅监听者
    /// - Parameter listener: 监听者对象
    func subscribe(listener: KTVServiceListenerProtocol?)
    
    /// 获取当前房间使用时长
    /// - Parameter channelName: 频道名
    /// - Returns: 使用时长，单位ms
    func getCurrentDuration(channelName: String) -> UInt64
}
