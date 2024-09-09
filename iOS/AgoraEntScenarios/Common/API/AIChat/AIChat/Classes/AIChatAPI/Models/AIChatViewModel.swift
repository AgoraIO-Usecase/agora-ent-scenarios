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
    
    @objc public required init(conversationId: String,type: AIChatType) {
        self.to = conversationId
        self.chatType = type
        super.init()
    }
    
    deinit {
        AIChatAudioTextConvertorService.shared.removeDelegate(self)
    }
    
    public func setupAudioConvertor() {
        let engine = AgoraRtcEngineKit.sharedEngine(withAppId: AppContext.shared.appId,delegate: nil)
        engine.setParameters("{\"che.audio.sf.nsEnable\":1}")
        engine.setParameters("{\"che.audio.sf.ainsToLoadFlag\":1}")
        engine.setParameters("{\"che.audio.sf.nsngAlgRoute\":12}")
        engine.setParameters("{\"che.audio.sf.nsngPredefAgg\":10}")
        AIChatAudioTextConvertorService.shared.run(appId: AppContext.shared.hyAppId, apiKey: AppContext.shared.hyAPIKey, apiSecret: AppContext.shared.hyAPISecret, convertType: .normal, agoraRtcKit: engine)
        AIChatAudioTextConvertorService.shared.addDelegate(self)
        
        AIChatAudioTextConvertorService.shared.setAudioVolumeIndication(interval: 200, smooth: 3)
    }
    
    public func bindDriver(driver: IAIChatMessagesListDriver,bot: AIChatBotProfileProtocol) {
        self.driver = driver
        self.bot = AIChatBotProfile()
        self.bot?.botId = bot.botId
        self.bot?.botName = bot.botName
        self.bot?.botIcon = bot.botIcon
        self.bot?.prompt = bot.prompt
        self.bot?.botDescription = bot.botDescription
        
        driver.addActionHandler(actionHandler: self)
        self.chatService = AIChatImplement(conversationId: self.to)
        self.chatService?.addListener(listener: self)
        DispatchQueue.main.asyncAfter(wallDeadline: .now()+1) {
            self.setupAudioConvertor()
        }
        self.loadMessages()
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
        AIChatAudioTextConvertorService.shared.startConvertor()
    }
    
    public func stopRecorder() {
        AIChatAudioTextConvertorService.shared.stopConvertor()
    }
    
    public func cancelRecorder() {
        AIChatAudioTextConvertorService.shared.flushConvertor()
    }
    
    
    public func sendMessage(text: String) {
        let info = self.fillExtensionInfo()
        Task {
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
            
            extensionInfo["ai_chat"] = ["prompt":currentBot.prompt,"context":contexts]
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
    }
    
    public func onMessageReceived(messages: [AgoraChatMessage]) {
        for message in messages {
            AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.to)?.markMessageAsRead(withId: message.messageId, error: nil)
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
            }
        }
    }
}

extension AIChatViewModel: AIChatAudioTextConvertorDelegate {
    
    func convertResultHandler(result: String, error: (any Error)?) {
        if error == nil {
            self.sendMessage(text: result)
        } else {
            SVProgressHUD.showError(withStatus: "出了点问题，请重试")
        }
    }
    
    func convertAudioVolumeHandler(totalVolume: Int) {
        self.driver?.refreshRecordIndicator(volume: totalVolume)
    }
}
