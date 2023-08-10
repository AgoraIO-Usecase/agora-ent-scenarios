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
            roomInfoView.setRoomInfo(avatar: targetUser?.avatar ?? "",
                                     name: targetUser?.userName ?? "",
                                     id: targetUser?.userId ?? "",
                                     time: Int64(Date().timeIntervalSince1970 * 1000))
        }
    }
    
    var currentUser: ShowTo1v1UserInfo? {
        didSet {
            smallCanvasView.titleLabel.text = currentUser?.userName ?? ""
            smallCanvasView.sizeToFit()
        }
    }
    
    private lazy var moveViewModel: MoveGestureViewModel = MoveGestureViewModel()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    
    lazy var smallCanvasView: CallCanvasView = {
        let view = CallCanvasView(frame: CGRect(origin: .zero, size: CGSize(width: 109, height: 163)))
        view.backgroundColor = UIColor(hexString: "#0038ff")?.withAlphaComponent(0.7)
        view.addGestureRecognizer(moveViewModel.gesture)
        return view
    }()
    
    private lazy var bottomBar: RoomBottomBar = {
        let bar = RoomBottomBar(frame: .zero)
        bar.delegate = self
        return bar
    }()
    
    deinit {
        showTo1v1Print("deinit-- CallViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.addSubview(smallCanvasView)
        
        moveViewModel.touchArea = view.bounds
        bigCanvasView.frame = view.bounds
        smallCanvasView.frame = CGRect(x: view.aui_width - 25 - 109, y: 82 + UIDevice.current.aui_SafeDistanceTop, width: 109, height: 163)
        smallCanvasView.layer.cornerRadius = 20
        smallCanvasView.clipsToBounds = true
        
        
        view.addSubview(hangupButton)
        
        hangupButton.aui_size = CGSize(width: 70, height: 70)
        hangupButton.aui_bottom = self.view.aui_height - 20 - UIDevice.current.aui_SafeDistanceBottom
        hangupButton.aui_centerX = self.view.aui_width / 2
    }
    
    @objc private func _hangupAction() {
        callApi?.hangup(roomId: roomInfo?.roomId ?? "", completion: { err in
        })
        dismiss(animated: false)
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
//            let connection = AgoraRtcConnection()
//            assert(targetUser != nil, "targetUser == nil")
//            connection.channelId = targetUser?.getRoomId() ?? ""
//            connection.localUid = UInt(currentUser?.userId ?? "") ?? 0
//            setupContentInspectConfig(true, connection: connection)
//            moderationAudio()
            break
        default:
            break
        }
    }
}
