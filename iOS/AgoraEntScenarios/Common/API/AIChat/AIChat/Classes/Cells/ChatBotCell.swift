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
        UIImageView(frame: CGRect(x: 20, y: 8, width: self.frame.width-40, height: self.frame.height-16)).contentMode(.scaleAspectFill).cornerRadius(16).layerProperties(UIColor(red: 0.894, green: 0.894, blue: 0.894, alpha: 0.8), 0.5)
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
        UIButton(type: .custom).backgroundColor(.clear)
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.contentView.addSubview(self.container)
        self.container.addSubViews([self.avatarView,self.nameLabel,self.messageLabel,self.chatIcon])
        self.setupConstraints()
        self.chatIcon.setBackgroundImage(UIImage(named: "chat_to_bot", in: .chatAIBundle, with: nil), for: .normal)
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
        self.avatarView.leftAnchor.constraint(equalTo: self.container.leftAnchor, constant: 18).isActive = true
        self.avatarView.centerYAnchor.constraint(equalTo: self.container.centerYAnchor).isActive = true
        self.avatarView.widthAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.heightAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.cornerRadius(32)
        
        self.nameLabel.translatesAutoresizingMaskIntoConstraints = false
        self.nameLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 14).isActive = true
        self.nameLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16).isActive = true
        self.nameLabel.heightAnchor.constraint(equalToConstant: 20).isActive = true
        
        self.messageLabel.translatesAutoresizingMaskIntoConstraints = false
        self.messageLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 16).isActive = true
        self.messageLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16).isActive = true
        
        // 创建一个垂直堆叠视图来包含 nameLabel 和 messageLabel
        let stackView = UIStackView(arrangedSubviews: [self.nameLabel, self.messageLabel])
        stackView.axis = .vertical
        stackView.spacing = 2
        stackView.alignment = .fill
        stackView.distribution = .fill
        stackView.translatesAutoresizingMaskIntoConstraints = false
        self.container.addSubview(stackView)
        
        // 设置堆叠视图的约束
        NSLayoutConstraint.activate([
            stackView.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 14),
            stackView.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -16),
            stackView.centerYAnchor.constraint(equalTo: self.avatarView.centerYAnchor)
        ])
        
        // 设置 messageLabel 的高度约束，确保至少有一定的高度
        self.messageLabel.heightAnchor.constraint(greaterThanOrEqualToConstant: 18).isActive = true
        
        self.chatIcon.translatesAutoresizingMaskIntoConstraints = false
        self.chatIcon.rightAnchor.constraint(equalTo: self.container.rightAnchor).isActive = true
        self.chatIcon.bottomAnchor.constraint(equalTo: self.container.bottomAnchor).isActive = true
        self.chatIcon.widthAnchor.constraint(equalToConstant: 49).isActive = true
        self.chatIcon.heightAnchor.constraint(equalToConstant: 24).isActive = true
    }


    
    open func refresh(bot: AIChatBotProfileProtocol) {
        self.container.image = UIImage(named: bot.type == .common ? "common_chatbot":"custom_chatbot", in: .chatAIBundle, with: nil)
        self.avatarView.sd_setImage(with: URL(string: bot.botIcon), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil), options: .retryFailed, context: nil)
        self.nameLabel.text = bot.botName.isEmpty ? bot.botId:bot.botName
        self.messageLabel.text = bot.botDescription
    }
}
