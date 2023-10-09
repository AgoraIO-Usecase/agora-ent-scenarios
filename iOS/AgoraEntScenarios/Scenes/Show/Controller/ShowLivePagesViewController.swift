//
//  ShowLivePagesViewController.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/1/13.
//

import Foundation
import UIKit

private let kPagesVCTag = "PagesVC"
class ShowLivePagesViewController: ViewController {
    
    var roomList: [ShowRoomListModel]?
    
    var focusIndex: Int = 0
    
    private var currentVC: ShowLiveViewController?
    
    let agoraKitManager = ShowAgoraKitManager.shared
        
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .zero
        layout.itemSize = self.view.bounds.size
        let collectionView = UICollectionView(frame: self.view.bounds, collectionViewLayout: layout)
        collectionView.register(UICollectionViewCell.self, forCellWithReuseIdentifier: NSStringFromClass(UICollectionViewCell.self))
        collectionView.scrollsToTop = false
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.isPagingEnabled = true
        collectionView.contentInsetAdjustmentBehavior = .never
        collectionView.bounces = false
        collectionView.showsVerticalScrollIndicator = false
        return collectionView
    }()
    
    deinit {
        showLogger.info("deinit-- ShowLivePagesViewController", context: kPagesVCTag)
        ShowAgoraKitManager.shared.leaveAllRoom()
        AppContext.unloadShowServiceImp()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.setNavigationBarHidden(true, animated: true)
        self.view.addSubview(collectionView)
        collectionView.isScrollEnabled = roomList?.count ?? 0 > 1 ? true : false
        scroll(to: fakeCellIndex(with: focusIndex))
        preloadEnterRoom()
    }
    
    private func addDebugButton(){
        let button = UIButton(type: .custom)
        button.setTitle("Video", for: .normal)
        button.addTarget(self, action: #selector(didClickDebugVideoButton), for: .touchUpInside)
        view.addSubview(button)
        button.snp.makeConstraints { make in
            make.left.top.equalTo(100)
            make.width.height.equalTo(60)
        }
        
        let Audiobutton = UIButton(type: .custom)
        Audiobutton.setTitle("Audio", for: .normal)
        Audiobutton.addTarget(self, action: #selector(didClickDebugAudioButton), for: .touchUpInside)
        view.addSubview(Audiobutton)
        Audiobutton.snp.makeConstraints { make in
            make.centerY.equalTo(button)
            make.left.equalTo(button.snp.right).offset(20)
            make.width.height.equalTo(60)
        }
        
        let superbutton = UIButton(type: .custom)
        superbutton.setTitle("超分", for: .normal)
        superbutton.addTarget(self, action: #selector(didClickDebugSuperButton), for: .touchUpInside)
        view.addSubview(superbutton)
        superbutton.snp.makeConstraints { make in
            make.centerY.equalTo(button)
            make.left.equalTo(Audiobutton.snp.right).offset(20)
            make.width.height.equalTo(60)
        }
    }
    
    @objc private func didClickDebugVideoButton(){
        agoraKitManager.setOffMediaOptionsVideo(roomid: currentVC?.room?.roomId ?? "")
    }
    
    @objc private func didClickDebugAudioButton(){
        agoraKitManager.setOffMediaOptionsAudio()
    }
    
    @objc private func didClickDebugSuperButton(){
        agoraKitManager.setSuperResolutionOn(false)
    }
}


private let kPageCacheHalfCount = 999999
//MARK: private
extension ShowLivePagesViewController {
    fileprivate func preloadEnterRoom() {
        guard let roomList = roomList, roomList.count > 2 else {return}
        let prevIdx = (focusIndex + roomList.count - 1) % roomList.count
        let nextIdx = (focusIndex + 1) % roomList.count
        let preloadIdxs = [prevIdx, nextIdx]
        showLogger.info("preloadEnterRoom: \(prevIdx) and \(nextIdx)", context: kPagesVCTag)
        preloadIdxs.forEach { idx in
            let room = roomList[idx]
            let roomId = room.roomId
            if roomId.isEmpty {return}
            ShowAgoraKitManager.shared.updateLoadingType(roomId: roomId, channelId: roomId, playState: .prejoined)
        }
    }
    
    fileprivate func fakeCellCount() -> Int {
        guard let count = roomList?.count else {
            return 0
        }
        return count > 2 ? count + kPageCacheHalfCount * 2 : count
    }
    
    fileprivate func realCellIndex(with fakeIndex: Int) -> Int {
        if fakeCellCount() < 3 {
            return fakeIndex
        }
        
        guard let realCount = roomList?.count else {
            showLogger.error("realCellIndex roomList?.count == nil", context: kPagesVCTag)
            return 0
        }
        let offset = kPageCacheHalfCount
        var realIndex = fakeIndex + realCount * max(1 + offset / realCount, 2) - offset
        realIndex = realIndex % realCount
        
        return realIndex
    }
    
    fileprivate func fakeCellIndex(with realIndex: Int) -> Int {
        if fakeCellCount() < 3 {
            return realIndex
        }
        
        guard let _ = roomList?.count else {
            showLogger.error("fakeCellIndex roomList?.count == nil", context: kPagesVCTag)
            return 0
        }
        let offset = kPageCacheHalfCount
        let fakeIndex = realIndex + offset
        
        return fakeIndex
    }
    
    private func scroll(to index: Int) {
        collectionView.scrollToItem(at: IndexPath(row: index, section: 0), at: .centeredVertically, animated: false)
    }
}

let kShowLiveRoomViewTag = 12345
//MARK: UICollectionViewDelegate & UICollectionViewDataSource
extension ShowLivePagesViewController: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: UICollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(UICollectionViewCell.self),
                                                                            for: indexPath)
        let idx = realCellIndex(with: indexPath.row)
        defer {
            showLogger.info("collectionView cellForItemAt: \(idx)/\(indexPath.row)  cache vc count: \(self.children.count)", context: kPagesVCTag)
        }
        
        guard let room = self.roomList?[idx]  else {
            return cell
        }
        
        if let origVC = cell.contentView.viewWithTag(kShowLiveRoomViewTag)?.next as? ShowLiveViewController {
            origVC.room = room
            return cell
        }
        
        let vc = ShowLiveViewController()
        vc.room = room
        vc.delegate = self
        vc.view.frame = self.view.bounds
        vc.view.tag = kShowLiveRoomViewTag
        cell.contentView.addSubview(vc.view)
        self.addChild(vc)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return fakeCellCount()
    }
    
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        let idx = realCellIndex(with: indexPath.row)
        guard let room = self.roomList?[idx], let vc = cell.contentView.viewWithTag(kShowLiveRoomViewTag)?.next as? ShowLiveViewController else {
//            assert(false, "room at index \(idx) not found")
            return
        }
        ShowRobotService.shared.startCloudPlayers()
        showLogger.info("willDisplay[\(room.roomId)]: \(idx)/\(indexPath.row)  cache vc count: \(self.children.count)", context: kPagesVCTag)
        vc.updateLoadingType(playState: .joined, roomId: room.roomId)
        currentVC = vc
        self.view.endEditing(true)
    }
    
    func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        let idx = realCellIndex(with: indexPath.row)
        if let visibleCellIndex = _getVisibleCellTuple().0, idx == realCellIndex(with: visibleCellIndex) {
            showLogger.info("didEndDisplaying break: \(idx)/\(indexPath.row)", context: kPagesVCTag)
            return
        }
        guard let room = self.roomList?[idx], let vc = cell.contentView.viewWithTag(kShowLiveRoomViewTag)?.next as? ShowLiveViewController else {
//            assert(false, "room at index \(idx) not found")
            return
        }
        showLogger.info("didEndDisplaying[\(room.roomId)]: \(idx)/\(indexPath.row)  cache vc count: \(self.children.count)", context: kPagesVCTag)
        vc.updateLoadingType(playState: .prejoined, roomId: room.roomId)
        self.view.endEditing(true)
    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let currentIndex = Int(scrollView.contentOffset.y / scrollView.height)
        cleanIdleRoom()
        if currentIndex > 0, currentIndex < fakeCellCount() - 1 {return}
        let realIndex = realCellIndex(with: currentIndex)
        let toIndex = fakeCellIndex(with: realIndex)
        showLogger.info("scrollViewDidEndDecelerating: from: \(currentIndex) to: \(toIndex) real: \(realIndex)", context: kPagesVCTag)
        
        scroll(to: toIndex)
    }
    
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        ShowAgoraKitManager.shared.callTimestampStart()
    }
    
    private func _getVisibleCellTuple() -> (Int?, UICollectionViewCell?) {
        for (i, cell) in collectionView.visibleCells.enumerated() {
            if cell.convert(cell.bounds.origin, from: self.view) == .zero {
                return (collectionView.indexPathsForVisibleItems[i].row, cell)
            }
        }
        
        return (nil, nil)
    }
    
    private func cleanIdleRoom() {
        let tuple = _getVisibleCellTuple()
        let visibleCell: UICollectionViewCell? = tuple.1
        let visibleIndex: Int? = tuple.0
        
        guard let visibleCellIndex = visibleIndex, let roomList = roomList else {return}
        let visibleIndexs = [visibleCellIndex + roomList.count - 1, visibleCellIndex, visibleCellIndex + 1]
        var visibleRoomIds: [String] = []
        visibleIndexs.forEach { index in
            let realIndex = self.realCellIndex(with: index)
            let room = self.roomList?[realIndex]
            visibleRoomIds.append(room?.roomId ?? "")
        }
        
        showLogger.info("cleanIdleRoom without \(visibleRoomIds))", context: kPagesVCTag)
        ShowAgoraKitManager.shared.cleanChannel(without: visibleRoomIds)
        
        //refresh visibleCell canvas after scroll to prevent adjacent rooms of pk from causing no display of images
        showLogger.info("updateRemoteCavans: \(currentVC?.room?.roomId ?? "")", context: kPagesVCTag)
        let currentVC = visibleCell?.contentView.viewWithTag(kShowLiveRoomViewTag)?.next as? ShowLiveViewController
        currentVC?.updateRemoteCavans()
    }
}

extension ShowLivePagesViewController {
    var isScrollEnable: Bool {
        set{
            collectionView.isScrollEnabled = newValue
        }
        get{
            return collectionView.isScrollEnabled
        }
    }
}

extension ShowLivePagesViewController: ShowLiveViewControllerDelegate {
    func currentUserIsOnSeat() {
        isScrollEnable = false
    }
    
    func currentUserIsOffSeat() {
        isScrollEnable = true
    }
}
