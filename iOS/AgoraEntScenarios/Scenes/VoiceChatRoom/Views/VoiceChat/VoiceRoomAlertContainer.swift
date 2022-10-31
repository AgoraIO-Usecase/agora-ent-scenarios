//
//  VoiceRoomAlertContainer.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/2.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomAlertContainer: UIView {
    lazy var indicator: UIImageView = .init(frame: CGRect(x: ScreenWidth / 2.0 - 19, y: 6, width: 38, height: 3)).image(UIImage("pop_indicator")!)

    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        addSubViews([cover, indicator])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
