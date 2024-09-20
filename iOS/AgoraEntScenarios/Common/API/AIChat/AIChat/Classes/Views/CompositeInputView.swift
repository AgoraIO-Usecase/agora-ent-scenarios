//
//  CompositeInputView.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/3.
//

import UIKit
import ZSwiftBaseLib

public class CompositeInputView: UIView {
    
    enum CompositeInputViewState {
        case normal
        case textEditing
        case textDraft
    }
    
    var sendClosure: ((String) -> Void)?
    
    var becomeFirstResponderClosure: ((Bool,CGFloat,CGFloat) -> Void)?
    
    var longPressAudioClosure: ((LongPressButton.State,LongPressButton.MoveDirection) -> Void)?
    
    var voiceChatClosure: (() -> Void)?
    
    private var keyboardHeight: CGFloat = 0
    
    private var keyboardDuration: TimeInterval = 0
    
    private var state: CompositeInputViewState = .normal
    
    private lazy var blur: UIVisualEffectView = {
        let blurView = UIVisualEffectView(effect: UIBlurEffect(style: UIBlurEffect.Style.light))
        return blurView
    }()
    
    private lazy var leftButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage(named: "phone in circle", in: .chatAIBundle, with: nil), for: .normal)
        button.backgroundColor = .clear
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(showVoiceChat), for: .touchUpInside)
        return button
    }()
    
    private lazy var rightButton: LongPressButton = {
        let button = LongPressButton(type: .custom)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.backgroundColor = .clear
        button.addTarget(self, action: #selector(rightAction), for: .touchUpInside)
        button.longPressCallback = { [weak self] in
            guard let `self` = self else { return }
            if self.state == .normal {
                self.pop.hide()
                self.longPressAudioClosure?($0,$1)
            }
        }
        return button
    }()
    
    private lazy var textView: PlaceholderTextEditor = {
        let textView = PlaceholderTextEditor()
        textView.translatesAutoresizingMaskIntoConstraints = false
        textView.isScrollEnabled = false
        textView.placeholder = "说点什么"
        textView.placeholderColor = UIColor(0x303553)
        textView.font = UIFont.systemFont(ofSize: 16)
        textView.backgroundColor = .clear
        textView.layoutManager.allowsNonContiguousLayout = false
        textView.adjustsFontForContentSizeCategory = true
        textView.bounces = false
        textView.tintColor = UIColor.theme.primaryColor6
        textView.contentInset = UIEdgeInsets(top: -2, left: 0, bottom: 0, right: 0)
        return textView
    }()
    
    private var leftButtonLeadingConstraint: NSLayoutConstraint?
    private var textViewLeadingConstraint: NSLayoutConstraint?
    private var textViewHeightConstraint: NSLayoutConstraint?
    
    
    private let pop = PopTip().tag(191).backgroundColor(UIColor(0x0CA5FD))
    
    private let chatType: AIChatType
    
    // ... 其他属性保持不变 ...
    
    init(frame: CGRect, type: AIChatType) {
        self.chatType = type
        super.init(frame: frame)
        self.setupViews()
        self.setupConstraints()
        self.setupTextViewDelegate()
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIApplication.keyboardWillShowNotification, object: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupViews() {
        self.addSubview(self.blur)
        self.addSubview(self.rightButton)
        self.addSubview(self.textView)
        
        if chatType == .chat {
            self.addSubview(self.leftButton)
        }
    }
    
    private func setupConstraints() {
        self.textViewHeightConstraint = self.textView.heightAnchor.constraint(equalToConstant: 30)
        self.textViewHeightConstraint?.isActive = true
        
        NSLayoutConstraint.activate([
            // Right Button
            self.rightButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -12),
            self.rightButton.bottomAnchor.constraint(equalTo: bottomAnchor,constant: -13),
            self.rightButton.widthAnchor.constraint(equalToConstant: 30),
            self.rightButton.heightAnchor.constraint(equalToConstant: 30),
            
            // TextView
            self.textView.topAnchor.constraint(equalTo: topAnchor, constant: 10),
            self.textView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -10),
            self.textView.trailingAnchor.constraint(equalTo: self.rightButton.leadingAnchor, constant: -8)
        ])
        
        if self.chatType == .chat {
            NSLayoutConstraint.activate([
                // Left Button
                self.leftButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 12),
                self.leftButton.centerYAnchor.constraint(equalTo: centerYAnchor),
                self.leftButton.widthAnchor.constraint(equalToConstant: 30),
                self.leftButton.heightAnchor.constraint(equalToConstant: 30),
                
                // TextView
                self.textView.leadingAnchor.constraint(equalTo: self.leftButton.trailingAnchor, constant: 8)
            ])
        } else {
            NSLayoutConstraint.activate([
                // TextView
                self.textView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 12)
            ])
        }
        
        self.rightButton.setImage(UIImage(named: "wave_in_circle", in: .chatAIBundle, with: nil), for: .normal)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    private func setupTextViewDelegate() {
        self.textView.delegate = self
    }
    
    private func updateHeight() {
        let size = self.textView.sizeThatFits(CGSize(width: textView.bounds.width, height: .greatestFiniteMagnitude))
        let newHeight = min(max(30, size.height), 110)
        let oldHeight = self.textViewHeightConstraint?.constant ?? 30
        
        self.textViewHeightConstraint?.constant = newHeight
        if newHeight > 109 {
            self.textView.isScrollEnabled = true
        } else {
            self.textView.isScrollEnabled = false
        }
        UIView.animate(withDuration: 0.2) {
            self.layoutIfNeeded()
            self.superview?.layoutIfNeeded()
        }
    }
    
    private func setEditingState(_ isEditing: Bool) {
        if chatType == .chat {
            self.leftButton.isHidden = isEditing
        }
        
        self.textViewLeadingConstraint?.isActive = false
        if isEditing {
            self.textViewLeadingConstraint = self.textView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 12)
        } else if chatType == .chat {
            self.textViewLeadingConstraint = self.textView.leadingAnchor.constraint(equalTo: leftButton.trailingAnchor, constant: 8)
        } else {
            self.textViewLeadingConstraint = self.textView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 12)
        }
        
        self.textViewLeadingConstraint?.isActive = true
        self.updateHeight()
        UIView.animate(withDuration: 0.2) {
            self.layoutIfNeeded()
        }
    }
    
    private func resetToInitialState() {
        
        self.sendClosure?(self.textView.text ?? "")
        
        self.textView.text = ""
        
        self.textView.placeholder = "说点什么"
        
        self.textViewHeightConstraint?.constant = 30
        
        self.setEditingState(false)
        
        self.textView.resignFirstResponder()
        
        self.rightButton.setImage(UIImage(named: "wave_in_circle", in: .chatAIBundle, with: nil), for: .normal)
        
        UIView.animate(withDuration: 0.2) {
            self.layoutIfNeeded()
        }
    }
    
    @objc private func rightAction() {
        if self.state != .normal {
            self.resetToInitialState()
        } else {
            self.showTips()
        }
    }
    
    @objc private func showVoiceChat() {
        self.voiceChatClosure?()
    }
    
    private func showTips() {
        guard let superContainer = self.superview else { return }
        pop.bubbleColor = UIColor(0x0CA5FD)
        pop.shadowColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.12)
        pop.shadowOpacity = 1
        pop.shadowRadius = 8
        pop.shadowOffset = CGSize(width: 0, height: 0)
        pop.cornerRadius = 14
        pop.shouldConsiderCutoutTapSeparately = true
        pop.show(customView: UILabel(frame: CGRect(x: 0, y: 0, width: 50, height: 16)).text("按住录音").font(.systemFont(ofSize: 12, weight: .regular)).textAlignment(.center).textColor(.white), direction: .up, in: superContainer, from: CGRect(x: self.frame.maxX - 68, y: self.frame.minY-8, width: 80, height: 33),duration: 3)
    }
    
    func setDisableState() {
        self.leftButton.isEnabled = false
        self.rightButton.isEnabled = false
        self.textView.isEditable = false
        self.textView.isSelectable = false
        self.backgroundColor = UIColor(white: 1, alpha: 0.4)
        self.rightButton.alpha = 0.5
        self.leftButton.alpha = 0.5
        self.textView.alpha = 0.5
    }
    
    @MainActor func setEnableState() {
        self.leftButton.isEnabled = true
        self.rightButton.isEnabled = true
        self.textView.isEditable = true
        self.textView.isSelectable = true
        self.backgroundColor = UIColor(white: 1, alpha: 0.8)
        self.rightButton.alpha = 1
        self.leftButton.alpha = 1
        self.textView.alpha = 1
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
    
    @objc public func hiddenInputBar() {
        
        if self.textView.isFirstResponder {
            UIView.animate(withDuration: self.keyboardDuration) {
                self.transform = .identity
            }
            self.rightButton.isSelected = !self.textView.text.isEmpty
            if self.textView.text.isEmpty {
                self.resetToInitialState()
            } else {
                self.textView.resignFirstResponder()
            }
        }
    }
    
    @objc public func showInputBar() {
        self.textView.becomeFirstResponder()
        UIView.animate(withDuration: self.keyboardDuration) {
            self.transform = CGAffineTransform(translationX: 0, y: self.frame.minY-self.keyboardHeight)
        }
    }
}

extension CompositeInputView: UITextViewDelegate {
    
    public func textViewShouldBeginEditing(_ textView: UITextView) -> Bool {
        self.rightButton.setImage(UIImage(named: "forbidden_send", in: .chatAIBundle, with: nil), for: .normal)
        return true
    }
    
    public func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        self.state = .textEditing
        var text = textView.text ?? ""
        let fullText = text.z.replaceStringWithRange(location: range.location, length: range.length, newString: text)
        if fullText.count > 300 {
            textView.text = fullText.z.subStringTo(300)
            return false
        }
        if self.textView.text.isEmpty {
            self.rightButton.setImage(UIImage(named: "forbidden_send", in: .chatAIBundle, with: nil), for: .normal)
        } else {
            self.rightButton.setImage(UIImage(named: "send", in: .chatAIBundle, with: nil), for: .normal)
        }
        return true
    }
    
    public func textViewDidEndEditing(_ textView: UITextView) {
        self.becomeFirstResponderClosure?(false,self.keyboardHeight,self.keyboardDuration)
        if self.textView.text.isEmpty {
            self.rightButton.setImage(UIImage(named: "wave_in_circle", in: .chatAIBundle, with: nil), for: .normal)
            self.state = .normal
        } else {
            self.state = .textDraft
        }
    }
    
    public func textViewDidChange(_ textView: UITextView) {
        self.updateHeight()
    }
    
    @objc private func keyboardWillShow(notification: Notification) {
        if !self.textView.isFirstResponder {
            return
        }
        if let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardHeight = keyboardFrame.cgRectValue.height
            self.keyboardHeight = keyboardHeight
            self.keyboardDuration = notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval ?? 0
            self.becomeFirstResponderClosure?(true,self.keyboardHeight,self.keyboardDuration)
            self.setEditingState(true)
            
        }
    }
}
