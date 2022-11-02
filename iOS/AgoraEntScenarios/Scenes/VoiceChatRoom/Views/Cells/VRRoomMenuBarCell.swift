//
//  VRRoomMenuBarCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib

public class VRRoomMenuBarCell: UICollectionViewCell {
    static let selectedFont = UIFont.systemFont(ofSize: 16, weight: .semibold)

    static let normalFont = UIFont.systemFont(ofSize: 12, weight: .regular)

    var item: VRRoomMenuBarEntity? {
        didSet {
            if let entity = self.item {
                self.render(entity)
            }
        }
    }

    lazy var content: UILabel = .init(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height)).textAlignment(.center)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        contentView.backgroundColor = .clear
        contentView.addSubview(content)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        content.frame = CGRect(x: 0, y: 0, width: frame.width, height: frame.height)
    }
}

extension VRRoomMenuBarCell {
    func render(_ item: VRRoomMenuBarEntity) {
        content.text = item.title + item.detail
        var font = VRRoomMenuBarCell.normalFont
        var color = UIColor(0x6C7192)
        if item.selected {
            font = VRRoomMenuBarCell.selectedFont
            color = .darkText
        }
        content.font = font
        content.textColor = color
    }
}
