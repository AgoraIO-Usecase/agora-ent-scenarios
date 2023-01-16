//
//  ShowLivePagesViewController.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/1/13.
//

import Foundation
import UIKit

class ShowLivePagesViewController: ViewController {
    var roomList: [ShowRoomListModel]?
    // 观众端预设类型
    var audiencePresetType: ShowPresetType?
    var selectedResolution = 1
    
    var focusIndex: Int = 0
    
    lazy var agoraKitManager: ShowAgoraKitManager = {
        let manager = ShowAgoraKitManager()
        manager.defaultSetting()
        return manager
    }()
    
    
    fileprivate var roomVCMap: [Int: ShowLiveViewController] = [Int: ShowLiveViewController]()
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .zero
        layout.itemSize = self.view.bounds.size
        let collectionView = UICollectionView(frame: self.view.bounds, collectionViewLayout: layout)
        collectionView.register(UICollectionViewCell.self, forCellWithReuseIdentifier: NSStringFromClass(UICollectionViewCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.isPagingEnabled = true
        collectionView.contentInsetAdjustmentBehavior = .never
        return collectionView
    }()
    
    deinit {
        print("deinit-- ShowLivePagesViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.setNavigationBarHidden(true, animated: true)
        self.view.addSubview(collectionView)
        collectionView.isScrollEnabled = roomList?.count ?? 0 > 1 ? true : false
        collectionView.scrollToItem(at: IndexPath(row: focusIndex, section: 0), at: .centeredVertically, animated: false)
    }
}


//MARK: live vc cache
extension ShowLivePagesViewController {
    
}

//MARK: UICollectionViewDelegate & UICollectionViewDataSource
extension ShowLivePagesViewController: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: UICollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(UICollectionViewCell.self),
                                                                          for: indexPath)
        print("collectionView... cellForItemAt: \(indexPath.row)")
        if let room = self.roomList?[indexPath.row] {
            if let vc = self.roomVCMap[indexPath.row] {
                vc.view.frame = self.view.bounds
                vc.view.removeFromSuperview()
                cell.contentView.addSubview(vc.view)
                self.addChild(vc)
                return cell
            }
            let vc = ShowLiveViewController(agoraKitManager: self.agoraKitManager)
            vc.audiencePresetType = self.audiencePresetType
            vc.selectedResolution = self.selectedResolution
            vc.room = room
            vc.view.frame = self.view.bounds
            cell.contentView.addSubview(vc.view)
            self.addChild(vc)
            self.roomVCMap[indexPath.row] = vc
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.roomList?.count ?? 0
    }
    
    
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        print("collectionView... willDisplay: \(indexPath.row)")
        guard let vc = self.roomVCMap[indexPath.row] else {
//            assert(false, "room at index \(indexPath.row) not found")
            return
        }
        vc.loadingType = .loading
//        self.roomVCMap.forEach { (key: Int, value: ShowLiveViewController) in
//            if value == vc {
//                value.loadingType = .loading
//                return
//            }
//            value.loadingType = .preload
//        }
    }
    
    func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        print("collectionView... didEndDisplaying: \(indexPath.row)")
        guard let vc = self.roomVCMap[indexPath.row] else {
//            assert(false, "room at index \(indexPath.row) not found")
            return
        }
        vc.loadingType = .preload
    }
}
