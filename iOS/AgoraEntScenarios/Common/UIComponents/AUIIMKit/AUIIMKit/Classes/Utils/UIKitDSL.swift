//
//  UIKitDSL.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation
import UIKit
import QuartzCore

/// Example
//private lazy var pop: UIButton = {
//    UIButton(type: .custom).frame(CGRect(x: 100, y: 100, width: 100, height: 50)).title("测试", .normal).font(.systemFont(ofSize: 15)).textColor(.black, .normal).addTargetFor(self, action: #selector(popAction), for: .touchUpInside)
//}()

public extension UIView {
    @discardableResult
    func setGradient(_ colors: [UIColor], _ points: [CGPoint]) -> Self {
        let gradientColors: [CGColor] = colors.map { $0.cgColor }
        let startPoint = points[0]
        let endPoint = points[1]
        let gradientLayer = CAGradientLayer().colors(gradientColors).startPoint(startPoint).endPoint(endPoint).frame(bounds).backgroundColor(UIColor.clear.cgColor).locations([0,1])
        layer.insertSublayer(gradientLayer, at: 0)
        return self
    }
    
    func createThemeGradient(_ color: [Any], _ points: [CGPoint]) -> Self {
        let startPoint = points[0]
        let endPoint = points[1]
        let gradientLayer = CAGradientLayer().startPoint(startPoint).endPoint(endPoint).frame(bounds).backgroundColor(UIColor.clear.cgColor).locations([0,1])
        gradientLayer.colors = color
        layer.insertSublayer(gradientLayer, at: 0)
        return self
    }
}

public extension UIView {
    
    @discardableResult
    func backgroundColor(_ color: UIColor) -> Self {
        let view = self
        view.backgroundColor = color
        return view
    }
    
    /// Description  单边圆角
    /// - Parameters:
    ///   - radius: 弧度
    ///   - corners: [左上角，右上角，左下角，右下角]
    ///   - color: layer's borderColor
    ///   - width: layer's borderWidth
    /// - Returns: Self
    @discardableResult
    func cornerRadius(_ radius: CGFloat , _ corners: [UIRectCorner] , _ color: UIColor , _ width: CGFloat) -> Self {
        let view = self
        view.clipsToBounds = true
        let corner = UIRectCorner(corners)
        let maskPath = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: corner, cornerRadii: CGSize(width: radius, height: radius))
        let maskLayer = CAShapeLayer().frame(self.bounds).borderColor(color.cgColor).borderWidth(width).path(maskPath.cgPath)
        maskLayer.path = maskPath.cgPath
        view.layer.mask = maskLayer
        return view
    }
    
    @discardableResult
    func contentMode(_ mode: UIView.ContentMode) -> Self {
        let view = self
        view.contentMode = mode
        return view
    }
    
    @discardableResult
    func cornerRadius(_ radius: CGFloat) -> Self  {
        let view = self
        view.clipsToBounds = true
        view.layer.cornerRadius = radius
        return view
    }
    
    @discardableResult
    func cornerRadiusMask(_ radius: CGFloat,_ mask: Bool) -> Self {
        let view = self
        view.layer.masksToBounds = mask
        view.layer.cornerRadius = radius
        return view
    }
    
    @discardableResult
    func layerProperties(_ color: UIColor,_ width: CGFloat) -> Self {
        let view = self
        view.layer.borderColor = color.cgColor
        view.layer.borderWidth = width
        return view
    }
    
    @discardableResult
    func isUserInteractionEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isUserInteractionEnabled = enable
        return view
    }
    
    @discardableResult
    func tag(_ tag: Int) -> Self {
        let view = self
        view.tag = tag
        return view
    }
    
    @discardableResult
    func center(_ point: CGPoint) -> Self {
        let view = self
        view.center = point
        return view
    }
    
    @discardableResult
    func transform(_ transform: CGAffineTransform) -> Self {
        let view = self
        view.transform = transform
        return view
    }
    
    @available(iOS 13.0, *)
    @discardableResult
    func transform3D(_ transform3D: CATransform3D) -> Self {
        let view = self
        view.transform3D = transform3D
        return view
    }
    
    @discardableResult
    func isMultipleTouchEnabled(_ isMultipleTouchEnabled: Bool) -> Self {
        let view = self
        view.isMultipleTouchEnabled = isMultipleTouchEnabled
        return view
    }
    
    @discardableResult
    func isExclusiveTouch(_ isExclusiveTouch: Bool) -> Self {
        let view = self
        view.isExclusiveTouch = isExclusiveTouch
        return view
    }
    
    /// Description Put all the subviews to be added in the array in order and add them at once
    /// - Parameter views: subviews
    func addSubViews(_ views: [UIView]) {
        views.forEach {
            self.addSubview($0)
        }
    }
    
    @discardableResult
    func addLayer(_ layer: CALayer) -> Self {
        let view = self
        view.layer.addSublayer(layer)
        return view
    }
    
}

public extension UILabel {

    @discardableResult
    func text(_ text: String?) -> Self {
        let view = self
        view.text = text
        return view
    }
    
    @discardableResult
    func textAlignment(_ textAlignment: NSTextAlignment) -> Self {
        let view = self
        view.textAlignment = textAlignment
        return view
    }
    
    @discardableResult
    func font(_ font: UIFont!) -> Self {
        let view = self
        view.font = font
        return view
    }
    
    @discardableResult
    func textColor(_ textColor: UIColor!) -> Self {
        let view = self
        view.textColor = textColor
        return view
    }
    
    @discardableResult
    func shadowColor(_ shadowColor: UIColor?) -> Self {
        let view = self
        view.shadowColor = shadowColor
        return view
    }
    
    @discardableResult
    func shadowOffset(_ shadowOffset: CGSize) -> Self {
        let view = self
        view.shadowOffset = shadowOffset
        return view
    }
    
    @discardableResult
    func lineBreakMode(_ lineBreakMode: NSLineBreakMode) -> Self {
        let view = self
        view.lineBreakMode = lineBreakMode
        return view
    }
    
    @discardableResult
    func attributedText(_ attributedText: NSAttributedString?) -> Self {
        let view = self
        view.attributedText = attributedText
        return view
    }
    
    @discardableResult
    func highlightedTextColor(_ highlightedTextColor: UIColor?) -> Self {
        let view = self
        view.highlightedTextColor = highlightedTextColor
        return view
    }
    
    @discardableResult
    func isHighlighted(_ isHighlighted: Bool) -> Self {
        let view = self
        view.isHighlighted = isHighlighted
        return view
    }
    
    @discardableResult
    func userInteractionEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isUserInteractionEnabled = enable
        return view
    }
    
    @discardableResult
    func isEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isEnabled = enable
        return view
    }
    
    @discardableResult
    func numberOfLines(_ num: Int) -> Self {
        let view = self
        view.numberOfLines = num
        return view
    }
    
    @discardableResult
    func adjustsFontSizeToFitWidth(_ width: Bool) -> Self {
        let view = self
        view.adjustsFontSizeToFitWidth = width
        return view
    }
    
}

public extension UIButton {
    
    @discardableResult
    func frame(_ frame: CGRect) -> Self {
        let view = self
        view.frame = frame
        return view
    }
    
    @discardableResult
    func userInteractionEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isUserInteractionEnabled = enable
        return view
    }
    
    @discardableResult
    func isEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isEnabled = enable
        return view
    }
    
    @discardableResult
    func title(_ title: String?, _ state: UIControl.State) -> Self  {
        let view = self
        view.setTitle(title, for: state)
        return view
    }
    
    @discardableResult
    func image(_ name: String, _ state: UIControl.State) -> Self {
        let view = self
        view.setImage(UIImage(named: name), for: state)
        return view
    }
    
    @discardableResult
    func font(_ font: UIFont!) -> Self {
        let view = self
        view.titleLabel?.font = font
        return view
    }
    
    @discardableResult
    func textColor(_ textColor: UIColor!, _ state: UIControl.State) -> Self {
        let view = self
        view.setTitleColor(textColor, for: state)
        return view
    }
    
    @discardableResult
    func backgroundImg(_ backgroundImg: UIImage, _ state: UIControl.State) -> Self {
        let view = self
        view.backgroundImage(for: state)
        return view
    }
    
    @discardableResult
    func imageEdgeInsets(_ edge: UIEdgeInsets) -> Self {
        let view = self
        view.imageEdgeInsets = edge
        return view
    }
    
    @discardableResult
    func titleEdgeInsets(_ edge: UIEdgeInsets) -> Self {
        let view = self
        view.titleEdgeInsets = edge
        return view
    }
    
    @discardableResult
    func contentEdgeInsets(_ edge: UIEdgeInsets) -> Self {
        let view = self
        view.contentEdgeInsets = edge
        return view
    }
    
    @discardableResult
    func attributedTitle(_ title: NSAttributedString?, _ state: UIControl.State) -> Self {
        let view = self
        view.setAttributedTitle(title, for: state)
        return view
    }
    
    @discardableResult
    func addTargetFor(_ target: Any?, action: Selector, for controlEvents: UIControl.Event) -> Self {
        let view = self
        view.addTarget(target, action: action, for: controlEvents)
        return view
    }
}

public extension UITextField {
    
    @discardableResult
    func userInteractionEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isUserInteractionEnabled = enable
        return view
    }
    
    @discardableResult
    func isEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isEnabled = enable
        return view
    }
    
    @discardableResult
    func text(_ txt: String?) -> Self {
        let view = self
        view.text = txt
        return view
    }
    
    @discardableResult
    func placeholder(_ txt: String?) -> Self {
        let view = self
        view.placeholder = txt
        return view
    }
    
    @discardableResult
    func attributedText(_ attribute: NSAttributedString?) -> Self {
        let view = self
        view.attributedText = attribute
        return view
    }
    
    @discardableResult
    func textAlignment(_ textAlignment: NSTextAlignment) -> Self {
        let view = self
        view.textAlignment = textAlignment
        return view
    }
    
    @discardableResult
    func font(_ font: UIFont!) -> Self {
        let view = self
        view.font = font
        return view
    }
    
    @discardableResult
    func textColor(_ textColor: UIColor!) -> Self {
        let view = self
        view.textColor = textColor
        return view
    }
    
    @discardableResult
    func delegate(_ del: UITextFieldDelegate?) -> Self {
        let view = self
        view.delegate = del
        return view
    }
    
    @discardableResult
    func clearButtonMode(_ clearMode: UITextField.ViewMode) -> Self {
        let view = self
        view.clearButtonMode = clearMode
        return view
    }
    
    @discardableResult
    func leftView(_ left: UIView?, _ model: UITextField.ViewMode) -> Self {
        let view = self
        view.leftView = left
        view.leftViewMode = model
        return view
    }
    
    @discardableResult
    func rightView(_ right: UIView?, _ model: UITextField.ViewMode) -> Self {
        let view = self
        view.rightView = right
        view.rightViewMode = model
        return view
    }
    
    @discardableResult
    func inputView(_ input: UIView?) -> Self {
        let view = self
        view.inputView = input
        return view
    }
    
    @discardableResult
    func inputAccessoryView(_ input: UIView?) -> Self {
        let view = self
        view.inputAccessoryView = input
        return view
    }
    
    @discardableResult
    func borderStyle(_ style: UITextField.BorderStyle) -> Self {
        let view = self
        view.borderStyle = style
        return view
    }
}

public extension UITextView {
    
    @discardableResult
    func userInteractionEnabled(_ enable: Bool) -> Self {
        let view = self
        view.isUserInteractionEnabled = enable
        return view
    }
    
    @discardableResult
    func isEditable(_ enable: Bool) -> Self {
        let view = self
        view.isEditable = enable
        return view
    }
    
    @discardableResult
    func text(_ txt: String?) -> Self {
        let view = self
        view.text = txt
        return view
    }
    
    @discardableResult
    func attributedText(_ attribute: NSAttributedString?) -> Self {
        let view = self
        view.attributedText = attribute
        return view
    }
    
    @discardableResult
    func textAlignment(_ textAlignment: NSTextAlignment) -> Self {
        let view = self
        view.textAlignment = textAlignment
        return view
    }
    
    @discardableResult
    func font(_ font: UIFont!) -> Self {
        let view = self
        view.font = font
        return view
    }
    
    @discardableResult
    func textColor(_ textColor: UIColor!) -> Self {
        let view = self
        view.textColor = textColor
        return view
    }
    
    @discardableResult
    func delegate(_ del: UITextViewDelegate?) -> Self {
        let view = self
        view.delegate = del
        return view
    }
}

public extension UITableView {
    
    @discardableResult
    func tableHeaderView(_ header: UIView?) -> Self {
        let view = self
        view.tableHeaderView = header
        return view
    }
    
    @discardableResult
    func tableFooterView(_ footer: UIView?) -> Self {
        let view = self
        view.tableFooterView = footer
        return view
    }
    
    @discardableResult
    func registerCell(_ cellClass: AnyClass?, forCellReuseIdentifier identifier: String) -> Self {
        let view = self
        view.register(cellClass, forCellReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func registerCell(_ nib: UINib?, _ identifier: String) -> Self {
        let view = self
        view.register(nib, forCellReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func register(_ aClass: AnyClass?, _ identifier: String) -> Self {
        let view = self
        view.register(aClass, forHeaderFooterViewReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func registerView(_ nib: UINib?, _ identifier: String) -> Self {
        let view = self
        view.register(nib, forHeaderFooterViewReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func delegate(_ del: UITableViewDelegate?) -> Self {
        let view = self
        view.delegate = del
        return view
    }
    
    @discardableResult
    func dataSource(_ del: UITableViewDataSource?) -> Self {
        let view = self
        view.dataSource = del
        return view
    }
    
    @discardableResult
    func rowHeight(_ height: CGFloat) -> Self {
        let view = self
        view.rowHeight = height
        return view
    }
    
    @discardableResult
    func sectionHeaderHeight(_ height: CGFloat) -> Self {
        let view = self
        view.sectionHeaderHeight = height
        return view
    }
    
    @discardableResult
    func sectionFooterHeight(_ height: CGFloat) -> Self {
        let view = self
        view.sectionFooterHeight = height
        return view
    }
    
    @discardableResult
    func estimatedRowHeight(_ height: CGFloat) -> Self {
        let view = self
        view.estimatedRowHeight = height
        return view
    }
    
    @discardableResult
    func estimatedSectionHeaderHeight(_ height: CGFloat) -> Self {
        let view = self
        view.estimatedSectionHeaderHeight = height
        return view
    }
    
    @discardableResult
    func estimatedSectionFooterHeight(_ height: CGFloat) -> Self {
        let view = self
        view.estimatedSectionFooterHeight = height
        return view
    }
    
    @discardableResult
    func separatorInset(edge: UIEdgeInsets) -> Self {
        let view = self
        view.separatorInset = edge
        return view
    }
    
    @discardableResult
    func separatorStyle(_ style: UITableViewCell.SeparatorStyle) -> Self {
        let view = self
        view.separatorStyle = style
        return view
    }
    
    @discardableResult
    func separatorColor(_ color: UIColor?) -> Self {
        let view = self
        view.separatorColor = color
        return view
    }
    
    @discardableResult
    func separatorEffect(_ effect: UIVisualEffect?) -> Self {
        let view = self
        view.separatorEffect = effect
        return view
    }
    
    @discardableResult
    func showsVerticalScrollIndicator(_ value: Bool) -> Self {
        let view = self
        view.showsVerticalScrollIndicator = value
        return view
    }
    
    @discardableResult
    func showsHorizontalScrollIndicator(_ value: Bool) -> Self {
        let view = self
        view.showsHorizontalScrollIndicator = value
        return view
    }
    
}

public extension UICollectionView {
    
    @discardableResult
    func collectionViewLayout(_ layout: UICollectionViewLayout) -> Self {
        let view = self
        view.collectionViewLayout = layout
        return view
    }
    
    @discardableResult
    func delegate(_ del: UICollectionViewDelegate?) -> Self {
        let view = self
        view.delegate = del
        return view
    }
    
    @discardableResult
    func dataSource(_ del: UICollectionViewDataSource?) -> Self {
        let view = self
        view.dataSource = del
        return view
    }
    
    @available(iOS 11.0, *)
    @discardableResult
    func dragDelegate(_ del: UICollectionViewDragDelegate?) -> Self {
        let view = self
        view.dragDelegate = del
        return view
    }
    
    @available(iOS 11.0, *)
    @discardableResult
    func dropDelegate(_ del: UICollectionViewDropDelegate?) -> Self {
        let view = self
        view.dropDelegate = del
        return view
    }
    
    @available(iOS 11.0, *)
    @discardableResult
    func dragInteractionEnabled(enabled: Bool) -> Self {
        let view = self
        view.dragInteractionEnabled = enabled
        return view
    }
    
    @discardableResult
    func registerCell(_ cellClass: AnyClass?, forCellReuseIdentifier identifier: String) -> Self {
        let view = self
        view.register(cellClass, forCellWithReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func registerCell(_ nib: UINib?, _ identifier: String) -> Self {
        let view = self
        view.register(nib, forCellWithReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func registerView(_ viewClass: AnyClass?, _ elementKind: String, _ identifier: String) -> Self {
        let view = self
        view.register(viewClass, forSupplementaryViewOfKind: elementKind, withReuseIdentifier: identifier)
        return view
    }

    @discardableResult
    func registerView(_ nib: UINib?, _ kind: String, _ identifier: String) -> Self {
        let view = self
        view.register(nib, forSupplementaryViewOfKind: kind, withReuseIdentifier: identifier)
        return view
    }
    
    @discardableResult
    func showsVerticalScrollIndicator(_ value: Bool) -> Self {
        let view = self
        view.showsVerticalScrollIndicator = value
        return view
    }
    
    @discardableResult
    func showsHorizontalScrollIndicator(_ value: Bool) -> Self {
        let view = self
        view.showsHorizontalScrollIndicator = value
        return view
    }
    
}

public extension UIAlertController {
    
    @discardableResult
    func addAlertAction(_ action: UIAlertAction) -> Self {
        let alert = self
        alert.addAction(action)
        return alert
    }
    
    @discardableResult
    func addAlertTextField(_ configurationHandler: ((UITextField) -> Void)? = nil) -> Self {
        let alert = self
        alert.addTextField(configurationHandler: configurationHandler)
        return alert
    }
    
}

public extension UIDatePicker {
    
    @discardableResult
    func datePickerMode(_ mode: UIDatePicker.Mode ) -> Self {
        let view = self
        view.datePickerMode = mode
        return view
    }
    
    @discardableResult
    func locale(_ local: Locale?) -> Self {
        let view = self
        view.locale = local
        return view
    }
    
    @discardableResult
    func calendar(_ cal: Calendar!) -> Self {
        let view = self
        view.calendar = cal
        return view
    }
    
    @discardableResult
    func timeZone(_ zone: TimeZone?) -> Self {
        let view = self
        view.timeZone = zone
        return view
    }
    
    @discardableResult
    func date(_ date: Date) -> Self {
        let view = self
        view.date = date
        return view
    }
    
    @discardableResult
    func minimumDate(_ date: Date) -> Self {
        let view = self
        view.minimumDate = date
        return view
    }
    
    @discardableResult
    func maximumDate(_ date: Date) -> Self {
        let view = self
        view.maximumDate = date
        return view
    }
    
}

public extension UIImageView {
    
    @discardableResult
    func image(_ img: UIImage?) -> Self {
        let view = self
        view.image = img
        return view
    }
    
    @discardableResult
    func highlightedImage(_ img: UIImage) -> Self {
        let view = self
        view.highlightedImage = img
        return view
    }
    
    @available(iOS 13.0, *)
    @discardableResult
    func preferredSymbolConfiguration(_ prefer: UIImage.SymbolConfiguration?) -> Self {
        let view = self
        view.preferredSymbolConfiguration = prefer
        return view
    }
    
    @discardableResult
    func highlighted(_ value: Bool) -> Self {
        let view = self
        view.isHighlighted = value
        return view
    }
    
    @discardableResult
    func animationImages(_ images: [UIImage]?) -> Self {
        let view = self
        view.animationImages = images
        return view
    }
    
    @discardableResult
    func highlightedAnimationImages(_ images: [UIImage]?) -> Self {
        let view = self
        view.highlightedAnimationImages = images
        return view
    }
    
    @discardableResult
    func animationDuration(_ duration: TimeInterval) -> Self {
        let view = self
        view.animationDuration = duration
        return view
    }
    
    @discardableResult
    func animationRepeatCount(_ count: Int) -> Self {
        let view = self
        view.animationRepeatCount = count
        return view
    }
    
    @discardableResult
    func tintColor(_ color: UIColor) -> Self {
        let view = self
        view.tintColor = color
        return view
    }
}

public extension UIBezierPath {
    @discardableResult
    func cgPath(_ path: CGPath) -> Self {
        let bezier = self
        bezier.cgPath = path
        return bezier
    }
    
    @discardableResult
    func moveTo(_ point: CGPoint) -> Self {
        let bezier = self
        bezier.move(to: point)
        return bezier
    }
    
    @discardableResult
    func addLineTo(_ point: CGPoint) -> Self {
        let bezier = self
        bezier.addLine(to: point)
        return bezier
    }
    
    @discardableResult
    func lineWidth(_ width: CGFloat) -> Self {
        let bezier = self
        bezier.lineWidth = width
        return bezier
    }
    
    @discardableResult
    func lineCapStyle(_ style: CGLineCap) -> Self {
        let bezier = self
        bezier.lineCapStyle = style
        return bezier
    }
    
    @discardableResult
    func lineJoinStyle(_ style: CGLineJoin) -> Self {
        let bezier = self
        bezier.lineJoinStyle = style
        return bezier
    }
    
    @discardableResult
    func miterLimit(_ miter: CGFloat) -> Self {
        let bezier = self
        bezier.miterLimit = miter
        return bezier
    }
    
    @discardableResult
    func flatness(_ flat: CGFloat) -> Self {
        let bezier = self
        bezier.flatness = flat
        return bezier
    }
    
    @discardableResult
    func usesEvenOddFillRule(_ rule: Bool) -> Self {
        let bezier = self
        bezier.usesEvenOddFillRule = rule
        return bezier
    }
    
}

@resultBuilder
public struct UIViewFunctionBuilder {
    public static func buildBlock(_ views: UIView...) -> [UIView] {
        var subviews = [UIView]()
        for view in views {
            subviews.append(view)
        }
        return subviews
    }
}

public extension UIView {
    
    convenience init(@UIViewFunctionBuilder _ builder: () -> [UIView]) {
        self.init()
        let views = builder()
        if let view = views.first {
            if view.frame != .zero {
                self.frame = view.frame
            } else {
                assert(false, "views stack top must has frame")
            }
        }
        self.addSubViews(views)
    }
    
}

public extension CALayer {
    @discardableResult
    func bounds(_ rect: CGRect) -> Self {
        let layer = self
        layer.bounds = rect
        return self
    }
    
    @discardableResult
    func position(_ point: CGPoint) -> Self {
        let layer = self
        layer.position = point
        return self
    }
    
    @discardableResult
    func zPosition(_ position: CGFloat) -> Self {
        let layer = self
        layer.zPosition = position
        return self
    }
    
    @discardableResult
    func anchorPoint(_ point: CGPoint) -> Self {
        let layer = self
        layer.anchorPoint = point
        return self
    }
    
    @discardableResult
    func anchorPointZ(_ position: CGFloat) -> Self {
        let layer = self
        layer.anchorPointZ = position
        return self
    }
    
    @discardableResult
    func transform(_ form: CATransform3D) -> Self {
        let layer = self
        layer.transform = form
        return self
    }
    
    @discardableResult
    func frame(_ rect: CGRect) -> Self {
        let layer = self
        layer.frame = rect
        return self
    }
    
    @discardableResult
    func hidden(_ hidden: Bool) -> Self {
        let layer = self
        layer.isHidden = hidden
        return self
    }
    
    @discardableResult
    func doubleSided(_ doubleSided: Bool) -> Self {
        let layer = self
        layer.isDoubleSided = doubleSided
        return self
    }
    
    @discardableResult
    func geometryFlipped(_ geometryFlipped: Bool) -> Self {
        let layer = self
        layer.isGeometryFlipped = geometryFlipped
        return self
    }
    
    @discardableResult
    func sublayers(_ layers: [CALayer]) -> Self {
        let layer = self
        layer.sublayers = layers
        return self
    }
    
    @discardableResult
    func addSublayers(_ layers: [CALayer]) -> Self {
        let layer = self
        layer.sublayers = layers
        return self
    }
    
    @discardableResult
    func mask(_ maskLayer: CALayer) -> Self {
        let layer = self
        layer.mask = maskLayer
        return self
    }
    
    @discardableResult
    func masksToBounds(_ mask: Bool) -> Self {
        let layer = self
        layer.masksToBounds = mask
        return self
    }
    
    @discardableResult
    func contents(_ content: Any?) -> Self {
        let layer = self
        layer.contents = content
        return self
    }
    
    @discardableResult
    func contentsRect(_ rect: CGRect) -> Self {
        let layer = self
        layer.contentsRect = rect
        return self
    }
    
    @discardableResult
    func contentsGravity(_ gravity: CALayerContentsGravity) -> Self {
        let layer = self
        layer.contentsGravity = gravity
        return self
    }
    
    @discardableResult
    func contentsScale(_ scale: CGFloat) -> Self {
        let layer = self
        layer.contentsScale = scale
        return self
    }
    
    @discardableResult
    func contentsCenter(_ rect: CGRect) -> Self {
        let layer = self
        layer.contentsCenter = rect
        return self
    }
    
    @discardableResult
    func contentsFormat(_ format: CALayerContentsFormat) -> Self {
        let layer = self
        if #available(iOS 10.0, *) {
            layer.contentsFormat = format
        }
        return self
    }
    
    @discardableResult
    func minificationFilter(_ filter: CALayerContentsFilter) -> Self {
        let layer = self
        layer.minificationFilter = filter
        return self
    }
    
    @discardableResult
    func magnificationFilter(_ filter: CALayerContentsFilter) -> Self {
        let layer = self
        layer.magnificationFilter = filter
        return self
    }
    
    @discardableResult
    func minificationFilterBias(_ filterBias: Float) -> Self {
        let layer = self
        layer.minificationFilterBias = filterBias
        return self
    }
    
    @discardableResult
    func isOpaque(_ opaque: Bool) -> Self {
        let layer = self
        layer.isOpaque = opaque
        return self
    }
    
    @discardableResult
    func needsDisplayOnBoundsChange(_ change: Bool) -> Self {
        let layer = self
        layer.needsDisplayOnBoundsChange = change
        return self
    }
    
    @discardableResult
    func drawsAsynchronously(_ isAsync: Bool) -> Self {
        let layer = self
        layer.drawsAsynchronously = isAsync
        return self
    }
    
    @discardableResult
    func edgeAntialiasingMask(_ mask: CAEdgeAntialiasingMask) -> Self {
        let layer = self
        layer.edgeAntialiasingMask = mask
        return self
    }
    
    @discardableResult
    func allowsEdgeAntialiasing(_ allow: Bool) -> Self {
        let layer = self
        layer.allowsEdgeAntialiasing = allow
        return self
    }
    
    @discardableResult
    func backgroundColor(_ color: CGColor?) -> Self {
        let layer = self
        layer.backgroundColor = color
        return self
    }
    
    @discardableResult
    func cornerRadius(_ radius: CGFloat) -> Self {
        let layer = self
        layer.cornerRadius = radius
        return self
    }
    
    @available(iOS 11.0,*)
    @discardableResult
    func maskedCorners(_ corners: CACornerMask) -> Self {
        let layer = self
        layer.maskedCorners = corners
        return self
    }
    
    @available(iOS 13.0,*)
    @discardableResult
    func cornerCurve(_ curve: CALayerCornerCurve) -> Self {
        let layer = self
        layer.cornerCurve = curve
        return self
    }
    
    @discardableResult
    func borderWidth(_ width: CGFloat) -> Self {
        let layer = self
        layer.borderWidth = width
        return self
    }
    
    @discardableResult
    func borderColor(_ color: CGColor?) -> Self {
        let layer = self
        layer.borderColor = color
        return self
    }
    
    @discardableResult
    func opacity(_ alpha: Float) -> Self {
        let layer = self
        layer.opacity = alpha
        return self
    }
    
    @discardableResult
    func allowsGroupOpacity(_ allow: Bool) -> Self {
        let layer = self
        layer.allowsGroupOpacity = allow
        return self
    }
    
    @discardableResult
    func compositingFilter(_ filter: Any?) -> Self {
        let layer = self
        layer.compositingFilter = filter
        return self
    }
    
    @discardableResult
    func filters(_ array: [Any]?) -> Self {
        let layer = self
        layer.filters = array
        return self
    }
    
    @discardableResult
    func backgroundFilters(_ array: [Any]?) -> Self {
        let layer = self
        layer.backgroundFilters = array
        return self
    }
    
    @discardableResult
    func shouldRasterize(_ should: Bool) -> Self {
        let layer = self
        layer.shouldRasterize = should
        return self
    }
    
    @discardableResult
    func rasterizationScale(_ scale: CGFloat) -> Self {
        let layer = self
        layer.rasterizationScale = scale
        return self
    }
    
    @discardableResult
    func shadowColor(_ color: CGColor?) -> Self {
        let layer = self
        layer.shadowColor = color
        return self
    }
    
    @discardableResult
    func shadowOpacity(_ opacity: Float) -> Self {
        let layer = self
        layer.shadowOpacity = opacity
        return self
    }
    
    @discardableResult
    func shadowOffset(_ offset: CGSize) -> Self {
        let layer = self
        layer.shadowOffset = offset
        return self
    }
    
    @discardableResult
    func shadowPath(_ path: CGPath?) -> Self {
        let layer = self
        layer.shadowPath = path
        return self
    }
    
    @discardableResult
    func name(_ text: String?) -> Self {
        let layer = self
        layer.name = text
        return self
    }
    
    @discardableResult
    func delegate(_ del: CALayerDelegate?) -> Self {
        let layer = self
        layer.delegate = del
        return self
    }
    
    @discardableResult
    func style(_ dic: [AnyHashable : Any]?) -> Self {
        let layer = self
        layer.style = dic
        return self
    }
}


public extension CAShapeLayer {
    /**
     open var path: CGPath?

     
     /* The color to fill the path, or nil for no fill. Defaults to opaque
      * black. Animatable. */
     
     open var fillColor: CGColor?

     
     /* The fill rule used when filling the path. Options are `non-zero' and
      * `even-odd'. Defaults to `non-zero'. */
     
     open var fillRule: CAShapeLayerFillRule

     
     /* The color to fill the path's stroked outline, or nil for no stroking.
      * Defaults to nil. Animatable. */
     
     open var strokeColor: CGColor?

     
     /* These values define the subregion of the path used to draw the
      * stroked outline. The values must be in the range [0,1] with zero
      * representing the start of the path and one the end. Values in
      * between zero and one are interpolated linearly along the path
      * length. strokeStart defaults to zero and strokeEnd to one. Both are
      * animatable. */
     
     open var strokeStart: CGFloat

     open var strokeEnd: CGFloat

     
     /* The line width used when stroking the path. Defaults to one.
      * Animatable. */
     
     open var lineWidth: CGFloat

     
     /* The miter limit used when stroking the path. Defaults to ten.
      * Animatable. */
     
     open var miterLimit: CGFloat

     
     /* The cap style used when stroking the path. Options are `butt', `round'
      * and `square'. Defaults to `butt'. */
     
     open var lineCap: CAShapeLayerLineCap

     
     /* The join style used when stroking the path. Options are `miter', `round'
      * and `bevel'. Defaults to `miter'. */
     
     open var lineJoin: CAShapeLayerLineJoin

     
     /* The phase of the dashing pattern applied when creating the stroke.
      * Defaults to zero. Animatable. */
     
     open var lineDashPhase: CGFloat

     
     /* The dash pattern (an array of NSNumbers) applied when creating the
      * stroked version of the path. Defaults to nil. */
     
     open var lineDashPattern: [NSNumber]?
     */
    
    @discardableResult
    func path(_ p: CGPath?) -> Self {
        let layer = self
        layer.path = p
        return layer
    }
    
    @discardableResult
    func fillColor(_ color: CGColor?) -> Self {
        let layer = self
        layer.fillColor = color
        return layer
    }
    
    @discardableResult
    func fillRule(_ rule: CAShapeLayerFillRule) -> Self {
        let layer = self
        layer.fillRule = rule
        return layer
    }
    
    @discardableResult
    func strokeColor(_ color: CGColor?) -> Self {
        let layer = self
        layer.strokeColor = color
        return layer
    }
    
    @discardableResult
    func strokeStart(_ point: CGFloat) -> Self {
        let layer = self
        layer.strokeStart = point
        return layer
    }
    
    @discardableResult
    func strokeEnd(_ point: CGFloat) -> Self {
        let layer = self
        layer.strokeEnd = point
        return layer
    }
    
    @discardableResult
    func lineWidth(_ width: CGFloat) -> Self {
        let layer = self
        layer.lineWidth = width
        return layer
    }
    
    @discardableResult
    func miterLimit(_ limit: CGFloat) -> Self {
        let layer = self
        layer.miterLimit = limit
        return layer
    }
    
    @discardableResult
    func lineCap(_ cap: CAShapeLayerLineCap) -> Self {
        let layer = self
        layer.lineCap = cap
        return layer
    }
    
    @discardableResult
    func lineJoin(_ join: CAShapeLayerLineJoin) -> Self {
        let layer = self
        layer.lineJoin = join
        return layer
    }
    
    @discardableResult
    func lineDashPhase(_ phase: CGFloat) -> Self {
        let layer = self
        layer.lineDashPhase = phase
        return layer
    }
    
    @discardableResult
    func lineDashPattern(_ patterns: [NSNumber]?) -> Self {
        let layer = self
        layer.lineDashPattern = patterns
        return layer
    }
}

public extension CAGradientLayer {
    @discardableResult
    func colors(_ colors: [Any]?) -> Self {
        let layer = self
        layer.colors = colors
        return layer
    }
    
    @discardableResult
    func locations(_ locations: [NSNumber]?) -> Self {
        let layer = self
        layer.locations = locations
        return layer
    }
    
    @discardableResult
    func startPoint(_ point: CGPoint) -> Self {
        let layer = self
        layer.startPoint = point
        return layer
    }
    
    @discardableResult
    func endPoint(_ point: CGPoint) -> Self {
        let layer = self
        layer.endPoint = point
        return layer
    }
    
    @discardableResult
    func type(_ type: CAGradientLayerType) -> Self {
        let layer = self
        layer.type = type
        return layer
    }
}
