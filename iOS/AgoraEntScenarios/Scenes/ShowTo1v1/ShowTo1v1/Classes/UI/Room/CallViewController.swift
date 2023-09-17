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
    
    private lazy var moveViewModel: MoveGestureViewModel = MoveGestureViewModel()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    
    private(set) lazy var localCanvasView: CallCanvasView = {
        let view = CallCanvasView(frame: CGRect(origin: .zero, size: CGSize(width: 109, height: 163)))
        view.backgroundColor = UIColor(hexString: "#0038ff")?.withAlphaComponent(0.7)
        view.tapClosure = {[weak self] in
            guard let self = self else {return}
            self.switchCanvasAction(canvasView: self.localCanvasView)
        }
        view.clipsToBounds = true
        return view
    }()
    
    deinit {
        showTo1v1Print("deinit-- CallViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        canvasContainerView.addSubview(localCanvasView)
        
        moveViewModel.touchArea = view.bounds
        _resetCanvas()
        
        view.addSubview(hangupButton)
        
        hangupButton.aui_size = CGSize(width: 70, height: 70)
        hangupButton.aui_bottom = self.view.aui_height - 20 - UIDevice.current.aui_SafeDistanceBottom
        hangupButton.aui_centerX = self.view.aui_width / 2
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        _hangupAction()
        roomInfoView.stopTime()
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
    
    @objc private func _hangupAction() {
        if roomInfo?.userId == currentUser?.userId {
            //房主拒绝找对方roomId
            callApi?.hangup(roomId: targetUser?.get1V1ChannelId() ?? "", completion: { err in
            })
        } else {
            //观众挂断找房间id，因为可能房主创建了多个房间造成多个房间呼叫一个频道串了
            callApi?.hangup(roomId: roomInfo?.roomId ?? "", completion: { err in
            })
        }
        guard navigationController?.viewControllers.contains(self) ?? false else {return}
        navigationController?.popViewController(animated: false)
    }
}

extension CallViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        showTo1v1Warn("rtcEngine warningCode == \(warningCode.rawValue)")
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        showTo1v1Warn("rtcEngine errorCode == \(errorCode.rawValue)")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        showTo1v1Warn("didJoinedOfUid: \(uid) elapsed: \(elapsed)")
    }
}

extension CallViewController {
    override func onCallStateChanged(with state: CallStateType,
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
            var channelId: String?
            if roomInfo?.userId == currentUser?.userId {
                //房主找对端
                channelId = targetUser?.get1V1ChannelId()
            } else {
                //观众找自己
                channelId = currentUser?.get1V1ChannelId()
            }
            //鉴权
            if let channelId = channelId,
               let userInfo = currentUser,
               let uid = UInt(userInfo.userId) {
                let connection = AgoraRtcConnection()
                connection.channelId = channelId
                connection.localUid = uid
                callApi?.setupContentInspectExConfig(rtcEngine: rtcEngine!,
                                                   enable: true,
                                                   connection: connection)
                callApi?.moderationAudio(appId: showTo1v1AppId!,
                                         channelName: channelId,
                                         user: userInfo)
            }
            break
        default:
            break
        }
    }
    
    func onCallEventChanged(with event: CallEvent, elapsed: Int) {
        showTo1v1Print("onCallEventChanged: \(event.rawValue)")
        switch event {
        case .localLeave, .remoteLeave:
            _hangupAction()
        default:
            break
        }
    }
}
