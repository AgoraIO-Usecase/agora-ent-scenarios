//
//  AUIArbiter.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/2.
//

import AgoraRtmKit

@objc public class AUIArbiter: NSObject {
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    private var currentUserInfo: AUIUserThumbnailInfo!
    private(set) var lockOwnerId: String = "" {
        didSet {
            notifyArbiterDidChange()
        }
    }
    private var arbiterDelegates: NSHashTable<AUIArbiterDelegate> = NSHashTable<AUIArbiterDelegate>()
    
    deinit {
        aui_info("deinit AUIArbiter", tag: "AUIArbiter")
        rtmManager.unsubscribeLock(channelName: channelName, lockName: kRTM_Referee_LockName, delegate: self)
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, userInfo: AUIUserThumbnailInfo) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.currentUserInfo = userInfo
        super.init()
        rtmManager.subscribeLock(channelName: channelName, lockName: kRTM_Referee_LockName, delegate: self)
        aui_info("init AUIArbiter", tag: "AUIArbiter")
    }
    
    public func subscribeEvent(delegate: AUIArbiterDelegate) {
        if arbiterDelegates.contains(delegate) { return }
        
        arbiterDelegates.add(delegate)
    }
    
    public func unSubscribeEvent(delegate: AUIArbiterDelegate) {
        arbiterDelegates.remove(delegate)
    }
    
    /// 创建锁
    public func create() {
        rtmManager.setLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
        }
    }
    
    /// 释放锁
    public func destroy() {
        rtmManager.removeLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
        }
    }
    
    /// 获取锁
    public func acquire() {
        rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
        }
    }
    
    /// 释放锁
    public func release() {
        rtmManager.releaseLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
        }
    }
    
    public func isArbiter() -> Bool {
        return lockOwnerId == currentUserInfo.userId
    }
}

//MARK: private
extension AUIArbiter {
    private func notifyError(error: NSError?) {
        guard let error = error else { return }
        arbiterDelegates.allObjects.forEach { delegate in
            delegate.onError(channelName: channelName, error: error)
        }
    }
    
    private func notifyArbiterDidChange() {
        arbiterDelegates.allObjects.forEach { delegate in
            delegate.onArbiterDidChange(channelName: channelName, arbiterId: self.lockOwnerId)
        }
    }
}

//MARK: AUIRtmLockProxyDelegate
extension AUIArbiter: AUIRtmLockProxyDelegate {
    public func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        aui_info("onReceiveLockDetail[\(channelName)]: \(lockDetail.owner)/\(currentUserInfo.userId)")
        guard channelName == self.channelName else {return}
        
        //如果lockOwnerId是自己，并且是切换了仲裁者(非首次获取，否则第一次roomService里onReceiveLockDetail拿到的是空)，需要刷新下
        if lockOwnerId.isEmpty == false, lockDetail.owner == currentUserInfo.userId {
            rtmManager.fetchMetaDataSnapshot(channelName: channelName) {[weak self] error in
                guard let self = self else { return }
                //TODO: error handler, retry?
                self.lockOwnerId = lockDetail.owner
            }
        } else {
            lockOwnerId = lockDetail.owner
        }
    }
    
    public func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        aui_info("onReleaseLockDetail[\(channelName)]: \(lockDetail.owner)")
        guard channelName == self.channelName else {return}
        rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
        self.lockOwnerId = ""
    }
}
