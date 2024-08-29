//
//  AUINetworking.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation

func maskSensitiveParameters(_ text: String) -> String {
    let patterns = [
        "appId=([^\\s'&]+)('?|&)",            // Match appId=xxx' or appId=xxx& format
        "ppId\"\\s*:\\s*\"([^\"]*)\"",        // Match ppId\":\"xxx\" format
        "appCertificate\":\"([^\"]*)\"",      // Match appCertificate\":\"xxx\" format
        "basicAuth\":\"([^\"]*)\"",           // Match basicAuth\":\"xxx\" format
        "token\":\"([^\"]*)\"",               // Match token\":\"xxx\" format
        "Authorization:(.*?)'",               // Match Authorization:xxx' format
        "Id:(.*?)'",                          // Match Id:xxx' format
        "Key:(.*?)'",                         // Match Key:xxx' format
        "Session:(.*?)'"                      // Match Session:xxx' format
    ]
    
    var maskedText = text
    
    for pattern in patterns {
        if let regex = try? NSRegularExpression(pattern: pattern, options: []) {
            let range = NSRange(location: 0, length: maskedText.utf16.count)
            
            regex.enumerateMatches(in: maskedText, options: [], range: range) { match, _, _ in
                if let match = match {
                    let fullMatchRange = match.range(at: 0)
                    
                    // Use capture groups for replacement
                    let nsString = maskedText as NSString
                    let fullMatchString = nsString.substring(with: fullMatchRange)
                    
                    // Define the replacement rule
                    let maskedString = fullMatchString.replacingOccurrences(of: nsString.substring(with: match.range(at: 1)), with: "***")
                    
                    // Replace sensitive content in the original string
                    maskedText = maskedText.replacingCharacters(in: Range(fullMatchRange, in: maskedText)!, with: maskedString)
                }
            }
        }
    }
    
    return maskedText
}

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
            let urlStr = maskSensitiveParameters(urlRequest.cURL())
            if let error = error {
                aui_error("request fail: \(error), curl: \(urlStr)", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(error, nil)
                }
                return
            }
            
            guard let data = data else {
                aui_error("parse fail: data empty,curl: \(urlStr)", tag: "AUINetworking")
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
                aui_error("parse fail throw: , curl: \(urlStr) \(err.localizedDescription)", tag: "AUINetworking")
                aui_error("parse fail: \(maskSensitiveParameters(String(data: data, encoding: .utf8) ?? "nil")),curl: \(urlStr)", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(err, nil)
                }
                return
            }
            
            guard let obj = obj else {
                aui_error("parse fail: \(maskSensitiveParameters(String(data: data, encoding: .utf8) ?? "nil")), curl: \(urlStr)", tag: "AUINetworking")
                DispatchQueue.main.async {
                    completion?(AUICommonError.networkParseFail.toNSError(), nil)
                }
                return
            }
            
            aui_info("request success url = \(urlStr) message = \(maskSensitiveParameters(String(data: data, encoding: .utf8) ?? "nil"))", tag: "AUINetworking")
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
