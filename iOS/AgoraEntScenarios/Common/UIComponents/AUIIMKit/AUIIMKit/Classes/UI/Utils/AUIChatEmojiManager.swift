//
//  AUIChatEmojiManager.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation
fileprivate let manager = AUIChatEmojiManager()
/*!
 *  \~Chinese
 *  表情资源管理类
 *
 *  \~English
 *  Emoticon resource management class
 *
 */
@objc open class AUIChatEmojiManager: NSObject {
    
    @objc static let shared = manager

    @objc var emojiMap: Dictionary<String,UIImage> = [:]

    @objc var emojis: [String] = ["U+1F600", "U+1F604", "U+1F609", "U+1F62E", "U+1F92A", "U+1F60E", "U+1F971", "U+1F974", "U+263A", "U+1F641", "U+1F62D", "U+1F610", "U+1F607", "U+1F62C", "U+1F913", "U+1F633", "U+1F973", "U+1F620", "U+1F644", "U+1F910", "U+1F97A", "U+1F928", "U+1F62B", "U+1F637", "U+1F912", "U+1F631", "U+1F618", "U+1F60D", "U+1F922", "U+1F47F", "U+1F92C", "U+1F621", "U+1F44D", "U+1F44E", "U+1F44F", "U+1F64C", "U+1F91D", "U+1F64F", "U+2764", "U+1F494", "U+1F495", "U+1F4A9", "U+1F48B", "U+2600", "U+1F31C", "U+1F308", "U+2B50", "U+1F31F", "U+1F389", "U+1F490"]
    
    /// Description Replace emoji resource
    /// - Parameters:
    ///   - map: Emoji map.Key is String type.Use as follows keys "
    ///   "U+1F600", "U+1F604", "U+1F609", "U+1F62E", "U+1F92A", "U+1F60E", "U+1F971", "U+1F974", "U+263A", "U+1F641", "U+1F62D", "U+1F610", "U+1F607", "U+1F62C", "U+1F913", "U+1F633", "U+1F973", "U+1F620", "U+1F644", "U+1F910", "U+1F97A", "U+1F928", "U+1F62B", "U+1F637", "U+1F912", "U+1F631", "U+1F618", "U+1F60D", "U+1F922", "U+1F47F", "U+1F92C", "U+1F621", "U+1F44D", "U+1F44E", "U+1F44F", "U+1F64C", "U+1F91D", "U+1F64F", "U+2764", "U+1F494", "U+1F495", "U+1F4A9", "U+1F48B", "U+2600", "U+1F31C", "U+1F308", "U+2B50", "U+1F31F", "U+1F389", "U+1F490"
    ///   "".Value is UIImage instance.
    ///   - emojis: Sort as follows order,["U+1F600", "U+1F604", "U+1F609", "U+1F62E", "U+1F92A", "U+1F60E", "U+1F971", "U+1F974", "U+263A", "U+1F641", "U+1F62D", "U+1F610", "U+1F607", "U+1F62C", "U+1F913", "U+1F633", "U+1F973", "U+1F620", "U+1F644", "U+1F910", "U+1F97A", "U+1F928", "U+1F62B", "U+1F637", "U+1F912", "U+1F631", "U+1F618", "U+1F60D", "U+1F922", "U+1F47F", "U+1F92C", "U+1F621", "U+1F44D", "U+1F44E", "U+1F44F", "U+1F64C", "U+1F91D", "U+1F64F", "U+2764", "U+1F494", "U+1F495", "U+1F4A9", "U+1F48B", "U+2600", "U+1F31C", "U+1F308", "U+2B50", "U+1F31F", "U+1F389", "U+1F490"]
    @objc func configEmojisMap(map: [String: UIImage], emojis: [String]) {
        self.emojiMap = map
        self.emojis = emojis
    }

    @objc func convertEmoji(input: NSMutableAttributedString, ranges: [NSRange], symbol: String) -> NSMutableAttributedString {
        let text = NSMutableAttributedString(attributedString: input)
        for range in ranges.reversed() {
            if range.location != NSNotFound, range.length != NSNotFound {
                let value = self.emojiMap.isEmpty ? UIImage.aui_Image(named: symbol):self.emojiMap[symbol]
                let attachment = NSTextAttachment()
                attachment.image = value
                attachment.bounds = CGRect(x: 0, y: -1.5, width: 14, height: 14)
                text.replaceCharacters(in: range, with: NSAttributedString(attachment: attachment))
            }
        }
        return text
    }
}
