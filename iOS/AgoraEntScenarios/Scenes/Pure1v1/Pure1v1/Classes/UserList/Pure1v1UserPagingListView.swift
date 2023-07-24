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
    
    private lazy var collectionView: UICollectionView = {
        // 列表
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = self.aui_size
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
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


extension Pure1v1UserPagingListView: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
   
   func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
       return userList.count
   }
   
   func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
       let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(Pure1v1UserCell.self), for: indexPath) as! Pure1v1UserCell
       let user = userList[indexPath.item]
       cell.userInfo = user
       cell.callClosure = { [weak self] user in
           self?.callClosure?(user)
       }
       return cell
   }
   
   func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
   }
   
}
