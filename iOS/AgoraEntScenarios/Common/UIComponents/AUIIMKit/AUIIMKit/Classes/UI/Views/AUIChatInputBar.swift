//
//  AUIChatInputBar.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit

public func getInput() -> AUIChatInputBar? {
    guard let window = getWindow() else {return nil}
    for subView in window.subviews {
        if let input = subView as? AUIChatInputBar {
            return input
        }
    }
    return nil
}

/*!
 *  \~Chinese
 *  文字以及表情输入框
 *
 *  \~English
 *  Text and emoticon input box
 *
 */
public class AUIChatInputBar: UIView, UITextViewDelegate {
    
    var keyboardHeight = CGFloat(0)
    /*!
     *  \~Chinese
     *  发送按钮回调包含要发送的字符串
     *
     *  \~English
     *  The send button callback contains the string to send
     *
     */
    public var sendClosure: ((String) -> Void)?
    /*!
     *  \~Chinese
     *  点击切换表情与文字键盘的回调，可用于事件统计
     *
     *  \~English
     *  Click to switch the callback of emoticon and text keyboard, which can be used for event statistics
     *
     */
    public var changeEmojiClosure: ((Bool) -> Void)?
    
    lazy var rightView: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: 4.5, width: 27, height: 27)).addTargetFor(self, action: #selector(changeToEmoji), for: .touchUpInside).backgroundColor(.clear)
    }()
    
    lazy var inputContainer: UIView = {
        UIView(frame: CGRect(x: 15, y: 13, width: AScreenWidth - 110, height: 36)).cornerRadius(18)
            .backgroundColor(AUIChatTheme.shared.inputbar.inputBackgroundColor)
    }()
    
    public lazy var inputField: PlaceHolderTextView = {
        PlaceHolderTextView(frame: CGRect(x: 20, y: 14, width: AScreenWidth - 146, height: 34))
            .delegate(self)
            .font(.systemFont(ofSize: 16, weight: .regular))
            .backgroundColor(.clear)
            .textColor(AUIChatTheme.shared.inputbar.textColor)
    }()
    
    lazy var send: UIButton = {
        UIButton(type: .custom)
            .frame(CGRect(x: AScreenWidth - 82, y: 12, width: 67, height: 36))
            .cornerRadius(18)
            .createThemeGradient(AUIChatTheme.shared.inputbar.sendGradientColors, self.config.sendGradientPoints)
            .title(aui_localized("Send"), .normal)
            .textColor(AUIChatTheme.shared.inputbar.sendColor, .normal)
            .font(AUIChatTheme.shared.inputbar.sendFont)
            .addTargetFor(self, action: #selector(sendMessage), for: .touchUpInside)
    }()
    
    private var limitCount: Int {
        var count = self.config.zhLimitCount
        if NSLocale.preferredLanguages.first!.hasPrefix("en") {
            count = self.config.enLimitCount
        }
        return count
    }
    
    var config = AUIChatInputBarConfig()
    
    let line = UIView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 1)).backgroundColor(.clear)
    
    var emoji: AUIEmojiView?
    
    override public init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc public convenience init(frame: CGRect,config: AUIChatInputBarConfig) {
        self.init(frame: frame)
        self.config = config
        self.rightView.setImage(UIImage.aui_Image(named: AUIChatTheme.shared.inputbar.emojiKeyboard)!, for: .normal)
        self.rightView.setImage(UIImage.aui_Image(named: AUIChatTheme.shared.inputbar.textKeyboard)!, for: .selected)
        self.addSubViews([self.inputContainer, self.inputField, self.send, self.line])
        self.inputField.tintColor = AUIChatTheme.shared.inputbar.cursorColor
        self.inputField.placeHolder = config.placeHolder
        self.inputField.placeHolderColor = config.placeHolderColor
        self.inputField.returnKeyType = config.returnKeyType
        self.inputField.textColor = AUIChatTheme.shared.inputbar.textColor
        var orgContainerInset = self.inputField.textContainerInset
        orgContainerInset.left = 6
        self.inputField.textContainerInset = orgContainerInset
        
        let view = UIView(frame: CGRect(x: self.inputContainer.frame.width - self.inputField.frame.height, y: 0, width: self.inputField.frame.height, height: self.inputField.frame.height)).backgroundColor(.clear)
        view.addSubview(self.rightView)
        self.inputContainer.addSubview(view)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIApplication.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIApplication.keyboardWillHideNotification, object: nil)
        self.backgroundColor = AUIChatTheme.shared.inputbar.backgroundColor
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        inputField.removeFromSuperview()
        emoji?.removeFromSuperview()
        emoji = nil
    }
    
    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func sendMessage() {
        self.hiddenInputBar()
        self.rightView.isSelected = false
        if self.sendClosure != nil,!self.inputField.attributedText.toString().isEmpty {
            self.sendClosure!(self.inputField.attributedText.toString().trimmingCharacters(in: .whitespacesAndNewlines))
        }
    }
    
    public func textViewDidEndEditing(_ textView: UITextView) {}
    
    public func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if text == "\n" {
            self.sendMessage()
            return false
        } else {
            if textView.attributedText.length >= self.limitCount,!text.isEmpty {
                let string = textView.text as NSString
                textView.text = string.substring(to: self.limitCount)
                return false
            }
        }
        return true
    }
    
    public override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        for view in subviews.reversed() {
            if view.isKind(of: type(of: view)),view.frame.contains(point){
                let childPoint = self.convert(point, to: view)
                let childView = view.hitTest(childPoint, with: event)
                return childView
            }
        }
        self.hiddenInputBar()
        return super.hitTest(point, with: event)
    }    

    @objc func changeToEmoji() {
        self.rightView.isSelected = !self.rightView.isSelected
        if self.changeEmojiClosure != nil {
            self.changeEmojiClosure!(rightView.isSelected)
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
        let frame = notification.a.keyboardEndFrame
        let duration = notification.a.keyboardAnimationDuration
        self.keyboardHeight = frame!.height
        UIView.animate(withDuration: duration!) {
            self.frame = CGRect(x: 0, y: AScreenHeight - 60 - frame!.height, width: AScreenWidth, height: self.frame.height)
        }
    }
    
    @objc private func keyboardWillHide(notification: Notification) {
        let frame = notification.a.keyboardEndFrame
        let duration = notification.a.keyboardAnimationDuration
        self.keyboardHeight = frame!.height
        self.frame = CGRect(x: 0, y: self.frame.origin.y, width: AScreenWidth, height: self.keyboardHeight + 5 + 60)
        if self.emoji == nil {
            let emoji = AUIEmojiView(frame: CGRect(x: 0, y: self.inputField.frame.maxY, width: AScreenWidth, height: self.keyboardHeight)).tag(124)
                .backgroundColor(AUIChatTheme.shared.inputbar.backgroundColor)
            self.emoji = emoji
        }
        if let emoji = self.emoji {
            addSubview(emoji)
        }
        self.emoji?.emojiClosure = { [weak self] in
            guard let self = self else { return }
            self.emoji?.deleteEmoji.isEnabled = true
            self.emoji?.deleteEmoji.isUserInteractionEnabled = true
            if self.inputField.attributedText.length <= self.limitCount {
                self.inputField.attributedText = self.convertText(text: self.inputField.attributedText, key: $0)
            }
        }
        self.emoji?.deleteClosure = { [weak self] in
            if self?.inputField.text?.count ?? 0 > 0 {
                self?.inputField.deleteBackward()
                self?.emoji?.deleteEmoji.isEnabled = true
                self?.emoji?.deleteEmoji.isUserInteractionEnabled = true
            } else {
                self?.emoji?.deleteEmoji.isEnabled = false
                self?.emoji?.deleteEmoji.isUserInteractionEnabled = false
            }
        }
        self.emoji?.isHidden = true
        UIView.animate(withDuration: duration!) {
            self.emoji?.isHidden = false
        }
    }
    
    @objc public func hiddenInputBar() {
        self.inputField.resignFirstResponder()
        UIView.animate(withDuration: 0.3) {
            self.frame = CGRect(x: 0, y: AScreenHeight, width: AScreenWidth, height: self.keyboardHeight + 60)
        }
        self.emoji?.removeFromSuperview()
        self.rightView.isSelected = false
    }
    
    /// Description AUIChatInputBar.hiddenInput() 隐藏window上的AUIChatInputBar
    @objc static public func hiddenInput() {
        getInput()?.hiddenInputBar()
    }

    func convertText(text: NSAttributedString?, key: String) -> NSAttributedString {
        let attribute = NSMutableAttributedString(attributedString: text!)
        let attachment = NSTextAttachment()
        attachment.image = AUIChatEmojiManager.shared.emojiMap.isEmpty ? UIImage.aui_Image(named: key):AUIChatEmojiManager.shared.emojiMap[key]
        attachment.bounds = CGRect(x: 0, y: -3.5, width: 18, height: 18)
        let imageText = NSMutableAttributedString(attachment: attachment)
        if #available(iOS 11.0, *) {
            imageText.addAttributes([.accessibilityTextCustom: key], range: NSMakeRange(0, imageText.length))
        } else {
            assert(false,"failed add accessibility custom text!")
        }
        attribute.append(imageText)
        return attribute
    }
    
    public func dismissKeyboard() {
        self.inputField.resignFirstResponder()
    }
}

public class AUIChatInputBarConfig: NSObject {
    
    var backgroundColor: UIColor = .white
    
    var textFont: UIFont = .systemFont(ofSize: 15)
    
    var placeHolder: String = "Aa"
    
    var placeHolderColor: UIColor = UIColor(0xB6B8C9)
    
    var cursorColor: UIColor = UIColor(0x009FFF)
    
    var textColor: UIColor = .black
    
    var textInputCornerRadius: CGFloat = 5
    
    var returnKeyType: UIReturnKeyType = .send
    
    var textInputIcon: UIImage? = UIImage.aui_Image(named: "key")
    
    var emojiInputIcon: UIImage? = UIImage.aui_Image(named: "face")
    
    /// Description Colors for gradient layer
    var sendGradientColors: [UIColor] = [UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)]
    
    /// start & end
    var sendGradientPoints: [CGPoint] = [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]
    
    var enLimitCount: Int = 80
    
    var zhLimitCount: Int = 30
    
    var mode: AUIThemeMode = .light {
        willSet {
            if newValue == .dark {
                backgroundColor = UIColor(0x1A1A1A)
                placeHolderColor = UIColor(0x666666)
                cursorColor = UIColor(0x009FFF)
                textColor = .white
            } else {
                backgroundColor = .white
                placeHolderColor = UIColor(0xF9FAFA)
                cursorColor = UIColor(0x009FFF)
                textColor = .black
            }
        }
    }
    
    public override init() {
        super.init()
    }
    
}

public extension NSAttributedString {
    func toString() -> String {
        let result = NSMutableAttributedString(attributedString: self)
        var replaceList: [(NSRange, String)] = []
        if #available(iOS 11.0, *) {
            result.enumerateAttribute(.accessibilityTextCustom, in: NSRange(location: 0, length: result.length), using: { value, range, _ in
                if let value = value as? String {
                    for i in range.location..<range.location + range.length {
                        replaceList.append((NSRange(location: i, length: 1), value))
                    }
                }
            })
        } else {
            assert(false,"failed add replace custom text!")
        }
        for i in replaceList.reversed() {
            result.replaceCharacters(in: i.0, with: i.1)
        }
        return result.string
    }
}

