//
//  AGCollectionLoadingDelegateHandler.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/9/1.
//

import Foundation

//秒开CollectionView delegate handler
open class AGCollectionLoadingDelegateHandler: AGBaseDelegateHandler {
    private weak var scrollView: UIScrollView? {
        didSet {
            guard let scrollView = scrollView, oldValue == nil else {return}
            preLoadVisibleItems(scrollView: scrollView)
        }
    }
    public override var roomList: AGRoomArray? {
        didSet {
            scrollView = nil
        }
    }
}

extension AGCollectionLoadingDelegateHandler {
    public func preLoadVisibleItems(scrollView: UIScrollView) {
        guard let roomList = roomList, roomList.count() > 0, let collectionView = scrollView as? UICollectionView else {
            return
        }
        let firstIdx = collectionView.indexPathsForVisibleItems.first?.row ?? 0
        let count = UInt(collectionView.indexPathsForVisibleItems.count)
        preLoadVisibleItems(index: firstIdx, count: count)
    }
}

extension AGCollectionLoadingDelegateHandler: UICollectionViewDelegate {
    public func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        self.scrollView = collectionView
    }
    
    public func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
        if decelerate {return}
        preLoadVisibleItems(scrollView: scrollView)
    }
    
    open func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        preLoadVisibleItems(scrollView: scrollView)
    }
}
