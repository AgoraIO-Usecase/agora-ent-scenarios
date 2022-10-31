//
//  VRCreateRoomInputView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit
import ZSwiftBaseLib

public class VRCreateRoomInputView: UIView, UITextFieldDelegate {
    var code = ""

    var name = ""

    var action: (() -> Void)?

    private let PinHeight = ((ScreenWidth - 90 - 3 * 16) / 4.0) * (53 / 60.0)

    private let codeMessage = LanguageManager.localValue(key: "Enter 4 Digit Password")

    private let nameMessage = LanguageManager.localValue(key: "Please set a name")

    private var offset = CGFloat(ScreenHeight < 812 ? 150 : 120)

    var oldCenter: CGPoint = .zero

    lazy var roomName: UILabel = .init(frame: CGRect(x: 40, y: 0, width: 80, height: 20)).font(.systemFont(ofSize: 14, weight: .regular)).text(LanguageManager.localValue(key: "Room Name")).textColor(.darkText).backgroundColor(.clear)

    lazy var randomName: UIButton = .init(type: .custom).frame(CGRect(x: ScreenWidth - 120, y: 0, width: 80, height: 20)).backgroundColor(.clear).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x3C4267), .normal)

    lazy var roomBackground: UIView = .init(frame: CGRect(x: 30, y: self.roomName.frame.maxY + 10, width: ScreenWidth - 60, height: 50)).cornerRadius(25).backgroundColor(.white)

    lazy var roomNameField: UITextField = .init(frame: CGRect(x: 45, y: self.roomName.frame.maxY + 15, width: ScreenWidth - 90, height: 40)).placeholder(LanguageManager.localValue(key: "Set Room Name")).font(.systemFont(ofSize: 18, weight: .regular)).textColor(.darkText).delegate(self)

    lazy var roomEncryption: UILabel = .init(frame: CGRect(x: self.roomName.frame.minX, y: self.roomBackground.frame.maxY + 12, width: 150, height: 20)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.darkText).text(LanguageManager.localValue(key: "Room Access")).backgroundColor(.clear)

    lazy var publicChoice: UIButton = .init(type: .custom).frame(CGRect(x: self.roomEncryption.frame.minX, y: self.roomEncryption.frame.maxY + 12, width: 90, height: 32)).title(LanguageManager.localValue(key: "Public"), .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x3C4267), .normal).backgroundColor(.clear).tag(21).addTargetFor(self, action: #selector(chooseEncryption(_:)), for: .touchUpInside)

    lazy var privateChoice: UIButton = .init(type: .custom).frame(CGRect(x: self.publicChoice.frame.maxX + 20, y: self.roomEncryption.frame.maxY + 12, width: 90, height: 32)).title(LanguageManager.localValue(key: "Private"), .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x3C4267), .normal).backgroundColor(.clear).tag(22).addTargetFor(self, action: #selector(chooseEncryption(_:)), for: .touchUpInside)

    lazy var pinCode: VRVerifyCodeView = .init(frame: CGRect(x: 0, y: self.publicChoice.frame.maxY + 15, width: ScreenWidth, height: self.PinHeight), codeNumbers: 4, space: 16, padding: 45)

    lazy var warnMessage: UILabel = .init(frame: CGRect(x: self.publicChoice.frame.minX, y: self.pinCode.frame.maxY + 12, width: ScreenWidth - 80, height: 20)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0xFA396A)).text(self.codeMessage)

    lazy var timeLimit: UIButton = .init(type: .custom).frame(CGRect(x: 30, y: self.frame.height - CGFloat(ZTabbarHeight) - 58, width: ScreenWidth - 60, height: 40)).isUserInteractionEnabled(false).attributedTitle(self.timeWarning, .normal)

    lazy var timeWarning: NSAttributedString = {
        var space = NSMutableAttributedString(string: "")
        if NSLocale.preferredLanguages.first!.hasPrefix("zh") {
            space = NSMutableAttributedString(string: "           ")
        }
        let attachment = NSTextAttachment()
        attachment.image = UIImage("candel")
        attachment.bounds = CGRect(x: 0, y: -3, width: 14, height: 14)
        let host = NSMutableAttributedString(attachment: attachment)
        space.append(host)
        space.append(NSAttributedString {
            AttributedText("This product is for testing only, not for commerce.\nEvery room will be maintained for 10 minutes.".localized()).foregroundColor(UIColor(0x3C4267)).font(.systemFont(ofSize: 12, weight: .regular)).lineBreakeMode(.byCharWrapping).alignment(.center)
        })
        return space
    }()

    lazy var create: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 30, y: self.frame.height - CGFloat(ZTabbarHeight), width: ScreenWidth - 60, height: 48)).cornerRadius(24).title(LanguageManager.localValue(key: "Next"), .normal).textColor(.white, .normal).font(.systemFont(ofSize: 16, weight: .semibold)).addTargetFor(self, action: #selector(createAction), for: .touchUpInside).setGradient([UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1), UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()

    lazy var createContainer: UIView = .init(frame: CGRect(x: 30, y: self.frame.height - CGFloat(ZTabbarHeight), width: ScreenWidth - 60, height: 48)).backgroundColor(.white)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = true
        addSubViews([roomName, randomName, roomBackground, roomNameField, roomEncryption, publicChoice, privateChoice, pinCode, warnMessage, timeLimit, createContainer, create])
        timeLimit.titleLabel?.numberOfLines = 0
        createContainer.layer.cornerRadius = 24
        createContainer.layer.shadowRadius = 8
        createContainer.layer.shadowOffset = CGSize(width: 0, height: 4)
        createContainer.layer.shadowColor = UIColor(red: 0, green: 0.55, blue: 0.98, alpha: 0.2).cgColor
        createContainer.layer.shadowOpacity = 1
        setupAttributes()
        pinCode.textValueChange = { [weak self] in
            self?.code = $0
        }
        pinCode.beginEdit = { [weak self] in
            self?.raise()
        }
        pinCode.inputFinish = { [weak self] in
            self?.code = $0
            self?.recover()
        }
        create.layer.shadowColor = UIColor(red: 0, green: 0.546, blue: 0.979, alpha: 0.2).cgColor
        create.layer.shadowOpacity = 1
        create.layer.shadowRadius = 8
        create.layer.shadowOffset = CGSize(width: 0, height: 4)
        warnMessage.isHidden = true
    }

    private func stateImage(button: UIButton) {
        button.setImage(UIImage("selected"), for: .selected)
        button.setImage(UIImage("unselected"), for: .normal)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension VRCreateRoomInputView {
    private func setupAttributes() {
        pinCode.alpha = 0
        randomName.set(image: UIImage("random"), title: LanguageManager.localValue(key: "Random"), titlePosition: .right, additionalSpacing: 5, state: .normal)
        stateImage(button: publicChoice)
        stateImage(button: privateChoice)
        publicChoice.titleEdgeInsets = UIEdgeInsets(top: publicChoice.titleEdgeInsets.top, left: 10, bottom: publicChoice.titleEdgeInsets.bottom, right: publicChoice.titleEdgeInsets.right)
        privateChoice.titleEdgeInsets(UIEdgeInsets(top: privateChoice.titleEdgeInsets.top, left: 10, bottom: privateChoice.titleEdgeInsets.bottom, right: privateChoice.titleEdgeInsets.right))
        publicChoice.isSelected = true
        publicChoice.contentHorizontalAlignment = .left
    }

    @objc internal func chooseEncryption(_ sender: UIButton) {
        if sender.tag == 21 {
            privateChoice.isSelected = false
            publicChoice.isSelected = true
            recover()
            warnMessage.isHidden = true
            endEditing(true)
            timeLimit.isHidden = false
        } else {
            timeLimit.isHidden = ScreenHeight < 812
            warnMessage.isHidden = false
            privateChoice.isSelected = true
            publicChoice.isSelected = false
            roomNameField.resignFirstResponder()
            pinCode.textFiled.becomeFirstResponder()
        }
        UIView.animate(withDuration: 0.3) {
            self.pinCode.alpha = self.publicChoice.isSelected ? 0 : 1
        }
    }

    private func recover() {
        if superview!.center.y >= oldCenter.y { return }
        UIView.animate(withDuration: 0.3) {
            self.superview?.center = CGPoint(x: self.superview!.center.x, y: self.superview!.center.y + self.offset)
        }
    }

    private func raise() {
        UIView.animate(withDuration: 0.3) {
            self.superview?.center = CGPoint(x: self.superview!.center.x, y: self.superview!.center.y - self.offset)
        }
    }

    @objc private func createAction() {
        if privateChoice.isSelected {
            if let name = roomNameField.text,!name.isEmpty,!self.code.isEmpty {
                if action != nil {
                    action!()
                }
            } else {
                if roomNameField.text?.isEmpty ?? true {
                    superview?.superview?.makeToast("No Room Name".localized(), point: superview?.superview?.center ?? .zero, title: nil, image: nil, completion: nil)
                }
            }
        } else {
            if let name = roomNameField.text,!name.isEmpty {
                if action != nil {
                    action!()
                }
            } else {
                superview?.superview?.makeToast("No Room Name".localized(), point: superview?.superview?.center ?? .zero, title: nil, image: nil, completion: nil)
            }
        }
    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        true
    }

    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        true
    }

    func textFieldDidEndEditing(_ textField: UITextField) {
        name = textField.text ?? ""
    }

    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if textField != roomNameField { return false }
        if !(textField.text ?? "").isEmpty {
            if let text = textField.text, text.count >= 32,!string.isEmpty {
                textField.text = (text as NSString).substring(to: 32)
                return false
            } else {
                return true
            }
        }
        return true
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        pinCode.resignFirstResponder()
        endEditing(true)
    }
}
