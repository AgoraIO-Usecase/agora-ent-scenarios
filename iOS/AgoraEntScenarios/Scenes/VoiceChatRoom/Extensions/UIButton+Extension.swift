//
//  UIButton+Extension.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/10/11.
//

import Foundation
import UIKit

var expandSizeKey = "expandSizeKey"

extension UIButton {
    open func vm_expandSize(size: CGFloat) {
        objc_setAssociatedObject(self, &expandSizeKey, size, objc_AssociationPolicy.OBJC_ASSOCIATION_COPY)
    }

    private func expandRect() -> CGRect {
        let expandSize = objc_getAssociatedObject(self, &expandSizeKey)
        if expandSize != nil {
            return CGRect(x: bounds.origin.x - (expandSize as! CGFloat), y: bounds.origin.y - (expandSize as! CGFloat), width: bounds.size.width + 2 * (expandSize as! CGFloat), height: bounds.size.height + 2 * (expandSize as! CGFloat))
        } else {
            return bounds
        }
    }

    override open func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        let buttonRect = expandRect()
        if buttonRect.equalTo(bounds) {
            return super.point(inside: point, with: event)
        } else {
            return buttonRect.contains(point)
        }
    }
}
