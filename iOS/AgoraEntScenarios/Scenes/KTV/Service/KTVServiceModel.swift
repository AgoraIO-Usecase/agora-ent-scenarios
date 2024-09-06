//
//  KTVServiceModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import Foundation
import RTMSyncManager
import AgoraCommon


func toKtvLocalize(_ str: String) -> String {
    return str.toSceneLocalization(with: "KtvResource") as String
}

func createError(code: Int = -1, msg: String) -> NSError {
    return NSError(domain: "AUIKit Error", code: Int(code), userInfo: [ NSLocalizedDescriptionKey : "\(msg)\(code)"])
}

public enum KTVServiceError {
    case createRoomFail(Int)   //创建房间失败
    case joinRoomFail(Int)     //加入房间失败
    case enterSeatFail(Int)    //上麦失败
    case leaveSeatFail(Int)    //下麦失败
    case kickSeatFail(Int)     //踢下麦失败
//    case getSongListFail(Int)  //获取歌曲失败失败
    case chooseSongFail(Int)   //点歌失败
    case removeSongFail(Int)   //删除歌曲失败
    case pinSongFail(Int)      //置顶歌曲失败
    case switchSongFail(Int)   //切歌失败
    
    public func toNSError() -> NSError {
        switch self {
        case .createRoomFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_create_room_fail_toast"))
        case .joinRoomFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_join_room_fail_toast"))
        case .enterSeatFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_enter_seat_fail_toast"))
        case .leaveSeatFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_leave_seat_fail_toast"))
        case .kickSeatFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_kick_seat_fail_toast"))
        case .chooseSongFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_choose_song_fail_toast"))
        case .removeSongFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_remove_song_fail_toast"))
        case .pinSongFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_pin_song_fail_toast"))
        case .switchSongFail(let code):
            return createError(code: code, msg: toKtvLocalize("ktv_switch_song_fail_toast"))
        }
    }
}

public enum KTVCommonError {
    case unknown      //未知错误
    case micSeatNotIdle   //麦位不空闲
    case micSeatAlreadyEnter   //已经上麦过了
    case userNoEnterSeat   //观众未上麦
    case chooseSongAlreadyExist   //歌曲已经选择过了
    case chooseSongNotExist   //歌曲已经选择过了
    case choristerAlreadyExist   //合唱用户已存在
    case choristerNotExist    //合唱用户不存在
    case noPermission   //无权限
    case chooseSongIsFail   //选择歌曲失败
    case currentSongNotFirst  //预期要变更为播放状态的歌曲不是第一首
    
    public func toNSError() -> NSError {
        switch self {
        case .micSeatNotIdle:
            return createError(msg: "mic seat not idle")
        case .micSeatAlreadyEnter:
            return createError(msg: "user already enter seat")
        case .userNoEnterSeat:
            return createError(msg: "user not enter seat")
        case .chooseSongAlreadyExist:
            return createError(msg: "already choose song")
        case .chooseSongNotExist:
            return createError(msg: "song not exist")
        case .choristerAlreadyExist:
            return createError(msg: "chorister already exist")
        case .choristerNotExist:
            return createError(msg: "chorister not exist")
        case .noPermission:
            return createError(msg: "no permission")
        case .chooseSongIsFail:
            return createError(msg: "choost song model fail")
        case .currentSongNotFirst:
            return createError(msg: "current song not first")
        default:
            return createError(msg: "unknown error")
        }
    }
}

extension NSError {
    static func auiError(_ description: String) -> NSError {
        return NSError(domain: "AUIKit Error",
                       code: -1,
                       userInfo: [ NSLocalizedDescriptionKey : description])
    }
}


enum AUIMicSeatCmd: String {
    case leaveSeatCmd = "leaveSeatCmd"    //下麦
    case enterSeatCmd = "enterSeatCmd"    //上麦
    case muteAudioCmd = "muteAudioCmd"    //mute/unmute audio
    case muteVideoCmd = "muteVideoCmd"    //mute/unmute video
    case kickSeatCmd = "kickSeatCmd"      //把某一个上麦用户踢下麦
}

enum AUIMusicCmd: String {
    case chooseSongCmd = "chooseSongCmd"   //添加一首歌
    case removeSongCmd = "removeSongCmd"   //移除一首歌
    case pinSongCmd = "pinSongCmd"       //置顶一首歌
    case updatePlayStatusCmd = "updatePlayStatusCmd"   //更新歌曲播放状态
    case removedUserSongs = "removedUserSongsCmd"   //移除指定用户所有歌曲
}

enum AUIChorusCmd: String {
    case joinCmd = "joinChorusCmd" //加入合唱
    case leaveCmd = "leaveChorusCmd" //退出合唱
    case kickAllCmd = "kickAllOutOfChorusCmd"  //移除所有合唱
    case kickUserCmd = "KickUserOutOfChorusCmd"   //踢出指定用户出合唱列表
}

@objcMembers
class KTVCreateRoomInfo: NSObject {
    var belCanto: String?
    var icon: String = ""
    var isPrivate: NSNumber?
    var name: String = ""
    var password: String?
    var soundEffect: String?
}

@objcMembers
class KTVChooseSongInputModel: NSObject {
    var songName: String?
    var songNo: String?
    var singer: String?
    var imageUrl: String?
}

extension AUIUserThumbnailInfo {
    @objc static func createUserInfo() -> AUIUserThumbnailInfo {
        let user = VLUserCenter.user
        let owner = AUIUserThumbnailInfo()
        owner.userName = user.name
        owner.userId = user.id
        owner.userAvatar = user.headUrl
        
        return owner
    }
}

/// 合唱者模型
@objcMembers 
open class KTVChoristerModel: NSObject {
    var userId: String = ""
    var chorusSongNo: String?          //合唱者演唱歌曲
 
    open override func isEqual(_ object: Any?) -> Bool {
        if let other = object as? KTVChoristerModel {
            return self.userId == other.userId && self.chorusSongNo == other.chorusSongNo
        }
        return false
    }
    
    open override var hash: Int {
        return userId.hashValue ^ (chorusSongNo?.hashValue ?? 0)
    }
    
    open override var description: String {
        return "\(userId)-\(chorusSongNo ?? "")"
    }
}
