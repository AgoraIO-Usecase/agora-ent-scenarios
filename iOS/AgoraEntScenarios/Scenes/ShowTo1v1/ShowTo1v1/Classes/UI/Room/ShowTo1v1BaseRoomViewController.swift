//
//  ShowTo1v1BaseRoomViewController.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/2.
//

import UIKit
import CallAPI
import AgoraRtcKit

class ShowTo1v1BaseRoomViewController: UIViewController {
    var onBackClosure: (()->())?
    var callApi: CallApiProtocol? {
        didSet {
//            oldValue?.removeListener(listener: self)
//            oldValue?.removeRTCListener?(listener: self.realTimeView)
//            callApi?.addListener(listener: self)
//            callApi?.addRTCListener?(listener: self.realTimeView)
        }
    }
    lazy var roomInfoView: ShowTo1v1RoomInfoView = ShowTo1v1RoomInfoView()
    lazy var bigCanvasView: UIView = UIView()
    private lazy var bottomBar: ShowTo1v1RoomBottomBar = {
        let bar = ShowTo1v1RoomBottomBar(frame: .zero)
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
        showTo1v1Print("deinit-- ShowTo1v1BaseRoomViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        view.addSubview(bigCanvasView)
        
        view.addSubview(roomInfoView)
        view.addSubview(bottomBar)
        
        roomInfoView.frame = CGRect(x: 15, y: UIDevice.current.aui_SafeDistanceTop, width: 202, height: 40)
        bigCanvasView.frame = view.bounds
        
        bottomBar.frame = CGRect(x: 0, y: view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 50, width: view.aui_width, height: 40)
    }
    
    @objc func onBackAction() {
        dismiss(animated: false)
        onBackClosure?()
    }
}

extension ShowTo1v1BaseRoomViewController: ShowTo1v1RoomBottomBarDelegate {
    func onClickSettingButton() {
        let settingMenuVC = ShowToolMenuViewController()
        settingMenuVC.type = ShowMenuType.idle_audience
        settingMenuVC.delegate = self
        present(settingMenuVC, animated: true)
    }
}

extension ShowTo1v1BaseRoomViewController: ShowToolMenuViewControllerDelegate {
    func onClickRealTimeDataButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
    }
}

extension ShowTo1v1BaseRoomViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
    }
}

