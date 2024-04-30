//
//  ChatInputView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/10.
//

import UIKit

private let kTextFiledHeight: CGFloat = 36
let kChatInputViewHeight: CGFloat = 56

protocol ChatInputViewDelegate: UITextFieldDelegate {
    func onEndEditing()
    func onClickSendButton(text: String)
}


class ChatInputView: UIView {
    weak var delegate: ChatInputViewDelegate?
    private var observerArray: [NSObjectProtocol] = []
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        view.layer.cornerRadius = kTextFiledHeight * 0.5
        view.layer.masksToBounds = true
        view.layer.borderWidth = 1
        view.layer.borderColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.6).cgColor
        view.backgroundColor = .joy_chat_input_bg
        return view
    }()
    
    lazy var textField: UITextField = {
        let textField = UITextField()
        textField.delegate = self
        textField.font = .joy_R_13
        textField.textColor = .joy_chat_input_text
        textField.returnKeyType = .send
        return textField
    }()
    
    deinit {
        observerArray.forEach { observe in
            NotificationCenter.default.removeObserver(observe)
        }
        observerArray.removeAll()
    }
        
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
        addObserver()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func addObserver(){
        let observe1 =
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillShowNotification, object: nil, queue: nil) { [weak self] notify in
            guard let self = self else {return}
            guard let keyboardRect = (notify.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
            guard let duration = notify.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else { return }
            let keyboradHeight = keyboardRect.size.height
            self.snp.updateConstraints { make in
                make.bottom.equalToSuperview().offset(-keyboradHeight)
            }
            UIView.animate(withDuration: duration) {
                self.layoutIfNeeded()
            }
        }
        
        let observe2 =
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillHideNotification, object: nil, queue: nil) {[weak self] notify in
            guard let self = self else {return}
            self.snp.updateConstraints { make in
                make.bottom.equalToSuperview()
            }
        }
        observerArray.append(observe1)
        observerArray.append(observe2)
    }
    
    private func createSubviews(){
        backgroundColor = .clear
        
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.height.equalTo(kTextFiledHeight)
            make.left.equalTo(12)
            make.right.equalTo(-12)
        }
        
        bgView.addSubview(textField)
        textField.snp.makeConstraints { make in
            make.top.bottom.equalToSuperview()
            make.left.equalTo(16)
            make.right.equalTo(-16)
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


extension ChatInputView: UITextFieldDelegate {
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        delegate?.onEndEditing()
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        sendAction()
        return true
    }
    
}
