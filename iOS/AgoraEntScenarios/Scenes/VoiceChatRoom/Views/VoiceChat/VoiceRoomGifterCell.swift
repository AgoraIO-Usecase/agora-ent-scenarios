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
        didSet {
            DispatchQueue.main.async {
                self.rankIndex.setTitle("\(self.index + 1)", for: .normal)
                self.rankIndex.setBackgroundImage(UIImage("top\(self.index + 1)"), for: .normal)
            }
        }
    }

    lazy var rankIndex: UIButton = .init(type: .custom).frame(CGRect(x: 15, y: self.contentView.frame.height / 2.0 - 8, width: 16, height: 16)).font(.init(name: "RobotoNembersVF", size: 14)).textColor(.darkText, .normal)

    lazy var avatar: UIImageView = .init(frame: CGRect(x: self.rankIndex.frame.maxX + 15, y: 12, width: 50, height: 50)).contentMode(.scaleAspectFit).cornerRadius(25).backgroundColor(.cyan)

    lazy var userName: UILabel = .init(frame: CGRect(x: self.avatar.frame.maxX + 9, y: self.avatar.center.y - 8, width: self.contentView.frame.width - self.avatar.frame.maxX - 95, height: 16)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x333333)).text("UserName")

    lazy var total: UIButton = .init(type: .custom).frame(CGRect(x: self.contentView.frame.width - 75, y: self.avatar.center.y - 15, width: 60, height: 30)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x6C7192), .normal).image("dollagora", .normal)

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.backgroundColor = .white
        contentView.addSubViews([rankIndex, avatar, userName, total])
        rankIndex.contentVerticalAlignment = .bottom
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        rankIndex.frame = CGRect(x: 15, y: contentView.frame.height / 2.0 - 12, width: 24, height: 24)
        avatar.frame = CGRect(x: rankIndex.frame.maxX + 15, y: 12, width: 50, height: 50)
        userName.frame = CGRect(x: avatar.frame.maxX + 9, y: avatar.center.y - 8, width: contentView.frame.width - avatar.frame.maxX - 95, height: 16)
        total.frame = CGRect(x: contentView.frame.width - 75, y: avatar.center.y - 15, width: 60, height: 30)
    }

    func refresh(item: VRUser?) {
        userName.text = item?.name ?? ""
        total.setTitle("  \(item?.amount ?? 0)", for: .normal)
        avatar.image = UIImage(item?.portrait ?? "")
    }
}
