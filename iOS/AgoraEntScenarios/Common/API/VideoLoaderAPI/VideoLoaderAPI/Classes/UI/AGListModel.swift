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
    var anchorInfoList: [AnchorInfo] {get}
    func channelName() -> String
    func userId() -> String
}

