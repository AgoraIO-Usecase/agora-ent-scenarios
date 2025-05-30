//
//  AUIActionSheetCell.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/28.
//

import UIKit
import SDWebImage
import SwiftTheme

@objc public enum AUIActionSheetItemLayoutType: Int {
    case horizontal = 0
    case vertical
}

open class AUIActionSheetItem: NSObject {
    public var title: String = ""
    public var isSelected: (()->(Bool))?
    public var callback: (()->())?
}

open class AUIActionSheetStyleItem: AUIActionSheetItem {
    public var icon: UIImage?
    public var backgroundIcon: UIImage?
    public var titleColor: UIColor = .aui_normalTextColor
    public var imageWidth: CGFloat = 32
    public var imageHeight: CGFloat = 32
    public var backgroundImageWidth: CGFloat = 56
    public var backgroundImageHeight: CGFloat = 56
    public var padding: CGFloat = 4
    public var selectedBorderColor: UIColor = .aui_primary
    public var selectedBorderWidth: CGFloat = 2
    public var selectedBorderRadius: CGFloat = 28
    
    class func horizontal() -> AUIActionSheetStyleItem {
        return AUIActionSheetStyleItem()
    }
    
    class func vertical() -> AUIActionSheetStyleItem {
        let item = AUIActionSheetStyleItem()
        item.imageWidth = 24
        item.imageHeight = 24
        item.backgroundImageWidth = 24
        item.backgroundImageHeight = 24
        return item
    }
}

open class AUIActionSheetCell: UICollectionViewCell {
    @objc var itemPadding: CGFloat = 5 {
        didSet {
            setNeedsLayout()
        }
    }
    var itemType: AUIActionSheetItemLayoutType = .vertical {
        didSet {
            resetStyle()
            resetTheme()
            setNeedsLayout()
        }
    }
    var item: AUIActionSheetItem? {
        didSet {
            titleLabel.text = item?.title
            imageView.isHidden = imageView.image == nil ? true : false
            backgroundImageView.isHidden = (backgroundImageView.image == nil && imageView.image == nil) ? true : false
            resetStyle()
            resetTheme()
            setNeedsLayout()
        }
    }
    private lazy var backgroundImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        return label
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubView()
    }
    
    private func _loadSubView() {
        addSubview(backgroundImageView)
        backgroundImageView.addSubview(imageView)
        addSubview(titleLabel)
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        switch itemType {
        case .vertical:
            if backgroundImageView.isHidden {
                titleLabel.frame = self.bounds
            } else {
                titleLabel.sizeToFit()
                let width = titleLabel.aui_width + backgroundImageView.aui_width + itemPadding
                backgroundImageView.aui_center = CGPoint(x: (aui_width - width) / 2 + backgroundImageView.aui_width / 2, y: aui_height / 2)
                titleLabel.aui_center = CGPoint(x: backgroundImageView.aui_right + itemPadding + titleLabel.aui_width / 2, y: backgroundImageView.aui_centerY)
            }
            break
        case .horizontal:
            if backgroundImageView.isHidden {
                titleLabel.frame = self.bounds
            } else {
                titleLabel.sizeToFit()
                let height = titleLabel.aui_height + backgroundImageView.aui_height + itemPadding
                backgroundImageView.aui_center = CGPoint(x: aui_width / 2, y: (aui_height - height) / 2 + backgroundImageView.aui_height / 2)
                titleLabel.aui_center = CGPoint(x: backgroundImageView.aui_centerX, y: backgroundImageView.aui_bottom + itemPadding + titleLabel.aui_height / 2)
            }
            break
        }
        
        imageView.aui_center = CGPoint(x: backgroundImageView.aui_width / 2, y: backgroundImageView.aui_height / 2)
    }
    
    private func resetStyle() {
        guard let style = item as? AUIActionSheetStyleItem else {return}
        self.itemPadding = style.padding
        titleLabel.textColor = style.titleColor
        imageView.image = style.icon
        backgroundImageView.image = style.backgroundIcon
        imageView.isHidden = imageView.image == nil ? true : false
        backgroundImageView.isHidden = (backgroundImageView.image == nil && imageView.image == nil) ? true : false
        
        if itemType == .horizontal {
            titleLabel.font = .aui_middle
            imageView.aui_width = 32
            imageView.aui_height = 32
            backgroundImageView.aui_width = 56
            backgroundImageView.aui_height = 56
        } else {
            titleLabel.font = .aui_big
            imageView.aui_width = 24
            imageView.aui_height = 24
            backgroundImageView.aui_width = 24
            backgroundImageView.aui_height = 24
        }
        
        if item?.isSelected?() ?? false {
            backgroundImageView.layer.borderWidth = style.selectedBorderWidth
            backgroundImageView.layer.borderColor = style.selectedBorderColor.cgColor
            backgroundImageView.layer.cornerRadius = style.selectedBorderRadius
        } else {
            backgroundImageView.layer.borderWidth = 0
            backgroundImageView.layer.borderColor = UIColor.clear.cgColor
        }
    }
}

//MARK: Theme
open class AUIActionSheetThemeItem: AUIActionSheetItem {
    public var icon: ThemeImagePicker?
    public var backgroundIcon: ThemeImagePicker?
    public var titleColor: ThemeColorPicker = "ActionSheetCell.titleColor"
    public var imageWidth: ThemeCGFloatPicker = "ActionSheetCell.horizontalImageWidth"
    public var imageHeight: ThemeCGFloatPicker = "ActionSheetCell.horizontalImageHeight"
    public var backgroundImageWidth: ThemeCGFloatPicker = "ActionSheetCell.horizontalBackgroundImageWidth"
    public var backgroundImageHeight: ThemeCGFloatPicker = "ActionSheetCell.horizontalBackgroundImageHeight"
    public var padding: ThemeCGFloatPicker = "ActionSheetCell.itemPadding"
    
    public var selectedBorderColor: ThemeCGColorPicker = "CommonColor.primary"
    public var selectedBorderWidth: ThemeCGFloatPicker = "ActionSheetCell.selectedBorderWidth"
    public var selectedBorderRadius: ThemeCGFloatPicker = "ActionSheetCell.selectedBorderRadius"
    
    public class func horizontal() -> AUIActionSheetThemeItem {
        return AUIActionSheetThemeItem()
    }
    
    public class func vertical() -> AUIActionSheetThemeItem {
        let item = AUIActionSheetThemeItem()
        item.imageWidth = "ActionSheetCell.verticalImageWidth"
        item.imageHeight = "ActionSheetCell.verticalImageHeight"
        item.backgroundImageWidth = "ActionSheetCell.verticalBackgroundImageWidth"
        item.backgroundImageHeight = "ActionSheetCell.verticalBackgroundImageHeight"
        return item
    }
}

extension AUIActionSheetCell {
    func resetTheme() {
        guard let theme = item as? AUIActionSheetThemeItem else {return}
        self.theme_padding = theme.padding
        titleLabel.theme_textColor = theme.titleColor
        imageView.theme_image = theme.icon
        backgroundImageView.theme_image = theme.backgroundIcon
        imageView.isHidden = imageView.image == nil ? true : false
        backgroundImageView.isHidden = (backgroundImageView.image == nil && imageView.image == nil) ? true : false
        
        if itemType == .horizontal {
            titleLabel.theme_font = "CommonFont.middle"
            imageView.theme_width = "ActionSheetCell.horizontalImageWidth"
            imageView.theme_height = "ActionSheetCell.horizontalImageHeight"
            backgroundImageView.theme_width = "ActionSheetCell.horizontalBackgroundImageWidth"
            backgroundImageView.theme_height = "ActionSheetCell.horizontalBackgroundImageHeight"
        } else {
            titleLabel.theme_font = "CommonFont.big"
            imageView.theme_width = "ActionSheetCell.verticalImageWidth"
            imageView.theme_height = "ActionSheetCell.verticalImageHeight"
            backgroundImageView.theme_width = "ActionSheetCell.verticalBackgroundImageWidth"
            backgroundImageView.theme_height = "ActionSheetCell.verticalBackgroundImageHeight"
        }
        
        if item?.isSelected?() ?? false {
            backgroundImageView.layer.theme_borderWidth = theme.selectedBorderWidth
            backgroundImageView.layer.theme_borderColor = theme.selectedBorderColor
            backgroundImageView.layer.theme_cornerRadius = theme.selectedBorderRadius
        } else {
            backgroundImageView.layer.theme_borderWidth = nil
            backgroundImageView.layer.theme_borderColor = nil
            backgroundImageView.layer.theme_cornerRadius = nil
            backgroundImageView.layer.borderWidth = 0
            backgroundImageView.layer.borderColor = UIColor.clear.cgColor
            backgroundImageView.layer.cornerRadius = 0
        }
    }
    
    var theme_padding: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setItemPadding:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setItemPadding:", newValue) }
    }
}
