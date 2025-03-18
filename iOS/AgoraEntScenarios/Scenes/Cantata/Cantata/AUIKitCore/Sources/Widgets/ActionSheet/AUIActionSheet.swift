//
//  AUIActionSheet.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/27.
//

import UIKit
import SwiftTheme

private let kEdgeSpace:CGFloat = 16
private let kAvatarViewWidth:CGFloat = 40
private let kUserTitleLeftPadding: CGFloat = 8

private let kAUIActionSheetCellId = "AUIActionSheetCellId"

open class AUIActionSheetStyle: NSObject {
    public var collectionViewTopEdge: CGFloat = 32
    public var itemType: AUIActionSheetItemLayoutType = .horizontal
    public var itemHeight: CGFloat = 48
    public var titleLabelFont: UIFont = .aui_big
    public var titleLabelTextColor: UIColor = .white
    public var nameLabelFont: UIFont = .aui_big
    public var nameLabelTextColor: UIColor = .white
    public var seatLabelFont: UIFont = .aui_small
    public var seatLabelTextColor: UIColor = .white.withAlphaComponent(0.8)
    public var avatarWidth: CGFloat = 40
    public var avatarHeight: CGFloat = 40
}

open class AUIActionSheetHeaderInfo: NSObject {
    public var title: String = ""
    public var subTitle: String = ""
    public var avatar: String = ""
}

open class AUIActionSheet: UIView {
    @objc private var itemHeight: CGFloat = 48 {
        didSet {
            layoutCollectionView()
        }
    }
    @objc var itemType: AUIActionSheetItemLayoutType = .vertical {
        didSet {
            layoutCollectionView()
        }
    }
    @objc private var collectionViewTopEdge: CGFloat = 32 {
        didSet {
            layoutCollectionView()
        }
    }
    private let title: String
    private let items: [AUIActionSheetItem]!
    private let headerInfo: AUIActionSheetHeaderInfo?
    
    private lazy var flowLayout = UICollectionViewFlowLayout()
    
    private lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.register(AUIActionSheetCell.self, forCellWithReuseIdentifier: kAUIActionSheetCellId)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear
        collectionView.showsVerticalScrollIndicator = false
        collectionView.showsHorizontalScrollIndicator = false
        return collectionView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
//        label.theme_font = "ActionSheet.normalFont"
//        label.theme_textColor = "ActionSheet.normalColor"
        return label
    }()
    
    private lazy var avatarView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var nameLabel: UILabel = {
        let label = UILabel()
//        label.theme_font = "ActionSheet.normalFont"
//        label.theme_textColor = "ActionSheet.normalColor"
        
        return label
    }()
    
    private lazy var seatLabel: UILabel = {
        let label = UILabel()
//        label.theme_font = "ActionSheet.seatLabelFont"
//        label.theme_textColor = "ActionSheet.seatLabelColor"
        
        return label
    }()
    
    public init(title: String,
                items: [AUIActionSheetItem],
                headerInfo: AUIActionSheetHeaderInfo?) {
        self.title = title
        self.items = items
        self.headerInfo = headerInfo
        super.init(frame: .zero)
        _loadSubviews()
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubviews() {
        backgroundColor = .clear
        addSubview(titleLabel)
        addSubview(avatarView)
        addSubview(nameLabel)
        addSubview(seatLabel)
        
        guard let window = getWindow() else {
            return
        }
        let windowFrame = window.frame
        titleLabel.text = self.title
        titleLabel.sizeToFit()
        titleLabel.aui_tl = CGPoint(x: kEdgeSpace, y: kEdgeSpace)
        
        if let headerInfo = self.headerInfo {
            avatarView.frame = CGRect(x: kEdgeSpace, y: titleLabel.aui_bottom + 13, width: kAvatarViewWidth, height: kAvatarViewWidth)
//            avatarView.theme_width = "ActionSheet.avatarWidth"
//            avatarView.theme_height = "ActionSheet.avatarHeight"
            avatarView.layer.cornerRadius = kAvatarViewWidth / 2
            avatarView.clipsToBounds = true
            nameLabel.frame = CGRect(x: avatarView.aui_right + kUserTitleLeftPadding,
                                     y: avatarView.aui_top,
                                     width: windowFrame.width - avatarView.aui_right,
                                     height: avatarView.aui_height / 2)
            seatLabel.frame = CGRect(x: nameLabel.aui_left, y: nameLabel.aui_bottom, width: nameLabel.aui_width, height: nameLabel.aui_height)
            avatarView.sd_setImage(with: URL(string: headerInfo.avatar))
            nameLabel.text = headerInfo.title
            seatLabel.text = headerInfo.subTitle
        } else {
            avatarView.alpha = 0
            nameLabel.alpha = 0
            seatLabel.alpha = 0
        }
        
        layoutCollectionView()
        addSubview(collectionView)
        sizeToFit()
    }
    
    private func layoutCollectionView() {
        guard let window = getWindow() else {
            return
        }
        let windowFrame = window.frame
        
        var tableViewTop: CGFloat = 0
        if let _ = self.headerInfo {
            tableViewTop = avatarView.aui_bottom
        } else {
            tableViewTop = titleLabel.aui_bottom
        }
        tableViewTop += collectionViewTopEdge
        if itemType == .vertical {
            flowLayout.minimumLineSpacing = 0
            let width: CGFloat = windowFrame.width
            let height: CGFloat = itemHeight
            flowLayout.itemSize = CGSize(width: width, height: height)
//            flowLayout.scrollDirection = .vertical
            collectionView.frame = CGRect(x: 0, y: tableViewTop, width: windowFrame.width, height: itemHeight * CGFloat(items.count))
        } else {
            let width: CGFloat = 56
            let height: CGFloat = itemHeight
            let padding = CGFloat(Int((windowFrame.width - CGFloat(items.count) * width) / CGFloat(items.count + 1)))
            flowLayout.sectionInset = UIEdgeInsets(top: 0, left: padding, bottom: 0, right: padding)
            flowLayout.minimumInteritemSpacing = padding
            flowLayout.itemSize = CGSize(width: width, height: height)
//            flowLayout.scrollDirection = .horizontal
            collectionView.frame = CGRect(x: 0, y: tableViewTop, width: windowFrame.width, height: itemHeight)
        }
    }
    
    open override func sizeThatFits(_ size: CGSize) -> CGSize {
        guard let windowFrame = getWindow()?.frame else {
            return .zero
        }
        return CGSize(width: windowFrame.width, height: collectionView.aui_bottom + UIDevice.current.aui_SafeDistanceBottom)
    }
    
    public func setStyle(style: AUIActionSheetStyle) {
        collectionViewTopEdge = style.collectionViewTopEdge
        itemType = style.itemType
        itemHeight = style.itemHeight
        
        titleLabel.font = style.titleLabelFont
        titleLabel.textColor = style.titleLabelTextColor
        
        nameLabel.font = style.nameLabelFont
        nameLabel.textColor = style.nameLabelTextColor
        
        seatLabel.font = style.seatLabelFont
        seatLabel.textColor = style.seatLabelTextColor
        
        seatLabel.font = style.seatLabelFont
        seatLabel.textColor = style.seatLabelTextColor
        
        avatarView.aui_width = style.avatarWidth
        avatarView.aui_height = style.avatarHeight
        
        layoutCollectionView()
        sizeToFit()
    }
}

extension AUIActionSheet: UICollectionViewDelegate, UICollectionViewDataSource {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.items.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: AUIActionSheetCell = collectionView.dequeueReusableCell(withReuseIdentifier: kAUIActionSheetCellId, for: indexPath) as! AUIActionSheetCell
        cell.itemType = itemType
        let item = self.items[indexPath.row]
        cell.item = item
        
        return cell
    }
    
    public func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let item = self.items[indexPath.row]
        item.callback?()
        //TODO: optimize reload policy
        collectionView.reloadData()
    }
}


//MARK: Theme
open class AUIActionSheetTheme: NSObject {
    public var collectionViewTopEdge: ThemeCGFloatPicker = "ActionSheet.collectionViewTopEdge"
    public var itemType: ThemeCGFloatPicker = "ActionSheet.itemType"
    public var itemHeight: ThemeCGFloatPicker = "ActionSheet.itemHeight"
    public var titleLabelFont: ThemeFontPicker = "ActionSheet.titleFont"
    public var titleLabelTextColor: ThemeColorPicker = "ActionSheet.titleColor"
    public var nameLabelFont: ThemeFontPicker = "ActionSheet.normalFont"
    public var nameLabelTextColor: ThemeColorPicker = "ActionSheet.normalColor"
    public var seatLabelFont: ThemeFontPicker = "ActionSheet.seatLabelFont"
    public var seatLabelTextColor: ThemeColorPicker = "ActionSheet.seatLabelColor"
    public var avatarWidth: ThemeCGFloatPicker = "ActionSheet.avatarWidth"
    public var avatarHeight: ThemeCGFloatPicker = "ActionSheet.avatarHeight"
}

extension AUIActionSheet {
    public func setTheme(theme: AUIActionSheetTheme) {
        theme_collectionViewTopEdge = theme.collectionViewTopEdge
        theme_itemType = theme.itemType
        theme_itemHeight = theme.itemHeight
        
        titleLabel.theme_font = theme.titleLabelFont
        titleLabel.theme_textColor = theme.titleLabelTextColor
        
        nameLabel.theme_font = theme.nameLabelFont
        nameLabel.theme_textColor = theme.nameLabelTextColor
        
        seatLabel.theme_font = theme.seatLabelFont
        seatLabel.theme_textColor = theme.seatLabelTextColor
        
        seatLabel.theme_font = theme.seatLabelFont
        seatLabel.theme_textColor = theme.seatLabelTextColor
        
        avatarView.theme_width = theme.avatarWidth
        avatarView.theme_height = theme.avatarHeight
        
        layoutCollectionView()
        sizeToFit()
    }
    
    @objc func setItemType(itemType: CGFloat) {
        let val = AUIActionSheetItemLayoutType(rawValue: Int(itemType)) ?? .vertical
        self.itemType = val
    }
    
    var theme_itemHeight: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setItemHeight:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setItemHeight:", newValue) }
    }
    var theme_itemType: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setItemTypeWithItemType:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setItemTypeWithItemType:", newValue) }
    }
    
    var theme_collectionViewTopEdge: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setCollectionViewTopEdge:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setCollectionViewTopEdge:", newValue) }
    }
}

