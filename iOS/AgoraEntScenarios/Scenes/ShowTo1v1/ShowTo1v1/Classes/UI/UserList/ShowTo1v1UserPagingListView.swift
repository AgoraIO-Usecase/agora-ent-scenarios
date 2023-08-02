//
//  ShowTo1v1UserPagingListView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation
import CallAPI

class CollectionViewDelegateProxy: CallApiProxy, UICollectionViewDelegate {}

class ShowTo1v1UserPagingListView: UIView {
    var callClosure: ((ShowTo1v1RoomInfo?)->())?
    var roomList: [ShowTo1v1RoomInfo] = [] {
        didSet {
            self.isHidden = roomList.count == 0 ? true : false
            reloadData()
        }
    }
    
    var delegate: UICollectionViewDelegate? {
        didSet {
            if let delegate = oldValue {
                proxy.removeListener(delegate)
            }
            
            guard let delegate = delegate else {return}
            proxy.addListener(delegate)
        }
    }
    
    private lazy var proxy: CollectionViewDelegateProxy = {
        let proxy = CollectionViewDelegateProxy()
        proxy.addListener(self)
        return proxy
    }()
    
    private lazy var collectionView: UICollectionView = {
        // 列表
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .zero
        layout.itemSize = self.aui_size
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.register(ShowTo1v1RoomCell.self, forCellWithReuseIdentifier: NSStringFromClass(ShowTo1v1RoomCell.self))
        collectionView.delegate = proxy
        collectionView.dataSource = self
        collectionView.isPagingEnabled = true
        collectionView.showsVerticalScrollIndicator = false
        collectionView.contentInsetAdjustmentBehavior = .never
        addSubview(collectionView)
        
        return collectionView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubview()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubview() {
        addSubview(collectionView)
    }
    
    func reloadData() {
        collectionView.reloadData()
    }
}

private let kPageCacheHalfCount = 5000
extension ShowTo1v1UserPagingListView {
    fileprivate func fakeCellCount() -> Int {
        guard roomList.count > 1 else {
            return roomList.count
        }
        return roomList.count + kPageCacheHalfCount * 2
    }
    
    fileprivate func realCellIndex(with fakeIndex: Int) -> Int {
        if fakeCellCount() < 3 {
            return fakeIndex
        }
        
        let realCount = roomList.count
        let offset = kPageCacheHalfCount
        var realIndex = fakeIndex + realCount * max(1 + offset / realCount, 2) - offset
        realIndex = realIndex % realCount
        
        return realIndex
    }
    
    fileprivate func fakeCellIndex(with realIndex: Int) -> Int {
        if fakeCellCount() < 3 {
            return realIndex
        }
        
        let offset = kPageCacheHalfCount
        let fakeIndex = realIndex + offset
        
        return fakeIndex
    }
    
    private func scroll(to index: Int) {
        collectionView.scrollToItem(at: IndexPath(row: index, section: 0), at: .centeredVertically, animated: false)
    }
}

extension ShowTo1v1UserPagingListView: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return fakeCellCount()
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(ShowTo1v1RoomCell.self), for: indexPath) as! ShowTo1v1RoomCell
        
        let roomInfo = roomList[realCellIndex(with: indexPath.row)]
        cell.roomInfo = roomInfo
        cell.callClosure = { [weak self] room in
            self?.callClosure?(room)
        }
        showTo1v1Print("load user: \(roomInfo.userName) \(realCellIndex(with: indexPath.row)) / \(indexPath.row)")
        return cell
    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let currentIndex = Int(scrollView.contentOffset.y / scrollView.height)
        if currentIndex > 0, currentIndex < fakeCellCount() - 1 {return}
        let realIndex = realCellIndex(with: currentIndex)
        let toIndex = fakeCellIndex(with: realIndex)
        showTo1v1Print("collectionView scrollViewDidEndDecelerating: from: \(currentIndex) to: \(toIndex) real: \(realIndex)")

        scroll(to: toIndex)
    }
 }
