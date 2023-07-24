//
//  Pure1v1Model.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation

@objcMembers
public class Pure1v1UserInfo: NSObject {
    public var userId: String = ""
    public var userName: String = ""
    public var avatar: String = ""
    
    var objectId: String = ""
    
    func getRoomId() ->String {
        return "\(userId)"
    }
    
    func bgImage() ->UIImage? {
        let uid = UInt(userId) ?? 0
        let image = UIImage.sceneImage(name: "user_bg\(uid % 3 + 1)")
        return image
    }
}
