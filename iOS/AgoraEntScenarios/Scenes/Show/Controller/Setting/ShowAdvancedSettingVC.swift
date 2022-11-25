//
//  ShowAdvancedSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit
//import JXCategoryView
import AgoraRtcKit

class ShowAdvancedSettingVC: UIViewController, UIGestureRecognizerDelegate {
    
    var mode: ShowMode?
    var isBroadcaster = true

    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    var settingManager: ShowSettingManager!
    
    // 当前设置的预设值
    var presetModeName: String?
    
    private let titles = ["show_advance_setting_video_title".show_localized,
                          "show_advance_setting_audio_title".show_localized]
    
    private lazy var videoSettingVC: ShowVideoSettingVC = {
        return createSettingVCForIndex(0)
    }()
    
    private lazy var audioSettingVC: ShowVideoSettingVC = {
        return createSettingVCForIndex(1)
    }()
    
    // 指示条
    private lazy var indicator: UIView = {
        let indicator = UIView()
        indicator.size = CGSize(width: 66, height: 2)
        indicator.backgroundColor = .show_zi03
        return indicator
    }()
     
    private lazy var segmentedView: AEACategoryView = {
        let layout = AEACategoryViewLayout()
        layout.itemSize = CGSize(width: Screen.width * 0.5, height: 40)
        let segmentedView = AEACategoryView(layout: layout)
        segmentedView.titles = titles
        segmentedView.delegate = self
        segmentedView.titleFont = .show_R_14
        segmentedView.titleSelectedFont = .show_navi_title
        segmentedView.titleColor = .show_Ellipse5
        segmentedView.titleSelectedColor = .show_Ellipse7
        segmentedView.backgroundColor = .clear
        segmentedView.defaultSelectedIndex = 0
        segmentedView.indicator = indicator
        return segmentedView
    }()
    
    private lazy var listContainerView: AEAListContainerView = {
        let containerView = AEAListContainerView()
        containerView.dataSource = self
        containerView.setSelectedIndex(0)
        return containerView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        configCustomNaviBar()
        setUpUI()
        navigationController?.interactivePopGestureRecognizer?.delegate = self
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
    
    private func setUpUI() {
        view.backgroundColor = .white
        
        view.addSubview(segmentedView)
        segmentedView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(naviBar.snp.bottom)
        }
    
        view.addSubview(listContainerView)
        listContainerView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(segmentedView.snp.bottom).offset(10)
            make.bottom.equalToSuperview()
        }
    }
    
    private func configCustomNaviBar(){
        // 标题
        naviBar.title = "show_advanced_setting_title".show_localized
        // 右边按钮
        let preSetButtonItem = ShowBarButtonItem(title: "show_advanced_setting_preset".show_localized, target: self, action: #selector(didClickPreSetBarButton))
        naviBar.rightItems = [preSetButtonItem]
        view.addSubview(naviBar)
    }
    
    private func createSettingVCForIndex(_ index: Int) -> ShowVideoSettingVC {
        // 主播端设置
        let broadcasterVideoSettings: [ShowSettingKey] = [
            .lowlightEnhance,
            .colorEnhance,
            .videoDenoiser,
            .videoCaptureSize,
            .FPS,
            .videoBitRate,
            .PVC,
            .SR
        ]
        // 观众端设置
        let audienceVideoSettings: [ShowSettingKey] = [
            .SR
        ]
        
        let audioSettings: [ShowSettingKey]  = [
            .earmonitoring,
            .recordingSignalVolume,
            .musincVolume,
            .audioBitRate
        ]
        let settings = isBroadcaster ? [broadcasterVideoSettings, audioSettings] : [audienceVideoSettings,[]]
        
        let vc = ShowVideoSettingVC()
        vc.settingManager = settingManager
        vc.dataArray = settings[index]
        vc.willChangeSettingParams = {[weak self] in
            guard let wSelf = self else { return false }
            return wSelf.showModifyAlertIfNeeded()
        }
        return vc
    }
    
    // 判断是否需要显示修改预设值的弹窗
    private func showModifyAlertIfNeeded() -> Bool {
        if presetModeName != nil {
            showAlert(message: "当前处于\"\(presetModeName!)\"最佳效果配置修改参数后视频体验将改变") {
                self.presetModeName = nil
            }
        }
        return presetModeName == nil
    }
  
}


extension ShowAdvancedSettingVC {
    // 点击预设按钮
    @objc private func didClickPreSetBarButton() {
        let vc = ShowPresettingVC()
        vc.didSelectedPresetType = {[weak self] type, modeName in
            self?.settingManager.updatePresetForType(type, mode: self?.mode ?? .signle)
            self?.videoSettingVC.reloadData()
            self?.audioSettingVC.reloadData()
            ToastView.show(text: "音频设置已更新至\"\(modeName)\"最佳配置")
            self?.presetModeName = modeName
        }
        present(vc, animated: true)
    }
    
    // 点击保存按钮
    @objc private func didClickSaveBarButton() {
        
    }
}

extension ShowAdvancedSettingVC:  AEAListContainerViewDataSource{
    
    func listContainerView(_ listContainerView: AEAListContainerView, viewControllerFor index: Int) -> UIViewController {
        if index == 0 {
            return videoSettingVC
        }
        return audioSettingVC
    }
}

extension ShowAdvancedSettingVC: AEACategoryViewDelegate {
    func categoryView(_ categoryView: AEACategoryView, didSelect item: AEACategoryItem, index: Int) {
        listContainerView.setSelectedIndex(index)
    }
}
