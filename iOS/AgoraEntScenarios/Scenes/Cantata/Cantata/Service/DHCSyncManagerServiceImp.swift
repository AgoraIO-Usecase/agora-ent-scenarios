//
//  KTVSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

import Foundation
import YYCategories
import SVProgressHUD
import AgoraCommon

private let kSceneId = "scene_cantata_6.0.0"

/// 座位信息
private let SYNC_MANAGER_SEAT_INFO = "seat_info"
// 选歌
private let SYNC_MANAGER_CHOOSE_SONG_INFO = "choose_song"

private let SYNC_MANAGER_USER_COLLECTION = "userCollection"

// 简化的房间用户模型，只包含房间中需要的用户信息
public class RoomUserModel: NSObject {
    var id: String = ""              // 用户唯一标识符
    var name: String = ""            // 用户名称
    var headUrl: String = ""         // 用户头像URL
    var userNo: String = ""          // 用户编号（可选）
    var objectId: String = ""        // 同步系统中的对象ID
    
    override init() {
        super.init()
    }
    
    convenience init(from json: [String: Any]) {
        self.init()
        if let id = json["id"] as? String {
            self.id = id
        }
        if let name = json["name"] as? String {
            self.name = name
        }
        if let headUrl = json["headUrl"] as? String {
            self.headUrl = headUrl
        }
        if let userNo = json["userNo"] as? String {
            self.userNo = userNo
        }
        if let objectId = json["objectId"] as? String {
            self.objectId = objectId
        }
    }
    
    static func from(jsonStr: String) -> RoomUserModel? {
        guard let data = jsonStr.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] else {
            return nil
        }
        let model = RoomUserModel(from: json)
        return model
    }
    
    func toDictionary() -> [String: Any] {
        if objectId.isEmpty {
            objectId = UUID().uuidString
        }
        
        var dict: [String: Any] = [:]
        dict["id"] = id
        dict["name"] = name
        dict["headUrl"] = headUrl
        dict["userNo"] = userNo
        dict["objectId"] = objectId
        return dict
    }
    
    // 从VLUserCenter.user创建实例的便利构造器
    static func fromCurrentUser() -> RoomUserModel {
        let model = RoomUserModel()
        let user = VLUserCenter.user
        model.id = user.id
        model.name = user.name ?? ""
        model.headUrl = user.headUrl ?? ""
        model.userNo = user.userNo ?? ""
        model.objectId = UUID().uuidString
        return model
    }
}

private func agoraAssert(_ message: String) {
    agoraAssert(false, message)
}

private func agoraAssert(_ condition: Bool, _ message: String) {
    CantataLog.error(text: message, tag: "KTVService")
    #if DEBUG
//    assert(condition, message)
    #else
    #endif
}

private func agoraPrint(_ message: String) {
//    #if DEBUG
    CantataLog.info(text: message, tag: "KTVService")
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

@objc public class DHCSyncManagerServiceImp: NSObject, KTVServiceProtocol {

    private var roomList: [VLRoomListModel]?
    private var userList: [RoomUserModel] = .init() // 修改为使用RoomUserModel
    private var seatMap: [String: VLRoomSeatModel] = .init()
    private var songList: [VLRoomSelSongModel] = .init()

    private var userListCountDidChanged: ((UInt) -> Void)?
    private var userDidChanged: ((KTVSubscribe, RoomUserModel) -> Void)? // 修改为使用RoomUserModel
    private var seatListDidChanged: ((KTVSubscribe, VLRoomSeatModel) -> Void)?
    private var roomStatusDidChanged: ((KTVSubscribe, VLRoomListModel) -> Void)?
    private var chooseSongDidChanged: ((KTVSubscribe, VLRoomSelSongModel, [VLRoomSelSongModel]) -> Void)?
//    private var singingScoreDidChanged: ((Double) -> Void)?
    private var networkDidChanged: ((KTVServiceNetworkStatus) -> Void)?
    private var roomExpiredDidChanged: (() -> Void)?

    private var roomNo: String?
    private var expireTimer: Timer?
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
        SyncUtil
            .scene(id: channelName)?
            .unsubscribe(key: SYNC_MANAGER_USER_COLLECTION)
        SyncUtil
            .scene(id: channelName)?
            .unsubscribe(key: SYNC_MANAGER_SEAT_INFO)
        SyncUtil
            .scene(id: channelName)?
            .unsubscribe(key: SYNC_MANAGER_CHOOSE_SONG_INFO)
        
        userListCountDidChanged = nil
        seatListDidChanged = nil
        roomStatusDidChanged = nil
        chooseSongDidChanged = nil
//        singingScoreDidChanged = nil
        networkDidChanged = nil
        roomExpiredDidChanged = nil
        expireTimer?.invalidate()
        expireTimer = nil
    }
    
    private func _checkRoomExpire() -> Bool {
        guard let room = self.room else { return false}
        
        let currentTs = Int64(Date().timeIntervalSince1970 * 1000)
        let expiredDuration = 20 * 60 * 1000
        agoraPrint("checkRoomExpire: \(currentTs - room.createdAt) / \(expiredDuration)")
        print("checkRoomExpire: \(currentTs - room.createdAt) / \(expiredDuration)")
        guard currentTs - room.createdAt > expiredDuration else { return  false}
        return true
    }

    private func initScene(completion: @escaping (NSError?) -> Void) {
        if syncUtilsInited {
            completion(nil)
            return
        }

        let appId = AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId
        SyncUtil.initSyncManager(sceneId: kSceneId, appId: appId) {
        }
        
        SyncUtil.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            defer {
                completion(state == .open ? nil : NSError(domain: "network error", code: 1000))
            }
            
            agoraPrint("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.networkDidChanged?(KTVServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                self._getUserInfo { err, list in
                    self.userListCountDidChanged?(UInt(list?.count ?? 0))
                }
                //延迟执行
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    self._seatListReloadIfNeed()
                }
                return
            }
            
            self.syncUtilsInited = true
          //  completion()
        }
    }
    
    // MARK: protocol method
    
    // MARK: room info
    public func getRoomList(with page: UInt, completion: @escaping (Error?, [VLRoomListModel]?) -> Void) {
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
                    return VLRoomListModel.yy_model(with: info.toJson()!.toDictionary())!
                })
                self.roomList = dataArray.sorted(by: { ($0.updatedAt > 0 ? $0.updatedAt : $0.createdAt) > ($1.updatedAt > 0 ? $1.updatedAt : $0.createdAt) })
                completion(nil, self.roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }

    public func createRoom(with inputModel: KTVCreateRoomInputModel,
                    completion: @escaping (Error?, KTVCreateRoomOutputModel?) -> Void)
    {
        let roomInfo = VLRoomListModel() // LiveRoomInfo(roomName: inputModel.name)
//        roomInfo.id = VLUserCenter.user.id//NSString.withUUID().md5() ?? ""
        roomInfo.name = inputModel.name
        roomInfo.isPrivate = inputModel.isPrivate?.intValue != 0
        roomInfo.password = inputModel.password
        roomInfo.creatorNo = VLUserCenter.user.id
        roomInfo.roomNo = "\(arc4random_uniform(899999) + 100000)" // roomInfo.id
        roomInfo.bgOption = 0
        roomInfo.roomPeopleNum = "0"
        roomInfo.icon = inputModel.icon
        roomInfo.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        roomInfo.creatorName = VLUserCenter.user.name
        roomInfo.creatorAvatar = VLUserCenter.user.headUrl
        roomInfo.streamMode = inputModel.streamMode
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
                var token1:String? = nil
                var token2:String? = nil
                var token3:String? = nil
                
                let dispatchGroup = DispatchGroup()
                dispatchGroup.enter()
                NetworkManager.shared.generateToken(channelName: channelName ?? "",
                                                    appId: AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId,
                                                    uid: "\(UserInfo.userId)",
                                                    tokenTypes: [.rtc, .rtm]) { token in
                    token1 = token
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateToken(channelName: "\(channelName ?? "")_ad",
                                                    appId: AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId,
                                                    uid: "\(UserInfo.userId)",
                                                    tokenTypes: [.rtc]) { token in
                    token2 = token
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateToken(channelName: "\(channelName ?? "")",
                                                    appId: AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId,
                                                    uid: "2023",
                                                    tokenTypes: [.rtc]) { token in
                    token3 = token
                    dispatchGroup.leave()
                }
                
                dispatchGroup.notify(queue: .main){
                    agoraPrint("createRoom get token cost: \(-date.timeIntervalSinceNow * 1000) ms")
                    guard let self = self,
                          let rtcToken = token1,
                          let rtmToken = token1,
                          let audienceToken = token2,
                          let rtcPlayerToken = token3
                    else {
                        agoraAssert(token1 != nil, "rtcToken == nil || rtmToken == nil")
                        agoraAssert(token2 != nil, "playerRtcToken == nil")
                        _hideLoadingIfNeed()
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.id == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    VLUserCenter.user.agoraPlayerRTCToken = rtcPlayerToken
                    VLUserCenter.user.audienceChannelToken = audienceToken
                    self.roomList?.append(roomInfo)
                    self._autoOnSeatIfNeed { seatArray in
                        agoraPrint("createRoom _autoOnSeatIfNeed cost: \(-date.timeIntervalSinceNow * 1000) ms")
                        _hideLoadingIfNeed()
                        let output = KTVCreateRoomOutputModel()
                        output.name = inputModel.name
                        output.roomNo = roomInfo.roomNo ?? ""
                        output.seatsArray = seatArray
                        output.creatorAvatar = inputModel.creatorAvatar
                        completion(nil, output)
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: {
                        self._addUserIfNeed()
                    })
                    
//                    self._subscribeChooseSong {}
                }
            } fail: { error in
                _hideLoadingIfNeed()
                completion(error, nil)
            }
        }
    }

    public func joinRoom(with inputModel: KTVJoinRoomInputModel,
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
                var token1:String? = nil
                var token2:String? = nil
                var token3:String? = nil
                
                let dispatchGroup = DispatchGroup()
                dispatchGroup.enter()
                NetworkManager.shared.generateToken(channelName: channelName ?? "",
                                                    appId: AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId,
                                                    uid: "\(UserInfo.userId)",
                                                    tokenTypes: [.rtc, .rtm]) { token in
                    token1 = token
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateToken(channelName: "\(channelName ?? "")_ad",
                                                    appId: AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId,
                                                    uid: "\(UserInfo.userId)",
                                                    tokenTypes: [.rtc]) { token in
                    token2 = token
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateToken(channelName: "\(channelName ?? "")",
                                                    appId: AppContext.shared.sceneConfig?.cantataAppId ?? AppContext.shared.appId,
                                                    uid: "2023",
                                                    tokenTypes: [.rtc]) { token in
                    token3 = token
                    dispatchGroup.leave()
                }
                
                dispatchGroup.notify(queue: .main){
                    agoraPrint("joinRoom get token cost: \(-date.timeIntervalSinceNow * 1000) ms")
                    guard let self = self,
                          let rtcToken = token1,
                          let rtmToken = token1,
                          let audienceToken = token2,
                          let rtcPlayerToken = token3
                    else {
                        _hideLoadingIfNeed()
                        agoraAssert(token1 != nil, "rtcToken == nil || rtmToken == nil")
                        agoraAssert(token2 != nil, "playerRtcToken == nil")
                        completion(nil, nil)
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.id == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    VLUserCenter.user.agoraPlayerRTCToken = rtcPlayerToken
                    VLUserCenter.user.audienceChannelToken = audienceToken
                    self._autoOnSeatIfNeed { seatArray in
                        agoraPrint("joinRoom _autoOnSeatIfNeed cost: \(-date.timeIntervalSinceNow * 1000) ms")
                        _hideLoadingIfNeed()
                        let output = KTVJoinRoomOutputModel()
                        output.creatorNo = userId
                        output.seatsArray = seatArray
                        output.streamMode = inputModel.streamMode
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
    
    public func leaveRoom(completion: @escaping (Error?) -> Void) {
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

    public func changeMVCover(with inputModel: KTVChangeMVCoverInputModel,
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
    public func enterSeat(with inputModel: KTVOnSeatInputModel,
                   completion: @escaping (Error?) -> Void) {
        let seatInfo = _getUserSeatInfo(seatIndex: Int(inputModel.seatIndex))
        seatInfo.isAudioMuted = 0
        _addSeatInfo(seatInfo: seatInfo,
                     finished: completion)
    }

    public func leaveSeat(with inputModel: KTVOutSeatInputModel,
                   completion: @escaping (Error?) -> Void) {
        guard let userNo = inputModel.userNo else {return}
        let seatInfo = seatMap["\(userNo)"]!
        _removeSeat(seatInfo: seatInfo) { error in
        }
        
        //remove current user's choose song
        _removeAllUserChooseSong(userNo: seatInfo.userNo ?? "")
        completion(nil)
    }
    
    public func leaveSeatWithoutRemoveSong(with seatModel: VLRoomSeatModel, completion: @escaping (Error?) -> Void) {
        _removeSeat(seatInfo: seatModel) { error in
        }
        completion(nil)
    }
    
    public func updateSeatAudioMuteStatus(with muted: Bool,
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

    public func updateSeatVideoMuteStatus(with muted: Bool,
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

    public func updateSeatScoreStatus(with score: Int,
                                   completion: @escaping (Error?) -> Void) {
        guard let seatInfo = self.seatMap
            .filter({ $0.value.userNo == VLUserCenter.user.id })
            .first?.value else {
            agoraAssert("open video seat not found")
            completion(nil)
            return
        }
        
        seatInfo.score = score
        _updateSeat(seatInfo: seatInfo,
                    finished: completion)
    }
    
    public func updateSongEndStatus(with musicEnd: Bool, inputModel: KTVRemoveSongInputModel, completion: @escaping (Error?) -> Void) {
        guard let topSong = songList.first,
              let song = songList.filter({ $0.objectId == inputModel.objectId }).first
        else {
            agoraAssert("make song to top not found! \(inputModel.songNo)")
            completion(nil)
            return
        }

        song.musicEnded = musicEnd
        _updateChooseSong(songInfo: song) { error in
            completion(error)
        }
    }
    
    // MARK: choose songs
    public func removeSong(with inputModel: KTVRemoveSongInputModel,
                    completion: @escaping (Error?) -> Void) {
        _removeChooseSong(songId: inputModel.objectId,
                          completion: completion)
    }

    public func getChoosedSongsList(completion: @escaping (Error?, [VLRoomSelSongModel]?) -> Void) {
        _getChooseSongInfo(finished: completion)
    }

    public func joinChorus(with inputModel: KTVJoinChorusInputModel,
                        completion: @escaping (Error?) -> Void) {
        guard let topSong = self.songList.filter({ $0.songNo == inputModel.songNo}).first else {
            agoraAssert("join Chorus fail")
            completion(nil)
            return
        }
        //TODO: _markSeatToPlaying without callback
        _markSeatChoursStatus(songCode: topSong.chorusSongId(),  completion: completion)
    }
    
    public func coSingerLeaveChorus(completion: @escaping (Error?) -> Void) {
        //TODO: _markSeatToPlaying without callback
        _markSeatChoursStatus(songCode: "", completion: completion)
    }

    public func markSongDidPlay(with inputModel: VLRoomSelSongModel,
                         completion: @escaping (Error?) -> Void) {
        inputModel.status = .playing
        _updateChooseSong(songInfo: inputModel, finished: completion)
    }

    public func chooseSong(with inputModel: KTVChooseSongInputModel,
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
    
    public func pinSong(with inputModel: KTVMakeSongTopInputModel,
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
    public func enterSoloMode() {
        _markSoloSongIfNeed()
    }

    //MARK: subscribe
    public func subscribeUserListCountChanged(with changedBlock: @escaping (UInt) -> Void) {
//        _unsubscribeAll()
        userListCountDidChanged = changedBlock
        _subscribeOnlineUsers {
        }
    }
    
    public func subscribeUserChanged(with changedBlock: @escaping (KTVSubscribe, RoomUserModel) -> Void) {
        userDidChanged = changedBlock
    }

    public func subscribeSeatListChanged(with changedBlock: @escaping (KTVSubscribe, VLRoomSeatModel) -> Void) {
        seatListDidChanged = changedBlock
        _subscribeSeats {
        }
    }
    
    public func subscribeRoomStatusChanged(with changedBlock: @escaping (KTVSubscribe, VLRoomListModel) -> Void) {
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

    public func subscribeChooseSongChanged(with changedBlock: @escaping (KTVSubscribe, VLRoomSelSongModel, [VLRoomSelSongModel]) -> Void) {
        chooseSongDidChanged = changedBlock
        _subscribeChooseSong {
        }
    }
    
    public func subscribeNetworkStatusChanged(with changedBlock: @escaping (KTVServiceNetworkStatus) -> Void) {
        networkDidChanged = changedBlock
    }
    
    public func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void) {
        expireTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] timer in
            guard let self = self else { return }
            
            if self.checkAndHandleRoomExpire(changedBlock: changedBlock) {
                timer.invalidate()
                self.expireTimer = nil
            }
        }
        
        // 立即执行一次检查到期的方法
        checkAndHandleRoomExpire(changedBlock: changedBlock)
    }

    private func checkAndHandleRoomExpire(changedBlock: @escaping () -> Void) -> Bool {
        guard let room = self.room else { return false }
        let currentTs = Int64(Date().timeIntervalSince1970 * 1000)
        let expiredDuration = 20 * 60 * 1000
        agoraPrint("checkRoomExpire: \(currentTs - room.createdAt) / \(expiredDuration)")
        
        if abs(currentTs - room.createdAt) > expiredDuration {
            expireTimer?.invalidate()
            expireTimer = nil
            changedBlock()
            return true
        }
        
        return false
    }
    
    public func unsubscribeAll() {
        _unsubscribeAll()
    }
}


//MARK: room operation
extension DHCSyncManagerServiceImp {
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
        expireTimer?.invalidate()
        expireTimer = nil
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
extension DHCSyncManagerServiceImp {
    private func _addUserIfNeed() {
//        _subscribeOnlineUsers {}
        _getUserInfo { error, userList in
            // current user already add
            if self.userList.contains(where: { $0.id == VLUserCenter.user.id }) {
                return
            }
            self._addUserInfo {
                agoraPrint("用户添加操作完成")
            }
        }
    }

    private func _getUserInfo(finished: @escaping (Error?, [RoomUserModel]?) -> Void) {
        guard let channelName = roomNo else {
            finished(nil, nil)
            return
        }
        agoraPrint("imp user get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_USER_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp user get success...")
                
                // 使用RoomUserModel.from方法解析用户数据
                let roomUsers = list.compactMap { RoomUserModel.from(jsonStr: $0.toJson()!)! }
                self?.userList = roomUsers
                
                self?._updateUserCount(completion: { error in })
                
                finished(nil, roomUsers)
            }, fail: { error in
                agoraPrint("imp user get fail :\(error.message)...")
                finished(error, nil)
            })
    }

    private func _addUserInfo(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            agoraPrint("addUserInfo channelName = nil")
            return
        }
        agoraPrint("imp user add ...")
        
        // 创建RoomUserModel实例并获取字典表示
        let params = RoomUserModel.fromCurrentUser().toDictionary()
        
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_USER_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp user add success...")
                finished()
            }, fail: { error in
                agoraPrint("imp user add fail :\(error.message)...")
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
            .subscribe(key: SYNC_MANAGER_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           agoraPrint("imp user subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let userModel = RoomUserModel.from(jsonStr: jsonStr)
                           else {
                               return
                           }
                           
                           if self.userList.contains(where: { $0.id == userModel.id }) {
                               self.userDidChanged?(.updated, userModel)
                               return
                           }
                           
                           self.userList.append(userModel)
                           self.userDidChanged?(.created, userModel)
                           self._updateUserCount { _ in }
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp user subscribe onDeleted...")
                           guard let self = self, let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) else {
                               return
                           }
                           let model = self.userList[index]
                           self.userDidChanged?(.deleted, model)
                           self.userList.remove(at: index)
                           self._updateUserCount { _ in }
                       }, onSubscribed: {
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
        
        // 查找当前用户
        guard let currentUser = userList.first(where: { $0.id == VLUserCenter.user.id }),
              !currentUser.objectId.isEmpty else {
            completion(nil)
            return
        }
        
        agoraPrint("imp user delete...")
        
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_USER_COLLECTION)
            .document(id: currentUser.objectId)
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

// MARK: Seat operation

extension DHCSyncManagerServiceImp {
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
            
            guard self.seatMap.count == 0 else {
                return
            }

            
            list.forEach { seat in
                guard let uid = seat.rtcUid else {return}
                self.seatMap["\(uid)"] = seat
            }
            
            completion(list)
            return
            
            //TODO: _getSeatInfo will callback if remove seat invoke
            
            // update seat info (user avater/nick name did changed) if seat existed
//            if let seat = self.seatMap.filter({ $0.value.userNo == VLUserCenter.user.id }).first?.value {
//                let targetSeatInfo = self._getUserSeatInfo(seatIndex: seat.seatIndex, model: seat)
//                targetSeatInfo.objectId = seat.objectId
//                self._updateSeat(seatInfo: targetSeatInfo) { error in
//                    completion(self._getInitSeats())
//                }
//                return
//            } else {
//
//            }

            guard VLUserCenter.user.ifMaster else {
                completion(self._getInitSeats())
                return
            }
            
            completion(self._getInitSeats())

            // add master to first seat
//            let targetSeatInfo = self._getUserSeatInfo(seatIndex: 0)
//            targetSeatInfo.isAudioMuted = 0
//            targetSeatInfo.isMaster = true
//            self._addSeatInfo(seatInfo: targetSeatInfo) { error in
//                completion(self._getInitSeats())
//            }
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
                guard let uid = seat.rtcUid else {return}
                _seatMap["\(uid)"] = seat
            }
            
            self.seatMap.forEach { (key, origSeat) in
                if key == nil {return}
                print(_seatMap[key])
                guard let seat = _seatMap[key] else {
                    let seat = VLRoomSeatModel()
                    _seatMap[key] = seat
                    self.seatListDidChanged?(.deleted, origSeat)
                    return
                }
                
                self.seatListDidChanged?(.updated, seat)
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
                guard let seatKey = seatInfo.rtcUid else {return}
                self?.seatMap["\(seatKey)"] = seatInfo
                finished(nil)
            }, fail: { error in
                agoraPrint("imp seat add fail...")
                finished(error)
            })
    }

    private func _subscribeSeats(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
          //  assertionFailure("channelName = nil")
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
                      let model = VLRoomSeatModel.yy_model(withJSON: jsonStr),
                      let userNo = model.userNo,
                      let uid = model.rtcUid
                else {
                    return
                }
                self.seatMap["\(uid)"] = model
                self.seatListDidChanged?(.created, model)
            }, onUpdated: { [weak self] object in
                agoraPrint("imp seat subscribe onupdated... [\(object.getId())]")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = VLRoomSeatModel.yy_model(withJSON: jsonStr),
                      let userNo = model.userNo,
                      let uid = model.rtcUid
                else {
                    return
                }
                self.seatMap["\(uid)"] = model
                self.seatListDidChanged?(.updated, model)
            }, onDeleted: { [weak self] object in
                agoraPrint("imp seat subscribe ondeleted... [\(object.getId())]")
                guard let self = self else {
                    return
                }
                let objectId = object.getId()
                if objectId.isEmpty {return}
                guard let origSeat = self.seatMap.filter({ $0.value.objectId == objectId }).first?.value else {
                    agoraPrint("delete seat not found")
                    return
                }
                let seat = VLRoomSeatModel()
                seat.seatIndex = origSeat.seatIndex
                seat.userNo = origSeat.userNo
                seat.chorusSongCode = origSeat.chorusSongCode
                guard let uid = origSeat.rtcUid else {return}
                if self.seatMap.keys.contains("\(uid)") {
                    self.seatMap.removeValue(forKey: "\(uid)")
                }
              //  self.seatMap["\(uid)"] = seat
                self.seatListDidChanged?(.deleted, seat)
                print("\(seat.userNo)已下麦")
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

extension DHCSyncManagerServiceImp {
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
                var totalList = list.compactMap({
                    VLRoomSelSongModel.yy_model(withJSON: $0.toJson()!)!
                })
                //self.songList需要剔除掉songNO或者userNo为空的model
                totalList = totalList.filter { $0.songNo != nil}
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
            CantataLog.warning(text: "_markSoloSongIfNeed break:  \(songList.first?.status.rawValue ?? 0) \(songList.first?.userNo ?? "")/\(VLUserCenter.user.id)")
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
                      let model = VLRoomSelSongModel.yy_model(withJSON: jsonStr)
                else {
                    return
                }
                let songList = self.songList.filter({ $0.objectId != model.objectId })
                let type: KTVSubscribe = songList.count == self.songList.count ? .created : .updated
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

