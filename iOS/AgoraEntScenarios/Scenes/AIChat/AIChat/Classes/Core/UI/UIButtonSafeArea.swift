//
//  UIButtonSafeArea.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//
import UIKit

extension UIButton {
    
    private var hitTestEdgeInsets: UIEdgeInsets? {
        get {
            return objc_getAssociatedObject(self, &AssociatedKeys.hitTestEdgeInsets) as? UIEdgeInsets
        }
        set {
            objc_setAssociatedObject(self, &AssociatedKeys.hitTestEdgeInsets, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        }
    }

    private struct AssociatedKeys {
        static var hitTestEdgeInsets = "hitTestEdgeInsets"
    }
    
    open override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        if let hitTestEdgeInsets = self.hitTestEdgeInsets {
            let hitArea = bounds.inset(by: hitTestEdgeInsets)
            return hitArea.contains(point)
        }
        return super.point(inside: point, with: event)
    }
    
    public func setHitTestEdgeInsets(_ edgeInsets: UIEdgeInsets) {
        self.hitTestEdgeInsets = edgeInsets
    }
}

