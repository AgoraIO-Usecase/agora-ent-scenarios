//
//  AIChatViewModel.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/4.
//

import UIKit
import AgoraChat
import AgoraRtcKit
import AgoraCommon
import ZSwiftBaseLib
import SVProgressHUD

public class AIChatViewModel: NSObject {
    
    private var currentTask: DispatchWorkItem?
    
    private let queue = DispatchQueue(label: "com.example.miniConversationsHandlerQueue")
    
    public private(set) var to = ""
    
    public private(set) var chatType = AIChatType.chat
    
    public private(set) weak var driver: IAIChatMessagesListDriver?
    
    public private(set) var chatService: AIChatServiceProtocol?
        
    public private(set) var bot: AIChatBotProfileProtocol?
    
    public private(set) var bots: [AIChatBotProfileProtocol] = []
    
    private let sttChannelId = "aiChat_\(VLUserCenter.user.id)"
    
    @objc public required init(conversationId: String,type: AIChatType) {
        self.to = conversationId
        self.chatType = type
        super.init()
    }
    
    deinit {
        guard let convertService = AppContext.audioTextConvertorService() else { return }
        convertService.removeDelegate(self)
        
        guard let rtcService = AppContext.rtcService() else { return }
        rtcService.leaveChannel(channelName: sttChannelId)
    }
        
    private func setupAudioConvertor() {
        guard let convertService = AppContext.audioTextConvertorService() else { return }
        convertService.addDelegate(self)
    }
    
    private func joinRTCChannel() {
        AppContext.rtcService()?.joinChannel(channelName: sttChannelId)
    }
    
    public func bindDriver(driver: IAIChatMessagesListDriver,bot: AIChatBotProfileProtocol) {
        self.driver = driver
        self.bot = AIChatBotProfile()
        self.bot?.botId = bot.botId
        self.bot?.botName = bot.botName
        self.bot?.botIcon = bot.botIcon
        self.bot?.prompt = bot.prompt
        if bot.voiceId.isEmpty,let iconName = bot.botIcon.fileName.components(separatedBy: ".").first {
            self.bot?.voiceId = AIChatBotImplement.voiceIds[iconName] ?? "female-chengshu"
        } else {
            self.bot?.voiceId = bot.voiceId
        }
        self.bot?.botDescription = bot.botDescription
        
        driver.addActionHandler(actionHandler: self)
        self.chatService = AIChatImplement(conversationId: self.to)
        self.chatService?.addListener(listener: self)
        DispatchQueue.global().asyncAfter(wallDeadline: .now()+1) {
            self.setupAudioConvertor()
            self.joinRTCChannel()
        }
        self.loadMessages()
        if self.chatType == .group {
            if let ext = AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.to)?.ext {
                if let info = ext[self.to] as? [String:Any] {
                    if let botIds = info["botIds"] as? [String] {
                        for botId in botIds {
                            for bot in AIChatBotImplement.commonBot {
                                if bot.botId == botId {
                                    self.bots.append(bot)
                                }
                            }
                            for bot in AIChatBotImplement.customBot {
                                if bot.botId == botId {
                                    self.bots.append(bot)
                                }
                            }
                        }
                    }
                }
            }
            self.bots.first?.selected = true
            self.driver?.refreshBots(bots: self.bots, enable: true)
        }
    }

    @objc public func loadMessages() {
        if let start = self.driver?.firstMessageId {
            self.chatService?.loadMessages(start: start, completion: { [weak self] messages,error in
                if error == nil,let dataSource = messages {
                    self?.driver?.insertMessages(messages: dataSource)
                } else {
                    consoleLogInfo("loadMessages error:\(error?.errorDescription ?? "")", type: .error)
                }
            })
        }
    }
}

extension AIChatViewModel: MessageListViewActionEventsDelegate {
    public func startRecorder() {
        AppContext.audioTextConvertorService()?.startConvertor()
    }
    
    public func stopRecorder() {
        AppContext.audioTextConvertorService()?.flushConvertor()
    }
    
    public func cancelRecorder() {
        AppContext.audioTextConvertorService()?.stopConvertor()
    }

    public func sendMessage(text: String) {
        Task {
            let info = self.fillExtensionInfo()
            let result = await self.chatService?.sendMessage(message: text,extensionInfo: info)
            if let message = result?.0,result?.1 == nil {
                DispatchQueue.main.async {
                    self.insertTimeAlert(message: message)
                    self.driver?.showMessage(message: message)
                }
            } else {
                consoleLogInfo("send message fail:\(result?.1?.errorDescription ?? "")", type: .error)
            }
        }
    }
    
    private func fillExtensionInfo() -> [String:Any] {
        var extensionInfo = Dictionary<String,Any>()
        let count = self.driver?.dataSource.count ?? 0
        if let messages = self.driver?.dataSource.suffix(count >= 10 ? 10:count),let currentBot = self.bot {
            var contexts = [[String:Any]]()
            for message in messages {
                if let body = message.body as? AgoraChatTextMessageBody {
                    if message.direction == .send {
                        contexts.append(["role":"user","name":VLUserCenter.user.name,"content":body.text])
                    } else {
                        contexts.append(["role":"assistant","name":currentBot.botName,"content":body.text])
                    }
                }
            }
            
            if let botId = self.driver?.selectedBot?.botId,self.chatType == .group {
                extensionInfo["ai_chat"] = ["prompt":currentBot.prompt,"context":contexts,"user_meta":["botId":botId]]
            } else {
                extensionInfo["ai_chat"] = ["prompt":currentBot.prompt,"context":contexts]
            }
        }
        return extensionInfo
    }
    
    public func onMessageListPullRefresh() {
        self.loadMessages()
    }
    
}

extension AIChatViewModel: AIChatListenerProtocol {
    
    public func onMessageContentEditedFinished(message: AgoraChatMessage) {
        self.driver?.editMessage(message: message, finished: true)
        self.driver?.refreshBots(bots: self.bots, enable: true)
    }
    
    public func onMessageReceived(messages: [AgoraChatMessage]) {
        for message in messages {
            AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.to)?.markMessageAsRead(withId: message.messageId, error: nil)
            if let bot = message.bot {
                var ext = message.ext ?? [:]
                ext.merge(bot.toDictionary()) { _, new in
                    new
                }
                message.ext = ext
                AgoraChatClient.shared().chatManager?.update(message)
            }
            self.driver?.showMessage(message: message)
            self.delayedTask()
        }
    }
    
    private func insertTimeAlert(message: AgoraChatMessage) {
        if let lastMessage = self.driver?.dataSource.last,lastMessage.messageId == message.messageId {
            let interval = abs(message.timestamp - lastMessage.timestamp) // 计算时间戳之间的差值
            let minutes = interval / 60 // 将差值转换为分钟
            if minutes > 20 {
                let timeMessage = AgoraChatMessage(conversationID: message.conversationId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"\(UInt64(Date().timeIntervalSince1970*1000))"])
                AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.to)?.insert(message, error: nil)
                self.driver?.showMessage(message: timeMessage)
            }
        }
    }
    
    public func onMessageContentEdited(message: AgoraChatMessage) {
        self.driver?.editMessage(message: message, finished: false)
        self.driver?.refreshBots(bots: self.bots, enable: false)
        self.delayedTask()
    }
    
    func delayedTask() {
        self.currentTask?.cancel()
        // Create Task
        let task = DispatchWorkItem { [weak self] in
            self?.performDelayTask()
        }
        self.currentTask = task
        self.queue.asyncAfter(deadline: .now() + 5, execute: task)
    }
    
    func performDelayTask() {
        if let lastMessageId = self.driver?.dataSource.last?.messageId,let message = AgoraChatClient.shared().chatManager?.getMessageWithMessageId(lastMessageId) {
            DispatchQueue.main.async {
                self.driver?.editMessage(message: message, finished: true)
                self.driver?.refreshBots(bots: self.bots, enable: true)
            }
        }
    }
}

extension AIChatViewModel: AIChatAudioTextConvertorDelegate {
    func convertResultHandler(result: String, error: (any Error)?) {
        if error == nil {
            print("conver message: \(result)")
            self.sendMessage(text: result)
        } else {
            SVProgressHUD.showError(withStatus: "出了点问题，请重试")
        }
    }
    
    func convertAudioVolumeHandler(volume: UInt, totalVolume: Int, uid: UInt) {
        self.driver?.refreshRecordIndicator(volume: Int(volume))
    }
}
