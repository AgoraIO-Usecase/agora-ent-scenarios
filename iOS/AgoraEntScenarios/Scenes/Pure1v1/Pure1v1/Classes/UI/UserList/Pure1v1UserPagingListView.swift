//
//  Pure1v1UserPagingListView.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation

class Pure1v1UserPagingListView: UIView {
    var callClosure: ((Pure1v1UserInfo?)->())?
    var userList: [Pure1v1UserInfo] = [] {
        didSet {
            self.isHidden = userList.count == 0 ? true : false
            reloadData()
        }
    }
    
    var localUserInfo: Pure1v1UserInfo?
    
    private lazy var collectionView: UICollectionView = {
        // 列表
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .zero
        layout.itemSize = self.aui_size
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
        collectionView.scrollsToTop = false
        collectionView.backgroundColor = .clear
        collectionView.register(Pure1v1UserCell.self, forCellWithReuseIdentifier: NSStringFromClass(Pure1v1UserCell.self))
        collectionView.delegate = self
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
extension Pure1v1UserPagingListView {
    fileprivate func fakeCellCount() -> Int {
        guard userList.count > 1 else {
            return userList.count
        }
        return userList.count + kPageCacheHalfCount * 2
    }
    
    fileprivate func realCellIndex(with fakeIndex: Int) -> Int {
        if fakeCellCount() < 3 {
            return fakeIndex
        }
        
        let realCount = userList.count
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

extension Pure1v1UserPagingListView: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return fakeCellCount()
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(Pure1v1UserCell.self), for: indexPath) as! Pure1v1UserCell
        
        let user = userList[realCellIndex(with: indexPath.row)]
        cell.userInfo = user
        cell.localUserInfo = localUserInfo
        cell.callClosure = { [weak self] user in
            self?.callClosure?(user)
        }
//        pure1v1Print("load user: \(user.userName) \(realCellIndex(with: indexPath.row)) / \(indexPath.row)")
        return cell
    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let currentIndex = Int(scrollView.contentOffset.y / scrollView.height)
        if currentIndex > 0, currentIndex < fakeCellCount() - 1 {return}
        let realIndex = realCellIndex(with: currentIndex)
        let toIndex = fakeCellIndex(with: realIndex)
        pure1v1Print("collectionView scrollViewDidEndDecelerating: from: \(currentIndex) to: \(toIndex) real: \(realIndex)")

        scroll(to: toIndex)
    }
 }
