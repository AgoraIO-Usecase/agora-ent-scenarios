//
//  ShowLivePreViewVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit
import AgoraRtcKit

class ShowCreateLiveVC: UIViewController {

    private var createView: ShowCreateLiveView!
    private var localView: UIView!
    
    private var selectedResolution = 1
    
//    let transDelegate = ShowPresentTransitioningDelegate()
    
    private let agoraKitManager = ShowAgoraKitManager()
        
    private lazy var beautyVC = ShowBeautySettingVC()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        agoraKitManager.startPreview(canvasView: localView)
        configNaviBar()
    }
    
    func configNaviBar() {
        self.navigationController?.isNavigationBarHidden = true
        
        let titleLabel = UILabel()
        view.addSubview(titleLabel)
        titleLabel.text = "navi_title_show_live".show_localized
        titleLabel.textColor = .show_main_text
        titleLabel.font = .show_navi_title
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(56)
            make.centerX.equalToSuperview()
        }
        
        let cancelButton = UIButton(type: .custom)
        cancelButton.setImage(UIImage.show_sceneImage(name: "show_preview_cancel"), for: .normal)
        cancelButton.addTarget(self, action: #selector(didClickCancelButton), for: .touchUpInside)
        view.addSubview(cancelButton)
        cancelButton.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.centerY.equalTo(titleLabel)
        }
    }
    
    private func setUpUI() {
        
        // 画布
        localView = UIView()
        view.addSubview(localView)
        localView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        // 创建房间
        createView = ShowCreateLiveView()
        createView.delegate = self
        view.addSubview(createView)
        createView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
//        beautyVC.transitioningDelegate = transDelegate
        beautyVC.dismissed = { [weak self] in
            self?.createView.hideBottomViews = false
        }
    }
    /*
    private func setupAgoraKit() {
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: nil)
//        agoraKit?.setLogFile(LogUtils.sdkLogPath())
        agoraKit?.setClientRole(.broadcaster)
        agoraKit?.setVideoEncoderConfiguration(videoEncoderConfig)
        
        agoraKit?.setVideoFrameDelegate(self)
        /// 开启扬声器
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = UInt(VLUserCenter.user.id) ?? 0
        canvas.renderMode = .hidden
        canvas.view = localView
        canvas.mirrorMode = .disabled
        agoraKit?.setupLocalVideo(canvas)
        agoraKit?.enableAudio()
        agoraKit?.enableVideo()
        agoraKit?.startPreview()
    }*/
    
    @objc private func didClickCancelButton(){
        ByteBeautyManager.shareManager.destroy()
        dismiss(animated: true)
    }
}

extension ShowCreateLiveVC: ShowCreateLiveViewDelegate {
    
    func onClickCameraBtnAction() {
//        agoraKit?.switchCamera()
        agoraKitManager.switchCamera()
    }
    
    func onClickBeautyBtnAction() {
        createView.hideBottomViews = true
        present(beautyVC, animated: true)
    }
    
    func onClickQualityBtnAction() {
        createView.hideBottomViews = true
        let vc = ShowSelectQualityVC()
        vc.defalutSelectIndex = selectedResolution
        present(vc, animated: true)
        vc.dismissed = { [weak self] in
            self?.createView.hideBottomViews = false
        }
        vc.selectedItem = {[weak self] resolution,index in
            guard let wSelf = self else { return }
            wSelf.selectedResolution = index
            wSelf.agoraKitManager.setCaptureVideoDimensions(CGSize(width: resolution.width, height: resolution.height))
        }
    }
    
    func onClickStartBtnAction() {
        guard let roomName = createView.roomName, roomName.count > 0 else {
            ToastView.show(text: "create_room_name_can_not_empty".show_localized)
            return
        }
        
        guard  let roomName = createView.roomName, roomName.count <= 16 else {
            ToastView.show(text: "create_room_name_too_long".show_localized)
            return
        }
        
        AppContext.showServiceImp.createRoom(roomName: roomName,
                                             roomId: createView.roomNo,
                                             thumbnailId: createView.roomBg) { [weak self] err, detailModel in
//            liveVC.agoraKit = self?.agoraKitManager.agoraKit
            guard let wSelf = self else { return }
            let liveVC = ShowLiveViewController()
            liveVC.room = detailModel
            liveVC.selectedResolution = wSelf.selectedResolution
            liveVC.agoraKitManager = wSelf.agoraKitManager
            wSelf.navigationController?.pushViewController(liveVC, animated: false)
        }
    }
}
