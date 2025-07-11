
import UIKit
import ZSwiftBaseLib
import AgoraChat
import SDWebImage

class AIConversationCell: UITableViewCell {
    
    private lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 20, y: 8, width: self.frame.width-40, height: self.frame.height-16)).contentMode(.scaleAspectFill).cornerRadius(16).layerProperties(UIColor(red: 0.894, green: 0.894, blue: 0.894, alpha: 0.8), 0.5)
    }()
    
    private lazy var avatarView: OverlappingAvatarsView = {
        OverlappingAvatarsView(frame: .zero).contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()
    
    private lazy var nameLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 16, weight: .semibold)).textColor(.black).textAlignment(.left)
    }()
    
    private lazy var timeLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 12, weight: .semibold)).textColor(UIColor(0x6C7192)).textAlignment(.right)
    }()
    
    private lazy var messageLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x303553)).numberOfLines(1).lineBreakMode(.byWordWrapping)
    }()
    
    private lazy var dot: UIView = {
        UIView().backgroundColor(UIColor(0xFA396A)).cornerRadius(6)
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.backgroundColor = .clear
        self.contentView.backgroundColor = .clear
        self.contentView.addSubview(self.container)
        self.container.addSubViews([self.avatarView,self.nameLabel,self.timeLabel,self.messageLabel,self.dot])
        self.setupConstraints()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.container.frame = CGRect(x: 20, y: 8, width: self.frame.width-40, height: self.frame.height-16)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupConstraints() {
        
        self.avatarView.translatesAutoresizingMaskIntoConstraints = false
        self.avatarView.leftAnchor.constraint(equalTo: self.container.leftAnchor, constant: 12).isActive = true
        self.avatarView.centerYAnchor.constraint(equalTo: self.container.centerYAnchor).isActive = true
        self.avatarView.widthAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.heightAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.cornerRadius(32)
        
        self.timeLabel.translatesAutoresizingMaskIntoConstraints = false
        self.timeLabel.topAnchor.constraint(equalTo: self.avatarView.topAnchor,constant: 10).isActive = true
        self.timeLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16).isActive = true
        self.timeLabel.heightAnchor.constraint(equalToConstant: 18).isActive = true
        self.timeLabel.widthAnchor.constraint(equalToConstant: 45).isActive = true
        
        self.nameLabel.translatesAutoresizingMaskIntoConstraints = false
        self.nameLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 16).isActive = true
        self.nameLabel.topAnchor.constraint(equalTo: self.avatarView.topAnchor,constant: 10).isActive = true
        self.nameLabel.rightAnchor.constraint(equalTo: self.timeLabel.leftAnchor, constant: -5).isActive = true
        self.nameLabel.heightAnchor.constraint(equalToConstant: 22).isActive = true
        
        self.messageLabel.translatesAutoresizingMaskIntoConstraints = false
        self.messageLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 16).isActive = true
        self.messageLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -38).isActive = true
        
        // 设置 messageLabel 的底部约束
        self.messageLabel.bottomAnchor.constraint(equalTo: self.avatarView.bottomAnchor,constant: -10).isActive = true
        
        // 设置 messageLabel 的顶部约束，优先级较低
        let topConstraint = self.messageLabel.topAnchor.constraint(equalTo: self.nameLabel.bottomAnchor, constant: 4)
        topConstraint.priority = .defaultLow
        topConstraint.isActive = true
        
        // 设置 messageLabel 的高度约束，确保至少有一定的高度
        self.messageLabel.heightAnchor.constraint(equalToConstant: 18).isActive = true
        
        self.dot.translatesAutoresizingMaskIntoConstraints = false
        self.dot.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16).isActive = true
        self.dot.centerYAnchor.constraint(equalTo: self.messageLabel.centerYAnchor).isActive = true
        self.dot.widthAnchor.constraint(equalToConstant: 12).isActive = true
        self.dot.heightAnchor.constraint(equalToConstant: 12).isActive = true
        
    }
    
    func refresh(with conversation: AIChatConversationInfo) {
        self.nameLabel.text = conversation.name.isEmpty ? conversation.bot?.botName:conversation.name
        self.timeLabel.text = conversation.lastMessage?.showDetailDate
        self.messageLabel.attributedText = conversation.showContent
        self.dot.isHidden = conversation.unreadCount == 0
        if let bot = conversation.bot {
            
            let botType: AIChatBotType = AIChatBotImplement.commonBotIds.contains(bot.botId) ? .common : .custom
            self.container.image = UIImage(named: botType == .common ? "common_chatbot":"custom_chatbot", in: .chatAIBundle, with: nil)
            let urls = (conversation.avatar.isEmpty ? bot.botIcon:conversation.avatar).components(separatedBy: ",")
            if urls.count > 1 || conversation.isGroup {
                self.avatarView.image = nil
                self.container.image = UIImage(named: "group_chatbot", in: .chatAIBundle, with: nil)
                self.avatarView.cornerRadius(0)
                self.avatarView.avatarView1.isHidden = false
                self.avatarView.avatarView2.isHidden = false
                self.avatarView.refresh(with: (urls[0], urls[1]))
            } else {
                self.avatarView.image = nil
                self.avatarView.avatarView1.isHidden = true
                self.avatarView.avatarView2.isHidden = true
                self.avatarView.sd_setImage(with: URL(string: conversation.avatar), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil), options: .retryFailed, context: nil)
                self.avatarView.cornerRadius(32)
            }
        } else {
            let urls = conversation.avatar.components(separatedBy: ",")
            if urls.count > 1 || conversation.isGroup {
                self.avatarView.image = nil
                self.container.image = UIImage(named: "group_chatbot", in: .chatAIBundle, with: nil)
                self.avatarView.cornerRadius(0)
                self.avatarView.avatarView1.isHidden = false
                self.avatarView.avatarView2.isHidden = false
                self.avatarView.refresh(with: (urls[0], urls[1]))
            } else {
                self.avatarView.image = nil
                self.avatarView.avatarView1.isHidden = true
                self.avatarView.avatarView2.isHidden = true
                self.avatarView.sd_setImage(with: URL(string: conversation.avatar), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil), options: .retryFailed, context: nil)
                self.avatarView.cornerRadius(32)
            }
        }
    }
}


open class AIChatConversationInfo: NSObject {
    
    public var isGroup = false
    
    public var id: String = ""
    
    open var name: String = ""
    
    open var avatar: String =  ""
    
    open var bot: AIChatBotProfileProtocol?
    
    open var lastMessage: AgoraChatMessage?
    
    open var unreadCount: Int = 0
    
    open lazy var showContent: NSAttributedString? = {
        guard let message = self.lastMessage else { return nil }
        var text = NSMutableAttributedString()
        
        let from = message.from
        var nickName = ""
        if nickName.isEmpty {
            nickName = from
        }
        if message.body.type == .text {
            var result = message.showType
            var nickname = self.lastMessage?.bot?.botName ?? ""
            var showContent = result
            if message.direction == .receive,self.isGroup {
                showContent = nickname+":"+result
            }
            if nickname.isEmpty {
                showContent = result
            }
            text.append(NSAttributedString {
                AttributedText(showContent).foregroundColor(Color(0x303553)).font(UIFont.theme.bodyLarge).lineBreakeMode(.byTruncatingTail)
            })
            let string = text.string as NSString
            
            let showText = NSMutableAttributedString {
                AttributedText(message.chatType != .chat ? nickName + ": ":"").foregroundColor(Theme.style == .dark ? UIColor.theme.neutralColor6:UIColor.theme.neutralColor5).font(Font.theme.bodyMedium).lineBreakeMode(.byTruncatingTail)
            }
            showText.append(text)
            showText.addAttribute(.foregroundColor, value: Color(0x303553), range: NSRange(location: 0, length: showText.length))
            showText.addAttribute(.font, value: UIFont.theme.bodyMedium, range: NSRange(location: 0, length: showText.length))
            return showText
        } else {
            let showText = NSMutableAttributedString {
                AttributedText((message.chatType == .chat ? message.showType:(nickName+":"+message.showType))).foregroundColor(Color(0x303553)).font(UIFont.theme.bodyMedium).lineBreakeMode(.byTruncatingTail)
            }
            return showText
        }
    }()
    
    
}
