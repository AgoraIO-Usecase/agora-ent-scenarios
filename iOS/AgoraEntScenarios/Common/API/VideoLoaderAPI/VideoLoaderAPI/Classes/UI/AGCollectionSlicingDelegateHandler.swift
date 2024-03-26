//
//  AGCollectionSlicingDelegateHandler.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/8/30.
//

import Foundation

//触发时机
@objc public enum AGVideoSlicingType: Int {
    case visible = 0   //显示时
    case endDrag       //放手时
    case endScroll     //滑动停止时
}

@objc public enum AGAudioSlicingType: Int {
    case endScroll     //滑动停止时
    case never = 100   //不展示
}

//秒切CollectionView delegate handler
@objcMembers
open class AGCollectionSlicingDelegateHandler: AGBaseDelegateHandler {
    public var videoSlicingType: AGVideoSlicingType = .visible      //视频展示时机
    public var audioSlicingType: AGAudioSlicingType = .endScroll    //声音展示时机
    public var onRequireRenderVideo:((AnchorInfo, UICollectionViewCell, IndexPath)->UIView?)? = nil
    private var needPrejoin: Bool = true    //是否需要秒切加速(上下未显示但是已经初始化的页面是否走默认join的策略)
    private var prejoinCount: Int = 1
    private var needReloadData: Bool = false
    #if DEBUG
    public private(set) var cellVisibleDate: [String: Date] = [:]
    #endif
    
    required public convenience init(localUid: UInt, needPrejoin: Bool) {
        self.init(localUid: localUid)
        self.needPrejoin = needPrejoin
    }
    
    required public init(localUid: UInt) {
        super.init(localUid: localUid)
    }
    
    public weak var scrollView: UIScrollView? {
        didSet {
            guard oldValue == nil, let newValue = scrollView, newValue.isDragging == false, newValue.isDecelerating == false else { return }
            let currentIndex = Int(newValue.contentOffset.y / newValue.frame.size.height)
            prejoin(focusIndex: currentIndex)
        }
    }
    
    public override var roomList: AGRoomArray? {
        didSet {
            debugLoaderPrint("[UI] update roomList")
            if let newValue = scrollView as? UICollectionView {
                var visibleRoomInfos:[IVideoLoaderRoomInfo] = []
                if newValue.isDragging == false, newValue.isDecelerating == false {
                    //更新roomlist时，已经完全停止则重新走停止后更新当前状态和上下预加载屏幕的状态
                    let state: AnchorState = audioSlicingType == .never ? .joinedWithVideo : .joinedWithAudioVideo
                    visibleRoomInfos = showVisibleRoom(collectionView: newValue, state: state, prejoinEnable: true)
                } else {
                    //没有停止的时候都改成joinedWithVideo，⚠️会存在当前房间画面无声音，滑动停止的时候才能听到
                    visibleRoomInfos = joinVideo()
                }
                //make diff
                let api = VideoLoaderApiImpl.shared
                visibleRoomInfos.forEach { roomInfo in
                    var anchorNeedsCleanIds = api.getUsedAnchorIds(tagId: roomInfo.channelName())
                    //find unused anchor info list
                    anchorNeedsCleanIds = anchorNeedsCleanIds.filter({ anchorId in
                        return roomInfo.anchorInfoList.first(where: { $0.channelName == anchorId}) == nil
                    })
                    anchorNeedsCleanIds.forEach { anchorId in
                        let anchorInfo = AnchorInfo()
                        anchorInfo.channelName = anchorId
                        api.switchAnchorState(newState: .idle,
                                              localUid: localUid,
                                              anchorInfo: anchorInfo,
                                              tagId: roomInfo.channelName())
                    }
                }
            }
            needReloadData = true
        }
    }
    
    public func getVisibleIndex(modelIndex: Int) -> Int {
        return modelIndex
    }
    
    public func getModelIndex(visibleIndex: Int) -> Int {
        return visibleIndex
    }
}

//MARK: private
extension AGCollectionSlicingDelegateHandler {
    fileprivate func joinVideo() -> [IVideoLoaderRoomInfo] {
        var visibleRoomInfos: [IVideoLoaderRoomInfo] = []
        guard let roomList = roomList, prejoinCount > 0, needPrejoin else {return visibleRoomInfos}
        guard let collectionView = scrollView as? UICollectionView else {return visibleRoomInfos}
        debugLoaderPrint("[UI]joinVideo start[\(collectionView.visibleCells.count)]=====")
        for (i, cell) in collectionView.visibleCells.enumerated() {
            let indexPath = collectionView.indexPathsForVisibleItems[i]
            if let roomInfo = roomList[indexPath.row] {
                let frame = cell.convert(cell.bounds, to: scrollView)
                if frame.intersects(collectionView.bounds) {
                    switchState(room: roomInfo, state: .joinedWithVideo, cell: cell, indexPath: indexPath)
                } else {
                    switchState(room: roomInfo, state: .prejoined, cell: cell, indexPath: indexPath)
                }
                visibleRoomInfos.append(roomInfo)
            }
        }
        debugLoaderPrint("[UI]joinVideo end=====")
        return visibleRoomInfos
    }
    
    fileprivate func prejoin(focusIndex: Int) {
        guard let roomList = roomList, focusIndex >= 0, prejoinCount > 0, needPrejoin else {return}
        let prevIdx = max(focusIndex - prejoinCount, 0)
        let nextIdx = min(focusIndex + prejoinCount, roomList.count() - 1)
        guard prevIdx < nextIdx else {return}
        var preloadIdxs:[Int] = []
        for i in prevIdx...nextIdx {
            if i == focusIndex {
                continue
            }
            preloadIdxs.append(i)
        }
        debugLoaderPrint("[UI]prejoin with index: \(preloadIdxs) start=====")
        preloadIdxs.forEach { idx in
            if let roomInfo = roomList[idx] {
                for anchorInfo in roomInfo.anchorInfoList {
                    VideoLoaderApiImpl.shared.switchAnchorState(newState: .prejoined, localUid: localUid, anchorInfo: anchorInfo, tagId: roomInfo.channelName())
                }
            }
        }
        debugLoaderPrint("[UI]prejoin with index: \(preloadIdxs) end=====")
    }
    
    fileprivate func _getVisibleCellTuple(collectionView: UICollectionView) -> (Int?, UICollectionViewCell?) {
        for (i, cell) in collectionView.visibleCells.enumerated() {
            if cell.convert(cell.frame.origin, from: collectionView) == .zero {
                return (collectionView.indexPathsForVisibleItems[i].row, cell)
            }
        }
        
        return (nil, nil)
    }
    
    fileprivate func showVisibleRoom(collectionView: UICollectionView?, state: AnchorState, prejoinEnable: Bool) -> [IVideoLoaderRoomInfo] {
        debugLoaderPrint("showVisibleRoom start ===== \(collectionView?.visibleCells.count ?? 0)")
        var visibleRoomInfos: [IVideoLoaderRoomInfo] = []
        guard let collectionView = collectionView else {return visibleRoomInfos}
        for (i, cell) in collectionView.visibleCells.enumerated() {
            let frame = cell.convert(cell.bounds, to: collectionView)
            if frame.intersects(collectionView.bounds) == false {continue}
            let indexPath = collectionView.indexPathsForVisibleItems[i]
            if let room = roomList?[indexPath.row] {
                switchState(room: room,
                            state: state,
                            cell: cell,
                            indexPath: indexPath)
                if prejoinEnable {
                    prejoin(focusIndex: indexPath.row)
                }
                visibleRoomInfos.append(room)
            }
        }
        debugLoaderPrint("showVisibleRoom end =====")
        return visibleRoomInfos
    }
    
    fileprivate func switchState(room: IVideoLoaderRoomInfo, state: AnchorState, cell: UICollectionViewCell, indexPath: IndexPath) {
        let videoLoaderApi = VideoLoaderApiImpl.shared
        for anchorInfo in room.anchorInfoList {
            videoLoaderApi.switchAnchorState(newState: state,
                                             localUid: localUid,
                                             anchorInfo: anchorInfo,
                                             tagId: room.channelName())
            let renderView = self.onRequireRenderVideo?(anchorInfo, cell, indexPath)
            let container = VideoCanvasContainer()
            container.uid = anchorInfo.uid
            container.container = renderView
            if state == .idle {
                container.setupMode = .remove
            }
            videoLoaderApi.renderVideo(anchorInfo: anchorInfo, container: container)
        }
    }
    
    open func cleanIdleRoom(collectionView: UICollectionView) {
        guard roomList?.count() ?? 0 > 0 else {return}
        let tuple = _getVisibleCellTuple(collectionView: collectionView)
        let visibleCell = tuple.1
        let visibleIndex = tuple.0
        
        guard let visibleIndex = visibleIndex, let _ = visibleCell, let roomList = roomList else {return}
        let visibleIndexs = [(visibleIndex + roomList.count() - 1) % roomList.count(), visibleIndex, (visibleIndex + 1) % roomList.count()]
        var visibleRoomIds: [String] = []
        let videoLoaderApi = VideoLoaderApiImpl.shared
        visibleIndexs.forEach { index in
            if let room = roomList[index] {
                for anchorInfo in room.anchorInfoList {
                    if visibleRoomIds.contains(anchorInfo.channelName) {continue}
                    visibleRoomIds.append(anchorInfo.channelName)
                }
            }
        }
        
        debugLoaderPrint("[UI]cleanIdleRoom without \(visibleRoomIds))")
        for (key, _) in videoLoaderApi.getConnectionMap() {
            if visibleRoomIds.contains(key) {continue}
            let roomInfo = AnchorInfo()
            roomInfo.channelName = key
            videoLoaderApi.switchAnchorState(newState: .idle, localUid: localUid, anchorInfo: roomInfo, tagId: key)
        }
    }
}

//MARK: UICollectionViewDelegate & UICollectionViewDataSource
extension AGCollectionSlicingDelegateHandler: UICollectionViewDelegate, UICollectionViewDataSource {
    //
    open func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return roomList?.count() ?? 0
    }
    
    open func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: UICollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(UICollectionViewCell.self),
                                                                            for: indexPath)
        
        self.scrollView = collectionView
        return cell
    }
    
    //
    open func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        let idx = indexPath.row
        guard let room = self.roomList?[idx] else {
//            assert(false, "room at index \(idx) not found")
            return
        }
        debugLoaderPrint("[UI]willDisplay[\(room.channelName())][count:\(room.anchorInfoList.count)]")
        
        if videoSlicingType == .visible || needReloadData {
            var state: AnchorState = .joinedWithVideo
            if audioSlicingType == .never {
            } else if needReloadData {
                state = .joinedWithAudioVideo
            }
            switchState(room: room,
                        state: state,
                        cell: cell,
                        indexPath: indexPath)
            
            //上报开始计算秒切出图
            VideoLoaderApiImpl.shared.startMediaRenderingTracing(anchorId: room.channelName())
        }
        needReloadData = false
        self.scrollView = collectionView
        
        #if DEBUG
        cellVisibleDate[room.channelName()] = Date()
        #endif
    }
    
    open func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        let idx = indexPath.row
        let visibleCellIndex = _getVisibleCellTuple(collectionView: collectionView).0
        if let visibleCellIndex = visibleCellIndex, idx == visibleCellIndex {
            debugLoaderPrint("[UI]didEndDisplaying break1: \(idx)/\(indexPath.row)")
            return
        } else if visibleCellIndex == nil {
            debugLoaderPrint("[UI]didEndDisplaying break2: \(idx)/\(indexPath.row)")
            return
        }
        guard let room = self.roomList?[idx] else { return }
        debugLoaderPrint("[UI]didEndDisplaying[\(room.channelName())][count:\(room.anchorInfoList.count)]: \(idx)/\(visibleCellIndex ?? -1)")
        let state: AnchorState = needPrejoin ? .prejoined : .idle
        switchState(room: room, state: state, cell: cell, indexPath: indexPath)
    }
    
    open func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
        guard videoSlicingType == .endDrag, let collectionView = scrollView as? UICollectionView else {return}
        let state: AnchorState = .joinedWithVideo
        let room = showVisibleRoom(collectionView: collectionView, state: state, prejoinEnable: false).first
        
        if let room = room {
            //上报开始计算秒切出图
            VideoLoaderApiImpl.shared.startMediaRenderingTracing(anchorId: room.channelName())
        }
    }
    
    open func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        guard let collectionView = scrollView as? UICollectionView else {return}
        cleanIdleRoom(collectionView: collectionView)
        //停止之后永远是把可视的变成
        let state: AnchorState = audioSlicingType == .never ? .joinedWithVideo : .joinedWithAudioVideo
        let room = showVisibleRoom(collectionView: collectionView, state: state, prejoinEnable: true).first
        
        if videoSlicingType == .endScroll, let room = room {
            //上报开始计算秒切出图
            VideoLoaderApiImpl.shared.startMediaRenderingTracing(anchorId: room.channelName())
        }
    }
}
