//
//  ShowAdvancedSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit
import JXCategoryView
import AgoraRtcKit

class ShowAdvancedSettingVC: UIViewController, UIGestureRecognizerDelegate {

    var agoraKit: AgoraRtcEngineKit! {
        didSet {
            settingManager = ShowSettingManager(agoraKit: agoraKit)
        }
    }
    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    private var settingManager: ShowSettingManager!
    
    private let titles = ["show_advance_setting_video_title".show_localized,
                          "show_advance_setting_audio_title".show_localized]
    
    // 指示条
    private lazy var indicator: JXCategoryIndicatorLineView = {
        let indicator = JXCategoryIndicatorLineView()
        indicator.indicatorWidth = 66
        indicator.indicatorHeight = 2
        indicator.indicatorColor = .show_zi03
        return indicator
    }()
    
    // 分类
    private lazy var segmentedView: JXCategoryTitleView = {
        let segmentedView = JXCategoryTitleView()
        segmentedView.isTitleColorGradientEnabled = true
        segmentedView.titles = titles
        segmentedView.titleFont = .show_R_14
        segmentedView.titleSelectedFont = .show_navi_title
        segmentedView.titleColor = .show_Ellipse5
        segmentedView.titleSelectedColor = .show_Ellipse7
        segmentedView.backgroundColor = .clear
        segmentedView.defaultSelectedIndex = 0
        segmentedView.indicators = [self.indicator]
        return segmentedView
    }()

    
    override func viewDidLoad() {
        super.viewDidLoad()
        configCustomNaviBar()
        setUpUI()
        navigationController?.interactivePopGestureRecognizer?.delegate = self
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
//        view.bringSubviewToFront(naviBar)
    }
    
    private func setUpUI() {
        view.backgroundColor = .white
        
        view.addSubview(segmentedView)
        segmentedView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(naviBar.snp.bottom)
            make.height.equalTo(30)
        }
        
        if let listContainerView = JXCategoryListContainerView(type: .scrollView, delegate: self) {
            segmentedView.listContainer = listContainerView
            view.addSubview(listContainerView)
            listContainerView.snp.makeConstraints { make in
                make.left.right.equalToSuperview()
                make.top.equalTo(segmentedView.snp.bottom).offset(25)
                make.bottom.equalToSuperview()
            }
        }
    }
    
    private func configCustomNaviBar(){
        // 标题
        naviBar.title = "show_advanced_setting_title".show_localized
        // 右边按钮
//        let saveBarButtonItem = ShowBarButtonItem(title: "show_advanced_setting_save".show_localized, target: self, action: #selector(didClickSaveBarButton))
        let preSetButtonItem = ShowBarButtonItem(title: "show_advanced_setting_preset".show_localized, target: self, action: #selector(didClickPreSetBarButton))
        naviBar.rightItems = [preSetButtonItem]
        view.addSubview(naviBar)
    }
    
    private func createSettingVCForIndex(_ index: Int) -> ShowVideoSettingVC {
        let videoSettings: [ShowSettingKey] = [.lowlightEnhance, .colorEnhance,.videoDenoiser,.beauty,.BFrame,.videoCaptureSize,.FPS]
        let audioSettings: [ShowSettingKey]  = []
        let settings = [videoSettings, audioSettings]
        
        let vc = ShowVideoSettingVC()
        vc.settingManager = settingManager
        vc.dataArray = settings[index]
        return vc
    }
  
}


extension ShowAdvancedSettingVC {
    // 点击预设按钮
    @objc private func didClickPreSetBarButton() {
        let vc = ShowPresettingVC()
        present(vc, animated: true)
    }
    
    // 点击保存按钮
    @objc private func didClickSaveBarButton() {
        
    }
}


extension ShowAdvancedSettingVC: JXCategoryListContainerViewDelegate {
    
    func number(ofListsInlistContainerView listContainerView: JXCategoryListContainerView!) -> Int {
        titles.count
    }
    
    func listContainerView(_ listContainerView: JXCategoryListContainerView!, initListFor index: Int) -> JXCategoryListContentViewDelegate! {
        return createSettingVCForIndex(index)
    }
    
}
