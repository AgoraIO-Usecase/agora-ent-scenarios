//
//  ShowBeautyVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView

enum ShowBeautyFaceVCType: CaseIterable {
    case beauty
//    case filter
    case style
    case adjust
    case sticker
    case background
    
    var title: String {
        switch self {
        case .beauty: return "create_beauty_setting_beauty_face".show_localized
//        case .filter: return "create_beauty_setting_filter".show_localized
        case .style: return "create_beauty_setting_special_effects".show_localized
        case .adjust: return "create_beauty_setting_special_adjust".show_localized
        case .sticker: return "create_beauty_setting_sticker".show_localized
        case .background: return "背景".show_localized
        }
    }
}

class ShowBeautySettingVC: UIViewController {
    
    var selectedItem: ((_ item: String)->())?
    var dismissed: (()->())?
    
    private var slider: UISlider!
    private let titles = ShowBeautyFaceVCType.allCases.filter({
        if BeautyModel.beautyType == .byte {
            return $0 != .adjust
        }
        return true
    }).map({ $0.title })
    private let vcs = ShowBeautyFaceVCType.allCases.map({ ShowBeautyFaceVC(type: $0) })
    
    // 背景
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .show_dark_cover_bg
        return bgView
    }()
    
    // 对比按钮
    private lazy var compareButton: UIButton = {
        let compareButton = UIButton(type: .custom)
        compareButton.setImage(UIImage.show_sceneImage(name: "show_beauty_compare"), for: .selected)
        if #available(iOS 13.0, *) {
            compareButton.setImage(UIImage.show_sceneImage(name: "show_beauty_compare")?
                                    .withTintColor(.show_zi03,
                                                   renderingMode: .alwaysOriginal), for: .normal)
        }
        compareButton.addTarget(self, action: #selector(didClickCompareButton(sender:)), for: .touchUpInside)
        compareButton.backgroundColor = UIColor(hex: "#000000", alpha: 0.25)
        compareButton.isSelected = BeautyManager.shareManager.isEnableBeauty
        compareButton.cornerRadius(18)
        return compareButton
    }()
    
    private lazy var segLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.text = "绿幕"
        label.font = .systemFont(ofSize: 12)
        label.isHidden = true
        return label
    }()
    private lazy var segSwitch: UISwitch = {
        let sw = UISwitch()
        sw.onTintColor = .show_zi03
        sw.addTarget(self, action: #selector(onTapSegSwitch(sender:)), for: .valueChanged)
        sw.isHidden = true
        return sw
    }()
    
    // 指示条
    private lazy var indicator: JXCategoryIndicatorLineView = {
        let indicator = JXCategoryIndicatorLineView()
        indicator.indicatorWidth = 30
        indicator.indicatorHeight = 2
        indicator.indicatorColor = .show_main_text
        return indicator
    }()
    
    // 分类
    private lazy var segmentedView: JXCategoryTitleView = {
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
        
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overCurrentContext
//        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private var beautyFaceVC: ShowBeautyFaceVC? {
        didSet {
            beautyFaceVC?.selectedItemClosure = { [weak self] value, isHiddenValue, isShowSegSwitch in
                guard let self = self else { return }
                self.slider.isHidden = isShowSegSwitch ? !ShowAgoraKitManager.isOpenGreen : isHiddenValue
                self.compareButton.isHidden = isShowSegSwitch ? true : isHiddenValue
                self.segSwitch.isHidden = !isShowSegSwitch
                self.segSwitch.isOn = isShowSegSwitch == false ? ShowAgoraKitManager.isOpenGreen : self.segSwitch.isOn
                self.segLabel.isHidden = !isShowSegSwitch
                self.slider.setValue(Float(value), animated: true)
            }
            beautyFaceVC?.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        beautyFaceVC = vcs.first
    }

    private func setupUI(){
        view.backgroundColor = .clear
        
        // slider
        slider = UISlider()
        slider.minimumTrackTintColor = .show_zi03
        slider.maximumTrackTintColor = .show_slider_tint
        slider.addTarget(self, action: #selector(onTapSliderHandler(sender:)), for: .valueChanged)
        view.addSubview(slider)
        slider.snp.makeConstraints { make in
            make.left.equalTo(22)
            make.right.equalTo(-83)
            make.height.equalTo(30)
            make.bottom.equalTo(-214)
        }
        
        // 对比按钮
        view.addSubview(compareButton)
        compareButton.snp.makeConstraints { make in
            make.centerY.equalTo(slider)
            make.right.equalTo(-20)
            make.width.height.equalTo(36)
        }
        
        view.addSubview(segSwitch)
        segSwitch.snp.makeConstraints { make in
            make.centerY.equalTo(slider)
            make.right.equalTo(-7)
        }

        view.addSubview(segLabel)
        segLabel.snp.makeConstraints { make in
            make.centerY.equalTo(slider)
            make.right.equalTo(segSwitch.snp.left).offset(-2)
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

    @objc
    private func onTapSliderHandler(sender: UISlider) {
        beautyFaceVC?.changeValueHandler(value: CGFloat(sender.value))
    }
    
    @objc
    private func onTapSegSwitch(sender: UISwitch) {
        func realChange(isOn: Bool){
            ShowAgoraKitManager.isOpenGreen = isOn
            slider.isHidden = !isOn
            if ShowAgoraKitManager.isBlur {
                ShowAgoraKitManager.shared.enableVirtualBackground(isOn: true,
                                                                   greenCapacity: slider.value)
            } else {
                ShowAgoraKitManager.shared.seVirtualtBackgoundImage(imagePath: "show_live_mritual_bg",
                                                                    isOn: true,
                                                                    greenCapacity: slider.value)
            }
        }
        if sender.isOn == true {
            sender.isOn = false
            showAlert(title: "提示", message: "为了保证虚拟背景最佳效果，请确认实际环境已搭建绿幕，否则虚拟背景不生效哦", confirmTitle: "确认开启", cancelTitle: "暂不开启") {
                realChange(isOn: true)
                sender.isOn = true
            }
        }else{
            realChange(isOn: sender.isOn)
        }
    }
}

extension ShowBeautySettingVC {
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
        dismissed?()
    }
    
    // 点击对比按钮
    @objc private func didClickCompareButton(sender: UIButton){
        sender.isSelected = !sender.isSelected
        BeautyManager.shareManager.isEnableBeauty = sender.isSelected
    }
}


extension ShowBeautySettingVC: JXCategoryViewDelegate {
    func categoryView(_ categoryView: JXCategoryBaseView!, didSelectedItemAt index: Int) {
        beautyFaceVC = vcs[index]
        if index == vcs.count - 1 {
            compareButton.isHidden = true
        }
    }
}

extension ShowBeautySettingVC: JXCategoryListContainerViewDelegate {
    
    func number(ofListsInlistContainerView listContainerView: JXCategoryListContainerView?) -> Int {
        titles.count
    }
    
    func listContainerView(_ listContainerView: JXCategoryListContainerView?,
                           initListFor index: Int) -> JXCategoryListContentViewDelegate? {
        vcs[index]
    }
}
