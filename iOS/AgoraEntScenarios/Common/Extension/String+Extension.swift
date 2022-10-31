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
        result.deallocate()

        let hash = NSMutableString()
        for i in 0..<digestLen {
            hash.appendFormat("%02x", result[i])
        }
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

    func timeFormat(secounds: TimeInterval,
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
