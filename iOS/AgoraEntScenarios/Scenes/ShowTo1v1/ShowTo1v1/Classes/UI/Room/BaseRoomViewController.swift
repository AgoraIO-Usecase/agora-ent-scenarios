//
//  BaseRoomViewController.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/2.
//

import UIKit
import CallAPI
import AgoraRtcKit

class BaseRoomViewController: UIViewController {
    var onBackClosure: (()->())?
    var rtcEngine: AgoraRtcEngineKit?
    var roomInfo: ShowTo1v1RoomInfo?
    var callApi: CallApiProtocol? {
        didSet {
            oldValue?.removeListener(listener: self)
            oldValue?.removeRTCListener?(listener: self.realTimeView)
            callApi?.addListener(listener: self)
            callApi?.addRTCListener?(listener: self.realTimeView)
        }
    }
    lazy var roomInfoView: RoomInfoView = RoomInfoView()
    lazy var bigCanvasView = CallCanvasView()
    private lazy var bottomBar: RoomBottomBar = {
        let bar = RoomBottomBar(frame: .zero)
        bar.delegate = self
        return bar
    }()
    
    lazy var realTimeView: ShowRealTimeDataView = {
        let realTimeView = ShowRealTimeDataView(isLocal: true)
        return realTimeView
    }()
    
    deinit {
        callApi = nil
        showTo1v1Print("deinit-- ShowTo1v1BaseRoomViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        view.addSubview(bigCanvasView)
        
        view.addSubview(roomInfoView)
        view.addSubview(bottomBar)
        view.addSubview(realTimeView)
        
        
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
        
        roomInfoView.frame = CGRect(x: 15, y: UIDevice.current.aui_SafeDistanceTop, width: 202, height: 40)
        bigCanvasView.frame = view.bounds
        
        bottomBar.frame = CGRect(x: 0, y: view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 50, width: view.aui_width, height: 40)
    }
    
    @objc func onBackAction() {
        dismiss(animated: false)
        onBackClosure?()
    }
}

extension BaseRoomViewController: RoomBottomBarDelegate {
    func onClickSettingButton() {
        let settingMenuVC = ShowToolMenuViewController()
        settingMenuVC.type = ShowMenuType.idle_audience
        settingMenuVC.delegate = self
        present(settingMenuVC, animated: true)
    }
}

extension BaseRoomViewController: ShowToolMenuViewControllerDelegate {
    func onClickRealTimeDataButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
    }
}

extension BaseRoomViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
    }
}

