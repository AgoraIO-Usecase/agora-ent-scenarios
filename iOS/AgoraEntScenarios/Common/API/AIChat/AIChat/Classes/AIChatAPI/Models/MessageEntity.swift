//
//  MessageEntity.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/2.
//

import UIKit
import AgoraChat
import ZSwiftBaseLib

/// The status of ``ChatMessage``.
@objc public enum ChatMessageStatus: UInt {
    case sending
    case succeed
    case failure
    case delivered
    case read
}

public enum ChatReceiveMessageState: UInt8 {
    case typing
    case editing
    case end
}


fileprivate let ScreenWidth = UIScreen.main.bounds.width

/// Audio message `default` height.
public var audioHeight = CGFloat(36)

/// File message `default` height.
public var fileHeight = CGFloat(60)

/// Alert message `default` height.
public var alertHeight = CGFloat(30)

/// Limit width of the message bubble.
public var limitBubbleWidth = CGFloat(ScreenWidth*(3/5.0))


@objcMembers open class MessageEntity: NSObject {
    
    required public override init() {
        super.init()
    }
    
    public var message: AgoraChatMessage = AgoraChatMessage()
    
    
    public var showUserName: String {
        ""
    }
    
    public var chatType = AIChatType.chat
        
    /// /// Message state.
    public var state: ChatMessageStatus = .sending
    
    /// Whether audio message playing or not.
    public var playing = false
    
    public var selected = false
    
    public var editState: ChatReceiveMessageState = .typing
    
    /// Message status image.
    public var stateImage: UIImage? {
        self.getStateImage()
    }
    
    open func getStateImage() -> UIImage? {
        switch self.state {
        case .sending:
            return UIImage(named: "message_status_spinner", in: .chatAIBundle, with: nil)
        case .succeed:
            return UIImage(named: "message_status_succeed", in: .chatAIBundle, with: nil)
        case .failure:
            return UIImage(named: "message_status_failure", in: .chatAIBundle, with: nil)
        case .delivered:
            return UIImage(named: "message_status_delivery", in: .chatAIBundle, with: nil)
        case .read:
            return UIImage(named: "message_status_read", in: .chatAIBundle, with: nil)
        }
    }
    
    /// Bubble size of the message.
    public lazy var bubbleSize: CGSize = {
        self.updateBubbleSize()
    }()
    
    /// Height for row.
    public lazy var height: CGFloat = {
        self.cellHeight()
    }()
    
    open func cellHeight() -> CGFloat {
        if let body = self.message.body as? AgoraChatCustomMessageBody,body.event == "AIChat_alert_message" {
            return self.bubbleSize.height
        } else {
            return 18+16+self.bubbleSize.height
        }
    }
    
    /// Text message show content.
    public private(set) lazy var content: NSMutableAttributedString? = {
        self.convertTextAttribute()
    }()
            
    open func updateBubbleSize() -> CGSize {
        switch self.message.body.type {
        case .text:
            if self.message.direction == .receive {
                if self.editState == .typing {
                    return CGSize(width: 68, height: 42)
                } else {
                    return self.updateTextBubbleSize()
                }
            } else {
                return self.updateTextBubbleSize()
            }
        default:
            return CGSize(width: limitBubbleWidth, height: 40)
        }
    }
    
    open func updateTextBubbleSize() -> CGSize {
        let text = (self.message.body as? AgoraChatTextMessageBody)?.text ?? ""
        if text.isEmpty {
            return .zero
        }
        let label = UILabel().numberOfLines(0).attributedText(self.content)
        let size = label.sizeThatFits(CGSize(width: limitBubbleWidth-28, height: 9999))
        let bottomSpace = CGFloat(self.message.direction == .send ? 0:42)
        return CGSize(width: size.width+28, height: size.height+(self.editState == .end ? bottomSpace:0)+20)
    }
    
    open func convertTextAttribute() -> NSMutableAttributedString? {
        if self.message.body.type == .text {
            if self.editState == .editing {
                if self.message.direction == .receive {
                    let text = self.editState == .editing ? String(self.message.showType.dropLast()):self.message.showType
                    return NSMutableAttributedString {
                        AttributedText(text).font(.systemFont(ofSize: 16, weight: .regular)).foregroundColor(self.message.direction == .send ? .white:.black).paragraphStyle(self.paragraphStyle())
                        AttributedText(" ●").foregroundColor(Color(0x979cbb)).font(Font.systemFont(ofSize: 10))
                    }
                } else {
                    return NSMutableAttributedString {
                        AttributedText(self.message.showType).font(.systemFont(ofSize: 16, weight: .regular)).foregroundColor(self.message.direction == .send ? .white:.black).paragraphStyle(self.paragraphStyle())
                    }
                }
            } else {
                return NSMutableAttributedString {
                    AttributedText(self.message.showType).font(.systemFont(ofSize: 16, weight: .regular)).foregroundColor(self.message.direction == .send ? .white:.black).paragraphStyle(self.paragraphStyle())
                }
            }
        } else if self.message.body.type == .custom {
            if let body = self.message.body as? AgoraChatCustomMessageBody,body.event == "AIChat_alert_message",let something = self.message.ext?["something"] as? String {
                if something.z.numCount == 13 {
                    return NSMutableAttributedString {
                        AttributedText(something.z.timeStampToString(dateFormat: "MMM d HH:mm")+"  ").foregroundColor(.white).font(UIFont.theme.bodySmall).lineHeight(multiple: 0.98, minimum: 16).alignment(.center).paragraphStyle(self.alertParagraphStyle())
                    }
                } else {
                    return NSMutableAttributedString {
                        AttributedText(something+"  ").foregroundColor(.white).font(UIFont.theme.bodySmall).lineHeight(multiple: 0.98, minimum: 16).alignment(.center).paragraphStyle(self.alertParagraphStyle())
                    }
                }

            } else {
                return nil
            }
        } else {
            return nil
        }
    }
    
    open func paragraphStyle() -> NSMutableParagraphStyle {
        var paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = 0.98
        return paragraphStyle
    }
    
    open func alertParagraphStyle() -> NSMutableParagraphStyle {
        var paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = 0.98
        paragraphStyle.firstLineHeadIndent = 7
        paragraphStyle.headIndent = 7           // 设置其余行的左缩进
        paragraphStyle.tailIndent = 7          // 设置右缩进
        return paragraphStyle
    }
}

extension AgoraChatMessage {
    
//    /// ``EaseProfileProtocol``
//    @objc public var user: EaseProfileProtocol? {
//        let cacheUser = EaseChatUIKitContext.shared?.userCache?[self.from]
//        if cacheUser != nil,let remark = cacheUser?.remark,!remark.isEmpty {
//            EaseChatUIKitContext.shared?.chatCache?[self.from]?.remark = remark
//        }
//        let chatUser = EaseChatUIKitContext.shared?.chatCache?[self.from]
//        if chatUser?.nickname.isEmpty ?? true {
//            chatUser?.nickname = cacheUser?.nickname ?? ""
//        }
//        if chatUser == nil,cacheUser != nil {
//            if let chatAvatarURL = chatUser?.avatarURL,!chatAvatarURL.isEmpty {
//                cacheUser?.avatarURL = chatAvatarURL
//            }
//            return cacheUser
//        }
//        return chatUser
//    }
    
    var bot: AIChatBotProfileProtocol? {
        if let botId = self.ext?[ "ai_chat"] as? [String:Any],let userMeta = botId["user_meta"] as? [String:Any],let botId = userMeta["botId"] as? String {
            if let bot = AIChatBotImplement.commonBot.first(where: { $0.botId == botId }) {
                return bot
            }
            if let bot = AIChatBotImplement.customBot.first(where: { $0.botId == botId }) {
                return bot
            }
        }
        return nil
    }
    
    /// Whether message edited or not.
    @objc public var edited: Bool {
        if self.body.type != .text {
            return false
        } else {
            if let body = self.body as? AgoraChatTextMessageBody {
                if body.operatorCount > 0,body.operationTime > 0 {
                    return true
                } else {
                    return false
                }
            }
            return false
        }
    }
    
    /// Message display date on chat cell.
    @objc open var showDetailDate: String {
        let messageDate = Date(timeIntervalSince1970: TimeInterval(self.timestamp/1000))
        if messageDate.compareDays() < 0 {
            return messageDate.z.dateString("MM/dd")
        } else {
            return messageDate.z.dateString("HH:mm")
        }
    }
    
    /// Message show type on the conversation list.
    @objc open var showType: String {
        var text = "[未知消息]"
        switch self.body.type {
        case .text: text = (self.body as? AgoraChatTextMessageBody)?.text ?? ""
        case .image: text = "[图片]"
        case .voice: text = "[音频]"
        case .video: text = "[视频]"
        case .file: text = "[文件]"
        case .location: text = "[位置]"
        case .combine: text = "[\("聊天记录")]"
        case .cmd: text = "[系统消息]"
        case .custom:
            if let body = self.body as? AgoraChatCustomMessageBody {
                if body.event == "AIChat_alert_message" {
                    text = ((self.ext?["something"] as? String) ?? "")
                }
            }
        default: break
        }
        return text
    }
    
    @objc open var contentSize: CGSize {
        CGSize(width: limitBubbleWidth, height: 30)
    }
    
    /// Translation of the text message.
    @objc public var translation: String? {
        (self.body as? AgoraChatTextMessageBody)?.translations?.first?.value
    }
    
}


extension Date {
    func compareDays() -> Int {
        Calendar.current.dateComponents([.day], from: Date(),to: self).day ?? 0
    }
}
