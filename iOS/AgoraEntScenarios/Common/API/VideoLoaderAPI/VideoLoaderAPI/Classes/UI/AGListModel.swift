//
//  AGListModel.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/8/30.
//

import Foundation

public let kUIListViewCellIdentifier = "ag_UICollectionViewCell"
func createDateFormatter()-> DateFormatter {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    return formatter
}

public let formatter = createDateFormatter()
public protocol IVideoLoaderRoomInfo: NSObjectProtocol {
    
    /// 当前房间的互动对象，如果有count > 1表示是pk或连麦，count == 1个表示单主播展示
    var anchorInfoList: [AnchorInfo] {get}
    
    /// 当前房间的id
    /// - Returns: <#description#>
    func channelName() -> String
    
    /// 当前房间的房主uid
    /// - Returns: <#description#>
    func userId() -> String
}

