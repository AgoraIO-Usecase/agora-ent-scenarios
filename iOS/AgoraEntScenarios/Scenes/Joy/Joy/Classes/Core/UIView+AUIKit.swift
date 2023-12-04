//
//  UIView+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/24.
//

import UIKit

extension UIView {
    @objc public var aui_left: CGFloat {
        set {
            self.frame = CGRect(x: newValue,
                                y: self.frame.origin.y,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin.x
        }
    }
    
    @objc public var aui_right: CGFloat {
        set {
            self.frame = CGRect(x: newValue - self.frame.width,
                                y: self.frame.origin.y,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin.x + self.frame.width
        }
    }
    
    @objc public var aui_top: CGFloat {
        set {
            self.frame = CGRect(x: self.frame.origin.x,
                                y: newValue,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin.y
        }
    }
    
    @objc public var aui_bottom: CGFloat {
        set {
            self.frame = CGRect(x: self.frame.origin.x,
                                y: newValue - self.frame.height,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin.y + self.frame.height
        }
    }
    
    @objc public var aui_center: CGPoint {
        set {
            self.frame = CGRect(x: newValue.x - self.frame.width / 2,
                                y: newValue.y - self.frame.height / 2,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return CGPoint(x: self.frame.origin.x + self.frame.width / 2, y: self.frame.origin.y + self.frame.height / 2)
        }
    }
    
    @objc public var aui_centerX: CGFloat {
        set {
            self.frame = CGRect(x: newValue - self.frame.width / 2,
                                y: self.frame.origin.y,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin.x + self.frame.width / 2
        }
    }
    
    @objc public var aui_centerY: CGFloat {
        set {
            self.frame = CGRect(x: self.frame.origin.x,
                                y: newValue - self.frame.height / 2,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin.y + self.frame.height / 2
        }
    }
    
    @objc public var aui_centerRatioX: CGFloat {
        set {
            let centerX = self.frame.width * newValue
            self.frame = CGRect(x: centerX - self.frame.width / 2,
                                y: self.frame.origin.y,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return (self.frame.origin.x + self.frame.width / 2) / self.frame.width
        }
    }
    
    @objc public var aui_centerRatioY: CGFloat {
        set {
            let centerY = self.frame.height * newValue
            self.frame = CGRect(x: self.frame.origin.x,
                                y: centerY - self.frame.width / 2,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return (self.frame.origin.y + self.frame.height / 2) / self.frame.height
        }
    }
    
    @objc public var aui_width: CGFloat {
        set {
            self.frame = CGRect(x: self.frame.origin.x,
                                y: self.frame.origin.y,
                                width: newValue,
                                height: self.frame.height)
        }
        get {
            return self.frame.width
        }
    }
    
    @objc public var aui_height: CGFloat {
        set {
            self.frame = CGRect(x: self.frame.origin.x,
                                y: self.frame.origin.y,
                                width: self.frame.width,
                                height: newValue)
        }
        get {
            return self.frame.height
        }
    }
    
    @objc public var aui_size: CGSize {
        set {
            self.frame = CGRect(x: self.frame.origin.x,
                                y: self.frame.origin.y,
                                width: newValue.width,
                                height: newValue.height)
        }
        get {
            return self.frame.size
        }
    }
    
    //左上角
    @objc public var aui_tl: CGPoint {
        set {
            self.frame = CGRect(x: newValue.x,
                                y: newValue.y,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return self.frame.origin
        }
    }
    
    //右上角
    @objc public var aui_tr: CGPoint {
        set {
            self.frame = CGRect(x: newValue.x - self.frame.width,
                                y: newValue.y,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return CGPoint(x: self.aui_right, y: self.aui_top)
        }
    }
    
    //左下角
    @objc public var aui_bl: CGPoint {
        set {
            self.frame = CGRect(x: newValue.x,
                                y: newValue.y - self.frame.height,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return CGPoint(x: self.aui_left, y: self.aui_bottom)
        }
    }
    
    //右下角
    @objc public var aui_br: CGPoint {
        set {
            self.frame = CGRect(x: newValue.x - self.frame.width,
                                y: newValue.y - self.frame.height,
                                width: self.frame.width,
                                height: self.frame.height)
        }
        get {
            return CGPoint(x: self.aui_right, y: self.aui_bottom)
        }
    }
    
    func setCornerRadius(_ radius: CGFloat) {
        let path = UIBezierPath(roundedRect: self.bounds, cornerRadius: radius)
        let shape = CAShapeLayer()
        shape.path = path.cgPath
        self.layer.mask = shape
    }
}

extension UIView {
    func setRoundedCorner(radius: CGFloat) {
        layer.cornerRadius = radius
        layer.masksToBounds = true
    }
    
    func setRoundedCorner(topLeft: CGFloat, topRight: CGFloat, bottomLeft: CGFloat, bottomRight: CGFloat) {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: bounds.minX + topLeft, y: bounds.minY))
        path.addLine(to: CGPoint(x: bounds.maxX - topRight, y: bounds.minY))
        path.addQuadCurve(to: CGPoint(x: bounds.maxX, y: bounds.minY + topRight), controlPoint: CGPoint(x: bounds.maxX, y: bounds.minY))
        path.addLine(to: CGPoint(x: bounds.maxX, y: bounds.maxY - bottomRight))
        path.addQuadCurve(to: CGPoint(x: bounds.maxX - bottomRight, y: bounds.maxY), controlPoint: CGPoint(x: bounds.maxX, y: bounds.maxY))
        path.addLine(to: CGPoint(x: bounds.minX + bottomLeft, y: bounds.maxY))
        path.addQuadCurve(to: CGPoint(x: bounds.minX, y: bounds.maxY - bottomLeft), controlPoint: CGPoint(x: bounds.minX, y: bounds.maxY))
        path.addLine(to: CGPoint(x: bounds.minX, y: bounds.minY + topLeft))
        path.addQuadCurve(to: CGPoint(x: bounds.minX + topLeft, y: bounds.minY), controlPoint: CGPoint(x: bounds.minX, y: bounds.minY))

        let mask = CAShapeLayer()
        mask.path = path.cgPath
        layer.mask = mask
    }
}
