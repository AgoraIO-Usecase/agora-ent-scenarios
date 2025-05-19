//
//  UIViewGradientLayer.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit

extension UIView {
    // 关联对象的key
    private struct AssociatedKeys {
        static var gradientLayer = "GradientLayerKey"
        static var gradientSet = "GradientSetKey"
    }
    
    // 检查渐变是否已经设置
    private var isGradientSet: Bool {
        get {
            return (objc_getAssociatedObject(self, &AssociatedKeys.gradientSet) as? Bool) ?? false
        }
        set {
            objc_setAssociatedObject(self, &AssociatedKeys.gradientSet, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        }
    }
    
    @discardableResult
    func createGradient(_ colors: [UIColor], _ points: [CGPoint], _ locations: [NSNumber]) -> Self {
        // 如果渐变已经设置，直接返回
        guard !isGradientSet else { return self }
        
        let gradientColors: [CGColor] = colors.map { $0.cgColor }
        let startPoint = points[0]
        let endPoint = points[1]
        
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = gradientColors
        gradientLayer.startPoint = startPoint
        gradientLayer.endPoint = endPoint
        gradientLayer.frame = bounds
        gradientLayer.locations = locations
        
        layer.insertSublayer(gradientLayer, at: 0)
        
        // 存储gradientLayer
        objc_setAssociatedObject(self, &AssociatedKeys.gradientLayer, gradientLayer, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        
        // 标记渐变已设置
        isGradientSet = true
        
        return self
    }
    
    // 可选：添加一个方法来移除渐变层
    func removeGradient() {
        guard isGradientSet,
              let gradientLayer = objc_getAssociatedObject(self, &AssociatedKeys.gradientLayer) as? CAGradientLayer else {
            return
        }
        
        gradientLayer.removeFromSuperlayer()
        objc_setAssociatedObject(self, &AssociatedKeys.gradientLayer, nil, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        isGradientSet = false
    }
}

