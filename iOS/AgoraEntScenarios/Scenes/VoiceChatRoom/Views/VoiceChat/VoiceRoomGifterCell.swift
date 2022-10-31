//
//  VoiceRoomGifterCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomGifterCell: UITableViewCell {
    
    var user: VRUser?
    
    var index: Int = 0 {
        didSet  {
            DispatchQueue.main.async {
                self.rankIndex.setTitle("\(self.index+1)", for: .normal)
                self.rankIndex.setBackgroundImage(UIImage("top\(self.index+1)"), for: .normal)
            }
        }
    }
    
    lazy var rankIndex: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 15, y: self.contentView.frame.height/2.0-8, width: 16, height: 16)).font(.init(name: "RobotoNembersVF", size: 14)).textColor(.darkText, .normal)
    }()

    lazy var avatar: UIImageView = {
        UIImageView(frame: CGRect(x: self.rankIndex.frame.maxX+15, y: 12, width: 50, height: 50)).contentMode(.scaleAspectFit).cornerRadius(25).backgroundColor(.cyan)
    }()
    
    lazy var userName: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX+9, y: self.avatar.center.y-8, width: self.contentView.frame.width-self.avatar.frame.maxX-95, height: 16)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x333333)).text("UserName")
    }()
    
    lazy var total: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.contentView.frame.width-75, y: self.avatar.center.y-15, width: 60, height: 30)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x6C7192), .normal).image("dollagora", .normal)
    }()
    
    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = .white
        self.contentView.addSubViews([self.rankIndex,self.avatar,self.userName,self.total])
        self.rankIndex.contentVerticalAlignment = .bottom
        
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.rankIndex.frame = CGRect(x: 15, y: self.contentView.frame.height/2.0-12, width: 24, height: 24)
        self.avatar.frame = CGRect(x: self.rankIndex.frame.maxX+15, y: 12, width: 50, height: 50)
        self.userName.frame = CGRect(x: self.avatar.frame.maxX+9, y: self.avatar.center.y-8, width: self.contentView.frame.width-self.avatar.frame.maxX-95, height: 16)
        self.total.frame = CGRect(x: self.contentView.frame.width-75, y: self.avatar.center.y-15, width: 60, height: 30)
    }

    func refresh(item: VRUser?) {
        self.userName.text = item?.name ?? ""
        self.total.setTitle("  \(item?.amount ?? 0)", for: .normal)
        self.avatar.image = UIImage(item?.portrait ?? "")
    }
}
