//
//  AUIReceipt.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/27.
//

import Foundation

struct AUIReceipt {
    var closure: ((NSError?)-> ())?
    var uniqueId: String
    let startDate: Date = Date()
}
