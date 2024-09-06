//
//  AppStateManager.swift
//  AFNetworking
//
//  Created by wushengtao on 2024/4/30.
//

import Foundation
import Network

public class AppStateManager {
    // 前后台切换通知
    public var appStateChangeHandler: ((Bool) -> Void)?
    
    // 锁屏解锁通知
    public var screenLockHandler: ((Bool) -> Void)?
    
    // 网络通断状态回调
    public var networkStatusChangeHandler: ((Bool) -> Void)?
    
    private let monitor = NWPathMonitor()
    
    public init() {
        // 注册应用状态改变通知
        NotificationCenter.default.addObserver(self, selector: #selector(appStateChanged), name: UIApplication.didEnterBackgroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(appStateChanged), name: UIApplication.didBecomeActiveNotification, object: nil)
        
        // 注册锁屏解锁通知
        NotificationCenter.default.addObserver(self, selector: #selector(screenLocked), name: UIApplication.willResignActiveNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(screenUnlocked), name: UIApplication.didBecomeActiveNotification, object: nil)
        
        // 监听网络状态改变
        monitor.pathUpdateHandler = { [weak self] path in
            let isNetworkAvailable = path.status == .satisfied
            CommonLogger.info("networkChange isNetworkAvailable: \(isNetworkAvailable)", tag: "AppStateManager")
            DispatchQueue.main.async {
                self?.networkStatusChangeHandler?(isNetworkAvailable)
            }
        }
        let queue = DispatchQueue(label: "NetworkMonitor")
        monitor.start(queue: queue)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        monitor.cancel()
    }
    
    // 应用状态改变通知处理
    @objc private func appStateChanged() {
        let isInBackground = UIApplication.shared.applicationState == .background
        CommonLogger.info("appStateChanged isInBackground: \(isInBackground)", tag: "AppStateManager")
        appStateChangeHandler?(isInBackground)
    }
    
    // 锁屏通知处理
    @objc private func screenLocked() {
        CommonLogger.info("screenLocked", tag: "AppStateManager")
        screenLockHandler?(true)
    }
    
    // 解锁通知处理
    @objc private func screenUnlocked() {
        CommonLogger.info("screenUnlocked", tag: "AppStateManager")
        screenLockHandler?(false)
    }
}
