//
//  FontTheme.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//
import Foundation
import UIKit


public extension UIFont {
    
    @objc static let theme: FontTheme = FontTheme()
        
}



@objcMembers public class FontTheme: NSObject {
    
    public var headlineLarge = UIFont.systemFont(ofSize: 20, weight: .semibold)
    
    public var headlineMedium = UIFont.systemFont(ofSize: 18, weight: .semibold)
    
    public var headlineSmall = UIFont.systemFont(ofSize: 16, weight: .semibold)
    
    public var headlineExtraSmall = UIFont.systemFont(ofSize: 14, weight: .semibold)
    
    public var titleLarge = UIFont.systemFont(ofSize: 18, weight: .medium)
    
    public var titleMedium = UIFont.systemFont(ofSize: 16, weight: .medium)
    
    public var titleSmall = UIFont.systemFont(ofSize: 14, weight: .medium)
    
    public var labelLarge = UIFont.systemFont(ofSize: 16, weight: .medium)
    
    public var labelMedium = UIFont.systemFont(ofSize: 14, weight: .medium)
    
    public var labelSmall = UIFont.systemFont(ofSize: 12, weight: .medium)
    
    public var labelExtraSmall = UIFont.systemFont(ofSize: 11, weight: .medium)
    
    public var bodyLarge = UIFont.systemFont(ofSize: 16, weight: .regular)
    
    public var bodyMedium = UIFont.systemFont(ofSize: 14, weight: .regular)
    
    public var bodySmall = UIFont.systemFont(ofSize: 12, weight: .regular)
    
    public var bodyExtraSmall = UIFont.systemFont(ofSize: 11, weight: .regular)
    
    public var giftNumberFont = UIFont(name: "HelveticaNeue-BoldItalic", size: 18) ?? UIFont.systemFont(ofSize: 18, weight: .bold)

}
    
