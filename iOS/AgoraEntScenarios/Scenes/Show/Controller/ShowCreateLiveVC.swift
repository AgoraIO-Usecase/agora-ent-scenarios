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
    
    private var agoraKit: AgoraRtcEngineKit?
    private var selectedResolution = 0
    private lazy var videoEncoderConfig: AgoraVideoEncoderConfiguration = {
        return AgoraVideoEncoderConfiguration(size: CGSize(width: 480, height: 840),
                                       frameRate: .fps30,
                                       bitrate: AgoraVideoBitrateStandard,
                                       orientationMode: .fixedPortrait,
                                       mirrorMode: .auto)
    }()
    
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
       let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    
    private lazy var channelMediaOptions: AgoraRtcChannelMediaOptions = {
       let option = AgoraRtcChannelMediaOptions()
        option.publishMicrophoneTrack = true
        option.publishCameraTrack = true
        option.clientRoleType = .broadcaster
        option.autoSubscribeVideo = true
        option.autoSubscribeAudio = true
        return option
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        setupAgoraKit()
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
    }
    
    private func setupAgoraKit() {
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: nil)
//        agoraKit?.setLogFile(LogUtils.sdkLogPath())
        agoraKit?.setClientRole(.broadcaster)
        agoraKit?.setVideoEncoderConfiguration(videoEncoderConfig)
        /// 开启扬声器
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = 0
        canvas.renderMode = .hidden
        canvas.view = localView
        agoraKit?.setupLocalVideo(canvas)
        agoraKit?.enableAudio()
        agoraKit?.enableVideo()
        agoraKit?.startPreview()
    }
    
    @objc private func didClickCancelButton(){
        dismiss(animated: true)
    }
}

extension ShowCreateLiveVC: ShowCreateLiveViewDelegate {
    
    func onClickCameraBtnAction() {
        agoraKit?.switchCamera()
    }
    
    func onClickBeautyBtnAction() {
        createView.hideBottomViews = true
        let beautyVC = ShowBeautySettingVC()
        beautyVC.modalPresentationStyle = .overCurrentContext
        present(beautyVC, animated: true)
        beautyVC.dismissed = { [weak self] in
            self?.createView.hideBottomViews = false
        }
    }
    
    func onClickQualityBtnAction() {
        createView.hideBottomViews = true
        let vc = ShowSelectQualityVC()
        vc.defalutSelectIndex = selectedResolution
        vc.modalPresentationStyle = .overCurrentContext
        present(vc, animated: true)
        vc.dismissed = { [weak self] in
            self?.createView.hideBottomViews = false
        }
        vc.selectedItem = {[weak self] resolution,index in
            guard let wSelf = self else { return }
            wSelf.selectedResolution = index
            wSelf.videoEncoderConfig.dimensions = CGSize(width: resolution.width, height: resolution.height)
            wSelf.agoraKit?.setVideoEncoderConfiguration(wSelf.videoEncoderConfig)
        }
    }
    
    func onClickStartBtnAction() {
        guard let roomName = createView.roomName, roomName.count > 0 else {
            ToastView.show(text: "房间名称不能为空")
            return
        }
        let room = ShowRoomListModel()
        room.roomName = createView.roomName
        room.roomNo = createView.roomNo
        room.thumbnailId = createView.roomBg
        room.ownerId = VLUserCenter.user.userNo
        room.ownerAvater = VLUserCenter.user.headUrl
        room.createdAt = Date().millionsecondSince1970()
        AppContext.showServiceImp.createRoom(room: room) { [weak self] err, detailModel in
            let liveVC = ShowLiveViewController()
            liveVC.room = detailModel
            liveVC.agoraKit = self?.agoraKit
            self?.navigationController?.pushViewController(liveVC, animated: false)
        }
    }

}
