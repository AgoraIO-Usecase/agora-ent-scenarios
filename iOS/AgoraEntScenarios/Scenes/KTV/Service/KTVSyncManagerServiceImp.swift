//
//  KTVSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

import Foundation
import YYCategories
import SVProgressHUD

private let kSceneId = "scene_ktv_2.3.0"

/// 座位信息
private let SYNC_MANAGER_SEAT_INFO = "seat_info"
// 选歌
private let SYNC_MANAGER_CHOOSE_SONG_INFO = "choose_song"

private func agoraAssert(_ message: String) {
    agoraAssert(false, message)
}

private func agoraAssert(_ condition: Bool, _ message: String) {
    KTVLog.error(text: message, tag: "KTVService")
    #if DEBUG
//    assert(condition, message)
    #else
    #endif
}

private func agoraPrint(_ message: String) {
//    #if DEBUG
    KTVLog.info(text: message, tag: "KTVService")
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

@objc class KTVSyncManagerServiceImp: NSObject, KTVServiceProtocol {
    private var roomList: [VLRoomListModel]?
    private var userList: [VLLoginModel] = .init()
    private var seatMap: [String: VLRoomSeatModel] = .init()
    private var songList: [VLRoomSelSongModel] = .init()

    private var userListCountDidChanged: ((UInt) -> Void)?
    private var userDidChanged: ((UInt, VLLoginModel) -> Void)?
    private var seatListDidChanged: ((UInt, VLRoomSeatModel) -> Void)?
    private var roomStatusDidChanged: ((UInt, VLRoomListModel) -> Void)?
    private var chooseSongDidChanged: ((UInt, VLRoomSelSongModel, [VLRoomSelSongModel]) -> Void)?
//    private var singingScoreDidChanged: ((Double) -> Void)?
    private var networkDidChanged: ((KTVServiceNetworkStatus) -> Void)?
    private var roomExpiredDidChanged: (() -> Void)?

    private var roomNo: String?
    
    private var room: VLRoomListModel? {
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

    private func initScene(completion: @escaping () -> Void) {
        if syncUtilsInited {
            completion()
            return
        }

        SyncUtil.initSyncManager(sceneId: kSceneId) {
        }
        
        SyncUtil.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            agoraPrint("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.networkDidChanged?(KTVServiceNetworkStatus(rawValue: UInt(state.rawValue)))
            guard state == .open else { return }
            guard !self.syncUtilsInited else {
                self._seatListReloadIfNeed()
                self._getUserInfo { err, list in
                    self.userListCountDidChanged?(UInt(list?.count ?? 0))
                }
                return
            }
            
            self.syncUtilsInited = true
            completion()
        }
    }
    
    // MARK: protocol method
    
    // MARK: room info
    func getRoomList(withPage page: UInt, completion: @escaping (Error?, [VLRoomListModel]?) -> Void) {
        initScene { [weak self] in
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
                    return VLRoomListModel.yy_model(with: info.toJson()!.toDictionary())!
                })
                self.roomList = dataArray.sorted(by: { ($0.updatedAt > 0 ? $0.updatedAt : $0.createdAt) > ($1.updatedAt > 0 ? $1.updatedAt : $0.createdAt) })
                completion(nil, self.roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }

    func createRoom(withInput inputModel: KTVCreateRoomInputModel,
                    completion: @escaping (Error?, KTVCreateRoomOutputModel?) -> Void)
    {
        let roomInfo = VLRoomListModel() // LiveRoomInfo(roomName: inputModel.name)
//        roomInfo.id = VLUserCenter.user.id//NSString.withUUID().md5() ?? ""
        roomInfo.name = inputModel.name
        roomInfo.isPrivate = inputModel.isPrivate.boolValue
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
        initScene { [weak self] in
            agoraPrint("createRoom initScene cost: \(-date.timeIntervalSinceNow * 1000) ms")
            SyncUtil.joinScene(id: roomInfo.roomNo,
                               userId: roomInfo.creator,
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
                        let output = KTVCreateRoomOutputModel()
                        output.name = inputModel.name
                        output.roomNo = roomInfo.roomNo
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

    func joinRoom(withInput inputModel: KTVJoinRoomInputModel,
                  completion: @escaping (Error?, KTVJoinRoomOutputModel?) -> Void)
    {
        guard let roomInfo = roomList?.filter({ $0.roomNo == inputModel.roomNo }).first else {
            agoraAssert("join Room fail")
            completion(nil, nil)
            return
        }

        let params = mapConvert(model: roomInfo)

        _showLoadingIfNeed()
        let date = Date()
        initScene { [weak self] in
            agoraPrint("joinRoom initScene cost: \(-date.timeIntervalSinceNow * 1000) ms")
            SyncUtil.joinScene(id: roomInfo.roomNo,
                               userId: roomInfo.creator,
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
                        let output = KTVJoinRoomOutputModel()
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

    func changeMVCover(withParams inputModel: KTVChangeMVCoverInputModel,
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
    func enterSeat(withInput inputModel: KTVOnSeatInputModel,
                   completion: @escaping (Error?) -> Void) {
        let seatInfo = _getUserSeatInfo(seatIndex: Int(inputModel.seatIndex))
        _addSeatInfo(seatInfo: seatInfo,
                     finished: completion)
    }

    func leaveSeat(withInput inputModel: KTVOutSeatInputModel,
                   completion: @escaping (Error?) -> Void) {
        let seatInfo = seatMap["\(inputModel.seatIndex)"]!
        _removeSeat(seatInfo: seatInfo) { error in
        }
        
        //remove current user's choose song
        _removeAllUserChooseSong(userNo: seatInfo.userNo)
        completion(nil)
    }
    
    func updateSeatAudioMuteStatus(withMuted muted: Bool,
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

    func updateSeatVideoMuteStatus(withMuted muted: Bool,
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
    func removeSong(withInput inputModel: KTVRemoveSongInputModel,
                    completion: @escaping (Error?) -> Void) {
        _removeChooseSong(songId: inputModel.objectId,
                          completion: completion)
    }

    func getChoosedSongsList(completion: @escaping (Error?, [VLRoomSelSongModel]?) -> Void) {
        _getChooseSongInfo(finished: completion)
    }

    func joinChorus(withInput inputModel: KTVJoinChorusInputModel,
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

    func markSongDidPlay(withInput inputModel: VLRoomSelSongModel,
                         completion: @escaping (Error?) -> Void) {
        inputModel.status = VLSongPlayStatusPlaying
        _updateChooseSong(songInfo: inputModel, finished: completion)
    }

    func chooseSong(withInput inputModel: KTVChooseSongInputModel,
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
        
        let songInfo = VLRoomSelSongModel()
       // songInfo.isChorus = inputModel.isChorus
        songInfo.songName = inputModel.songName
        songInfo.songNo = inputModel.songNo
//        songInfo.songUrl = inputModel.songUrl
        songInfo.imageUrl = inputModel.imageUrl
        songInfo.singer = inputModel.singer
        songInfo.status = VLSongPlayStatusIdle
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
    
    func pinSong(withInput inputModel: KTVMakeSongTopInputModel,
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
        if topSong.objectId != song.objectId, topSong.status != VLSongPlayStatusPlaying {
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
    
//    func updateSingingScore(withScore score: Double) {
////        assertionFailure()
//        _addSingingScore(score: score) {
//        }
//    }

    //MARK: subscribe
    func subscribeUserListCountChanged(_ changedBlock: @escaping (UInt) -> Void) {
//        _unsubscribeAll()
        userListCountDidChanged = changedBlock
        _subscribeOnlineUsers {
        }
    }
    
    func subscribeUserChanged(_ changedBlock: @escaping (UInt, VLLoginModel) -> Void) {
        userDidChanged = changedBlock
    }

    func subscribeSeatListChanged(_ changedBlock: @escaping (UInt, VLRoomSeatModel) -> Void) {
        seatListDidChanged = changedBlock
        _subscribeSeats {
        }
    }
    
    func subscribeRoomStatusChanged(_ changedBlock: @escaping (UInt, VLRoomListModel) -> Void) {
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
                                 let model = VLRoomListModel.yy_model(withJSON: jsonStr),
                                 model.roomNo == channelName
                           else {
                               return
                           }
                           agoraPrint("imp room subscribe onUpdated...")
                           self?.roomStatusDidChanged?(KTVSubscribeUpdated.rawValue, model)
                       }, onDeleted: { [weak self] object in
                           guard let model = self?.roomList?.filter({ $0.roomNo == object.getId()}).first,
                                 model.roomNo == channelName
                           else {
                               return
                           }
                           agoraPrint("imp room subscribe onDeleted...")
                           self?.roomStatusDidChanged?(KTVSubscribeDeleted.rawValue, model)
                       }, onSubscribed: {}, fail: { error in
                       })
    }

    func subscribeChooseSongChanged(_ changedBlock: @escaping (UInt, VLRoomSelSongModel, [VLRoomSelSongModel]) -> Void) {
        chooseSongDidChanged = changedBlock
        _subscribeChooseSong {
        }
    }
    
//    func subscribeSingingScoreChanged(_ changedBlock: @escaping (Double) -> Void) {
//        singingScoreDidChanged = changedBlock
//        _subscribeSingScore()
//    }
    
    func subscribeNetworkStatusChanged(_ changedBlock: @escaping (KTVServiceNetworkStatus) -> Void) {
        networkDidChanged = changedBlock
    }
    
    func subscribeRoomWillExpire(_ changedBlock: @escaping () -> Void) {
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
extension KTVSyncManagerServiceImp {
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
extension KTVSyncManagerServiceImp {
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
                               self.userDidChanged?(KTVSubscribeUpdated.rawValue, model)
                               return
                           }
                           
                           self.userList.append(model)
                           agoraPrint("imp user subscribe onUpdated2... \(self.userList.count)")
                           self.userDidChanged?(KTVSubscribeCreated.rawValue, model)
                           self._updateUserCount { error in
                           }
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp user subscribe onDeleted...")
                           guard let self = self, let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) else {
                               return
                           }
                           let model = self.userList[index]
                           self.userDidChanged?(KTVSubscribeDeleted.rawValue, model)
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
        
        roomStatusDidChanged?(KTVSubscribeDeleted.rawValue, roomInfo)
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

// MARK: Seat operation

extension KTVSyncManagerServiceImp {
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
    
    private func _getInitSeats() -> [VLRoomSeatModel] {
        var seatArray = [VLRoomSeatModel]()
        for i in 0...7 {
            if let seat = seatMap["\(i)"] {
                seatArray.append(seat)
                continue
            }
            let seat = VLRoomSeatModel()
            seat.seatIndex = i
            seatArray.append(seat)

            seatMap["\(i)"] = seat
        }

        return seatArray
    }

    private func _getUserSeatInfo(seatIndex: Int, model: VLRoomSeatModel? = nil) -> VLRoomSeatModel {
        let user = VLUserCenter.user
        let seatInfo = VLRoomSeatModel()
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

    private func _autoOnSeatIfNeed(completion: @escaping ([VLRoomSeatModel])->()) {
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
            
            var _seatMap: [String: VLRoomSeatModel] = .init()
            seatList.forEach { seat in
                _seatMap["\(seat.seatIndex)"] = seat
            }
            
            self.seatMap.forEach { (key, origSeat) in
                guard let seat = _seatMap[key] else {
                    let seat = VLRoomSeatModel()
                    seat.seatIndex = origSeat.seatIndex
                    _seatMap[key] = seat
                    self.seatListDidChanged?(KTVSubscribeDeleted.rawValue, origSeat)
                    return
                }
                
                self.seatListDidChanged?(KTVSubscribeUpdated.rawValue, seat)
            }
            self.seatMap = _seatMap
        }
    }

    private func _getSeatInfo(finished: @escaping (Error?, [VLRoomSeatModel]?) -> Void) {
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
                let seats = list.compactMap({ VLRoomSeatModel.yy_model(withJSON: $0.toJson()!)! })
                
                finished(nil, seats)
            }, fail: { error in
                agoraPrint("imp seat get fail...")
                agoraPrint("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _updateSeat(seatInfo: VLRoomSeatModel,
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

    private func _removeSeat(seatInfo: VLRoomSeatModel,
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

    private func _addSeatInfo(seatInfo: VLRoomSeatModel,
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
                      let model = VLRoomSeatModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                self.seatMap["\(model.seatIndex)"] = model
                self.seatListDidChanged?(KTVSubscribeCreated.rawValue, model)
            }, onUpdated: { [weak self] object in
                agoraPrint("imp seat subscribe onupdated... [\(object.getId())]")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = VLRoomSeatModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                self.seatMap["\(model.seatIndex)"] = model
                self.seatListDidChanged?(KTVSubscribeUpdated.rawValue, model)
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
                let seat = VLRoomSeatModel()
                seat.seatIndex = origSeat.seatIndex
                self.seatMap["\(origSeat.seatIndex)"] = seat
                self.seatListDidChanged?(KTVSubscribeDeleted.rawValue, seat)
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

extension KTVSyncManagerServiceImp {
    private func _sortChooseSongList() {
        songList = songList.sorted(by: { model1, model2 in
            if model1.status == VLSongPlayStatusPlaying {
                return true
            }
            if model2.status == VLSongPlayStatusPlaying {
                return false
            }
            if model1.pinAt < 1,  model2.pinAt < 1 {
                return model1.createAt - model2.createAt < 0 ? true : false
            }
            
            return model1.pinAt - model2.pinAt > 0 ? true : false
        })
    }

    private func _getChooseSongInfo(finished: @escaping (Error?, [VLRoomSelSongModel]?) -> Void) {
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
                    VLRoomSelSongModel.yy_model(withJSON: $0.toJson()!)!
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

    private func _updateChooseSong(songInfo: VLRoomSelSongModel,
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

    private func _addChooseSongInfo(songInfo: VLRoomSelSongModel, finished: @escaping (Error?) -> Void) {
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
              topSong.status == VLSongPlayStatusIdle, // ready status
             // topSong.isChorus == false,
              topSong.userNo == VLUserCenter.user.id
        else {
            return
        }

        topSong.status = VLSongPlayStatusPlaying
        _updateChooseSong(songInfo: topSong) { error in
        }
    }

    private func _markSoloSongIfNeed() {
        guard let topSong = songList.first,
            //  topSong.isChorus == true, // current is chorus
              topSong.userNo == VLUserCenter.user.id
        else {
            KTVLog.warning(text: "_markSoloSongIfNeed break:  \(songList.first?.status.rawValue ?? 0) \(songList.first?.userNo ?? "")/\(VLUserCenter.user.id)")
            return
        }
        
        let status = topSong.status
        topSong.status = VLSongPlayStatusPlaying
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
                      let model = VLRoomSelSongModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                let songList = self.songList.filter({ $0.objectId != model.objectId })
                let type = songList.count == self.songList.count ? KTVSubscribeCreated : KTVSubscribeUpdated
                self.songList = songList
                self.songList.append(model)
                self._sortChooseSongList()
                self.chooseSongDidChanged?(type.rawValue, model, self.songList)
//                self._markCurrentSongIfNeed()
                agoraPrint("imp song subscribe onUpdated... [\(object.getId())] count: \(self.songList.count)")
            }, onDeleted: { [weak self] object in
                guard let self = self,
                      let origSong = self.songList.filter({ $0.objectId == object.getId()}).first
                else {
                    return
                }
                self.songList = self.songList.filter({ $0.objectId != origSong.objectId })
                self.chooseSongDidChanged?(KTVSubscribeDeleted.rawValue, origSong, self.songList)
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
