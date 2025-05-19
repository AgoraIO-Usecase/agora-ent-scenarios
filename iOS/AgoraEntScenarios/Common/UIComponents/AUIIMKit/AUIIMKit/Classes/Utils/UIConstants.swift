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


let logTag = "AUIIMKit"
let formatter = DateFormatter()
func aui_info(_ message: String, tag: String = logTag) {
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    print("\(timeString)[\(tag)] \(message)")
}

func aui_benchmark(_ text: String, cost: Double, tag: String = logTag) {
    aui_info("[Benchmark]\(text): \(Int64(cost * 1000)) ms", tag: tag)
}

func aui_warn(_ text: String, tag: String = logTag) {
    aui_info("[Warning]\(text)", tag: tag)
}

func aui_error(_ text: String, tag: String = logTag) {
    aui_info("[Error]\(text)", tag: tag)
}
