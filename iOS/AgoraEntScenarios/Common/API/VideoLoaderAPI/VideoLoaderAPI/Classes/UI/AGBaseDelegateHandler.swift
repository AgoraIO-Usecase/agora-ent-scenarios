//
//  AGBaseDelegateHandler.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/9/6.
//

import Foundation

@objcMembers
open class AGRoomArray: NSObject {
    public private(set) var roomList: [IVideoLoaderRoomInfo] = []
    
    required public init(roomList: [IVideoLoaderRoomInfo]?) {
        super.init()
        self.roomList = roomList ?? []
    }
    
    open subscript(_ index: Int) -> IVideoLoaderRoomInfo? {
        if index < roomList.count && index >= 0 {
            return roomList[index]
        } else {
            return nil
        }
    }
    
    open func count() -> Int {
        return roomList.count
    }
}

@objcMembers
open class AGBaseDelegateHandler: NSObject {
    public private(set) var localUid: UInt = 0
    public var roomList: AGRoomArray?
    
    required public init(localUid: UInt) {
        super.init()
        self.localUid = localUid
    }
    
    func preLoadVisibleItems(index: Int, count: UInt) {
        guard let roomList = roomList, index >= 0 else {return}
        var padding = (20 - Int(count)) / 2
        padding = padding > 0 ? padding : 0
        let start = index - padding < 0 ? 0 : index - padding
        var end = start + (Int(count) - 1) + padding
        end = end < roomList.count() ? end : roomList.count() - 1
        var preloadAnchorList: [AnchorInfo] = []
        var preloadAnchorIds: [String] = []
        
        var i = index
        var isForward = true
        var stride = 1
        while true {
            if i >= 0, i < roomList.count() {
                if let room = roomList[i] {
                    room.anchorInfoList.forEach { anchorInfo in
                        if preloadAnchorIds.contains(anchorInfo.channelName) {return}
                        preloadAnchorList.append(anchorInfo)
                        preloadAnchorIds.append(anchorInfo.channelName)
                    }
                }
                if preloadAnchorIds.count > 20 {
                    break
                }
            }
            if index - stride < start, index + stride > end {
                break
            }
            
            if isForward {
                isForward = false
                i = index + stride
            } else {
                isForward = true
                i = index - stride
                stride += 1
            }
        }
        
        VideoLoaderApiImpl.shared.preloadAnchor(preloadAnchorList: preloadAnchorList.reversed(), uid: localUid)
    }
}
