//
//  MessageCell.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/2.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage
import AgoraChat

/// Tag used for identifying the avatar view in the message cell.
public let avatarTag = 900

/// Tag used for identifying the reply view in the message cell.
public let replyTag = 199

/// Tag used for identifying the bubble view in the message cell.
public let bubbleTag = 200

public let topicTag = 201

public let reactionTag = 202

/// Tag used for identifying the status view in the message cell.
public let statusTag = 168

public let checkBoxTag = 189

/// Enum representing the style of a message cell.
@objc public enum MessageCellStyle: UInt {
    case text
    case image
    case video
    case location
    case voice
    case file
    case cmd
    case contact
    case alert
    case combine
}

/// Enum representing the different areas that can be clicked in a message cell.
@objc public enum MessageCellClickArea: UInt {
    case avatar
    case reply
    case bubble
    case topic
    case reaction
    case status
    case checkbox
}


/// The amount of space between the message bubble and the cell.
let message_bubble_space = CGFloat(1)

@objcMembers open class MessageCell: UITableViewCell {
    
    private var longGestureEnabled: Bool = true
    
    public var entity = MessageEntity()
    
    public private(set) var towards = BubbleTowards.left
    
    public private(set) var chatType = AIChatType.chat
        
    public var clickAction: ((MessageCellClickArea,MessageEntity) -> Void)?
        
    public var longPressAction: ((MessageCellClickArea,MessageEntity) -> Void)?
    
    public private(set) lazy var avatar: UIImageView = {
        self.createAvatar()
    }()
    
    /**
     Creates an avatar image view.
     
     - Returns: An instance of `ImageView` configured with the necessary properties.
     */
    @objc open func createAvatar() -> UIImageView {
        UIImageView(frame: .zero).contentMode(.scaleAspectFill).backgroundColor(.clear).tag(avatarTag)
    }
    
    public private(set) lazy var nickName: UILabel = {
        self.createNickName()
    }()
    
    @objc open func createNickName() -> UILabel {
        UILabel(frame: .zero).backgroundColor(.clear).font(UIFont.theme.labelSmall)
    }
    
    public private(set) lazy var bubbleMultiCorners: MessageBubbleMultiCorner = {
        self.createBubbleMultiCorners()
    }()
    
    @objc open func createBubbleMultiCorners() -> MessageBubbleMultiCorner {
        MessageBubbleMultiCorner(frame: .zero, forward: self.towards).tag(bubbleTag)
    }
    
    public private(set) lazy var status: UIImageView = {
        self.statusView()
    }()
    
    @objc open func statusView() -> UIImageView {
        UIImageView(frame: .zero).backgroundColor(.clear).tag(statusTag)
    }
    
    internal override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    /// ``MessageCell`` required init method.
    /// - Parameters:
    ///   - towards: ``BubbleTowards`` is towards of the bubble.
    ///   - reuseIdentifier: Cell reuse identifier.
    @objc(initWithTowards:reuseIdentifier:chatType:)
    required public init(towards: BubbleTowards,reuseIdentifier: String, chatType: AIChatType) {
        self.towards = towards
        self.chatType = chatType
        super.init(style: .default, reuseIdentifier: reuseIdentifier)
        self.backgroundColor = .clear
        self.contentView.backgroundColor = .clear
        self.contentView.addSubview(self.nickName)
        self.contentView.addSubview(self.avatar)
        self.addGestureTo(view: self.avatar, target: self)
        self.contentView.addSubview(self.bubbleMultiCorners)
//        self.longPressGestureTo(view: self.bubbleMultiCorners, target: self)
       
        self.contentView.addSubview(self.status)
        self.addGestureTo(view: self.status, target: self)
        Theme.registerSwitchThemeViews(view: self)
        self.switchTheme(style: Theme.style)
    }
    
    @objc public func addGestureTo(view: UIView,target: Any?) {
        view.isUserInteractionEnabled = true
        view.addGestureRecognizer(UITapGestureRecognizer(target: target, action: #selector(clickAction(gesture:))))
    }
    
    @objc public func longPressGestureTo(view: UIView,target: Any?) {
        view.isUserInteractionEnabled = true
        let longPress = UILongPressGestureRecognizer(target: target, action: #selector(longPressAction(gesture:)))
        view.addGestureRecognizer(longPress)
    }
    
    @objc open func clickAction(gesture: UITapGestureRecognizer) {
        if let tag = gesture.view?.tag {
//            switch tag {
//            case statusTag:
//                self.clickAction?(.status,self.entity)
//            case replyTag:
//                self.clickAction?(.reply,self.entity)
//            case bubbleTag:
//                self.clickAction?(.bubble,self.entity)
//            case avatarTag:
//                self.clickAction?(.avatar,self.entity)
//            case topicTag:
//                self.clickAction?(.topic,self.entity)
//            case checkBoxTag:
//                self.clickAction?(.checkbox,self.entity)
//            default:
//                break
//            }
        }
    }
    
    @objc open func longPressAction(gesture: UILongPressGestureRecognizer) {
        if let tag = gesture.view?.tag {
//            switch gesture.state {
//            case .began:
//                switch tag {
//                case bubbleTag:
//                    self.longPressAction?(.bubble,self.entity)
//                case avatarTag:
//                    self.longPressAction?(.avatar,self.entity)
//                default:
//                    break
//                }
//            default:
//                break
//            }
        }
    }
    
    private func addRotation() {
        let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
        rotationAnimation.toValue = NSNumber(value: Double.pi * 2)
        rotationAnimation.duration = 1
        rotationAnimation.repeatCount = 999
        rotationAnimation.isRemovedOnCompletion = false
        rotationAnimation.fillMode = CAMediaTimingFillMode.forwards
        
        self.status.layer.add(rotationAnimation, forKey: "rotationAnimation")
    }
        
    /// Refresh cell with ``MessageEntity``
    /// - Parameter entity: ``MessageEntity``
    @objc(refreshWithEntity:)
    open func refresh(entity: MessageEntity) {
        self.towards = entity.message.direction == .send ? .right:.left
        self.entity = entity
        self.updateAxis(entity: entity)
        
       
        if entity.message.direction == .send {
            self.nickName.isHidden = true
        } else {
            if self.chatType == .group {
                //remark > nickname > userId
                self.nickName.text = entity.showUserName
                self.nickName.isHidden = false
            } else {
                self.nickName.isHidden = true
            }
        }
        
        //avatar
        self.avatar.cornerRadius(14)
//        if let user = entity.message.user {
//            if !user.avatarURL.isEmpty {
//                self.avatar.image(with: user.avatarURL, placeHolder: Appearance.avatarPlaceHolder)
//            } else {
//                self.avatar.image = Appearance.avatarPlaceHolder
//            }
//        }
        //message status
        self.status.image = entity.stateImage
        if entity.state == .sending {
            self.addRotation()
        } else {
            self.status.layer.removeAllAnimations()
        }
        
    }
    
    
    /// Update cell subviews axis with ``MessageEntity``
    /// - Parameter entity: ``MessageEntity``
    @objc(updateAxisWithEntity:)
    open func updateAxis(entity: MessageEntity) {
        let bubbleSize = entity.bubbleSize
        if entity.message.direction == .receive {
            self.avatar.isHidden = entity.chatType == .chat
            self.avatar.frame = CGRect(x: 20, y: entity.height - 28 - 16, width: 28, height: 28)
            self.nickName.frame = CGRect(x: self.avatar.frame.maxX+8, y: 0, width: limitBubbleWidth, height: 16)
            self.nickName.textAlignment = .left
            self.bubbleMultiCorners.towards = .left
            self.bubbleMultiCorners.frame = CGRect(x: self.avatar.isHidden ? 20:self.avatar.frame.maxX+8, y: entity.height - 18 - 16 - bubbleSize.height, width: bubbleSize.width, height: bubbleSize.height)
            self.bubbleMultiCorners.updateBubbleCorner()
            self.status.isHidden = true
            self.avatar.isHidden = false
        } else {
            self.status.isHidden = false
            self.avatar.isHidden = true
            self.bubbleMultiCorners.towards = .right
            self.bubbleMultiCorners.frame = CGRect(x: ScreenWidth-bubbleSize.width-20, y: entity.height - 16 - bubbleSize.height, width: bubbleSize.width, height: bubbleSize.height)
            self.bubbleMultiCorners.towards = entity.message.direction == .send ? .right:.left
            self.bubbleMultiCorners.updateBubbleCorner()

            self.status.frame = CGRect(x: self.bubbleMultiCorners.frame.minX-12-20, y: entity.height - 16 - 20, width: 20, height: 20)
        }
    }
    
    
    @objc func updateMessageStatus(entity: MessageEntity) {
        self.status.image = entity.stateImage
        if entity.state != .sending {
            self.status.layer.removeAllAnimations()
            self.status.stopAnimating()
        } else {
            self.addRotation()
        }
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}


/**
 An extension of `MessageCell` that conforms to the `ThemeSwitchProtocol`.
 It provides a method to switch the theme of the cell.
 */
extension MessageCell: ThemeSwitchProtocol {
    /**
     Switches the theme of the cell.
     
     - Parameter style: The style of the theme to switch to.
     */
    open func switchTheme(style: ThemeStyle) {
        self.nickName.textColor = style == .dark ? UIColor.theme.neutralSpecialColor6:UIColor.theme.neutralSpecialColor5
    }
}
