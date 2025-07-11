//
//  AppContext.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import Bugly

@objc public class AppContext: NSObject {
    @objc public static let shared: AppContext = .init()
    @objc public var sceneLocalizeBundleName: String?
    @objc public var sceneImageBundleName: String?
    @objc public var extDic: NSMutableDictionary = NSMutableDictionary()
    @objc public var isDeveloperMode = false
    @objc var imageCahe = [String: AnyObject]()
    @objc public var localizedCache = [String: String]()
    @objc public var sceneConfig: VLSceneConfigsModel?
    private var _appId: String = ""
    private var _certificate: String = ""
    private var _imAppKey: String = ""
    private var _imClientId: String = ""
    private var _imClientSecret: String = ""
    private var _restfulApiKey: String = ""
    private var _restfulApiSecret: String = ""
    private var _hostUrl: String = ""
    private var _baseServerUrl: String = ""
    private var _roomManagerUrl: String = ""
    private var _buglyIsStarted: Bool = false
    @objc public var agoraRTMToken: String = ""
    @objc public var agoraRTCToken: String = ""
    
    @objc public var isAgreeLicense: Bool = false {
        didSet {
            guard isAgreeLicense else {
                return
            }
            
            guard !_buglyIsStarted else {
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
        _buglyIsStarted = true
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
    
    @objc public var RestfulApiKey: String {
        get {
            return _restfulApiKey
        }
        set {
            _restfulApiKey = newValue
        }
    }
    
    @objc public var RestfulApiSecret: String {
        get {
            return _restfulApiSecret
        }
        set {
            _restfulApiSecret = newValue
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
    
    @objc public var roomManagerUrl: String {
        get {
            return _roomManagerUrl
        }
        set {
            _roomManagerUrl = newValue
        }
    }
    
    @objc public var baseServerUrl: String {
        get {
            return _baseServerUrl
        }
        set {
            _baseServerUrl = newValue
        }
    }
    
    public var hyAppId: String = ""
    public var hyAPISecret: String = ""
    public var hyAPIKey: String = ""
    
    public var aichatAgentHost: String = ""
}
