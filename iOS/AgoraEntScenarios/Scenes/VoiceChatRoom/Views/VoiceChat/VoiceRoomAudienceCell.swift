//
//  VoiceRoomAudienceCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/7.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomAudienceCell: UITableViewCell {
    lazy var avatar: UIImageView = .init(frame: CGRect(x: 15, y: 12, width: 50, height: 50)).contentMode(.scaleAspectFit).cornerRadius(25).backgroundColor(.orange)

    lazy var userName: UILabel = .init(frame: CGRect(x: self.avatar.frame.maxX + 9, y: self.avatar.center.y - 8, width: self.contentView.frame.width - self.avatar.frame.maxX - 95, height: 16)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x333333)).text("UserName")

    lazy var operation: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.contentView.frame.width - 75, y: self.avatar.center.y - 15, width: 60, height: 30)).title("test", .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.white, .normal).setGradient([UIColor(0x219BFF), UIColor(0x345DFF)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).cornerRadius(15)
    }()

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.addSubViews([avatar, userName, operation])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        userName.frame = CGRect(x: avatar.frame.maxX + 9, y: avatar.center.y - 8, width: contentView.frame.width - avatar.frame.maxX - 95, height: 16)
        operation.frame = CGRect(x: contentView.frame.width - 75, y: avatar.center.y - 15, width: 60, height: 30)
    }
}
