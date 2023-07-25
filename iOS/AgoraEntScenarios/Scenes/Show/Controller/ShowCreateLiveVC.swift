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
    
    lazy var agoraKitManager: ShowAgoraKitManager = {
        let manager = ShowAgoraKitManager.shared
        if AppContext.shared.isDebugMode {
            manager.debugDefaultBroadcastorSetting()
        }else{
            manager.defaultSetting()
        }
        return manager
    }()
        
    private lazy var beautyVC = ShowBeautySettingVC()
    
    deinit {
        showLogger.info("deinit-- ShowCreateLiveVC")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configNaviBar()
        
        agoraKitManager.startPreview(canvasView: self.localView)
        ShowNetStateSelectViewController.showInViewController(self)
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
        
        // 创建默认美颜效果
        ShowBeautyFaceVC.beautyData.forEach({
            BeautyManager.shareManager.setBeauty(path: $0.path,
                                                     key: $0.key,
                                                     value: $0.value)
        })
    }
    
    private func showPreset() {
        if AppContext.shared.isDebugMode {
            let vc = ShowPresettingVC()
            vc.didSelectedPresetType = {[weak self] type, modeName in
                var level = ShowAgoraKitManager.DeviceLevel.medium
                switch type {
                case .show_low:     level = .low
                case .show_medium:  level = .medium
                case .show_high:    level = .high
                case .unknown:      level = .medium
                }
                ShowAgoraKitManager.shared.deviceLevel = level
                ShowAgoraKitManager.shared.updateVideoProfileForMode(.single)
            }
            present(vc, animated: true)
        } else {
            ShowNetStateSelectViewController.showInViewController(self)
        }
    }
    
    @objc func didClickCancelButton(){
        BeautyManager.shareManager.destroy()
        agoraKitManager.cleanCapture()
        dismiss(animated: true)
    }
}

extension ShowCreateLiveVC: ShowCreateLiveViewDelegate {
    
    func onClickSettingBtnAction() {
        if AppContext.shared.isDebugMode {
            let vc = ShowDebugSettingVC()
            vc.isBroadcastor = true
            self.navigationController?.pushViewController(vc, animated: true)
        }else{
            showPreset()
        }
    }
    
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
//        vc.defalutSelectIndex = selectedResolution
        present(vc, animated: true)
        vc.dismissed = { [weak self] in
            self?.createView.hideBottomViews = false
        }
        vc.selectedItem = {[weak self] resolution,index in
            guard let wSelf = self else { return }
//            wSelf.selectedResolution = index
//            wSelf.agoraKitManager.setCaptureVideoDimensions(CGSize(width: resolution.width, height: resolution.height))
            wSelf.agoraKitManager.selectCaptureVideoDimensions(index: index)
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
        
        let roomId = createView.roomNo
        AppContext.showServiceImp(createView.roomNo).createRoom(roomName: roomName,
                                                                roomId: roomId,
                                                                thumbnailId: createView.roomBg) { [weak self] err, detailModel in
            if err != nil {
                ToastView.show(text: err!.localizedDescription)
            }
//            liveVC.agoraKit = self?.agoraKitManager.agoraKit
            guard let wSelf = self, let detailModel = detailModel else { return }
            let liveVC = ShowLivePagesViewController()
            liveVC.agoraKitManager = wSelf.agoraKitManager
            liveVC.roomList = [detailModel]
//            liveVC.selectedResolution = wSelf.selectedResolution
            liveVC.focusIndex = liveVC.roomList?.firstIndex(where: { $0.roomId == roomId }) ?? 0
            
            wSelf.navigationController?.pushViewController(liveVC, animated: false)
        }
    }
}
