//
//  AUITokenGenerateNetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/20.
//

import Foundation

public class SyncTokenGenerateNetworkModel: SyncNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v2/token/generate"
    }
    
    public var appId: String? = AUIRoomContext.shared.commonConfig?.appId
    public var appCert: String? = AUIRoomContext.shared.commonConfig?.appCert
    public var channelName: String?
    public var userId: String?
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: String] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        return result
    }
}
