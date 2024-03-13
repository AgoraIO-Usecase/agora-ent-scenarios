//
//  AUINetworking.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation


public extension URLRequest {
    func cURL(pretty: Bool = false) -> String {
        let newLine = pretty ? "\\\n" : ""
        let method = (pretty ? "--request " : "-X ") + "\(httpMethod ?? "GET") \(newLine)"
        let url: String = (pretty ? "--url " : "") + "\'\(url?.absoluteString ?? "")\' \(newLine)"

        var cURL = "curl "
        var header = ""
        var data = ""

        if let httpHeaders = allHTTPHeaderFields, httpHeaders.keys.count > 0 {
            for (key, value) in httpHeaders {
                header += (pretty ? "--header " : "-H ") + "\'\(key): \(value)\' \(newLine)"
            }
        }

        if let bodyData = httpBody, let bodyString = String(data: bodyData, encoding: .utf8), !bodyString.isEmpty {
            data = "--data '\(bodyString)'"
        }

        cURL += method + url + header + data

        return cURL
    }
}

open class SyncNetworking: NSObject {
    static let shared: SyncNetworking = SyncNetworking()
    
    private var reqMap: [String: (URLSessionDataTask, SyncNetworkModel)] = [:]
    
    override init() {}
    
    private func baserequest(model: SyncNetworkModel, progress: ((Float) -> Void)?, completion: ((Error?, Any?) -> Void)?) {
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
            urlRequest.httpBody = encodeToJsonData(model.getParameters())
        }
        
        let handleResponse: ((Data?,URLResponse?,Error?) ->Void) = { data, response, error in
            if let error = error {
                aui_error("request fail: \(error), curl: \(urlRequest.cURL())", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(error, nil)
                }
                return
            }
            
            guard let data = data else {
                aui_error("parse fail: data empty,curl: \(urlRequest.cURL())", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(AUICommonError.httpError((response as? HTTPURLResponse)?.statusCode ?? -1, "http error").toNSError(), nil)
                }
                return
            }
            
            self.reqMap[model.uniqueId] = nil
            
            var obj: Any? = nil
            do {
                try obj = model.parse(data: data)
            } catch let err {
                aui_error("parse fail throw: , curl: \(urlRequest.cURL()) \(err.localizedDescription)", tag: "AUINetworking")
                aui_error("parse fail: \(String(data: data, encoding: .utf8) ?? "nil"),curl: \(urlRequest.cURL())", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(err, nil)
                }
                return
            }
            
            guard let obj = obj else {
                aui_error("parse fail: \(String(data: data, encoding: .utf8) ?? "nil"), curl: \(urlRequest.cURL())", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(AUICommonError.networkParseFail.toNSError(), nil)
                }
                return
            }
            
            aui_error("request success url = \(urlRequest.cURL()) message = \(String(data: data, encoding: .utf8) ?? "nil")", tag: "AUINetworking")
            DispatchQueue.main.async {
                completion?(nil, obj)
            }
        }
        
        let dataTask = URLSession.shared.dataTask(with: urlRequest,completionHandler: handleResponse)
        dataTask.resume()
        reqMap[model.uniqueId] = (dataTask, model)
    }
    
    public func request(model: SyncNetworkModel, completion:  ((Error?, Any?) -> Void)?) {
        baserequest(model: model, progress: nil, completion: completion)
    }
    
    public func cancel(model: SyncNetworkModel) {
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
