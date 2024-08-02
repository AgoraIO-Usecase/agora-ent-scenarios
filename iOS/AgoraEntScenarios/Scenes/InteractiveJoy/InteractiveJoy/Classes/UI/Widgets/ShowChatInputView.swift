//
//  ShowChatInputView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/10.
//

import UIKit

private let kTextFiledHeight: CGFloat = 36

protocol ShowChatInputViewDelegate: UITextFieldDelegate {
    func onEndEditing()
    func onClickEmojiButton()
    func onClickSendButton(text: String)
}


class ShowChatInputView: UIView {
    
    weak var delegate: ShowChatInputViewDelegate?
    
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "F1F3F8")
        view.layer.cornerRadius = kTextFiledHeight * 0.5
        view.layer.masksToBounds = true
        return view
    }()
    
    lazy var textField: UITextField = {
        let textField = UITextField()
        textField.delegate = self
        textField.font = UIFont.joy_R_14
        textField.textColor = UIColor(hexString: "3C4267")
        textField.returnKeyType = .send
        return textField
    }()
    
    private lazy var sendButton: UIButton = {
        let button = UIButton(type: .custom)
        button.titleLabel?.font = UIFont.joy_R_14
        button.setTitleColor(UIColor(red: 1, green: 1, blue: 1, alpha: 1), for: .normal)
        button.setBackgroundImage(UIImage.sceneImage(name: "show_live_chat_bar_send@"), for: .normal)
        button.setTitle(LanguageManager.localValue(key: "dialog_selected_send"), for: .normal)
        button.addTarget(self, action: #selector(didClickSendButton), for: .touchUpInside)
        return button
    }()
        	
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        backgroundColor = .white
        
        addSubview(sendButton)
        sendButton.snp.makeConstraints { make in
            make.right.equalTo(-15)
            make.centerY.equalToSuperview()
        }
        
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.height.equalTo(kTextFiledHeight)
            make.left.equalTo(15)
            make.right.equalTo(sendButton.snp.left).offset(-20)
        }
        
        
        bgView.addSubview(textField)
        textField.snp.makeConstraints { make in
            make.left.equalTo(10)
            make.top.bottom.equalToSuperview()
            make.right.equalTo(-10)
        }
    }
    
    @objc private func didClickSendButton(){
       sendAction()
    }
    
    private func sendAction(){
        if let text = textField.text?.trimmingCharacters(in: .whitespacesAndNewlines), text.count > 0 {
            delegate?.onClickSendButton(text: text)
        }
        textField.text = nil
        textField.resignFirstResponder()
    }
    
}


extension ShowChatInputView: UITextFieldDelegate {
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        delegate?.onEndEditing()
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        sendAction()
        return true
    }
    
}
