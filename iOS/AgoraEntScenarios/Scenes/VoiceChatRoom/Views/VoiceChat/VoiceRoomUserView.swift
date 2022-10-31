//
//  VoiceRoomUserView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomUserView: UIView {
    private var controllers = [UIViewController]()

    private var titles = [String]()

    lazy var header: VoiceRoomAlertContainer = .init(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56))

    lazy var switchBar: VoiceRoomSwitchBar = .init(frame: CGRect(x: 0, y: 16, width: ScreenWidth, height: 40), titles: self.titles)

    lazy var container: VoiceRoomPageContainer = .init(frame: CGRect(x: 0, y: self.header.frame.maxY, width: ScreenWidth, height: self.frame.height - self.header.frame.height), viewControllers: self.controllers).backgroundColor(.white)

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    convenience init(frame: CGRect, controllers: [UIViewController], titles: [String], position: VoiceRoomSwitchBarDirection) {
        self.init(frame: frame)
        self.controllers = controllers
        self.titles = titles
        addSubViews([header, switchBar, container])
        container.scrollClosure = { [weak self] in
            self?.switchBar.moveTo(direction: $0 > 0 ? .right : .left)
        }
        switchBar.selectAction = { [weak self] in
            self?.container.index = $0 - 11
        }
        if position == .right {
            switchBar.moveTo(direction: .right)
            container.index = 1
        }
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
