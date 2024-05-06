//
//  AUICollectionModel.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

enum AUIAnyType : Codable, Equatable {
     case int(Int)
     case string(String)
     case list([AUIAnyType])
     case dictionary([String : AUIAnyType])

     public init(from decoder: Decoder) throws {
         // Can be made prettier, but as a simple example:
         let container = try decoder.singleValueContainer()
         do {
             self = .int(try container.decode(Int.self))
         } catch DecodingError.typeMismatch {
             do {
                 self = .string(try container.decode(String.self))
             } catch DecodingError.typeMismatch {
                 do {
                     self = .list(try container.decode([AUIAnyType].self))
                 } catch DecodingError.typeMismatch {
                     self = .dictionary(try container.decode([String : AUIAnyType].self))
                 }
             }
         }
     }

     public func encode(to encoder: Encoder) throws {
         var container = encoder.singleValueContainer()
         switch self {
         case .int(let int): try container.encode(int)
         case .string(let string): try container.encode(string)
         case .list(let list): try container.encode(list)
         case .dictionary(let dictionary): try container.encode(dictionary)
         }
     }

    static func ==(_ lhs: AUIAnyType, _ rhs: AUIAnyType) -> Bool {
        switch (lhs, rhs) {
        case (.int(let int1), .int(let int2)): return int1 == int2
        case (.string(let string1), .string(let string2)): return string1 == string2
        case (.list(let list1), .list(let list2)): return list1 == list2
        case (.dictionary(let dict1), .dictionary(let dict2)): return dict1 == dict2
        default: return false
        }
    }
    
    public init(array: [Any]) {
        var typeArray: [AUIAnyType] = []
        array.forEach { value in
            if let v = value as? Int {
                typeArray.append(.int(v))
            } else if let v = value as? String {
                typeArray.append(.string(v))
            } else if let v = value as? [Any] {
                typeArray.append(AUIAnyType(array: v))
            } else if let v = value as? [String: Any] {
                typeArray.append(AUIAnyType(map: v))
            }
        }
        self = .list(typeArray)
    }
    
    public init(map: [String: Any]) {
        var typeMap: [String: AUIAnyType] = [:]
        map.forEach { (key: String, value: Any) in
            if let v = value as? Int {
                typeMap[key] = .int(v)
            } else if let v = value as? Bool {
                typeMap[key] = .int(v ? 1 : 0)
            } else if let v = value as? String {
                typeMap[key] = .string(v)
            } else if let v = value as? [Any] {
                typeMap[key] = AUIAnyType(array: v)
            } else if let v = value as? [String: Any] {
                typeMap[key] = AUIAnyType(map: v)
            }
        }
        self = .dictionary(typeMap)
    }
    
    public func toJsonObject() -> Any {
        switch self {
        case .int(let int):
            return int
        case .string(let string):
            return string
        case .list(let list):
            var jsonList: [Any] = []
            list.forEach { value in
                let val = (value as AUIAnyType).toJsonObject()
                jsonList.append(val)
            }
            return jsonList
        case .dictionary(let dictionary):
            var jsonMap: [String: Any] = [:]
            dictionary.forEach { (key: String, value: AUIAnyType) in
                let val = (value as AUIAnyType).toJsonObject()
                jsonMap[key] = val
            }
            return jsonMap
        }
    }
}

enum AUIMessageType: Int, Codable {
    case normal = 1
    case receipt = 2
}

enum AUICollectionOprationType: Int, Codable {
    case add = 0      //新增
    case update = 1   //更新，对传递进来的map进行根节点上的替换
    case merge = 2    //合并，对传递进来的map进行每个子节点的替换
    case remove = 3   //删除
    case clean = 4    //清理对应scene的key/value，相当于在rtm metadata里移除这个collection的所有信息
    case calculate = 10 //计算属性，增加/减少
}

struct AUICollectionError: Codable {
    public var code: Int?
    public var reason: String?
    
    enum CodingKeys: String, CodingKey {
        case code, reason
    }
}

struct AUICollectionCalcValue: Codable {
    public var value: Int
    public var min: Int
    public var max: Int
    
    enum CodingKeys: String, CodingKey {
        case value, min, max
    }
}

struct AUICollectionCalcData: Codable {
    public var key: [String]
    public var value: AUICollectionCalcValue
    
    enum CodingKeys: String, CodingKey {
        case key, value
    }
}

struct AUICollectionMessagePayload: Codable {
    public var type: AUICollectionOprationType?
    public var dataCmd: String?                    //[可选]基于这次改动的类型
    public var filter: AUIAnyType?   //表示列表里对应item[filter.key] == filter.value 的数据都要按照data里修改
    public var data: AUIAnyType?

    enum CodingKeys: String, CodingKey {
        case type, dataCmd, filter, data
    }
}

struct AUICollectionMessage: Codable {
    public var channelName: String  //频道名，防止用户加入多个频道导致消息窜了
    public var messageType: AUIMessageType  //消息类型，表示正常类型/回执
    public var sceneKey: String   //表示修改的表字段，根据这个key每个collection自动和自己的observerKey判断是否匹配以进行下一步
    public var uniqueId: String   //唯一表示，用于校验回执
    public var payload: AUICollectionMessagePayload
    
    enum CodingKeys: String, CodingKey {
        case channelName, messageType, sceneKey, uniqueId, payload
    }
}
