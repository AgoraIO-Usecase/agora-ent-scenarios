//
//  ThemeManager.swift
//  AUIKit
//
//  Created by FanPengpeng on 2023/7/20.
//

import UIKit
import SwiftTheme

public class AUIThemeManager: NSObject {
    
        public static let shared: AUIThemeManager = AUIThemeManager()
        
        public var themeNames = ["Light", "Dark"]

        public private(set) var currentThemeName: String?
        
        private override init() {
            super.init()
            switchTheme(themeName: "Light")
        }
        
        
        public private(set) var themeIdx = 0
        
        
        public private(set) var themeResourcePaths: Set<URL> = Set()
    
        private var themeFolderPaths: Set<URL> = Set()
        
        public func addThemeFolderPath(path: URL) {
            themeFolderPaths.insert(path)
            guard let themeName = currentThemeName else {return}
            switchTheme(themeName: themeName)
        }
        
        public func resetTheme() {
            CATransaction.begin()
            CATransaction.setDisableActions(true)
            switchTheme(themeName: themeNames[themeIdx])
            CATransaction.commit()
        }
        
        public func switchThemeToNext() {
            themeIdx = (themeIdx + 1) % themeNames.count
            resetTheme()
        }
        
        public func switchTheme(themeName: String) {
            guard let folderPath = Bundle.main.path(forResource: "auiTheme", ofType: "bundle") else {return}
            
            aui_info("switchTheme: \(themeName)", tag: "AUIKaraokeRoomView")
            let themeFolderPath = "\(folderPath)/\(themeName)/theme"
            
            let jsonDict = themeFolderPath.aui_theme()
            guard jsonDict.count > 0 else {
                aui_error("SwiftTheme WARNING: Can't read json '\(themeName)' at: \(themeFolderPath)", tag: "AUIKaraokeRoomView")
                return
            }
            
            currentThemeName = themeName
            
            themeResourcePaths.removeAll()
            themeFolderPaths.forEach { path in
                let themeFolderPath = "\(path.relativePath)/\(themeName)/theme"
                let resourceFolderPath = "\(path.relativePath)/\(themeName)/resource/"
                themeResourcePaths.insert(URL(fileURLWithPath: resourceFolderPath))
                let dic = themeFolderPath.aui_theme()
                dic.forEach { (key: Any, value: Any) in
                    guard let key = key as? String else {return}
                    jsonDict.setValue(value, forKey: key)
                }
            }
            
            let path = "\(folderPath)/\(themeName)/resource/"
            ThemeManager.setTheme(dict: jsonDict, path: .sandbox(URL(fileURLWithPath: path)))
        }
}
