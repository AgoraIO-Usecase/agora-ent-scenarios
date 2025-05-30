//
//  NSAttributesStringFunctionBuilder.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation


#if canImport(UIKit)
import UIKit
public typealias Font = UIFont
public typealias Color = UIColor
public typealias Size = CGSize
#elseif canImport(AppKit)
import AppKit
public typealias Font = NSFont
public typealias Color = NSColor
public typealias Size = NSSize
#endif



//typealias AttributedText = NSAttributedString.AttributedText

//extension NSAttributedString {
     struct AttributedText: Component {
        let string: String
        let attributes: Attributes

        init(_ string: String, attributes: Attributes = [:]) {
            self.string = string
            self.attributes = attributes
        }
    }
//}

typealias Attributes = [NSAttributedString.Key: Any]

@resultBuilder
struct NSAttributedStringFunctionBuilder {
    public static func buildBlock(_ components: Component...) -> NSAttributedString {
        let text = NSMutableAttributedString(string: "")
        for component in components {
            text.append(component.attributedString)
        }
        return text
    }
}

extension NSAttributedString {
    convenience init(@NSAttributedStringFunctionBuilder _ builder: () -> NSAttributedString) {
        self.init(attributedString: builder())
    }
}

protocol Component {
    var string: String { get }
    var attributes: Attributes { get }
    var attributedString: NSAttributedString { get }
}

enum Ligature: Int {
    case none = 0
    case `default` = 1

    #if canImport(AppKit)
    case all = 2 // Value 2 is unsupported on iOS
    #endif
}

extension Component {
    private func build(_ string: String, attributes: Attributes) -> Component {
        return AttributedText(string, attributes: attributes)
    }

    var attributedString: NSAttributedString {
        NSAttributedString(string: string, attributes: attributes)
    }

    func attribute(_ newAttribute: NSAttributedString.Key, value: Any) -> Component {
        attributes([newAttribute: value])
    }

    func attributes(_ newAttributes: Attributes) -> Component {
        var attributes = self.attributes
        for attribute in newAttributes {
            attributes[attribute.key] = attribute.value
        }
        return build(string, attributes: attributes)
    }
}

// MARK: Basic Modifiers
extension Component {
    func backgroundColor(_ color: Color) -> Component {
        attributes([.backgroundColor: color])
    }

    func baselineOffset(_ baselineOffset: CGFloat) -> Component {
        attributes([.baselineOffset: baselineOffset])
    }

    func font(_ font: Font) -> Component {
        attributes([.font: font])
    }

    func foregroundColor(_ color: Color) -> Component {
        attributes([.foregroundColor: color])
    }

    func expansion(_ expansion: CGFloat) -> Component {
        attributes([.expansion: expansion])
    }

    func kerning(_ kern: CGFloat) -> Component {
        attributes([.kern: kern])
    }

    func ligature(_ ligature: Ligature) -> Component {
        attributes([.ligature: ligature.rawValue])
    }

    func obliqueness(_ obliqueness: Float) -> Component {
        attributes([.obliqueness: obliqueness])
    }

    func shadow(color: Color? = nil, radius: CGFloat, x: CGFloat, y: CGFloat) -> Component {
        let shadow = NSShadow()
        shadow.shadowColor = color
        shadow.shadowBlurRadius = radius
        shadow.shadowOffset = .init(width: x, height: y)
        return attributes([.shadow: shadow])
    }

    func strikethrough(style: NSUnderlineStyle, color: Color? = nil) -> Component {
        if let color = color {
            return attributes([.strikethroughStyle: style.rawValue,
                               .strikethroughColor: color])
        }
        return attributes([.strikethroughStyle: style.rawValue])
    }

    func stroke(width: CGFloat, color: Color? = nil) -> Component {
        if let color = color {
            return attributes([.strokeWidth: width,
                               .strokeColor: color])
        }
        return attributes([.strokeWidth: width])
    }

    func textEffect(_ textEffect: NSAttributedString.TextEffectStyle) -> Component {
        return attributes([.textEffect: textEffect])
    }

    func underline(_ style: NSUnderlineStyle, color: Color? = nil) -> Component {
        if let color = color {
            return attributes([.underlineStyle: style.rawValue,
                               .underlineColor: color])
        }
        return attributes([.underlineStyle: style.rawValue])
    }

    func writingDirection(_ writingDirection: NSWritingDirection) -> Component {
        return attributes([.writingDirection: writingDirection.rawValue])
    }

    #if canImport(AppKit)
    public func vertical() -> Component {
        return attributes([.verticalGlyphForm: 1])
    }
    #endif
}

// MARK: - Paragraph Style Modifiers
extension Component {
    func paragraphStyle(_ paragraphStyle: NSParagraphStyle) -> Component {
        return attributes([.paragraphStyle: paragraphStyle])
    }

    func paragraphStyle(_ paragraphStyle: NSMutableParagraphStyle) -> Component {
        return attributes([.paragraphStyle: paragraphStyle])
    }

    private func getMutableParagraphStyle() -> NSMutableParagraphStyle {
        if let mps = attributes[.paragraphStyle] as? NSMutableParagraphStyle {
            return mps
        } else if let ps = attributes[.paragraphStyle] as? NSParagraphStyle,
            let mps = ps.mutableCopy() as? NSMutableParagraphStyle {
            return mps
        }
        return NSMutableParagraphStyle()
    }

    func alignment(_ alignment: NSTextAlignment) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.alignment = alignment
        return self.paragraphStyle(paragraphStyle)
    }

    func firstLineHeadIndent(_ indent: CGFloat) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.firstLineHeadIndent = indent
        return self.paragraphStyle(paragraphStyle)
    }

    func headIndent(_ indent: CGFloat) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.headIndent = indent
        return self.paragraphStyle(paragraphStyle)
    }

    func tailIndent(_ indent: CGFloat) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.tailIndent = indent
        return self.paragraphStyle(paragraphStyle)
    }

    func lineBreakeMode(_ lineBreakMode: NSLineBreakMode) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.lineBreakMode = lineBreakMode
        return self.paragraphStyle(paragraphStyle)
    }

    func lineHeight(multiple: CGFloat = 0, maximum: CGFloat = 0, minimum: CGFloat) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = multiple
        paragraphStyle.maximumLineHeight = maximum
        paragraphStyle.minimumLineHeight = minimum
        return self.paragraphStyle(paragraphStyle)
    }

    func lineSpacing(_ spacing: CGFloat) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.lineSpacing = spacing
        return self.paragraphStyle(paragraphStyle)
    }

    func paragraphSpacing(_ spacing: CGFloat, before: CGFloat = 0) -> Component  {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.paragraphSpacing = spacing
        paragraphStyle.paragraphSpacingBefore = before
        return self.paragraphStyle(paragraphStyle)
    }

    func baseWritingDirection(_ baseWritingDirection: NSWritingDirection) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.baseWritingDirection = baseWritingDirection
        return self.paragraphStyle(paragraphStyle)
    }

    func hyphenationFactor(_ hyphenationFactor: Float) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.hyphenationFactor = hyphenationFactor
        return self.paragraphStyle(paragraphStyle)
    }

    @available(iOS 9.0, tvOS 9.0, watchOS 2.0, OSX 10.11, *)
    func allowsDefaultTighteningForTruncation() -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.allowsDefaultTighteningForTruncation = true
        return self.paragraphStyle(paragraphStyle)
    }

    func tabsStops(_ tabStops: [NSTextTab], defaultInterval: CGFloat = 0) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.tabStops = tabStops
        paragraphStyle.defaultTabInterval = defaultInterval
        return self.paragraphStyle(paragraphStyle)
    }

    #if canImport(AppKit) && !targetEnvironment(macCatalyst)
    public func textBlocks(_ textBlocks: [NSTextBlock]) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.textBlocks = textBlocks
        return self.paragraphStyle(paragraphStyle)
    }

    public func textLists(_ textLists: [NSTextList]) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.textLists = textLists
        return self.paragraphStyle(paragraphStyle)
    }

    public func tighteningFactorForTruncation(_ tighteningFactorForTruncation: Float) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.tighteningFactorForTruncation = tighteningFactorForTruncation
        return self.paragraphStyle(paragraphStyle)
    }

    public func headerLevel(_ headerLevel: Int) -> Component {
        let paragraphStyle = getMutableParagraphStyle()
        paragraphStyle.headerLevel = headerLevel
        return self.paragraphStyle(paragraphStyle)
    }
    #endif
}

typealias ImageAttachment = NSAttributedString.ImageAattachment

extension NSAttributedString {
    struct ImageAattachment: Component {
        let string: String = ""
        let attributes: Attributes = [:]
        private let attachment: NSTextAttachment

        init(_ image: UIImage, size: Size? = nil) {
            let attachment = NSTextAttachment()
            attachment.image = image

            if let size = size {
                attachment.bounds.size = size
            }

            self.attachment = attachment
        }

        var attributedString: NSAttributedString {
            return NSAttributedString(attachment: attachment)
        }
    }
}

//typealias Link = NSAttributedString.Link
//
//extension NSAttributedString {
    struct Link: Component {
        let string: String
        let url: URL
        let attributes: Attributes

        init(_ string: String, url: URL, attributes: Attributes = [:]) {
            self.string = string
            self.url = url

            var attributes = attributes
            attributes[.link] = url
            self.attributes = attributes
        }

        var attributedString: NSAttributedString {
            NSAttributedString(string: string, attributes: attributes)
        }
    }
//}

//typealias Empty = NSAttributedString.Empty
//typealias Space = NSAttributedString.Space
//typealias LineBreak = NSAttributedString.LineBreak

//extension NSAttributedString {
    struct Empty: Component {
        let string: String = ""
        let attributes: Attributes = [:]
        init() { }
    }

    struct Space: Component {
        let string: String = " "
        let attributes: Attributes = [:]
        init() { }
    }

    struct LineBreak: Component {
        let string: String = "\n"
        let attributes: Attributes = [:]
        init() { }
    }
//}
