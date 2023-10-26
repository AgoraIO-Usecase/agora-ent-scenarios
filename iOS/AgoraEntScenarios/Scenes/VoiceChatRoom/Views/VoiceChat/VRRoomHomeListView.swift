//
//  VRRoomHomeListView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/23.
//

import UIKit
import ZSwiftBaseLib
class VRRoomHomeListView: UIView {
    var clickBlock:((Int)->Void)?
    var refreshBlock:((Bool)->Void)?
    var backBlock:(()->Void)?
    var collectionView: UICollectionView!
    let refreshControl = UIRefreshControl()
    var roomList: [VRRoomEntity]? {
        didSet {
            collectionView.reloadData()
        }
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        
        let titleLabel = UILabel(frame: CGRect(x: (bounds.width - 100)/2.0, y: ZStatusBarHeight + 20, width: 100, height: 20))
        titleLabel.text = "语聊房"
        titleLabel.font = UIFont.systemFont(ofSize: 17)
        titleLabel.textAlignment = .center
        self.addSubview(titleLabel)
        
        let backBtn = UIButton(frame: CGRect(x: 20, y: ZStatusBarHeight + 20, width: 12, height: 20))
        backBtn.addTarget(self, action: #selector(back), for: .touchUpInside)
        backBtn.setBackgroundImage( UIImage.sceneImage(name: "vr_back"), for: .normal)
        backBtn.extendedTouchArea = UIEdgeInsets(top: -20, left: -20, bottom: -20, right: -20)
        self.addSubview(backBtn)
        
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .vertical
        let middleMargin: CGFloat = 10
        let itemW = (bounds.width - 30) / 2.0
        let itemH = itemW
        flowLayout.itemSize = CGSize(width: itemW, height: itemH)
        flowLayout.minimumInteritemSpacing = middleMargin
        flowLayout.minimumLineSpacing = middleMargin // 修改为 middleMargin，使得两个项目显示在一行中
        
        collectionView = UICollectionView(frame: CGRect(x: 0, y: ZNavgationHeight + 20, width: bounds.width, height: bounds.height - ZNavgationHeight - 20), collectionViewLayout: flowLayout)
        collectionView.contentInset = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 10)
        collectionView.backgroundColor = .white
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(VRRoomHomeListCell.self, forCellWithReuseIdentifier: "home")
        self.addSubview(collectionView)
        collectionView.refreshControl = refreshControl
        refreshControl.addTarget(self, action: #selector(refreshData), for: .valueChanged)
    }
    
    @objc func refreshData() {
        guard let block = refreshBlock else {return}
        block(true)
        refreshControl.beginRefreshing()
    }
    
    @objc func back() {
        guard let backBlock = backBlock else {return}
        backBlock()
    }
}

extension VRRoomHomeListView: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return roomList?.count ?? 0
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: VRRoomHomeListCell  = collectionView.dequeueReusableCell(withReuseIdentifier: "home", for: indexPath) as! VRRoomHomeListCell
        cell.roomEntity = roomList?[indexPath.item]
        let index = indexPath.row % 5
        cell.bgImgView.image = UIImage.sceneImage(name: "create_bg_\(index)")
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let block = clickBlock else {return}
        block(indexPath.item)
    }
}

extension UIButton {
    private struct AssociatedKeys {
        static var extendedTouchArea = UIEdgeInsets.zero
    }
    
    var extendedTouchArea: UIEdgeInsets {
        get {
            guard let value = objc_getAssociatedObject(self, &AssociatedKeys.extendedTouchArea) as? UIEdgeInsets else {
                return UIEdgeInsets.zero
            }
            return value
        }
        set {
            objc_setAssociatedObject(self, &AssociatedKeys.extendedTouchArea, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        }
    }
    
    open override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let extendedBounds = bounds.inset(by: extendedTouchArea)
        if extendedBounds.contains(point) {
            return self
        }
        return super.hitTest(point, with: event)
    }
}
