//
//  AUISceneCondition.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/5/8.
//

import Foundation


/// 加入房间成功条件判断
class AUISceneEnterCondition: NSObject {
    private var channelName: String
    private var arbiter: AUIArbiter
    var enterCompletion: (()->())?
    var lockOwnerRetrived: Bool = false {
        didSet {
            aui_info("set lockOwnerRetrived = \(lockOwnerRetrived)", tag: kSceneTag)
            checkRoomValid()
        }
    }
    var lockOwnerAcquireSuccess: Bool = false {
        didSet {
            aui_info("set lockOwnerAcquireSuccess = \(lockOwnerAcquireSuccess)", tag: kSceneTag)
            checkRoomValid()
        }
    }
    var subscribeSuccess: Bool = false {
        didSet {
            aui_info("set subscribeSuccess = \(subscribeSuccess)", tag: kSceneTag)
            checkRoomValid()
        }
    }
    
    var ownerId: String = "" {
        didSet {
            aui_info("set ownerId = \(ownerId)", tag: kSceneTag)
            AUIRoomContext.shared.roomOwnerMap[channelName] = ownerId
            checkRoomValid()
        }
    }
    
    
    init(channelName: String, arbiter: AUIArbiter) {
        self.channelName = channelName
        self.arbiter = arbiter
        super.init()
    }
    
    /*
     检查加入房间成功，需要以下条件全部满足：
     1. subscribe成功
     2. 获取到初始化信息(拿到ownerId房主uid)
     3. 锁也获取成功(acquire的callback成功收到，收到callback写metadata才能成功，如果没有收到callback但是获取到锁主，哪怕锁住是自己，写metadata还是会失败)
     4. 获取到锁主(修改metadata需要向锁主发消息)
     */
    private func checkRoomValid() {
        aui_info("checkRoomValid[\(channelName)] subscribeSuccess: \(subscribeSuccess), lockOwnerRetrived: \(lockOwnerRetrived), ownerId: \(ownerId) isArbiter: \(arbiter.isArbiter()), lockOwnerAcquireSuccess: \(lockOwnerAcquireSuccess)", tag: kSceneTag)
        guard subscribeSuccess, lockOwnerRetrived, !ownerId.isEmpty else { return }
        //如果是锁主，需要判断有没有acquire成功回调，回调后有本地对比，没有成功回调前setmetadata会失败-12008
        if arbiter.isArbiter(), lockOwnerAcquireSuccess == false {return}
        if let completion = self.enterCompletion {
            completion()
        }
    }
}

//房间过期条件判断
class AUISceneExpiredCondition: NSObject {
    private var roomExpiration: RoomExpirationPolicy
    private var channelName: String
    private var lastUpdateDate: Date?
    var roomDidExpired: (()->())?
    
    var joinCompletion: Bool = false {
        didSet {
            aui_info("set joinCompletion \(joinCompletion)", tag: kSceneTag)
            checkRoomExpired()
        }
    }
    
    var createTimestemp: UInt64? {
        didSet {
            aui_info("set createTimestemp \(createTimestemp ?? 0)", tag: kSceneTag)
            checkRoomExpired()
        }
    }
    
    var userSnapshotList: [AUIUserInfo]? {
        didSet {
            aui_info("set userSnapshotList count = \(userSnapshotList?.count ?? 0)", tag: kSceneTag)
            checkRoomExpired()
        }
    }
    
    //房主曾经离开过房间
    var ownerHasLeftRoom: Bool = false {
        didSet {
            aui_info("set ownerHasLeftRoom = \(ownerHasLeftRoom)", tag: kSceneTag)
            checkRoomExpired()
        }
    }
    
    var lastUpdateTimestemp: UInt64? {
        didSet {
            self.lastUpdateDate = Date()
            aui_info("set lastUpdateTimestemp = \(lastUpdateTimestemp ?? 0)", tag: kSceneTag)
            checkRoomExpired()
        }
    }
    
    required init(channelName: String, roomExpiration: RoomExpirationPolicy) {
        self.channelName = channelName
        self.roomExpiration = roomExpiration
        super.init()
    }
    
    
    /*
     检查房间过期，其中一个不满足表示过期,需要房间加入完成后检查（目前认为没有enter完成不做检查过期处理）：
     1.房主加入需要检查房主在用户列表里(通过who now查询)
     2.观众加入检查房主不在用户列表里
     3.房间时间过期（动态配置，不可直接写死20min）
     */
    private func checkRoomExpired() {
        aui_info("checkRoomExpired[\(channelName)] joinCompletion: \(joinCompletion), userSnapshotList count: \(userSnapshotList?.count ?? 0), createTimestemp: \(createTimestemp ?? 0)", tag: kSceneTag)
        guard joinCompletion, let userList = userSnapshotList, let createTs = createTimestemp else { return }
        
        if roomExpiration.isAssociatedWithOwnerOffline {
            let isRoomOwner = AUIRoomContext.shared.isRoomOwner(channelName: channelName)
            if isRoomOwner {
                //step 1
                if ownerHasLeftRoom {
                    aui_info("checkRoomExpired: room owner has left", tag: kSceneTag)
                    roomDidExpired?()
                }
            } else {
                //step 2
                guard let _ = userList.filter({ AUIRoomContext.shared.isRoomOwner(channelName: channelName, userId: $0.userId)}).first else {
                    //room owner not found, clean room
                    aui_info("checkRoomExpired: room owner leave", tag: kSceneTag)
                    roomDidExpired?()
                    return
                }
            }
        }
        
        //TODO: 目前检查只有enter room时，如果房主一直在房间内不会过期，是否需要内部检查，还是让上层处理
        //step 3
        if roomExpiration.expirationTime > 0, let updateTs = lastUpdateTimestemp {
            if Int64(updateTs) - Int64(createTs) > roomExpiration.expirationTime {
                aui_info("checkRoomExpired: room is expired: \(updateTs) - \(createTs) > \(roomExpiration.expirationTime)", tag: kSceneTag)
                roomDidExpired?()
                return
            }
        }
    }
    
    /*
      createTimestemp --[createDuration]--> lastUpdateTimestemp (lastUpdateTimestemp - createTimestemp = createDuration)
                                              (lastUpdateDate) --[deltaDuration]--> nowDate (nowDate - lastUpdateDate = deltaDuration)
     */
    func roomUsageDuration() -> UInt64? {
        guard let currentTs = roomCurrentTs(), let createTs = createTimestemp else {return nil}
        let duration = currentTs - createTs
        
        return duration
    }
    
    func roomCurrentTs() -> UInt64? {
        guard let updateTs = lastUpdateTimestemp, let date = lastUpdateDate else {return nil}
        let deltaDuration = UInt64(-date.timeIntervalSinceNow * 1000)

        return updateTs + deltaDuration
    }
}
