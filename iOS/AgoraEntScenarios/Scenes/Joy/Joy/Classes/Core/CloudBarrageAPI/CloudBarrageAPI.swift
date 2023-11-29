//
//  CloudBarrageAPI.swift
//  Joy
//
//  Created by wushengtao on 2023/11/29.
//

import Foundation
import AgoraRtcKit

public struct CloudBarrageConfig {
    var appId: String?
    var host: String?
    var engine: AgoraRtcEngineKit?
}

@objcMembers
public class CloudBarrageAPI: NSObject {
    private var apiConfig: CloudBarrageConfig?
    public let shared: CloudBarrageAPI = CloudBarrageAPI()
    
    private lazy var throttler: AUIThrottler = AUIThrottler()
    private var msgId: Int = 0
    private var streamId: Int = 0
    private var msgArray = [Agora_Pb_Rctrl_RctrlMsg]()
    
    public func setup(apiConfig: CloudBarrageConfig) {
        self.apiConfig = apiConfig
        createDataStream()
    }
}

// MARK: public
extension CloudBarrageAPI {
    
    /// 获取游戏列表信息
    /// - Parameters:
    ///   - pageNum: 页数
    ///   - pageSize: 每页显示数量
    ///   - completion: <#completion description#>
    public func getGameList(pageNum: Int = 1,
                            pageSize: Int = 10,
                            completion: @escaping (NSError?, [CloudGameInfo]?) -> Void) {
        
    }
    
    
    /// 获取游戏详情信息
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - completion: <#completion description#>
    public func getGameInfo(gameId: String, completion: @escaping (NSError?, CloudGameDetailInfo?) -> Void) {
        
    }
    
    /// 获取礼物列表
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - completion: <#completion description#>
    public func getGiftInfo(gameId: String, completion: @escaping (NSError?, [CloudGameInfo]?) -> Void) {
        
    }
    
    
    /// 发送评论
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - commentConfig: 评论内容
    ///   - completion: <#completion description#>
    public func sendComment(gameId: String,
                            commentConfig: CloudGameSendCommentConfig,
                            completion: @escaping (NSError?) -> Void) {
        
    }
    
    
    /// 发送点赞
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - likeConfig: 点赞内容
    ///   - completion: <#completion description#>
    public func sendLike(gameId: String,
                         likeConfig: CloudGameSendLikeConfig,
                         completion: @escaping (NSError?, [CloudGameInfo]?) -> Void) {
    }
    
    
    /// 开始游戏
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - config: 开始内容
    ///   - completion: <#completion description#>
    public func startGame(gameId: String,
                          config: CloudGameStartConfig,
                          completion: @escaping ((NSError?, String?)->Void)) {
        
    }
    
    
    /// 结束游戏
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - vid: agora app对应的vid
    ///   - roomId: 主播房间ID
    ///   - openId: 主播ID
    ///   - taskId:  启动游戏的任务ID，通过startGame返回
    ///   - completion: <#completion description#>
    public func endGame(gameId:String,
                        vid: String,
                        roomId: String,
                        openId: String,
                        taskId: String,
                        completion: @escaping ((NSError?)->Void)) {
        
    }
    
    /*
     UserGameStatusSchedule UserGameStatus = "schedule"
     UserGameStatusScheduled UserGameStatus = "scheduled"
     UserGameStatusStarting UserGameStatus = "starting"
     UserGameStatusStartFailed UserGameStatus = "start_failed"
     UserGameStatusStarted UserGameStatus = "started"
     UserGameStatusStopping UserGameStatus = "stopping"
     UserGameStatusStopped UserGameStatus = "stopped"
     */
    /// 查询游戏状态
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - taskId: 启动游戏的任务ID，通过startGame返回
    ///   - completion: <#completion description#>
    public func getGameStatus(gameId:String,
                              taskId: String,
                              completion: @escaping ((_ status: String?)->Void)) {
        
    }
    
    public func updateGameToken(gameId:String,
                                tokenConfig: CloudGameTokenConfig,
                                completion: @escaping ((NSError?)->Void)) {
        
    }
    
    
     func sendKeyboardEvent(type: Agora_Pb_Rctrl_KeyboardEventType, key:Character) {
        msgId += 1
        let currentDate = Date()
        var event = Agora_Pb_Rctrl_KeyboardEventMsg()
        event.vkey = UInt32(key.asciiValue ?? 0)
        event.keyboardEvent = UInt32(type.rawValue)
        event.state = type == .keyboardEventKeyDown ? 1 : 0xC0000001
        
        var msg = Agora_Pb_Rctrl_RctrlMsg()
        msg.type = .keyboardEventType
        msg.msgID = UInt32(msgId)
        msg.timestamp = UInt64(currentDate.timeIntervalSince1970)
        if let eventData = try? event.serializedData() {
            msg.payload = eventData
        }
        
        sendEventMessage(msg: msg)
    }
    
     func sendMouseEvent(type: Agora_Pb_Rctrl_MouseEventType,
                               point: CGPoint,
                               gameViewSize: CGSize) {
        msgId += 1
        let currentDate = Date()
        
        let x = (Int(point.x) << 16) / Int(gameViewSize.width)
        let y = (Int(point.y) << 16) / Int(gameViewSize.height)
        var event = Agora_Pb_Rctrl_MouseEventMsg()
        event.mouseEvent = UInt32(type.rawValue)
        event.x = Int32(x)
        event.y = Int32(y)
        event.extData = 1
        
        var msg = Agora_Pb_Rctrl_RctrlMsg()
        msg.type = .mouseEventType
        msg.msgID = UInt32(msgId)
        msg.timestamp = UInt64(currentDate.timeIntervalSince1970)
        if let eventData = try? event.serializedData() {
            msg.payload = eventData
        }
        
        sendEventMessage(msg: msg)
    }
}

extension CloudBarrageAPI {
    private func sendEventMessage(msg: Agora_Pb_Rctrl_RctrlMsg) {
        msgArray.append(msg)
        
        throttler.triggerLastEvent(after: 0.03) {
            self.sendDataStream()
        }
    }
    
    private func createData() -> Data? {
        guard msgArray.count > 0 else { return nil }
        var msgs = Agora_Pb_Rctrl_RctrlMsges()
        msgs.msges = msgArray
        let data = try? msgs.serializedData()
        return data
    }
    
    private func createDataStream() {
        guard let engine = self.apiConfig?.engine else {
            joyError("createDataStream fail: engine == nil")
            return
        }
        let config = AgoraDataStreamConfig()
        config.ordered = true
        config.syncWithAudio = true
        let ret = engine.createDataStream(&streamId, config: config)
        if ret == 0 { return }
        joyWarn("createStream fail! ret:\(ret), streamId: \(streamId)")
    }
    
    private func sendDataStream() {
        guard let engine = self.apiConfig?.engine else {
            joyError("sendDataStream fail: engine == nil")
            return
        }
        guard let data = self.createData() else { return }
        self.msgArray.removeAll()
        let ret = engine.sendStreamMessage(streamId, data: data)
        if ret == 0 { return }
        joyWarn("sendStreamMessage fail! ret:\(ret), streamId: \(streamId)")
    }
}
