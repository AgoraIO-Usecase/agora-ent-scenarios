//
//  obs.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

public struct Scene: Codable {
    let id: String
    let userId: String
    let property: [String : Any]?
    
    public init(id: String,
                userId: String,
                property: [String : Any]?) {
        self.id = id
        self.userId = userId
        self.property = property
    }
    
    func toJson() -> String {
        var dict = [String : Any]()
        dict["id"] = id
        dict["userId"] = userId
        let _ = self.property?.map({ (key,value) in
            dict[key] = value
        })
        return Utils.getJson(dict: dict as NSDictionary)
    }
    
    enum CodingKeys: String, CodingKey {
        case id
        case userId
        case property
    }
    
    public init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        id = try values.decode(String.self, forKey: .id)
        userId = try values.decode(String.self, forKey: .userId)
        let data = try values.decode(Data.self, forKey: .property)
        property = try JSONSerialization.jsonObject(with: data,
                                                    options: []) as? [String: Any]
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(userId, forKey: .userId)
        let data = try JSONSerialization.data(withJSONObject: property ?? [:],
                                              options: [])
        try container.encode(data, forKey: .property)
    }
}

/* key 是对象存储中的id, 在rtm中是channelAttribute的key, value在对象存储中是一条记录, 在rtm中是一个json字符串 */
open class Attribute: IObject, Equatable {
    var object: String
    var key: String
    public func getId() -> String {
        return key
    }
    
    public func getPropertyWith(key: String, type: Any.Type) -> Any? {
        let dict = Utils.getDict(text: object)
        return dict?[key]
    }
    
    public func toJson() -> String? {
        var dict = Utils.toDictionary(jsonString: object)
        dict["objectId"] = key
        return Utils.toJsonString(dict: dict)
    }
    
    public init(key: String, value: String) {
        self.key = key
        self.object = value
    }
    
    public static func == (lhs: Attribute, rhs: Attribute) -> Bool {
        return lhs.object == rhs.object &&
        lhs.key == rhs.key
    }
    
    public func toObject<T>() throws -> T? where T : Decodable {
        let jsonDecoder = JSONDecoder()
        if let data = object.data(using: .utf8) {
            return try jsonDecoder.decode(T.self, from: data)
        }
        return nil
    }
}

struct CollectionItem: Codable {
    let objectId: String
    
    static func decodeWithString(jsonString: String, decoder: JSONDecoder) -> CollectionItem? {
        guard let data = jsonString.data(using: .utf8) else {
            Log.errorText(text: "json string can not be trans a data", tag: "CollectionItem.decodeWithString")
            return nil
        }
        do {
            let obj = try decoder.decode(CollectionItem.self, from: data)
            return obj
        } catch let error {
            Log.error(error: error as CustomStringConvertible, tag:  "CollectionItem.decodeWithString")
            return nil
        }
    }
    
    static func getObjId(jsonString: String, decoder: JSONDecoder) -> String? {
        let obj = CollectionItem.decodeWithString(jsonString: jsonString, decoder: decoder)
        return obj?.objectId
    }
}



