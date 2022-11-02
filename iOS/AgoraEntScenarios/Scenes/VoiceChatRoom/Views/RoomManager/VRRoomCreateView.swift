//
//  VRRoomCreateView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib

public class VRRoomCreateView: UIImageView {
    var action: (() -> Void)?

    lazy var createRoom: UIButton = .init(type: .custom).frame(CGRect(x: ScreenWidth / 2.0 - 95, y: 10, width: 190, height: 50)).cornerRadius(25).addTargetFor(self, action: #selector(createAction), for: .touchUpInside).font(.systemFont(ofSize: 16, weight: .semibold))

    lazy var createContainer: UIView = .init(frame: CGRect(x: ScreenWidth / 2.0 - 95, y: 10, width: 190, height: 50)).backgroundColor(.white)

    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(createContainer)
        addSubview(createRoom)
        createContainer.layer.cornerRadius = 25
        createContainer.layer.shadowRadius = 8
        createContainer.layer.shadowOffset = CGSize(width: 0, height: 4)
        createContainer.layer.shadowColor = UIColor(red: 0, green: 0.55, blue: 0.98, alpha: 0.2).cgColor
        createContainer.layer.shadowOpacity = 1
        isUserInteractionEnabled = true
        createRoom.set(image: UIImage("add"), title: LanguageManager.localValue(key: "Create Room"), titlePosition: .right, additionalSpacing: 7, state: .normal)
        createRoom.setGradient([UIColor(red: 0.13, green: 0.61, blue: 1, alpha: 1), UIColor(red: 0.2, green: 0.37, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc private func createAction() {
        if action != nil {
            action!()
        }
    }
}

extension UIView {
    @discardableResult
    func setGradient(_ colors: [UIColor], _ points: [CGPoint]) -> Self {
        let gradientColors: [CGColor] = colors.map { $0.cgColor }
        let startPoint = points[0]
        let endPoint = points[1]
        let gradientLayer = CAGradientLayer().colors(gradientColors).startPoint(startPoint).endPoint(endPoint).frame(bounds).backgroundColor(UIColor.clear.cgColor)
        layer.insertSublayer(gradientLayer, at: 0)
        return self
    }
}
