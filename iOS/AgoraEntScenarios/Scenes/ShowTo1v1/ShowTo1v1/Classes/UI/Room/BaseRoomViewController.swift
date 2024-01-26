//
//  BaseRoomViewController.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/2.
//

import UIKit
import CallAPI
import AgoraRtcKit
import AgoraCommon
let kNormalIconSize = CGSize(width: 32, height: 32)
class BaseRoomViewController: UIViewController {
    var onBackClosure: (()->())?
    var rtcEngine: AgoraRtcEngineKit?
    var roomInfo: ShowTo1v1RoomInfo?
    var callApi: CallApiImpl? {
        didSet {
            oldValue?.removeListener(listener: self)
            callApi?.addListener(listener: self)
        }
    }
    private(set) lazy var roomInfoView: RoomInfoView = RoomInfoView()
    private(set) lazy var canvasContainerView = UIView()
    private(set) lazy var remoteCanvasView: CallCanvasView = {
        let view = CallCanvasView()
        view.tapClosure = {[weak self] in
            guard let self = self else {return}
            self.switchCanvasAction(canvasView: self.remoteCanvasView)
        }
        view.clipsToBounds = true
        return view
    }()
    
    lazy var moreBtn: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "icon_live_more"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.addTarget(self, action: #selector(onMoreAction), for: .touchUpInside)
        return button
    }()
    
    private(set) lazy var bottomBar: RoomBottomBar = {
        let bar = RoomBottomBar(frame: .zero)
        bar.delegate = self
        return bar
    }()
    
    private(set) lazy var realTimeView: ShowRealTimeDataView = {
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
        
        canvasContainerView.frame = view.bounds
        view.addSubview(canvasContainerView)
        canvasContainerView.addSubview(remoteCanvasView)
        
        view.addSubview(roomInfoView)
        view.addSubview(bottomBar)
        
        roomInfoView.frame = CGRect(x: 15, y: UIDevice.current.aui_SafeDistanceTop, width: 202, height: 40)
        remoteCanvasView.frame = view.bounds
        
        bottomBar.frame = CGRect(x: 0, y: view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 50, width: view.aui_width, height: 40)
        
        realTimeView.roomId = roomInfo?.roomId ?? ""
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        _showRealTimeView()
    }
    
    @objc func onBackAction() {
        onBackClosure?()
    }
    
    func switchCanvasAction(canvasView: CallCanvasView) {
    }
    
    @objc func onMoreAction() {
        let dialog = AUiMoreDialog(frame: view.bounds)
        view.addSubview(dialog)
        dialog.show()
    }
}

extension BaseRoomViewController: RoomBottomBarDelegate {
    public func onClick(actionType: RoomBottomBarType) {
        if actionType == .more {
            let settingMenuVC = ShowToolMenuViewController()
            settingMenuVC.type = ShowMenuType.idle_audience
            settingMenuVC.delegate = self
            present(settingMenuVC, animated: true)
        }
    }
}

extension BaseRoomViewController: ShowToolMenuViewControllerDelegate {
    func _showRealTimeView() {
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop + 50)
        }
    }
    
    func onClickRealTimeDataButtonSelected(_ menu: ShowToolMenuViewController, _ selected: Bool) {
        _showRealTimeView()
    }
}

extension BaseRoomViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType, 
                            stateReason: CallStateReason,
                            eventReason: String,
                            eventInfo: [String : Any]) {
    }
}

