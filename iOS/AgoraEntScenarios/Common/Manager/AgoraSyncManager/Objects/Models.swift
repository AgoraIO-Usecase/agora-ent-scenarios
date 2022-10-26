//
//  obs.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

public class Scene: Codable {
    let id: String
    let userId: String
    let property: [String: String]?

    public init(id: String,
                userId: String,
                property: [String: String]?)
    {
        self.id = id
        self.userId = userId
        self.property = property
    }

    func toJson() -> String {
        var dict = [String: String]()
        dict["id"] = id
        dict["userId"] = userId
        let _ = property?.map({ key, value in
            dict[key] = value
        })
        return Utils.getJson(dict: dict as NSDictionary)
    }
}

/* key 是对象存储中的id, 在rtm中是channelAttribute的key, value在对象存储中是一条记录, 在rtm中是一个json字符串 */
class Attribute: IObject, Equatable {
    var object: String
    var key: String
    func getId() -> String {
        return key
    }

    func getPropertyWith(key: String, type: Any.Type) -> Any? {
        let dict = Utils.getDict(text: object)
        return dict?[key]
    }

    func toJson() -> String? {
        var dict = Utils.toDictionary(jsonString: object)
        dict["objectId"] = key
        return Utils.toJsonString(dict: dict)
    }

    init(key: String, value: String) {
        self.key = key
        object = value
    }

    static func == (lhs: Attribute, rhs: Attribute) -> Bool {
        return lhs.object == rhs.object &&
            lhs.key == rhs.key
    }

    func toObject<T>() throws -> T? where T: Decodable {
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
        } catch {
            Log.error(error: error as CustomStringConvertible, tag: "CollectionItem.decodeWithString")
            return nil
        }
    }

    static func getObjId(jsonString: String, decoder: JSONDecoder) -> String? {
        let obj = CollectionItem.decodeWithString(jsonString: jsonString, decoder: decoder)
        return obj?.objectId
    }
}
