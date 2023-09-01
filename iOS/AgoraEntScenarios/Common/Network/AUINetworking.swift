//
//  AUINetworking.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import Alamofire

open class AUINetworking: NSObject {
    static let shared: AUINetworking = AUINetworking()
    
    private var reqMap: [String: (DataRequest, AUINetworkModel)] = [:]
    
    override init() {
        
    }
    
    public func request(model: AUINetworkModel, completion:  ((Error?, Any?) -> Void)?) {
        cancel(model: model)
        if model.host.count == 0 {
            completion?(AUICommonError.httpError(-1, "request host is empty").toNSError(), nil)
            return
        }
        let url = "\(model.host)\(model.interfaceName ?? "")"
        
        let dataReq = AF.request(url,
                                 method: model.method.getAfMethod(),
                                 parameters: model.getParameters(),
                                 encoding: JSONEncoding.default,
                                 headers: model.getHeaders(),
                                 interceptor: nil,
                                 requestModifier: nil).response {[weak self] resp in
            guard let self = self else {return}
            
            guard let data = resp.data else {
                showLogger.error("parse fail: data empty", context: "AUINetworking")
                completion?(AUICommonError.httpError(resp.response?.statusCode ?? -1, "http error").toNSError(), nil)
                return
            }

            self.reqMap[model.uniqueId] = nil
            if let error = resp.error {
                showLogger.error("request fail: \(error)", context: "AUINetworking")
                completion?(error, nil)
                return
            }

            var obj: Any? = nil
            do {
                try obj = model.parse(data: data)
            } catch let err {
                showLogger.error("parse fail throw: \(err.localizedDescription)", context: "AUINetworking")
                showLogger.error("parse fail: \(String(data: data, encoding: .utf8) ?? "nil")", context: "AUINetworking")
                completion?(err, nil)
                return
            }
            guard let obj = obj else {
                showLogger.error("parse fail: \(String(data: data, encoding: .utf8) ?? "nil")", context: "AUINetworking")
                completion?(AUICommonError.networkParseFail.toNSError(), nil)
                return
            }
            showLogger.error("request success \(String(data: data, encoding: .utf8) ?? "nil")", context: "AUINetworking")
            completion?(nil, obj)
        }
        dataReq.cURLDescription { url in
            showLogger.error("request: \(url)")
        }
        
        reqMap[model.uniqueId] = (dataReq, model)
    }
    
    public func cancel(model: AUINetworkModel) {
        guard let pair = reqMap[model.uniqueId] else {return}
        pair.0.cancel()
        reqMap[model.uniqueId] = nil
    }
}
