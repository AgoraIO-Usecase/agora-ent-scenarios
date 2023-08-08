//
//  ShowTo1v1CallViewController.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import Foundation
import CallAPI
import AgoraRtcKit

class ShowTo1v1CallViewController: ShowTo1v1BaseRoomViewController {
    var targetUser: ShowTo1v1UserInfo? {
        didSet {
            roomInfoView.setRoomInfo(avatar: targetUser?.avatar ?? "",
                                     name: targetUser?.userName ?? "",
                                     id: targetUser?.userId ?? "",
                                     time: Int64(Date().timeIntervalSince1970 * 1000))
        }
    }
    
    private lazy var moveViewModel: MoveGestureViewModel = MoveGestureViewModel()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    
    lazy var smallCanvasView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "#0038ff")?.withAlphaComponent(0.7)
        view.addGestureRecognizer(moveViewModel.gesture)
        return view
    }()
    
    private lazy var bottomBar: ShowTo1v1RoomBottomBar = {
        let bar = ShowTo1v1RoomBottomBar(frame: .zero)
        bar.delegate = self
        return bar
    }()
    
    deinit {
        showTo1v1Print("deinit-- Pure1v1CallViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.addSubview(smallCanvasView)
        
        moveViewModel.touchArea = view.bounds
        bigCanvasView.frame = view.bounds
        smallCanvasView.frame = CGRect(x: view.aui_width - 25 - 109, y: 82 + UIDevice.current.aui_SafeDistanceTop, width: 109, height: 163)
        smallCanvasView.layer.cornerRadius = 20
        smallCanvasView.clipsToBounds = true
    }
    
    @objc private func _hangupAction() {
        callApi?.hangup(roomId: targetUser?.userId ?? "", completion: { err in
        })
    }
}

extension ShowTo1v1CallViewController: AgoraRtcEngineDelegate {
    private func delayRefreshRealTimeInfo(_ task: (()->())?) {
        if #available(iOS 13.0, *) {
            Throttler.throttle(delay: .seconds(1)) { [weak self] in
                DispatchQueue.main.async {
                    task?()
                }
            }
        } else {
            // Fallback on earlier versions
        }
    }
    
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

extension ShowTo1v1CallViewController {
    override func onCallStateChanged(with state: CallStateType,
                                stateReason: CallReason,
                                eventReason: String,
                                elapsed: Int,
                                eventInfo: [String : Any]) {
    }
    
    func onCallEventChanged(with event: CallEvent, elapsed: Int) {
        showTo1v1Warn("onCallEventChanged: \(event.rawValue)")
        switch event {
        case .localLeave, .remoteLeave:
            _hangupAction()
        default:
            break
        }
    }
}
