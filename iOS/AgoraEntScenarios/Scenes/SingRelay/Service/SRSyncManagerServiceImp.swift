//
//  SRSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

import Foundation
import YYCategories
import SVProgressHUD

private let rSceneId = "scene_singrelay_3.5.0"

/// 座位信息
private let SYNC_MANAGER_SEAT_INFO = "seat_info"
// 选歌
private let SYNC_MANAGER_CHOOSE_SONG_INFO = "choose_song"
//接唱
private let SYNC_MANAGER_UPDATE_GAME_STATE = "sing_battle_game_info"

private func agoraAssert(_ message: String) {
    agoraAssert(false, message)
}

private func agoraAssert(_ condition: Bool, _ message: String) {
    SRLog.error(text: message, tag: "SRService")
    #if DEBUG
//    assert(condition, message)
    #else
    #endif
}

private func agoraPrint(_ message: String) {
//    #if DEBUG
    SRLog.info(text: message, tag: "SRService")
//    #else
//    #endif
}

private func _showLoadingIfNeed() {
    SVProgressHUD.show()
}

private func _hideLoadingIfNeed() {
    SVProgressHUD.dismiss()
}

private func mapConvert(model: NSObject) ->[String: Any] {
    let params = model.yy_modelToJSONObject() as! [String: Any]
    //TODO: convert to swift map to fix SyncManager parse NSDictionary bugs
    var swiftParams = [String: Any]()
    params.forEach { (key: String, value: Any) in
        swiftParams[key] = value
    }
    return swiftParams
}

@objc class SRSyncManagerServiceImp: NSObject, SRServiceProtocol {

    private var roomList: [VLSRRoomListModel]?
    private var userList: [VLLoginModel] = .init()
    private var seatMap: [String: VLSRRoomSeatModel] = .init()
    private var songList: [VLSRRoomSelSongModel] = .init()

    private var userListCountDidChanged: ((UInt) -> Void)?
    private var userDidChanged: ((SRSubscribe, VLLoginModel) -> Void)?
    private var seatListDidChanged: ((SRSubscribe, VLSRRoomSeatModel) -> Void)?
    private var roomStatusDidChanged: ((SRSubscribe, VLSRRoomListModel) -> Void)?
    private var chooseSongDidChanged: ((SRSubscribe, VLSRRoomSelSongModel, [VLSRRoomSelSongModel]) -> Void)?
//    private var singingScoreDidChanged: ((Double) -> Void)?
    private var networkDidChanged: ((SRServiceNetworkStatus) -> Void)?
    private var roomExpiredDidChanged: (() -> Void)?

    private var roomNo: String?
    
    private var room: VLSRRoomListModel? {
        return self.roomList?.filter({ $0.roomNo == self.roomNo }).first
    }

    private var syncUtilsInited: Bool = false

    // MARK: Private

    private func getRoomNo() -> String {
        guard let roomNo = roomNo else {
            agoraAssert("roomNo == nil")
            return ""
        }

        return roomNo
    }
    
    private func _unsubscribeAll() {
        guard let channelName = roomNo else {
            return
        }
        agoraPrint("imp all unsubscribe...")
        SyncUtil
            .scene(id: channelName)?
            .unsubscribeScene()
        
        userListCountDidChanged = nil
        seatListDidChanged = nil
        roomStatusDidChanged = nil
        chooseSongDidChanged = nil
//        singingScoreDidChanged = nil
        networkDidChanged = nil
        roomExpiredDidChanged = nil
    }
    
    private func _checkRoomExpire() {
        guard let room = self.room else { return }
        
        let currentTs = Int64(Date().timeIntervalSince1970 * 1000)
        let expiredDuration = 20 * 60 * 1000
//        agoraPrint("checkRoomExpire: \(currentTs - room.createdAt) / \(expiredDuration)")
        guard currentTs - room.createdAt > expiredDuration else { return }
        
        guard let callback = self.roomExpiredDidChanged else {
            return
        }
        callback()
    }

    private func initScene(completion: @escaping (NSError?) -> Void) {
        if syncUtilsInited {
            completion(nil)
            return
        }

        SyncUtil.initSyncManager(sceneId: rSceneId) {
        }
        
        SyncUtil.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            defer {
                completion(state == .open ? nil : NSError(domain: "network error", code: 1000))
            }
            
            agoraPrint("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.networkDidChanged?(SRServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                self._seatListReloadIfNeed()
                self._getUserInfo { err, list in
                    self.userListCountDidChanged?(UInt(list?.count ?? 0))
                }
                return
            }
            
            self.syncUtilsInited = true
          //  completion()
        }
    }
    
    // MARK: protocol method
    
    // MARK: room info
    func getRoomList(with page: UInt, completion: @escaping (Error?, [VLSRRoomListModel]?) -> Void) {
        initScene { [weak self] error in
            if let error = error  {
                _hideLoadingIfNeed()
                completion(error, nil)
                return
            }
            guard page < 1 else {
                completion(nil, [])
                return
            }
            
            SyncUtil.fetchAll { results in
                agoraPrint("result == \(results.compactMap { $0.toJson() })")
                guard let self = self else {
                    return
                }

                let dataArray = results.map({ info in
                    return VLSRRoomListModel.yy_model(with: info.toJson()!.toDictionary())!
                })
                self.roomList = dataArray.sorted(by: { ($0.updatedAt > 0 ? $0.updatedAt : $0.createdAt) > ($1.updatedAt > 0 ? $1.updatedAt : $0.createdAt) })
                completion(nil, self.roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }

    func createRoom(with inputModel: SRCreateRoomInputModel,
                    completion: @escaping (Error?, SRCreateRoomOutputModel?) -> Void)
    {
        let roomInfo = VLSRRoomListModel() // LiveRoomInfo(roomName: inputModel.name)
//        roomInfo.id = VLUserCenter.user.id//NSString.withUUID().md5() ?? ""
        roomInfo.name = inputModel.name
        roomInfo.isPrivate = inputModel.isPrivate == true
        roomInfo.password = inputModel.password
        roomInfo.creatorNo = VLUserCenter.user.id
        roomInfo.roomNo = "\(arc4random_uniform(899999) + 100000)" // roomInfo.id
        roomInfo.bgOption = 0
        roomInfo.roomPeopleNum = "0"
        roomInfo.icon = inputModel.icon
        roomInfo.createdAt = Int64(Date().timeIntervalSince1970 * 1000)

        let params = mapConvert(model: roomInfo)

        _showLoadingIfNeed()
        let date = Date()
        initScene { [weak self] error in
            if let error = error  {
                _hideLoadingIfNeed()
                completion(error, nil)
                return
            }
            agoraPrint("createRoom initScene cost: \(-date.timeIntervalSinceNow * 1000) ms")
            SyncUtil.joinScene(id: roomInfo.roomNo ?? "",
                               userId: roomInfo.creator ?? "",
                               isOwner: true,
                               property: params) { result in
                agoraPrint("createRoom joinScene cost: \(-date.timeIntervalSinceNow * 1000) ms")
                let channelName = result.getPropertyWith(key: "roomNo", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creatorNo", type: String.self) as? String ?? ""
                self?.roomNo = channelName
                
                let playerRTCUid = UserInfo.userId//VLUserCenter.user.agoraPlayerRTCUid;
                var tokenMap1:[Int: String] = [:], tokenMap2:[Int: String] = [:]
                
                let dispatchGroup = DispatchGroup()
                dispatchGroup.enter()
                NetworkManager.shared.generateTokens(channelName: channelName ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    tokenMap1 = tokenMap
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateTokens(channelName: "\(channelName ?? "")_ex",
                                                     uid: "\(playerRTCUid)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc]) { tokenMap in
                    tokenMap2 = tokenMap
                    dispatchGroup.leave()
                }
                
                dispatchGroup.notify(queue: .main){
                    agoraPrint("createRoom get token cost: \(-date.timeIntervalSinceNow * 1000) ms")
                    guard let self = self,
                          let rtcToken = tokenMap1[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap1[NetworkManager.AgoraTokenType.rtm.rawValue],
                          let rtcPlayerToken = tokenMap2[NetworkManager.AgoraTokenType.rtc.rawValue]
                    else {
                        agoraAssert(tokenMap1.count == 2, "rtcToken == nil || rtmToken == nil")
                        agoraAssert(tokenMap2.count == 1, "playerRtcToken == nil")
                        _hideLoadingIfNeed()
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.id == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    VLUserCenter.user.agoraPlayerRTCToken = rtcPlayerToken
                    self.roomList?.append(roomInfo)
                    self._autoOnSeatIfNeed { seatArray in
                        agoraPrint("createRoom _autoOnSeatIfNeed cost: \(-date.timeIntervalSinceNow * 1000) ms")
                        _hideLoadingIfNeed()
                        let output = SRCreateRoomOutputModel()
                        output.name = inputModel.name
                        output.roomNo = roomInfo.roomNo ?? ""
                        output.seatsArray = seatArray
                        completion(nil, output)
                    }
                    self._addUserIfNeed()
//                    self._subscribeChooseSong {}
                }
            } fail: { error in
                _hideLoadingIfNeed()
                completion(error, nil)
            }
        }
    }

    func joinRoom(with inputModel: SRJoinRoomInputModel,
                  completion: @escaping (Error?, SRJoinRoomOutputModel?) -> Void)
    {
        guard let roomInfo = roomList?.filter({ $0.roomNo == inputModel.roomNo }).first else {
            agoraAssert("join Room fail")
            completion(nil, nil)
            return
        }

        let params = mapConvert(model: roomInfo)

        _showLoadingIfNeed()
        let date = Date()
        initScene { [weak self] error in
            if let error = error  {
                _hideLoadingIfNeed()
                completion(error, nil)
                return
            }
            agoraPrint("joinRoom initScene cost: \(-date.timeIntervalSinceNow * 1000) ms")
            SyncUtil.joinScene(id: roomInfo.roomNo ?? "",
                               userId: roomInfo.creator ?? "",
                               isOwner: roomInfo.creator == VLUserCenter.user.id,
                               property: params) { result in
                agoraPrint("joinRoom joinScene cost: \(-date.timeIntervalSinceNow * 1000) ms")
                let channelName = result.getPropertyWith(key: "roomNo", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creatorNo", type: String.self) as? String ?? ""
                self?.roomNo = channelName
                
                let playerRTCUid = UserInfo.userId//VLUserCenter.user.agoraPlayerRTCUid
                var tokenMap1:[Int: String] = [:], tokenMap2:[Int: String] = [:]
                
                let dispatchGroup = DispatchGroup()
                dispatchGroup.enter()
                NetworkManager.shared.generateTokens(channelName: channelName ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    tokenMap1 = tokenMap
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateTokens(channelName: "\(channelName ?? "")_ex",
                                                     uid: "\(playerRTCUid)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc]) { tokenMap in
                    tokenMap2 = tokenMap
                    dispatchGroup.leave()
                }
                
                dispatchGroup.notify(queue: .main){
                    agoraPrint("joinRoom get token cost: \(-date.timeIntervalSinceNow * 1000) ms")
                    guard let self = self,
                          let rtcToken = tokenMap1[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap1[NetworkManager.AgoraTokenType.rtm.rawValue],
                          let rtcPlayerToken = tokenMap2[NetworkManager.AgoraTokenType.rtc.rawValue]
                    else {
                        _hideLoadingIfNeed()
                        agoraAssert(tokenMap1.count == 2, "rtcToken == nil || rtmToken == nil")
                        agoraAssert(tokenMap2.count == 1, "playerRtcToken == nil")
                        completion(nil, nil)
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.id == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    VLUserCenter.user.agoraPlayerRTCToken = rtcPlayerToken
                    self._autoOnSeatIfNeed { seatArray in
                        agoraPrint("joinRoom _autoOnSeatIfNeed cost: \(-date.timeIntervalSinceNow * 1000) ms")
                        _hideLoadingIfNeed()
                        let output = SRJoinRoomOutputModel()
                        output.creatorNo = userId
                        output.seatsArray = seatArray
                        completion(nil, output)
                    }
                    self._addUserIfNeed()
//                    self._subscribeChooseSong {}
                }
            } fail: { error in
                _hideLoadingIfNeed()
                completion(error, nil)
            }
        }
    }
    
    func leaveRoom(completion: @escaping (Error?) -> Void) {
        guard let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first else {
            agoraAssert("leaveRoom channelName = nil")
            completion(nil)
            return
        }
        
        //current user is room owner, remove room
        if roomInfo.creatorNo == VLUserCenter.user.id {
            _removeRoom(completion: completion)
            return
        }
        _leaveRoom(completion: completion)
    }

    func changeMVCover(with inputModel: SRChangeMVCoverInputModel,
                       completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo,
              let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first
        else {
            agoraAssert("channelName = nil")
            completion(nil)
            return
        }
        roomInfo.bgOption = Int(inputModel.mvIndex)
//        let objectId = roomInfo.objectId
        let objectId = channelName
        var params = mapConvert(model: roomInfo)
        params["objectId"] = objectId
        agoraPrint("imp room update mv... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { object in
                guard let _ = object.first else {
                    agoraPrint("udpate mv fail")
                    return
                }
                agoraPrint("imp room update mv success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp room update mv fail \(error.message)...")
                completion(error)
            })
        completion(nil)
    }
    
    // MARK: mic seat
    func enterSeat(with inputModel: SROnSeatInputModel,
                   completion: @escaping (Error?) -> Void) {
        let seatInfo = _getUserSeatInfo(seatIndex: Int(inputModel.seatIndex))
        _addSeatInfo(seatInfo: seatInfo,
                     finished: completion)
    }

    func leaveSeat(with inputModel: SROutSeatInputModel,
                   completion: @escaping (Error?) -> Void) {
        let seatInfo = seatMap["\(inputModel.seatIndex)"]!
        _removeSeat(seatInfo: seatInfo) { error in
        }
        
        //remove current user's choose song
        _removeAllUserChooseSong(userNo: seatInfo.userNo ?? "")
        completion(nil)
    }
    
    func updateSeatAudioMuteStatus(with muted: Bool,
                                   completion: @escaping (Error?) -> Void) {
        guard let seatInfo = self.seatMap
            .filter({ $0.value.userNo == VLUserCenter.user.id })
            .first?.value else {
            agoraAssert("mute seat not found")
            completion(nil)
            return
        }
        
        seatInfo.isAudioMuted = muted ? 1 : 0
        _updateSeat(seatInfo: seatInfo,
                    finished: completion)
    }

    func updateSeatVideoMuteStatus(with muted: Bool,
                                   completion: @escaping (Error?) -> Void) {
        guard let seatInfo = self.seatMap
            .filter({ $0.value.userNo == VLUserCenter.user.id })
            .first?.value else {
            agoraAssert("open video seat not found")
            completion(nil)
            return
        }
        
        seatInfo.isVideoMuted = muted ? 1 : 0
        _updateSeat(seatInfo: seatInfo,
                    finished: completion)
    }

    
    // MARK: choose songs
    func removeSong(with inputModel: SRRemoveSongInputModel,
                    completion: @escaping (Error?) -> Void) {
        _removeChooseSong(songId: inputModel.objectId,
                          completion: completion)
    }

    func getChoosedSongsList(completion: @escaping (Error?, [VLSRRoomSelSongModel]?) -> Void) {
        _getChooseSongInfo(finished: completion)
    }

    func joinChorus(with inputModel: SRJoinChorusInputModel,
                        completion: @escaping (Error?) -> Void) {
        guard let topSong = self.songList.filter({ $0.songNo == inputModel.songNo}).first else {
            agoraAssert("join Chorus fail")
            completion(nil)
            return
        }
        //TODO: _markSeatToPlaying without callback
        _markSeatChoursStatus(songCode: topSong.chorusSongId(),  completion: completion)
    }
    
    func coSingerLeaveChorus(completion: @escaping (Error?) -> Void) {
        //TODO: _markSeatToPlaying without callback
        _markSeatChoursStatus(songCode: "", completion: completion)
    }

    func markSongDidPlay(with inputModel: VLSRRoomSelSongModel,
                         completion: @escaping (Error?) -> Void) {
        inputModel.status = .playing
        _updateChooseSong(songInfo: inputModel, finished: completion)
    }

    func chooseSong(with inputModel: SRChooseSongInputModel,
                    completion: @escaping (Error?) -> Void)
    {
        //添加歌曲前先判断
        var flag = false
        _ =  songList.compactMap { model in
            if model.songNo == inputModel.songNo {
                agoraPrint("The song has been added")
                flag = true
                return
            }
        }
        if flag {return}
        
        let songInfo = VLSRRoomSelSongModel()
       // songInfo.isChorus = inputModel.isChorus
        songInfo.songName = inputModel.songName
        songInfo.songNo = inputModel.songNo
//        songInfo.songUrl = inputModel.songUrl
        songInfo.imageUrl = inputModel.imageUrl
        songInfo.singer = inputModel.singer
        songInfo.status = .idle
        /// 是谁点的歌
        songInfo.userNo = VLUserCenter.user.id
//        songInfo.userId = UserInfo.userId
        /// 点歌人昵称
        songInfo.name = VLUserCenter.user.name

        _addChooseSongInfo(songInfo: songInfo) { error in
            // TODO(wushengtao): fetch all list can not be changed if immediately invoke
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.2) {
                completion(error)
            }
        }
    }
    
    func pinSong(with inputModel: SRMakeSongTopInputModel,
                 completion: @escaping (Error?) -> Void) {
//        assert(false)
        guard let topSong = songList.first,
              let song = songList.filter({ $0.objectId == inputModel.objectId }).first
        else {
            agoraAssert("make song to top not found! \(inputModel.songNo)")
            completion(nil)
            return
        }

        // mark input song to top
        song.pinAt = Int64(Date().timeIntervalSince1970 * 1000)

        //if top song is playing status, keep it always on top(_sortChooseSongList)
        if topSong.objectId != song.objectId, topSong.status != .playing {
            topSong.pinAt = Int64(Date().timeIntervalSince1970 * 1000)
            _updateChooseSong(songInfo: topSong) { error in
            }
        }

        // mark current playing song to top
        _updateChooseSong(songInfo: song) { error in
            completion(error)
        }
    }
    
    
    //MARK: about lyrics
    func enterSoloMode() {
        _markSoloSongIfNeed()
    }

    //MARK: subscribe
    func subscribeUserListCountChanged(with changedBlock: @escaping (UInt) -> Void) {
//        _unsubscribeAll()
        userListCountDidChanged = changedBlock
        _subscribeOnlineUsers {
        }
    }
    
    func subscribeUserChanged(with changedBlock: @escaping (SRSubscribe, VLLoginModel) -> Void) {
        userDidChanged = changedBlock
    }

    func subscribeSeatListChanged(with changedBlock: @escaping (SRSubscribe, VLSRRoomSeatModel) -> Void) {
        seatListDidChanged = changedBlock
        _subscribeSeats {
        }
    }
    
    func subscribeRoomStatusChanged(with changedBlock: @escaping (SRSubscribe, VLSRRoomListModel) -> Void) {
        roomStatusDidChanged = changedBlock

        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp room subscribe...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: "",
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           guard let jsonStr = object.toJson(),
                                 let model = VLSRRoomListModel.yy_model(withJSON: jsonStr),
                                 model.roomNo == channelName
                           else {
                               return
                           }
                           agoraPrint("imp room subscribe onUpdated...")
                           self?.roomStatusDidChanged?(.updated, model)
                       }, onDeleted: { [weak self] object in
                           guard let model = self?.roomList?.filter({ $0.roomNo == object.getId()}).first,
                                 model.roomNo == channelName
                           else {
                               return
                           }
                           agoraPrint("imp room subscribe onDeleted...")
                           self?.roomStatusDidChanged?(.deleted, model)
                       }, onSubscribed: {}, fail: { error in
                       })
    }

    func subscribeChooseSongChanged(with changedBlock: @escaping (SRSubscribe, VLSRRoomSelSongModel, [VLSRRoomSelSongModel]) -> Void) {
        chooseSongDidChanged = changedBlock
        _subscribeChooseSong {
        }
    }
    
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (SRServiceNetworkStatus) -> Void) {
        networkDidChanged = changedBlock
    }
    
    func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void) {
        roomExpiredDidChanged = changedBlock
        Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { [weak self] timer in
            guard let self = self else { return }
            
            self._checkRoomExpire()
            if self.roomExpiredDidChanged == nil {
                timer.invalidate()
            }
        }
        
        DispatchQueue.main.async {
            self._checkRoomExpire()
        }
    }
    
    func unsubscribeAll() {
        _unsubscribeAll()
    }
}


//MARK: room operation
extension SRSyncManagerServiceImp {
    private func _leaveRoom(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            completion(nil)
            return
        }
        _removeUser { error in
        }

        //leave if enter seat
        if let seat = seatMap.filter({ $0.value.userNo == VLUserCenter.user.id }).first?.value {
            _removeSeat(seatInfo: seat) { error in
            }
        }
        
        //remove current user's choose song
        _removeAllUserChooseSong()

        _unsubscribeAll()
        SyncUtil.leaveScene(id: channelName)
        roomNo = nil
        completion(nil)
    }

    private func _removeRoom(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            completion(nil)
            return
        }
        _unsubscribeAll()
        SyncUtil.scene(id: channelName)?.deleteScenes()
        roomNo = nil
        completion(nil)
    }
}

// MARK: User operation
extension SRSyncManagerServiceImp {
    private func _addUserIfNeed() {
//        _subscribeOnlineUsers {}
        _getUserInfo { error, userList in
            // current user already add
            if self.userList.contains(where: { $0.id == VLUserCenter.user.id }) {
                return
            }
            self._addUserInfo {
//                self._getUserInfo { error, userList in
//                }
            }
        }
    }

    private func _getUserInfo(finished: @escaping (Error?, [VLLoginModel]?) -> Void) {
        guard let channelName = roomNo else {
//            agoraAssert("channelName = nil")
            finished(nil, nil)
            return
        }
        agoraPrint("imp user get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp user get success...")
                let users = list.compactMap({ VLLoginModel.yy_model(withJSON: $0.toJson()!)! })
//            guard !users.isEmpty else { return }
                self?.userList = users
                self?._updateUserCount(completion: { error in

                })
                finished(nil, users)
            }, fail: { error in
                agoraPrint("imp user get fail :\(error.message)...")
                agoraPrint("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _addUserInfo(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            agoraPrint("addUserInfo channelName = nil")
            return
        }
        agoraPrint("imp user add ...")
        let model = VLUserCenter.user

        let params = mapConvert(model: model)
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp user add success...")
                finished()
            }, fail: { error in
                agoraPrint("imp user add fail :\(error.message)...")
                agoraPrint(error.message)
                finished()
            })
    }

    private func _subscribeOnlineUsers(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp user subscribe...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           agoraPrint("imp user subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = VLLoginModel.yy_model(withJSON: jsonStr)
                           else {
                               return
                           }
                           if self.userList.contains(where: { $0.id == model.id }) {
                               self.userDidChanged?(.updated, model)
                               return
                           }
                           
                           self.userList.append(model)
                           agoraPrint("imp user subscribe onUpdated2... \(self.userList.count)")
                           self.userDidChanged?(.created, model)
                           self._updateUserCount { error in
                           }
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp user subscribe onDeleted...")
                           guard let self = self, let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) else {
                               return
                           }
                           let model = self.userList[index]
                           self.userDidChanged?(.deleted, model)
                           self.userList.remove(at: index)
                           self._updateUserCount { error in
                           }
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                           finished()
                       }, fail: { error in
                           agoraPrint("imp user subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                           finished()
                       })
    }

    private func _removeUser(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("_removeUser channelName = nil")
            completion(nil)
            return
        }
        guard let objectId = userList.filter({ $0.id == VLUserCenter.user.id }).first?.objectId else {
//            agoraAssert("_removeUser objectId = nil")
            completion(nil)
            return
        }
        agoraPrint("imp user delete... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .document(id: objectId)
            .delete(success: {_ in
                agoraPrint("imp user delete success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp user delete fail \(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }

    private func _updateUserCount(completion: @escaping (Error?) -> Void) {
        _updateUserCount(with: userList.count)
        
        //user count == 0, notify to leave room except room owner
        guard userList.count == 0,
           let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first,
            roomInfo.creatorNo != VLUserCenter.user.id else {
            return
        }
        
        roomStatusDidChanged?(.deleted, roomInfo)
    }

    private func _updateUserCount(with count: Int) {
        guard let channelName = roomNo,
              let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first,
              roomInfo.creatorNo == VLUserCenter.user.id
        else {
//            assert(false, "channelName = nil")
            agoraPrint("updateUserCount channelName = nil")
            userListCountDidChanged?(UInt(count))
            return
        }
        let roomPeopleNum = "\(count)"
        if roomPeopleNum == roomInfo.roomPeopleNum {
            return
        }
        roomInfo.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        roomInfo.roomPeopleNum = roomPeopleNum
        var params = mapConvert(model: roomInfo)
        let objectId = channelName
        agoraPrint("imp room update user count... [\(objectId)]")
        params["objectId"] = objectId
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { obj in
                agoraPrint("imp room update user count success...")
            }, fail: { error in
                agoraPrint("imp room update user count fail \(error.message)...")
            })

        userListCountDidChanged?(UInt(count))
    }
}

// MARK: 抢唱的增删改查

extension SRSyncManagerServiceImp {
    
    func innerSingRelayInfo(_ completion: @escaping (Error?, SingRelayModel?) -> Void) {
        guard let channelName = roomNo else {
            assertionFailure("channelName is nil")
            completion(nil, nil)
            return
        }
        
        agoraPrint("Getting sing battle game info...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_UPDATE_GAME_STATE)
            .get(success: { list in
                agoraPrint("Got sing battle game info success...")
                let models = list.compactMap({ SingRelayModel.yy_model(withJSON: $0.toJson()!) })
                
                completion(nil, models.first)
            }, fail: { error in
                agoraPrint("Got sing battle game info fail...")
                agoraPrint("error = \(error.description)")
                completion(error, nil)
            })
    }
    
    func innerAddSingRelayInfo(_ model: SingRelayModel, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            assertionFailure("channelName is nil")
            return
        }
        
        agoraPrint("Adding sing battle game info...")
        let params = mapConvert(model: model)
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_UPDATE_GAME_STATE)
            .add(data: params, success: { _ in
                agoraPrint("Add sing battle game info success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("Add sing battle game info fail :\(error.message)...")
                agoraPrint(error.message)
                completion(NSError(domain: error.message, code: error.code))
            })
    }

    func innerUpdateSingRelayInfo(_ model: SingRelayModel, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo,
              let objectId = model.objectId else {
            assertionFailure("channelName or objectId is nil")
            return
        }

        agoraPrint("Updating sing battle game info... [\(objectId)]")
        let params = mapConvert(model: model)
      print("[AgoraSyncManager] service update2")
//        let params = mapConvert(model: <#T##NSObject#>)
//        ]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_UPDATE_GAME_STATE)
            .update(id: objectId,
                    data: params,
                    success: {
                completion(nil)
                agoraPrint("Update sing battle game info success...\(model.objectId).....\(objectId)")
            }, fail: { error in
                completion(NSError(domain: error.message, code: error.code))
                agoraPrint("Update sing battle game info fail...")
            })
    }

    func innerSubscribeSingRelayInfo(completion: @escaping (SRSubscribe, SingRelayModel?, Error?) -> Void) {
        guard let channelName = roomNo else {
            assertionFailure("channelName is nil")
            return
        }
        
        agoraPrint("sbg state subscribe...")
        SyncUtil.scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_UPDATE_GAME_STATE,
                       onCreated: { object in
                        guard let jsonStr = object.toJson(),
                            let model = SingRelayModel.yy_model(withJSON: jsonStr) else {
                                return
                        }
                        agoraPrint("sbg state subscribe oncreated... [\(object.getId())]")
                completion(.created , model, nil)
            }, onUpdated: { object in
                agoraPrint("sbg state subscribe onupdated... [\(object.getId())]")
                guard let jsonStr = object.toJson(),
                    let model = SingRelayModel.yy_model(withJSON: jsonStr) else {
                        return
                }
                completion(.updated, model, nil)
            }, onDeleted: { object in
                agoraPrint("sbg state subscribe ondeleted... [\(object.getId())]")
            }, onSubscribed: {
    //            LogUtils.log(message: "subscribe message", level: .info)
            }, fail: { error in
                agoraPrint("imp seat subscribe fail...")
                ToastView.show(text: error.message)
                completion(.failed, nil, NSError(domain: error.message, code: error.code))
            })
    }
}

// MARK: Seat operation

extension SRSyncManagerServiceImp {
    private func _markSeatChoursStatus(songCode: String, completion: @escaping (Error?)->()) {
        guard let seatInfo = self.seatMap
            .filter({ $0.value.userNo == VLUserCenter.user.id })
            .first?.value else {
            agoraAssert("mark join seat not found")
            //TODO: error
            completion(nil)
            return
        }
        seatInfo.chorusSongCode = songCode
        _updateSeat(seatInfo: seatInfo, finished: completion)
    }
    
    private func _getInitSeats() -> [VLSRRoomSeatModel] {
        var seatArray = [VLSRRoomSeatModel]()
        for i in 0...7 {
            if let seat = seatMap["\(i)"] {
                seatArray.append(seat)
                continue
            }
            let seat = VLSRRoomSeatModel()
            seat.seatIndex = i
            seatArray.append(seat)

            seatMap["\(i)"] = seat
        }

        return seatArray
    }

    private func _getUserSeatInfo(seatIndex: Int, model: VLSRRoomSeatModel? = nil) -> VLSRRoomSeatModel {
        let user = VLUserCenter.user
        let seatInfo = VLSRRoomSeatModel()
        seatInfo.seatIndex = seatIndex
        seatInfo.rtcUid = user.id
        seatInfo.userNo = user.id
        seatInfo.headUrl = user.headUrl
        seatInfo.name = user.name
        
        
        if let m = model {
            /// 是否自己静音
            seatInfo.isAudioMuted = m.isAudioMuted
            /// 是否开启视频
            seatInfo.isVideoMuted = m.isVideoMuted

            /// 新增, 判断当前歌曲是否是自己点的
            seatInfo.isOwner = m.isOwner

            seatInfo.chorusSongCode = m.chorusSongCode
        } else {
            /// 是否自己静音
            seatInfo.isAudioMuted = 1
            /// 是否开启视频
            seatInfo.isVideoMuted = 1

            /// 新增, 判断当前歌曲是否是自己点的
            seatInfo.isOwner = false

            seatInfo.chorusSongCode = ""
        }
        

        return seatInfo
    }

    private func _autoOnSeatIfNeed(completion: @escaping ([VLSRRoomSeatModel])->()) {
//        _subscribeSeats {}

        userList.removeAll()
        songList.removeAll()
        seatMap.removeAll()
        _getSeatInfo { [weak self] (error, list) in
            guard let self = self, let list = list else {
                return
            }
            
            //TODO: _getSeatInfo will callback if remove seat invoke
            guard self.seatMap.count == 0 else {
                return
            }
            
            list.forEach { seat in
                self.seatMap["\(seat.seatIndex)"] = seat
            }

            // update seat info (user avater/nick name did changed) if seat existed
            if let seat = self.seatMap.filter({ $0.value.userNo == VLUserCenter.user.id }).first?.value {
                let targetSeatInfo = self._getUserSeatInfo(seatIndex: seat.seatIndex, model: seat)
                targetSeatInfo.objectId = seat.objectId
                self._updateSeat(seatInfo: targetSeatInfo) { error in
                    completion(self._getInitSeats())
                }
                return
            }
            guard VLUserCenter.user.ifMaster else {
                completion(self._getInitSeats())
                return
            }

            // add master to first seat
            let targetSeatInfo = self._getUserSeatInfo(seatIndex: 0)
            targetSeatInfo.isAudioMuted = 0
            targetSeatInfo.isMaster = true
            self._addSeatInfo(seatInfo: targetSeatInfo) { error in
                completion(self._getInitSeats())
            }
        }
    }
    
    private func _seatListReloadIfNeed() {
        guard let _ = roomNo else {
            agoraPrint("_seatListReloadIfNeed break")
            return
        }
        _getSeatInfo {[weak self] (error, seatList) in
            guard let self = self,
                    error == nil,
                    let seatList = seatList else { return }
            
            var _seatMap: [String: VLSRRoomSeatModel] = .init()
            seatList.forEach { seat in
                _seatMap["\(seat.seatIndex)"] = seat
            }
            
            self.seatMap.forEach { (key, origSeat) in
                guard let seat = _seatMap[key] else {
                    let seat = VLSRRoomSeatModel()
                    seat.seatIndex = origSeat.seatIndex
                    _seatMap[key] = seat
                    self.seatListDidChanged?(.deleted, origSeat)
                    return
                }
                
                self.seatListDidChanged?(.updated, seat)
            }
            self.seatMap = _seatMap
        }
    }

    private func _getSeatInfo(finished: @escaping (Error?, [VLSRRoomSeatModel]?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            finished(nil, nil)
            return
        }
        agoraPrint("imp seat get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .get(success: { list in
                agoraPrint("imp seat get success...")
                let seats = list.compactMap({ VLSRRoomSeatModel.yy_model(withJSON: $0.toJson()!)! })
                
                finished(nil, seats)
            }, fail: { error in
                agoraPrint("imp seat get fail...")
                agoraPrint("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _updateSeat(seatInfo: VLSRRoomSeatModel,
                            finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo,
              let objectId = seatInfo.objectId
        else {
//            assert(false, "channelName = nil")
            agoraPrint("updateSeatInfo channelName = nil")
            return
        }
        
        agoraPrint("imp seat update... [\(objectId)]")
        let params = mapConvert(model: seatInfo)
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .update(id: objectId,
                    data: params,
                    success: {
                agoraPrint("imp seat update success...")
                finished(nil)
            }, fail: { error in
                agoraPrint("imp seat update fail...")
                finished(NSError(domain: error.message, code: error.code))
            })
    }

    private func _removeSeat(seatInfo: VLSRRoomSeatModel,
                            finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo,
              let objectId = seatInfo.objectId
        else {
//            assert(false, "channelName = nil")
            agoraPrint("removeSeat channelName = nil")
            return
        }
        
        agoraPrint("imp seat delete... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .document(id: objectId)
            .delete(success: {_ in
                agoraPrint("imp seat delete success...")
                finished(nil)
            }, fail: { error in
                agoraPrint("imp seat delete fail...")
                finished(NSError(domain: error.message, code: error.code))
            })
    }

    private func _addSeatInfo(seatInfo: VLSRRoomSeatModel,
                             finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            agoraPrint("addUserInfo channelName = nil")
            let error = NSError(domain: "addUserInfo channelName = nil", code: -1)
            finished(error)
            return
        }
        
        agoraPrint("imp seat add...")
        let params = mapConvert(model: seatInfo)
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .add(data: params,
                 success: {[weak self] obj in
                agoraPrint("imp seat add success...")
                seatInfo.objectId = obj.getId()
                self?.seatMap["\(seatInfo.seatIndex)"] = seatInfo
                finished(nil)
            }, fail: { error in
                agoraPrint("imp seat add fail...")
                finished(error)
            })
    }

    private func _subscribeSeats(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            assertionFailure("channelName = nil")
            return
        }
        agoraPrint("imp seat subscribe...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_SEAT_INFO,
                       onCreated: { [weak self] object in
                agoraPrint("imp seat subscribe oncreated... [\(object.getId())]")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = VLSRRoomSeatModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                self.seatMap["\(model.seatIndex)"] = model
                self.seatListDidChanged?(.created, model)
            }, onUpdated: { [weak self] object in
                agoraPrint("imp seat subscribe onupdated... [\(object.getId())]")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = VLSRRoomSeatModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                self.seatMap["\(model.seatIndex)"] = model
                self.seatListDidChanged?(.updated, model)
            }, onDeleted: { [weak self] object in
                agoraPrint("imp seat subscribe ondeleted... [\(object.getId())]")
                guard let self = self else {
                    return
                }
                let objectId = object.getId()
                guard let origSeat = self.seatMap.filter({ $0.value.objectId == objectId }).first?.value else {
                    agoraPrint("delete seat not found")
                    return
                }
                let seat = VLSRRoomSeatModel()
                seat.seatIndex = origSeat.seatIndex
                self.seatMap["\(origSeat.seatIndex)"] = seat
                self.seatListDidChanged?(.deleted, seat)
            }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                finished()
            }, fail: { error in
                agoraPrint("imp seat subscribe fail...")
                ToastView.show(text: error.message)
                finished()
            })
    }
}

// MARK: Choose song operation

extension SRSyncManagerServiceImp {
    private func _sortChooseSongList() {
        songList = songList.sorted(by: { model1, model2 in
            if model1.status == .playing{
                return true
            }
            if model2.status == .playing {
                return false
            }
            if model1.pinAt < 1,  model2.pinAt < 1 {
                return model1.createAt - model2.createAt < 0 ? true : false
            }
            
            return model1.pinAt - model2.pinAt > 0 ? true : false
        })
    }

    private func _getChooseSongInfo(finished: @escaping (Error?, [VLSRRoomSelSongModel]?) -> Void) {
        guard let channelName = roomNo else {
//            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp song get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .get(success: { [weak self] list in
                guard let self = self else {
                    return
                }
                agoraPrint("imp song get success... \(list.count)")
                let totalList = list.compactMap({
                    VLSRRoomSelSongModel.yy_model(withJSON: $0.toJson()!)!
                })
                self.songList = totalList.filterDuplicates({$0.songNo})
                self._sortChooseSongList()
                let songList = self.songList
                finished(nil, songList)
            }, fail: { error in
                agoraPrint("imp song get fail \(error.description)...")
                finished(error, nil)
            })
    }
    
    func updateChooseSong(withSongInfo songInfo: VLSRRoomSelSongModel, finished: @escaping (Error?) -> Void) {
        _updateChooseSong(songInfo: songInfo, finished: finished)
    }

    private func _updateChooseSong(songInfo: VLSRRoomSelSongModel,
                                  finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo, let objectId = songInfo.objectId else {
//            assert(false, "channelName = nil")
            agoraPrint("update song channelName = nil")
            return
        }

        let params = mapConvert(model: songInfo)
        agoraPrint("imp song update... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .update(id: objectId,
                    data: params,
                    success: {
                agoraPrint("imp song update success...")
                finished(nil)
            }, fail: { error in
                agoraPrint("imp song update fail \(error.description)...")
                finished(error)
            })
    }

    private func _addChooseSongInfo(songInfo: VLSRRoomSelSongModel, finished: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            agoraPrint("addUserInfo channelName = nil")
            return
        }
        agoraPrint("imp song add...")
        songInfo.createAt = Int64(Date().timeIntervalSince1970 * 1000)
        let params = mapConvert(model: songInfo)
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .add(data: params,
                 success: { obj in
                agoraPrint("imp song add success...")
                finished(nil)
            }, fail: { error in
                agoraPrint("imp song add fail...")
                finished(error)
            })
    }
    
    private func _removeAllUserChooseSong(userNo: String = VLUserCenter.user.id) {
        let userSongLists = self.songList.filter({ $0.userNo == userNo})
        //reverse delete songs to fix conflicts (user A remove song1 & user B update song1.status = 2)
        userSongLists.reversed().forEach { model in
            self._removeChooseSong(songId: model.objectId) { error in
            }
        }
    }

    private func _removeChooseSong(songId: String?, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo,
              let objectId = songId
        else {
            agoraAssert("channelName = nil")
            completion(nil)
            return
        }
        agoraPrint("imp song delete... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .document(id: objectId)
            .delete(success: {_ in
                completion(nil)
                agoraPrint("imp song delete success...")
            }, fail: { error in
                agoraPrint("imp song delete fail \(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }

    private func _markCurrentSongIfNeed() {
        guard let topSong = songList.first,
              topSong.status == .playing, // ready status
             // topSong.isChorus == false,
              topSong.userNo == VLUserCenter.user.id
        else {
            return
        }

        topSong.status = .playing
        _updateChooseSong(songInfo: topSong) { error in
        }
    }

    private func _markSoloSongIfNeed() {
        guard let topSong = songList.first,
            //  topSong.isChorus == true, // current is chorus
              topSong.userNo == VLUserCenter.user.id
        else {
            SRLog.warning(text: "_markSoloSongIfNeed break:  \(songList.first?.status.rawValue ?? 0) \(songList.first?.userNo ?? "")/\(VLUserCenter.user.id)")
            return
        }
        
        let status = topSong.status
        topSong.status = .playing
        _updateChooseSong(songInfo: topSong) { error in
        }
        topSong.status = status
        //自己不需要标记
//        _markSeatChoursStatus(songCode: nil) { err in
//        }
    }

    private func _subscribeChooseSong(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            finished()
            return
        }
        agoraPrint("imp song subscribe...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_CHOOSE_SONG_INFO,
                       onCreated: {  _ in
            }, onUpdated: { [weak self] object in
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = VLSRRoomSelSongModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                let songList = self.songList.filter({ $0.objectId != model.objectId })
                let type: SRSubscribe = songList.count == self.songList.count ? .created : .updated
                self.songList = songList
                self.songList.append(model)
                self._sortChooseSongList()
                self.chooseSongDidChanged?(type, model, self.songList)
//                self._markCurrentSongIfNeed()
                agoraPrint("imp song subscribe onUpdated... [\(object.getId())] count: \(self.songList.count)")
            }, onDeleted: { [weak self] object in
                guard let self = self,
                      let origSong = self.songList.filter({ $0.objectId == object.getId()}).first
                else {
                    return
                }
                self.songList = self.songList.filter({ $0.objectId != origSong.objectId })
                self.chooseSongDidChanged?(.deleted, origSong, self.songList)
//               self._markCurrentSongIfNeed()
                agoraPrint("imp song subscribe onDeleted... [\(object.getId())] count: \(self.songList.count)")
            }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                finished()
            }, fail: { error in
                agoraPrint("imp song subscribe fail \(error.message)...")
                ToastView.show(text: error.message)
                finished()
            })
    }
}
