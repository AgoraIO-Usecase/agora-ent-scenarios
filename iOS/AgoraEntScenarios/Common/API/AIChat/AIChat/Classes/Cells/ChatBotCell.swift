//
//  ChatBotCell.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage

class ChatBotCell: UITableViewCell {

    private lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 8, width: self.frame.width, height: self.frame.height-16)).contentMode(.scaleAspectFill).cornerRadius(16)
    }()
    
    private lazy var avatarView: UIImageView = {
        UIImageView().contentMode(.scaleAspectFill)
    }()
    
    private lazy var nameLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 16, weight: .semibold)).textColor(UIColor(0x2A2A2A)).textAlignment(.left)
    }()
    
    private lazy var messageLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x303553)).numberOfLines(2)
    }()
    
    lazy var chatIcon: UIButton = {
        UIButton(type: .custom).title("聊天", .normal)
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.addSubview(self.container)
        self.container.addSubViews([self.avatarView,self.nameLabel,self.messageLabel,self.chatIcon])
        self.setupConstraints()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.container.frame = CGRect(x: 0, y: 8, width: self.frame.width, height: self.frame.height-16)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupConstraints() {
        
        self.avatarView.translatesAutoresizingMaskIntoConstraints = false
        self.avatarView.leftAnchor.constraint(equalTo: self.container.leftAnchor, constant: 34).isActive = true
        self.avatarView.centerYAnchor.constraint(equalTo: self.container.centerYAnchor).isActive = true
        self.avatarView.widthAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.heightAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.cornerRadius(32)
        
        self.nameLabel.translatesAutoresizingMaskIntoConstraints = false
        self.nameLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 16).isActive = true
        self.nameLabel.topAnchor.constraint(equalTo: self.avatarView.topAnchor).isActive = true
        self.nameLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16).isActive = true
        self.nameLabel.heightAnchor.constraint(equalToConstant: 20).isActive = true
        
        self.messageLabel.translatesAutoresizingMaskIntoConstraints = false
        self.messageLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 16).isActive = true
        self.messageLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16).isActive = true
        
        // 设置 messageLabel 的底部约束
        self.messageLabel.bottomAnchor.constraint(equalTo: self.avatarView.bottomAnchor).isActive = true
        
        // 设置 messageLabel 的顶部约束，优先级较低
        let topConstraint = self.messageLabel.topAnchor.constraint(equalTo: self.nameLabel.bottomAnchor, constant: 4)
        topConstraint.priority = .defaultLow
        topConstraint.isActive = true
        
        // 设置 messageLabel 的高度约束，确保至少有一定的高度
        self.messageLabel.heightAnchor.constraint(greaterThanOrEqualToConstant: 18).isActive = true
        
        self.chatIcon.translatesAutoresizingMaskIntoConstraints = false
        self.chatIcon.rightAnchor.constraint(equalTo: self.container.rightAnchor).isActive = true
        self.chatIcon.bottomAnchor.constraint(equalTo: self.container.bottomAnchor).isActive = true
        self.chatIcon.widthAnchor.constraint(equalToConstant: 49).isActive = true
        self.chatIcon.heightAnchor.constraint(equalToConstant: 24).isActive = true
        self.chatIcon.createGradient([UIColor(red: 0.4, green: 0.47, blue: 1, alpha: 1),UIColor(red: 0, green: 0.617, blue: 1, alpha: 1)], [CGPoint(x: 0.25, y: 0.5),CGPoint(x: 0.75, y: 0.5)], [NSNumber(integerLiteral: 0),NSNumber(integerLiteral: 1)]).cornerRadius(16, [.topLeft], .clear, 0).isUserInteractionEnabled(false)
        
    }

    
    open func refresh(bot: AIChatBotProfileProtocol) {
        self.avatarView.sd_setImage(with: URL(string: bot.botIcon)!, placeholderImage: UIImage(named: "avatar_placeholder"))
        self.nameLabel.text = bot.botName
        self.messageLabel.text = bot.prompt
    }
}
