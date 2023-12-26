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
}
