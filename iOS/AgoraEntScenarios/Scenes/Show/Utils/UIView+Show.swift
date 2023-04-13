//
//  UIView+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import Foundation

extension UIView {
    /// 设置部分圆角 ( 需要在frame确定后）
    /// - Parameters:
    ///   - corners: 指定的圆角位置
    ///   - radius: 圆角半径
    func setRoundingCorners(_ corners: UIRectCorner, rect: CGRect? = nil, radius: CGFloat) {
        let path = UIBezierPath(roundedRect: rect ?? bounds, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        let shapeLayer = CAShapeLayer()
        shapeLayer.path = path.cgPath
        layer.mask = shapeLayer
        layer.masksToBounds = true
    }
}


