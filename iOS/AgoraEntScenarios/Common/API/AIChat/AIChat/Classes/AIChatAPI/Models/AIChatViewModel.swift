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
        aichatPrint("deinit AIChatViewModel", context: "AIChatViewModel")
    }
        
    private func setupAudioConvertor() {
        guard let convertService = AppContext.audioTextConvertorService() else { return }
        convertService.addDelegate(self)
    }
    
    private func joinRTCChannel() {
        AppContext.rtcService()?.joinChannel(channelName: sttChannelId)
        AppContext.rtcService()?.addDelegate(channelName: sttChannelId, delegate: self)
//        AppContext.rtcService()?.updateRole(channelName: sttChannelId, role: .broadcaster)
    }
    
    private func leaveRTCChannel() {
        guard let rtcService = AppContext.rtcService() else { return }
        rtcService.leaveChannel(channelName: sttChannelId)
        rtcService.removeDelegate(channelName: sttChannelId, delegate: self)
    }
    
    private func teardownAudioConvertor() {
        guard let convertService = AppContext.audioTextConvertorService() else { return }
        convertService.removeDelegate(self)
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
//        DispatchQueue.global().asyncAfter(wallDeadline: .now()+1) {
            self.setupAudioConvertor()
            self.joinRTCChannel()
//        }
        self.loadMessages()
        self.refreshGroupBots()
    }
    
    public func unbindDriver() {
        self.teardownAudioConvertor()
        self.leaveRTCChannel()
        AppContext.destoryConvertorService()
    }
    
    func refreshGroupBots() {
        if self.chatType == .group {
            let selectedId = self.bots.first { $0.selected }?.botId ?? ""
            if let ext = AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.to)?.ext {
                self.bots.removeAll()
                if let info = ext[self.to] as? [String:Any] {
                    if let botIds = info["botIds"] as? [String] {
                        for botId in botIds {
                            for bot in AIChatBotImplement.commonBot {
                                if bot.botId == botId {
                                    if selectedId == botId {
                                        bot.selected = true
                                    }
                                    self.bots.append(bot)
                                }
                                
                            }
                            for bot in AIChatBotImplement.customBot {
                                if bot.botId == botId {
                                    if selectedId == botId {
                                        bot.selected = true
                                    }
                                    self.bots.append(bot)
                                }
                            }
                        }
                    }
                }
            }
            if selectedId.isEmpty {
                self.bots.first?.selected = true
            }
            self.driver?.refreshBots(bots: self.bots, enable: true)
        }
    }

    @objc public func loadMessages() {
        if let start = self.driver?.firstMessageId {
            self.chatService?.loadMessages(start: start, completion: { [weak self] messages,error in
                if error == nil,let dataSource = messages {
                    self?.driver?.insertMessages(messages: dataSource)
                } else {
                    aichatPrint("loadMessages error:\(error?.errorDescription ?? "")")
                }
            })
        }
    }
}

extension AIChatViewModel: MessageListViewActionEventsDelegate {
    
    public func onPlayButtonClick(message: MessageEntity) {
        SpeechManager.shared.stopSpeaking()
        if message.message.existTTSFile {
            message.playing = !message.playing
            if !message.playing {
                SpeechManager.shared.speak(textMessage: message.message)
            }
        } else {
            message.downloading = true
            SpeechManager.shared.generateVoice(textMessage: message.message, voiceId: message.message.bot?.voiceId ?? "female-chengshu") { [weak self] error, url in
                guard let `self` = self else { return }
                message.downloading = false
                if error == nil {
                    DispatchQueue.main.async {
                        self.driver?.refreshMessagePlayButtonState(message: message)
                    }
                } else {
                    aichatPrint("消息:\(message.message.messageId) 生成语音失败:\(error?.localizedDescription ?? "未知错误")")
                }
            }
        }
    }
    
    public func resendMessage(message: AgoraChatMessage) {
        self.driver?.updateMessageStatus(message: message, status: .sending)
        Task {
            let result = await self.chatService?.resendMessage(messageId: message.messageId)
            if result == nil {
                aichatPrint("resend message fail:\(result?.1?.errorDescription ?? "")")
                DispatchQueue.main.async {
                    self.driver?.updateMessageStatus(message: message, status: .failure)
                }
                ToastView.show(text: "发送失败:\(result?.1?.errorDescription ?? "")")
            } else {
                if result?.1 == nil {
                    if let message = result?.0 {
                        DispatchQueue.main.async {
                            self.driver?.updateMessageStatus(message: message, status: .succeed)
                        }
                    }
                } else {
                    DispatchQueue.main.async {
                        self.driver?.updateMessageStatus(message: message, status: .failure)
                    }
                    ToastView.show(text: "发送失败:\(result?.1?.errorDescription ?? "")")
                    aichatError("resend message fail:\(result?.1?.errorDescription ?? "")")
                }
            }
        }
    }
    
    public func startRecorder() {
        AppContext.audioTextConvertorService()?.startConvertor()
        AppContext.rtcService()?.updateRole(channelName: sttChannelId, role: .broadcaster)
        AppContext.rtcService()?.muteLocalAudioStream(channelName: sttChannelId, isMute: true)
    }
    
    public func stopRecorder() {
        AppContext.audioTextConvertorService()?.flushConvertor()
    }
    
    public func cancelRecorder() {
        AppContext.audioTextConvertorService()?.stopConvertor()
        AppContext.rtcService()?.updateRole(channelName: sttChannelId, role: .audience)
    }

    public func sendMessage(text: String) {
        Task {
            let info = self.fillExtensionInfo()
            let result = await self.chatService?.sendMessage(message: text,extensionInfo: info)
            if let message = result?.0 {
                DispatchQueue.main.async {
                    self.insertTimeAlert(message: message)
                    self.driver?.showMessage(message: message)
                }
            } else {
                aichatPrint("send message fail:\(result?.1?.errorDescription ?? "")")
                ToastView.show(text: "发送失败:\(result?.1?.errorDescription ?? "")")
            }
        }
    }
    
    func fillExtensionInfo() -> [String:Any] {
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
    func convertResultHandler(result: String, error: Error?) {
        cancelRecorder()
        if error == nil {
            aichatError("conver message: \(result)")
            var text = result
            if result.count > 300 {
                text = String(result.prefix(300))
            }
            self.driver?.dismissRecorderView()
            self.sendMessage(text: text)
        } else {
            SVProgressHUD.showError(withStatus: "出了点问题，请重试")
        }
    }
    
    func convertAudioVolumeHandler(volume: UInt, totalVolume: Int, uid: UInt) {
        self.driver?.refreshRecordIndicator(volume: Int(volume))
    }
}

extension AIChatViewModel: AgoraRtcEngineDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
//        guard speakers.count > 0, totalVolume > 10 else {return}
        DispatchQueue.main.async {
//            print("reportAudioVolumeIndicationOfSpeakers: \(totalVolume) \(speakers.map({ "\($0.uid)_\($0.volume)"}))")
            self.driver?.refreshRecordIndicator(volume: 100)
        }
    }
}
