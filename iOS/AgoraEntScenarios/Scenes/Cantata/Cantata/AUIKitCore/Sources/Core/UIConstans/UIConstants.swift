//
//  UIConstants.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit
import Foundation

public let AScreenWidth = UIScreen.main.bounds.width

public let AScreenHeight = UIScreen.main.bounds.height

public let AEdgeZero: UIEdgeInsets = .zero

@available(iOS 11.0, *)
public let ASafeAreaExist = (UIApplication.shared.keyWindow?.rootViewController?.view.safeAreaInsets != .zero)

public let ABottomBarHeight = (UIApplication.shared.statusBarFrame.height > 20 ? 34:0)

public let ATabBarHeight = (UIApplication.shared.statusBarFrame.height > 20 ? 49+34:49)

public let AStatusBarHeight :CGFloat = UIApplication.shared.statusBarFrame.height

public let ANavigationHeight :CGFloat = UIApplication.shared.statusBarFrame.height + 44

