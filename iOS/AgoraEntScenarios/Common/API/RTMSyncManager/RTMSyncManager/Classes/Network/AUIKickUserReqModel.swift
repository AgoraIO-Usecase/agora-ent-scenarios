//
//  AUIKickUserReqModel.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/26.
//

import UIKit

@objcMembers open class SyncKickUserReqModel: SyncNetworkModel {
    
    public override init() {
        super.init()
        interfaceName = "/v1/users/kickOut"
    }
    
    /// Description 当前api 调用者
    public var operatorId: String?
    
    public var roomId: String?
    
    /// Description 被踢用户id
    public var uid: Int = 0
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic1 = dic as? [String: Any],let result = dic1["data"] as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        return result
    }
}
