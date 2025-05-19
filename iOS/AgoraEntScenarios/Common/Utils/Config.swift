//
//  Config.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/4.
//

import Foundation

let chatViewWidth = UIScreen.main.bounds.size.width * (287 / 375.0)

public enum UserInfo {
    public static var userId: String {
        return VLUserCenter.user.id

//        let id = UserDefaults.standard.integer(forKey: "UserId")
//        if id > 0 {
//            return UInt(id)
//        }
//        let user = UInt(arc4random_uniform(8999999) + 1000000)
//        UserDefaults.standard.set(user, forKey: "UserId")
//        UserDefaults.standard.synchronize()
//        return user
    }
//    static var uid: String {
//        "\(userId)"
//    }
}

