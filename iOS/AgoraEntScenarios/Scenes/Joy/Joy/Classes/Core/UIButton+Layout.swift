//
//  UIButton+Layout.swift
//  Joy
//
//  Created by wushengtao on 2023/7/25.
//

import Foundation

extension UIButton {
    func adjustVerticallyAlign(spacing: CGFloat = 0) {
        let imageSize = imageView!.frame.size
        let titleSize = titleLabel!.frame.size
        titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageSize.width, bottom: -(imageSize.height + spacing), right: 0)
        imageEdgeInsets = UIEdgeInsets(top: -(titleSize.height + spacing), left: 0, bottom: 0, right: -titleSize.width)
    }
    
    func adjustHorizonAlign(spacing: CGFloat = 0) {
        let imageSize = imageView!.frame.size
        let titleSize = titleLabel!.frame.size
        titleEdgeInsets = UIEdgeInsets(top: 0, left: spacing, bottom: 0, right: 0)
        imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: spacing)
    }
}
