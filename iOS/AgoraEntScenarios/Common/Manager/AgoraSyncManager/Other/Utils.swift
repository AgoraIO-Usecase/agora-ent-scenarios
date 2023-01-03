//
//  Utils.swift
//  RtmSyncManager
//
//  Created by xianing on 2021/9/25.
//

import Foundation

class Utils {
    static func getJson(dict: NSDictionary?) -> String {
        let data = try? JSONSerialization.data(withJSONObject: dict!, options: JSONSerialization.WritingOptions(rawValue: 0))
        let jsonStr = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
        return jsonStr! as String
    }

    static func getDict(text: String) -> [String: AnyObject]? {
        if let data = text.data(using: String.Encoding.utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: [JSONSerialization.ReadingOptions(rawValue: 0)]) as? [String: AnyObject]
            } catch let error as NSError {
                print(error)
            }
        }
        return nil
    }

    /// JSON字符串转字典
    static func toDictionary(jsonString: String) -> [String: Any] {
        guard let jsonData = jsonString.data(using: .utf8) else { return [:] }
        guard let dict = try? JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers), let result = dict as? [String: Any] else { return [:] }
        return result
    }

    /// 字典转JSON字符串
    static func toJsonString(dict: [String: Any]?) -> String? {
        guard let dict = dict else { return nil }
        if !JSONSerialization.isValidJSONObject(dict) {
            print("字符串格式错误！")
            return nil
        }
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        guard let jsonString = String(data: data, encoding: .utf8) else { return nil }
        return jsonString
    }
}

class GroceryProduct: Decodable {
    var name: String
    var points: Int
    var description: String
}
