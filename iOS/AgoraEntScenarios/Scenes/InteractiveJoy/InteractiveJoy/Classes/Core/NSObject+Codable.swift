//
//  NSObject+Codable.swift
//  Joy
//
//  Created by wushengtao on 2023/12/6.
//

import Foundation

extension NSObject {
    func decodeModel<T: Codable>(_ dictionary: [String: Any]) -> T? {
        let decoder = JSONDecoder()
        do {
            let data = try JSONSerialization.data(withJSONObject: dictionary, options: .prettyPrinted)
            let model = try decoder.decode(T.self, from: data)
            return model
        } catch {
            JoyLogger.warn("decode model fail: \(error)")
        }
        return nil
    }
    
    func decodeModelArray<T: Codable>(_ array: [[String: Any]]) -> [T]? {
        var modelArray: [T] = []
        for dic in array {
            if let model: T = decodeModel(dic) {
                modelArray.append(model)
            }
        }
        if modelArray.count > 0 {
            return modelArray
        }
        return nil
    }
    
    func encodeModel(_ model: Codable) -> [String: Any]? {
        let encoder = JSONEncoder()
        encoder.keyEncodingStrategy = .useDefaultKeys
        var dictionary: [String: Any]?
        do {
            let data = try encoder.encode(model)
            dictionary = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
        } catch {
            JoyLogger.warn("encode model fail: \(error.localizedDescription)")
            return nil
        }
        
        return dictionary
    }
}
