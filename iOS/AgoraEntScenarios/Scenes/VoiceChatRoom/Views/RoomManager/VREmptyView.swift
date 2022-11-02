//
//  VREmptyView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import UIKit
import ZSwiftBaseLib

public class VREmptyView: UIView {
    var emptyImage = UIImage("empty")

    lazy var image: UIImageView = .init(frame: CGRect(x: 90, y: 60, width: self.frame.width - 180, height: (231 / 397.0) * (self.frame.width - 180))).contentMode(.scaleAspectFit).image(self.emptyImage!)

    lazy var text: UILabel = .init(frame: CGRect(x: 20, y: self.image.frame.maxY + 10, width: self.frame.width - 40, height: 60)).textAlignment(.center).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x979CBB)).numberOfLines(0)

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    convenience init(frame: CGRect, title: String, image: UIImage?) {
        self.init(frame: frame)
        if image != nil {
            emptyImage = image!
        }
        addSubViews([self.image, text])
        text.text = title.localized()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
