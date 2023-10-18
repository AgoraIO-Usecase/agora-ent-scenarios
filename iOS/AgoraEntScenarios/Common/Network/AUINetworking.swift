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
    
    private var reqMap: [String: (URLSessionDataTask, AUINetworkModel)] = [:]
    
    override init() {}
    
    private func baserequest(model: AUINetworkModel, progress: ((Float) -> Void)?, completion: ((Error?, Any?) -> Void)?) {
        cancel(model: model)
        if model.host.count == 0 {
            completion?(AUICommonError.httpError(-1, "request host is empty").toNSError(), nil)
            return
        }
        var url = "\(model.host)\(model.interfaceName ?? "")"
        if model.method == .get {
            url = url.appendingParameters(parameters: model.getParameters())
        }
        
        guard let url = URL(string: url) else {
            completion?(AUICommonError.httpError(-1, "invalid url").toNSError(), nil)
            return
        }
        
        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = model.method.getAfMethod()
        urlRequest.allHTTPHeaderFields = model.getHeaders()
        if model.method == .post {
            urlRequest.httpBody = model.getHttpBody()
        }
        
        let handleResponse: ((Data?,URLResponse?,Error?) ->Void) = { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    showLogger.error("request fail: \(error)", context: "AUINetworking")
                    completion?(error, nil)
                    return
                }
                
                guard let data = data else {
                    showLogger.error("parse fail: data empty", context: "AUINetworking")
                    completion?(AUICommonError.httpError((response as? HTTPURLResponse)?.statusCode ?? -1, "http error").toNSError(), nil)
                    return
                }
                
                self.reqMap[model.uniqueId] = nil
                
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
        }
        
        if let uploadModel = model as? AUIUploadNetworkModel {
            let uploadTask = URLSession.shared.uploadTask(with: urlRequest, from: uploadModel.fileData, completionHandler: handleResponse)
            uploadTask.resume()
            reqMap[model.uniqueId] = (uploadTask, model)
        } else {
            let dataTask = URLSession.shared.dataTask(with: urlRequest,completionHandler: handleResponse)
            dataTask.resume()
            reqMap[model.uniqueId] = (dataTask, model)
        }
    }
    
    public func request(model: AUINetworkModel, completion:  ((Error?, Any?) -> Void)?) {
        baserequest(model: model, progress: nil, completion: completion)
    }
    
    public func upload(model: AUIUploadNetworkModel, progress:((Float)->Void)?, completion:  ((Error?, Any?) -> Void)?) {
        baserequest(model: model, progress: progress, completion: completion)
    }
    
    public func cancel(model: AUINetworkModel) {
        guard let pair = reqMap[model.uniqueId] else {return}
        pair.0.cancel()
        reqMap[model.uniqueId] = nil
    }
}

extension String {
    func appendingParameters(parameters: [String: Any]?) -> String {
        guard let parameters = parameters else {
            return self
        }
        var url = self
        if !parameters.isEmpty {
            let paramComponents = parameters.map { "\($0.key)=\($0.value)" }
            let paramString = paramComponents.joined(separator: "&")
            url += "?\(paramString)"
        }
        return url
    }
}
