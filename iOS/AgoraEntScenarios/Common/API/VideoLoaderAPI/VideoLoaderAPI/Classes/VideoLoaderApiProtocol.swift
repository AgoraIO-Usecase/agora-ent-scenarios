//
//  VideoLoaderApiProtocol.swift
//  VideoLoaderAPI
//
//  Created by Agora on 2023/7/27.
//

import Foundation
import AgoraRtcKit

/// 加载状态
@objc public enum AnchorState: Int {
    case idle = 0                 //空闲
    case prejoined                //预加载
    case joinedWithVideo          //只加载视频
    case joinedWithAudioVideo     //加载视频和音频
}

/// 初始化配置信息
@objc public class VideoLoaderConfig: NSObject {
    public weak var rtcEngine: AgoraRtcEngineKit?    //rtc engine实例
}

//房间信息
@objc public class AnchorInfo: NSObject {
    public var channelName: String = ""   //频道名
    public var uid: UInt = 0              //频道对应的uid
    public var token: String = ""         //频道对应的token
}

@objc public class VideoCanvasContainer: NSObject {
    public var container: UIView?                          //需要渲染到的view
    public var uid: UInt = 0                               //需要渲染的用户uid
    public var setupMode: AgoraVideoViewSetupMode = .add   //画布模式
//    public var viewIndex: Int = 0
//    public var renderMode: Int = Constants.RENDER_MODE_HIDDEN
}

@objc public protocol IVideoLoaderApiListener: NSObjectProtocol {
    
    /// 状态变更回调
    /// - Parameters:
    ///   - newState: <#newState description#>
    ///   - oldState: <#oldState description#>
    ///   - channelName: <#channelName description#>
    @objc optional func onStateDidChange(newState: AnchorState, oldState: AnchorState, channelName: String)
    
    /// 获取到首帧的回调(耗时计算为从设置joinedWithVideo/joinedWithAudioVideo到出图)
    /// - Parameters:
    ///   - channelName: 房间id
    ///   - elapsed: 耗时
    @objc optional func onFirstFrameRecv(channelName: String, uid: UInt, elapsed: Int64)
}

@objc public protocol IVideoLoaderApi: NSObjectProtocol {
    
    /// 初始化配置
    /// - Parameters:
    ///   - config: <#config description#>
    func setup(config: VideoLoaderConfig)
    
    
    /// preload房间列表
    /// - Parameter preloadAnchorList: preload的list
    /// - Parameter userId: 当前用户的uid
    func preloadAnchor(preloadAnchorList: [AnchorInfo], uid: UInt)
    
    /// 切换状态
    /// - Parameters:
    ///   - newState: 目标状态
    ///   - localUid: 本地用户id
    ///   - anchorInfo: 频道对象
    ///   - tagId: 标记操作频道依赖的标识，例如可能多个房间共用一路频道流，这里通过tagId来增加对单个频道的引用，如果设置多个tagId，清理的时候需要对多个tagId设置为idle，如果不需要，可以设置为nil
    func switchAnchorState(newState: AnchorState, localUid: UInt, anchorInfo: AnchorInfo, tagId: String?)
    
    /// 获取当前频道状态
    /// - Parameter anchorInfo: 频道对象
    /// - Returns: <#description#>
    func getAnchorState(anchorInfo: AnchorInfo) -> AnchorState
    
    /// 获取所有频道的rtc connection map
    /// - Returns: <#description#>
    func getConnectionMap() -> [String: AgoraRtcConnection]
    
    /// 渲染到指定画布上
    /// - Parameters:
    ///   - anchorInfo: 频道对象
    ///   - container: <#container description#>
    func renderVideo(anchorInfo: AnchorInfo, container: VideoCanvasContainer)
    
    /// 清除缓存
    func cleanCache()
 
    /// 添加api代理
    /// - Parameter listener: <#listener description#>
    func addListener(listener: IVideoLoaderApiListener)
    
    /// 移除api代理
    /// - Parameter listener: <#listener description#>
    func removeListener(listener: IVideoLoaderApiListener)
    
    /// 添加RTC代理
    /// - Parameter listener: <#listener description#>
    func addRTCListener(anchorId: String, listener: AgoraRtcEngineDelegate)
    
    /// 移除RTC代理
    /// - Parameter listener: <#listener description#>
    func removeRTCListener(anchorId: String, listener: AgoraRtcEngineDelegate)
    
    /// 获取rtc delegate
    /// - Parameter anchorId: 对应频道的id
    /// - Returns: <#description#>
    func getRTCListener(anchorId: String) -> AgoraRtcEngineDelegate?
}
