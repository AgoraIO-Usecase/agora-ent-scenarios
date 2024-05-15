//
//  AUIRoomListNetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/18.
//

import UIKit

@objcMembers
public class SyncRoomListNetworkModel: SyncNetworkModel {
    public var lastCreateTime: NSNumber?
    public var pageSize: Int = 10
    public var appId: String? = AUIRoomContext.shared.commonConfig?.appId
    public var sceneId: String?
    public override init() {
        super.init()
        interfaceName = "/v2/room/list"
    }
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: Any],
              let list = result["list"],
              let ts = dic["ts"] as? Int64,
              let roomList = NSArray.yy_modelArray(with: AUIRoomInfo.self, json: list) else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        return ["ts": ts, "list": roomList]
    }
}

