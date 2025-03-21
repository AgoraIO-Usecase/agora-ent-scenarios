//
//  AUICommonDialog.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/23.
//

import UIKit
import SwiftTheme

func getDialog() -> AUICommonDialog? {
    guard let window = getWindow() else {return nil}
    for subView in window.subviews {
        if let dialog = subView as? AUICommonDialog {
            return dialog
        }
    }
    return nil
}

public class AUICommonDialogStyle: NSObject {
    public var indicatorColor: UIColor = UIColor.aui_lightGrey35
    public var contentControlColor: UIColor = UIColor.aui_black90
}

private let kRadius: CGFloat = 12
open class AUICommonDialog: UIView {
    private var touchPrevVal: CGFloat = 0
    private lazy var contentView: UIView = {
        let view = UIView()
        view.layer.cornerRadius = kRadius
        view.clipsToBounds = true
        view.backgroundColor = .clear
        return view
    }()
    
    private lazy var indicatorView: UIView = {
        let view = UIView()
        view.aui_size = CGSize(width: 48, height: 3)
        view.layer.cornerRadius = 1.5
        view.clipsToBounds = true
        view.theme_backgroundColor = "CommonColor.lightGrey35"
        view.backgroundColor = UIColor(red: 249/255.0, green: 250/255.0, blue: 250/255.0, alpha: 1)
        return view
    }()
    
    private lazy var contentControlView: UIView = {
        let view = UIView()
        view.theme_backgroundColor = "CommonColor.black90"
        return view
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubviews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubviews()
    }
    
    private func _loadSubviews() {
        addSubview(contentView)
        contentView.addSubview(contentControlView)
        contentView.addSubview(indicatorView)
        
//        setStyle(style: AUICommonDialogStyle())
        
        let gesture = UIPanGestureRecognizer()
        gesture.addTarget(self, action: #selector(onPanGesture(_:)))
        gesture.delegate = self
        contentView.addGestureRecognizer(gesture)
    }
    
    open override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        if let point = touches.first?.location(in: self), !contentView.frame.contains(point) {
            hidden()
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        contentControlView.frame = contentView.bounds
        indicatorView.aui_center = CGPoint(x: contentView.aui_width / 2, y: 9)
    }
    
    @discardableResult
    public class func show(contentView: UIView) -> AUICommonDialog? {
        guard let window = getWindow() else {
            return nil
        }
        let dialog = AUICommonDialog(frame: window.frame)
        window.addSubview(dialog)
        dialog.contentView.addSubview(contentView)
        dialog.contentView.aui_size = contentView.aui_size
        let contentSize = contentView.bounds.size
        dialog.contentView.frame = CGRect(x: 0, y: dialog.frame.height, width: contentSize.width, height: contentSize.height)
        dialog.show()
        
        return dialog
    }
    
    public class func hidden() {
        guard let dialog = getDialog() else {
            return
        }
        
        dialog.hidden()
    }
    
    public func show() {
        UIView.animate(withDuration: 0.3) {
            self.contentView.aui_bottom = self.aui_height
        } completion: { flag in
        }
    }
    
    public func hidden() {
        UIView.animate(withDuration: 0.3) {
            self.contentView.aui_top = self.aui_height
        } completion: { flag in
            self.removeFromSuperview()
        }
    }
    
    public func setStyle(style: AUICommonDialogStyle) {
        indicatorView.backgroundColor = style.indicatorColor
        contentControlView.backgroundColor = style.contentControlColor
    }
}

extension AUICommonDialog: UIGestureRecognizerDelegate {
    open override func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        let point = gestureRecognizer.location(in: self.contentView)
        if point.y > 80 {
            return false
        }
            
        return true
    }
}

extension AUICommonDialog {
    @objc func onPanGesture(_ ges: UIPanGestureRecognizer) {
        let state = ges.state
        let val = ges.location(in: self).y
        if state == .began {
            touchPrevVal = val
        } else if state == .changed {
            let offset = val - touchPrevVal
            touchPrevVal = val
            let bottom = contentView.aui_bottom + offset
            contentView.aui_bottom = max(bottom, aui_height)
        } else if state == .ended || state == .cancelled {
            if contentView.aui_bottom - aui_height > 30 {
                hidden()
            } else {
                UIView.animate(withDuration: 0.3) {
                    self.contentView.aui_bottom = self.aui_height
                } completion: { flag in
                    
                }
            }
        }
    }
}

public class AUICommonDialogTheme: NSObject {
    public var indicatorColor: ThemeColorPicker = "CommonColor.lightGrey35"
    public var contentControlColor: ThemeColorPicker = "ActionSheet.backgroundColor"
}
extension AUICommonDialog {
    public func setTheme(_ theme:AUICommonDialogTheme) {
        indicatorView.theme_backgroundColor = theme.indicatorColor
        contentControlView.theme_backgroundColor = theme.contentControlColor
    }
    
    @discardableResult
    public class func show(contentView: UIView, theme: AUICommonDialogTheme) -> AUICommonDialog? {
        let dialog = show(contentView: contentView)
        dialog?.setTheme(theme)
        return dialog
    }
}
