//
//  StringExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation


public extension String {
    
    var a: AUIKitSwiftLib<Self> {
        AUIKitSwiftLib.init(self)
    }
    
}

public extension AUIKitSwiftLib where Base == String {
    
    /// Description jsonString convert dic
    /// - Returns: Dictionary
    func jsonToDictionary() -> Dictionary<String,Any> {
        base.data(using: .utf8)?.a.toDictionary() ?? [:]
    }
    
    func jsonToDics() -> [Dictionary<String,Any>] {
        base.data(using: .utf8)?.a.toArray() ?? []
    }
    
    /// Description Localized string in current bundle
    /// - Parameter type: AUIBundleType
    /// - Returns: Localized result
    func localize(type: AUIBundleType) -> Base {
        LanguageManager.localValue(key: base, type: .chat)
    }
    
    
    /// Description Intercept the string of the specified subscript
    /// - Parameter index: from destination index
    /// - Returns: string value
    func subStringFrom(_ index: Int) -> String {
        let temporaryIndex = base.index(base.startIndex, offsetBy: index)
        return String(base[temporaryIndex...])
    }
    
    /// Description Intercept the string of the specified subscript
    /// - Parameter index: to destination index
    /// - Returns: string value
    func subStringTo(_ index: Int) -> String {
        let temporaryString = base
        let temporaryIndex = temporaryString.index(temporaryString.startIndex, offsetBy: index)
        return String(temporaryString[...temporaryIndex])
    }
    ///3,替换某个range的字符串
    func replaceStringWithRange(location: Int, length: Int, newString: String) -> String {
        if location + length > base.count {
            return base
        }
        let start = base.startIndex
        let location_start = base.index(start, offsetBy: location)
        let location_end = base.index(location_start, offsetBy: length)
        let result = base.replacingCharacters(in: location_start..<location_end, with: newString)
        return result
    }
    
    
    /// Description  Get the substring of a range
    /// - Parameters:
    ///   - location: location
    ///   - length: length
    /// - Returns: value
    func subStringWithRange(location: Int, length: Int) -> String {
        if location + length > base.count{
            return base
        }
        let str: String = base
        let start = str.startIndex
        let startIndex = str.index(start, offsetBy: location)
        let endIndex = str.index(startIndex, offsetBy: length)
        return String(str[startIndex..<endIndex])
    }
    
    
    /// Description Get all ranges that meet the conditional substring
    /// - Parameters:
    ///   - searchString: search text
    ///   - inString: full text
    /// - Returns: Ranges
    func rangesOfString(_ searchString: String) -> [NSRange] {
        var results = [NSRange]()
        let fullText = base as NSString
        if searchString.count > 0 && fullText.length > 0 {
            var searchRange = NSMakeRange(0, fullText.length)
            var range = fullText.range(of: searchString, options: [], range: searchRange)
            while (range.location != NSNotFound) {
                results.append(range)
                searchRange = NSMakeRange(NSMaxRange(range), fullText.length - NSMaxRange(range))
                range = fullText.range(of: searchString, options: [], range: searchRange)
            }
            
        }
        return results
    }
    
    func sizeWithText(font: UIFont, size: CGSize) -> CGSize {
        let attributes = [NSAttributedString.Key.font: font]
        let option = NSStringDrawingOptions.usesLineFragmentOrigin
        let rect:CGRect = base.boundingRect(with: size, options: option, attributes: attributes, context: nil)
        return rect.size;
    }
}
