//
//  Array+Extension.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2022/3/21.
//

import UIKit

extension Array {
    // 去重
    func filterDuplicates<E: Equatable>(_ filter: (Element) -> E) -> [Element] {
        var result = [Element]()
        for value in self {
            let key = filter(value)
            if !result.map({ filter($0) }).contains(key) {
                result.append(value)
            }
        }
        return result
    }
}
