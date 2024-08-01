//
//  AUIChatEntity.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit

@objcMembers open class AUIChatEntity: NSObject {
    
    public var messageId: String? = ""
            
    public var user: AUIChatUserInfo = AUIChatUserInfo()
    
    public var content: String? = ""
    
    public var joined: Bool? = false
    
    public var attachmentImages: [UIImage]?
    
    public var fullText: String? {
        (self.user.userName) + (self.content ?? "")
    }

    public lazy var height: CGFloat? = UILabel(frame: CGRect(x: 0, y: 0, width: chatViewWidth - 54, height: 15)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(.byWordWrapping).attributedText(self.attributeContent).sizeThatFits(CGSize(width: chatViewWidth - 54, height: 9999)).height + 26

    public lazy var width: CGFloat? = UILabel(frame: CGRect(x: 0, y: 0, width: chatViewWidth - 54, height: 15)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(.byWordWrapping).attributedText(self.attributeContent).sizeThatFits(CGSize(width: chatViewWidth - 54, height: 9999)).width

    public lazy var attributeContent: NSAttributedString? = self.renderAttributeText()
}

@objc public extension AUIChatEntity {
    
    func renderAttributeText() -> NSAttributedString {
        if self.joined! == false {
            
            let imageText = NSMutableAttributedString()
            if let images = self.attachmentImages {
                for image in images {
                    let attachment = NSTextAttachment()
                    attachment.image = image
                    attachment.bounds = CGRect(x: 0, y: -1.5, width: 14, height: 14)
                    imageText.append(NSAttributedString(string: " "))
                    imageText.append(NSMutableAttributedString(attachment: attachment))
                }
            }
            
            var text = NSMutableAttributedString {
                AttributedText(self.user.userName + " : ")
                    .foregroundColor(Color(0x8BB3FF))
                    .font(UIFont.systemFont(ofSize: 14))
                    .lineSpacing(5)
                AttributedText(self.content!)
                    .foregroundColor(self.joined! == false ? Color.white : Color(0xFCF0B3))
                    .font(.systemFont(ofSize: 14, weight: .regular))
                    .lineSpacing(5)
            }
            var string = text.string as NSString
            if self.attachmentImages != nil,self.attachmentImages!.count > 0 {
                imageText.append(text)
                text = imageText
                string = imageText.string as NSString
            }
            for symbol in AUIChatEmojiManager.shared.emojis {
                if string.range(of: symbol).location != NSNotFound {
                    let ranges = text.string.a.rangesOfString(symbol)
                    text = AUIChatEmojiManager.shared.convertEmoji(input: text, ranges: ranges, symbol: symbol)
                }
            }
            return text
        } else {
            let attachment = NSTextAttachment()
            attachment.image = UIImage.aui_Image(named: "shaking_hand")
            attachment.bounds = CGRect(x: 0, y: -4.5, width: 18, height: 18)
            let attributeText = NSMutableAttributedString {
                AttributedText(self.user.userName)
                    .foregroundColor(Color(0x8BB3FF))
                    .font(UIFont.systemFont(ofSize: 14))
                    .lineSpacing(5)
                Space()
                AttributedText(aui_localized("Joined")).foregroundColor(self.joined! == false ? Color.white : Color(0xFCF0B3)).font(.systemFont(ofSize: 14, weight: .semibold)).lineSpacing(5)
                Space()
            }
            attributeText.append(NSMutableAttributedString(attachment: attachment))
            return attributeText
        }
    }
}

