//
//  JoyBaseDialog.swift
//  Joy
//
//  Created by wushengtao on 2023/11/30.
//

import UIKit

private let kDialogTag = 111223456
private let kDialogAnimationDuration = 0.3
class JoyBaseDialog: UIView {
//    private lazy var gradientLayer: CAGradientLayer = {
//        let layer = CAGradientLayer()
//        layer.colors = [
//            UIColor(hexString: "#040925")!.cgColor,
//            UIColor(hexString: "#D4CFE5")!.cgColor,
//        ]
//
//        return layer
//    }()
    private lazy var indicatorView: UIView = {
        let view = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 37, height: 3)))
        view.setCornerRadius(1.5)
        view.backgroundColor = UIColor(red: 0.83, green: 0.81, blue: 0.899, alpha: 1)
        return view
    }()
    private(set) lazy var dialogView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 20
        view.clipsToBounds = true
        return view
    }()
    
    private(set) lazy var contentView: UIView = UIView()
    
    private(set) lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .joy_title_text
        label.font = .joy_S_16
        label.text = labelTitle()
        label.textAlignment = .center
        return label
    }()
    
    private(set) lazy var button: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle(buttonTitle(), for: .normal)
        button.addTarget(self, action: #selector(onClickButton), for: .touchUpInside)
        return button
    }()
    
    override required init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func contentSize() -> CGSize {
        return .zero
    }
    
    func labelTitle() -> String {
        return ""
    }
    
    func buttonTitle() -> String {
        return ""
    }
    
    fileprivate func _loadSubView() {
        backgroundColor = .clear
        addSubview(dialogView)
        dialogView.addSubview(contentView)
        dialogView.addSubview(indicatorView)
        dialogView.addSubview(titleLabel)
        dialogView.addSubview(button)
//        contentView.layer.addSublayer(gradientLayer)
        loadCustomContentView(contentView: contentView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let contentSize = contentSize()
        indicatorView.aui_centerX = width / 2
        indicatorView.aui_top = 8
        dialogView.frame = CGRect(x: 0, y: self.aui_height - contentSize.height, width: contentSize.width, height: contentSize.height)
        titleLabel.frame = CGRect(x: 0, y: 23, width: dialogView.width, height: 22)
        let buttonMargin = UIEdgeInsets(top: titleLabel.aui_bottom + 14, left: 24, bottom: 34, right: 24)
        button.frame = CGRect(x: buttonMargin.left,
                              y: dialogView.height - 40 - buttonMargin.bottom,
                              width: dialogView.width - buttonMargin.left - buttonMargin.right,
                              height: 40)
        button.setjoyVerticalDefaultGradientBackground()
        contentView.frame = CGRect(x: 0, y: buttonMargin.top, width: dialogView.width, height: button.aui_top - buttonMargin.top - 20)
//        gradientLayer.frame = CGRect(x: 0, y: 0, width: contentView.aui_width, height: 19)
    }
    
    func loadCustomContentView(contentView: UIView) {
    }
    
    static func show<T: JoyBaseDialog>() -> T? {
        T.hidden()
        guard let window = getWindow() else {return nil}
        let dialog: T = T(frame: window.bounds)
        dialog.tag = kDialogTag
        window.addSubview(dialog)
        dialog.showAnimation()
        return dialog
    }
    
    func showAnimation() {
        setNeedsLayout()
        layoutIfNeeded()
        dialogView.aui_top = self.aui_height
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.dialogView.aui_bottom = self.aui_height
        }
    }
    
    func hiddenAnimation() {
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.dialogView.aui_top = self.aui_height
        } completion: { success in
            self.removeFromSuperview()
        }
    }
    
    static func hiddenAnimation() {
        guard let dialog = getWindow()?.viewWithTag(kDialogTag) as? JoyBaseDialog else {
            return
        }
        
        dialog.hiddenAnimation()
    }
    
    static func hidden() {
        getWindow()?.viewWithTag(kDialogTag)?.removeFromSuperview()
    }
}

extension JoyBaseDialog {
    @objc public func onClickButton() {
        
    }
}
