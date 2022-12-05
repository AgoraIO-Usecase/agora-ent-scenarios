//
//  ShowBeautyFaceVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView

class ShowBeautyFaceVC: UIViewController {
    
    var selectedItemClosure: ((_ value: CGFloat, _ isHiddenSldier: Bool) -> Void)?
    
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
    static let beautyData = ByteBeautyModel.createBeautyData()
    static let styleData = ByteBeautyModel.createStyleData()
    static let filterData = ByteBeautyModel.createFilterData()
    static let stickerData = ByteBeautyModel.createStickerData()
     
    private lazy var dataArray: [ByteBeautyModel] = {
        switch type {
        case .beauty: return ShowBeautyFaceVC.beautyData
        case .style: return ShowBeautyFaceVC.styleData
        case .filter: return ShowBeautyFaceVC.filterData
        case .sticker: return ShowBeautyFaceVC.stickerData
        }
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
    
    private func setBeautyHandler(value: CGFloat, isReset: Bool) {
        let model = dataArray[defalutSelectIndex]
        model.value = value
        switch type {
        case .beauty:
            if isReset {
                ByteBeautyManager.shareManager.reset(datas: dataArray)
                return
            }
            ByteBeautyManager.shareManager.setBeauty(path: model.path,
                                                     key: model.key,
                                                     value: model.value)
            
        case .filter:
            if isReset {
                ByteBeautyManager.shareManager.resetFilter(datas: dataArray)
                return
            }
            ByteBeautyManager.shareManager.setFilter(path: model.path,
                                                     value: model.value)
            
        case .style:
            if isReset {
                ByteBeautyManager.shareManager.reset(datas: dataArray)
                ByteBeautyManager.shareManager.reset(datas: dataArray,
                                                     key: "Makeup_ALL")
                return
            }
            ByteBeautyManager.shareManager.setStyle(path: model.path,
                                                    key: model.key,
                                                    value: model.value)
            ByteBeautyManager.shareManager.setStyle(path: model.path,
                                                    key: "Makeup_ALL",
                                                    value: model.makupValue)
            
        case .sticker:
            ByteBeautyManager.shareManager.setSticker(path: model.path)
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
            selectedItemClosure?(model.value, model.path == nil)
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
        setBeautyHandler(value: model.value, isReset: model.path == nil)
        model.isSelected = true
        dataArray[indexPath.item] = model
        collectionView.reloadItems(at: [IndexPath(item: indexPath.item, section: 0)])
        
        if type == .sticker {
            selectedItemClosure?(0, true)
            return
        }
        selectedItemClosure?(model.value, model.path == nil)
    }
}

extension ShowBeautyFaceVC : JXCategoryListContentViewDelegate {
    
    func listView() -> UIView! {
        return view
    }
}
