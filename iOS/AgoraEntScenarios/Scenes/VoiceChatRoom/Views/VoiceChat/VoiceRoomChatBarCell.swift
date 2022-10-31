//
//  VoiceRoomChatBarCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/31.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomChatBarCell: UICollectionViewCell {
    
    lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).contentMode(.scaleAspectFit).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)).cornerRadius(self.contentView.frame.height/2.0)
    }()
    
    lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()
    
    let redDot = UIView().backgroundColor(.red).cornerRadius(3)
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.contentView.addSubview(self.container)
        self.contentView.addSubview(self.redDot)
        self.contentView.addSubview(self.icon)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        let r = self.contentView.frame.width/2.0;
        let length = CGFloat(ceilf(Float(r)/sqrt(2)))
        self.redDot.frame = CGRect(x: self.frame.width/2.0+length, y: self.contentView.frame.height/2.0-length, width: 6, height: 6)
        self.icon.frame =  CGRect(x: 7, y: 7, width: self.contentView.frame.width-14, height: self.contentView.frame.height-14)
    }
    
}


public class VoiceRoomEmojiCell: UICollectionViewCell {
    
    lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: 7, y: 7, width: self.contentView.frame.width-14, height: self.contentView.frame.height-14)).contentMode(.scaleAspectFit).backgroundColor(.white)
    }()
        
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor =  .white
        self.contentView.addSubview(self.icon)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.icon.frame =  CGRect(x: 7, y: 7, width: self.contentView.frame.width-14, height: self.contentView.frame.height-14)
    }
    
}
