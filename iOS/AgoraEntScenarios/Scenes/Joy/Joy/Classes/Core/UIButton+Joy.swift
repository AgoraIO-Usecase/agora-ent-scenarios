//
//  UIButton+Joy.swift
//  Joy
//
//  Created by wushengtao on 2023/11/30.
//

import Foundation

extension UIButton {
    func setGradientBackground(colors: [UIColor], startPoint: CGPoint, endPoint: CGPoint, cornerRadius: CGFloat) {
        let gradientLayer = CAGradientLayer()
        gradientLayer.frame = bounds
        gradientLayer.colors = colors.map { $0.cgColor }
        gradientLayer.startPoint = startPoint
        gradientLayer.endPoint = endPoint
        gradientLayer.cornerRadius = cornerRadius
        
        UIGraphicsBeginImageContext(gradientLayer.bounds.size)
        gradientLayer.render(in: UIGraphicsGetCurrentContext()!)
        let backgroundImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        setBackgroundImage(backgroundImage, for: .normal)
    }
    
    func setjoyDefaultGradientBackground() {
        let colors = [
            UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1),
            UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)
        ] // 渐变颜色数组
        let startPoint = CGPoint(x: 0, y: 0) // 渐变起点
        let endPoint = CGPoint(x: 0, y: 1) // 渐变终点
        let cornerRadius: CGFloat = aui_height / 2 // 圆角半径
        setGradientBackground(colors: colors, startPoint: startPoint, endPoint: endPoint, cornerRadius: cornerRadius)
    }
}
