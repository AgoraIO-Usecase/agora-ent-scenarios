//
//  JoyBannerView.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/12/15.
//

import UIKit
import SDWebImage

private let kBannerEdge = 20.0

@objcMembers
open class JoyBannerArray: NSObject {
    public private(set) var bannerList: [CloudBannerInfo] = []
    
    required public convenience init(bannerList: [CloudBannerInfo]?) {
        self.init()
        self.bannerList = bannerList ?? []
    }
    
    open subscript(_ index: Int) -> CloudBannerInfo? {
        let idx = realIndex(index: index)
        if idx < bannerList.count && idx >= 0 {
            return bannerList[index]
        } else {
            return nil
        }
    }
    
    var count: Int {
        guard bannerList.count > 1 else {
            return bannerList.count
        }
        return bannerList.count * 3
    }
    
    var realCount: Int {
        return bannerList.count
    }
    
    func fakeIndex(index: Int) -> Int {
        guard bannerList.count > 1 else {
            return index
        }
        return bannerList.count + index
    }
    
    func realIndex(index: Int) -> Int {
        guard bannerList.count > 1 else {
            return index
        }
        return index % bannerList.count
    }
}

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
    private var timer: Timer?
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
    
    var bannerList: JoyBannerArray = JoyBannerArray(bannerList: []) {
        didSet {
            listView.reloadData()
            scrollToCenter()
            startTimer()
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

extension JoyBannerView {
    private func scrollToCenter() {
        var index = listView.indexPathsForVisibleItems.first?.row ?? 0
        index = index % bannerList.realCount
        let offset: CGFloat = (self.listView.width - kBannerEdge) * CGFloat(bannerList.fakeIndex(index: index))
        let newContentOffset = targetContentOffsetForProposedContentOffset(CGPoint(x: offset, y: 0), withScrollingVelocity: .zero)
        listView.setContentOffset(newContentOffset, animated: false)
    }
    
    private func scrollToNext() {
        var index = listView.indexPathsForVisibleItems.first?.row ?? 0
        if index == 0 || index == bannerList.count - 1 {
            scrollToCenter()
            index = index % bannerList.realCount
        }
        index += 1
        let offset: CGFloat = (self.listView.width - kBannerEdge) * CGFloat(index)
        let newContentOffset = targetContentOffsetForProposedContentOffset(CGPoint(x: offset, y: 0), withScrollingVelocity: .zero)
        listView.setContentOffset(newContentOffset, animated: true)
    }
    
    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    private func startTimer() {
        stopTimer()
        timer = Timer.scheduledTimer(withTimeInterval: 3, repeats: false, block: {[weak self] timer in
            self?.scrollToNext()
        })
    }
}

extension JoyBannerView: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return bannerList.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(JoyBannerViewCell.self), for: indexPath) as! JoyBannerViewCell
        let banner = bannerList[bannerList.realIndex(index: indexPath.item)]
        cell.bannerView.sd_setImage(with: URL(string: banner?.url ?? ""))
        return cell
    }
    
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        stopTimer()
    }
    
    func scrollViewWillEndDragging(_ scrollView: UIScrollView, withVelocity velocity: CGPoint, targetContentOffset: UnsafeMutablePointer<CGPoint>) {
        let proposedContentOffset = targetContentOffset.pointee
        
        // 在这里手动调用 targetContentOffsetForProposedContentOffset 方法
        let newContentOffset = targetContentOffsetForProposedContentOffset(scrollView.contentOffset, withScrollingVelocity: velocity)
        targetContentOffset.pointee = newContentOffset
        
        startTimer()
    }
    
    open func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        guard let collectionView = scrollView as? UICollectionView else {return}
        startTimer()
        if scrollView.contentOffset.x > -1,
           scrollView.contentOffset.x + scrollView.width + 1 < scrollView.contentSize.width {
            return
        }
        
        scrollToCenter()
    }
    
    func scrollViewDidEndScrollingAnimation(_ scrollView: UIScrollView) {
        startTimer()
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
