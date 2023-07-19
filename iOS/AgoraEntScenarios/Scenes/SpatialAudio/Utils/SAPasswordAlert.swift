//
//  VoiceRoomPasswordAlert.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/11.
//

import UIKit
import ZSwiftBaseLib

public class SAPasswordAlert: UIView {
    /// 30 is cancel,other is confirm
    @objc public var actionEvents: ((Int) -> Void)?

    var code = ""

    lazy var title: UILabel = .init(frame: CGRect(x: 30, y: 30, width: self.frame.width - 60, height: 20)).textColor(.darkText).font(.systemFont(ofSize: 16, weight: .regular)).textAlignment(.center).text("spatial_voice_enter_4_digit_password".localized_spatial())

    lazy var pinCode: SAVerifyCodeView = .init(frame: CGRect(x: 0, y: self.title.frame.maxY + 32, width: self.frame.width, height: (self.frame.width - 63 - 3 * 16) / 4.0), codeNumbers: 4, space: 16, padding: 31.5).backgroundColor(.white)

    lazy var cancel: UIButton = .init(type: .custom).frame(CGRect(x: 25, y: self.pinCode.frame.maxY + 35, width: (self.frame.width - 75) / 2.0, height: 40)).cornerRadius(20).backgroundColor(UIColor(0xEFF4FF)).textColor(UIColor(0x756E98), .normal).title("spatial_voice_cancel".localized_spatial(), .normal).font(.systemFont(ofSize: 16, weight: .semibold)).tag(30).addTargetFor(self, action: #selector(buttonAction(_:)), for: .touchUpInside)

    lazy var confirm: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.frame.width / 2.0 + 10, y: self.pinCode.frame.maxY + 35, width: (self.frame.width - 75) / 2.0, height: 40)).cornerRadius(20).backgroundColor(UIColor(0xEFF4FF)).textColor(.white, .normal).title("spatial_voice_confirm".localized_spatial(), .normal).font(.systemFont(ofSize: 16, weight: .semibold)).setGradient([UIColor(0x0B8AF2), UIColor(0x2753FF)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).tag(31).addTargetFor(self, action: #selector(buttonAction(_:)), for: .touchUpInside)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubViews([title, pinCode, cancel, confirm])
        pinCode.inputFinish = { [weak self] in
            self?.code = $0
        }
        pinCode.textValueChange = { [weak self] in
            self?.code = $0
        }
        pinCode.textFiled.becomeFirstResponder()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc func buttonAction(_ sender: UIButton) {
        if actionEvents != nil {
            DispatchQueue.main.async {
                self.actionEvents!(sender.tag)
            }
        }
    }
}
