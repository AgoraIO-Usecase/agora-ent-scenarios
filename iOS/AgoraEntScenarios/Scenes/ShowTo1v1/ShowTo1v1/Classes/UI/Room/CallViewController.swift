//
//  CallViewController.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import Foundation
import CallAPI
import AgoraRtcKit

class CallViewController: BaseRoomViewController {
    var targetUser: ShowTo1v1UserInfo? {
        didSet {
            roomInfoView.startTime(Int64(Date().timeIntervalSince1970 * 1000))
            _resetCanvas()
        }
    }
    var currentUser: ShowTo1v1UserInfo?
    var currentState: CallStateType = .idle
    
    var rtcChannelName: String? {
        didSet {
            let localUid = Int(currentUser?.getUIntUserId() ?? 0)
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
    private lazy var moveViewModel: MoveGestureViewModel = MoveGestureViewModel()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    
    private(set) lazy var localCanvasView: CallCanvasView = {
        let view = CallCanvasView(frame: CGRect(origin: .zero, size: CGSize(width: 109, height: 163)))
        view.tapClosure = {[weak self] in
            guard let self = self else {return}
            self.switchCanvasAction(canvasView: self.localCanvasView)
        }
        view.clipsToBounds = true
        return view
    }()
    
    deinit {
        ShowTo1v1Logger.info("deinit-- CallViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        canvasContainerView.addSubview(localCanvasView)
        
        moveViewModel.touchArea = view.bounds
        _resetCanvas()
        
        moreBtn.aui_size = kNormalIconSize
        moreBtn.aui_right = view.aui_width - 15
        moreBtn.aui_centerY = roomInfoView.aui_centerY
        view.addSubview(moreBtn)
        
        view.addSubview(hangupButton)
        
        hangupButton.aui_size = CGSize(width: 70, height: 70)
        hangupButton.aui_bottom = self.view.aui_height - 20 - UIDevice.current.aui_SafeDistanceBottom
        hangupButton.aui_centerX = self.view.aui_width / 2
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        _hangupAction()
        roomInfoView.stopTime()
        
        if isMovingFromParent {
            onBackClosure?()
        }
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
                                     id: targetUser?.uid ?? "")
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
                                     id: currentUser?.uid ?? "")
        }
    }
    
    override func switchCanvasAction(canvasView: CallCanvasView) {
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
    
    override func menuTypes() -> [ShowToolMenuType] {
        return [.real_time_data, .camera, .mic]
    }
    
    @objc private func _hangupAction() {
        callApi?.hangup(remoteUserId: UInt(targetUser?.uid ?? "") ?? 0, reason: nil, completion: { err in
        })
    }
}

extension CallViewController {
    public override func onClickCameraButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        self.selectedMap[.camera] = selected
        menu.selectedMap = selectedMap
        guard let rtcChannelName = rtcChannelName, let uid = Int(currentUser?.uid ?? "") else {return}
        let connection = AgoraRtcConnection(channelId: rtcChannelName, localUid: uid)
        if selected {
            rtcEngine?.stopPreview()
        } else {
            rtcEngine?.startPreview()
        }
        rtcEngine?.muteLocalVideoStreamEx(selected, connection: connection)
    }
    
    public override func onClickMicButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        self.selectedMap[.mic] = selected
        menu.selectedMap = selectedMap
        guard let rtcChannelName = rtcChannelName, let uid = Int(currentUser?.uid ?? "") else {return}
        let connection = AgoraRtcConnection(channelId: rtcChannelName, localUid: uid)
        rtcEngine?.muteLocalAudioStreamEx(selected, connection: connection)
    }
}

extension CallViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        ShowTo1v1Logger.warn("rtcEngine warningCode == \(warningCode.rawValue)")
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        ShowTo1v1Logger.warn("rtcEngine errorCode == \(errorCode.rawValue)")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        ShowTo1v1Logger.warn("didJoinedOfUid: \(uid) elapsed: \(elapsed)")
    }
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didAudioMuted muted: Bool, byUid uid: UInt) {
        ShowTo1v1Logger.info("didAudioMuted[\(uid)] \(muted)")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didVideoMuted muted: Bool, byUid uid: UInt) {
        ShowTo1v1Logger.info("didVideoMuted[\(uid)] \(muted)")
        self.remoteCanvasView.canvasView.isHidden = muted
    }
}

extension CallViewController {
    override func onCallStateChanged(with state: CallStateType,
                                     stateReason: CallStateReason,
                                     eventReason: String,
                                     eventInfo: [String : Any]) {
        currentState = state
        localCanvasView.emptyView.isHidden = true
        remoteCanvasView.emptyView.isHidden = true
        switch state {
        case .connecting:
            self.rtcChannelName
        case .connected:
            localCanvasView.emptyView.isHidden = false
            remoteCanvasView.emptyView.isHidden = false
            selectedMap.removeAll()
            self.remoteCanvasView.canvasView.isHidden = false
            var channelId: String? = rtcChannelName
            if roomInfo?.uid == currentUser?.uid {
                ConnectedToastView.show(user: targetUser!, canvasView: self.view)
            }
            //鉴权
            if let channelId = channelId,
               let userInfo = currentUser,
               let uid = UInt(userInfo.uid) {
                let connection = AgoraRtcConnection()
                connection.channelId = channelId
                connection.localUid = uid
                callApi?.setupContentInspectExConfig(rtcEngine: rtcEngine!,
                                                   enable: true,
                                                   connection: connection)
                callApi?.moderationAudio(channelName: channelId)
            }
            break
        case .prepared:
            guard navigationController?.viewControllers.contains(self) ?? false else {return}
            navigationController?.popViewController(animated: false)
        default:
            break
        }
    }
    
    func onCallEventChanged(with event: CallEvent, eventReason: String?) {
        ShowTo1v1Logger.info("onCallEventChanged: \(event.rawValue) eventReason: '\(eventReason ?? "")'")
        switch event {
        case .remoteLeft:
            if currentState == .connected {
                AUIToast.show(text: "call_toast_remote_fail".showTo1v1Localization())
            }
            _hangupAction()
        default:
            break
        }
    }
}
