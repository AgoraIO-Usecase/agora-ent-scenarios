//
//  String+Extension.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import CommonCrypto
import UIKit

public extension String {
    var md5Encrypt: String {
        guard count > 0 else {
            print("⚠️⚠️⚠️md5加密无效的字符串⚠️⚠️⚠️")
            return ""
        }
        let str = cString(using: String.Encoding.utf8)
        let strLen = CUnsignedInt(lengthOfBytes(using: String.Encoding.utf8))
        let digestLen = Int(CC_MD5_DIGEST_LENGTH)
        let result = UnsafeMutablePointer<CUnsignedChar>.allocate(capacity: digestLen)
//        CC_SHA256(str!, strLen, result)
        CC_MD5(str!, strLen, result)

        let hash = NSMutableString()
        for i in 0..<digestLen {
            hash.appendFormat("%02x", result[i])
        }
        result.deallocate()
        let value = hash as String
        return value // [1, 16]
    }

    subscript(index: Int, length: Int) -> String {
        let startIndex = self.index(startIndex, offsetBy: index)
        let endIndex = self.index(startIndex, offsetBy: length)
        return String(self[startIndex..<endIndex])
    }
}

extension String {
    func size(font: UIFont, drawRange size: CGSize) -> CGSize {
        let attributes = [NSAttributedString.Key.font: font]
        let option = NSStringDrawingOptions.usesLineFragmentOrigin
        let rect = (self as NSString).boundingRect(with: size,
                                                   options: option,
                                                   attributes: attributes,
                                                   context: nil)
        return rect.size
    }
}

extension String {
    var timeStamp: String {
        let date = Date()
        let timeInterval = date.timeIntervalSince1970
        let millisecond = CLongLong(timeInterval * 1000)
        return "\(millisecond)"
    }

    var timeStamp16: String {
        let date = Date()
        let timeInterval = date.timeIntervalSince1970
        let millisecond = CLongLong(timeInterval * 1000000)
        return "\(millisecond)"
    }

    func isChinese(str: String) -> Bool {
        let match = "(^[\\u4e00-\\u9fa5]+$)"
        let predicate = NSPredicate(format: "SELF matches %@", match)
        return predicate.evaluate(with: str)
    }

    public func timeFormat(secounds: TimeInterval,
                    h: String = ":",
                    m: String = ":",
                    s: String = "",
                    isShowHour: Bool = false) -> String
    {
        guard !secounds.isNaN else { return "00\(m)00" }
        var minTime = Int(secounds / 60)
        let second = Int(secounds.truncatingRemainder(dividingBy: 60))
        var hour = 0
        if isShowHour || minTime >= 60 {
            hour = Int(minTime / 60)
            minTime -= hour * 60
            return String(format: "%02d%@%02d%@%02d%@", hour, h, minTime, m, second, s)
        }
        return String(format: "%02d%@%02d%@", minTime, m, second, s)
    }
}

public extension NSAttributedString {
    func toString() -> String {
        let result = NSMutableAttributedString(attributedString: self)
        var replaceList: [(NSRange, String)] = []
        result.enumerateAttribute(.accessibilityTextCustom, in: NSRange(location: 0, length: result.length), using: { value, range, _ in
            if let value = value as? String {
                for i in range.location..<range.location + range.length {
                    replaceList.append((NSRange(location: i, length: 1), value))
                }
            }
        })
        for i in replaceList.reversed() {
            result.replaceCharacters(in: i.0, with: i.1)
        }
        return result.string
    }
}

extension String {
    // 获取时间格式
    func timeIntervalToMMSSFormat(interval: TimeInterval) -> String {
        if interval >= 3600 {
            let hour = interval / 3600
            let min = interval.truncatingRemainder(dividingBy: 3600) / 60
            let sec = interval.truncatingRemainder(dividingBy: 3600).truncatingRemainder(dividingBy: 60)
            return String(format: "%02d:%02d:%02d", Int(hour), Int(min), Int(sec))
        } else {
            let min = interval / 60
            let sec = interval.truncatingRemainder(dividingBy: 60)
            return String(format: "%02d:%02d", Int(min), Int(sec))
        }
    }

    // 字符串截取
    func textSubstring(startIndex: Int, length: Int) -> String {
        let startIndex = index(self.startIndex, offsetBy: startIndex)
        let endIndex = index(startIndex, offsetBy: length)
        let subvalues = self[startIndex ..< endIndex]
        return String(subvalues)
    }

    /**
     *  临时文件路径
     */
    public static func tempFilePath() -> String {
        return NSHomeDirectory().appending("/tmp").appending("/MusicTemp.mp3")
    }

    /**
     *  缓存文件夹路径
     */
    public static func cacheFolderPath() -> String {
        return NSHomeDirectory().appending("/Library").appending("/MusicCaches")
    }

    /**
     *  获取网址中的文件名
     */
    public var fileName: String {
        components(separatedBy: "/").last ?? ""
    }
}

extension String {
    // 判断字符串中的字符类型
    public enum CharacterType {
        case chinese
        case english
    }
    // 计算指定类型的字符数
    public func countOfCharacters(for type: CharacterType) -> Int {
        var count = 0
        for scalar in unicodeScalars {
            switch type {
            case .chinese:
                // 判断字符是否为中文字符（Unicode 编码范围为 4E00 到 9FFF）
                if scalar.value >= 0x4E00 && scalar.value <= 0x9FFF {
                    count += 1
                }

            case .english:
                // 判断字符是否为英文字符（Unicode 编码范围为 0000 到 007F）
                if scalar.value <= 0x007F {
                    count += 1
                }
            }
        }
        return count
    }
}
