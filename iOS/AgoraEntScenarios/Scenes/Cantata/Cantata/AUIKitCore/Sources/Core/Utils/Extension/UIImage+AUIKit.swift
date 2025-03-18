//
//  UIImage+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/3.
//

import UIKit
import SwiftTheme

//public var themeResourcePaths: Set<URL> = Set()
extension UIImage {
    public class func aui_Image(named: String) -> UIImage? {
        if let filePath = String.aui_imageFilePath(named: named),
           let image = UIImage(contentsOfFile: filePath)  {
            return image
        }
        
        aui_error("load image not found imagefilePath: \(ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path ?? named)")
        assert(false)

        return nil
    }
}

extension String {
    public static func aui_imageFilePath(named: String) -> String? {
        let scales = ["@3x", "@2x", ""]
        for path in AUIThemeManager.shared.themeResourcePaths {
            for scale in scales {
                let filePath = path.appendingPathComponent(named).path.appendPngExentionIfEmpty(scale: scale)
                if FileManager.default.fileExists(atPath: filePath) {
                    return filePath
                }
            }
        }
        
        for scale in scales {
            let filePath = ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path.appendPngExentionIfEmpty(scale: scale) ?? ""
            if FileManager.default.fileExists(atPath: filePath) {
                return filePath
            }
        }
        return nil
    }
    
    private func appendPngExentionIfEmpty(scale: String = "") -> String{
        let url = URL(fileURLWithPath: self)
        if url.pathExtension.isEmpty {
            return appending("\(scale).png")
        }
        return self
    }
    
}

extension URL {
    public static func aui_imageFileURL(named: String) -> URL? {
        if let path = String.aui_imageFilePath(named: named) {
            return URL(fileURLWithPath: path)
        }
        return nil
    }
    
   
}
