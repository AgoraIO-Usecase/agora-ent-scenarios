//
//  VRRoomListCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import SDWebImage
import UIKit
import ZSwiftBaseLib

public final class VRRoomListCell: UITableViewCell {
    var entity = VRRoomEntity()

    lazy var background: UIImageView = .init(frame: CGRect(x: 20, y: 15, width: ScreenWidth - 40, height: self.frame.height - 15)).image(UIImage("normal_room") ?? UIImage()).backgroundColor(.clear)

    lazy var accessSymbol: UIButton = .init(type: .custom).frame(CGRect(x: 0, y: 0, width: 68, height: 24)).font(UIFont.systemFont(ofSize: 10, weight: .regular)).backgroundColor(.clear)

    lazy var roomName: UILabel = .init(frame: CGRect(x: self.background.frame.width / 2.0 - 10, y: 28, width: self.background.frame.width / 2.0 - 20, height: 40)).font(UIFont.systemFont(ofSize: 15, weight: .medium)).textColor(.white).text("SDOIAJSIDOIASDJOIJSOIJDSDSADADSAD").numberOfLines(2)

    lazy var ownerName: UIButton = .init(type: .custom).frame(CGRect(x: self.roomName.frame.minX - 10, y: self.roomName.frame.maxY + 15, width: self.background.frame.width / 2.0 - 20, height: 16)).isUserInteractionEnabled(false).font(UIFont.systemFont(ofSize: 10, weight: .regular))

    lazy var seenCount: UIButton = .init(type: .custom).frame(CGRect(x: self.roomName.frame.minX - 10, y: self.background.frame.height - 36, width: 55, height: 16)).isUserInteractionEnabled(false).font(UIFont.systemFont(ofSize: 10, weight: .regular))

    lazy var entryRoom: UIButton = .init(type: .custom).frame(CGRect(x: self.background.frame.width - 72, y: self.background.frame.height - 40, width: 56, height: 24)).font(UIFont.systemFont(ofSize: 12, weight: .semibold)).backgroundColor(.clear).title(LanguageManager.localValue(key: "Enter"), .normal)

    lazy var entryBlur: UIView = .init(frame: self.entryRoom.frame).backgroundColor(UIColor(white: 1, alpha: 0.3)).cornerRadius(self.entryRoom.frame.height / 2.0)

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.backgroundColor = .clear
        backgroundColor = .clear
        contentView.addSubview(background)
        background.addSubViews([accessSymbol, roomName, ownerName, seenCount, entryBlur, entryRoom])
        ownerName.imageEdgeInsets(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: background.frame.width / 2.0 - 40))
        ownerName.titleEdgeInsets(UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5))
        seenCount.imageEdgeInsets(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 40))
        seenCount.titleEdgeInsets(UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5))
        ownerName.titleLabel?.lineBreakMode(.byTruncatingTail)
        ownerName.imageView?.contentMode = .scaleAspectFit
        seenCount.setImage(UIImage("person_in_circle"), for: .normal)
        seenCount.imageView?.contentMode = .scaleAspectFit
        seenCount.contentHorizontalAlignment = .left
        seenCount.titleLabel?.lineBreakMode(.byTruncatingTail)
        ownerName.contentHorizontalAlignment = .left
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setupViewsAttributes(room: VRRoomEntity) {
        entity = room
        if let show = room.is_private, show == true {
            accessSymbol.set(image: UIImage("suo"), title: LanguageManager.localValue(key: "Private"), titlePosition: .right, additionalSpacing: 5, state: .normal)
            accessSymbol.setBackgroundImage(UIImage("securityType"), for: .normal)
            accessSymbol.isHidden = false
        } else {
            accessSymbol.isHidden = true
        }
        roomName.text = room.name
        ownerName.setTitle("\(room.owner?.name ?? "")", for: .normal)
        seenCount.setTitle("\(room.member_count ?? 0)", for: .normal)
        ownerName.setImage(UIImage(room.owner?.portrait ?? ""), for: .normal)
        ownerName.setImage(UIImage(named: "mine_avatar_placeHolder"), for: .normal)
        ownerName.imageView?.sd_setImage(with: URL(string: room.owner?.portrait ?? ""), completed: { image, error, type, url in
            self.ownerName.setImage(image, for: .normal)
        })
        print("avatar: \(room.owner?.portrait ?? "")")
        var image = UIImage("normal_room")
        if (room.type ?? 0) == 1 {
            image = UIImage("specific_room")
        }
        background.image = image
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        background.frame = CGRect(x: 20, y: 15, width: ScreenWidth - 40, height: contentView.frame.height - 15)
        let height = entity.name?.z.sizeWithText(font: .systemFont(ofSize: 15, weight: .medium), size: CGSize(width: background.frame.width / 2.0 - 20, height: 999)).height ?? 0
        accessSymbol.frame = CGRect(x: 0, y: 0, width: 68, height: 24)
        roomName.frame = CGRect(x: background.frame.width / 2.0 - 10, y: 28, width: background.frame.width / 2.0 - 20, height: height > 18 ? 36 : 18)
        ownerName.frame = CGRect(x: roomName.frame.minX, y: roomName.frame.maxY + 9, width: background.frame.width / 2.0 - 20, height: 16)
        seenCount.frame = CGRect(x: roomName.frame.minX, y: background.frame.height - 36, width: 55, height: 16)
        entryRoom.frame = CGRect(x: background.frame.width - 72, y: background.frame.height - 40, width: 56, height: 24)
        entryBlur.frame = entryRoom.frame
    }
}
