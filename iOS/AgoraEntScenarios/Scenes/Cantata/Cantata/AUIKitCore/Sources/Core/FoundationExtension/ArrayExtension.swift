//
//  ArrayExten.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

public extension Array {
    ///数组越界防护
    subscript(safe idx: Index) -> Element? {
        if idx < 0 { return nil }
        return idx < self.endIndex ? self[idx] : nil
    }
    
    subscript(safe range: Range<Int>) -> ArraySlice<Element>? {
        if range.startIndex < 0 { return nil }
        return range.endIndex <= self.endIndex ? self[range.startIndex...range.endIndex]:nil
    }
}
