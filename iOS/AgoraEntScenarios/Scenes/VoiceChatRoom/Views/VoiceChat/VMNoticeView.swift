//
//  VMNoticeView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/6.
//

import SnapKit
import UIKit
import ZSwiftBaseLib
class VMNoticeView: UIView {
    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()

    private var lineImgView: UIImageView = .init()
    private var canBtn: UIButton = .init()
    private var subBtn: UIButton = .init()
    private var titleLabel: UILabel = .init()
    private var tv: UITextView = .init()
    private var limLabel: UILabel = .init()
    private var editBtn: UIButton = .init()

    private let placeHolder: String = "Announce to chatroom, 140 character limit."

    var resBlock: ((Bool, String?) -> Void)?

    var roleType: ROLE_TYPE = .owner {
        didSet {
            if roleType == .owner {
                canBtn.isHidden = true
                subBtn.isHidden = true
                limLabel.isHidden = true
                editBtn.isHidden = false
                tv.isEditable = false
            } else {
                canBtn.isHidden = true
                subBtn.isHidden = true
                limLabel.isHidden = true
                editBtn.isHidden = true
                tv.isEditable = false
            }
        }
    }

    var noticeStr: String = "" {
        didSet {
            let notice = noticeStr.count == 0 ? "Welcome" : noticeStr
            tv.text = notice
            limLabel.text = "\(notice.count)/140"
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func layoutUI() {
        let path = UIBezierPath(roundedRect: bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer

        addSubview(cover)

        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage("pop_indicator")
        addSubview(lineImgView)

        canBtn.frame = CGRect(x: 15, y: 18, width: 68, height: 30)
        canBtn.setTitle(LanguageManager.localValue(key: "Cancel"), for: .normal)
        canBtn.setTitleColor(.lightGray, for: .normal)
        canBtn.font(UIFont.systemFont(ofSize: 13))
        canBtn.addTargetFor(self, action: #selector(can), for: .touchUpInside)
        addSubview(canBtn)

        subBtn.frame = CGRect(x: ScreenWidth - 85, y: 18, width: 68, height: 30)

        let gl = CAGradientLayer()
        gl.startPoint = CGPoint(x: 0.18, y: 0)
        gl.endPoint = CGPoint(x: 0.66, y: 1)
        gl.colors = [UIColor(red: 33 / 255.0, green: 155 / 255.0, blue: 1, alpha: 1).cgColor, UIColor(red: 52 / 255.0, green: 93 / 255.0, blue: 1, alpha: 1).cgColor]
        gl.locations = [0, 1.0]
        subBtn.layer.cornerRadius = 15
        subBtn.layer.masksToBounds = true
        gl.frame = subBtn.bounds
        subBtn.layer.addSublayer(gl)

        subBtn.setTitleColor(.white, for: .normal)
        subBtn.setTitle(LanguageManager.localValue(key: "Post"), for: .normal)
        subBtn.font(UIFont.systemFont(ofSize: 13))
        subBtn.addTargetFor(self, action: #selector(sub), for: .touchUpInside)
        addSubview(subBtn)

        titleLabel.frame = CGRect(x: ScreenWidth / 2.0 - 40, y: 22, width: 80, height: 22)
        titleLabel.textAlignment = .center
        titleLabel.text = LanguageManager.localValue(key: "Notice")
        titleLabel.textColor = .black
        titleLabel.font = UIFont.systemFont(ofSize: 16)
        addSubview(titleLabel)

        tv.frame = CGRect(x: 10, y: 60, width: ScreenWidth - 20, height: 160)
        tv.text = "Welcome"
        tv.setPlaceholder(text: placeHolder)
        tv.textColor = UIColor(red: 0.235, green: 0.257, blue: 0.403, alpha: 1)
        tv.font = UIFont.systemFont(ofSize: 14)
        tv.delegate = self
        addSubview(tv)

        limLabel.frame = CGRect(x: ScreenWidth - 80, y: bounds.size.height - 30, width: 80, height: 20)
        limLabel.textColor = UIColor(red: 0.593, green: 0.612, blue: 0.732, alpha: 1)
        limLabel.font = UIFont.systemFont(ofSize: 14)
        limLabel.textAlignment = .center
        limLabel.text = "0/140"
        addSubview(limLabel)
        limLabel.isHidden = true

        let isHairScreen = SwiftyFitsize.isFullScreen
        editBtn.frame = CGRect(x: 20, y: bounds.size.height - (isHairScreen ? 87 : 53), width: bounds.size.width - 40, height: 48)
        editBtn.setTitle(LanguageManager.localValue(key: "Edit"), for: .normal)
        editBtn.font(UIFont.systemFont(ofSize: 16))
        editBtn.setTitleColor(.white, for: .normal)
        editBtn.layer.cornerRadius = 24
        editBtn.layer.masksToBounds = true
        editBtn.setBackgroundImage(UIImage("blue_btn_bg"), for: .normal)
        editBtn.addTargetFor(self, action: #selector(edit), for: .touchUpInside)
        addSubview(editBtn)
    }

    @objc private func can() {
        canBtn.isHidden = true
        subBtn.isHidden = true
        editBtn.isHidden = false
        limLabel.isHidden = true
        tv.resignFirstResponder()
    }

    @objc private func sub() {
        guard let block = resBlock else { return }
        block(true, tv.text.count == 0 ? nil : tv.text)
    }

    @objc private func edit() {
        canBtn.isHidden = false
        subBtn.isHidden = false
        editBtn.isHidden = true
        limLabel.isHidden = false
        tv.isEditable = true
        tv.becomeFirstResponder()
    }
}

extension VMNoticeView: UITextViewDelegate {
    func textViewDidChange(_ textView: UITextView) {
        let text = textView.text
        if text!.count >= 140 {
            let indexStart = text!.startIndex
            let indexEnd = text!.index(indexStart, offsetBy: 140)
            tv.text = String(text![indexStart..<indexEnd])
            limLabel.text = "140/140"
            tv.textColor = UIColor(red: 0.235, green: 0.257, blue: 0.403, alpha: 1)
        } else if text!.count > 0 {
            tv.text = text
            limLabel.text = "\(text!.count)/140"
            tv.textColor = UIColor(red: 0.235, green: 0.257, blue: 0.403, alpha: 1)
        }
    }
}

extension UITextView {
    func setPlaceholder(text: String) {
        let placeholderLabel = UILabel()
        placeholderLabel.numberOfLines = 0
        placeholderLabel.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        placeholderLabel.textColor = UIColor(red: 0.593, green: 0.612, blue: 0.732, alpha: 1)
        placeholderLabel.text = text
        placeholderLabel.sizeToFit()
        addSubview(placeholderLabel)
        setValue(placeholderLabel, forKeyPath: "_placeholderLabel")
    }
}
