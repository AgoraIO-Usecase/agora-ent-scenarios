//
//  VoiceCreateRoomView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/25.
//

import Foundation
import UIKit

enum VRCreateRoomActionType: Int {
    case normal = 0
    case encrypt = 1
    case showKeyboard = 2
}

protocol VRCreateRoomViewDelegate: AnyObject {
    func createBtnAction(_ roomModel: VRRoomEntity)
    func didCreateRoomAction(_ type: VRCreateRoomActionType)
}

class VoiceCreateRoomView: UIView {
    weak var delegate: VRCreateRoomViewDelegate?
    let screenW: CGFloat = UIScreen.main.bounds.width
    var inputTF: UITextField!
    var screatView: UIView!
    var iconImgView: UIImageView!
    var publicBtn: UIButton!
    var screatBtn: UIButton!
    var addRoomModel: VRRoomEntity!
    var warningView: UIView!
    var enBtn: UIButton!
    var setLabel: UILabel!
    
    init(frame: CGRect, withDelegate delegate: VRCreateRoomViewDelegate) {
        super.init(frame: frame)
        self.delegate = delegate
        self.addRoomModel = VRRoomEntity()
        createRandomNumber()
        setupView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupView() {
        let weakSelf = self
        self.addRoomModel.is_private = false
        let text = LanguageManager.localValue(key: "game_create_tips")
        let font = UIFont.systemFont(ofSize: 12)
        let constraintSize = CGSize(width: self.width - 78, height: CGFloat.greatestFiniteMagnitude)
        let attributes = [NSAttributedString.Key.font: font]
        let textRect = text.boundingRect(with: constraintSize, options: [.usesLineFragmentOrigin, .usesFontLeading], attributes: attributes, context: nil)
        let textHeight = ceil(textRect.height)
        
        self.warningView = UIView(frame: CGRect(x: 20, y: 20, width: self.width - 40, height: textHeight + 10))
        self.warningView.backgroundColor = UIColor(hexString: "#FA396A1A")
        self.warningView.layer.cornerRadius = 5
        self.warningView.layer.masksToBounds = true
        self.addSubview(self.warningView)
        
        let warImgView = UIImageView(frame: CGRect(x: 10, y: 6, width: 16, height: 16))
        warImgView.image = UIImage.sceneImage(name: "zhuyi", bundleName: "VoiceChatRoomResource")
        self.warningView.addSubview(warImgView)
        
        let contentLabel = UILabel(frame: CGRect(x: 30, y: 5, width: self.warningView.width - 38, height: textHeight))
        contentLabel.numberOfLines = 0
        let attributedText = NSMutableAttributedString(string: text)
        attributedText.addAttribute(.foregroundColor, value: UIColor.black, range: NSRange(location: 0, length: 77))
        attributedText.addAttribute(.foregroundColor, value: UIColor.red, range: NSRange(location: 77, length: 41))
        contentLabel.font = UIFont.systemFont(ofSize: 12)
        contentLabel.attributedText = attributedText
        self.warningView.addSubview(contentLabel)
        
        let roomTitleLabel = UILabel(frame: CGRect(x: 20, y: self.warningView.bottom + 20, width: 70, height: 20))
        roomTitleLabel.font = UIFont.systemFont(ofSize: 14)
        roomTitleLabel.textColor = UIColor(hexString: "#000000")
        roomTitleLabel.text = LanguageManager.localValue(key: "game_room_name")
        self.addSubview(roomTitleLabel)
        
        let randomBtn = UIButton(type: .custom)
        randomBtn.setTitle(LanguageManager.localValue(key: "game_random"), for: .normal)
        randomBtn.setImage(UIImage.sceneImage(name: "online_create_randomIcon", bundleName: "VoiceChatRoomResource"), for: .normal)
        randomBtn.spacingBetweenImageAndTitle = 3
        randomBtn.contentHorizontalAlignment = .center
        randomBtn.setTitleColor(UIColor(hexString: "#3C4267"), for: .normal)
        randomBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        randomBtn.addTarget(self, action: #selector(randomBtnClickEvent), for: .touchUpInside)
        self.addSubview(randomBtn)
        randomBtn.sizeToFit()
        randomBtn.frame = CGRect(x: screenW - randomBtn.width - 20, y: roomTitleLabel.top, width: randomBtn.width, height: 20)
        
        let inputBgView = UIView(frame: CGRect(x: 20, y: roomTitleLabel.bottom + 8, width: screenW - 40, height: 48))
        inputBgView.layer.cornerRadius = 8
        inputBgView.layer.masksToBounds = true
        inputBgView.backgroundColor = UIColor(hexString: "#F5F8FF")
        self.addSubview(inputBgView)
        
        self.inputTF = UITextField(frame: CGRect(x: 12, y: 9, width: inputBgView.width - 24, height: 30))
        self.inputTF.accessibilityIdentifier = "voice_chat_create_room_name_tf"
        self.inputTF.textColor = UIColor(hexString: "#040925")
        self.inputTF.font = UIFont.systemFont(ofSize: 18)
        self.inputTF.clearButtonMode = .whileEditing
        self.inputTF.tintColor = UIColor(hexString: "#345DFF")
        inputBgView.addSubview(self.inputTF)
        self.inputTF.delegate = self
        
        let secretLabel = UILabel(frame: CGRect(x: 20, y: inputBgView.bottom + 24, width: 100, height: 20))
        secretLabel.font = UIFont.systemFont(ofSize: 14)
        secretLabel.textColor = UIColor(hexString: "#000000")
        secretLabel.text = LanguageManager.localValue(key: "game_room_access")
        secretLabel.sizeToFit()
        self.addSubview(secretLabel)
        
        self.enBtn = UIButton(frame: CGRect(x: secretLabel.right + 8, y: inputBgView.bottom + 24, width: 32, height: 20))
        self.enBtn.setBackgroundImage(UIImage.sceneImage(name: "guan", bundleName: "VoiceChatRoomResource"), for: .normal)
        self.enBtn.setBackgroundImage(UIImage.sceneImage(name: "open", bundleName: "VoiceChatRoomResource"), for: .selected)
        self.enBtn.addTarget(self, action: #selector(enChange), for: .touchUpInside)
        self.addSubview(self.enBtn)
        
        self.setLabel = UILabel(frame: CGRect(x: self.width - 170, y: inputBgView.bottom + 25.5, width: 150, height: 17))
        self.setLabel.font = UIFont.systemFont(ofSize: 12)
        self.setLabel.textColor = UIColor(hexString: "#FA396A")
        self.setLabel.text = LanguageManager.localValue(key: "game_enter_4_digit_password")
        self.setLabel.sizeToFit()
        self.setLabel.isHidden = true
        self.addSubview(self.setLabel)
        
        self.screatView = UIView(frame: CGRect(x: 20, y: self.setLabel.bottom + 8, width: screenW - 40, height: 48 ))
        self.screatView.isHidden = true
        self.addSubview(self.screatView)
        
        let pwdView = VerifyCodeView(frame: CGRect(x: 0, y: 0, width: screenW - 40, height: 48), codeNumbers: 4, space: 10, padding: 0)
        pwdView.inputFinish = { pwd in
            weakSelf.addRoomModel.roomPassword = pwd
            if let delegate = weakSelf.delegate {
                delegate.didCreateRoomAction(weakSelf.addRoomModel.is_private ? .encrypt : .normal)
            }
        }
        self.screatView.addSubview(pwdView)
        
        let createBtn = UIButton()
        createBtn.setBackgroundImage(UIImage.sceneImage(name: "createRoomBtn", bundleName: "VoiceChatRoomResource"), for: .normal)
        createBtn.accessibilityIdentifier = "voice_chat_create_room_next_btn"
        createBtn.adjustsImageWhenHighlighted = false
        createBtn.contentMode = .scaleAspectFit
        createBtn.addTarget(self, action: #selector(createBtnClickEvent), for: .touchUpInside)
        self.addSubview(createBtn)
        
        createBtn.snp.makeConstraints { make in
            make.left.equalTo(self.snp_left).offset(30)
            make.right.equalTo(self.snp_right).offset(-30)
            make.height.equalTo(48)
            make.bottom.equalTo(self.snp_bottom).offset(-20)
        }
        
        randomBtnClickEvent()
    }
    
    @objc func randomBtnClickEvent() {
        createRandomNumber()
        self.inputTF.text = self.addRoomModel.name
    }
    
    @objc func enChange(_ btn: UIButton) {
        btn.isSelected = !btn.isSelected
        addRoomModel.is_private = btn.isSelected
        screatView.isHidden = !btn.isSelected
        setLabel.isHidden = !btn.isSelected
        self.endEditing(true)
        if let delegate = delegate {
            delegate.didCreateRoomAction(btn.isSelected ? .encrypt : .normal)
        }
    }
    
    @objc func createBtnClickEvent() {
        guard inputTF.text?.count ?? 0 > 0 else {
            return
        }
        addRoomModel.name = inputTF.text
        if let delegate = delegate{
            delegate.createBtnAction(addRoomModel)
        }
    }
    
    func createRandomNumber() {
        let roomNameKey = "show_create_room_name\(Int(arc4random_uniform(21) + 1))"
        let name = LanguageManager.localValue(key: roomNameKey)

        addRoomModel.name = name
    }
    
    func itemBtnClickEvent(_ sender: UIButton) {
        if sender.tag == 0 {
            screatBtn.isSelected = false
            publicBtn.isSelected = true
            addRoomModel.is_private = false
        } else {
            publicBtn.isSelected = false
            screatBtn.isSelected = true
            addRoomModel.is_private = true
        }
        screatView.isHidden = !screatBtn.isSelected
    }
    
    
}

extension VoiceCreateRoomView: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        if let delegate = delegate{
            delegate.didCreateRoomAction(self.addRoomModel.is_private ? .encrypt : .normal)
        }
        return true
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.endEditing(true)
        if let delegate = delegate{
            delegate.didCreateRoomAction(self.addRoomModel.is_private ? .encrypt : .normal)
        }
    }
}
