//
//  AIChatNavigation.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage

public let StatusBarHeight :CGFloat = UIApplication.shared.statusBarFrame.height

public let NavigationHeight :CGFloat = StatusBarHeight + 44

@objc public enum AIChatNavigationBarClickEvent: UInt {
    case back
    case avatar
    case title
    case subtitle
    case rightTitle
    case rightItems
    case cancel
}

/// Navigation  bar of the EaseChatUIKit.
@objcMembers open class AIChatNavigation: UIView {
    
//    public var userState: UserState = .online {
//        willSet {
//            DispatchQueue.main.async {
//                self.status.image = nil
//                self.status.backgroundColor = newValue == .online ? (Theme.style == .dark ? UIColor.theme.secondaryColor6:UIColor.theme.secondaryColor5):(Theme.style == .dark ? UIColor.theme.neutralColor6:UIColor.theme.neutralColor5)
//            }
//        }
//    }
    
    public var titleOriginFrame = CGRect.zero
    
    public var clickClosure: ((AIChatNavigationBarClickEvent,IndexPath?) -> ())?
    
    public let backImage = UIImage(named: "back", in: .chatAIBundle, with: nil)?.withTintColor(UIColor.theme.neutralColor3)
    
    private var rightImages = [UIImage]()
    
    private var showLeft = false
    
    /// Title kind of the ``NSAttributedString``.
    public var titleAttribute: NSAttributedString? {
        didSet {
            self.titleLabel.text = nil
            self.titleLabel.attributedText = self.titleAttribute
        }
    }
    
    /// Entry edit mode or not.
    public var editMode = false {
        didSet {
            DispatchQueue.main.async {
                self.cancel.isHidden = !self.editMode
                self.rightItems.isHidden = self.editMode
                self.rightItem.isHidden = self.editMode
                self.leftItem.isHidden = self.editMode
            }
        }
    }
    
    /// TitleLabel's text.
    public var title: String? {
        didSet {
            self.titleLabel.attributedText = nil
            self.titleLabel.text = self.title
            if self.detail.text == nil || self.detail.text?.isEmpty ?? false {
                self.titleLabel.center = CGPoint(x: self.titleLabel.center.x, y: self.leftItem.center.y)
            } else {
                self.titleLabel.frame = self.titleOriginFrame
            }
        }
    }
    
    /// Detail's text.
    public var subtitle: String? {
        didSet {
            self.detail.text = self.subtitle
        }
    }
    
    /// Avatar url.Only available when using initialization method with avatar.
    public var avatarURL: String? {
        didSet {
            if let url = self.avatarURL {
                let urls = url.components(separatedBy: ",")
                if urls.count > 1{
                    self.avatar.bottomRightImageView.isHidden = false
                    self.avatar.topLeftImageView.isHidden = false
                    self.avatar.refresh(with: (urls[0],urls[1]))
                } else {
                    self.avatar.bottomRightImageView.isHidden = true
                    self.avatar.topLeftImageView.isHidden = true
                    if let avatar_url = URL(string: url) {
                        self.avatar.sd_setImage(with: avatar_url)
                    }
                    
                }
            }
        }
    }
    
    public private(set) lazy var leftItem: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 8, y: self.frame.height-30, width: 24, height: 24)).tag(0).addTargetFor(self, action: #selector(buttonAction(sender:)), for: .touchUpInside).backgroundColor(.clear)
    }()
    
    public private(set) lazy var avatar: SquareViewWithImages = {
        SquareViewWithImages(frame: CGRect(x: self.showLeft ? self.leftItem.frame.maxX:CGFloat(10), y: self.frame.height-38, width: 32, height: 32)).backgroundColor(.clear).cornerRadius(16).tag(1).contentMode(.scaleAspectFill)
    }()
    
    public private(set) lazy var status: UIImageView = {
        self.createAvatarStatus()
    }()
    
    open func createAvatarStatus() -> UIImageView {
        let r = self.avatar.frame.width / 2.0
        let length = CGFloat(sqrtf(Float(r)))
        let x = (true ? (r + length + 3):(self.avatar.frame.width-10))
        let y = (true ? (r + length + 3):(self.avatar.frame.height-10))
        return UIImageView(frame: CGRect(x: self.avatar.frame.minX+x, y: self.avatar.frame.minY+y, width: 12, height: 12)).backgroundColor(UIColor.theme.secondaryColor5).cornerRadius(6).layerProperties(UIColor.theme.neutralColor98, 2).contentMode(.scaleAspectFit)
    }
    
    public private(set) lazy var titleLabel: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX+4, y: (nearStatusBar ? StatusBarHeight:0)+2, width: ScreenWidth-self.avatar.frame.maxX*2-8*3, height: 22)).font(UIFont.theme.headlineSmall).textColor(UIColor.theme.neutralColor1).backgroundColor(.clear).tag(2)
    }()
    
    public private(set) lazy var detail: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX+4, y: self.titleLabel.frame.maxY, width: self.titleLabel.frame.width, height: 14)).font(UIFont.theme.bodyExtraSmall).textColor(UIColor.theme.neutralColor5).backgroundColor(.clear)
    }()
    
    public private(set) lazy var layout: UICollectionViewFlowLayout = {
        let flow = UICollectionViewFlowLayout()
        flow.itemSize = CGSize(width: 36, height: 36)
        flow.scrollDirection = .horizontal
        flow.minimumLineSpacing = 0
        flow.minimumInteritemSpacing = 4
        return flow
    }()
    
    public private(set) lazy var rightItems: UICollectionView = {
        UICollectionView(frame: CGRect(x: ScreenWidth-CGFloat(self.rightImages.count*36)-8, y: self.frame.height-42, width: CGFloat(self.rightImages.count*36), height: 36), collectionViewLayout: self.layout).registerCell(EaseChatNavigationBarRightCell.self, forCellReuseIdentifier: "EaseChatNavigationBarRightCell").delegate(self).dataSource(self).backgroundColor(.clear)
    }()
    
    public private(set) lazy var rightItem: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: ScreenWidth-158, y: self.frame.height-34, width: 138, height: 28)).font(UIFont.theme.labelMedium).tag(3).backgroundColor(.clear).addTargetFor(self, action: #selector(buttonAction(sender:)), for: .touchUpInside)
    }()
    
    public private(set) lazy var separateLine: UIView = {
        UIView(frame: CGRect(x: 0, y: self.frame.height-0.5, width: ScreenWidth, height: 0.5)).backgroundColor(UIColor.theme.neutralColor9)
    }()
    
    public private(set) lazy var cancel: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: ScreenWidth-58, y: self.frame.height-36, width: 50, height: 28)).backgroundColor(.clear).title("取消", .normal).font(UIFont.theme.labelMedium).textColor(UIColor.theme.primaryColor5, .normal).tag(4).addTargetFor(self, action: #selector(buttonAction(sender:)), for: .touchUpInside)
    }()
    
    private var originalRenderRightImage = false
    
    private var nearStatusBar = true

    internal override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    /// EaseChatNavigationBar init method with right items kind of images.
    /// - Parameters:
    ///   - showLeftItem: Whether show left button or not.
    ///   - textAlignment: Title and subtitle text alignment.
    ///   - placeHolder: Avatar default image.
    ///   - avatarURL: Avatar url.
    ///   - rightImages: Right buttons kind of `[UIImage]`.
    ///   - hiddenAvatar: Whether hide avatar or not.
    @objc required public convenience init(frame: CGRect = CGRect(x: 0, y: 0, width: ScreenWidth, height: NavigationHeight),showLeftItem: Bool, textAlignment: NSTextAlignment = .center, placeHolder: UIImage? = nil,avatarURL: String? = nil,rightImages: [UIImage] = [],hiddenAvatar: Bool = false,nearStatusBar: Bool = true) {
        self.init(frame: frame)
        self.showLeft = showLeftItem
        self.nearStatusBar = nearStatusBar
        if showLeftItem {
            var width = CGFloat(self.rightImages.count*36)
            if self.avatar.frame.maxX+4 > width {
                width = self.avatar.frame.maxX+4
            }
            if hiddenAvatar {
                self.addSubViews([self.leftItem,self.titleLabel,self.detail,self.rightItems,self.separateLine,self.cancel])
            } else {
                self.addSubViews([self.leftItem,self.avatar,self.status,self.titleLabel,self.detail,self.rightItems,self.separateLine,self.cancel])
            }
            self.titleLabel.frame = CGRect(x: (hiddenAvatar ? self.leftItem.frame.maxX:self.avatar.frame.maxX)+8, y: (nearStatusBar ? StatusBarHeight:0)+4, width: ScreenWidth - width*2 - 4, height: 22)
            if textAlignment == .center {
                self.titleLabel.center = CGPoint(x: self.center.x, y: self.titleLabel.center.y)
            }
            self.detail.frame = CGRect(x: self.titleLabel.frame.minX, y: self.titleLabel.frame.maxY, width: self.titleLabel.frame.width, height: 14)
        } else {
            if hiddenAvatar {
                self.addSubViews([self.titleLabel,self.detail,self.rightItems,self.separateLine,self.cancel])
            } else {
                self.addSubViews([self.avatar,self.status,self.titleLabel,self.detail,self.rightItems,self.separateLine,self.cancel])
            }
            self.bringSubviewToFront(self.avatar)
            self.bringSubviewToFront(self.status)
            self.titleLabel.frame = CGRect(x: (hiddenAvatar ? self.leftItem.frame.maxX:self.avatar.frame.maxX)+8, y: (nearStatusBar ? StatusBarHeight:0), width: ScreenWidth - CGFloat(self.rightImages.count*36)*2, height: 22)
            if textAlignment == .center {
                self.titleLabel.center = CGPoint(x: self.center.x, y: self.titleLabel.center.y)
            }
            self.detail.frame = CGRect(x: self.titleLabel.frame.minX, y: self.titleLabel.frame.maxY, width: self.titleLabel.frame.width, height: 14)
        }
        self.titleOriginFrame = self.titleLabel.frame
        self.titleLabel.textAlignment = textAlignment
        self.detail.textAlignment = textAlignment
        if let url = avatarURL {
            self.avatar.sd_setImage(with: URL(string: url), placeholderImage: UIImage(named: "bot_avatar", in: .chatAIBundle, with: nil))
        } else {
            self.avatar.image = nil
        }
        self.leftItem.setHitTestEdgeInsets(UIEdgeInsets(top: -10, left: -10, bottom: -10, right: -10))
        self.addGesture()
        self.cancel.isHidden = true
        self.leftItem.center = CGPoint(x: self.leftItem.center.x, y: self.leftItem.center.y-2)
        self.avatar.frame = CGRect(x: self.showLeft ? self.leftItem.frame.maxX:CGFloat(10), y: self.frame.height-38, width: 32, height: 32)
        self.updateRightItems(images: rightImages)
        self.titleLabel.frame = CGRect(x: (hiddenAvatar ? self.leftItem.frame.maxX:self.avatar.frame.maxX)+8, y: (nearStatusBar ? StatusBarHeight:0)+4, width: ScreenWidth - self.rightItems.frame.width - 8 - ((hiddenAvatar ? self.leftItem.frame.maxX:self.avatar.frame.maxX)+8), height: 22)
        self.detail.frame = CGRect(x: self.titleLabel.frame.minX, y: self.titleLabel.frame.maxY, width: self.titleLabel.frame.width, height: 14)
        self.status.isHidden = true
        self.leftItem.setImage(self.backImage, for: .normal)
        self.separateLine.isHidden = true
        Theme.registerSwitchThemeViews(view: self)
        self.switchTheme(style: Theme.style)
    }
    
    /// EaseChatNavigationBar init method with right item kind of text.
    /// - Parameters:
    ///   - textAlignment: ``NSTextAlignment``
    ///   - rightTitle: Title of the right item.
    @objc required public convenience init(frame: CGRect = CGRect(x: 0, y: 0, width: ScreenWidth, height: NavigationHeight),textAlignment: NSTextAlignment = .center,rightTitle: String? = nil) {
        self.init(frame: frame)
        self.addSubViews([self.leftItem,self.titleLabel,self.detail,self.rightItem,self.separateLine,self.cancel])
        self.leftItem.setHitTestEdgeInsets(UIEdgeInsets(top: -10, left: -10, bottom: -10, right: -10))
        self.titleLabel.frame = CGRect(x: self.leftItem.frame.maxX+4, y: StatusBarHeight+4, width: ScreenWidth - 168, height: 22)
        if textAlignment == .center {
            self.titleLabel.frame = CGRect(x: 84, y: StatusBarHeight+4, width: ScreenWidth - 168, height: 22)
            self.titleLabel.center = CGPoint(x: self.center.x, y: self.titleLabel.center.y)
        }
        self.rightItem.contentHorizontalAlignment = .right
        self.rightItem.setTitle(rightTitle, for: .normal)
        self.detail.frame = CGRect(x: self.titleLabel.frame.minX, y: self.titleLabel.frame.maxY, width: self.titleLabel.frame.width, height: 14)
        self.titleLabel.textAlignment = textAlignment
        self.detail.textAlignment = textAlignment
        self.addGesture()
        self.cancel.isHidden = true
        self.status.isHidden = true
        self.titleOriginFrame = self.titleLabel.frame
        self.leftItem.setImage(self.backImage, for: .normal)
        Theme.registerSwitchThemeViews(view: self)
        self.switchTheme(style: Theme.style)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func addGesture() {
        let gesture = UITapGestureRecognizer(target: self, action: #selector(clickAction(gesture:)))
        self.titleLabel.isUserInteractionEnabled = true
        self.avatar.isUserInteractionEnabled = true
        self.titleLabel.addGestureRecognizer(gesture)
        self.avatar.addGestureRecognizer(gesture)
    }
    
    @objc open func clickAction(gesture: UITapGestureRecognizer) {
        guard let tag = gesture.view?.tag else { return }
        switch tag {
        case 1:
            self.clickClosure?(.avatar,nil)
        case 2:
            self.clickClosure?(.title,nil)
        default:
            break
        }
    }
    
    @objc open func buttonAction(sender: UIButton) {
        switch sender.tag {
        case 0:
            self.clickClosure?(.back,nil)
        case 3:
            self.clickClosure?(.rightTitle,nil)
        case 4:
            self.clickClosure?(.cancel,nil)
        default:
            break
        }
    }
    
    @objc public func updateRightItems(images: [UIImage],original: Bool = false) {
        self.originalRenderRightImage = original
        self.rightImages.removeAll()
        if images.count > 3 {
            self.rightImages = Array(images.prefix(3))
        } else {
            self.rightImages.append(contentsOf: images)
        }
        self.rightItems.frame = CGRect(x: ScreenWidth-CGFloat(images.count*36)-8, y: (nearStatusBar ? StatusBarHeight:0)+8, width: CGFloat(images.count*36), height: 36)
        self.rightItems.reloadData()
    }

}

extension AIChatNavigation: UICollectionViewDataSource,UICollectionViewDelegate {
     
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.rightImages.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "EaseChatNavigationBarRightCell", for: indexPath) as? EaseChatNavigationBarRightCell
        if self.originalRenderRightImage {
            cell?.imageView.image = self.rightImages[safe: indexPath.row]?.withRenderingMode(.alwaysOriginal)
        } else {
            cell?.imageView.image = self.rightImages[safe: indexPath.row]?.withTintColor(Theme.style == .dark ? UIColor.theme.neutralColor98:UIColor.theme.neutralColor3)
        }
        return cell ?? UICollectionViewCell()
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        let cell = collectionView.cellForItem(at: indexPath)
        UIView.animate(withDuration: 0.382, delay: 0) {
            cell?.backgroundColor = Theme.style == .dark ? UIColor.theme.neutralColor2:UIColor.theme.neutralColor95
            cell?.contentView.backgroundColor = Theme.style == .dark ? UIColor.theme.neutralColor2:UIColor.theme.neutralColor95
        } completion: { finished in
            cell?.backgroundColor = .clear
            cell?.contentView.backgroundColor = .clear
        }
        self.clickClosure?(.rightItems,indexPath)
    }
}

extension AIChatNavigation: ThemeSwitchProtocol {
    public func switchTheme(style: ThemeStyle) {
        self.backgroundColor = style == .dark ? UIColor.theme.neutralColor1:UIColor.theme.neutralColor98
//        switch self.userState {
//        case .online:
//            self.status.backgroundColor = style == .dark ? UIColor.theme.secondaryColor6:UIColor.theme.secondaryColor5
//        case .offline:
//            self.status.backgroundColor = style == .dark ? UIColor.theme.neutralColor6:UIColor.theme.neutralColor5
//        }
        self.titleLabel.textColor = style == .dark ? UIColor.theme.neutralColor98:UIColor.theme.neutralColor1
        self.detail.textColor = style == .dark ? UIColor.theme.neutralColor6:UIColor.theme.neutralColor5
        self.leftItem.setImage(self.backImage?.withTintColor(Theme.style == .dark ? UIColor.theme.neutralColor98:UIColor.theme.neutralColor3), for: .normal)
        self.rightItem.setTitleColor(style == .dark ? UIColor.theme.neutralColor3:UIColor.theme.neutralColor7, for: .disabled)
        self.rightItem.setTitleColor(style == .dark ? UIColor.theme.primaryColor6:UIColor.theme.primaryColor5, for: .normal)
        self.separateLine.backgroundColor = style == .dark ? UIColor.theme.neutralColor2:UIColor.theme.neutralColor9
        self.cancel.textColor(style == .dark ? UIColor.theme.primaryColor6:UIColor.theme.primaryColor5, .normal)
        self.status.layerProperties(style == .dark ? UIColor.theme.neutralColor1:UIColor.theme.neutralColor98, 2)
        self.rightItems.reloadData()
    }
    
    
}

@objc open class EaseChatNavigationBarRightCell: UICollectionViewCell {
    
    lazy var imageView: UIImageView = {
        UIImageView(frame: CGRect(x: 6, y: 6, width: self.contentView.frame.width-12, height: self.contentView.frame.height-12)).contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()
    
    @objc public override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.addSubview(self.imageView)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

