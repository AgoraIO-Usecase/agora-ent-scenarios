//
//  BundleExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

public enum AUIBundleType: Int {
    case chat
    case karaoke
    case live
    case gift
}

public let IMResourceBundle = Bundle(path: Bundle.main.path(forResource: "ChatResource", ofType: "bundle") ?? "") ?? Bundle.main

public let KaraokeResourceBundle = Bundle(path: Bundle.main.path(forResource: "KaraokeResource", ofType: "bundle") ?? "") ?? Bundle.main

public let LiveResourceBundle = Bundle(path: Bundle.main.path(forResource: "LiveResource", ofType: "bundle") ?? "") ?? Bundle.main

public let GiftBundle = Bundle(path: Bundle.main.path(forResource: "Gift", ofType: "bundle") ?? "") ?? Bundle.main

public extension Bundle {
    class var chatBundle: Bundle { IMResourceBundle }
    class var karaokeRoomBundle: Bundle { KaraokeResourceBundle }
    class var liveRoomBundle: Bundle { LiveResourceBundle }
    class var giftBundle: Bundle { GiftBundle }
    
    class func bundle(for type: AUIBundleType) -> Bundle {
        switch type {
        case .chat:
            return Bundle.chatBundle
        case .karaoke:
            return Bundle.karaokeRoomBundle
        case .live:
            return Bundle.liveRoomBundle
        case .gift:
            return Bundle.giftBundle
        }
    }
    
}
