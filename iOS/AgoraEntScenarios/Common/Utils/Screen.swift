//
//  Screen.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import Foundation
import UIKit

struct Screen {
    /// 屏幕宽度
    static let width = UIScreen.main.bounds.width
    /// 屏幕高度
    static let height = UIScreen.main.bounds.height
    /// 线的高度
    static let kSeparatorHeight = 1.0 / UIScreen.main.scale

    /// 导航高度
    static var kNavHeight: CGFloat {
        44 + statusHeight()
    }

    /// 标准屏幕比例
    static let scaleSize: CGFloat = width / 375.0

    /// 安全区域高度
    static func safeHeight() -> CGFloat {
        guard let safeInserts = UIApplication.keyWindow?.safeAreaInsets else {
            return 0
        }
        return height - safeInserts.top - safeInserts.bottom
    }

    /// 等比宽度
    static func uiWidth(_ mywith: CGFloat) -> CGFloat {
        return mywith * scaleSize
    }

    /// 状态栏高度
    static func statusHeight() -> CGFloat {
        var height: CGFloat = 0.0
        if #available(iOS 13.0, *) {
            let statusBarManager = UIApplication.keyWindow?.windowScene?.statusBarManager
            height = statusBarManager?.statusBarFrame.height ?? 44

        } else {
            height = UIApplication.shared.statusBarFrame.height
        }

        return height
    }

    /// tabbar高度
    static func tabbarHeight() -> CGFloat {
        let tabVc = UITabBarController()
        return tabVc.tabBar.frame.size.height
    }

    /// 安全区域底部高度
    static func safeAreaBottomHeight() -> CGFloat {
        guard let safeInserts = UIApplication.keyWindow?.safeAreaInsets else {
            return 0
        }
        return safeInserts.bottom
    }

    /// 安全区域顶部高度
    static func safeAreaTopHeight() -> CGFloat {
        guard let safeInserts = UIApplication.shared.windows.first?.safeAreaInsets else {
            return 0
        }
        return safeInserts.top
    }

    /// 以375宽度为基准
    static func contentPixel(pixel: CGFloat) -> CGFloat {
        return Screen.width * pixel / 375.0
    }
}

extension Int {
    var fit: CGFloat {
        return Screen.scaleSize * CGFloat(self)
    }
}

extension CGFloat {
    var fit: CGFloat {
        return Screen.scaleSize * CGFloat(self)
    }
}

extension Double {
    var fit: CGFloat {
        return Screen.scaleSize * CGFloat(self)
    }
}
