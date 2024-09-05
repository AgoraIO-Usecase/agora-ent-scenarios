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
        ShowLogger.info("deinit-- ShowCreateLiveVC", context: kCreateLiveVCTag)
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        ShowLogger.info("init-- ShowCreateLiveVC", context: kCreateLiveVCTag)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configNaviBar()
        
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
        
        ShowAgoraKitManager.shared.setupLocalVideo(canvasView: self.localView)
        checkAndSetupBeautyPath() {[weak self] err in
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
        guard isBeautyDownloaded() else { return }
        showPreset()
    }
    
    func onClickCameraBtnAction() {
        guard isBeautyDownloaded() else { return }
        ShowAgoraKitManager.shared.switchCamera(enableBeauty: true)
    }
    
    func onClickBeautyBtnAction() {
        guard isBeautyDownloaded() else { return }
        createView.hideBottomViews = true
        present(beautyVC, animated: true)
    }
    
    func onClickStartBtnAction() {
        guard isBeautyDownloaded() else { return }
        guard let roomName = createView.roomName, roomName.count > 0 else {
            ToastView.show(text: "create_room_name_can_not_empty".show_localized)
            return
        }
        
        guard  let roomName = createView.roomName, roomName.count <= 16 else {
            ToastView.show(text: "create_room_name_too_long".show_localized)
            return
        }
        ShowLogger.info("onClickStartBtnAction[\(createView.roomNo)]", context: kCreateLiveVCTag)
        let roomId = createView.roomNo
        SVProgressHUD.show()
        self.view.isUserInteractionEnabled = false
        AppContext.showServiceImp()?.createRoom(roomId: createView.roomNo,
                                                roomName: roomName) { [weak self] err, detailModel in
            guard let wSelf = self else { return }
            SVProgressHUD.dismiss()
            wSelf.view.isUserInteractionEnabled = true
            if let _ = err {
                ToastView.show(text: "show_create_room_fail".show_localized)
                return
            }
            guard let detailModel = detailModel else { return }
            let liveVC = ShowLivePagesViewController()
            liveVC.roomList = [detailModel]
            liveVC.focusIndex = liveVC.roomList?.firstIndex(where: { $0.roomId == roomId }) ?? 0
            
            wSelf.navigationController?.pushViewController(liveVC, animated: false)
        }
    }
}
