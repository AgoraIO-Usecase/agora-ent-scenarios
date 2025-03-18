//
//  AUILabelSegment.swift
//  AUISegmented
//
//  Created by George Marmaridis on 08/10/2017.
//

#if canImport(UIKit)

import UIKit
import SwiftTheme

open class AUILabelSegment: AUISegmentedSegment {
    // MARK: Constants
    private struct DefaultValues {
        static let normalBackgroundColor: ThemeColorPicker = "CommonColor.clear"
        static let normalTextColor: ThemeColorPicker = "CommonColor.normalTextColor"
        static let normalFont: ThemeFontPicker = "CommonFont.middle"
        static let selectedBackgroundColor: ThemeColorPicker = "CommonColor.primary"
        static let selectedTextColor: ThemeColorPicker = "CommonColor.normalTextColor"
        static let selectedFont: ThemeFontPicker = "CommonFont.middle"
    }
    
    // MARK: Properties
    public let text: String?
    
    public let normalFont: ThemeFontPicker
    public let normalTextColor: ThemeColorPicker
    public let normalBackgroundColor: ThemeColorPicker
    
    public let selectedFont: ThemeFontPicker
    public let selectedTextColor: ThemeColorPicker
    public let selectedBackgroundColor: ThemeColorPicker
    
    private let numberOfLines: Int
    private let accessibilityIdentifier: String?
    
    // MARK: Lifecycle
    public init(text: String? = nil,
                numberOfLines: Int = 1,
                normalBackgroundColor: ThemeColorPicker? = nil,
                normalFont: ThemeFontPicker? = nil,
                normalTextColor: ThemeColorPicker? = nil,
                selectedBackgroundColor: ThemeColorPicker? = nil,
                selectedFont: ThemeFontPicker? = nil,
                selectedTextColor: ThemeColorPicker? = nil,
                accessibilityIdentifier: String? = nil) {
        self.text = text
        self.numberOfLines = numberOfLines
        self.normalBackgroundColor = normalBackgroundColor ?? DefaultValues.normalBackgroundColor
        self.normalFont = normalFont ?? DefaultValues.normalFont
        self.normalTextColor = normalTextColor ?? DefaultValues.normalTextColor
        self.selectedBackgroundColor = selectedBackgroundColor ?? DefaultValues.selectedBackgroundColor
        self.selectedFont = selectedFont ?? DefaultValues.selectedFont
        self.selectedTextColor = selectedTextColor ?? DefaultValues.selectedTextColor
        self.accessibilityIdentifier = accessibilityIdentifier
    }
    
    // MARK: BetterSegmentedControlSegment
    public var intrinsicContentSize: CGSize? {
        selectedView.intrinsicContentSize
    }
    
    public lazy var normalView: UIView = {
        createLabel(withText: text,
                    backgroundColor: normalBackgroundColor,
                    font: normalFont,
                    textColor: normalTextColor,
                    accessibilityIdentifier: accessibilityIdentifier)
    }()
    public lazy var selectedView: UIView = {
        createLabel(withText: text,
                    backgroundColor: selectedBackgroundColor,
                    font: selectedFont,
                    textColor: selectedTextColor,
                    accessibilityIdentifier: accessibilityIdentifier)
    }()
    open func createLabel(withText text: String?,
                          backgroundColor: ThemeColorPicker,
                          font: ThemeFontPicker,
                          textColor: ThemeColorPicker,
                          accessibilityIdentifier: String?) -> UILabel {
        let label = UILabel()
        label.text = text
        label.numberOfLines = numberOfLines
        label.theme_backgroundColor = backgroundColor
        label.theme_font = font
        label.theme_textColor = textColor
        label.lineBreakMode = .byTruncatingTail
        label.textAlignment = .center
        label.accessibilityIdentifier = accessibilityIdentifier
        return label
    }
}

public extension AUILabelSegment {
    class func segments(withTitles titles: [String],
                        numberOfLines: Int = 1,
                        normalBackgroundColor: ThemeColorPicker? = nil,
                        normalFont: ThemeFontPicker? = nil,
                        normalTextColor: ThemeColorPicker? = nil,
                        selectedBackgroundColor: ThemeColorPicker? = nil,
                        selectedFont: ThemeFontPicker? = nil,
                        selectedTextColor: ThemeColorPicker? = nil) -> [AUISegmentedSegment] {
        titles.map {
            AUILabelSegment(text: $0,
                         numberOfLines: numberOfLines,
                         normalBackgroundColor: normalBackgroundColor,
                         normalFont: normalFont,
                         normalTextColor: normalTextColor,
                         selectedBackgroundColor: selectedBackgroundColor,
                         selectedFont: selectedFont,
                         selectedTextColor: selectedTextColor)
        }
    }
}

#endif
