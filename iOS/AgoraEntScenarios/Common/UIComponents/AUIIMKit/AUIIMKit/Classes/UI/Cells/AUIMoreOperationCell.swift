//
//  AUIMoreOperationCell.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/7.
//

import UIKit
import SDWebImage
/*!
 *  \~Chinese
 *  更多列表中Cell数据实体协议
 *
 *  \~English
 *  More list cell data protocol.
 *
 */
@objc public protocol AUIMoreOperationCellDataProtocol: NSObjectProtocol {
    
    var index: Int {get set}
    var iconUrl: String {get set}
    var placeHolder: UIImage? {get set}
    var operationName: String {get set}
    var showRedDot: Bool {get set}
}
/*!
 *  \~Chinese
 *  更多列表中Cell数据实体
 *
 *  \~English
 *  More list cell data entity.
 *
 */
@objc public class AUIMoreOperationCellEntity: NSObject, AUIMoreOperationCellDataProtocol {
    
    public var index: Int = 0
    
    public var iconUrl: String = ""
    
    public var placeHolder: UIImage? = UIImage.aui_Image(named: "hands")
    
    public var operationName: String = aui_localized("Application List")//"申请列表"
    
    public var showRedDot: Bool = true
    

}
/*!
 *  \~Chinese
 *  更多列表中Cell
 *
 *  \~English
 *  More list cell.
 *
 */
final public class AUIMoreOperationCell: UICollectionViewCell {
    
    private lazy var iconContainer: UIImageView = {
        UIImageView(frame: CGRect(x: 5, y: 0, width: self.contentView.frame.width-10, height: self.contentView.frame.width-10))
            .contentMode(.scaleAspectFit)
            .backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2))
            .backgroundColor(.clear)
            .cornerRadius((self.contentView.frame.width-10) / 2.0)
            .createThemeGradient(AUIChatTheme.shared.bottombar.moreGradientColors, self.config.iconContainerGradientLocations)
    }()

    private lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: 8, y: 7, width: self.iconContainer.frame.width-16, height: self.iconContainer.frame.width-16)).contentMode(.scaleAspectFit).backgroundColor(.clear)
    }()

    private let redDot = UIView().backgroundColor(.red).cornerRadius(3)
    
    public var config: AUIMoreOperationCellConfig = AUIMoreOperationCellConfig() {
        willSet {
            self.iconContainer.setGradient(newValue.iconContainerGradientColors, newValue.iconContainerGradientLocations)
            self.title.font = newValue.titleFont
            self.title.textColor = newValue.titleColor
        }
    }
        
    private lazy var title: UILabel = {
        UILabel(frame: CGRect(x: 0, y: self.iconContainer.frame.maxY+4, width: self.frame.width, height: 32)).font(self.config.titleFont).textColor(self.config.titleColor).numberOfLines(0).textAlignment(.center)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.contentView.addSubViews([self.iconContainer,self.title,self.redDot])
        self.iconContainer.addSubViews([self.icon])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let r = self.iconContainer.frame.width / 2.0
        let length = CGFloat(ceilf(Float(r) / sqrt(2)))
        self.redDot.frame = CGRect(x: r + length + 3, y: r - length, width: 6, height: 6)
        self.icon.frame = CGRect(x: 8, y: 7, width: self.iconContainer.frame.width-16, height: self.iconContainer.frame.width-16)
    }
    /*!
     *  \~Chinese
     *  刷新AUIMoreOperationCell方法
     *
     *  @param info  遵循AUIMoreOperationCellDataProtocol协议的实体对象
     *
     *  \~English
     *  Refresh AUIMoreOperationCell method.
     *
     *  @param info  An entity object conforming to the AUIMoreOperationCellDataProtocol protocol.
     */
    public func refresh(info: AUIMoreOperationCellDataProtocol) {
        self.title.text = info.operationName
        self.icon.sd_setImage(with: URL(string: info.iconUrl), placeholderImage: info.placeHolder)
        self.redDot.isHidden = !info.showRedDot
    }
}

@objc public enum AUIThemeMode: Int {
    case light
    case dark
}

@objc public class AUIMoreOperationCellConfig: NSObject {
    
    var iconContainerGradientColors: [UIColor] = [UIColor(red: 0.898, green: 0.961, blue: 1, alpha: 1),UIColor(red: 0.486, green: 0.357, blue: 1, alpha: 0)]
    
    var iconContainerGradientLocations: [CGPoint] = [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)]
    
    var titleFont: UIFont = .systemFont(ofSize: 12, weight: .regular)
    
    var titleColor: UIColor = UIColor(0x464E53)
    
    
    
    var mode: AUIThemeMode = .light {
        willSet {
            switch newValue {
            case .light:
                self.iconContainerGradientColors = [UIColor(red: 0.898, green: 0.961, blue: 1, alpha: 1),UIColor(red: 0.486, green: 0.357, blue: 1, alpha: 0)]
                self.titleColor = UIColor(0x464E53)
            case .dark:
                self.iconContainerGradientColors = [UIColor(red: 0, green: 0.248, blue: 0.4, alpha: 1),UIColor(red: 0.104, green: 0, blue: 0.4, alpha: 0.2)]
                self.titleColor = UIColor(0xACB4B9)
            }
        }
    }
}
