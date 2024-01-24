//
//  CGSize+Joy.swift
//  Joy
//
//  Created by wushengtao on 2023/12/18.
//

import Foundation

public extension CGSize {
    func fitRect(imageSize: CGSize, scaleToInner: Bool = false) -> CGRect {
        guard imageSize.width != 0, imageSize.height != 0 else {
            assert(imageSize.width != 0)
            assert(imageSize.height != 0)
            return .zero
        }
        if (scaleToInner) {
            //缩放到self内
            let maxSize = CGSize(width: abs(self.width), height: abs(self.height))
            let ratio: CGFloat = abs(imageSize.width / imageSize.height)
            let maxRatio: CGFloat = abs(maxSize.width / maxSize.height)
            var fitSize: CGSize = maxRatio > ratio ?
                CGSize(width:round(maxSize.height * ratio), height:round(maxSize.height)) :
                CGSize(width:round(maxSize.width), height:round(maxSize.width / ratio))
            let scaleW: CGFloat = fitSize.width / maxSize.width
            let scaleH: CGFloat = fitSize.height / maxSize.height
            let scale: CGFloat = max(scaleW, scaleH)
            fitSize = CGSize(width:round(fitSize.width * scale), height:round(fitSize.height * scale));
            
            return CGRect(x: (maxSize.width - fitSize.width) / 2, y: (maxSize.height - fitSize.height) / 2, width: fitSize.width, height: fitSize.height);
        } else {
            //缩放到撑满self
            let maxSize = CGSize(width: abs(self.width), height: abs(self.height))
            let ratio: CGFloat = abs(maxSize.width / imageSize.width)
            let maxRatio: CGFloat = abs(maxSize.height / imageSize.height)
            let fitRatio = fmax(maxRatio, ratio)
            let fitSize: CGSize =
                CGSize(width: imageSize.width * fitRatio, height: imageSize.height * fitRatio)
            var x: CGFloat = 0.0, y: CGFloat = 0.0
            if fitSize.width > maxSize.width {
                x = (maxSize.width - fitSize.width) / 2.0
            } else if fitSize.height > maxSize.height {
                y = maxSize.height - fitSize.height
            }
            return CGRect(origin: CGPoint(x: x, y: y), size: fitSize)

        }
    }
}
