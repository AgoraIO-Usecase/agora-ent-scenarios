//
//  VoiceRoomGiftCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/31.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomGiftCell: UITableViewCell {
    var gift: VoiceRoomGiftEntity?

    lazy var container: UIView = .init(frame: CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 5)).backgroundColor(.clear)

    lazy var avatar: UIImageView = .init(frame: CGRect(x: 5, y: 5, width: self.frame.width / 5.0, height: self.frame.width / 5.0)).contentMode(.scaleAspectFit)

    lazy var userName: UILabel = .init(frame: CGRect(x: self.avatar.frame.maxX + 6, y: 8, width: self.frame.width / 5.0 * 2 - 12, height: 15)).font(.systemFont(ofSize: 12, weight: .semibold)).textColor(.white)

    lazy var giftName: UILabel = .init(frame: CGRect(x: self.avatar.frame.maxX + 6, y: self.userName.frame.maxY, width: self.frame.width / 5.0 * 2 - 12, height: 15)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(.white)

    lazy var giftIcon: UIImageView = .init(frame: CGRect(x: self.frame.width / 5.0 * 3, y: 0, width: self.frame.width / 5.0, height: self.contentView.frame.height)).contentMode(.scaleAspectFit).image(UIImage("heart")!)

    lazy var giftNumbers: UILabel = .init(frame: CGRect(x: self.frame.width / 5.0 * 4 + 8, y: 10, width: self.frame.width / 5.0 - 16, height: self.frame.height - 20)).font(.init(name: "RobotoNembersVF", size: 16)).textColor(.white)

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.backgroundColor = .clear
        backgroundColor = .clear
        contentView.addSubview(container)
        container.addSubViews([avatar, userName, giftName, giftIcon, giftNumbers])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        container.frame = CGRect(x: 0, y: 5, width: contentView.frame.width, height: contentView.frame.height - 5)
        container.cornerRadius(22).setGradient([UIColor(red: 0.05, green: 0, blue: 0.76, alpha: 0.24), UIColor(red: 0.71, green: 0.37, blue: 1, alpha: 0.64)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
        avatar.frame = CGRect(x: 5, y: 5, width: contentView.frame.height - 15, height: contentView.frame.height - 15)
        avatar.cornerRadius((contentView.frame.height - 15) / 2.0)
        userName.frame = CGRect(x: avatar.frame.maxX + 6, y: 5, width: frame.width / 5.0 * 2 - 12, height: 15)
        giftName.frame = CGRect(x: avatar.frame.maxX + 6, y: userName.frame.maxY + 2, width: frame.width / 5.0 * 2 - 12, height: 15)
        giftIcon.frame = CGRect(x: frame.width / 5.0 * 3, y: 0, width: container.frame.height, height: container.frame.height)
        giftNumbers.frame = CGRect(x: giftIcon.frame.maxX + 5, y: 5, width: container.frame.width - giftIcon.frame.maxX - 5, height: container.frame.height - 5)
    }

    func refresh(item: VoiceRoomGiftEntity) {
        if gift == nil {
            gift = item
        }
        avatar.image = gift?.avatar
        userName.text = gift?.userName ?? ""
        giftName.text = "Sent " + (gift?.gift_name ?? "")
        giftIcon.image = UIImage("\(gift?.gift_id ?? "")")
        giftNumbers.text = "X \(gift?.gift_count ?? "1")"
    }
}
