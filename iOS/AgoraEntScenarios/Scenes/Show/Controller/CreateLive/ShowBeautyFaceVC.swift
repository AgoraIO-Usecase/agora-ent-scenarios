//
//  ShowBeautyFaceVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit
import JXCategoryView

class ShowBeautyFaceVC: UIViewController {
    
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
        return collectionView
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configDefaultSelect()
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
        return 10
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: ShowBeautyFaceCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(ShowBeautyFaceCell.self), for: indexPath) as! ShowBeautyFaceCell
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
    }
}

extension ShowBeautyFaceVC : JXCategoryListContentViewDelegate {
    
    func listView() -> UIView! {
        return view
    }
}
