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
    var isOutside = false

    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    var settingManager: ShowAgoraKitManager!
    
    // 当前设置的预设值
    var presetModeName: String?
    
    private let titles = ["show_advance_setting_video_title".show_localized,
                          "show_advance_setting_audio_title".show_localized]
    
    private lazy var videoSettingVC: ShowVideoSettingVC? = {
        return createSettingVCForIndex(0)
    }()
    
    private lazy var audioSettingVC: ShowVideoSettingVC? = {
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
        if isBroadcaster {
            // 自动弹出预设
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                self.didClickPreSetBarButton()
            }
        }
    }
    
    private func setUpUI() {
        view.backgroundColor = .white
        
        view.addSubview(segmentedView)
        segmentedView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(naviBar.snp.bottom)
        }
        segmentedView.isHidden = !isBroadcaster
        
        view.addSubview(listContainerView)
        listContainerView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            if self.isBroadcaster {
                make.top.equalTo(segmentedView.snp.bottom).offset(10)
            }else{
                make.top.equalTo(naviBar.snp.bottom).offset(10)
            }
            make.bottom.equalToSuperview()
        }
    }
    
    private func configCustomNaviBar(){
        // 标题
        naviBar.title = "show_advanced_setting_title".show_localized
        // 右边按钮
        if isBroadcaster {
            let preSetButtonItem = ShowBarButtonItem(title: "show_advanced_setting_preset".show_localized, target: self, action: #selector(didClickPreSetBarButton))
            naviBar.rightItems = [preSetButtonItem]
        }
        view.addSubview(naviBar)
    }
    
    private func createSettingVCForIndex(_ index: Int) -> ShowVideoSettingVC? {
        // 主播端设置
        let outsideSettings: [ShowSettingKey] = [
            .H265,
            .colorEnhance,
            .lowlightEnhance,
            .videoDenoiser,
            .PVC,
            .videoEncodeSize,
            .FPS,
            .videoBitRate
        ]
        let insideSettings: [ShowSettingKey] = [
            .colorEnhance,
            .lowlightEnhance,
            .videoDenoiser,
            .PVC,
            .videoEncodeSize,
            .FPS,
            .videoBitRate
        ]
        let broadcasterVideoSettings: [ShowSettingKey] = isOutside ? outsideSettings : insideSettings
        // 观众端设置
        let audienceVideoSettings: [ShowSettingKey] = [
            .SR
        ]
        
        let audioSettings: [ShowSettingKey]  = [
            .earmonitoring,
            .recordingSignalVolume,
            .musincVolume,
        ]
        let settings = isBroadcaster ? [broadcasterVideoSettings, audioSettings] : [audienceVideoSettings]
        if settings.count <= index {
            return nil
        }
        
        let vc = ShowVideoSettingVC()
        vc.settingManager = settingManager
       
        vc.dataArray = settings[index]
        vc.willChangeSettingParams = {[weak self] key, value in
            guard let wSelf = self else { return false }
            return wSelf.showModifyAlertIfNeeded(key,value: value)
        }
        return vc
    }
    
    // 判断是否需要显示修改预设值的弹窗
    private func showModifyAlertIfNeeded(_ key: ShowSettingKey, value: Any) -> Bool {
        if presetModeName != nil {
            let msg1 = "show_presetting_alert_will_change_value_message1".show_localized
            let msg2 = "show_presetting_alert_will_change_value_message2".show_localized
            showAlert(title:"show_presetting_alert_will_change_value_title".show_localized, message: "\(msg1)\"\(presetModeName!)\"\(msg2)") { [weak self] in
                self?.presetModeName = nil
                key.writeValue(value)
                self?.videoSettingVC?.reloadData()
                self?.audioSettingVC?.reloadData()
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
            self?.videoSettingVC?.reloadData()
            self?.audioSettingVC?.reloadData()
            let text1 = "show_presetting_update_toast1".show_localized
            let text2 = "show_presetting_update_toast2".show_localized
            ToastView.show(text: "\(text1)\"\(modeName)\"\(text2)")
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
            return videoSettingVC ?? UIViewController()
        }
        return audioSettingVC ?? UIViewController()
    }
}

extension ShowAdvancedSettingVC: AEACategoryViewDelegate {
    func categoryView(_ categoryView: AEACategoryView, didSelect item: AEACategoryItem, index: Int) {
        listContainerView.setSelectedIndex(index)
    }
}
