//
//  UIImageView+Extension.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/10/23.
//

import Foundation

extension UIImageView {
    
    /// 根据视图宽高自适应下载阿里云的图片
    /// - Parameters:
    ///   - urlString: 阿里云上的图片地址
    ///   - placeholderImage: 占位图
    @objc func autoResizeWithAliyunUrlString(_ urlString: String, placeholderImage: UIImage?){
        let newUrlString = String(format: "\(urlString)?x-oss-process=image/resize,w_%.f,h_%.f", self.bounds.size.width * 3, self.bounds.size.height * 3)
        if let url = URL(string: newUrlString) {
            sd_setImage(with: url, placeholderImage: placeholderImage)
        }
    }
}
