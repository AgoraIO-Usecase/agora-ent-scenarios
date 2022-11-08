//
//  KTVSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

import Foundation
import YYCategories

private let kSceneId = "scene_ktv"

/// 座位信息
private let SYNC_MANAGER_SEAT_INFO = "seat_info"
// 选歌
private let SYNC_MANAGER_CHOOSE_SONG_INFO = "choose_song"

private func agoraAssert(_ message: String) {
    agoraAssert(false, message)
}

private func agoraAssert(_ condition: Bool, _ message: String) {
    #if DEBUG
    assert(condition, message)
    #else
    
    #endif
}

@objc class KTVSyncManagerServiceImp: NSObject, KTVServiceProtocol {
    private var roomList: [VLRoomListModel]?
    private var userList: [VLLoginModel] = .init()
    private var seatMap: [String: VLRoomSeatModel] = .init()
    private var songList: [VLRoomSelSongModel] = .init()

    private var userListCountDidChanged: ((UInt) -> Void)?
    private var seatListDidChanged: ((UInt, VLRoomSeatModel) -> Void)?
    private var roomStatusDidChanged: ((UInt, VLRoomListModel) -> Void)?
    private var chooseSongDidChanged: ((UInt, VLRoomSelSongModel) -> Void)?

    private var roomNo: String? {
        didSet {
            if oldValue == roomNo {
                return
            }
            guard let _ = roomNo else {
                return
            }

            syncUtilsInited = false
        }
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

    private func initScene(completion: @escaping () -> Void) {
        if syncUtilsInited {
            completion()
            return
        }

        SyncUtil.initSyncManager(sceneId: kSceneId) { [weak self] in
            guard let self = self else {
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
            SyncUtil.fetchAll { results in
                print("result == \(results.compactMap { $0.toJson() })")
                guard let self = self else {
                    return
                }

                let dataArray = results.map({ info in
                    return VLRoomListModel.yy_model(with: info.toJson()!.toDictionary())!
                })
                self.roomList = dataArray.sorted(by: { TimeInterval($0.updatedAt ?? $0.createdAt ?? "0") ?? 0 > TimeInterval($1.updatedAt ?? $0.createdAt ?? "0") ?? 0 })
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
        roomInfo.creator = VLUserCenter.user.userNo
        roomInfo.creatorNo = VLUserCenter.user.userNo
        roomInfo.roomNo = "\(arc4random_uniform(899999) + 100000)" // roomInfo.id
        roomInfo.bgOption = Int.random(in: 1...2)
        roomInfo.roomPeopleNum = "0"
        roomInfo.createdAt = "\(Date().timeIntervalSince1970)"
//        roomInfo.soundEffect;
//        roomInfo.belCanto;
//        roomInfo.createdAt;
//        roomInfo.updatedAt;
//        roomInfo.status;
//        roomInfo.deletedAt;
//        roomInfo.roomPeopleNum;
//        roomInfo.icon;

        let params = roomInfo.yy_modelToJSONObject() as? [String: Any]

        initScene { [weak self] in
            SyncUtil.joinScene(id: roomInfo.roomNo,
                               userId: roomInfo.creator,
                               property: params) { result in
                //            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
                let channelName = result.getPropertyWith(key: "roomNo", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creator", type: String.self) as? String ?? ""
                self?.roomNo = channelName
                NetworkManager.shared.generateTokens(channelName: channelName ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    guard let self = self,
                          let rtcToken = tokenMap[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap[NetworkManager.AgoraTokenType.rtm.rawValue]
                    else {
                        agoraAssert(tokenMap.count == 2, "rtcToken == nil || rtmToken == nil")
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.userNo == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    self.roomList?.append(roomInfo)
//                    VLUserCenter.user.agoraPlayerRTCToken = response.data[@"agoraPlayerRTCToken"];
                    let output = KTVCreateRoomOutputModel()
                    output.name = inputModel.name
                    output.roomNo = roomInfo.roomNo
                    output.seatsArray = self._emptySeats()
                    completion(nil, output)
                    self._addUserIfNeed()
                    self._autoOnSeatIfNeed()
                    self._subscribeChooseSong {}
                }
            } fail: { error in
                completion(error, nil)
            }
        }
    }

    func joinRoom(withInput inputModel: KTVJoinRoomInputModel,
                  completion: @escaping (Error?, KTVJoinRoomOutputModel?) -> Void)
    {
        guard let roomInfo = roomList?.filter({ $0.roomNo == inputModel.roomNo }).first else {
            agoraAssert("join Room fail")
            return
        }

        let params = roomInfo.yy_modelToJSONObject() as? [String: Any]

        initScene { [weak self] in
            SyncUtil.joinScene(id: roomInfo.roomNo,
                               userId: roomInfo.creator,
                               property: params) { result in
                //            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
                let channelName = result.getPropertyWith(key: "roomNo", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creator", type: String.self) as? String ?? ""
                self?.roomNo = channelName
                NetworkManager.shared.generateTokens(channelName: channelName ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    guard let self = self,
                          let rtcToken = tokenMap[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap[NetworkManager.AgoraTokenType.rtm.rawValue]
                    else {
                        agoraAssert(tokenMap.count == 2, "rtcToken == nil || rtmToken == nil")
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.userNo == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
//                    VLUserCenter.user.agoraPlayerRTCToken = response.data[@"agoraPlayerRTCToken"];
                    let output = KTVJoinRoomOutputModel()
                    output.creator = userId
                    output.seatsArray = self._emptySeats()
                    completion(nil, output)
                    self._addUserIfNeed()
                    self._autoOnSeatIfNeed()
                    self._subscribeChooseSong {}
                }
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    func leaveRoom(completion: @escaping (Error?) -> Void) {
        guard let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first else {
            agoraAssert("leaveRoom channelName = nil")
            return
        }
        
        //current user is room owner, remove room
        if roomInfo.creator == VLUserCenter.user.userNo {
            _removeRoom(completion: completion)
            return
        }
        _leaveRoom(completion: completion)
    }


    func changeMVCover(withInput inputModel: KTVChangeMVCoverInputModel,
                       completion: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo,
              let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first
        else {
            agoraAssert("channelName = nil")
            return
        }
        roomInfo.bgOption = Int(inputModel.mvIndex)
        var params = roomInfo.yy_modelToJSONObject() as! [String: Any]
        params["objectId"] = channelName
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { object in
                        guard let _ = object.first else {
                            print("udpate mv fail")
                            return
                        }
                        completion(nil)
                    }, fail: { error in
                        completion(error)
                    })
        completion(nil)
    }
    
    // MARK: mic seat
    func onSeat(withInput inputModel: KTVOnSeatInputModel, completion: @escaping (Error?) -> Void) {
        let seatInfo = _getUserSeatInfo(seatIndex: Int(inputModel.seatIndex))
        _addSeatInfo(seatInfo: seatInfo,
                     finished: completion)
    }

    func outSeat(withInput inputModel: KTVOutSeatInputModel, completion: @escaping (Error?) -> Void) {
        let seatInfo = seatMap["\(inputModel.userOnSeat)"]!
        _removeSeat(seatInfo: seatInfo) { error in
            // TODO(wushengtao): whitout callback
        }
        completion(nil)
    }
    
    func openAudioStatus(withStatus openStatus: Bool,
                         completion: @escaping (Error?) -> Void) {
        guard let seatInfo = self.seatMap
            .filter({ $0.value.userNo == VLUserCenter.user.userNo })
            .first?.value else {
            agoraAssert("mute seat not found")
            return
        }
        
        seatInfo.isSelfMuted = openStatus ? 0 : 1
        _updateSeat(seatInfo: seatInfo,
                    finished: completion)
    }

    func openVideoStatus(withStatus openStatus: Bool,
                         completion: @escaping (Error?) -> Void) {
        guard let seatInfo = self.seatMap
            .filter({ $0.value.userNo == VLUserCenter.user.userNo })
            .first?.value else {
            agoraAssert("open video seat not found")
            return
        }
        
        seatInfo.isVideoMuted = openStatus ? 1 : 0
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
            return
        }
        //isChorus always true
        topSong.isChorus = inputModel.isChorus == "1" ? true : false
        topSong.status = 2
        topSong.chorusNo = VLUserCenter.user.userNo
        _updateChooseSong(songInfo: topSong,
                          finished: completion)
    }

    func markSongDidPlay(withInput inputModel: VLRoomSelSongModel,
                         completion: @escaping (Error?) -> Void) {
        _updateChooseSong(songInfo: inputModel, finished: completion)
    }

    func chooseSong(withInput inputModel: KTVChooseSongInputModel,
                    completion: @escaping (Error?) -> Void)
    {
        let songInfo = VLRoomSelSongModel()
        songInfo.isChorus = inputModel.isChorus
        songInfo.songName = inputModel.songName
        songInfo.songNo = inputModel.songNo
//        songInfo.songUrl = inputModel.songUrl
        songInfo.imageUrl = inputModel.imageUrl
        songInfo.singer = inputModel.singer
        songInfo.status = 0
        /// 是谁点的歌
        songInfo.userNo = VLUserCenter.user.userNo
        songInfo.userId = UserInfo.userId
        /// 点歌人昵称
        songInfo.name = VLUserCenter.user.name
        _addChooseSongInfo(songInfo: songInfo) { error in
            // TODO(wushengtao): fetch all list can not be changed if immediately invoke
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.2) {
                completion(error)
            }
        }
    }

    func makeSongTop(withInput inputModel: KTVMakeSongTopInputModel, completion: @escaping (Error?) -> Void) {
//        assert(false)
        guard let topSong = songList.first,
              let song = songList.filter({ $0.objectId == inputModel.objectId }).first
        else {
            agoraAssert("make song to top not found! \(inputModel.songNo)")
            return
        }

        // mark input song to top
        let targetSort = (_minSort() ?? 0) - 1
        song.sort = "\(targetSort)"

        if topSong.objectId != song.objectId {
            topSong.sort = "\(targetSort - 1)"
            _updateChooseSong(songInfo: topSong) { error in
            }
        }

        // mark current playing song to top
        _updateChooseSong(songInfo: song) { error in
            completion(error)
        }
    }
    
    
    //MARK: about lyrics
    func becomeSolo() {
        _markSoloSongIfNeed()
    }
    
    func updateSingingScore(withTotalVolume totalVolume: Double) {
//        assertionFailure()
        guard let topSong = self.songList.first else {
//            assertionFailure()
            return
        }
        
        topSong.status = 2
        topSong.score = totalVolume
        _updateChooseSong(songInfo: topSong) { error in
            
        }
    }

    //MARK: subscribe
    func subscribeUserListCount(changed changedBlock: @escaping (UInt) -> Void) {
        userListCountDidChanged = changedBlock
    }

    func subscribeSeatList(changed changedBlock: @escaping (UInt, VLRoomSeatModel) -> Void) {
        seatListDidChanged = changedBlock
    }

    func subscribeRoomStatus(changed changedBlock: @escaping (UInt, VLRoomListModel) -> Void) {
        roomStatusDidChanged = changedBlock

        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
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
                           self?.roomStatusDidChanged?(KTVSubscribeUpdated.rawValue, model)
                       }, onDeleted: { [weak self] object in
                           guard let model = self?.roomList?.filter({ $0.roomNo == object.getId()}).first,
                                 model.roomNo == channelName
                           else {
                               return
                           }
                           self?.roomStatusDidChanged?(KTVSubscribeDeleted.rawValue, model)
                       }, onSubscribed: {}, fail: { error in
                       })
    }

    func subscribeChooseSong(changed changedBlock: @escaping (UInt, VLRoomSelSongModel) -> Void) {
        chooseSongDidChanged = changedBlock
    }


    

    // MARK: Deprecated protocol method
//    func publishChooseSongEvent() {
////        assertionFailure()
//        // replace with subscribeChooseSong()
//    }
//
//    func leaveChannel() {
////        assert(false)
//        // ignore
//    }
//
//    func publishMuteEvent(withMuteStatus muteStatus: Bool, completion: @escaping (Error?) -> Void) {
//        // replace with muteWithMuteStatus
//    }
//
//    func publishVideoOpenEvent(withOpenStatus openStatus: Bool, completion: @escaping (Error?) -> Void) {
//        // replace with openVideoStatus()
//    }
//
//    func publishSongDidChangedEvent(withOwnerStatus isMaster: Bool) {
//        // replace with subscribeChooseSong()
//    }
//
//
//    func publishJoinToChorus(completion: @escaping (Error?) -> Void) {
//        //replace with joinChorusWithInput
////        assertionFailure()
//    }
//
//    func publishSongOwner(withOwnerId userNo: String) {
////        assertionFailure()
//        //ignore
//    }
//
//
//    func subscribeRtmMessage(statusChanged changedBlock: @escaping (AgoraRtmChannel, AgoraRtmMessage, AgoraRtmMember) -> Void) {
////        assert(false)
//    }
}


//MARK: room operation
extension KTVSyncManagerServiceImp {
    private func _leaveRoom(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        _removeUser { error in
            // TODO(wushengtao): whitout callback
//            self.updateUserCount(with: max(self.userList.count - 1, 0))
        }
        // TODO(wushengtao): bacause of removeUser can not recv callback, invoke immediately
        _updateUserCount(with: max(userList.count - 1, 0))

        if let seat = seatMap.filter({ $0.value.userNo == VLUserCenter.user.userNo }).first?.value {
            _removeSeat(seatInfo: seat) { error in
            }
        }
        
        //remove current user's choose song
        _removeAllUsersChooseSong()

        SyncUtil.leaveScene(id: channelName)
        roomNo = nil
        completion(nil)
    }

    private func _removeRoom(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
//        removeUser { error in
//            //TODO(wushengtao): whitout callback
        ////            completion(error)
//        }
//        SyncUtil.leaveScene(id: channelName)
        SyncUtil.scene(id: channelName)?.deleteScenes()
        roomNo = nil
        completion(nil)
    }
}

// MARK: User operation
extension KTVSyncManagerServiceImp {
    private func _addUserIfNeed() {
        _subscribeOnlineUsers {}
        _getUserInfo { error, userList in
            // current user already add
            if self.userList.contains(where: { $0.userNo == VLUserCenter.user.userNo }) {
                return
            }
            self._addUserInfo {
                self._getUserInfo { error, userList in
                }
            }
        }
    }

    private func _getUserInfo(finished: @escaping (Error?, [VLLoginModel]?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                let users = list.compactMap({ VLLoginModel.yy_model(withJSON: $0.toJson()!)! })
//            guard !users.isEmpty else { return }
                self?.userList = users
                self?._updateUserCount(completion: { error in

                })
                finished(nil, users)
            }, fail: { error in
                print("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _addUserInfo(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            print("addUserInfo channelName = nil")
            return
        }
        let model = VLUserCenter.user

        let params = model.yy_modelToJSONObject() as! [String: Any]
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION).add(data: params, success: { object in
            finished()
        }, fail: { error in
            print(error.message)
            finished()
        })
    }

    private func _subscribeOnlineUsers(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { object in
                           guard let jsonStr = object.toJson(), let model = VLLoginModel.yy_model(withJSON: jsonStr) else { return }
                           if self.userList.contains(where: { $0.userNo == model.userNo }) { return }
                           self.userList.append(model)
                           self._updateUserCount { error in
                           }
                       }, onDeleted: { object in
                           if let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) {
                               self.userList.remove(at: index)
                               self._updateUserCount { error in
                               }
                           }
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                           finished()
                       }, fail: { error in
                           ToastView.show(text: error.message)
                           finished()
                       })
    }

    private func _removeUser(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        let objectId = userList.filter({ $0.userNo == UserInfo.userId && $0.objectId != nil }).first?.objectId ?? ""
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .delete(id: objectId,
                    success: {
                        completion(nil)
                    },
                    fail: { error in
                        completion(NSError(domain: error.message, code: error.code))
                    })
    }

    private func _updateUserCount(completion: @escaping (Error?) -> Void) {
        _updateUserCount(with: userList.count)
    }

    private func _updateUserCount(with count: Int) {
        guard let channelName = roomNo,
              let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first
        else {
//            assert(false, "channelName = nil")
            print("updateUserCount channelName = nil")
            return
        }
        let roomPeopleNum = "\(count)"
        if roomPeopleNum == roomInfo.roomPeopleNum {
            return
        }
        roomInfo.updatedAt = "\(Date().timeIntervalSince1970)"
        roomInfo.roomPeopleNum = roomPeopleNum
        var params = roomInfo.yy_modelToJSONObject() as! [String: Any]
//        SyncUtil
//            .scene(id: channelName)?
//            .collection(className: "")
//            .add(data: params,
//                 success: { object in
//                completion(nil)
//            }, fail: { error in
//                completion(error)
//            })

        params["objectId"] = channelName
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { obj in
                        print("updateUserCount success")
                    },
                    fail: { error in
                        print("updateUserCount fail")
                    })

        userListCountDidChanged?(UInt(count))
    }
}

// MARK: Seat operation

extension KTVSyncManagerServiceImp {
    private func _emptySeats() -> [VLRoomSeatModel] {
        var seatArray = [VLRoomSeatModel]()
        for i in 0...7 {
            let seat = VLRoomSeatModel()
            seat.onSeat = i
//            seat.objectId = "\(i)"
            seatArray.append(seat)

            seatMap["\(i)"] = seat
        }

        return seatArray
    }

    private func _getUserSeatInfo(seatIndex: Int) -> VLRoomSeatModel {
        let user = VLUserCenter.user
        let seatInfo = VLRoomSeatModel()
        seatInfo.onSeat = seatIndex
        seatInfo.id = user.id
        seatInfo.userNo = user.userNo
        seatInfo.headUrl = user.headUrl
        seatInfo.name = user.name
        /// 是否合唱
        seatInfo.joinSing = false
        /// 是否自己静音
        seatInfo.isSelfMuted = 0
        /// 是否开启视频
        seatInfo.isVideoMuted = 0

        /// 新增, 判断当前歌曲是否是自己点的
        seatInfo.ifSelTheSingSong = false

        seatInfo.ifJoinedChorus = false

        return seatInfo
    }

    private func _autoOnSeatIfNeed() {
        _subscribeSeats {}

        _getSeatInfo { [weak self] error, list in
            guard let self = self else {
                return
            }

            // mock callback
            self.seatMap.forEach { (key: String, value: VLRoomSeatModel) in
                if value.objectId == nil {
                    return
                }

                self.seatListDidChanged?(KTVSubscribeCreated.rawValue, value)
            }

            // update seat info (user avater/nick name did changed) if seat existed
            if let seat = self.seatMap.filter({ $0.value.userNo == VLUserCenter.user.userNo }).first?.value {
                let targetSeatInfo = self._getUserSeatInfo(seatIndex: seat.onSeat)
                targetSeatInfo.objectId = seat.objectId
                self._updateSeat(seatInfo: targetSeatInfo) { error in
                }
                return
            }
            guard VLUserCenter.user.ifMaster else {
                return
            }

            // add master to first seat
            let targetSeatInfo = self._getUserSeatInfo(seatIndex: 0)
            self._addSeatInfo(seatInfo: targetSeatInfo) { error in
            }
        }
    }

    private func _getSeatInfo(finished: @escaping (Error?, [VLRoomSeatModel]?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .get(success: { [weak self] list in
                guard let self = self else {
                    return
                }
                let seats = list.compactMap({ VLRoomSeatModel.yy_model(withJSON: $0.toJson()!)! })
                seats.forEach { seat in
                    self.seatMap["\(seat.onSeat)"] = seat
                }

                finished(nil, seats)
            }, fail: { error in
                print("error = \(error.description)")
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
            print("updateSeatInfo channelName = nil")
            return
        }

        let params = seatInfo.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .update(id: objectId,
                    data: params,
                    success: {
//                        finished(nil)
                    }, fail: { error in
//                        finished(NSError(domain: error.message, code: error.code))
                    })
        //TODO(wushengtao): callbacll never received, mock it
        DispatchQueue.main.async {
            finished(nil)
        }
    }

    private func _removeSeat(seatInfo: VLRoomSeatModel,
                            finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo,
              let objectId = seatInfo.objectId
        else {
//            assert(false, "channelName = nil")
            print("removeSeat channelName = nil")
            return
        }

        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .delete(id: objectId,
                    success: {
                        finished(nil)
                    }, fail: { error in
                        finished(NSError(domain: error.message, code: error.code))
                    })
    }

    private func _addSeatInfo(seatInfo: VLRoomSeatModel,
                             finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            print("addUserInfo channelName = nil")
            return
        }

        let params = seatInfo.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INFO)
            .add(data: params,
                 success: { obj in
                     finished(nil)
                 }, fail: { error in
                     finished(error)
                 })
    }

    private func _subscribeSeats(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_SEAT_INFO,
                       onCreated: { [weak self] object in
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = VLRoomSeatModel.yy_model(withJSON: jsonStr)
                           else {
                               return
                           }
                           self.seatMap["\(model.onSeat)"] = model
                           self.seatListDidChanged?(KTVSubscribeCreated.rawValue, model)
                       }, onUpdated: { [weak self] object in
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = VLRoomSeatModel.yy_model(withJSON: jsonStr)
                           else {
                               return
                           }
                           self.seatMap["\(model.onSeat)"] = model
                           self.seatListDidChanged?(KTVSubscribeUpdated.rawValue, model)
                       }, onDeleted: { [weak self] object in
                           guard let self = self else {
                               return
                           }
                           let objectId = object.getId()
                           guard let origSeat = self.seatMap.filter({ $0.value.objectId == objectId }).first?.value else {
                               print("delete seat not found")
                               return
                           }
                           let seat = VLRoomSeatModel()
                           seat.onSeat = origSeat.onSeat
                           self.seatMap["\(origSeat.onSeat)"] = seat
                           self.seatListDidChanged?(KTVSubscribeDeleted.rawValue, origSeat)
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                           finished()
                       }, fail: { error in
                           ToastView.show(text: error.message)
                           finished()
                       })
    }
}

// MARK: Choose song operation

extension KTVSyncManagerServiceImp {
    private func _minSort() -> Int? {
        var sort: Int?
        songList.forEach { model in
            let _sort = Int(model.sort) ?? 0
            sort = sort == nil ? _sort : min(sort!, _sort)
        }

        return sort
    }

    private func _sortChooseSongList() {
        songList = songList.sorted(by: { Int($0.sort)! < Int($1.sort)! })
    }

    private func _getChooseSongInfo(finished: @escaping (Error?, [VLRoomSelSongModel]?) -> Void) {
        guard let channelName = roomNo else {
//            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .get(success: { [weak self] list in
                guard let self = self else {
                    return
                }
                self.songList = list.compactMap({ VLRoomSelSongModel.yy_model(withJSON: $0.toJson()!)! })
                self._sortChooseSongList()
                let songList = self.songList
                finished(nil, songList)
            }, fail: { error in
                print("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _updateChooseSong(songInfo: VLRoomSelSongModel,
                                  finished: @escaping (Error?) -> Void)
    {
        guard let channelName = roomNo, let objectId = songInfo.objectId else {
//            assert(false, "channelName = nil")
            print("update song channelName = nil")
            return
        }

        let params = songInfo.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .update(id: objectId,
                    data: params,
                    success: {
                        // TODO(wushengtao): missing callback
//                        finished(nil)
                    }, fail: { error in
//                        finished(error)
                        // TODO(wushengtao): missing callback
                    })

        // TODO(wushengtao): mock
        finished(nil)
    }

    private func _addChooseSongInfo(songInfo: VLRoomSelSongModel, finished: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            print("addUserInfo channelName = nil")
            return
        }
        let targetSort = (Int(songList.last?.sort ?? "-1") ?? -1) + 1
        songInfo.sort = "\(targetSort)"
        let params = songInfo.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .add(data: params,
                 success: { obj in
                     finished(nil)
                 }, fail: { error in
                     finished(error)
                 })
    }
    
    private func _removeAllUsersChooseSong() {
        let userSongLists = self.songList.filter({ $0.userNo == VLUserCenter.user.userNo})
        userSongLists.forEach { model in
            self._removeChooseSong(songId: model.objectId) { error in
                
            }
        }
        
    }

    private func _removeChooseSong(songId: String?, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo,
              let objectId = songId
        else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_CHOOSE_SONG_INFO)
            .delete(id: objectId,
                    success: {
                        // TODO(wsushengtao) callback foreach loop
//                completion(nil)
                        print("removeChooseSong success")
                    },
                    fail: { error in
                        completion(NSError(domain: error.message, code: error.code))
                    })

        // TODO(wushengtao)mock
        completion(nil)
    }

    private func _markCurrentSongIfNeed() {
        guard let topSong = songList.first,
              topSong.status == 0, // ready status
              topSong.isChorus == false,
              topSong.userNo == VLUserCenter.user.userNo
        else {
            return
        }

        topSong.status = 2
        _updateChooseSong(songInfo: topSong) { error in
        }
    }

    private func _markSoloSongIfNeed() {
        guard let topSong = songList.first,
              topSong.isChorus == true, // current is chorus
              topSong.userNo == VLUserCenter.user.userNo
        else {
            return
        }

        topSong.isChorus = false
        topSong.status = 2
        _updateChooseSong(songInfo: topSong) { error in
        }
    }

    private func _subscribeChooseSong(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_CHOOSE_SONG_INFO,
                       onCreated: { [weak self] object in
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = VLRoomSelSongModel.yy_model(withJSON: jsonStr)
                           else {
                               return
                           }
                           self.songList.append(model)
                           self._sortChooseSongList()
                           self.chooseSongDidChanged?(KTVSubscribeCreated.rawValue, model)
                           self._markCurrentSongIfNeed()
                       }, onUpdated: { [weak self] object in
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = VLRoomSelSongModel.yy_model(withJSON: jsonStr)
                           else {
                               return
                           }
                           self.songList = self.songList.filter({ $0.objectId != model.objectId })
                           self.songList.append(model)
                           self._sortChooseSongList()
                           self.chooseSongDidChanged?(KTVSubscribeUpdated.rawValue, model)
                           self._markCurrentSongIfNeed()
                       }, onDeleted: { [weak self] object in
                           guard let self = self,
                                 let origSong = self.songList.filter({ $0.objectId == object.getId()}).first
                           else {
                               return
                           }
                           self.songList = self.songList.filter({ $0.objectId != origSong.objectId })
                           self.chooseSongDidChanged?(KTVSubscribeDeleted.rawValue, origSong)
                           self._markCurrentSongIfNeed()
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                           finished()
                       }, fail: { error in
                           ToastView.show(text: error.message)
                           finished()
                       })
    }
}
