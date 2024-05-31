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
    public func create(completion: ((NSError?)-> ())? = nil) {
        rtmManager.setLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
            completion?(err)
        }
    }
    
    /// 销毁锁
    public func destroy(completion: ((NSError?)-> ())? = nil) {
        rtmManager.removeLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
            completion?(err)
        }
    }
    
    /// 获取锁
    public func acquire(completion: ((NSError?)-> ())? = nil) {
        rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
            completion?(err)
        }
    }
    
    /// 释放锁
    public func release(completion: ((NSError?)-> ())? = nil) {
        rtmManager.releaseLock(channelName: channelName, lockName: kRTM_Referee_LockName) {[weak self] err in
            self?.notifyError(error: err)
            completion?(err)
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
        aui_info("notifyError: \(error.localizedDescription)", tag: "AUIArbiter")
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
    public func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail, eventType: AgoraRtmLockEventType) {
        aui_info("onReceiveLockDetail[\(channelName)]: \(lockDetail.owner)/\(currentUserInfo.userId)")
        guard channelName == self.channelName else {return}
        /*
         下列两种情况需要刷新下metadata到最新
         1. 如果lockOwnerId是自己，需要在通知外部锁转移前刷新下
         2. 如果lockOwnerId不是自己，而之前lockOwnerId是自己，说明自己从仲裁者切换成非仲裁者了，需要通知外部后刷新下(因为collection认为是锁主的情况下是不会用远端数据的)，可能自己的本地数据没有到最新
         */
        let gotLock = lockDetail.owner == currentUserInfo.userId
        let lossLockToOthers = lockOwnerId == currentUserInfo.userId && lockDetail.owner != currentUserInfo.userId
        if gotLock {
            rtmManager.fetchMetaDataSnapshot(channelName: channelName) {[weak self] error in
                guard let self = self else { return }
                //TODO: error handler, retry?
                self.lockOwnerId = lockDetail.owner
            }
        } else if lossLockToOthers {
            self.lockOwnerId = lockDetail.owner
            rtmManager.fetchMetaDataSnapshot(channelName: channelName) {[weak self] error in
                guard let self = self else { return }
                //TODO: error handler, retry?
                
            }
        } else {
            lockOwnerId = lockDetail.owner
        }
    }
    
    public func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail, eventType: AgoraRtmLockEventType) {
        aui_info("onReleaseLockDetail[\(channelName)]: \(lockDetail.owner)")
        guard channelName == self.channelName else {return}
        rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
        //过期可能会在获取锁之后收到，导致把正确的锁主清理了，因此只在锁主是自己的时候才处理
        if eventType == .lockExpired, lockOwnerId == currentUserInfo.userId {
            self.lockOwnerId = ""
        }
        
    }
}
