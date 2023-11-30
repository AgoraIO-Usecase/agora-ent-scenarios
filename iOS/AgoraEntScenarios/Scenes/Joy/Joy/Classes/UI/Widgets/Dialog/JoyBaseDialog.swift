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
    private(set) lazy var dialogView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 20
        view.clipsToBounds = true
        return view
    }()
    
    private(set) lazy var contentView: UIView = {
        let view = UIView()
        return view
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
    
    func buttonTitle() -> String {
        return ""
    }
    
    fileprivate func _loadSubView() {
        backgroundColor = .clear
        addSubview(dialogView)
        dialogView.addSubview(contentView)
        dialogView.addSubview(button)
//        contentView.layer.addSublayer(gradientLayer)
        loadCustomContentView(contentView: contentView)
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        // 检查是否有子视图响应触摸事件
        if let superViewHitTestView = super.hitTest(point, with: event), superViewHitTestView != self {
            return superViewHitTestView
        } else {
            // 如果没有子视图响应，则返回父视图，使触摸事件穿透到下一层视图
            return nil
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let contentSize = contentSize()
        dialogView.frame = CGRect(x: 0, y: self.aui_height - contentSize.height, width: contentSize.width, height: contentSize.height)
        let buttonMargin = UIEdgeInsets(top: 20, left: 24, bottom: 34, right: 24)
        button.frame = CGRect(x: buttonMargin.left, 
                              y: dialogView.height - 40 - buttonMargin.bottom,
                              width: dialogView.width - buttonMargin.left - buttonMargin.right,
                              height: 40)
        let colors = [
            UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1),
            UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)
        ] // 渐变颜色数组
        let startPoint = CGPoint(x: 0, y: 0) // 渐变起点
        let endPoint = CGPoint(x: 0, y: 1) // 渐变终点
        let cornerRadius: CGFloat = button.height / 2 // 圆角半径
        button.setGradientBackground(colors: colors, startPoint: startPoint, endPoint: endPoint, cornerRadius: cornerRadius)
        contentView.frame = CGRect(x: 0, y: 20, width: dialogView.width, height: button.aui_top - buttonMargin.top - 20)
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
    
    static func hidden() {
        getWindow()?.viewWithTag(kDialogTag)?.removeFromSuperview()
    }
}

extension JoyBaseDialog {
    @objc public func onClickButton() {
        
    }
}
