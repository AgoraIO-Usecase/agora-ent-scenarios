//
//  UIButton+EntLayout.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/12/19.
//

import Foundation

extension UIButton {
    @objc var spacingBetweenImageAndTitle: CGFloat {
        set {
//            self.set
            self.setInsets(contentPadding: UIEdgeInsets.zero, imageTitlePadding: newValue)
        }
        get {
            return 0
        }
    }
    
    func setInsets(contentPadding: UIEdgeInsets,
                   imageTitlePadding: CGFloat) {
        self.contentEdgeInsets = UIEdgeInsets(
            top: contentPadding.top,
            left: contentPadding.left,
            bottom: contentPadding.bottom,
            right: contentPadding.right + imageTitlePadding
        )
        self.titleEdgeInsets = UIEdgeInsets(
            top: 0,
            left: imageTitlePadding,
            bottom: 0,
            right: -imageTitlePadding
        )
    }
}
