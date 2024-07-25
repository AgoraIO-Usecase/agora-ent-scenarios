//
//  UIImage+Color.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2022/3/21.
//

import UIKit

extension UIImage {
    func color(_ color: UIColor,
               width: CGFloat,
               height: CGFloat,
               cornerRadius: CGFloat) -> UIImage? {
        let rect = CGRect(x: 0.0, y: 0.0, width: width, height: height)
        UIGraphicsBeginImageContext(rect.size)
        let context = UIGraphicsGetCurrentContext()
        let path = UIBezierPath(roundedRect: rect, cornerRadius: cornerRadius) // Adjust cornerRadius as needed
        context?.setFillColor(color.cgColor)
        path.fill()
        
        let img: UIImage? = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return img
    }
}
