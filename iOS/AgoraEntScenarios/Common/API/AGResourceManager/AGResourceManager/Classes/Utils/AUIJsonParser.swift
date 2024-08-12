//
//  AUIJsonParser.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/12/6.
//

import Foundation

func decodeModel<T: Codable>(_ dictionary: [String: Any]) -> T? {
    let decoder = JSONDecoder()
    do {
        let data = try JSONSerialization.data(withJSONObject: dictionary, options: .prettyPrinted)
        let model = try decoder.decode(T.self, from: data)
        return model
    } catch {
        aui_warn("decode model fail: \(error)")
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

func encodeModel<T: Codable>(_ model: T) -> [String: Any]? {
    let encoder = JSONEncoder()
    encoder.keyEncodingStrategy = .useDefaultKeys
    var dictionary: [String: Any]?
    do {
        let data = try encoder.encode(model)
        dictionary = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
    } catch {
        aui_warn("encode model fail: \(error.localizedDescription)")
        return nil
    }
    
    return dictionary
}


func encodeModelToJsonStr<T: Codable>(_ model: T) -> String? {
    guard let jsonObj = encodeModel(model) else { return nil }
    guard let data = try? JSONSerialization.data(withJSONObject: jsonObj, options: .prettyPrinted),
          let message = String(data: data, encoding: .utf8) else {
        return nil
    }
    
    return message
}

public func decodeToJsonObj(_ jsonStr: String) -> Any? {
    guard let jsonData = jsonStr.data(using: .utf8),
          let jsonObj = try? JSONSerialization.jsonObject(with: jsonData, options: []) else {
        return nil
    }
    
    return jsonObj
}

public func encodeToJsonData(_ jsonObj: Any?) -> Data? {
    guard let jsonObj = jsonObj,
          let data = try? JSONSerialization.data(withJSONObject: jsonObj, options: .prettyPrinted) else {
        return nil
    }
    
    return data
}

public func encodeToJsonStr(_ jsonObj: Any) -> String? {
    guard let data = encodeToJsonData(jsonObj),
          let value = String(data: data, encoding: .utf8) else {
        return nil
    }
    
    return value
}


public func isValuesEqual(_ value1: Any, _ value2: Any) -> Bool {
    if let dict1 = value1 as? [String: Any], let dict2 = value2 as? [String: Any] {
        return isDictionariesEqual(dict1, dict2)
    }
    
    if let array1 = value1 as? [Any], let array2 = value2 as? [Any] {
        return isArraysEqual(array1, array2)
    }
    
    if let str1 = value1 as? String, let str2 = value2 as? String {
        return str1 == str2
    }
    
    return value1 as AnyObject === value2 as AnyObject
}

public func isDictionariesEqual(_ dict1: [String: Any], _ dict2: [String: Any]) -> Bool {
    // Step 1
    if dict1.keys.count != dict2.keys.count {
        return false
    }
    
    // Step 2
    for (key, value) in dict1 {
        if let otherValue = dict2[key] {
            if !isValuesEqual(value, otherValue) {
                return false
            }
        } else {
            return false
        }
    }
    
    return true
}

public func isArraysEqual(_ array1: [Any], _ array2: [Any]) -> Bool {
    if array1.count != array2.count {
        return false
    }
    
    for (index, value) in array1.enumerated() {
        if !isValuesEqual(value, array2[index]) {
            return false
        }
    }
    
    return true
}
