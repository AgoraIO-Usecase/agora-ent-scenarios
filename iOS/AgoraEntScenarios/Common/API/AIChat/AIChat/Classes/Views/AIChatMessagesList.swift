//
//  AIChatMessagesList.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/2.
//

import UIKit
import ZSwiftBaseLib
import AgoraChat
import AgoraCommon

let messagesPageSize = 20

/// The height of the bottom safe area of the screen.
let BottomBarHeight = UIApplication.shared.windows.first?.safeAreaInsets.bottom ?? 0

let EditBeginTypingMessageId = "EditBeginTypingMessageId"

@objc public protocol IAIChatMessagesListDriver: NSObjectProtocol {
    
    var firstMessageId: String {get}
    
    var dataSource: [AgoraChatMessage] {get}
    
    var scrolledBottom: Bool {get}
    
    var selectedBot: AIChatBotProfileProtocol? {get}
    
    func addActionHandler(actionHandler: MessageListViewActionEventsDelegate)
    
    func removeEventHandler(actionHandler: MessageListViewActionEventsDelegate)
    
    func insertMessages(messages: [AgoraChatMessage])
    
    func showMessage(message: AgoraChatMessage)
    
    func editMessage(message: AgoraChatMessage,finished: Bool)
    
    func refreshRecordIndicator(volume: Int)
    
    func refreshBots(bots: [AIChatBotProfileProtocol], enable: Bool)
    
    func dismissRecorderView()
    
    func updateMessageStatus(message: AgoraChatMessage,status: ChatMessageStatus)
    
    func refreshMessagePlayButtonState(message: MessageEntity)
}

@objc public protocol MessageListViewActionEventsDelegate: NSObjectProtocol {
    
    func sendMessage(text: String)
        
    func onMessageListPullRefresh()
    
    func startRecorder()
    
    func stopRecorder()
    
    func cancelRecorder()
    
    func resendMessage(message: AgoraChatMessage)
    
    func onPlayButtonClick(message: MessageEntity)
}


open class AIChatMessagesList: UIView {

    private var lastOffsetY = CGFloat(0)

    private var cellOffset = CGFloat(0)
    
    private var inputBottomConstraint: NSLayoutConstraint?
    
    private var chatTopConstraint: NSLayoutConstraint?
    
    private var chatBottomConstraint: NSLayoutConstraint?
    
    private var eventHandlers: NSHashTable<MessageListViewActionEventsDelegate> = NSHashTable<MessageListViewActionEventsDelegate>.weakObjects()

    public private(set) var messages: [MessageEntity] = [MessageEntity]()
        
    public var scrolledBottom: Bool {
        let contentHeight = self.chatView.contentSize.height
        let tableViewHeight = self.chatView.bounds.size.height
        let yOffset = self.chatView.contentOffset.y
        return Int(ceilf(Float(yOffset))) >= Int(ceilf(Float(contentHeight - tableViewHeight)))
    }

    public private(set) lazy var chatView: UITableView = {
        UITableView(frame: .zero, style: .plain).delegate(self).dataSource(self).separatorStyle(.none).tableFooterView(UIView()).backgroundColor(.clear).isUserInteractionEnabled(true)
    }()

    public private(set) lazy var gradientLayer: CAGradientLayer = {
        CAGradientLayer().startPoint(CGPoint(x: 0, y: 0)).endPoint(CGPoint(x: 0, y: 0.15)).colors([UIColor.clear.withAlphaComponent(0).cgColor, UIColor.clear.withAlphaComponent(1).cgColor]).locations([NSNumber(0), NSNumber(1)]).rasterizationScale(UIScreen.main.scale).frame(self.blurView.frame)
    }()

    public private(set) lazy var blurView: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height)).backgroundColor(.clear).isUserInteractionEnabled(true)
    }()
    
    public private(set) lazy var inputBar: CompositeInputView = {
        CompositeInputView(frame: .zero,type: self.chatType).backgroundColor(UIColor(white: 1, alpha: 0.8)).cornerRadius(16)
    }()
    
    private var bots: [AIChatBotProfileProtocol] = []
    
    private lazy var botsList: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.itemSize = CGSize(width: 28, height: 28)
        layout.minimumInteritemSpacing = 12
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout).registerCell(ChatBotSelectCell.self, forCellReuseIdentifier: "ChatBotSelectCell").delegate(self).dataSource(self).backgroundColor(.clear)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        return collectionView
    }()
    
    private var moreMessagesCount = 0  {
        willSet {
            DispatchQueue.main.async {
                self.moreMessages.isHidden = newValue <= 0
                self.moreMessages.frame = CGRect(x: (self.frame.width-36)/2.0, y: self.inputBar.frame.minY-44, width: 36, height: 36)
            }
            
        }
    }
    
    public private(set) lazy var moreMessages: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: (self.frame.width-36)/2.0, y: self.inputBar.frame.minY-44, width: 36, height: 36)).font(UIFont.theme.labelMedium).addTargetFor(self, action: #selector(scrollTableViewToBottom), for: .touchUpInside).backgroundColor(UIColor(white: 1, alpha: 0.8)).cornerRadius(18)
    }()
    
    private lazy var audioRecorderView: AIChatAudioRecorderView = {
        AIChatAudioRecorderView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ScreenHeight)).contentMode(.scaleAspectFill)
    }()
    
    private var chatType: AIChatType = .chat
    
    private var botEnable = false
    
    private var currentTask: DispatchWorkItem?
    
    private let queue = DispatchQueue(label: "com.example.miniConversationsHandlerQueue")

    var voiceChatClosure: (()->())?
    
    public required init(frame: CGRect, chatType: AIChatType) {
        super.init(frame: frame)
        self.chatType = chatType
        self.isUserInteractionEnabled = true
        self.addSubViews([self.blurView])
        self.blurView.layer.mask = self.gradientLayer
        if chatType == .chat {
            self.blurView.addSubViews([self.chatView,self.inputBar,self.moreMessages])
        } else {
            self.blurView.addSubViews([self.chatView,self.inputBar,self.botsList])
        }
        self.setupMoreMessages()
        self.setInputBarConstraints()
        self.chatView.refreshControl = UIRefreshControl()
        self.chatView.refreshControl?.tintColor = UIColor(white: 1, alpha: 0.8)
        self.chatView.refreshControl?.addTarget(self, action: #selector(pullRefresh), for: .valueChanged)
        self.chatView.allowsSelection = false
        self.chatView.keyboardDismissMode = .onDrag
        self.inputActions()
        self.moreMessages.setImage(UIImage(named: "more_messages", in: .chatAIBundle, with: nil), for: .normal)
        AppContext.speechManager()?.playCompletion = { [weak self] in
            if $0 {
                self?.refreshPlayState()
            }
        }
    }
    
    func refreshPlayState() {
        if AppContext.speechManager()?.playState == .playing {
            return
        }
        DispatchQueue.main.async {
            for message in self.messages {
                if message.playing {
                    message.playing = false
                    message.downloading = false
                    self.refreshMessagePlayButtonState(message: message)
                    break
                }
            }
            self.chatView.reloadData()
        }
    }
    
    func setupMoreMessages() {
        self.moreMessages.isHidden = true
        self.moreMessages.layer.masksToBounds = false
        let shadowPath0 = UIBezierPath(roundedRect: self.moreMessages.bounds, cornerRadius: 4)
        let layer0 = CALayer()
        layer0.shadowPath = shadowPath0.cgPath
        layer0.shadowColor = UIColor(red: 0.275, green: 0.306, blue: 0.325, alpha: 0.15).cgColor
        layer0.shadowOpacity = 1
        layer0.shadowRadius = 8
        layer0.shadowOffset = CGSize(width: 2, height: 4)
        layer0.bounds = self.moreMessages.bounds
        layer0.position = self.moreMessages.center
        self.moreMessages.layer.addSublayer(layer0)
    }
    
    func inputActions() {
        self.inputBar.becomeFirstResponderClosure = { [weak self] firstResponder,keyboardHeight, duration in
            self?.keyboardAnimation(duration: duration, keyboardHeight: keyboardHeight, firstResponder: firstResponder)
        }
        
        self.inputBar.sendClosure = { [weak self] text in
            if text.isEmpty {
                return
            }
            guard let `self` = self else { return }
            for handler in self.eventHandlers.allObjects {
                handler.sendMessage(text: text)
            }
            self.inputBar.setDisableState()
            self.calculateTableViewLimitHeight()
        }
        
        self.inputBar.longPressAudioClosure = { [weak self] in
            self?.judgeLongPressDirection(state: $0, direction: $1)
        }
        
        self.inputBar.voiceChatClosure = { [weak self] in
            self?.voiceChatClosure?()
        }
    }
    
    func keyboardAnimation(duration: TimeInterval, keyboardHeight: CGFloat, firstResponder: Bool) {
        UIView.animate(withDuration: duration) {
            if firstResponder {
                var raiseHeight = keyboardHeight
                if keyboardHeight > 335 {
                    raiseHeight = 340
                }
                if keyboardHeight >= 216 {
                    self.inputBottomConstraint?.constant = -raiseHeight
                    if let topConstraint = self.chatTopConstraint?.constant {
                        self.chatTopConstraint?.constant = topConstraint - raiseHeight
                    }
                }
            } else {
                self.inputBottomConstraint?.constant = 0
                self.calculateTableViewLimitHeight()
            }
            self.layoutIfNeeded()
            let lastIndexPath = IndexPath(row: self.messages.count - 1, section: 0)
            if lastIndexPath.row >= 0 {
                self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: true)
            }
        }
    }
    
    private func judgeLongPressDirection(state: LongPressButton.State, direction: LongPressButton.MoveDirection) {
        aichatPrint("judgeLongPressDirection state: \(state) direction: \(direction)", context: "AIChatMessagesList")
        switch state {
        case .start:
            if direction == .none {
                self.inputBar.resetToInitialState()
                for handler in self.eventHandlers.allObjects {
                    handler.startRecorder()
                }
                if self.audioRecorderView.superview == nil {
                    self.superview?.addSubview(self.audioRecorderView)
                } else {
                    self.audioRecorderView.refreshBackground(with: .start)
                }
            }
        case .cancel:
            for handler in self.eventHandlers.allObjects {
                handler.cancelRecorder()
            }
            self.audioRecorderView.refreshBackground(with: state)
        case .end:
            for handler in self.eventHandlers.allObjects {
                handler.stopRecorder()
            }
            self.audioRecorderView.refreshBackground(with: .start)
            self.audioRecorderView.removeFromSuperview()
        default:
            break
        }
    }
    
    @objc private func pullRefresh() {
        for handler in self.eventHandlers.allObjects {
            handler.onMessageListPullRefresh()
        }
    }
    
    func setInputBarConstraints() {
        
        self.chatView.translatesAutoresizingMaskIntoConstraints = false
        
        self.inputBar.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.inputBar.heightAnchor.constraint(lessThanOrEqualToConstant: 130),
            self.inputBar.heightAnchor.constraint(greaterThanOrEqualToConstant: 50)
        ])
        
        NSLayoutConstraint.activate([
            self.inputBar.leadingAnchor.constraint(equalTo: self.leadingAnchor,constant: 20),
            self.inputBar.trailingAnchor.constraint(equalTo: self.trailingAnchor,constant: -20),
        ])
        
        self.inputBottomConstraint = self.inputBar.bottomAnchor.constraint(equalTo: self.safeAreaLayoutGuide.bottomAnchor)
        self.inputBottomConstraint?.isActive = true
        
        NSLayoutConstraint.activate([
            self.chatView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            self.chatView.trailingAnchor.constraint(equalTo: self.trailingAnchor),
        ])
        
        self.chatBottomConstraint = self.chatView.bottomAnchor.constraint(equalTo: self.inputBar.topAnchor,constant: (self.chatType == .chat || self.bots.count <= 1) ? -21:-68)
        self.chatBottomConstraint?.isActive = true
        
        self.chatTopConstraint = self.chatView.topAnchor.constraint(lessThanOrEqualTo: self.topAnchor)
        self.chatTopConstraint?.isActive = true
        
        if self.chatType == .group {
            NSLayoutConstraint.activate([
                self.botsList.bottomAnchor.constraint(equalTo: self.inputBar.topAnchor,constant: -12),
                self.botsList.leadingAnchor.constraint(equalTo: self.inputBar.leadingAnchor),
                self.botsList.trailingAnchor.constraint(equalTo: self.inputBar.trailingAnchor),
                self.botsList.heightAnchor.constraint(equalToConstant: 28)
            ])
        }
    }
    
    func calculateTableViewLimitHeight() {
        let height = self.messages.reduce(0) { $0 + $1.height }
        let limitHeight = self.frame.height - (self.bots.count <= 1 ? 21:68) - self.inputBar.frame.height - BottomBarHeight
        if height >= limitHeight {
            self.chatTopConstraint?.constant = 0
        } else {
            let inputBarHeight = self.inputBar.frame.height
            let bottom = BottomBarHeight
            let top = self.frame.height - (self.bots.count <= 1 ? 21:68) - self.inputBar.frame.height - BottomBarHeight - height
            self.chatTopConstraint?.constant = top
        }
        self.chatBottomConstraint?.constant = self.bots.count <= 1 ? -21:-68
        self.layoutIfNeeded()
    }
    
    @available(*, unavailable)
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        consoleLogInfo("deinit \(self.swiftClassName ?? "")", type: .debug)
    }

}

extension AIChatMessagesList: UICollectionViewDataSource,UICollectionViewDelegate {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.bots.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "ChatBotSelectCell", for: indexPath) as? ChatBotSelectCell
        if let bot = self.bots[safe: indexPath.row] {
            cell?.refresh(bot: bot,enable: self.botEnable)
        }
        return cell ?? ChatBotSelectCell()
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if !self.botEnable {
            return
        }
        if let bot = self.bots[safe: indexPath.row] {
            self.bots.forEach { $0.selected = false }
            bot.selected = true
            collectionView.reloadData()
        }
    }
}

extension AIChatMessagesList:UITableViewDelegate, UITableViewDataSource {
    
    @objc public func scrollTableViewToBottom() {
        if self.messages.count > 1 {
            self.chatView.reloadData()
            let lastIndexPath = IndexPath(row: self.chatView.numberOfRows(inSection: 0) - 1, section: 0)
            if lastIndexPath.row >= 0 {
                self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: true)
                self.moreMessages.isHidden = true
            }
        }
    }

    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.messages.count
    }

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let height = self.messages[safe: indexPath.row]?.height ?? 0
        return height
    }

    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let entity = self.messages[safe: indexPath.row] else { return MessageCell() }
        let direction: BubbleTowards = entity.message.direction == .send ? .right:.left
        guard let cell = self.registerMessageCell(tableView: tableView, indexPath: indexPath) else {
            return MessageCell(towards: direction, reuseIdentifier: "AIChat.MessageCell", chatType: self.chatType)
        }
        cell.refresh(entity: entity)
        cell.selectionStyle = .none
        cell.clickAction = { [weak self] type,entity in
            self?.processBubbleClickAction(area:type,entity: entity)
        }
        
        cell.longPressAction = { _, _ in
            let textToCopy = entity.message.getPasteBoardText()
            let pasteboard = UIPasteboard.general
            pasteboard.string = textToCopy
            ToastView.show(text: "already copy '\(textToCopy)' to pasteboard", postion: .center)
        }
        
        return cell
    }
    
    func processBubbleClickAction(area: MessageCellClickArea, entity: MessageEntity) {
        switch area {
        case .bubble:
            if AppContext.speechManager()?.playState == .playing {
                AppContext.speechManager()?.stopSpeaking()
            }
            for message in self.messages {
                if message.message.messageId != entity.message.messageId {
                    message.playing = false
                    message.downloading = false
                }
            }
            for handler in self.eventHandlers.allObjects {
                handler.onPlayButtonClick(message: entity)
            }
            self.chatView.reloadData()
        case .status:
            for handler in self.eventHandlers.allObjects {
                handler.resendMessage(message: entity.message)
            }
        default:
            break
        }
    }

    
    private func convertMessage(message: AgoraChatMessage,editFinished: Bool = false) -> MessageEntity {
        let entity = MessageEntity()
        if message.status == .pending {
            message.status = .succeed
        }
        entity.chatType = self.chatType
        entity.state = self.convertStatus(message: message)
        entity.editState = editFinished ? .end:.editing
        entity.message = message
        _ = entity.content
        _ = entity.bubbleSize
        _ = entity.height
        return entity
    }

    private func convertStatus(message: AgoraChatMessage) -> ChatMessageStatus {
        switch message.status {
        case .succeed:
            if message.isReadAcked {
                return .read
            }
            if message.isDeliverAcked {
                return .delivered
            }
            return .succeed
        case .pending:
            return .sending
        default:
            return .failure
        }
    }
    
    private func registerMessageCell(tableView: UITableView,indexPath: IndexPath) -> MessageCell? {
        if let message = self.messages[safe: indexPath.row]?.message {
            let towards: BubbleTowards = message.direction.rawValue == 0 ? .right:.left
            switch message.body.type {
            case .text:
                var cell = tableView.dequeueReusableCell(with: TextMessageCell.self, reuseIdentifier: "AIChat.TextMessageCell")
                if cell == nil {
                    cell = TextMessageCell(towards: towards, reuseIdentifier: "AIChat.TextMessageCell", chatType: self.chatType)
                }
                return cell
            case .custom:
                if let body = message.body as? AgoraChatCustomMessageBody {
                    switch body.event {
                    case "AIChat_alert_message":
                        var cell = tableView.dequeueReusableCell(with: AlertMessageCell.self, reuseIdentifier: "AIChat.AlertCell")
                        if cell == nil {
                            cell = AlertMessageCell(towards: towards, reuseIdentifier: "AIChat.AlertCell",chatType: self.chatType)
                        }
                        return cell
                    default:
                        return nil
                    }
                } else {
                    return nil
                }
            default:
                return nil
            }
        } else {
            return nil
        }
    }
    
    public func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let offsetY = scrollView.contentOffset.y
        let deltaY = self.lastOffsetY - scrollView.contentOffset.y
        if deltaY > 0 , offsetY > 0 {
            if !self.scrolledBottom {
                self.moreMessagesCount = 1
            }
        } else {
            self.moreMessagesCount = 0
        }
        
        // 更新上一次的内容偏移量
        self.lastOffsetY = scrollView.contentOffset.y
    }
}

extension AIChatMessagesList: IAIChatMessagesListDriver {
    public func refreshMessagePlayButtonState(message: MessageEntity) {
        if let index = self.messages.firstIndex(where: { $0.message.messageId == message.message.messageId }) {
            if let cell = self.chatView.cellForRow(at: IndexPath(row: index, section: 0)) as? TextMessageCell {
                cell.refresh(entity: message)
            }
        }
    }
    
    public func updateMessageStatus(message: AgoraChatMessage, status: ChatMessageStatus) {
        if let index = self.messages.firstIndex(where: { $0.message.localTime == message.localTime }) {
            self.messages[safe: index]?.message = message
            self.messages[safe: index]?.state = status
            if let cell = self.chatView.cellForRow(at: IndexPath(row: index, section: 0)) as? MessageCell {
                cell.updateMessageStatus(entity: self.messages[index])
            }
        }
        self.inputBar.setEnableState()
    }
    
    public func dismissRecorderView() {
        self.audioRecorderView.removeFromSuperview()
    }
    
    public var selectedBot: (any AIChatBotProfileProtocol)? {
        self.bots.first { $0.selected }
    }
    
    
    public func refreshBots(bots: [any AIChatBotProfileProtocol], enable: Bool) {
        self.botEnable = enable
        self.bots.removeAll()
        self.bots.append(contentsOf: bots)
        self.botsList.reloadData()
        if self.chatType == .group {
            self.botsList.isHidden = bots.count <= 1
        }
        self.calculateTableViewLimitHeight()
    }
    
    public func refreshRecordIndicator(volume: Int) {
        self.audioRecorderView.updateIndicatorImage(volume: volume)
    }
    
    public var firstMessageId: String {
        self.messages.first?.message.messageId ?? ""
    }
    
    public var dataSource: [AgoraChatMessage] {
        self.messages.map { $0.message }
    }
    
    /// Add UI actions handler.
    /// - Parameter actionHandler: ``MessageListViewActionEventsDelegate``
    public func addActionHandler(actionHandler: MessageListViewActionEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }
    
    /// Remove UI action handler.
    /// - Parameter actionHandler: ``MessageListViewActionEventsDelegate``
    public func removeEventHandler(actionHandler: MessageListViewActionEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
    public func insertMessages(messages: [AgoraChatMessage]) {
        self.chatView.refreshControl?.endRefreshing()
        let pullBeforeMessageId = self.messages.first?.message.messageId ?? ""
        self.messages.insert(contentsOf: messages.map({
            self.convertMessage(message: $0,editFinished: true)
        }), at: 0)
        self.calculateTableViewLimitHeight()
        self.chatView.reloadData()
        if self.messages.count <= messagesPageSize {
            let lastIndexPath = IndexPath(row: self.messages.count - 1, section: 0)
            if lastIndexPath.row > 0 {
                self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: false)
            }
        } else {
            if let beforeIndex = self.messages.firstIndex(where: { $0.message.messageId == pullBeforeMessageId }) {
                self.chatView.scrollToRow(at: IndexPath(row: beforeIndex, section: 0), at: .top, animated: false)
            }
        }
    }
    
    public func showMessage(message: AgoraChatMessage) {
        let entity = self.convertMessage(message: message)
        if message.direction == .send {
            self.messages.append(entity)
        } else {
            if let index = self.messages.firstIndex(where: { $0.message.messageId == EditBeginTypingMessageId }) {
                self.messages.replaceSubrange(index...index, with: [entity])
            }
        }
        self.calculateTableViewLimitHeight()
        self.showMessageAnimation(message: message)
        DispatchQueue.main.asyncAfter(wallDeadline: .now()+0.3) {
            if message.direction == .send,message.status == .succeed {
                self.insertTypingMessage(to: message.from)
                self.delayedTask()
            }
        }
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
        self.messages.removeAll { $0.message.messageId == EditBeginTypingMessageId }
        self.botEnable = false
        DispatchQueue.main.async {
            self.inputBar.setEnableState()
            self.chatView.reloadData()
            self.calculateTableViewLimitHeight()
        }
    }
    
    private func showMessageAnimation(message: AgoraChatMessage) {
        let scrolledBottom = self.scrolledBottom
        self.chatView.reloadData()
        if self.messages.count > 1 {
            if message.direction == .send {
                let lastIndexPath = IndexPath(row: self.messages.count - 1, section: 0)
                if lastIndexPath.row > 0 {
                    self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: true)
                }
                
            } else {
                if scrolledBottom {
                    let lastIndexPath = IndexPath(row: self.messages.count - 1, section: 0)
                    if lastIndexPath.row > 0 {
                        self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: true)
                    }
                } else {
                    self.moreMessagesCount += 1
                }
            }
            
        }
    }
    
    public func editMessage(message: AgoraChatMessage,finished: Bool = false) {
        for (index, entity) in self.messages.enumerated() {
            if entity.message.messageId == message.messageId || entity.message.messageId == EditBeginTypingMessageId {
                self.messages[index] = self.convertMessage(message: message,editFinished: finished)
                self.chatView.beginUpdates()
                self.chatView.reloadRows(at: [IndexPath(row: index, section: 0)], with: .none)
                self.chatView.endUpdates()
                self.chatView.scrollToRow(at: IndexPath(row: index, section: 0), at: .bottom, animated: true)
                self.feedback(with: .light)
                if finished {
                    self.inputBar.setEnableState()
                    self.feedback(with: .medium)
                }
                break
            }
        }
        self.calculateTableViewLimitHeight()
    }
    
    func feedback(with style: UIImpactFeedbackGenerator.FeedbackStyle) {
        let feedbackGenerator = UIImpactFeedbackGenerator(style: style)
        feedbackGenerator.prepare()
        feedbackGenerator.impactOccurred()
    }
        
    private func insertTypingMessage(to: String) {
        let entity = MessageEntity()
        if self.messages.first(where: { $0.message.messageId == EditBeginTypingMessageId }) != nil {
            return
        }
        let botId = self.bots.filter { $0.selected }.first?.botId ?? ""
        let message = AgoraChatMessage(conversationID: to, body: AgoraChatTextMessageBody(text: "•••"), ext: ["ai_chat":["user_meta":["botId":botId]]])
        message.direction = .receive
        message.from = botId
        message.messageId = EditBeginTypingMessageId
        entity.message = message
        entity.editState = .typing
        entity.chatType = self.chatType
        entity.state = .succeed
        _ = entity.content
        _ = entity.bubbleSize
        _ = entity.height
        self.messages.append(entity)
        self.showMessageAnimation(message: entity.message)
    }
    
}

extension UITableView {
    /// Dequeues a UICollectionView Cell with a generic type and indexPath
    /// - Parameters:
    ///   - type: A generic cell type
    ///   - indexPath: The indexPath of the row in the UICollectionView
    /// - Returns: A Cell from the type passed through
    func dequeueReusableCell<Cell: UITableViewCell>(with type: Cell.Type, reuseIdentifier: String) -> Cell? {
        dequeueReusableCell(withIdentifier: reuseIdentifier) as? Cell
    }
    
    @objc public func reloadDataSafe() {
        DispatchQueue.main.async {
            self.reloadData()
        }
    }
}
