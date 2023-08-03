//
//  Pure1v1CallViewController.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation
import CallAPI
import AgoraRtcKit

class Pure1v1CallViewController: UIViewController {
    var callApi: CallApiProtocol? {
        didSet {
            oldValue?.removeListener(listener: self)
            callApi?.addListener(listener: self)
            oldValue?.removeRTCListener?(listener: self)
            callApi?.addRTCListener?(listener: self)
        }
    }
    var appId: String = ""
    var rtcEngine: AgoraRtcEngineKit?
    var currentUser: Pure1v1UserInfo?
    var targetUser: Pure1v1UserInfo? {
        didSet {
            roomInfoView.setRoomInfo(avatar: targetUser?.avatar ?? "",
                                     name: targetUser?.userName ?? "",
                                     id: targetUser?.userId ?? "",
                                     time: Int64(Date().timeIntervalSince1970 * 1000))
            
            smallCanvasView.aui_tl = CGPoint(x: view.aui_width - 25 - 109, y: 82 + UIDevice.current.aui_SafeDistanceTop)
            roomInfoView.timerCallBack = {[weak self] duration in
                if duration < 20 * 60 {
                    return
                }
                self?.roomInfoView.setRoomInfo(avatar: nil, name: nil, id: nil, time: nil)
                self?._hangupAction()
            }
        }
    }
    private lazy var moveViewModel: MoveGestureViewModel = MoveGestureViewModel()
    private lazy var roomInfoView: Pure1v1RoomInfoView = Pure1v1RoomInfoView()
    lazy var bigCanvasView: UIView = UIView()
    lazy var smallCanvasView: UIView = {
        let view = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 109, height: 163)))
        view.backgroundColor = UIColor(hexString: "#0038ff")?.withAlphaComponent(0.7)
        view.addGestureRecognizer(moveViewModel.gesture)
        return view
    }()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    private lazy var bottomBar: Pure1v1RoomBottomBar = {
        let bar = Pure1v1RoomBottomBar(frame: .zero)
        bar.delegate = self
        return bar
    }()
    
    private lazy var realTimeView: ShowRealTimeDataView = {
        let realTimeView = ShowRealTimeDataView(isLocal: true)
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
        return realTimeView
    }()
    
    deinit {
        pure1v1Print("deinit-- Pure1v1CallViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        view.addSubview(bigCanvasView)
        view.addSubview(smallCanvasView)
        
        view.addSubview(roomInfoView)
        view.addSubview(bottomBar)
        view.addSubview(hangupButton)
        
        moveViewModel.touchArea = view.bounds
        roomInfoView.frame = CGRect(x: 15, y: UIDevice.current.aui_SafeDistanceTop, width: 202, height: 40)
        bigCanvasView.frame = view.bounds
        smallCanvasView.layer.cornerRadius = 20
        smallCanvasView.clipsToBounds = true
        
        bottomBar.frame = CGRect(x: 0, y: view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 50, width: view.aui_width, height: 40)
        
        hangupButton.aui_size = CGSize(width: 70, height: 70)
        hangupButton.aui_bottom = self.view.aui_height - 20 - UIDevice.current.aui_SafeDistanceBottom
        hangupButton.aui_centerX = self.view.aui_width / 2
    }
    
    @objc private func _hangupAction() {
        callApi?.hangup(roomId: targetUser?.getRoomId() ?? "", completion: { err in
        })
        dismiss(animated: false)
    }
}

extension Pure1v1CallViewController: Pure1v1RoomBottomBarDelegate {
    func onClickSettingButton() {
        let settingMenuVC = ShowToolMenuViewController()
        settingMenuVC.type = ShowMenuType.idle_audience
        settingMenuVC.delegate = self
        present(settingMenuVC, animated: true)
    }
}

extension Pure1v1CallViewController: ShowToolMenuViewControllerDelegate {
    func onClickRealTimeDataButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
    }
}

extension Pure1v1CallViewController: AgoraRtcEngineDelegate {
    private func delayRefreshRealTimeInfo(_ task: (()->())?) {
        if #available(iOS 13.0, *) {
            Throttler.throttle(delay: .seconds(1)) { [weak self] in
                DispatchQueue.main.async {
                    task?()
                    self?.resetRealTimeIfNeeded()
                }
            }
        } else {
            // Fallback on earlier versions
        }
    }
    private func resetRealTimeIfNeeded() {
//        if role == .broadcaster && interactionStatus != .pking && interactionStatus != .onSeat {
//            realTimeView.cleanRemoteDescription()
//        }
//        if role == .audience && interactionStatus != .pking && interactionStatus != .onSeat {
//            realTimeView.cleanLocalDescription()
//        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        pure1v1Warn("rtcEngine warningCode == \(warningCode.rawValue)")
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        pure1v1Warn("rtcEngine errorCode == \(errorCode.rawValue)")
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateChannelStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateChannelStats(stats)
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateLocalAudioStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateLocalAudioStats(stats)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateLocalVideoStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateLocalVideoStats(stats)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateVideoStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateVideoStats(stats)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateAudioStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateAudioStats(stats)
        }
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, uplinkNetworkInfoUpdate networkInfo: AgoraUplinkNetworkInfo) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateUplinkNetworkInfo(networkInfo)
            self?.realTimeView.receiveStatsInfo?.updateUplinkNetworkInfo(networkInfo)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, downlinkNetworkInfoUpdate networkInfo: AgoraDownlinkNetworkInfo) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateDownlinkNetworkInfo(networkInfo)
            self?.realTimeView.receiveStatsInfo?.updateDownlinkNetworkInfo(networkInfo)
        }
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        pure1v1Print("didJoinedOfUid: \(uid) elapsed: \(elapsed)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, contentInspectResult result: AgoraContentInspectResult) {
        pure1v1Warn("contentInspectResult: \(result.rawValue)")
        guard result != .neutral else { return }
        AUIToast.show(text: "call_content_inspect_warning".pure1v1Localization())
    }
}

extension Pure1v1CallViewController: CallApiListenerProtocol {
        func onCallStateChanged(with state: CallStateType,
                                stateReason: CallReason,
                                eventReason: String,
                                elapsed: Int,
                                eventInfo: [String : Any]) {
            let publisher = eventInfo[kPublisher] as? String ?? currentUser?.userId
            guard publisher == currentUser?.userId else {
                return
            }
            
            switch state {
            case .connected:
                let connection = AgoraRtcConnection()
                assert(targetUser != nil, "targetUser == nil")
                connection.channelId = targetUser?.getRoomId() ?? ""
                connection.localUid = UInt(currentUser?.userId ?? "") ?? 0
                setupContentInspectConfig(true, connection: connection)
                moderationAudio()
                break
            default:
                break
            }
        }
        
        func onCallEventChanged(with event: CallEvent, elapsed: Int) {
            pure1v1Print("onCallEventChanged: \(event.rawValue)")
            switch event {
            case .localLeave, .remoteLeave:
                _hangupAction()
            default:
                break
            }
        }
}

extension Pure1v1CallViewController {
    private func setupContentInspectConfig(_ enable: Bool, connection: AgoraRtcConnection) {
        guard let rtcEngine = rtcEngine else {return}
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": "\(connection.localUid)",
            "sceneName": "Pure1v1",
            "userNo": "\(connection.localUid)"
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            pure1v1Error("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 30
        module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspectEx(enable, config: config, connection: connection)
        pure1v1Print("setupContentInspectConfig[\(enable)]: uid:\(connection.localUid) channelId: \(connection.channelId) ret:\(ret)")
    }
    
    /// 语音审核
    private func moderationAudio() {
        let userInfo = ["id": currentUser?.userId ?? "",
                        "sceneName": "show",
                        "userNo": currentUser?.userId ?? "",
                        "userName": currentUser?.userName ?? ""] as NSDictionary
        let parasm: [String: Any] = ["appId": appId,
                                     "channelName": targetUser?.userId ?? "",
                                     "channelType": AgoraChannelProfile.liveBroadcasting.rawValue,
                                     "traceId": NSString.withUUID().md5(),
                                     "src": "iOS",
                                     "payload": userInfo.yy_modelToJSONString()]
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.agoralab.co/v1/moderation/audio",
                                          params: parasm) { response in
            pure1v1Print("moderationAudio response === \(response)")
        } failure: { errr in
            pure1v1Error(errr)
        }
    }
}
