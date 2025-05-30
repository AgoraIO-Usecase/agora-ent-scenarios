//
//  MessageBubbleMultiCorner.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/2.
//

import UIKit

@objc public enum BubbleTowards: UInt {
    case right
    case left
}

@objc public class BubbleCornerRadius: NSObject {
    var topLeft: CGFloat
    var topRight: CGFloat
    var bottomLeft: CGFloat
    var bottomRight: CGFloat
    
    init(topLeft: CGFloat, topRight: CGFloat, bottomLeft: CGFloat, bottomRight: CGFloat) {
        self.topLeft = topLeft
        self.topRight = topRight
        self.bottomLeft = bottomLeft
        self.bottomRight = bottomRight
    }
}

@objc open class MessageBubbleMultiCorner: UIView {
    
    public var towards = BubbleTowards.right
        
    internal override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc required public init(frame: CGRect, forward: BubbleTowards) {
        self.towards = forward
        super.init(frame: frame)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updateBubbleCorner() {
        let shapeLayer = CAShapeLayer()
        let receiveColor = UIColor(0xE8E4F5)
        let sendColor = UIColor.theme.primaryColor5
        let path = self.roundedRect(bounds: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height))
        shapeLayer.path = path
        self.layer.mask = shapeLayer
        self.clipsToBounds = false
        self.backgroundColor = self.towards == .left ? receiveColor:sendColor
    }
    
    func roundedRect(bounds: CGRect) -> CGPath {
        let cornerRadius = self.towards == .left ? BubbleCornerRadius(topLeft: 10, topRight: 14, bottomLeft: 4, bottomRight: 14):BubbleCornerRadius(topLeft: 14, topRight: 10, bottomLeft: 14, bottomRight: 4)
        let minX = bounds.minX
        let minY = bounds.minY
        let maxX = bounds.maxX
        let maxY = bounds.maxY
        let topLeftCenterX = minX + cornerRadius.topLeft
        let topLeftCenterY = minY + cornerRadius.topLeft

        let bottomLeftCenterX = minX + cornerRadius.bottomLeft
        let bottomLeftCenterY = maxY - cornerRadius.bottomLeft

        let bottomRightCenterX = maxX - cornerRadius.bottomRight
        let bottomRightCenterY = maxY - cornerRadius.bottomRight

        let topRightCenterX = maxX - cornerRadius.topRight
        let topRightCenterY = minY + cornerRadius.topRight

        let path = CGMutablePath()

        //顶 左
        path.addArc(center: CGPoint(x: topLeftCenterX, y: topLeftCenterY), radius: cornerRadius.topLeft, startAngle: CGFloat(Double.pi), endAngle: CGFloat(Double.pi * 3 / 2), clockwise: false)
        //顶 右
        path.addArc(center: CGPoint(x: topRightCenterX, y: topRightCenterY), radius: cornerRadius.topRight, startAngle: CGFloat(Double.pi * 3 / 2), endAngle: 0, clockwise: false)
        //底 右
        path.addArc(center: CGPoint(x: bottomRightCenterX, y: bottomRightCenterY), radius: cornerRadius.bottomRight, startAngle: 0, endAngle: CGFloat(Double.pi / 2), clockwise: false)
        //底 左
        path.addArc(center: CGPoint(x: bottomLeftCenterX, y: bottomLeftCenterY), radius: cornerRadius.bottomLeft, startAngle: CGFloat(Double.pi / 2), endAngle: CGFloat(Double.pi), clockwise: false)
        path.closeSubpath()
        return path
    }

}
