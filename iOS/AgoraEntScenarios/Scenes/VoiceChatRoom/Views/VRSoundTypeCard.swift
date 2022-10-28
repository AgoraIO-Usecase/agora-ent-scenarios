//
//  VRSoundTypeCard.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import UIKit
import ZSwiftBaseLib

public class VRSoundTypeCard: HorizontalCardView {
    private var content = ""

    lazy var imageView: UIImageView = .init(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height)).contentMode(.scaleToFill)

    lazy var noteLabel: UILabel = .init().numberOfLines(4).font(.systemFont(ofSize: 12, weight: .regular)).textColor(.white)

    lazy var titleLabel: UILabel = .init().font(.systemFont(ofSize: 16, weight: .semibold)).textColor(.white)

    convenience init(frame: CGRect, title: String, note: String, background: UIImage) {
        self.init(frame: frame)
        backgroundColor = .clear
        content = note
        addSubViews([imageView, noteLabel, titleLabel])
        imageView.image = background
        let height = note.z.sizeWithText(font: .systemFont(ofSize: 12, weight: .regular), size: CGSize(width: self.frame.width - 32, height: 9999)).height
        noteLabel.frame = CGRect(x: 16, y: self.frame.height - height - 18, width: self.frame.width - 32, height: height + 5)
        titleLabel.frame = CGRect(x: 16, y: self.frame.height - height - 18 - 22 - 10, width: self.frame.width - 32, height: 22)
        noteLabel.text = note
        titleLabel.text = title
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let height = content.z.sizeWithText(font: .systemFont(ofSize: 12, weight: .regular), size: CGSize(width: frame.width - 32, height: 9999)).height
        noteLabel.frame = CGRect(x: 16, y: frame.height - height - 18, width: frame.width - 32, height: height + 5)
        titleLabel.frame = CGRect(x: 16, y: frame.height - height - 18 - 22 - 10, width: frame.width - 32, height: 22)
    }

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
