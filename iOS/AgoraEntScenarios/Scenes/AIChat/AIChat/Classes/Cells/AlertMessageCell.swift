//
//  AlertMessageCell.swift
//  Pods
//
//  Created by 朱继超 on 2024/9/5.
//

import UIKit
import ZSwiftBaseLib

@objc open class AlertMessageCell: MessageCell {
    
    public private(set) lazy var content: UILabel = {
        UILabel(frame: .zero).textAlignment(.center).backgroundColor(.clear).tag(bubbleTag).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.35)).cornerRadius(8)
    }()
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc(initWithTowards:reuseIdentifier:chatType:) 
    required public init(towards: BubbleTowards, reuseIdentifier: String, chatType: AIChatType) {
        super.init(towards: towards, reuseIdentifier: reuseIdentifier, chatType: chatType)
        self.status.isHidden = true
        self.nickName.isHidden = true
        self.avatar.isHidden = true
        self.bubbleMultiCorners.isHidden = true
        self.contentView.addSubViews([self.content])
        self.addGestureTo(view: self.content, target: self)
        self.switchTheme(style: Theme.style)
        self.content.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.content.topAnchor.constraint(greaterThanOrEqualTo: contentView.topAnchor, constant: 2),
            self.content.bottomAnchor.constraint(lessThanOrEqualTo: contentView.bottomAnchor, constant: -2),
            self.content.centerYAnchor.constraint(equalTo: contentView.centerYAnchor),
//            self.content.leadingAnchor.constraint(lessThanOrEqualTo: contentView.leadingAnchor, constant: 7),
//            self.content.trailingAnchor.constraint(lessThanOrEqualTo: contentView.trailingAnchor, constant: -7),
            self.content.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            self.content.heightAnchor.constraint(greaterThanOrEqualToConstant: 26)
        ])
    }
    
    open override func clickAction(gesture: UITapGestureRecognizer) {
        
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    open override func refresh(entity: MessageEntity) {
        self.entity = entity
        self.content.attributedText = entity.content
    }
}


