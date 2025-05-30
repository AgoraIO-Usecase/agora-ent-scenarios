//
//  AUIChatCell.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit
/*!
 *  \~Chinese
 *  弹幕区域cell
 *
 *  \~English
 *  Chat Cell.
 *
 */
public class AUIChatCell: UITableViewCell {
    
    private var config = AUIChatCellConfig()

    public lazy var container: UIImageView = {
        let view = UIImageView(frame: CGRect(x: 15, y: 6, width: self.contentView.frame.width - 30, height: self.frame.height - 6))
        view.backgroundColor = AUIChatTheme.shared.barrage.containerBackgroundColor
        view.layer.borderWidth = AUIChatTheme.shared.barrage.containerLayerWidth
        view.layer.borderColor = AUIChatTheme.shared.barrage.containerLayerColor.cgColor
        view.layer.cornerRadius = AUIChatTheme.shared.barrage.containerLayerCornerRadius
        return view
    }()

    public lazy var content: UILabel = {
        UILabel(frame: CGRect(x: 10, y: 7, width: self.container.frame.width - 20, height: self.container.frame.height - 18)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(self.config.contentLineBreakMode)
    }()

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.backgroundColor = .clear
        self.contentView.backgroundColor = .clear
    }
    
    @objc public convenience init(reuseIdentifier: String?, config: AUIChatCellConfig) {
        self.init(style: .default, reuseIdentifier: reuseIdentifier)
        self.config = config
        self.contentView.addSubview(self.container)
        self.container.addSubview(self.content)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    
    /// Description 刷新渲染聊天弹幕的实体，内部包含高度宽度以及富文本缓存
    /// - Parameter chat: 实体对象
    @objc public func refresh(chat: AUIChatEntity) {
        self.container.frame = CGRect(x: 15, y: 6, width: chat.width! + 30, height: chat.height! - 6)
        self.content.attributedText = chat.attributeContent
        self.content.preferredMaxLayoutWidth =  self.container.frame.width - 24
        self.content.frame = CGRect(x: 12, y: 7, width:  self.container.frame.width - 24, height:  self.container.frame.height - 16)
        if chat.joined ?? false {
            container.layer.borderWidth = AUIChatTheme.shared.barrage.containerLayerWidth
            container.layer.borderColor = AUIChatTheme.shared.barrage.containerLayerColor.cgColor
        } else {
            self.container.layerProperties(.clear, 0)
        }
        
    }
}

@objcMembers public class AUIChatCellConfig: NSObject {
    
    public var containerBackgroundColor: UIColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
    
    public var containerLayerColor: UIColor = UIColor(red: 0.978, green: 0.98, blue: 0.982, alpha: 0.6)
    
    public var containerLayerWidth: CGFloat = 1
    
    public var containerLayerCornerRadius: CGFloat = 12
    
    public var contentLineBreakMode: NSLineBreakMode = .byWordWrapping
    
    public var mode: AUIThemeMode = .dark {
        willSet {
            switch newValue {
            case .dark:
                self.containerBackgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
            case .light:
                self.containerBackgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.2)
            }
        }
    
    }
    
    public override init() {
        super.init()
    }
    
}
