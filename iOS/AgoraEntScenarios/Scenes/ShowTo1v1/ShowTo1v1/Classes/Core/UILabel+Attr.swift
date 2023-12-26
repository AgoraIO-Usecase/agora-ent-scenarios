//
//  UILabel+Attr.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/10.
//

import Foundation

struct AUILabelAttrInfo {
    var size: CGSize = .zero
    var content: Any?    //UIImage / String
}

extension UILabel {
    static func createAttrLabel(font: UIFont, attrInfos: [AUILabelAttrInfo]) -> UILabel {
        let label = UILabel()
        label.font = font
        
        let attrStr = NSMutableAttributedString()
        for attr in attrInfos {
            if let content = attr.content as? String {
                let textAttr = NSAttributedString(string: content)
                attrStr.append(textAttr)
            } else if let content = attr.content as? UIImage {
                let attach = NSTextAttachment()
                attach.image = content
                let imageSize = attr.size
                attach.bounds = CGRect(origin: CGPoint(x: 0, y: (label.font.capHeight - imageSize.height).rounded() / 2), size: imageSize)
                let imgAttr = NSAttributedString(attachment: attach)
                attrStr.append(imgAttr)
            }
        }
        
        label.attributedText = attrStr
        
        return label
    }
}
