//
//  AUIJukeBoxCellDataProtocol.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/10.
//

import Foundation

public protocol AUIJukeBoxItemDataProtocol: NSObjectProtocol {
    var songCode: String {get}
    var avatarUrl: String {get}   //头像
    var title: String {get}    //主标题
    var subTitle: String? {get}  //副标题
}

public protocol AUIJukeBoxItemSelectedDataProtocol: AUIJukeBoxItemDataProtocol {
    var isPlaying: Bool {get} //是否在播放
    var userId: String? {get}  //歌曲拥有者
    var switchEnable: Bool { get set} // 是否可以切歌
}


extension AUIMusicModel: AUIJukeBoxItemDataProtocol {
    public var avatarUrl: String {
        return self.poster
    }
    
    public var title: String {
        return self.name
    }
    
    public var subTitle: String? {
        return self.singer
    }
}



extension AUIChooseMusicModel: AUIJukeBoxItemSelectedDataProtocol {
    static let SwitchEnableKey = UnsafeRawPointer(UnsafeMutablePointer<Int8>.allocate(capacity: 1))
    
    public var switchEnable: Bool {
        set {
            objc_setAssociatedObject(self, AUIChooseMusicModel.SwitchEnableKey, newValue, .OBJC_ASSOCIATION_ASSIGN)
        }
        
        get {
            if let value = objc_getAssociatedObject(self, AUIChooseMusicModel.SwitchEnableKey) as? Bool {
                return value
            }
            return false
        }
    }
    
    static func modelPropertyBlacklist() -> [Any] {
        return ["switchEnable"]
    }
    
    public var userId: String? {
        return self.owner?.userId
    }
    
    public var isPlaying: Bool {
        return self.playStatus == .playing
    }
    
}
