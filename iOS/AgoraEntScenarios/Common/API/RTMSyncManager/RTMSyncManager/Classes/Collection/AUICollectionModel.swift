//
//  AUICollectionModel.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

enum AUIAnyType : Codable, Equatable {
     case uint(UInt)
     case int(Int)
     case uint64(UInt64)
     case int64(Int64)
     case bool(Bool)
     case string(String)
     case list([AUIAnyType])
     case dictionary([String : AUIAnyType])
    
     public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        
        let result: Result<AUIAnyType, Error> = Result {
            if let intValue = try? container.decode(Int.self) {
                return .int(intValue)
            } else if let uintValue = try? container.decode(UInt.self) {
                return .uint(uintValue)
            } else if let intValue = try? container.decode(Int64.self) {
                return .int64(intValue)
            } else if let uintValue = try? container.decode(UInt64.self) {
                return .uint64(uintValue)
            } else if let boolValue = try? container.decode(Bool.self) {
                return .bool(boolValue)
            } else if let stringValue = try? container.decode(String.self) {
                return .string(stringValue)
            } else if let listValue = try? container.decode([AUIAnyType].self) {
                return .list(listValue)
            } else if let dictionaryValue = try? container.decode([String: AUIAnyType].self) {
                return .dictionary(dictionaryValue)
            } else {
                throw DecodingError.dataCorrupted(DecodingError.Context(codingPath: container.codingPath, debugDescription: "Unable to decode AUIAnyType"))
            }
        }
        
        self = try result.get()
    }

     public func encode(to encoder: Encoder) throws {
         var container = encoder.singleValueContainer()
         switch self {
         case .uint(let uint): try container.encode(uint)
         case .int(let int): try container.encode(int)
         case .uint64(let uint): try container.encode(uint)
         case .int64(let int): try container.encode(int)
         case .bool(let bool): try container.encode(bool)
         case .string(let string): try container.encode(string)
         case .list(let list): try container.encode(list)
         case .dictionary(let dictionary): try container.encode(dictionary)
         }
     }

    static func ==(_ lhs: AUIAnyType, _ rhs: AUIAnyType) -> Bool {
        switch (lhs, rhs) {
        case (.uint(let uint1), .int(let uint2)): return uint1 == uint2
        case (.int(let int1), .int(let int2)): return int1 == int2
        case (.uint64(let uint1), .uint64(let uint2)): return uint1 == uint2
        case (.int64(let int1), .int64(let int2)): return int1 == int2
        case (.bool(let bool1), .bool(let bool2)): return bool1 == bool2
        case (.string(let string1), .string(let string2)): return string1 == string2
        case (.list(let list1), .list(let list2)): return list1 == list2
        case (.dictionary(let dict1), .dictionary(let dict2)): return dict1 == dict2
        default: return false
        }
    }
    
    public init(array: [Any]) {
        var typeArray: [AUIAnyType] = []
        array.forEach { value in
            if let v = value as? UInt {
                typeArray.append(.uint(v))
            } else if let v = value as? Int {
                typeArray.append(.int(v))
            } else if let v = value as? UInt64 {
                typeArray.append(.uint64(v))
            } else if let v = value as? Int64 {
                typeArray.append(.int64(v))
            } else if let v = value as? Bool {
                typeArray.append(.bool(v))
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
            if let v = value as? UInt {
                typeMap[key] = .uint(v)
            } else if let v = value as? Int {
                typeMap[key] = .int(v)
            } else if let v = value as? UInt64 {
                typeMap[key] = .uint64(v)
            } else if let v = value as? Int64 {
                typeMap[key] = .int64(v)
            } else if let v = value as? Bool {
                typeMap[key] = .bool(v)
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
        case .uint(let uint):
            return uint
        case .int(let int):
            return int
        case .uint64(let uint):
            return uint
        case .int64(let int):
            return int
        case .bool(let bool):
            return bool
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
