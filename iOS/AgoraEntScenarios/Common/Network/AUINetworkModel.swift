//
//  AUINetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import Alamofire
import YYModel

public enum AUINetworkMethod: Int {
    case get = 0
    case post
    
    func getAfMethod() -> Alamofire.HTTPMethod {
        switch self {
        case .get:
            return Alamofire.HTTPMethod.get
        case .post:
            return Alamofire.HTTPMethod.post
        }
    }
}

@objcMembers
open class AUINetworkModel: NSObject {
    public let uniqueId: String = UUID().uuidString
    public var host: String = KeyCenter.HostUrl
    public var interfaceName: String?
    public var method: AUINetworkMethod = .post
    
    static func modelPropertyBlacklist() -> [Any] {
        return ["uniqueId", "host", "interfaceName", "method"]
    }
    
    public func getHeaders() -> HTTPHeaders {
        var headers = HTTPHeaders()
        let header = HTTPHeader(name: "Content-Type", value: "application/json")
        headers.add(header)
        return headers
    }
    
    public func getParameters() -> Parameters? {
        let param = self.yy_modelToJSONObject() as? Parameters
        return param
    }
    
    public func request(completion: ((Error?, Any?)->())?) {
        AUINetworking.shared.request(model: self, completion: completion)
    }
    
    
    public func parse(data: Data?) throws -> Any  {
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
open class AUIUploadNetworkModel: AUINetworkModel {
    public var fileData: Data!
    public var name: String!
    public var fileName: String?
    public var mimeType: String?
    
    public func upload(progress:((Float) -> Void)? = nil ,completion: ((Error?, Any?)->())?) {
        AUINetworking.shared.upload(model: self, progress: progress, completion: completion)
    }
}
