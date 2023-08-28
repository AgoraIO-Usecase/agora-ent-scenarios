//
//  AppContext.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import Bugly

@objcMembers public class AppContext: NSObject {
    @objc public static let shared: AppContext = .init()
    @objc public var sceneLocalizeBundleName: String?
    @objc public var sceneImageBundleName: String?
    @objc public var extDic: NSMutableDictionary = NSMutableDictionary()
    @objc public var isDebugMode = false
    @objc var imageCahe = [String: AnyObject]()
    @objc public var localizedCache = [String: String]()
    
    private var _appId: String = ""
    private var _certificate: String = ""
    private var _imAppKey: String = ""
    private var _imClientId: String = ""
    private var _imClientSecret: String = ""
    private var _cloudPlayerKey: String = ""
    private var _cloudPlayerSecret: String = ""
    private var _hostUrl: String = ""
    @objc public var isAgreeLicense: Bool = false {
        didSet {
            guard isAgreeLicense else {
                return
            }
            setupBugly()
        }
    }
    
    override init() {
        super.init()
        
        if VLUserCenter.shared().isLogin() {
            setupBugly()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupBugly() {
#if DEBUG
#else
        let config = BuglyConfig()
        config.reportLogLevel = BuglyLogLevel.warn
        config.unexpectedTerminatingDetectionEnable = true
        config.debugMode = true
        Bugly.start(withAppId: "e188384728", config: config)
#endif
    }

    @objc func getLang() -> String {
        guard let lang = NSLocale.preferredLanguages.first else {
            return "en"
        }

        if lang.contains("zh") {
            return "zh-Hans"
        }

        return "en"
    }

    // MARK: user

    // MARK: App Config

    @objc public var appId: String {
        get {
            return _appId
        }
        set {
            _appId = newValue
        }
    }
    
    @objc public var certificate: String {
        get {
            return _certificate
        }
        set {
            _certificate = newValue
        }
    }
    
    @objc public var imAppKey: String {
        get {
            return _imAppKey
        }
        set {
            _imAppKey = newValue
        }
    }
    
    @objc public var imClientId: String {
        get {
            return _imClientId
        }
        set {
            _imClientId = newValue
        }
    }
    
    @objc public var imClientSecret: String {
        get {
            return _imClientSecret
        }
        set {
            _imClientSecret = newValue
        }
    }
    
    @objc public var cloudPlayerKey: String {
        get {
            return _cloudPlayerKey
        }
        set {
            _cloudPlayerKey = newValue
        }
    }
    
    @objc public var cloudPlayerSecret: String {
        get {
            return _cloudPlayerSecret
        }
        set {
            _cloudPlayerSecret = newValue
        }
    }
    
    @objc public var hostUrl: String {
        get {
            return _hostUrl
        }
        set {
            _hostUrl = newValue
        }
    }

    @objc func appRTCToken() -> String {
        return VLUserCenter.user.agoraRTCToken
    }
    
    @objc func appRTMToken() -> String {
        return VLUserCenter.user.agoraRTMToken
    }
}
