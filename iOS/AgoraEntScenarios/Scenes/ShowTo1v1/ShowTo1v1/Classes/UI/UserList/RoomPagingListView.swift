//
//  RoomPagingListView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation
import CallAPI
import VideoLoaderAPI

class CollectionViewDelegateProxy: CallApiProxy, UICollectionViewDelegate {}

/*
class ShowCycleRoomArray: AGRoomArray {
    private var halfCount: Int = 9999999
    fileprivate func fakeCellCount() -> Int {
        return roomList.count > 1 ? roomList.count + halfCount * 2 : roomList.count
    }
    
    required init(roomList: [IVideoLoaderRoomInfo]?) {
        super.init(roomList: roomList)
        let count = max(roomList?.count ?? 0, 1)
        halfCount = (9999999 / count) * count
    }

    fileprivate func realCellIndex(with fakeIndex: Int) -> Int {
        if fakeCellCount() < 2 {
            return fakeIndex
        }

        let realCount = roomList.count
        let offset = halfCount
        var realIndex = fakeIndex + realCount * max(1 + offset / realCount, 2) - offset
        realIndex = realIndex % realCount

        return realIndex
    }

    fileprivate func fakeCellIndex(with realIndex: Int) -> Int {
        if fakeCellCount() < 2 {
            return realIndex
        }

        let offset = halfCount
        let fakeIndex = realIndex + offset

        return fakeIndex
    }
    
    override subscript(index: Int) -> IVideoLoaderRoomInfo? {
        let realIndex = realCellIndex(with: index)
        if realIndex < roomList.count && realIndex >= 0 {
            return roomList[realIndex]
        } else {
            return nil
        }
    }
    
    override func count() -> Int {
        return fakeCellCount()
    }
}
 */

class RoomPagingListView: UIView {
    var isLoop = false
    var refreshBeginClousure: (()->())?
    private lazy var refreshControl: UIRefreshControl = {
        let ctrl = UIRefreshControl()
        ctrl.addTarget(self, action: #selector(refreshControlValueChanged(_ :)), for: .valueChanged)
        return ctrl
    }()
    
    
    var callClosure: ((ShowTo1v1RoomInfo?)->())?
    var tapClosure: ((ShowTo1v1RoomInfo?)->())?
    var roomList: [ShowTo1v1RoomInfo] = [] {
        didSet {
            delegateHandler.roomList = AGRoomArray(roomList: roomList)
            reloadData()
        }
    }
    private var localUserInfo: ShowTo1v1UserInfo!
    
    private lazy var delegateHandler: AGCollectionSlicingDelegateHandler = {
        let handler = AGCollectionSlicingDelegateHandler(localUid: self.localUserInfo.getUIntUserId() ?? 0, needPrejoin: true)
        handler.videoSlicingType = .visible
        handler.audioSlicingType = .never
        handler.onRequireRenderVideo = { [weak self] (info, cell, indexPath) in
            guard let cell = cell as? RoomListCell else { return nil }
            return cell.canvasView
        }
        return handler
    }()
    
    lazy var collectionView: UICollectionView = {
        // 列表
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .zero
        layout.itemSize = self.aui_size
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
        collectionView.scrollsToTop = false
        collectionView.backgroundColor = .clear
        collectionView.register(RoomListCell.self, forCellWithReuseIdentifier: NSStringFromClass(RoomListCell.self))
        collectionView.delegate = delegateHandler
        collectionView.dataSource = self
        collectionView.isPagingEnabled = true
        collectionView.showsVerticalScrollIndicator = false
        collectionView.contentInsetAdjustmentBehavior = .never
        collectionView.refreshControl = refreshControl
        collectionView.contentInset = UIEdgeInsets(top: 30, left: 0, bottom: 0, right: 0)
        addSubview(collectionView)
        
        return collectionView
    }()
    
    required init(frame: CGRect, localUserInfo: ShowTo1v1UserInfo) {
        super.init(frame: frame)
        self.localUserInfo = localUserInfo
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
    
    func reloadCurrentItem() {
        guard let cell = collectionView.visibleCells.first,
              let indexPath = collectionView.indexPath(for: cell) else {
            return
        }
        showTo1v1Print("reloadCurrentItem: \(indexPath.row)")
        collectionView.delegate?.collectionView?(collectionView, willDisplay: cell, forItemAt: indexPath)
    }
    
    @objc private func refreshControlValueChanged(_ refrshControl: UIRefreshControl) {
        self.refreshBeginClousure?()
    }
    
    func endRefreshing(){
        refreshControl.endRefreshing()
    }
}

extension RoomPagingListView: UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return delegateHandler.roomList?.count() ?? 0
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(RoomListCell.self), for: indexPath) as! RoomListCell
        
        let roomInfo = delegateHandler.roomList?[indexPath.row] as? ShowTo1v1RoomInfo
        cell.roomInfo = roomInfo
        cell.localUserInfo = localUserInfo
        cell.callClosure = { [weak self] room in
            self?.callClosure?(room)
        }
        cell.tapClosure = { [weak self] room in
            self?.tapClosure?(room)
        }
        showTo1v1Print("load user: \(roomInfo?.userName ?? "") \(indexPath.row)")
        return cell
    }
 }


/*
class ShowLivePagesSlicingDelegateHandler: AGCollectionSlicingDelegateHandler {
    private func scroll(to index: Int) {
        guard let collectionView = scrollView as? UICollectionView else {return}
        collectionView.scrollToItem(at: IndexPath(row: index, section: 0), at: .centeredVertically, animated: false)
    }
    
    override func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        super.scrollViewDidEndDecelerating(scrollView)
        let currentIndex = Int(scrollView.contentOffset.y / scrollView.height)
        if currentIndex > 0, currentIndex < (roomList?.count() ?? 0) - 1 {return}
        let toIndex = currentIndex
        if let cycleArray = roomList as? ShowCycleRoomArray {
            let realIndex = cycleArray.realCellIndex(with: toIndex)
            let fakeIndex = cycleArray.fakeCellIndex(with: realIndex)
            showTo1v1Print("collectionView scrollViewDidEndDecelerating: from: \(currentIndex) to: \(toIndex) real: \(realIndex)")

            scroll(to: toIndex)
        }
    }
}
 */

