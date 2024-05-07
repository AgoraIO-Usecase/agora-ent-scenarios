//
//  Logger.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/6.
//

import Foundation

let logTag = "ResourceManager"
let formatter = DateFormatter()
func aui_info(_ message: String, tag: String = logTag) {
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    if let closure = AGResourceManagerContext.shared.displayLogClosure {
        closure("[\(tag)] \(message)")
        return
    }
    print("\(timeString)[\(tag)] \(message)")
}

func aui_warn(_ message: String, tag: String = logTag) {
    aui_info("[Warning]\(message)", tag: tag)
}

func aui_error(_ message: String, tag: String = logTag) {
    aui_info("[Error]\(message)", tag: tag)
}

func aui_benchmark(_ text: String, cost: Double, tag: String = logTag) {
    aui_info("[Benchmark]\(text): \(Int64(cost * 1000)) ms", tag: tag)
}

func aui_debug(_ message: String, tag: String = logTag) {
    #if DEBUG
    aui_info(message, tag: tag)
    #endif
}
