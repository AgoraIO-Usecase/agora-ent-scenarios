//
//  VRRoomListCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage

public final class VRRoomListCell: UITableViewCell {
    
    var entity = VRRoomEntity()
    
    lazy var background: UIImageView = {
        UIImageView(frame: CGRect(x: 20, y: 15, width: ScreenWidth-40, height: self.frame.height - 15)).image(UIImage("normal_room") ?? UIImage()).backgroundColor(.clear)
    }()
    
    lazy var accessSymbol: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: 0, width: 68, height: 24)).font(UIFont.systemFont(ofSize: 10, weight: .regular)).backgroundColor(.clear)
    }()
    
    lazy var roomName: UILabel = {
        UILabel(frame:CGRect(x: self.background.frame.width/2.0 - 10, y: 28, width: self.background.frame.width/2.0 - 20, height: 40)).font(UIFont.systemFont(ofSize: 15, weight: .medium)).textColor(.white).text("SDOIAJSIDOIASDJOIJSOIJDSDSADADSAD").numberOfLines(2)
    }()
    
    lazy var ownerName: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.roomName.frame.minX-10, y: self.roomName.frame.maxY+15, width: self.background.frame.width/2.0 - 20, height: 16)).isUserInteractionEnabled(false).font(UIFont.systemFont(ofSize: 10, weight: .regular))
    }()
    
    lazy var seenCount: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.roomName.frame.minX-10, y: self.background.frame.height-36, width: 55, height: 16)).isUserInteractionEnabled(false).font(UIFont.systemFont(ofSize: 10, weight: .regular))
    }()
    
    lazy var entryRoom: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.background.frame.width - 72, y: self.background.frame.height - 40, width: 56, height: 24)).font(UIFont.systemFont(ofSize: 12, weight: .semibold)).backgroundColor(.clear).title(LanguageManager.localValue(key: "Enter"), .normal)
    }()
    
    lazy var entryBlur: UIView = {
        UIView(frame: self.entryRoom.frame).backgroundColor(UIColor(white: 1, alpha: 0.3)).cornerRadius(self.entryRoom.frame.height/2.0)
    }()
    
    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.contentView.addSubview(self.background)
        self.background.addSubViews([self.accessSymbol,self.roomName,self.ownerName,self.seenCount,self.entryBlur,self.entryRoom])
        self.ownerName.imageEdgeInsets(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: self.background.frame.width/2.0 - 40))
        self.ownerName.titleEdgeInsets(UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5))
        self.seenCount.imageEdgeInsets(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 40))
        self.seenCount.titleEdgeInsets(UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5))
        self.ownerName.titleLabel?.lineBreakMode(.byTruncatingTail)
        self.ownerName.imageView?.contentMode = .scaleAspectFit
        self.seenCount.setImage(UIImage("person_in_circle"), for: .normal)
        self.seenCount.imageView?.contentMode = .scaleAspectFit
        self.seenCount.contentHorizontalAlignment = .left
        self.seenCount.titleLabel?.lineBreakMode(.byTruncatingTail)
        self.ownerName.contentHorizontalAlignment = .left
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupViewsAttributes(room: VRRoomEntity) {
        self.entity = room
        if let show = room.is_private,show == true {
            self.accessSymbol.set(image: UIImage("suo"), title:LanguageManager.localValue(key: "Private") , titlePosition: .right, additionalSpacing: 5, state: .normal)
            self.accessSymbol.setBackgroundImage(UIImage("securityType"), for: .normal)
            self.accessSymbol.isHidden = false
        } else {
            self.accessSymbol.isHidden = true
        }
        self.roomName.text = room.name
        self.ownerName.setTitle("\(room.owner?.name ?? "")", for: .normal)
        self.seenCount.setTitle("\(room.member_count ?? 0)", for: .normal)
        self.ownerName.setImage(UIImage( room.owner?.portrait ?? ""), for: .normal)
        self.ownerName.setImage(UIImage(named: "mine_avatar_placeHolder"), for: .normal)
        self.ownerName.imageView?.sd_setImage(with: URL(string: room.owner?.portrait ?? ""),completed: { image, error, type, url in
            self.ownerName.setImage(image, for: .normal)
        })
        print("avatar: \(room.owner?.portrait ?? "")")
        var image = UIImage( "normal_room")
        if (room.type ?? 0) == 1 {
            image = UIImage("specific_room")
        }
        self.background.image = image
        
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.background.frame = CGRect(x: 20, y: 15, width: ScreenWidth-40, height: self.contentView.frame.height - 15)
        let height = self.entity.name?.z.sizeWithText(font: .systemFont(ofSize: 15, weight: .medium), size: CGSize(width: self.background.frame.width/2.0-20, height: 999)).height ?? 0
        self.accessSymbol.frame = CGRect(x: 0, y: 0, width: 68, height: 24)
        self.roomName.frame = CGRect(x: self.background.frame.width/2.0 - 10, y: 28, width: self.background.frame.width/2.0 - 20, height: height > 18 ? 36:18)
        self.ownerName.frame = CGRect(x: self.roomName.frame.minX, y: self.roomName.frame.maxY+9, width: self.background.frame.width/2.0 - 20, height: 16)
        self.seenCount.frame = CGRect(x: self.roomName.frame.minX, y: self.background.frame.height-36, width: 55, height: 16)
        self.entryRoom.frame = CGRect(x: self.background.frame.width - 72, y: self.background.frame.height - 40, width: 56, height: 24)
        self.entryBlur.frame = self.entryRoom.frame
    }

}
