//
//  AUIChatTheme.swift
//  AFNetworking
//
//  Created by wushengtao on 2024/7/31.
//

import Foundation
import YYCategory

public class AUIChatTheme {
    public static let shared = AUIChatTheme()
    public public(set) lazy var barrage = AUIChatBarrageTheme()
    public public(set) lazy var bottombar = AUIChatBottomBarTheme()
    public public(set) lazy var inputbar = AUIChatInputBarTheme()
}

public class AUIChatBarrageTheme {
    var containerBackgroundColor = UIColor(hexString:"#00000050")!
    var containerLayerWidth = 1.0
    var containerLayerColor = UIColor(hexString:"#F9FAFA99")!
    var containerLayerCornerRadius = 12.0
}

public class AUIChatBottomBarTheme {
    var containerBackgroundColor = UIColor(hexString:"#00000050")!
    var moreGradientColors = [UIColor(hexString:"#003F66FF")!, UIColor(hexString:"#1B006651")!]
}

public class AUIChatInputBarTheme {
    var backgroundColor = UIColor(hexString:"#FFFFFF")!
    var textFont = UIFont.systemFont(ofSize: 15)
    var placeHolderColor = UIColor(hexString:"#B6B8C9")!
    var cursorColor = UIColor(hexString:"#009FFF")!
    var textColor = UIColor(hexString:"#171A1C")!
    var inputContainerLayerColor = UIColor(hexString:"#E4E3ED")!
    var inputContainerLayerWidth = 1.0
    var inputBackgroundColor = UIColor(hexString:"#F1F2F3")!
    var textInputCornerRadius = 5.0
    var sendGradientColors = [UIColor(hexString:"#33B1FF")!, UIColor(hexString:"#7C5BFF")!]
    var sendFont = UIFont.systemFont(ofSize: 16)
    var sendColor = UIColor(hexString:"#FFFFFF")!
    var emojiKeyboard = "face"
    var textKeyboard = "key"
    var enLimitCount = 80.0
    var zhLimitCount = 30.0
    var emojiOperationColor = UIColor(hexString:"#F9FAFA")!
}
