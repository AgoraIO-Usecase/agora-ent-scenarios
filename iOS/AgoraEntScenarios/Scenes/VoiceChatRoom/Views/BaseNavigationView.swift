//
//  BaseNavigationView.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
@_exported import ZSwiftBaseLib

public class BaseNavigationView: UIView {
    lazy var back: UIButton = .init(type: .custom).frame(CGRect(x: 20, y: ZStatusBarHeight + 7, width: 30, height: 30))

    lazy var title: UILabel = .init(frame: CGRect(x: self.back.frame.maxX + 10, y: ZStatusBarHeight + 10, width: ScreenWidth - 120, height: 20)).font(UIFont.systemFont(ofSize: 16, weight: .semibold)).backgroundColor(.clear).textAlignment(.center)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubViews([back, title])
        title.center = CGPoint(x: center.x, y: title.center.y)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
