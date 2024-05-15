//
//  AUINetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import YYModel

public enum SyncNetworkMethod: Int {
    case get = 0
    case post
    
    func getAfMethod() -> String {
        switch self {
        case .get:
            return "GET"
        case .post:
            return "POST"
        }
    }
}

@objcMembers
open class SyncNetworkModel: NSObject {
    public var uniqueId: String = UUID().uuidString
    public var host: String = AUIRoomContext.shared.commonConfig?.host ?? ""
    public var interfaceName: String?
    public var method: SyncNetworkMethod = .post
    
    static func modelPropertyBlacklist() -> [Any] {
        return ["uniqueId", "host", "interfaceName", "method"]
    }
    
    public func getHeaders() -> [String: String] {
        return ["Content-Type": "application/json"]
    }
    
    public func getParameters() -> [String: Any]? {
        let param = self.yy_modelToJSONObject() as? [String: Any]
        return param
    }
    
    public func request(completion: ((Error?, Any?)->())?) {
        SyncNetworking.shared.request(model: self, completion: completion)
    }
    
    
    public func parse(data: Data?) throws -> Any?  {
        guard let data = data,
              let dic = try? JSONSerialization.jsonObject(with: data) else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        if let dic = (dic as? [String: Any]), let code = dic["code"] as? Int, code != 0 {
            let message = dic["message"] as? String ?? ""
            throw AUICommonError.httpError(code, message).toNSError()
        }
        
        return dic
    }
    
    public func createBasicAuth(key: String, password: String) -> String {
        let loginString = String(format: "%@:%@", key, password)
        guard let loginData = loginString.data(using: String.Encoding.utf8) else {
            return ""
        }
        let base64LoginString = loginData.base64EncodedString()
        return base64LoginString
    }
}


@objcMembers
open class SyncCommonNetworkModel: SyncNetworkModel {
    public var userId: String?
}
