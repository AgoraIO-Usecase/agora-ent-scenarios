//
//  GroupBotCell.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/10.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage

class GroupBotCell: UITableViewCell {

    private lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 8, width: self.frame.width, height: self.frame.height-16)).contentMode(.scaleAspectFill).cornerRadius(16)
    }()
    
    private lazy var avatarView: UIImageView = {
        UIImageView().contentMode(.scaleAspectFill)
    }()
    private lazy var nameLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 16, weight: .semibold)).textColor(UIColor(0x2A2A2A)).textAlignment(.left)
    }()
    
    lazy var selectSymbol: UIImageView = {
        UIImageView(frame: .zero).contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()
    
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.contentView.addSubview(self.container)
        self.container.addSubViews([self.avatarView,self.nameLabel,self.selectSymbol])
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
        self.avatarView.leftAnchor.constraint(equalTo: self.container.leftAnchor, constant: 18).isActive = true
        self.avatarView.centerYAnchor.constraint(equalTo: self.container.centerYAnchor).isActive = true
        self.avatarView.widthAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.heightAnchor.constraint(equalToConstant: 64).isActive = true
        self.avatarView.cornerRadius(32)
        
        
        self.nameLabel.translatesAutoresizingMaskIntoConstraints = false
        self.nameLabel.leftAnchor.constraint(equalTo: self.avatarView.rightAnchor, constant: 14).isActive = true
        self.nameLabel.centerYAnchor.constraint(equalTo: self.container.centerYAnchor).isActive = true
        self.nameLabel.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -50).isActive = true
        self.nameLabel.heightAnchor.constraint(equalToConstant: 20).isActive = true
        
        self.selectSymbol.translatesAutoresizingMaskIntoConstraints = false
        self.selectSymbol.centerYAnchor.constraint(equalTo: self.container.centerYAnchor).isActive = true
        self.selectSymbol.rightAnchor.constraint(equalTo: self.container.rightAnchor, constant: -24).isActive = true
        self.selectSymbol.widthAnchor.constraint(equalToConstant: 18).isActive = true
        self.selectSymbol.heightAnchor.constraint(equalToConstant: 18).isActive = true
        
    }

    
    open func refresh(bot: AIChatBotProfileProtocol) {
        self.container.image = UIImage(named: bot.type == .common ? "common_chatbot":"custom_chatbot", in: .chatAIBundle, with: nil)
        self.avatarView.sd_setImage(with: URL(string: bot.botIcon), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil), options: .retryFailed, context: nil)
        self.nameLabel.text = bot.botName.isEmpty ? bot.botId:bot.botName
        self.selectSymbol.image = UIImage(named: bot.selected ? "selected":"unselected", in: .chatAIBundle, with: nil)
    }

}
