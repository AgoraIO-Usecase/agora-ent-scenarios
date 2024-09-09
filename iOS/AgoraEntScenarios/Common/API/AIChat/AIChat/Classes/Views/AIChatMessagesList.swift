//
//  AIChatMessagesList.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/2.
//

import UIKit
import ZSwiftBaseLib
import AgoraChat

let messagesPageSize = 20

/// The height of the bottom safe area of the screen.
let BottomBarHeight = UIApplication.shared.windows.first?.safeAreaInsets.bottom ?? 0

let EditBeginTypingMessageId = "EditBeginTypingMessageId"

@objc public protocol IAIChatMessagesListDriver: NSObjectProtocol {
    
    var firstMessageId: String {get}
    
    var dataSource: [AgoraChatMessage] {get}
    
    var scrolledBottom: Bool {get}
    
    func addActionHandler(actionHandler: MessageListViewActionEventsDelegate)
    
    func removeEventHandler(actionHandler: MessageListViewActionEventsDelegate)
    
    func insertMessages(messages: [AgoraChatMessage])
    
    func showMessage(message: AgoraChatMessage)
    
    func editMessage(message: AgoraChatMessage,finished: Bool)
    
    func refreshRecordIndicator(volume: Int)
}

@objc public protocol MessageListViewActionEventsDelegate: NSObjectProtocol {
    
    func sendMessage(text: String)
    
    func onMessageListPullRefresh()
    
    func startRecorder()
    
    func stopRecorder()
    
    func cancelRecorder()
}


open class AIChatMessagesList: UIView {

    private var lastOffsetY = CGFloat(0)

    private var cellOffset = CGFloat(0)
    
    private var inputBottomConstraint: NSLayoutConstraint?
    
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
        CompositeInputView(frame: .zero).backgroundColor(UIColor(white: 1, alpha: 0.8)).cornerRadius(16)
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
        UIButton(type: .custom).frame(CGRect(x: (self.frame.width-36)/2.0, y: self.inputBar.frame.minY-44, width: 36, height: 36)).font(UIFont.theme.labelMedium).addTargetFor(self, action: #selector(scrollTableViewToBottom), for: .touchUpInside).backgroundColor(UIColor(white: 1, alpha: 0.68)).cornerRadius(18)
    }()
    
    private lazy var audioRecorderView: AIChatAudioRecorderView = {
        AIChatAudioRecorderView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ScreenHeight))
    }()
    
    private var chatType: AIChatType = .chat
    
    var voiceChatClosure: (()->())?
    
    public required init(frame: CGRect, chatType: AIChatType) {
        super.init(frame: frame)
        self.chatType = chatType
        self.isUserInteractionEnabled = true
        self.addSubViews([self.blurView])
        self.blurView.layer.mask = self.gradientLayer
        self.blurView.addSubViews([self.chatView,self.inputBar,self.moreMessages])
        self.setupMoreMessages()
        self.setInputBarConstraints()
        self.chatView.refreshControl = UIRefreshControl()
        self.chatView.refreshControl?.addTarget(self, action: #selector(pullRefresh), for: .valueChanged)
        self.chatView.allowsSelection = false
        self.chatView.keyboardDismissMode = .onDrag
        self.inputActions()
        self.moreMessages.setImage(UIImage(named: "more_messages", in: .chatAIBundle, with: nil), for: .normal)
        SpeechManager.shared.playCompletion = { [weak self] in
            if $0 {
                self?.refreshPlayState()
            }
        }
    }
    
    func refreshPlayState() {
        for message in self.messages {
            message.playing = false
        }
        self.chatView.reloadData()
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
                if keyboardHeight >= 216 {
                    self.inputBottomConstraint?.constant = -keyboardHeight+20
                }
            } else {
                self.inputBottomConstraint?.constant = BottomBarHeight-20
            }
            self.layoutIfNeeded()
            let lastIndexPath = IndexPath(row: self.messages.count - 1, section: 0)
            if lastIndexPath.row >= 0 {
                self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: true)
            }
        }
    }
    
    private func judgeLongPressDirection(state: LongPressButton.State,direction: LongPressButton.MoveDirection) {
        self.audioRecorderView.refreshBackground(with: state)
        switch state {
        case .start:
            for handler in self.eventHandlers.allObjects {
                handler.startRecorder()
            }
            self.superview?.addSubview(self.audioRecorderView)
        case .cancel:
            if direction == .none {
                for handler in self.eventHandlers.allObjects {
                    handler.cancelRecorder()
                }
                self.audioRecorderView.removeFromSuperview()
            }
        case .end:
            for handler in self.eventHandlers.allObjects {
                handler.stopRecorder()
            }
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
        NSLayoutConstraint.activate([
            self.chatView.topAnchor.constraint(equalTo: self.topAnchor),
            self.chatView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            self.chatView.trailingAnchor.constraint(equalTo: self.trailingAnchor),
            self.chatView.bottomAnchor.constraint(equalTo: self.inputBar.topAnchor)
        ])
        
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
        
        
    }
    
    @available(*, unavailable)
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        consoleLogInfo("deinit \(self.swiftClassName ?? "")", type: .debug)
    }

}

extension AIChatMessagesList:UITableViewDelegate, UITableViewDataSource {
    
    @objc public func scrollTableViewToBottom() {
        if self.messages.count > 1 {
            self.chatView.reloadData()
            let lastIndexPath = IndexPath(row: self.chatView.numberOfRows(inSection: 0) - 1, section: 0)
            if lastIndexPath.row >= 0 {
                self.chatView.scrollToRow(at: lastIndexPath, at: .bottom, animated: true)
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
        guard let  cell = self.registerMessageCell(tableView: tableView, indexPath: indexPath) else {
            return MessageCell(towards: direction, reuseIdentifier: "AIChat.MessageCell", chatType: self.chatType)
        }
        cell.refresh(entity: entity)
        cell.selectionStyle = .none
        cell.clickAction = { [weak self] type,entity in
            if type == .bubble {
                self?.processBubbleClickAction(entity: entity)
            }
        }
        
        return cell
    }
    
    func processBubbleClickAction(entity: MessageEntity) {
        for message in self.messages {
            message.playing = false
        }
        entity.playing = !entity.playing
        self.chatView.reloadData()
    }

    
    private func convertMessage(message: AgoraChatMessage,editFinished: Bool = false) -> MessageEntity {
        let entity = MessageEntity()
        if message.status == .pending {
            message.status = .succeed
        }
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
}

extension AIChatMessagesList: IAIChatMessagesListDriver {
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
        self.showMessageAnimation(message: message)
        DispatchQueue.main.asyncAfter(wallDeadline: .now()+0.3) {
            if message.direction == .send {
                self.insertTypingMessage(to: message.from)
            }
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
    }
    
    func feedback(with style: UIImpactFeedbackGenerator.FeedbackStyle) {
        let feedbackGenerator = UIImpactFeedbackGenerator(style: style)
        feedbackGenerator.prepare()
        feedbackGenerator.impactOccurred()
    }
        
    private func insertTypingMessage(to: String) {
        let entity = MessageEntity()
        let message = AgoraChatMessage(conversationID: to, body: AgoraChatTextMessageBody(text: "•••"), ext: nil)
        message.direction = .receive
        message.messageId = EditBeginTypingMessageId
        entity.message = message
        
        entity.editState = .typing
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
