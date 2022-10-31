//
//  VoiceRoomInviteCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/13.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomInviteCell: UITableViewCell {
    var inviteClosure: ((VRUser?) -> Void)?

    var user: VRUser?

    lazy var avatar: UIImageView = .init(frame: CGRect(x: 15, y: 12, width: 50, height: 50)).contentMode(.scaleAspectFit).cornerRadius(25).backgroundColor(.cyan)

    lazy var userName: UILabel = .init(frame: CGRect(x: self.avatar.frame.maxX + 9, y: self.avatar.center.y - 8, width: self.contentView.frame.width - self.avatar.frame.maxX - 95, height: 16)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x333333)).text("UserName")

    lazy var operation: UIButton = .init(type: .custom).frame(CGRect(x: self.contentView.frame.width - 80, y: self.avatar.center.y - 15, width: 65, height: 30)).title("Invite", .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.white, .normal).addTargetFor(self, action: #selector(invite), for: .touchUpInside)

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.backgroundColor = .white
        contentView.addSubViews([avatar, userName, operation])
        operation.layer.shadowColor = UIColor(red: 0, green: 0.55, blue: 0.98, alpha: 0.2).cgColor
        operation.layer.shadowOffset = CGSize(width: 0, height: 4)
        operation.layer.shadowRadius = 8
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        userName.frame = CGRect(x: avatar.frame.maxX + 9, y: avatar.center.y - 8, width: contentView.frame.width - avatar.frame.maxX - 100, height: 16)
        operation.frame = CGRect(x: contentView.frame.width - 85, y: avatar.center.y - 15, width: 76, height: 30)
    }

    func refresh(item: VRUser?) {
        user = item
        userName.text = item?.name
        if item?.invited == false {
            item?.invited = (item?.mic_index ?? 0 != -1)
        }
        avatar.image = UIImage(item?.portrait ?? "")
        operation.setTitle(item?.invited == true ? LanguageManager.localValue(key: "Invited") : LanguageManager.localValue(key: "Invite"), for: .normal)
        operation.setBackgroundImage(UIImage(item?.invited == true ? "" : "blue_btn_bg"), for: .normal)
        var color = UIColor.white
        if item?.invited == true {
            color = UIColor(0x979CBB)
        }
        operation.setTitleColor(color, for: .normal)
    }

    @objc func invite() {
        if inviteClosure != nil, user?.mic_index ?? 0 < 1, user?.invited ?? false == false {
            inviteClosure!(user)
        }
    }
}
