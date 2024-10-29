//
//  AUIChatBarFunctionCell.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit
/*!
 *  \~Chinese
 *  底部功能区域Collection Cell
 *
 *  \~English
 *  Bottom functional area Collection Cell.
 *
 */
public class AUIChatBarFunctionCell: UICollectionViewCell {
    /*!
     *  \~Chinese
     *  Cell中颜色字体背景色配置
     *
     *  \~English
     *  Color font background color configuration in Cell.
     *
     */
    public var config: AUIChatBarFunctionCellConfig = AUIChatBarFunctionCellConfig() {
        willSet {
            self.container.backgroundColor = newValue.containerBackgroundColor
            self.container.cornerRadius(newValue.containerRadius)
        }
    }
    
    
    lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height))
            .contentMode(.scaleAspectFit)
            .backgroundColor(AUIChatTheme.shared.bottombar.containerBackgroundColor)
            .cornerRadius(self.contentView.frame.height / 2.0)
    }()

    lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()

    let redDot = UIView().backgroundColor(.red).cornerRadius(3)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
        self.contentView.backgroundColor = .clear
        self.contentView.addSubViews([self.container,self.redDot,self.icon])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let r = contentView.frame.width / 2.0
        self.container.cornerRadius(r)
        let length = CGFloat(ceilf(Float(r) / sqrt(2)))
        self.redDot.frame = CGRect(x: frame.width / 2.0 + length, y: contentView.frame.height / 2.0 - length, width: 6, height: 6)
        self.icon.frame = CGRect(x: 7, y: 7, width: contentView.frame.width - 14, height: contentView.frame.height - 14)
    }
}


@objc public class AUIChatBarFunctionCellConfig: NSObject {
    var containerBackgroundColor: UIColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
    
    var containerRadius: CGFloat = 10
    
    var mode: AUIThemeMode = .light {
        willSet {
            self.containerBackgroundColor = newValue == .dark ? UIColor(red: 0, green: 0, blue: 0, alpha: 0.3) : UIColor(red: 1, green: 1, blue: 1, alpha: 0.2)
        }
    }
}
