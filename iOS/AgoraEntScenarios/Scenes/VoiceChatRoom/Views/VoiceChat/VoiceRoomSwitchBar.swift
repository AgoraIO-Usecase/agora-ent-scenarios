//
//  VoiceRoomSwitchBar.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit
import ZSwiftBaseLib

@objc public enum VoiceRoomSwitchBarDirection: Int {
    case left = 1
    case right = 2
}

public class VoiceRoomSwitchBar: UIView {
    @objc public var selectAction: ((Int) -> Void)?

    lazy var leftItem: UIButton = .init(type: .custom).frame(CGRect(x: 20, y: 0, width: ScreenWidth / 2.0 - 30, height: 24)).font(.systemFont(ofSize: 16, weight: .semibold)).textColor(.darkText, .selected).addTargetFor(self, action: #selector(selected(_:)), for: .touchUpInside).tag(11)

    lazy var rightItem: UIButton = .init(type: .custom).frame(CGRect(x: ScreenWidth / 2.0 + 10, y: 0, width: ScreenWidth / 2.0 - 30, height: 24)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.darkText, .selected).addTargetFor(self, action: #selector(selected(_:)), for: .touchUpInside).tag(12)

    lazy var indicator: UIView = .init(frame: CGRect(x: self.leftItem.center.x - 12, y: self.leftItem.frame.maxY + 5, width: 24, height: 3)).backgroundColor(UIColor(0x156EF3)).cornerRadius(1.5)

    let line = UIView().backgroundColor(UIColor(0xF2F2F2))

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    convenience init(frame: CGRect, titles: [String]) {
        self.init(frame: frame)
        leftItem.isSelected = true
        line.frame = CGRect(x: 0, y: frame.height - 1, width: frame.width, height: 1)
        if titles.count == 1 {
            addSubViews([leftItem, indicator, line])
            leftItem.frame = CGRect(x: 20, y: 0, width: ScreenWidth - 40, height: 24)
            leftItem.setTitle(titles.first ?? "", for: .normal)
            leftItem.setTitleColor(UIColor(0x979CBB), for: .normal)
            indicator.frame = CGRect(x: self.frame.width / 2.0 - 12, y: leftItem.frame.maxY + 5, width: 24, height: 3)
        } else {
            addSubViews([leftItem, rightItem, indicator, line])
            leftItem.setTitle(titles.first ?? "", for: .normal)
            rightItem.setTitle(titles.last ?? "", for: .normal)
            leftItem.setTitleColor(UIColor(0x979CBB), for: .normal)
            rightItem.setTitleColor(UIColor(0x979CBB), for: .normal)
        }
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc func selected(_ sender: UIButton) {
        refreshUI(sender)
        moveTo(direction: sender.tag == 11 ? .left : .right)
        if selectAction != nil {
            selectAction!(sender.tag)
        }
    }

    private func refreshUI(_ sender: UIButton) {
        leftItem.isSelected = (sender.tag == 11)
        rightItem.isSelected = (sender.tag != 11)
        if sender.tag == 11 {
            leftItem.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
            rightItem.titleLabel?.font = .systemFont(ofSize: 14, weight: .regular)
        } else {
            rightItem.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
            leftItem.titleLabel?.font = .systemFont(ofSize: 14, weight: .regular)
        }
    }

    @objc func moveTo(direction: VoiceRoomSwitchBarDirection) {
        let sender = direction == .left ? leftItem : rightItem
        refreshUI(sender)
        var point = CGPoint(x: leftItem.center.x, y: indicator.center.y)
        if sender != leftItem {
            point = CGPoint(x: rightItem.center.x, y: indicator.center.y)
        }
        UIView.animate(withDuration: 0.3) {
            self.indicator.center = point
        }
    }
}
