//
//  ShowBeautyVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView

class ShowBeautySettingVC: UIViewController {
    
    var selectedItem: ((_ item: String)->())?
    var dismissed: (()->())?
    
    private var slider: UISlider!
    private let titles = ["create_beauty_setting_beauty_face".show_localized,
                          "create_beauty_setting_filter".show_localized,
                          "create_beauty_setting_special_effects".show_localized,
                          "create_beauty_setting_make_up".show_localized,
                          "create_beauty_setting_bg".show_localized]
    
    // 背景
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .show_beauty_setting_bg
        return bgView
    }()
    
    // 对比按钮
    private lazy var compareButton = {
        let compareButton = UIButton(type: .custom)
        compareButton.setImage(UIImage.show_sceneImage(name: "show_beauty_compare"), for: .normal)
        compareButton.addTarget(self, action: #selector(didClickCompareButton), for: .touchUpInside)
        return compareButton
    }()
    
    // 指示条
    private lazy var indicator = {
        let indicator = JXCategoryIndicatorLineView()
        indicator.indicatorWidth = 30
        indicator.indicatorHeight = 2
        indicator.indicatorColor = .show_main_text
        return indicator
    }()
    
    // 分类
    private lazy var segmentedView = {
        let segmentedView = JXCategoryTitleView()
        segmentedView.isTitleColorGradientEnabled = true
        segmentedView.titles = titles
        segmentedView.titleFont = .show_R_14
        segmentedView.titleSelectedFont = .show_M_15
        segmentedView.titleColor = .show_beauty_deselect
        segmentedView.titleSelectedColor = .show_main_text
        segmentedView.backgroundColor = .clear
        segmentedView.defaultSelectedIndex = 0
        segmentedView.delegate = self
        segmentedView.indicators = [self.indicator]
        return segmentedView
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI(){
        view.backgroundColor = .clear
        
        // slider
        slider = UISlider()
        slider.minimumTrackTintColor = .show_zi03
        slider.maximumTrackTintColor = .show_slider_tint
        view.addSubview(slider)
        slider.snp.makeConstraints { make in
            make.left.equalTo(22)
            make.right.equalTo(-80)
            make.height.equalTo(30)
            make.bottom.equalTo(-214)
        }
        
        // 对比按钮
        
        view.addSubview(compareButton)
        compareButton.snp.makeConstraints { make in
            make.centerY.equalTo(slider)
            make.right.equalTo(-20)
        }
        
        view.addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(203)
        }

        bgView.addSubview(segmentedView)
        segmentedView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(10)
            make.height.equalTo(30)
        }
        
        if let listContainerView = JXCategoryListContainerView(type: .scrollView, delegate: self) {
            segmentedView.listContainer = listContainerView
            bgView.addSubview(listContainerView)
            listContainerView.snp.makeConstraints { make in
                make.left.right.equalToSuperview()
                make.top.equalTo(segmentedView.snp.bottom).offset(25)
                make.height.equalTo(70)
            }
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        bgView.setRoundingCorners([.topLeft, .topRight], radius: 20)
    }

}

extension ShowBeautySettingVC {
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
        dismissed?()
    }
    
    // 点击对比按钮
    @objc private func didClickCompareButton(){
        
    }
}


extension ShowBeautySettingVC: JXCategoryViewDelegate {
    
}

extension ShowBeautySettingVC: JXCategoryListContainerViewDelegate {
    
    func number(ofListsInlistContainerView listContainerView: JXCategoryListContainerView!) -> Int {
        titles.count
    }
    
    func listContainerView(_ listContainerView: JXCategoryListContainerView!, initListFor index: Int) -> JXCategoryListContentViewDelegate! {
        return ShowBeautyFaceVC()
    }
    
}
