//
//  VideoLoaderApiProtocol.swift
//  VideoLoaderAPI
//
//  Created by Agora on 2023/7/27.
//

import Foundation
import AgoraRtcKit

/// 加载状态
@objc public enum RoomStatus: Int {
    case idle = 0    //空闲
    case prejoined   //预加载
    case joined      //已加载
}


/// 初始化配置信息
public class VideoLoaderConfig: NSObject {
    public var rtcEngine: AgoraRtcEngineKit!    //rtc engine实例
    public var userId: UInt = 0
}

//房间信息
public class RoomInfo: NSObject {
    public var channelName: String = ""   //频道名
    public var uid: UInt = 0              //频道对应的uid
    public var token: String = ""         //频道对应的token
}

public class VideoCanvasContainer: NSObject {
    public var container: UIView?
    public var uid: UInt = 0
//    public var viewIndex: Int = 0
//    public var renderMode: Int = Constants.RENDER_MODE_HIDDEN
}

@objc public protocol IVideoLoaderApiListener: NSObjectProtocol {
    @objc optional func onStateDidChange(newState: RoomStatus, oldState: RoomStatus, channelName: String)
    
    @objc optional func debugInfo(_ message: String)
    @objc optional func debugWarning(_ message: String)
    @objc optional func debugError(_ message: String)
}

@objc public protocol IVideoLoaderApi: NSObjectProtocol {
    
    /// 初始化配置
    /// - Parameters:
    ///   - config: <#config description#>
    func setup(config: VideoLoaderConfig)
    
    /// preload房间列表
    /// - Parameter preloadRoomList: <#preloadRoomList description#>
    func preloadRoom(preloadRoomList: [RoomInfo])
    
    /// 切换状态
    /// - Parameters:
    ///   - newState: 目标状态
    ///   - roomInfo: 频道对象
    ///   - tagId: 标记操作频道依赖的标识，例如可能多个房间共用一路频道流，这里通过tagId来增加对单个频道的引用，如果设置多个tagId，清理的时候需要对多个tagId设置为idle，如果不需要，可以设置为nil
    func switchRoomState(newState: RoomStatus, roomInfo: RoomInfo, tagId: String?)
    
    /// 获取当前频道状态
    /// - Parameter roomInfo: <#roomInfo description#>
    /// - Returns: <#description#>
    func getRoomState(roomInfo: RoomInfo) -> RoomStatus
    
    func getConnectionMap() -> [String: AgoraRtcConnection]
    
    /// 渲染到指定画布上
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - container: <#container description#>
    func renderVideo(roomInfo: RoomInfo, container: VideoCanvasContainer)
    
    /// 清除缓存
    func cleanCache()
    
    /// 退出某个频道外的其他频道
    func leaveChannelWithout(roomId: String)
    
    /// 添加api代理
    /// - Parameter listener: <#listener description#>
    func addListener(listener: IVideoLoaderApiListener)
    
    /// 移除api代理
    /// - Parameter listener: <#listener description#>
    func removeListener(listener: IVideoLoaderApiListener)
    
    /// 添加RTC代理
    /// - Parameter listener: <#listener description#>
    func addRTCListener(roomId: String, listener: AgoraRtcEngineDelegate)
    
    /// 移除RTC代理
    /// - Parameter listener: <#listener description#>
    func removeRTCListener(roomId: String, listener: AgoraRtcEngineDelegate)
    
    func getRTCListener(roomId: String) -> AgoraRtcEngineDelegate?
}
