//
//  ShowBeautyFaceVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView

class ShowBeautyFaceVC: UIViewController {
    
    var selectedItemClosure: ((_ value: CGFloat, _ isHiddenSldier: Bool, _ isShowSegSwitch: Bool) -> Void)?
    
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
    static var backgroundData = BeautyModel.createBackgroundData()
     
    private lazy var dataArray: [BeautyModel] = {
        var tempArray: [BeautyModel] = []
        switch type {
        case .beauty: tempArray = ShowBeautyFaceVC.beautyData
        case .style: tempArray = ShowBeautyFaceVC.styleData
        case .adjust: tempArray = ShowBeautyFaceVC.adjustData
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
        guard value > 0 else { return }
        setBeautyHandler(value: value, isReset: false)
    }
    
    func reloadData() {
        collectionView.reloadData()
    }
    
    static func resetData(){
        beautyData = BeautyModel.createBeautyData()
        styleData = BeautyModel.createStyleData()
        adjustData = BeautyModel.createAdjustData()
        filterData = BeautyModel.createFilterData()
        stickerData = BeautyModel.createStickerData()
    }
    
    private func setBeautyHandler(value: CGFloat, isReset: Bool) {
        guard !dataArray.isEmpty else { return }
        let model = dataArray[defalutSelectIndex]
        model.value = value
        switch type {
        case .beauty, .adjust:
            if isReset {
                BeautyManager.shareManager.reset(datas: dataArray, type: type)
                return
            }
            BeautyManager.shareManager.setBeauty(path: model.path,
                                                     key: model.key,
                                                     value: model.value)
                        
        case .style:
            if isReset {
                BeautyManager.shareManager.resetStyle(datas: dataArray)
                return
            }
            BeautyManager.shareManager.setStyle(path: model.path,
                                                    key: model.key,
                                                    value: model.value)
            
        case .sticker:
            BeautyManager.shareManager.setSticker(path: model.path)
            
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
            selectedItemClosure?(model.value, model.key == nil, type == .background && indexPath.item > 0)
            defalutSelectIndex = indexPath.item
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let preModel = dataArray[defalutSelectIndex]
        preModel.isSelected = false
        dataArray[defalutSelectIndex] = preModel
        collectionView.reloadItems(at: [IndexPath(item: defalutSelectIndex, section: 0)])
        
        defalutSelectIndex = indexPath.item
        let model = dataArray[indexPath.item]
        setBeautyHandler(value: model.value, isReset: model.key == nil)
        model.isSelected = true
        dataArray[indexPath.item] = model
        collectionView.reloadItems(at: [IndexPath(item: indexPath.item, section: 0)])
        
        if type == .sticker {
            selectedItemClosure?(0, true, false)
            return
        }
        selectedItemClosure?(model.value, model.path == nil, type == .background && model.value > 0)
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
