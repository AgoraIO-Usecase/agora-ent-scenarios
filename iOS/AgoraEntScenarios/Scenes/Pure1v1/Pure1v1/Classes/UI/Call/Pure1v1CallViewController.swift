//
//  Pure1v1CallViewController.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation
import CallAPI
import AgoraRtcKit
import AgoraCommon
class Pure1v1CallViewController: UIViewController {
    var callApi: CallApiProtocol? {
        didSet {
            oldValue?.removeListener(listener: self)
            callApi?.addListener(listener: self)
        }
    }
    var rtcChannelName: String? {
        didSet {
            let localUid = Int(currentUser?.userId ?? "") ?? 0
            if let oldValue = oldValue {
                let connection = AgoraRtcConnection(channelId: oldValue, localUid: localUid)
                connection.channelId = oldValue
                rtcEngine?.removeDelegateEx(self.realTimeView, connection: connection)
                rtcEngine?.removeDelegateEx(self, connection: connection)
            }
            
            if let rtcChannelName = rtcChannelName {
                let connection = AgoraRtcConnection(channelId: rtcChannelName, localUid: localUid)
                rtcEngine?.addDelegateEx(self.realTimeView, connection: connection)
                rtcEngine?.addDelegateEx(self, connection: connection)
                self.realTimeView.roomId = rtcChannelName
            }
        }
    }
    var currentState: CallStateType = .idle
    var rtcEngine: AgoraRtcEngineKit?
    var currentUser: Pure1v1UserInfo?
    var targetUser: Pure1v1UserInfo? {
        didSet {
            roomInfoView.startTime(Int64(Date().timeIntervalSince1970 * 1000))
            roomInfoView.timerCallBack = {[weak self] duration in
                if duration < 20 * 60 {
                    return
                }
                self?.roomInfoView.stopTime()
                self?._hangupAction()
            }
            _resetCanvas()
        }
    }
    private lazy var selectedMap: [ShowToolMenuType: Bool] = [:]
    private lazy var moveViewModel: MoveGestureViewModel = MoveGestureViewModel()
    private lazy var roomInfoView: Pure1v1RoomInfoView = Pure1v1RoomInfoView()
    lazy var moreBtn: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.scene1v1Image(name: "icon_live_more"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.addTarget(self, action: #selector(onMoreAction), for: .touchUpInside)
        return button
    }()
    lazy var canvasContainerView = UIView()
    lazy var remoteCanvasView: Pure1v1CanvasView = {
        let view = Pure1v1CanvasView()
        view.tapClosure = {[weak self] in
            guard let self = self else {return}
            self._switchCanvasAction(canvasView: self.remoteCanvasView)
        }
        view.clipsToBounds = true
        return view
    }()
    lazy var localCanvasView: Pure1v1CanvasView = {
        let view = Pure1v1CanvasView(frame: .zero)
        view.tapClosure = {[weak self] in
            guard let self = self else {return}
            self._switchCanvasAction(canvasView: self.localCanvasView)
        }
        view.clipsToBounds = true
        return view
    }()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.scene1v1Image(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    private lazy var bottomBar: Pure1v1RoomBottomBar = {
        let bar = Pure1v1RoomBottomBar(frame: .zero)
        bar.delegate = self
        return bar
    }()
    
    private lazy var realTimeView = ShowRealTimeDataView(isLocal: true)
    
    private let subtitleView = SubtitleView(frame: .zero)
    
    private let rttVC = ShowRttViewController()
    
    deinit {
        Pure1v1Logger.info("deinit-- Pure1v1CallViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        canvasContainerView.frame = view.bounds
        view.addSubview(canvasContainerView)
        canvasContainerView.addSubview(remoteCanvasView)
        canvasContainerView.addSubview(localCanvasView)
        
        _resetCanvas()
        
        view.addSubview(roomInfoView)
        view.addSubview(bottomBar)
        view.addSubview(hangupButton)
        view.addSubview(moreBtn)
        view.addSubview(subtitleView)
        
        moveViewModel.touchArea = view.bounds
        roomInfoView.frame = CGRect(x: 15, y: UIDevice.current.aui_SafeDistanceTop, width: 202, height: 40)
        
        bottomBar.frame = CGRect(x: 0, y: view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 50, width: view.aui_width, height: 40)
        
        hangupButton.aui_size = CGSize(width: 70, height: 70)
        hangupButton.aui_bottom = self.view.aui_height - 20 - UIDevice.current.aui_SafeDistanceBottom
        hangupButton.aui_centerX = self.view.aui_width / 2
        
        subtitleView.snp.makeConstraints { make in
            make.width.equalToSuperview()
            make.bottom.equalTo(hangupButton.snp.top)
            make.height.equalTo(235)
        }
        
        moreBtn.aui_size = CGSize(width: 32, height: 32)
        moreBtn.aui_right = view.aui_width - 15
        moreBtn.aui_centerY = roomInfoView.aui_centerY
        
//        showRealDataView()
    }
    
    private func _resetCanvas() {
        remoteCanvasView.frame = view.bounds
        localCanvasView.frame = CGRect(origin: CGPoint(x: canvasContainerView.aui_width - 25 - 109, y: 82 + UIDevice.current.aui_SafeDistanceTop), size: CGSize(width: 109, height: 163))
        canvasContainerView.bringSubviewToFront(localCanvasView)
        _updateCanvas()
    }
    
    private func _updateCanvas() {
        if canvasContainerView.subviews.first == remoteCanvasView {
            localCanvasView.layer.cornerRadius = 20
            remoteCanvasView.layer.cornerRadius = 0
            
            remoteCanvasView.titleLabel.text = ""
            localCanvasView.titleLabel.text = currentUser?.userName ?? ""
            localCanvasView.sizeToFit()
            
            remoteCanvasView.removeGestureRecognizer(moveViewModel.gesture)
            localCanvasView.addGestureRecognizer(moveViewModel.gesture)
            
            
            roomInfoView.setRoomInfo(avatar: targetUser?.avatar ?? "",
                                     name: targetUser?.userName ?? "",
                                     id: targetUser?.userId ?? "")
        } else {
            remoteCanvasView.layer.cornerRadius = 20
            localCanvasView.layer.cornerRadius = 0
            
            localCanvasView.titleLabel.text = ""
            remoteCanvasView.titleLabel.text = targetUser?.userName ?? ""
            remoteCanvasView.sizeToFit()
            localCanvasView.removeGestureRecognizer(moveViewModel.gesture)
            remoteCanvasView.addGestureRecognizer(moveViewModel.gesture)
            
            roomInfoView.setRoomInfo(avatar: currentUser?.avatar ?? "",
                                     name: currentUser?.userName ?? "",
                                     id: currentUser?.userId ?? "")
        }
    }
    
    private func _switchCanvasAction(canvasView: Pure1v1CanvasView) {
        guard canvasView == canvasContainerView.subviews.last else {return}
        
        let localFrame = localCanvasView.frame
        localCanvasView.frame = remoteCanvasView.frame
        remoteCanvasView.frame = localFrame
        if canvasView == remoteCanvasView {
            canvasContainerView.bringSubviewToFront(localCanvasView)
        } else {
            canvasContainerView.bringSubviewToFront(remoteCanvasView)
        }
        _updateCanvas()
    }
    
    private func showRealDataView() {
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
    }
    
    @objc private func _hangupAction() {
        callApi?.hangup(remoteUserId: UInt(targetUser?.userId ?? "") ?? 0, reason: nil, completion: { err in
        })
        dismiss(animated: false)
    }
    
    @objc func onMoreAction() {
        let dialog = AUiMoreDialog(frame: view.bounds)
        view.addSubview(dialog)
        dialog.show()
    }
}

extension Pure1v1CallViewController: Pure1v1RoomBottomBarDelegate {
    func onClickSettingButton() {
        let settingMenuVC = ShowToolMenuViewController(menuTypes: [.real_time_data, .camera, .mic])
        settingMenuVC.selectedMap = selectedMap
        settingMenuVC.delegate = self
        present(settingMenuVC, animated: true)
    }
    
    func onClickRttButton() {
        guard let channelName = self.rtcChannelName else {return}
        rttVC.channelName = channelName
        rttVC.resetRttStatus()
        rttVC.clickDetailButonAction = { vc in
            self.present(vc, animated: true)
        }
        present(rttVC, animated: true)
    }
}

extension Pure1v1CallViewController: ShowToolMenuViewControllerDelegate {
    func onClickCameraButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        self.selectedMap[.camera] = selected
        menu.selectedMap = selectedMap
        guard let rtcChannelName = rtcChannelName, let uid = Int(currentUser?.userId ?? "") else {return}
        let connection = AgoraRtcConnection(channelId: rtcChannelName, localUid: uid)
        if selected {
            rtcEngine?.stopPreview()
        } else {
            rtcEngine?.startPreview()
        }
        rtcEngine?.muteLocalVideoStreamEx(selected, connection: connection)
    }
    
    func onClickMicButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        self.selectedMap[.mic] = selected
        menu.selectedMap = selectedMap
        guard let rtcChannelName = rtcChannelName, let uid = Int(currentUser?.userId ?? "") else {return}
        let connection = AgoraRtcConnection(channelId: rtcChannelName, localUid: uid)
        rtcEngine?.muteLocalAudioStreamEx(selected, connection: connection)
    }
    
    func onClickRealTimeDataButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        showRealDataView()
    }
}

extension Pure1v1CallViewController: CallApiListenerProtocol {
    
    func onCallStateChanged(with state: CallStateType, 
                            stateReason: CallStateReason,
                            eventReason: String,
                            eventInfo: [String : Any]) {
        self.currentState = state
        if state == .connected {
            selectedMap.removeAll()
            self.remoteCanvasView.canvasView.isHidden = false
            remoteCanvasView.emptyView.isHidden = false
            localCanvasView.emptyView.isHidden = false
        } else {
            if (state == .prepared) {
                RttManager.shared.disableRtt(force: true) { success in
                }
                self.subtitleView.rttView.clear()
            }
            remoteCanvasView.emptyView.isHidden = true
            localCanvasView.emptyView.isHidden = true
        }
    }
    
    func onCallEventChanged(with event: CallEvent, eventReason: String?) {
        Pure1v1Logger.info("onCallEventChanged event: \(event.rawValue) eventReason: '\(eventReason ?? "")'")
        switch event {
        case .remoteLeft:
            if currentState == .connected {
                AUIToast.show(text: "call_toast_remote_fail".pure1v1Localization())
            }
            _hangupAction()
        default:
            break
        }
    }
    
    func callDebugInfo(message: String, logLevel: CallLogLevel) {
        switch logLevel {
        case .normal:
            Pure1v1Logger.info(message, tag: "CallApi")
        case .warning:
            Pure1v1Logger.info(message, tag: "CallApi")
        case .error:
            Pure1v1Logger.info(message, tag: "CallApi")
        }
    }
}


extension Pure1v1CallViewController: AgoraRtcEngineDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didAudioMuted muted: Bool, byUid uid: UInt) {
        Pure1v1Logger.info("didAudioMuted[\(uid)] \(muted)")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didVideoMuted muted: Bool, byUid uid: UInt) {
        Pure1v1Logger.info("didVideoMuted[\(uid)] \(muted)")
        if (uid != 20000 && uid != 40000) {
            self.remoteCanvasView.canvasView.isHidden = muted
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        if (String(uid) == RttManager.shared.pubBotUid) {
            subtitleView.rttView.pushMessageData(data: data, uid: uid)
        }
        
//        guard let text: Agora_SpeechToText_Text = try? Agora_SpeechToText_Text(serializedData: data) else {return}
//
//        var translate: String = ""
//        text.trans.first?.texts.forEach({ word in
//            translate += word
//        })
//        if (translate != "") {
//            print("RttApiManager \(translate)")
//        }
    }
}

extension Pure1v1CallViewController: RttEventListener {
    func onRttStart() {
        self.bottomBar.setRttButtonView(enable: true)
    }
    
    func onRttStop() {
        self.bottomBar.setRttButtonView(enable: false)
    }
}
