//
//  VoiceRoomInputBar.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/5.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomInputBar: UIView,UITextViewDelegate {
    
    var keyboardHeight = CGFloat(0)
    
    public var sendClosure: ((String)->())?
    
    public var changeEmojiClosure: ((Bool)->())?
        
    lazy var rightView: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: 4.5, width: 27, height: 27)).addTargetFor(self, action: #selector(changeToEmoji), for: .touchUpInside)
    }()
    
    lazy var inputContainer: UIView = {
        UIView(frame: CGRect(x: 15, y: 13, width: ScreenWidth-110, height: 36)).cornerRadius(18).layerProperties(UIColor(0xE4E3ED), 1).backgroundColor(.white)
    }()
    
    public lazy var inputField: PlaceHolderTextView = {
        PlaceHolderTextView(frame: CGRect(x: 20, y: 14, width: ScreenWidth-146, height: 34)).delegate(self).font(.systemFont(ofSize: 16, weight: .regular)).backgroundColor(.clear).textColor(.darkText)
    }()
    
    lazy var send: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: ScreenWidth - 82, y: 12, width: 67, height: 36)).cornerRadius(18).setGradient([UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1),UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)]).title(LanguageManager.localValue(key: "Send"), .normal).textColor(.white, .normal).font(.systemFont(ofSize: 16, weight: .regular)).addTargetFor(self, action: #selector(sendMessage), for: .touchUpInside)
    }()
    
    private var limitCount: Int {
        var count = 30
        if NSLocale.preferredLanguages.first!.hasPrefix("en") {
            count = 80
        }
        return count
    }
    
    let line = UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 1)).backgroundColor(.white)
    
    var emoji: VoiceRoomEmojiListView?
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.rightView.setImage(UIImage("face"), for: .normal)
        self.rightView.setImage(UIImage("key"), for: .selected)
        self.addSubViews([self.inputContainer,self.inputField,self.send,self.line])
        self.inputField.tintColor = UIColor(0x009FFF)
        self.inputField.placeHolder = "Aa"
        self.inputField.placeHolderColor = UIColor(0xB6B8C9)
        self.inputField.returnKeyType = .send
        var orgContainerInset = self.inputField.textContainerInset
        orgContainerInset.left = 6
        self.inputField.textContainerInset = orgContainerInset
        self.inputField.returnKeyType = .send
        
        let view = UIView(frame: CGRect(x: self.inputContainer.frame.width-self.inputField.frame.height , y: 0, width: self.inputField.frame.height, height: self.inputField.frame.height)).backgroundColor(.white)
        view.addSubview(self.rightView)
        self.inputContainer.addSubview(view)
        self.setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1),UIColor(red: 1, green: 1, blue: 1, alpha: 0)], [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIApplication.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIApplication.keyboardWillHideNotification, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func sendMessage() {
        self.hiddenInputBar()
        self.rightView.isSelected = false
        if self.sendClosure != nil,!self.inputField.attributedText.toString().isEmpty {
            self.sendClosure!(self.inputField.attributedText.toString().trimmingCharacters(in:.whitespacesAndNewlines))
        }
    }

    public func textViewDidEndEditing(_ textView: UITextView) {
        
    }
    
    public func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if text == "\n" {
            self.sendMessage()
            return false
        } else {
            if textView.attributedText.length >= self.limitCount,!text.isEmpty {
                let string = textView.text as NSString
                textView.text = string.substring(to: self.limitCount)
                self.superview?.makeToast("Reach Limit!", point: CGPoint(x: self.center.x, y: ZNavgationHeight), title: nil, image: nil, completion: nil)
                return false
            }
        }
        return true
    }

    
    @objc func changeToEmoji() {
        self.rightView.isSelected = !self.rightView.isSelected
        if self.changeEmojiClosure != nil {
            self.changeEmojiClosure!(self.rightView.isSelected)
        }
        if self.rightView.isSelected {
            self.inputField.resignFirstResponder()
        } else {
            self.inputField.becomeFirstResponder()
        }
    }
    
    @objc private func keyboardWillShow(notification: Notification) {
        if !self.inputField.isFirstResponder {
            return
        }
        let frame = notification.keyboardEndFrame
        let duration = notification.keyboardAnimationDuration
        self.keyboardHeight = frame!.height
        UIView.animate(withDuration: duration!) {
            self.frame = CGRect(x: 0, y: ScreenHeight-60-frame!.height, width: ScreenWidth, height: 60)
        }
    }
    
    @objc private func keyboardWillHide(notification: Notification) {
        let frame = notification.keyboardEndFrame
        let duration = notification.keyboardAnimationDuration
        self.keyboardHeight = frame!.height
        self.frame = CGRect(x: 0, y: self.frame.origin.y, width: ScreenWidth, height: self.keyboardHeight+5+60)
        let emoji = VoiceRoomEmojiListView(frame: CGRect(x: 0, y: self.inputField.frame.maxY, width: ScreenWidth, height: self.keyboardHeight)).tag(124)
        self.emoji = emoji
        emoji.emojiClosure = { [weak self] in
            guard let `self` = self else { return }
            emoji.deleteEmoji.isEnabled = true
            emoji.deleteEmoji.isUserInteractionEnabled = true
            if self.inputField.attributedText.length <= self.limitCount {
                self.inputField.attributedText = self.convertText(text: self.inputField.attributedText, key: $0)
            } else {
                self.superview?.makeToast("Reach Limit!", point: CGPoint(x: self.center.x, y: ZNavgationHeight), title: nil, image: nil, completion: nil)
            }
        }
        emoji.deleteClosure = { [weak self] in
            if self?.inputField.text?.count ?? 0 > 0 {
                self?.inputField.deleteBackward()
                emoji.deleteEmoji.isEnabled = true
                emoji.deleteEmoji.isUserInteractionEnabled = true
            } else {
                emoji.deleteEmoji.isEnabled = false
                emoji.deleteEmoji.isUserInteractionEnabled = false
            }
        }
        emoji.isHidden = true
        self.addSubview(emoji)
        UIView.animate(withDuration: duration!) {
            emoji.isHidden = false
        }
    }
    
    @objc public func hiddenInputBar() {
        self.inputField.resignFirstResponder()
        UIView.animate(withDuration: 0.3) {
            self.frame = CGRect(x: 0, y: ScreenHeight, width: ScreenWidth, height: self.keyboardHeight+60)
        }
        self.emoji?.removeFromSuperview()
        self.rightView.isSelected = false
    }
    
    func convertText(text: NSAttributedString?,key: String) -> NSAttributedString {
        let attribute = NSMutableAttributedString(attributedString: text!)
        let attachment = NSTextAttachment()
        attachment.image = UIImage(key)
        attachment.bounds = CGRect(x: 0, y: -3.5, width: 18, height: 18)
        let imageText = NSMutableAttributedString(attachment: attachment)
        imageText.addAttributes([.accessibilityTextCustom : key], range: NSMakeRange(0, imageText.length))
        attribute.append(imageText)
        return attribute
    }
}

public extension NSAttributedString {
    func toString() -> String {
        let result = NSMutableAttributedString(attributedString: self)
        var replaceList: [(NSRange, String)] = []
        result.enumerateAttribute(.accessibilityTextCustom, in: NSRange(location: 0, length: result.length), using: { value, range, _ in
            if let value = value as? String {
                for i in range.location..<range.location + range.length {
                    replaceList.append((NSRange(location: i, length: 1), value))
                }
            }
        })
        for i in replaceList.reversed() {
            result.replaceCharacters(in: i.0, with: i.1)
        }
        return result.string
    }
}
