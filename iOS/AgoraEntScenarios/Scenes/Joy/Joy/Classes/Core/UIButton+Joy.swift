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
    
    func setjoyVerticalDefaultGradientBackground() {
        let colors = [
            UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1),
            UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)
        ] // 渐变颜色数组
        let startPoint = CGPoint(x: 0, y: 0) // 渐变起点
        let endPoint = CGPoint(x: 0, y: 1) // 渐变终点
        let cornerRadius: CGFloat = aui_height / 2 // 圆角半径
        setGradientBackground(colors: colors, startPoint: startPoint, endPoint: endPoint, cornerRadius: cornerRadius)
    }
    
    func setjoyHorizonalDefaultGradientBackground() {
        let colors = [
            UIColor(hexString: "#219BFF")!,
            UIColor(hexString: "#345DFF")!,
            UIColor(hexString: "#53D8F7")!,
            UIColor(hexString: "#9D5BFF")!,
            UIColor(hexString: "#CC35F2")!
        ] // 渐变颜色数组
        let startPoint = CGPoint(x: 0, y: 0) // 渐变起点
        let endPoint = CGPoint(x: 1, y: 0) // 渐变终点
        let cornerRadius: CGFloat = aui_height / 2 // 圆角半径
        setGradientBackground(colors: colors, startPoint: startPoint, endPoint: endPoint, cornerRadius: cornerRadius)
    }
}
