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
        
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .vertical
        let middleMargin: CGFloat = 10
        let itemW = (bounds.width - 30) / 2.0
        let itemH = itemW
        flowLayout.itemSize = CGSize(width: itemW, height: itemH)
        flowLayout.minimumInteritemSpacing = middleMargin
        flowLayout.minimumLineSpacing = middleMargin // 修改为 middleMargin，使得两个项目显示在一行中
        
        collectionView = UICollectionView(frame: CGRect(x: 0, y: 20, width: bounds.width, height: bounds.height - 20), collectionViewLayout: flowLayout)
        collectionView.contentInset = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 10)
        collectionView.backgroundColor = .clear
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
    
//    open override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
//        let extendedBounds = bounds.inset(by: extendedTouchArea)
//        if extendedBounds.contains(point) {
//            return self
//        }
//        return super.hitTest(point, with: event)
//    }
}
