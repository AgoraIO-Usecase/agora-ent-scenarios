//
//  JoyBannerView.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/12/15.
//

import UIKit
import SDWebImage

private let kBannerEdge = 20.0

class JoyBannerViewCell: UICollectionViewCell {
    let bannerView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFit
        view.backgroundColor = .joy_room_info_cover
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        contentView.addSubview(bannerView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bannerView.frame = contentView.bounds
        bannerView.setCornerRadius(16)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class JoyBannerView: UIView {
    private lazy var listView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.sectionInset = UIEdgeInsets(top: 0, left: kBannerEdge, bottom: 0, right: kBannerEdge)
        let itemWidth = self.width - kBannerEdge * 2
        layout.itemSize = CGSize(width: itemWidth, height: self.height)
        layout.minimumLineSpacing = kBannerEdge
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
        collectionView.collectionViewLayout = layout
        collectionView.backgroundColor = .clear
        collectionView.register(JoyBannerViewCell.self, forCellWithReuseIdentifier: NSStringFromClass(JoyBannerViewCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
//        collectionView.isPagingEnabled = true
        collectionView.bounces = false
        collectionView.showsHorizontalScrollIndicator = false
        return collectionView
    }()
    
    var bannerList: [CloudBannerInfo] = [] {
        didSet {
            listView.reloadData()
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(listView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        listView.frame = bounds
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension JoyBannerView: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return bannerList.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(JoyBannerViewCell.self), for: indexPath) as! JoyBannerViewCell
        let banner = bannerList[indexPath.item]
        cell.bannerView.sd_setImage(with: URL(string: banner.url ?? ""))
        return cell
    }
    
    func scrollViewWillEndDragging(_ scrollView: UIScrollView, withVelocity velocity: CGPoint, targetContentOffset: UnsafeMutablePointer<CGPoint>) {
        let proposedContentOffset = targetContentOffset.pointee
        
        // 在这里手动调用 targetContentOffsetForProposedContentOffset 方法
        let newContentOffset = targetContentOffsetForProposedContentOffset(scrollView.contentOffset, withScrollingVelocity: velocity)
        targetContentOffset.pointee = newContentOffset
        
//        scrollView.setContentOffset(newContentOffset, animated: true)
    }
    
    func targetContentOffsetForProposedContentOffset(_ proposedContentOffset: CGPoint, withScrollingVelocity velocity: CGPoint) -> CGPoint {
        var finalOffset = proposedContentOffset
        if let collectionViewFlowLayout = listView.collectionViewLayout as? UICollectionViewFlowLayout {
            let pageWidth = collectionViewFlowLayout.itemSize.width + collectionViewFlowLayout.minimumLineSpacing
            let currentPage = round(proposedContentOffset.x / pageWidth)
            let xOffset = pageWidth * currentPage
            
            finalOffset.x = xOffset
        }
        
        return finalOffset
    }
}
