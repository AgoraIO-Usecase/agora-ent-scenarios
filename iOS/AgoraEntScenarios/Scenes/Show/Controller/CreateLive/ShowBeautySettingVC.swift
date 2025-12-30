//
//  ShowBeautyVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView
import SnapKit

enum ShowBeautyFaceVCType: CaseIterable {
    case beauty
    case adjust  // 画质（色调、色温、饱和度、亮度）
    case style   // 美妆
    case filter  // 滤镜
    case sticker // 贴纸
    case shape   // 脸型（保留用于其他美颜SDK，Agora中脸型参数在beauty页面）
    case animoj
    case background
    
    var title: String {
        switch self {
        case .beauty: return "create_beauty_setting_beauty_face".show_localized
        case .adjust: return "create_beauty_setting_adjust".show_localized  // 画质
        case .style: return "create_beauty_setting_special_effects".show_localized
        case .filter: return "create_beauty_setting_Filter".show_localized
        case .sticker: return "create_beauty_setting_sticker".show_localized
        case .animoj: return "create_beauty_setting_special_animoji".show_localized
        case .shape: return "create_beauty_setting_shape".show_localized
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
        // Default range: 0..1 (will be adjusted based on parameter type)
        slider.minimumValue = 0
        slider.maximumValue = 1
        return slider
    }()
    
    // Track current parameter key to determine value range
    private var currentParameterKey: String?
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
    private var compareButtonRightConstraint: Constraint?
    private var titles: [String] {
        ShowBeautyFaceVCType.allCases.filter({
            if BeautyModel.beautyType == .byte {
                return $0 != .animoj && $0 != .shape && $0 != .filter
            } else if BeautyModel.beautyType == .agora {
                // Agora: 美颜、画质、美妆、滤镜、贴纸、背景（与Android一致）
                return $0 != .animoj && $0 != .shape
            } else if BeautyModel.beautyType == .fu {
                // 相芯: 美颜、风格妆、贴纸、背景
                return $0 == .beauty || $0 == .style || $0 == .sticker || $0 == .background
            } else {
                // 其他类型（如 sense）
                return $0 != .animoj && $0 != .shape && $0 != .filter
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
    
    // 对比按钮（素颜对比，所有美颜类型都支持）
    private lazy var compareButton: UIButton = {
        let compareButton = UIButton(type: .custom)
        compareButton.setImage(UIImage.show_sceneImage(name: "show_beauty_compare"), for: .normal)
        compareButton.setImage(UIImage.show_sceneImage(name: "show_beauty_compare")?
                                .withTintColor(.show_zi03,
                                               renderingMode: .alwaysOriginal), for: .highlighted)
        compareButton.backgroundColor = UIColor(hex: "#000000", alpha: 0.25)
        compareButton.cornerRadius(18)
        
        // Use touch events for press-and-hold behavior
        compareButton.addTarget(self, action: #selector(didTouchDownCompareButton(sender:)), for: .touchDown)
        compareButton.addTarget(self, action: #selector(didTouchUpCompareButton(sender:)), for: [.touchUpInside, .touchUpOutside])
        compareButton.addTarget(self, action: #selector(didTouchCancelCompareButton(sender:)), for: [.touchCancel])
        
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
        button.setTitle("show_beautification_vendor_name".show_localized, for: .normal)
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
    
    private var selectedType: BeautyFactoryType? = nil
    
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
            
            // Compare button visibility is controlled by selectedItemClosure based on current page
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
            beautyFaceVC?.selectedItemClosure = { [weak self] value, isHiddenValue, isShowSegSwitch, parameterKey in
                guard let self = self else { return }
                
                // Update current parameter key and slider range
                self.currentParameterKey = parameterKey
                self.updateSliderRangeForCurrentParameter()
                
                // Set slider value based on parameter type
                let sliderValue = self.convertValueToSliderRange(value)
                self.slider.value = Float(sliderValue)
                
                self.slider.isHidden = isShowSegSwitch ? !ShowAgoraKitManager.isOpenGreen : isHiddenValue
                self.sliderLabel.isHidden = self.slider.isHidden
                // Match Android: compare button is always visible (all control code is commented out in Android)
                // Don't hide compare button based on slider visibility or page type
                self.compareButton.isHidden = false
                self.segSwitch.isHidden = !isShowSegSwitch
                self.segSwitch.isOn = isShowSegSwitch == false ? ShowAgoraKitManager.isOpenGreen : self.segSwitch.isOn
                self.segLabel.isHidden = !isShowSegSwitch
                
                // Match Android layout: compare button is before switch in layout order
                // When switch is visible, position compare button to the left of switch
                // When switch is hidden, position compare button at right edge (-20)
                // Match Android marginStart="18dp" spacing
                self.compareButtonRightConstraint?.deactivate()
                if isShowSegSwitch {
                    // Switch is visible: position compare button to the left of label
                    self.compareButton.snp.remakeConstraints { make in
                        make.centerY.equalTo(self.slider)
                        make.width.height.equalTo(36)
                        make.right.equalTo(self.segLabel.snp.left).offset(-18)
                    }
                } else {
                    // Switch is hidden: position compare button at right edge
                    self.compareButton.snp.remakeConstraints { make in
                        make.centerY.equalTo(self.slider)
                        make.width.height.equalTo(36)
                        self.compareButtonRightConstraint = make.right.equalTo(-20).constraint
                    }
                }
                
                self.updateSliderLabelPostion()
            }
            
            // Set reset callback (match Android onResetClickListener)
            // Note: Android doesn't close dialog on reset, just updates controller view
            beautyFaceVC?.resetClickClosure = { [weak self] in
                // Match Android: updateControllerView(BeautyManager.beautyType)
                // In iOS, we don't need to do anything here as resetBeauty already handles everything
            }
            
            beautyFaceVC?.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        // Ensure default beauty type is Agora (not SenseTime or Byte)
        if BeautyModel.beautyType != .agora && BeautyModel.beautyType != .fu {
            BeautyModel.beautyType = .agora
        }
        beautyVenderButton.setTitle(BeautyModel.beautyType.title, for: .normal)
        vcs = createBeautyVC()
        beautyFaceVC = vcs.first
        
        // Initialize beauty if Agora is selected by default
        // This ensures beauty effects are applied even if user doesn't manually click the vendor button
        if BeautyModel.beautyType == .agora {
            // Check if agoraKit is available before initializing
            if ShowAgoraKitManager.shared.engine != nil {
                BeautyManager.shareManager.updateBeautyRedner()
            }
        }
    }
    
    private func createBeautyVC() -> [ShowBeautyFaceVC] {
        ShowBeautyFaceVCType.allCases.filter({
            if BeautyModel.beautyType == .byte {
                return $0 != .animoj && $0 != .shape && $0 != .filter
            } else if BeautyModel.beautyType == .agora {
                // Agora: 美颜、画质、美妆、滤镜、贴纸、背景（与Android一致）
                return $0 != .animoj && $0 != .shape
            } else if BeautyModel.beautyType == .fu {
                // 相芯: 美颜、风格妆、贴纸、背景
                return $0 == .beauty || $0 == .style || $0 == .sticker || $0 == .background
            } else {
                // 其他类型（如 sense）
                return $0 != .animoj && $0 != .shape && $0 != .filter
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
        
        // 对比按钮 - 位置会根据开关显示状态动态调整
        // Match Android: compare button is before switch in layout order
        view.addSubview(compareButton)
        compareButton.snp.makeConstraints { make in
            make.centerY.equalTo(slider)
            make.width.height.equalTo(36)
            // 默认位置：右侧 -20，当开关显示时，会在开关左侧
            compareButtonRightConstraint = make.right.equalTo(-20).constraint
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
        // Convert slider value to parameter value based on current parameter type
        let parameterValue = convertSliderValueToParameterValue(slider.value)
        beautyFaceVC?.changeValueHandler(value: parameterValue)
        updateSliderLabelPostion()
    }
    
    private func updateSliderLabelPostion() {
        // Display value in -50..50 range format
        let displayValue = convertSliderValueToDisplayValue(slider.value)
        sliderLabel.text = "\(displayValue)"
        let trackRect = slider.trackRect(forBounds: slider.bounds)
        let thumbRect = slider.thumbRect(forBounds: slider.bounds, trackRect: trackRect, value: slider.value)
        sliderLabelCenterCons?.constant = thumbRect.midX
        sliderLabelCenterCons?.isActive = true
    }
    
    // MARK: - Value Range Conversion Methods
    
    /// Check if current parameter requires -50..50 range
    private func requiresNegativeRange(key: String?) -> Bool {
        guard let key = key else { return false }
        
        // 根据不同的美颜类型，使用不同的 key 集合
        // FaceUnity 和 Agora 的参数 key 命名不同
        if BeautyModel.beautyType == .fu {
            // FaceUnity 的参数 key（使用小写匹配，支持大小写和小写两种格式）
            // 注意：原来的代码使用小写 key，但 BeautyModel 中的 key 是大小写混合的
            // 为了兼容，统一转换为小写进行比较
            let negativeRangeKeys: Set<String> = [
                "chin",             // 瘦下巴
                "forehead",         // 发际线
                "eyeposition",      // 眼移动（eyePosition）
                "eyedistance",      // 眼距（eyeDistance）
                "eyecorner",        // 眼角
                "noselength",       // 长鼻（noseLength）
                "mouth",            // 嘴型
                "eyebrowposition",  // 眉上下（eyebrowPosition）
                "eyebrowthickness", // 眉粗细（eyebrowThickness）
                "hue",              // 色调（画质调整）
                "temperature",      // 色温（画质调整）
                "saturation",       // 饱和度（画质调整）
                "brightness"        // 亮度（画质调整）
            ]
            // 统一转换为小写进行比较，支持大小写和小写两种格式
            return negativeRangeKeys.contains(key.lowercased())
        } else if BeautyModel.beautyType == .agora {
            // Agora 的参数 key（全部小写）
            let negativeRangeKeys: Set<String> = [
                // 基础美颜参数
                "clarity",          // 清晰度 - UI: -50~50, SDK: -1.0~1.0
                
                // 脸型参数（-50~50 UI → -100~100 SDK）
                "facelength",       // 长脸
                "chinlength",       // 瘦下巴
                "hairlineheight",   // 发际线
                
                // 眼部参数（-50~50 UI → -100~100 SDK）
                "eyeposition",      // 眼移动
                "eyedistance",      // 眼距
                "eyeinnercorner",   // 内眼角
                "eyeoutercorner",   // 外眼角
                
                // 鼻部参数（-50~50 UI → -100~100 SDK）
                "noselength",       // 长鼻
                "nosegeneral",      // 鼻综合
                
                // 嘴部参数（-50~50 UI → -100~100 SDK）
                "mouthsize",        // 嘴型
                
                // 眉毛参数（-50~50 UI → -100~100 SDK）
                "eyebrowposition",  // 眉上下
                "eyebrowthickness", // 眉粗细
                
                // 画质调整参数（-50~50 UI → -1.0~1.0 SDK）
                "hue",              // 色调
                "temperature",      // 色温
                "saturation",       // 饱和度
                "brightness"        // 亮度
            ]
            return negativeRangeKeys.contains(key.lowercased())
        } else {
            // 其他美颜类型（sense, byte 等）
            // 暂时返回 false，如果需要可以后续添加
            return false
        }
    }
    
    /// Update slider range based on current parameter
    private func updateSliderRangeForCurrentParameter() {
        guard let key = currentParameterKey else {
            // Default range
            slider.minimumValue = 0
            slider.maximumValue = 100
            return
        }
        
        if requiresNegativeRange(key: key) {
            // Range: -50..50 (for clarity and other negative parameters)
            slider.minimumValue = -50
            slider.maximumValue = 50
        } else if BeautyModel.beautyType == .fu || BeautyModel.beautyType == .agora {
            // FaceUnity and Agora: Use 0..100 range to match Android
            slider.minimumValue = 0
            slider.maximumValue = 100
        } else {
            // Other beauty types (sense, byte): keep 0..1 range
            slider.minimumValue = 0
            slider.maximumValue = 1
        }
    }
    
    /// Convert parameter value to slider range
    private func convertValueToSliderRange(_ value: CGFloat) -> CGFloat {
        guard let key = currentParameterKey else {
            // Default: assume parameter is in 0..100 range
            return value
        }
        
        if requiresNegativeRange(key: key) {
            // Parameter value is in -50..50 range, slider is also -50..50
            return value
        } else if BeautyModel.beautyType == .fu {
            // FaceUnity: All parameters (including style makeup) are in 0..100 UI range
            // Style makeup default value is 60 (UI range), matching Android
            return value
        } else if BeautyModel.beautyType == .agora {
            // Agora: Parameter is in 0..100 range, slider is also 0..100
            return value
        } else {
            // Other beauty types (sense, byte): Parameter is in 0..1 range, convert to 0..100 for display
            return value * 100
        }
    }
    
    /// Convert slider value to parameter value
    /// This method should return the value that will be passed to BeautyManager.setBeauty() or setStyle()
    /// For most parameters, the value should be in UI range (-50..50 or 0..100)
    /// For FaceUnity style makeup, the value should be in SDK range (0.0-1.0) because setStyle expects SDK range
    private func convertSliderValueToParameterValue(_ sliderValue: Float) -> CGFloat {
        guard let key = currentParameterKey else {
            // Default: return slider value as is (slider range is already set correctly)
            return CGFloat(sliderValue)
        }
        
        if requiresNegativeRange(key: key) {
            // Slider value is in -50..50 range, return as is (match Android: value is directly used)
            // convertUIValueToSDKValue will handle the conversion to 0-1 range: (value + 50) / 100
            return CGFloat(sliderValue)
        } else if BeautyModel.beautyType == .fu {
                return CGFloat(sliderValue)
        } else if BeautyModel.beautyType == .agora {
            // Agora: Slider value is in 0..100 range, return as is
            // Conversion to SDK range is handled by AgoraBeautyManager
            return CGFloat(sliderValue)
        } else {
            // Other beauty types: Slider value is in 0..1 range, return as is
            return CGFloat(sliderValue)
        }
    }
    
    /// Convert slider value to display value (-50..50 or 0..100)
    private func convertSliderValueToDisplayValue(_ sliderValue: Float) -> Int {
        guard let key = currentParameterKey else {
            // Default: display slider value as is
            return Int(sliderValue)
        }
        
        if requiresNegativeRange(key: key) {
            // Display value in -50..50 range
            return Int(sliderValue)
        } else {
            // Display value in 0..100 range (slider is already 0..100 for FaceUnity)
            return Int(sliderValue)
        }
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
    
    // 按下对比按钮 - 显示素颜
    @objc private func didTouchDownCompareButton(sender: UIButton) {
        // Check license
        guard BeautyManager.shareManager.checkLicense() else {
            ToastView.show(text: "show_beauty_license_disable".show_localized)
            return
        }
        
        // Save current state and disable beauty effects (works for all beauty types)
        BeautyManager.shareManager.actionBareFace()
        sender.isHighlighted = true
    }
    
    // 释放对比按钮 - 恢复美颜
    @objc private func didTouchUpCompareButton(sender: UIButton) {
        // Restore beauty effects (works for all beauty types)
        BeautyManager.shareManager.actionBeauty()
        sender.isHighlighted = false
    }
    
    // 取消触摸 - 恢复美颜
    @objc private func didTouchCancelCompareButton(sender: UIButton) {
        // Restore beauty effects (works for all beauty types)
        BeautyManager.shareManager.actionBeauty()
        sender.isHighlighted = false
    }
}


extension ShowBeautySettingVC: JXCategoryViewDelegate {
    func categoryView(_ categoryView: JXCategoryBaseView!, didSelectedItemAt index: Int) {
        beautyFaceVC = vcs[index]
        // Match Android: compare button is always visible (all control code is commented out in Android)
        // No need to control visibility when switching pages
        compareButton.isHidden = false
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
