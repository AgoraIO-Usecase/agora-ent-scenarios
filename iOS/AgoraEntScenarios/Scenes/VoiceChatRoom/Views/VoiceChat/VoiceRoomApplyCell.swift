//
//  VoiceRoomApplyCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/13.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomApplyCell: UITableViewCell {
    
    var agreeClosure: ((VoiceRoomApply?) -> ())?
    
    var user: VoiceRoomApply? 

    lazy var avatar: UIImageView = {
        UIImageView(frame: CGRect(x: 15, y: 12, width: 50, height: 50)).contentMode(.scaleAspectFit).cornerRadius(25).backgroundColor(.cyan)
    }()
    
    lazy var userName: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX+9, y: self.avatar.center.y-8, width: self.contentView.frame.width-self.avatar.frame.maxX-95-16, height: 16)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x333333)).text("UserName")
    }()
    
    lazy var operation: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.contentView.frame.width-91, y: self.avatar.center.y-15, width: 76, height: 30)).title("Accept", .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.white, .normal).addTargetFor(self, action: #selector(apply), for: .touchUpInside)
    }()

    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = .white
        self.contentView.addSubViews([self.avatar,self.userName,self.operation])
        self.operation.layer.shadowColor = UIColor(red: 0, green: 0.55, blue: 0.98, alpha: 0.2).cgColor
        self.operation.layer.shadowOffset = CGSize(width: 0, height: 4)
        self.operation.layer.shadowRadius = 8
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.userName.frame = CGRect(x: self.avatar.frame.maxX+9, y: self.avatar.center.y-8, width: self.contentView.frame.width-self.avatar.frame.maxX-95-16, height: 16)
        self.operation.frame = CGRect(x: self.contentView.frame.width-91, y: self.avatar.center.y-15, width: 76, height: 30)
    }

    func refresh(item: VoiceRoomApply?) {
        self.user = item
        self.userName.text = item?.member?.name
        self.avatar.image = UIImage(item?.member?.portrait ?? "")
        self.operation.setTitle(item?.member?.invited == true ? LanguageManager.localValue(key: "Accepted"):LanguageManager.localValue(key: "Accept"), for: .normal)
        self.operation.setBackgroundImage(UIImage(item?.member?.invited == true ? "":"blue_btn_bg"), for: .normal)
        var color = UIColor.white
        if item?.member?.invited == true {
            color = UIColor(0x979CBB)
        }
        self.operation.setTitleColor(color, for: .normal)
    }
    
    @objc func apply() {
        if self.agreeClosure != nil,self.user?.member?.mic_index ?? 0 < 1,self.user?.member?.invited ?? false == false  {
            self.agreeClosure!(self.user)
        }
    }
}
