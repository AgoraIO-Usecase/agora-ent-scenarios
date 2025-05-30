//
//  ThemeHelper.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/22.
//

import Foundation
import UIKit

public extension UIColor {
    /// 便利构造Hex颜色
    ///
    /// - Parameters:
    ///   - string: hex值
    ///   - alpha: alpha值，默认1.0
    convenience init(hex string: String, alpha: CGFloat = 1.0) {
        var hex = string.hasPrefix("#") ? String(string.dropFirst()) : string
        guard hex.count == 3 || hex.count == 6 else {
            self.init(white: 1.0, alpha: 0.0)
            return
        }

        if hex.count == 3 {
            for (indec, char) in hex.enumerated() {
                hex.insert(char, at: hex.index(hex.startIndex, offsetBy: indec * 2))
            }
        }

        self.init(
            red: CGFloat((Int(hex, radix: 16)! >> 16) & 0xFF) / 255.0,
            green: CGFloat((Int(hex, radix: 16)! >> 8) & 0xFF) / 255.0,
            blue: CGFloat(Int(hex, radix: 16)! & 0xFF) / 255.0,
            alpha: alpha
        )
    }

    var randomColor: UIColor {
        UIColor(red: CGFloat(arc4random() % 256) / 255.0,
                green: CGFloat(arc4random() % 256) / 255.0,
                blue: CGFloat(arc4random() % 256) / 255.0,
                alpha: 1)
    }
}

extension String {
    public func aui_allFilePaths() -> [String] {
        var filePaths: [String] = []
        let fileManager = FileManager.default
        guard let names = try? fileManager.contentsOfDirectory(atPath: self) else {
            return filePaths
        }
        names.forEach { name in
            var isDirectory: ObjCBool = ObjCBool(false)
            let filePath = "\(self)/\(name)"
            if fileManager.fileExists(atPath: filePath, isDirectory: &isDirectory) {
                if isDirectory.boolValue {
                    let paths = filePath.aui_allFilePaths()
                    filePaths += paths
                } else {
                    filePaths.append(filePath)
                }
            }
        }
        
        return filePaths
    }
    
    public func aui_theme() ->NSDictionary {
        let themePaths = self.aui_allFilePaths()
        let map: NSMutableDictionary = NSMutableDictionary()
        themePaths.forEach { jsonPath in
            guard let data = try? Data(contentsOf: URL(fileURLWithPath: jsonPath)),
                  let json = try? JSONSerialization.jsonObject(with: data, options: .fragmentsAllowed),
                  let jsonDict = json as? [String: Any] else {
                aui_warn("load json theme fail: \(jsonPath)")
                return
            }
            
            map.addEntries(from: jsonDict)
        }
        
        return map
    }
}
