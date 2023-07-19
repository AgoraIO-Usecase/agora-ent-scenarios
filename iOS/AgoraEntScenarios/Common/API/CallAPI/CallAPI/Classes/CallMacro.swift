//
//  CallMacro.swift
//  CallAPI
//
//  Created by wushengtao on 2023/6/12.
//

import Foundation

#if DEBUG
let formatter = DateFormatter()
#endif

func callPrint(_ message: String) {
#if DEBUG
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    print("\(timeString) [CallApi]\(message)")
#endif
}
