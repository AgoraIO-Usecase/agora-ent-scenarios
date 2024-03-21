//
//  ShowLivePreViewVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit
import AgoraRtcKit
import SVProgressHUD
import AgoraCommon

class ShowCreateLiveVC: UIViewController {

    private var createView: ShowCreateLiveView!
    private var localView: UIView!
        
    private lazy var beautyVC = ShowBeautySettingVC()
    
    deinit {
        showLogger.info("deinit-- ShowCreateLiveVC")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configNaviBar()
        
        ShowAgoraKitManager.shared.setupLocalVideo(mirrorMode: .auto, canvasView: self.localView)
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
        
        beautyVC.dismissed = { [weak self] in
            self?.createView.hideBottomViews = false
        }
        
        if let engine = ShowAgoraKitManager.shared.engine {
            BeautyManager.shareManager.setup(engine: engine)
        } else {
            assert(false, "rtc engine == nil")
        }
        
        self.checkAndSetupBeautyPath() {[weak self] err in
            guard let self = self else {return}
            if let _ = err {return}
            
            BeautyManager.shareManager.initBeautyRender()
            
            // 创建默认美颜效果
            ShowBeautyFaceVC.beautyData.forEach({
                BeautyManager.shareManager.setBeauty(path: $0.path,
                                                     key: $0.key,
                                                     value: $0.value)
            })
            
            // 美颜设置
            BeautyManager.shareManager.configBeautyAPI()
            ShowAgoraKitManager.shared.startPreview(canvasView: self.localView)
        }
    }
    
    private func showPreset() {
        if AppContext.shared.isDebugMode {
            let vc = ShowDebugSettingVC()
            vc.isBroadcastor = true
            self.navigationController?.pushViewController(vc, animated: true)
        } else {
            ShowNetStateSelectViewController.showInViewController(self)
        }
    }
    
    @objc func didClickCancelButton(){
        BeautyManager.shareManager.destroy()
        ShowAgoraKitManager.shared.cleanCapture()
        ShowBeautyFaceVC.resetData()
        cancelBeautyResource()
        dismiss(animated: true)
    }
}

extension ShowCreateLiveVC: ShowCreateLiveViewDelegate {
    
    func onClickSettingBtnAction() {
        if isBeautyDownloading() { return }
        showPreset()
    }
    
    func onClickCameraBtnAction() {
        if isBeautyDownloading() { return }
        ShowAgoraKitManager.shared.switchCamera()
    }
    
    func onClickBeautyBtnAction() {
        if isBeautyDownloading() { return }
        createView.hideBottomViews = true
        present(beautyVC, animated: true)
    }
    
    func onClickStartBtnAction() {
        if isBeautyDownloading() { return }
        guard let roomName = createView.roomName, roomName.count > 0 else {
            ToastView.show(text: "create_room_name_can_not_empty".show_localized)
            return
        }
        
        guard  let roomName = createView.roomName, roomName.count <= 16 else {
            ToastView.show(text: "create_room_name_too_long".show_localized)
            return
        }
        
        let roomId = createView.roomNo
        SVProgressHUD.show()
        AppContext.showServiceImp(createView.roomNo)?.createRoom(roomName: roomName,
                                                                roomId: roomId,
                                                                thumbnailId: createView.roomBg) { [weak self] err, detailModel in
            SVProgressHUD.dismiss()
            if err != nil {
                ToastView.show(text: err!.localizedDescription)
            }
            guard let wSelf = self, let detailModel = detailModel else { return }
            let liveVC = ShowLivePagesViewController()
            liveVC.roomList = [detailModel]
            liveVC.focusIndex = liveVC.roomList?.firstIndex(where: { $0.roomId == roomId }) ?? 0
            
            wSelf.navigationController?.pushViewController(liveVC, animated: false)
        }
    }
}
