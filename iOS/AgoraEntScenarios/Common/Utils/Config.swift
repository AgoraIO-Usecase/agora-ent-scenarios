//
//  Config.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/4.
//

import Foundation

let SYNC_MANAGER_PARAM_KEY_ID = "defaultChannel"
/// 子房间名
let SYNC_COLLECTION_SUB_ROOM = "SubRoom"

let SYNC_MANAGER_PARAM_KEY_APPID = "appId"
/// 礼物
let SYNC_MANAGER_GIFT_INFO = "giftInfo"
/// PK游戏信息
let SYNC_MANAGER_GAME_APPLY_INFO = "gameApplyInfo"
/// 观众游戏信息
let SYNC_MANAGER_GAME_INFO = "gameInfo"
/// pk信息
let SYNC_MANAGER_PK_INFO = "pkInfo"
/// 用户信息
let SYNC_MANAGER_AGORA_VOICE_USERS = "agoraVoiceUsers"
/// 夜店用户信息
let SYNC_MANAGER_AGORA_CLUB_USERS = "agoraClubUsers"
/// 消息信息
let SYNC_SCENE_ROOM_MESSAGE_INFO = "messageInfo"
/// 房间内用户列表
let SYNC_SCENE_ROOM_USER_COLLECTION = "userCollection"
/// 商品信息
let SYNC_SCENE_SHOPPING_INFO = "shoppingInfo"

let APP_SCENARIO: Int = 100
let SERVICE_TYPE: Int = 12

let chatViewWidth = ScreenWidth * (287 / 375.0)

enum UserInfo {
    static var userId: String {
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
