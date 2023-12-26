//
//  VRRoomHomeListView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/23.
//

import UIKit

class SRRoomHomeListView: UIView {
    var clickBlock:((Int)->Void)?
    var refreshBlock:((Bool)->Void)?
    var collectionView: UICollectionView!
    let refreshControl = UIRefreshControl()
    var roomList: [SARoomEntity]? {
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
        
        collectionView = UICollectionView(frame: CGRect(x: 0, y: 0, width: bounds.width, height: bounds.height), collectionViewLayout: flowLayout)
        collectionView.contentInset = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 10)
        collectionView.backgroundColor = .clear
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(SRRoomHomeListCell.self, forCellWithReuseIdentifier: "home")
        self.addSubview(collectionView)
        collectionView.refreshControl = refreshControl
        refreshControl.addTarget(self, action: #selector(refreshData), for: .valueChanged)
    }
    
    @objc func refreshData() {
        guard let block = refreshBlock else {return}
        block(true)
        refreshControl.beginRefreshing()
    }
    
}

extension SRRoomHomeListView: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return roomList?.count ?? 0
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: SRRoomHomeListCell  = collectionView.dequeueReusableCell(withReuseIdentifier: "home", for: indexPath) as! SRRoomHomeListCell
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
