//
//  VRCreateRoomInputView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit
import ZSwiftBaseLib
import AgoraCommon
public class VRCreateRoomInputView: UIView, UITextFieldDelegate {
    var code = ""

    var name = ""

    var action: (() -> Void)?
    
    var isPrivate: Bool = false
    
    var privateBlock: ((Bool)->Void)?

    private let PinHeight = ((ScreenWidth - 90 - 3 * 16) / 4.0) * (53 / 60.0)

    private let codeMessage = LanguageManager.localValue(key: "voice_enter_4_digit_password")

    private let nameMessage = LanguageManager.localValue(key: "voice_please_set_a_name")

    private var offset = CGFloat(ScreenHeight < 812 ? 150 : 120)

    var oldCenter: CGPoint = .zero

    lazy var roomName: UILabel = .init(frame: CGRect(x: 40, y: 0, width: 80, height: 20)).font(.systemFont(ofSize: 14, weight: .regular)).text(LanguageManager.localValue(key: "voice_room_name")).textColor(.darkText).backgroundColor(.clear)

    lazy var randomName: UIButton = .init(type: .custom).frame(CGRect(x: ScreenWidth - 120, y: 0, width: 80, height: 20)).backgroundColor(.clear).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x3C4267), .normal)

    lazy var roomBackground: UIView = .init(frame: CGRect(x: 30, y: self.roomName.frame.maxY + 10, width: ScreenWidth - 60, height: 50)).cornerRadius(25).backgroundColor(.white)

    lazy var roomNameField: UITextField = .init(frame: CGRect(x: 45, y: self.roomName.frame.maxY + 15, width: ScreenWidth - 90, height: 40)).placeholder(LanguageManager.localValue(key: "voice_set_room_name")).font(.systemFont(ofSize: 18, weight: .regular)).textColor(.darkText).delegate(self)

    lazy var roomEncryption: UILabel = .init(frame: CGRect(x: self.roomName.frame.minX, y: self.roomBackground.frame.maxY + 12, width: 60, height: 20)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.darkText).text(LanguageManager.localValue(key: "voice_room_access")).backgroundColor(.clear)

    lazy var encryptBtn: UIButton = .init(frame: CGRect(x: Int(self.roomEncryption.frame.maxX + 20.0), y: Int(self.roomBackground.frame.maxY) + 12, width: 32, height: 20)).addTargetFor(self, action: #selector(encrypt), for: .touchUpInside)
    
//    lazy var publicChoice: UIButton = .init(type: .custom).frame(CGRect(x: self.roomEncryption.frame.minX, y: self.roomEncryption.frame.maxY + 12, width: 90, height: 32)).title(LanguageManager.localValue(key: "voice_public"), .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x3C4267), .normal).backgroundColor(.clear).tag(21).addTargetFor(self, action: #selector(chooseEncryption(_:)), for: .touchUpInside)
//
//    lazy var privateChoice: UIButton = .init(type: .custom).frame(CGRect(x: self.publicChoice.frame.maxX + 20, y: self.roomEncryption.frame.maxY + 12, width: 90, height: 32)).title(LanguageManager.localValue(key: "voice_private"), .normal).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x3C4267), .normal).backgroundColor(.clear).tag(22).addTargetFor(self, action: #selector(chooseEncryption(_:)), for: .touchUpInside)

    lazy var pinCode: VerifyCodeView = .init(frame: CGRect(x: 0, y: self.roomEncryption.frame.maxY + 15, width: ScreenWidth, height: self.PinHeight), codeNumbers: 4, space: 16, padding: 45)

    lazy var warnMessage: UILabel = .init(frame: CGRect(x: self.frame.size.width - 120, y: self.roomEncryption.frame.origin.y, width: ScreenWidth - 80, height: 20)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0xFA396A)).text(self.codeMessage)

    lazy var timeLimit: UIButton = .init(type: .custom).frame(CGRect(x: 30, y: self.frame.height - CGFloat(ZTabbarHeight) - 58, width: ScreenWidth - 60, height: 40)).isUserInteractionEnabled(false).attributedTitle(self.timeWarning, .normal)

    lazy var timeWarning: NSAttributedString = {
        var space = NSMutableAttributedString(string: "")
        if NSLocale.preferredLanguages.first!.hasPrefix("zh") {
            space = NSMutableAttributedString(string: "           ")
        }
        let attachment = NSTextAttachment()
        attachment.image = UIImage.sceneImage(name: "candel", bundleName: "VoiceChatRoomResource")
        attachment.bounds = CGRect(x: 0, y: -3, width: 14, height: 14)
        let host = NSMutableAttributedString(attachment: attachment)
        space.append(host)
        space.append(NSAttributedString {
            AttributedText("voice_demo_test_desc".voice_localized()).foregroundColor(UIColor(0x3C4267)).font(.systemFont(ofSize: 12, weight: .regular)).lineBreakeMode(.byCharWrapping).alignment(.center)
        })
        return space
    }()
    
    

    override public init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = true
        encryptBtn.setBackgroundImage(UIImage.sceneImage(name: "guan", bundleName: "VoiceChatRoomResource"), for: .normal)
        encryptBtn.setBackgroundImage(UIImage.sceneImage(name: "open", bundleName: "VoiceChatRoomResource"), for: .selected)
        roomNameField.accessibilityIdentifier = "voice_chat_create_room_name_tf"
        addSubViews([roomName, randomName, roomBackground, roomNameField, roomEncryption, encryptBtn, pinCode, warnMessage])
        timeLimit.titleLabel?.numberOfLines = 0

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
        
//        create.titleLabel?.accessibilityIdentifier = "voice_chat_create_room_next_btn"
        
        warnMessage.isHidden = true
 
    }

    private func stateImage(button: UIButton) {
        button.setImage(UIImage.sceneImage(name: "selected", bundleName: "VoiceChatRoomResource"), for: .selected)
        button.setImage(UIImage.sceneImage(name: "unselected", bundleName: "VoiceChatRoomResource"), for: .normal)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension VRCreateRoomInputView {
    private func setupAttributes() {
        pinCode.alpha = 0
        randomName.set(image:UIImage.sceneImage(name: "random", bundleName: "VoiceChatRoomResource"), title: LanguageManager.localValue(key: "voice_random"), titlePosition: .right, additionalSpacing: 5, state: .normal)
//        stateImage(button: publicChoice)
//        stateImage(button: privateChoice)
//        publicChoice.titleEdgeInsets = UIEdgeInsets(top: publicChoice.titleEdgeInsets.top, left: 10, bottom: publicChoice.titleEdgeInsets.bottom, right: publicChoice.titleEdgeInsets.right)
//        privateChoice.titleEdgeInsets(UIEdgeInsets(top: privateChoice.titleEdgeInsets.top, left: 10, bottom: privateChoice.titleEdgeInsets.bottom, right: privateChoice.titleEdgeInsets.right))
//        publicChoice.isSelected = true
//        publicChoice.contentHorizontalAlignment = .left
    }
    
    @objc private func encrypt(_ btn: UIButton) {
        btn.isSelected = !btn.isSelected
        guard let block = privateBlock else {return}
        if !btn.isSelected {
//            privateChoice.isSelected = false
//            publicChoice.isSelected = true
            recover()
            warnMessage.isHidden = true
//            endEditing(true)
            //timeLimit.isHidden = false
            self.isPrivate = false
            block(false)
        } else {
           // timeLimit.isHidden = ScreenHeight < 812
            warnMessage.isHidden = false
//            privateChoice.isSelected = true
//            publicChoice.isSelected = false
//            roomNameField.resignFirstResponder()
//            pinCode.textFiled.becomeFirstResponder()
            self.isPrivate = true
            block(true)
        }
        UIView.animate(withDuration: 0.3) {
            self.pinCode.alpha = btn.isSelected ? 1 : 0
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
        if self.isPrivate {
            if let name = roomNameField.text,!name.isEmpty,!self.code.isEmpty {
                if action != nil {
                    action!()
                }
            } else {
                if roomNameField.text?.isEmpty ?? true {
                    superview?.superview?.makeToast("voice_no_room_name".voice_localized(), point: superview?.superview?.center ?? .zero, title: nil, image: nil, completion: nil)
                }
            }
        } else {
            if let name = roomNameField.text,!name.isEmpty {
                if action != nil {
                    action!()
                }
            } else {
                superview?.superview?.makeToast("voice_no_room_name".voice_localized(), point: superview?.superview?.center ?? .zero, title: nil, image: nil, completion: nil)
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
