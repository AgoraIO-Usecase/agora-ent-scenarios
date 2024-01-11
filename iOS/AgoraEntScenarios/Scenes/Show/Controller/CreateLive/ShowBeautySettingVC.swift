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
    case style
    case adjust
    case animoj
    case sticker
    case background
    
    var title: String {
        switch self {
        case .beauty: return "create_beauty_setting_beauty_face".show_localized
        case .style: return "create_beauty_setting_special_effects".show_localized
        case .adjust: return "create_beauty_setting_special_adjust".show_localized
        case .animoj: return "create_beauty_setting_special_animoji".show_localized
        case .sticker: return "create_beauty_setting_sticker".show_localized
        case .background: return "背景".show_localized
        }
    }
}

class ShowBeautySettingVC: UIViewController {
    
    var selectedItem: ((_ item: String)->())?
    var dismissed: (()->())?
    
    private lazy var slider: UISlider = {
       let slider = UISlider()
        slider.minimumTrackTintColor = .show_zi03
        slider.maximumTrackTintColor = .show_slider_tint
        slider.addTarget(self, action: #selector(onTapSliderHandler(sender:)), for: .valueChanged)
        return slider
    }()
    private lazy var sliderLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#FFFFFF", alpha: 1.0)
        label.font = .systemFont(ofSize: 12)
        label.setContentHuggingPriority(.defaultHigh, for: .vertical)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private var sliderLabelCenterCons: NSLayoutConstraint?
    private var titles: [String] {
        ShowBeautyFaceVCType.allCases.filter({
            if BeautyModel.beautyType == .byte {
                return $0 != .animoj
            } else if BeautyModel.beautyType == .agora {
                return $0 != .animoj && $0 != .sticker && $0 != .style
            } else {
                return $0 != .animoj
            }
        }).map({ $0.title })
    }
    private var vcs: [ShowBeautyFaceVC] = []
    
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
    
    private lazy var beautyVenderButton: LLButton = {
        let button = LLButton()
        button.setTitleColor(UIColor(hex: "#FFFFFF", alpha: 0.6), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14)
        button.setTitle(BeautyModel.beautyType.title, for: .normal)
        button.backgroundColor = UIColor(hex: "#18191B", alpha: 0.4)
        let image = UIImage.sceneImage(name: "show_beauty_vernder_arrow_right") ?? UIImage()
        button.setImage(image, for: .normal)
        button.cornerRadius(8)
        button.imageAlignment = .right
        button.spaceBetweenTitleAndImage = 5
        button.addTarget(self, action: #selector(onClickBeautyVenderButton(sender:)), for: .touchUpInside)
        button.setContentHuggingPriority(.defaultHigh, for: .vertical)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()
    private lazy var beautyVenderView: ShowBeautyVenderView = {
        let view = ShowBeautyVenderView()
        view.onSelectedBeautyVenderClosure = { [weak self] type in
            guard let self = self else { return }
            self.beautyVenderButton.setTitle(type.title, for: .normal)
            BeautyManager.shareManager.destroy(isAll: false)
            BeautyModel.beautyType = type
            ShowBeautyFaceVC.resetData()
            BeautyManager.shareManager.updateBeautyRedner()
            self.vcs = self.createBeautyVC()
            self.segmentedView.titles = self.titles
            self.segmentedView.reloadData()
            self.segmentedView.selectItem(at: 0)
            self.onClickBeautyVenderButton(sender: self.beautyVenderButton)
        }
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private var beautyVenderViewH: NSLayoutConstraint?
        
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
                self.sliderLabel.isHidden = self.slider.isHidden
                self.compareButton.isHidden = isShowSegSwitch ? true : isHiddenValue
                self.segSwitch.isHidden = !isShowSegSwitch
                self.segSwitch.isOn = isShowSegSwitch == false ? ShowAgoraKitManager.isOpenGreen : self.segSwitch.isOn
                self.segLabel.isHidden = !isShowSegSwitch
                self.slider.setValue(Float(value), animated: true)
                self.updateSliderLabelPostion()
            }
            beautyFaceVC?.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        vcs = createBeautyVC()
        beautyFaceVC = vcs.first
    }
    
    private func createBeautyVC() -> [ShowBeautyFaceVC] {
        ShowBeautyFaceVCType.allCases.filter({
            if BeautyModel.beautyType == .byte {
                return $0 != .animoj
            } else if BeautyModel.beautyType == .agora {
                return $0 != .animoj && $0 != .sticker && $0 != .style
            } else {
                return $0 != .animoj
            }
        }).map({ ShowBeautyFaceVC(type: $0) })
    }

    private func setupUI(){
        view.backgroundColor = .clear
        
        // slider
        view.addSubview(slider)
        slider.snp.makeConstraints { make in
            make.left.equalTo(22)
            make.right.equalTo(-83)
            make.height.equalTo(30)
            make.bottom.equalTo(-214)
        }
        view.addSubview(sliderLabel)
        sliderLabel.bottomAnchor.constraint(equalTo: slider.topAnchor, constant: -3).isActive = true
        sliderLabel.heightAnchor.constraint(equalToConstant: 17).isActive = true
        sliderLabelCenterCons = sliderLabel.centerXAnchor.constraint(equalTo: slider.leadingAnchor)
        
        // 对比按钮
        view.addSubview(compareButton)
        compareButton.snp.makeConstraints { make in
            make.centerY.equalTo(slider)
            make.right.equalTo(-20)
            make.width.height.equalTo(36)
        }
        
        view.addSubview(beautyVenderButton)
        beautyVenderButton.leadingAnchor.constraint(equalTo: slider.leadingAnchor).isActive = true
        beautyVenderButton.bottomAnchor.constraint(equalTo: sliderLabel.topAnchor, constant: -5).isActive = true
        beautyVenderButton.widthAnchor.constraint(equalToConstant: 100).isActive = true
        beautyVenderButton.heightAnchor.constraint(equalToConstant: 40).isActive = true
        
        view.addSubview(beautyVenderView)
        beautyVenderView.leadingAnchor.constraint(equalTo: beautyVenderButton.leadingAnchor).isActive = true
        beautyVenderView.bottomAnchor.constraint(equalTo: beautyVenderButton.topAnchor).isActive = true
        beautyVenderView.trailingAnchor.constraint(equalTo: beautyVenderButton.trailingAnchor).isActive = true
        beautyVenderViewH = beautyVenderView.heightAnchor.constraint(equalToConstant: 0)
        beautyVenderViewH?.isActive = true
        
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
        updateSliderLabelPostion()
    }
    
    private func updateSliderLabelPostion() {
        sliderLabel.text = "\(Int(slider.value * 100))"
        let trackRect = slider.trackRect(forBounds: slider.bounds)
        let thumbRect = slider.thumbRect(forBounds: slider.bounds, trackRect: trackRect, value: slider.value)
        sliderLabelCenterCons?.constant = thumbRect.midX
        sliderLabelCenterCons?.isActive = true
    }
    
    @objc
    private func onTapSegSwitch(sender: UISwitch) {
        func realChange(isOn: Bool){
            ShowAgoraKitManager.isOpenGreen = isOn
            slider.isHidden = !isOn
            sliderLabel.isHidden = slider.isHidden
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
    
    @objc
    private func onClickBeautyVenderButton(sender: UIButton) {
        sender.isSelected = !sender.isSelected
        beautyVenderViewH?.constant = sender.isSelected ? CGFloat(40 * BeautyFactoryType.allCases.count) : 0
        beautyVenderViewH?.isActive = true
        beautyVenderButton.layer.maskedCorners = sender.isSelected ? [.layerMinXMaxYCorner, .layerMaxXMaxYCorner] : [.layerMinXMinYCorner, .layerMaxXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        UIView.animate(withDuration: 0.25) {
            self.view.layoutIfNeeded()
            sender.imageView?.transform = sender.isSelected ? .init(rotationAngle: -.pi / 2) : .identity
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
        // 判断存在美颜证书
        if BeautyManager.shareManager.checkLicense() {
            sender.isSelected = !sender.isSelected
            BeautyManager.shareManager.isEnableBeauty = sender.isSelected
        } else {
            ToastView.show(text: "show_beauty_license_disable".show_localized)
        }
        slider.isHidden = !sender.isSelected
        sliderLabel.isHidden = slider.isHidden
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

enum LLImageAlignment: NSInteger {
    case left = 0
    case top
    case bottom
    case right
}
class LLButton: UIButton {
    var imageAlignment: LLImageAlignment = .left
    var spaceBetweenTitleAndImage: CGFloat = 0
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let space: CGFloat = spaceBetweenTitleAndImage
        let titleW: CGFloat = titleLabel?.bounds.width ?? 0
        let titleH: CGFloat = titleLabel?.bounds.height ?? 0
        let imageW: CGFloat = imageView?.bounds.width ?? 0
        let imageH: CGFloat = imageView?.bounds.height ?? 0
        let btnCenterX: CGFloat = bounds.width / 2
        let imageCenterX: CGFloat = btnCenterX - titleW / 2
        let titleCenterX = btnCenterX + imageW / 2
        
        switch imageAlignment {
        case .top:
            titleEdgeInsets = UIEdgeInsets(top: imageH / 2 + space / 2, left: -(titleCenterX - btnCenterX), bottom: -(imageH/2 + space/2), right: titleCenterX-btnCenterX)
            imageEdgeInsets = UIEdgeInsets(top: -(titleH / 2 + space / 2), left: btnCenterX - imageCenterX, bottom: titleH / 2 + space / 2, right: -(btnCenterX - imageCenterX))
            
        case .left:
            titleEdgeInsets = UIEdgeInsets(top: 0, left: space / 2, bottom: 0, right: -space / 2)
            imageEdgeInsets = UIEdgeInsets(top: 0, left: -space / 2, bottom: 0, right: space)
            
        case .bottom:
            titleEdgeInsets = UIEdgeInsets(top: -(imageH / 2 + space / 2), left: -(titleCenterX - btnCenterX), bottom: imageH / 2 + space / 2, right: titleCenterX - btnCenterX)
            imageEdgeInsets = UIEdgeInsets(top: titleH / 2 + space / 2, left: btnCenterX - imageCenterX,bottom: -(titleH / 2 + space / 2), right: -(btnCenterX - imageCenterX))
            
        case .right:
            titleEdgeInsets = UIEdgeInsets(top: 0, left: -(imageW + space / 2), bottom: 0, right: imageW + space / 2)
            imageEdgeInsets = UIEdgeInsets(top: 0, left: titleW + space / 2, bottom: 0, right: -(titleW + space / 2))
        }
    }
}
