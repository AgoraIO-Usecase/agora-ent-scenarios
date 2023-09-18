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
    public var roomId: String = ""
    public var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    var objectId: String = ""
    
    func getRoomId() ->String {
        return "\(userId)_\(createdAt)"
    }
    
    func bgImage() ->UIImage? {
        let uid = UInt(userId) ?? 0
        let image = UIImage.sceneImage(name: "user_bg\(uid % 9 + 1)")
        return image
    }
}

class Pure1v1CalleeTokenConfig: NSObject {
    var callerRoomId: String?   //主叫频道的token
    var callerToken: String?    //主叫频道的token
    var isAccept: Bool?     //是否接受主叫呼叫
    
    func isValide(roomId: String) -> Bool {
        guard let _ = callerToken, roomId == callerRoomId, isAccept == true else {return false}
        return true
    }
}
