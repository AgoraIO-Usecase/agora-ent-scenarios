//
//  VoiceRoomBusinessApi.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/29.
//

import Foundation

public enum VoiceRoomBusinessApi {
    case login(Void)

    // MARK: - room api

    case fetchRoomList(cursor: String, pageSize: Int, type: Int?) // String cursor(上次请求游标) Int pageSize
    case createRoom(Void)
    case fetchRoomInfo(roomId: String) // String roomId
    case deleteRoom(roomId: String) // String roomId
    case modifyRoomInfo(roomId: String) // String roomId
    case fetchRoomMembers(roomId: String, cursor: String, pageSize: Int) // String roomId,String cursor
    case validatePassWord(roomId: String) /// {roomId}/validPassword
    case joinRoom(roomId: String) // String roomId
    case leaveRoom(roomId: String) // String roomId
    case kickUser(roomId: String) // String roomId
    case fetchGiftContribute(roomId: String) // String roomId,String cursor
    case giftTo(roomId: String) // String roomId

    // MARK: - mic api

    case fetchApplyMembers(roomId: String, cursor: String, pageSize: Int) // String roomId,String cursor
    case submitApply(roomId: String) // String roomId
    case cancelApply(roomId: String) // String roomId
    case agreeApply(roomId: String) // String roomId
    case refuseApply(roomId: String) // String roomId
    case fetchMicsInfo(roomId: String) // String roomId
    case closeMic(roomId: String) // String roomId
    case cancelCloseMic(roomId: String, index: Int) // String roomId
    case leaveMic(roomId: String, index: Int) // String roomId
    case muteMic(roomId: String) // String roomId
    case unmuteMic(roomId: String, index: Int) // String roomId
    case exchangeMic(roomId: String) // String roomId
    case kickMic(roomId: String) // String roomId
    case lockMic(roomId: String) // String roomId
    case unlockMic(roomId: String, index: Int) // String roomId
    case inviteUserToMic(roomId: String) // String roomId
    case agreeInvite(roomId: String) // String roomId
    case refuseInvite(roomId: String) // String roomId
}
