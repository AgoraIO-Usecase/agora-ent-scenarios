//
//  ShowBeautyFaceVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView

class ShowBeautyFaceVC: UIViewController {
    
    var selectedItemClosure: ((_ value: CGFloat, _ isHiddenSldier: Bool, _ isShowSegSwitch: Bool, _ parameterKey: String?) -> Void)?
    var resetClickClosure: (() -> Void)?
    
    var defalutSelectIndex = 0
       
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumInteritemSpacing = 15
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        layout.itemSize = CGSize(width: 48, height: 70)
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.register(ShowBeautyFaceCell.self, forCellWithReuseIdentifier: NSStringFromClass(ShowBeautyFaceCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.showsHorizontalScrollIndicator = false
        return collectionView
    }()
    static var beautyData = BeautyModel.createBeautyData()
    static var styleData = BeautyModel.createStyleData()
    static var adjustData = BeautyModel.createAdjustData()
    static var filterData = BeautyModel.createFilterData()
    static var stickerData = BeautyModel.createStickerData()
    static var animojData = BeautyModel.createAnimojiData()
    static var shapeData = BeautyModel.createAgoraShapeData()
    static var backgroundData = BeautyModel.createBackgroundData()
     
    private lazy var dataArray: [BeautyModel] = {
        var tempArray: [BeautyModel] = []
        switch type {
        case .beauty: tempArray = ShowBeautyFaceVC.beautyData
        case .adjust: tempArray = ShowBeautyFaceVC.adjustData  // 画质
        case .shape: tempArray = ShowBeautyFaceVC.shapeData
        case .style: tempArray = ShowBeautyFaceVC.styleData
        case .filter: tempArray = ShowBeautyFaceVC.filterData
        case .sticker: tempArray = ShowBeautyFaceVC.stickerData
        case .animoj: return ShowBeautyFaceVC.animojData
        case .background: tempArray = ShowBeautyFaceVC.backgroundData
        }
        return tempArray
    }()
    
    private var type: ShowBeautyFaceVCType = .beauty
    
    init(type: ShowBeautyFaceVCType) {
        super.init(nibName: nil, bundle: nil)
        self.type = type
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configDefaultSelect()
    }
    
    func changeValueHandler(value: CGFloat) {
        // Check if current parameter supports negative values
        // For adjust type (temperature, saturation, etc.), values can be negative (-50 to 50)
        // For beauty type, some parameters also support negative values (chinLength, hairlineHeight, noseLength, mouthSize, etc.)
        if type != .adjust {
            // Check if current selected parameter supports negative values
            guard !dataArray.isEmpty else { return }
            let model = dataArray[defalutSelectIndex]
            if let key = model.key {
                // Use shared utility method to check if this parameter supports negative values
                if !ShowBeautySettingVC.isNegativeRangeKey(key) {
                    // This parameter doesn't support negative values, reject negative input
                    guard value >= 0 else { return }
                }
                // If parameter supports negative values, allow any value (including negative)
            } else {
                // No key means "none" button, should be >= 0
                guard value >= 0 else { return }
            }
        }
        setBeautyHandler(value: value, isReset: false)
    }
    
    func reloadData() {
        collectionView.reloadData()
    }
    
    static func resetData(){
        beautyData = BeautyModel.createBeautyData()
        shapeData = BeautyModel.createShapBeautyData()
        styleData = BeautyModel.createStyleData()
        adjustData = BeautyModel.createAdjustData()
        filterData = BeautyModel.createFilterData()
        stickerData = BeautyModel.createStickerData()
        backgroundData = BeautyModel.createBackgroundData()
    }
    
    private func setBeautyHandler(value: CGFloat, isReset: Bool, previousSelectedIndex: Int? = nil) {
        guard !dataArray.isEmpty else { return }
        let model = dataArray[defalutSelectIndex]
        switch type {
        case .beauty:
            if isReset {
                // Use the provided previousSelectedIndex, or fallback to current defalutSelectIndex
                let prevIndex = previousSelectedIndex ?? defalutSelectIndex
                // Reset beauty parameters - this will update dataArray values
                BeautyManager.shareManager.reset(datas: dataArray, type: type)
                BeautyManager.shareManager.isEnableBeauty = true
                // After reset, update slider with the value of the previously selected parameter
                if prevIndex < dataArray.count {
                    let previousModel = dataArray[prevIndex]
                    if let key = previousModel.key {
                        // Update slider with the reset value of the previously selected parameter
                        selectedItemClosure?(previousModel.value, false, false, key)
                    }
                }
                // Reload collection view to reflect reset values
                collectionView.reloadData()
                return
            }
            // For non-reset operations, update model value
            model.value = value
            // Handle "none" button: disable beauty (match Android: beautyConfig.beauty = false)
            if model.key == nil {
                BeautyManager.shareManager.isEnableBeauty = false
                return
            }
            // Enable beauty when setting any beauty parameter (match Android: beauty = true in setter)
            // Only set if not already enabled to avoid unnecessary enable() calls
            if !BeautyManager.shareManager.isEnableBeauty {
            BeautyManager.shareManager.isEnableBeauty = true
            }
            BeautyManager.shareManager.setBeauty(path: model.path,
                                                     key: model.key,
                                                     value: model.value)
                        
        case .adjust:
            // 画质调整参数（色调、色温、饱和度、亮度）
            // Handle "none" button: reset all adjust parameters to 0
            if model.key == nil {
                // Reset all adjust parameters
                ShowBeautyFaceVC.adjustData.forEach { adjustModel in
                    if let adjustKey = adjustModel.key {
                        BeautyManager.shareManager.setBeauty(path: adjustModel.path,
                                                                 key: adjustKey,
                                                                 value: 0)
                    }
                }
                return
            }
            // Update model.value with the new value (important: this was missing!)
            // Note: adjust type doesn't have reset button, so isReset is always false
            model.value = value
            // Enable beauty when setting adjust parameters (required for Agora SDK)
            if !BeautyManager.shareManager.isEnableBeauty {
                BeautyManager.shareManager.isEnableBeauty = true
            }
            BeautyManager.shareManager.setBeauty(path: model.path,
                                                     key: model.key,
                                                     value: model.value)
            
        case .style:
            if model.key == nil {
                BeautyManager.shareManager.resetStyle(datas: dataArray)
                return
            }
            // Update model.value with the new value
            // Note: style type doesn't have reset button, so isReset is always false
            model.value = value
            BeautyManager.shareManager.setStyle(path: model.path,
                                                    key: model.key,
                                                    value: model.value)
            
        case .sticker:
            // For FaceUnity, sticker uses path (not key), so check path instead of key
            // For Agora, sticker uses key, so check key
            if BeautyModel.beautyType == .fu {
                // FaceUnity: check path
                if model.path == nil {
                    BeautyManager.shareManager.resetSticker(datas: dataArray)
                    return
                }
            } else {
                // Agora and others: check key
                if model.key == nil {
                    BeautyManager.shareManager.resetSticker(datas: dataArray)
                    return
                }
            }
            // Update model.value with the new value
            // Note: sticker type doesn't have reset button, so isReset is always false
            model.value = value
            // Match example code: pass path, key, and value
            BeautyManager.shareManager.setSticker(path: model.path,
                                                  key: model.key,
                                                  value: model.value)
            
        case .animoj:
            BeautyManager.shareManager.setAnimoji(path: model.path)
            
        case .background:
            if model.path == nil {
                ShowAgoraKitManager.isOpenGreen = false
                ShowAgoraKitManager.isBlur = false
                ShowAgoraKitManager.shared.enableVirtualBackground(isOn: false)
                ShowAgoraKitManager.shared.seVirtualtBackgoundImage(imagePath: nil,
                                                                    isOn: false)
            } else if model.path == "xuhua" {
                ShowAgoraKitManager.isBlur = true
                ShowAgoraKitManager.shared.enableVirtualBackground(isOn: true,
                                                                   greenCapacity: Float(value))
                
            } else {
                ShowAgoraKitManager.isBlur = false
                ShowAgoraKitManager.shared.seVirtualtBackgoundImage(imagePath: model.key,
                                                                    isOn: true,
                                                                    greenCapacity: Float(value))
            }
            
        case .shape:
            // Update model.value with the new value
            // Note: shape type doesn't have reset button, so isReset is always false
            model.value = value
            BeautyManager.shareManager.setBeauty(path: model.path,
                                                     key: model.key,
                                                     value: model.value)
        case .filter:
            if model.key == nil {
                BeautyManager.shareManager.resetFilter(datas: dataArray)
                return
            }
            // Update model.value with the new value
            // Note: filter type doesn't have reset button, so isReset is always false
            model.value = value
            BeautyManager.shareManager.setFilter(path: model.path, key: model.key, value: model.value)
        }
        
        
    }
    
    private func setUpUI(){
        // 列表
        view.addSubview(collectionView)
        collectionView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    // 默认选中
    private func configDefaultSelect(){
        CATransaction.begin()
        CATransaction.setCompletionBlock {
            let indexPath = IndexPath(item: self.defalutSelectIndex, section: 0)
            if self.collectionView.numberOfItems(inSection: 0)  > self.defalutSelectIndex {
                self.collectionView.selectItem(at: indexPath, animated: false, scrollPosition: .left)
            }
        }
        collectionView.reloadData()
        CATransaction.commit()
    }
}

extension ShowBeautyFaceVC: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: ShowBeautyFaceCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(ShowBeautyFaceCell.self),
                                                                          for: indexPath) as! ShowBeautyFaceCell
        let model = dataArray[indexPath.item]
        cell.setupModel(model: model)
        if model.isSelected {
            selectedItemClosure?(model.value, model.key == nil, type == .background && indexPath.item > 0, model.key)
            defalutSelectIndex = indexPath.item
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let preModel = dataArray[defalutSelectIndex]
        let previousSelectedIndex = defalutSelectIndex // Save before updating
        preModel.isSelected = false
        dataArray[defalutSelectIndex] = preModel
        collectionView.reloadItems(at: [IndexPath(item: defalutSelectIndex, section: 0)])
        
        defalutSelectIndex = indexPath.item
        let model = dataArray[indexPath.item]
        
        // 区分"无"按钮和"重置"按钮
        // "无"按钮：key == nil 且 name == "show_beauty_item_none"
        // "重置"按钮：name == "show_beauty_item_beauty_reset"
        let isResetButton = model.name == "show_beauty_item_beauty_reset".show_localized
    
        setBeautyHandler(value: model.value, isReset: isResetButton, previousSelectedIndex: previousSelectedIndex)
        
        model.isSelected = true
        dataArray[indexPath.item] = model
        collectionView.reloadItems(at: [IndexPath(item: indexPath.item, section: 0)])
        
        if type == .sticker {
            selectedItemClosure?(0, true, false, nil)
            return
        }
        selectedItemClosure?(model.value, model.path == nil, type == .background && model.value > 0, model.key)
    }
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        switch type {
        case .beauty:
            let model = dataArray[indexPath.item]
            let nsString = (model.name ?? "") as NSString
            let attributes = [NSAttributedString.Key.font: UIFont.show_R_11]
            let size = nsString.size(withAttributes: attributes as [NSAttributedString.Key : Any])
            let w = size.width < 52 ? 52 : size.width
            return CGSize(width: w, height: 70)

        default:
            return CGSize(width: 52, height: 70)
        }
    }
}

extension ShowBeautyFaceVC : JXCategoryListContentViewDelegate {
    
    func listView() -> UIView! {
        return view
    }
}
