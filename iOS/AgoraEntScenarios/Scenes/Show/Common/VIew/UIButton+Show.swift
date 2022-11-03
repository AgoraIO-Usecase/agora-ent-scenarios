//
//  UIButton+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/3.
//

import UIKit

extension UIButton {
    
    @objc enum ShowDirectionType: Int {
        case top, bottom, left, right
    }
    
    func setDirection(_ direction: ShowDirectionType, offset: CGFloat) {
        if imageView?.image == nil || imageView?.image?.size.width == 0 || imageView?.image?.size.height == 0 {
            fatalError("Please set the image first")
            
        }
        if titleLabel?.text?.count == 0 || titleLabel?.text == "" {
            fatalError("Please set the title first")
        }
        guard let imgSize = imageView?.image?.size else { return }
        guard let titleLabel = titleLabel else { return }
        let attributes = [NSAttributedString.Key.font: titleLabel.font]
        guard let text: NSString = titleLabel.text as? NSString else { return }
        let textSize = text.boundingRect(with: CGSize(width: bounds.size.width - imgSize.width, height: bounds.size.height), options: .usesLineFragmentOrigin, context: nil).size
        
        switch (direction) {
        case .top:
            imageEdgeInsets = UIEdgeInsets(top: 0, left: textSize.width/2, bottom: textSize.height+offset, right: -textSize.width/2)
            titleEdgeInsets = UIEdgeInsets(top:imgSize.height+offset, left: -imgSize.width, bottom: 0,right: 0)
        case .left:
                imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: offset)
                titleEdgeInsets = UIEdgeInsets(top:0, left: offset, bottom: 0, right: 0)
        case .bottom:
            imageEdgeInsets = UIEdgeInsets(top: textSize.height+offset, left: textSize.width/2,bottom: 0,right: -textSize.width/2);
            titleEdgeInsets = UIEdgeInsets(top: 0,left: -imgSize.width, bottom: imgSize.height+offset,right: 0)
        case .right:
            imageEdgeInsets = UIEdgeInsets(top: 0, left: textSize.width+offset,bottom: 0, right: -textSize.width);
            titleEdgeInsets = UIEdgeInsets(top: 0, left: -imgSize.width-offset, bottom: 0, right: imgSize.width);
        }
    }
}
