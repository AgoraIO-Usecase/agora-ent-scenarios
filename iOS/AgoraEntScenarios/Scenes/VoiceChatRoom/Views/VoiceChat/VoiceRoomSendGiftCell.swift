//
//  VoiceRoomSendGiftCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/8.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomSendGiftCell: UICollectionViewCell {
    public var gift: VoiceRoomGiftEntity? {
        willSet {
            if newValue != nil {
                DispatchQueue.main.async { self.refresh(item: newValue!) }
            }
        }
    }

    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 5)).cornerRadius(12).layerProperties(UIColor(0xD2BDFF), 1).setGradient([UIColor(red: 0.905, green: 0.765, blue: 1, alpha: 1), UIColor(red: 0.994, green: 0.985, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).backgroundColor(.clear)
    }()

    lazy var icon: UIImageView = .init(frame: CGRect(x: self.contentView.frame.width / 2.0 - 24, y: 16.5, width: 48, height: 48)).contentMode(.scaleAspectFit)

    lazy var name: UILabel = .init(frame: CGRect(x: 0, y: self.icon.frame.maxY + 4, width: self.contentView.frame.width, height: 18)).textAlignment(.center).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x040925))

    lazy var displayValue: UIButton = .init(type: .custom).frame(CGRect(x: 0, y: self.name.frame.maxY + 1, width: self.contentView.frame.width, height: 15)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(red: 0.425, green: 0.445, blue: 0.573, alpha: 0.5), .normal)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        contentView.backgroundColor = .white
        backgroundColor = .white
        contentView.addSubViews([cover, icon, name, displayValue])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func refresh(item: VoiceRoomGiftEntity) {
        icon.image = UIImage(item.gift_id ?? "")
        name.text = item.gift_name
        displayValue.set(image: UIImage("dollagora"), title: item.gift_price ?? "100", titlePosition: .right, additionalSpacing: 5, state: .normal)
        cover.isHidden = !item.selected
        cover.frame = CGRect(x: 0, y: 5, width: contentView.frame.width, height: contentView.frame.height - 5)
        icon.frame = CGRect(x: contentView.frame.width / 2.0 - 24, y: 16.5, width: 48, height: 48)
        displayValue.frame = CGRect(x: 0, y: name.frame.maxY + 1, width: contentView.frame.width, height: 15)
    }
}
