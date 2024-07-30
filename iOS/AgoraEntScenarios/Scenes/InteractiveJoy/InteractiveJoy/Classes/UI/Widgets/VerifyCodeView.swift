//
//  VerifyCodeView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit
import ZSwiftBaseLib

public class VerifyCodeView: UIView {
    @objc public var beginEdit: (() -> Void)?

    /// 输入值改变
    @objc public var textValueChange: ((_ text: String) -> Void)?

    /// 输入完成
    @objc public var inputFinish: ((_ text: String) -> Void)?

    /// 验证码输入框个数
    @objc var inputTextNum: Int = 4

    /// 输入框
    @objc public lazy var textFiled: VRVerifyCodeTextView = {
        let textFiled = VRVerifyCodeTextView(frame: CGRect(x: self.padding, y: 0, width: self.frame.width - 2 * self.padding, height: self.frame.height)).backgroundColor(.clear).textColor(.clear).delegate(self)
       // textFiled.tintColor = .darkText
        textFiled.keyboardType = .decimalPad
        textFiled.addTarget(self, action: #selector(textFiledDidChange(_:)), for: .editingChanged)
        textFiled.addTarget(self, action: #selector(textFiledDidEnd(_:)), for: .editingDidEnd)
        return textFiled
    }()

    /// 验证码数量
    @objc public var codeViews: [VRVerifyCodeNumberView] = []

    /// 验证码输入框距离两边的边距
    @objc var padding: CGFloat = 15

    /// 每个验证码输入框间距
    @objc var spacing: CGFloat = 10

    /// 是否在输入中
    @objc var isInput = true

    @objc public override init(frame: CGRect) {
        super.init(frame: frame)
    }

    @objc public convenience init(frame: CGRect, codeNumbers: Int, space: CGFloat, padding: CGFloat) {
        self.init(frame: frame)
        spacing = space
        self.padding = padding
        inputTextNum = codeNumbers
        addSubview(textFiled)
        initSubviews()
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardShow(note:)), name: UIResponder.keyboardWillShowNotification, object: nil)

        NotificationCenter.default.addObserver(self, selector: #selector(keyboardHidden(note:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    private func initSubviews() {
        // 每个验证码框宽度
        let itemWidth = CGFloat(frame.width - padding * 2 - spacing * (CGFloat(inputTextNum) - 1)) / CGFloat(inputTextNum)
        for i in 0..<inputTextNum {
            let codeNumView = VRVerifyCodeNumberView(frame: CGRect(x: padding + CGFloat(i) * (spacing + itemWidth), y: 0, width: itemWidth, height: frame.height)).isUserInteractionEnabled(false).backgroundColor(UIColor.init(hexString: "#F5F8FF")!).cornerRadius(8)
            
            if i != 0 {
                codeNumView.setCursorStatus(true)
                codeNumView.layer.borderWidth = 0
            } else {
                codeNumView.setCursorStatus(false)
                codeNumView.setBottomLineFocus(isFocus: true)
                codeNumView.setNum(num: nil)
                codeNumView.layer.borderWidth = 2.0
                codeNumView.layer.borderColor = UIColor.init(hexString: "#2E6CF6")?.cgColor
            }
            codeViews.append(codeNumView)
        }
        addSubViews(codeViews)
    }

    @available(*, unavailable)
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - 供外部调用方法

public extension VerifyCodeView {
    /// 清除所有输入
    @objc func cleanCodes() {
        textFiled.text = ""
        textFiledDidChange(textFiled)
        allCursorHidden()
    }

    /// 隐藏所有输入光标
    @objc func allCursorHidden() {
        DispatchQueue.main.async {
            for i in 0..<self.codeViews.count {
                let codeView = self.codeViews[i]
                codeView.setCursorStatus(true)
                if codeView.getNum().count == 0 {
                    codeView.setBottomLineFocus(isFocus: false)
                }
            }
        }
    }
}

// MARK: - 键盘显示隐藏

public extension VerifyCodeView {
    @objc func keyboardShow(note: Notification) {
        isInput = false
        textFiledDidChange(textFiled)
        isInput = true
    }

    @objc func keyboardHidden(note: Notification) {
        allCursorHidden()
    }
}

// MARK: - UITextViewDelegate

extension VerifyCodeView: UITextFieldDelegate {
    public func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        // 输入框已有的值
        var inputText = textFiled.text ?? ""

        if string.count == 0 { // 删除
            if range.location != inputText.count - 1 { // 删除的不是最后一个
                if inputText.count > 0 {
                    // 手动删除最后一位
                    textFiled.text?.removeLast()
                    textFiledDidChange(textFiled)
                }
                return false
            }
        }

        if let tempRange = Range(range, in: inputText) {
            // 拼接输入后的值
            inputText = inputText.replacingCharacters(in: tempRange, with: string)
            let meetRegx = "[0-9]*"
            let characterSet = NSPredicate(format: "SELF MATCHES %@", meetRegx)
            if characterSet.evaluate(with: inputText) == false {
                return false
            }
        }

        if inputText.count > inputTextNum {
            return false
        }

        return true
    }

    @objc func textFiledDidChange(_ textFiled: UITextField) {
        let inputStr = textFiled.text ?? ""

        textValueChange?(inputStr)

        for i in 0..<codeViews.count {
            let codeView = codeViews[i]
            if i < inputStr.count {
                codeView.setNum(num: inputStr[String.Index(utf16Offset: i, in: inputStr)].description)
                codeView.setBottomLineFocus(isFocus: true)
                codeView.setCursorStatus(true)
                codeView.layer.borderWidth = 0
            } else {
                if inputStr.count == 0, i == 0 {
                    codeView.setCursorStatus(false)
                    codeView.setBottomLineFocus(isFocus: true)
                    codeView.setNum(num: nil)
                    codeView.layer.borderWidth = 2.0
                } else {
                    codeView.setCursorStatus(i != inputStr.count)
                    codeView.setNum(num: nil)
                    codeView.setBottomLineFocus(isFocus: i == inputStr.count)
                    codeView.layer.borderWidth = i == inputStr.count ? 2.0 : 0.0
                }
                codeView.layer.borderColor = UIColor.init(hexString: "#2E6CF6")?.cgColor
            }
        }

        if isInput, inputStr.count >= inputTextNum {
            // 结束编辑
            DispatchQueue.main.async {
                textFiled.resignFirstResponder()
            }
            allCursorHidden()
        }
    }

    @objc func textFiledDidEnd(_ textFiled: UITextField) {
        guard let inputStr = textFiled.text else { return }
        if isInput {
            inputFinish?(inputStr)
        }
    }

    public func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        if beginEdit != nil {
            beginEdit!()
        }
        return true
    }
}
