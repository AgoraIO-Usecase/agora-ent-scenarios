//
//  AUIError.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation

public enum AUICommonError {
    case unknown      //未知错误
    case rtcError(Int32)    //rtc错误
    case rtmError(Int32)    //rtm错误
    case rtmNotPresence   //rtm presence错误
    case httpError(Int, String)  //http错误
    case networkParseFail   //http响应解析错误
    case missmatchRoomConfig  //找不到对应房间token信息
    case micSeatNotIdle   //麦位不空闲
    case micSeatAlreadyEnter   //已经上麦过了
    case userNoEnterSeat   //观众未上麦
    case chooseSongAlreadyExist   //歌曲已经选择过了
    case chooseSongNotExist   //歌曲已经选择过了
    case choristerAlreadyExist   //合唱用户已存在
    case choristerNotExist    //合唱用户不存在
    case noPermission   //无权限
    case chooseSongIsFail   //选择歌曲失败
    case noResponse    //无响应
    
    public func toNSError() -> NSError {
        func createError(code: Int = -1, msg: String) -> NSError {
            return NSError(domain: "AUIKit Error", code: Int(code), userInfo: [ NSLocalizedDescriptionKey : msg])
        }
        switch self {
        case .httpError(let error, let msg):
            if error == 10001 {
                return createError(code: Int(error), msg: "the room has been destroyed")
            }
            return createError(code: Int(error), msg: msg)
        case .rtcError(let error):
            return createError(code: Int(error), msg: "rtc operation fail: \(error)")
        case .rtmError(let error):
            return createError(code: Int(error), msg: "rtm error: \(error)")
        case .rtmNotPresence:
            return createError(msg: "rtm fail: not presence")
        case .networkParseFail:
            return createError(msg: "http parse fail")
        case .missmatchRoomConfig:
            return createError(msg: "room config missmatch")
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
        case .noResponse:
            return createError(msg: "no response")
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
