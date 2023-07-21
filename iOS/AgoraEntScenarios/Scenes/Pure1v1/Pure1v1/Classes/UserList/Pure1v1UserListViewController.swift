//
//  Pure1v1UserListViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit
import YYCategories
import CallAPI
import AgoraRtcKit

class Pure1v1UserListViewController: UIViewController {
    var appId: String = ""
    var appCertificate: String = ""
    var userInfo: Pure1v1UserInfo?
    private lazy var callAPI = CallApiImpl()
    private lazy var naviBar: Pure1v1NaviBar = Pure1v1NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private lazy var service: Pure1v1ServiceProtocol = Pure1v1ServiceImp(appId: appId, user: userInfo)
    private lazy var noDataView: Pure1v1UserNoDataView = Pure1v1UserNoDataView(frame: self.view.bounds)
    private lazy var listView: Pure1v1UserPagingListView = {
        let listView = Pure1v1UserPagingListView(frame: self.view.bounds)
        listView.callClosure = { [weak self] user in
            
        }
        return listView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(noDataView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
        service.joinRoom {[weak self] error in
            self?.naviBar.refreshButton.isHidden = false
            self?._refreshAction()
        }
        
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        
        let tokenConfig: CallTokenConfig = CallTokenConfig()
        tokenConfig.roomId = "\(userInfo.userId)"
        NetworkManager.shared.generateTokens(appId: appId,
                                             appCertificate: appCertificate,
                                             channelName: tokenConfig.roomId,
                                             uid: userInfo.userId,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            tokenConfig.rtcToken = tokens[AgoraTokenType.rtc.rawValue]!
            tokenConfig.rtmToken = tokens[AgoraTokenType.rtm.rawValue]!
            
            self?._initCallAPI(tokenConfig: tokenConfig)
        }
    }
    
    private func _initCallAPI(tokenConfig: CallTokenConfig) {
        let config = CallConfig()
        config.role = .caller  // Pure 1v1 can only be set as the caller
        config.mode = .pure1v1
        config.appId = appId
        config.userId = UInt(userInfo?.userId ?? "")!
        config.autoAccept = false
        config.rtcEngine = _createRtcEngine()
//        config.localView = rightView
//        config.remoteView = leftView
        
        callAPI.initialize(config: config, token: tokenConfig) { error in
            // Requires active call to prepareForCall
            let prepareConfig = PrepareConfig.callerConfig()
            prepareConfig.autoLoginRTM = true
            prepareConfig.autoSubscribeRTM = true
            self.callAPI.prepareForCall(prepareConfig: prepareConfig) { err in
            }
        }
        callAPI.addListener(listener: self)
    }
    
    private func _createRtcEngine() ->AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: nil)
        
        engine.setClientRole(.broadcaster)
        return engine
    }
}

extension Pure1v1UserListViewController {
    @objc func _backAction() {
        callAPI.deinitialize {
        }
        service.leaveRoom { err in
        }
        self.navigationController?.popViewController(animated: true)
    }
    
    @objc func _refreshAction() {
        service.getUserList {[weak self] list in
            self?.listView.userList = list.filter({$0.userId != self?.userInfo?.userId})
        }
    }
}

extension Pure1v1UserListViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
        
    }
    
    
}
