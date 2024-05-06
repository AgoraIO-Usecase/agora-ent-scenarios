//
//  AUIMapHandler.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/16.
//

import Foundation

func mergeMap(origMap: [String: Any], newMap: [String: Any]) -> [String: Any] {
    var _origMap = origMap
    newMap.forEach { (k, v) in
        if let dic = v as? [String: Any] {
            let origDic: [String: Any] = _origMap[k] as? [String: Any] ?? [:]
            let newDic = mergeMap(origMap: origDic, newMap: dic)
            _origMap[k] = newDic
        } else {
            //TODO: array ?
            _origMap[k] = v
        }
    }
    return _origMap
}

func calculateMap(origMap: [String: Any],
                  key: [String],
                  value: Int,
                  min: Int,
                  max: Int) -> [String: Any]? {
    var _origMap = origMap
    if key.count > 1 {
        let curKey = key.first ?? ""
        let subKey = Array(key.suffix(from: 1))
        
        guard let subValue = _origMap[curKey] as? [String: Any],
              let newMap = calculateMap(origMap: subValue,
                                        key: subKey,
                                        value: value,
                                        min: min,
                                        max: max) else {
            return nil
        }
        _origMap[curKey] = newMap
        return _origMap
    }
    guard let curKey = key.first, let subValue = _origMap[curKey] as? Int else { return nil }
    let curValue = subValue + value
    guard curValue <= max, curValue >= min else {
        aui_info("calculateMap out of range")
        return nil
    }
    _origMap[curKey] = curValue
    
    return _origMap
}


/// 根据filter条件过滤出array命中的索引，如果filter为空，则默认选中所有
/// - Parameters:
///   - array: <#array description#>
///   - filter: <#filter description#>
/// - Returns: <#description#>
func getItemIndexes(array: [[String: Any]], filter: [[String: Any]]?) -> [Int]? {
    guard let filter = filter, filter.count > 0 else {
        let indexes = Array(array.indices)
        return indexes.isEmpty ? nil : indexes
    }
    
    func isMatchFilter(key: String, itemValue: [String: Any], filterValue: Any?)-> Bool {
        let valueV = itemValue[key]
        //only filter String/Bool/Int
        if let valueV = valueV as? String, let filterV = filterValue as? String {
            if valueV == filterV {
                return true
            }
        } else if let valueV = valueV as? Bool, let filterV = filterValue as? Bool {
            if valueV == filterV {
                return true
            }
        } else if let valueV = valueV as? Int, let filterV = filterValue as? Int {
            if valueV == filterV {
                return true
            }
        } else if let valueV = valueV as? [String: Any], 
                    let filterV = filterValue as? [String: Any],
                    filterV.keys.count == 1,
                    let filterVKey = filterV.keys.first {
            //next level match again
            return isMatchFilter(key: filterVKey, itemValue: valueV, filterValue: filterV[filterVKey])
        }
        
        return false
    }
    
    var indexes: [Int] = []
    for (i, value) in array.enumerated() {
        for filterItem in filter {
            var match = false
            for (k, v) in filterItem {
                if isMatchFilter(key: k, itemValue: value, filterValue: v) {
                    match = true
                    break
                }
            }
            if match {
                indexes.append(i)
                break
            }
        }
    }
    return indexes.isEmpty ? nil : indexes
}
