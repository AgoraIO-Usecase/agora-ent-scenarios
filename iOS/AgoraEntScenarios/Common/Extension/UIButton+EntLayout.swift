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

extension UIButton {
    /// Image position in counterclockwise direction🔄
    @objc enum ImgPosition: Int { case top, left, bottom, right }

    /// Reset the position of image and title (default spacing is 0)
    @objc func adjustImageTitlePosition(_ position: ImgPosition, spacing: CGFloat = 0 ) {
         self.sizeToFit()
         
         let imageWidth = self.imageView?.image?.size.width
         let imageHeight = self.imageView?.image?.size.height
         
         let labelWidth = self.titleLabel?.frame.size.width
         let labelHeight = self.titleLabel?.frame.size.height
         
         switch position {
         case .top:
             imageEdgeInsets = UIEdgeInsets(top: -labelHeight! - spacing / 2, left: 0, bottom: 0, right: -labelWidth!)
             titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageWidth!, bottom: -imageHeight! - spacing / 2, right: 0)
             self.titleLabel?.frame = CGRect(x: 0, y: 0, width: labelWidth!, height: labelWidth!)
             self.imageView?.frame = CGRect(x: 0, y: 0, width: imageWidth!, height: imageWidth!)
             break
             
         case .left:
             imageEdgeInsets = UIEdgeInsets(top: 0, left: -spacing / 2, bottom: 0, right: 0)
             titleEdgeInsets = UIEdgeInsets(top: 0, left: spacing * 1.5, bottom: 0, right: 0)
             break
             
         case .bottom:
             imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: -labelHeight! - spacing / 2, right: -labelWidth!)
             titleEdgeInsets = UIEdgeInsets(top: -imageHeight! - spacing / 2, left: -imageWidth!, bottom: 0, right: 0)
             break
             
         case .right:
             imageEdgeInsets = UIEdgeInsets(top: 0, left: labelWidth! + spacing / 2, bottom: 0, right: -labelWidth! - spacing / 2)
             titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageWidth! - spacing / 2, bottom: 0, right: imageWidth! + spacing / 2)
             break
         }
     }
}
