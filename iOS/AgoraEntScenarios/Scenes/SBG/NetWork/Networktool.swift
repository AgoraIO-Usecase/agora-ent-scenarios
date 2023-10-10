//
//  Networktool.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/18.
//

import Foundation

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
}

class NetworkTools {
    
    func request(_ urlString: String, method: HTTPMethod, parameters: [String: Any]?, completion: @escaping (Result<Data, Error>) -> Void) {
        
        guard let url = URL(string: urlString) else {
            // URL 无法转换，处理错误
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue

        if method == .post, let params = parameters {
            do {
                request.httpBody = try JSONSerialization.data(withJSONObject: params)
                request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            } catch {
                // JSON 转换失败，处理错误
            }
        } else if method == .get, let params = parameters {
            if let urlComponents = NSURLComponents(url: url, resolvingAgainstBaseURL: false) {
                urlComponents.queryItems = []
                for (key, value) in params {
                    urlComponents.queryItems?.append(URLQueryItem(name: key, value: String(describing: value)))
                }
                request.url = urlComponents.url!
            }
        }

        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data, let response = response as? HTTPURLResponse else {
                completion(.failure(NSError(domain: "NetworkTools", code: 0, userInfo: nil)))
                return
            }
            
            if 200..<300 ~= response.statusCode {
                completion(.success(data))
            } else {
                completion(.failure(NSError(domain: "NetworkTools", code: response.statusCode, userInfo: nil)))
            }
        }
        
        task.resume()
    }
    
}
