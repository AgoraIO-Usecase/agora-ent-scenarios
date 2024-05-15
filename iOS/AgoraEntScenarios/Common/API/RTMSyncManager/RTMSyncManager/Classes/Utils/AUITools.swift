//
//  AUITools.swift
//  FBSnapshotTestCase
//
//  Created by wushengtao on 2024/1/23.
//

import Foundation

let logTag = "SyncManager"
let formatter = DateFormatter()
func aui_info(_ message: String, levelTag: String? = nil, tag: String? = nil) {
    let visibleTag = "\(tag == nil ? "" : "[\(tag!)]")\(levelTag == nil ? "" : "[\(levelTag!)]")[\(logTag)]"
    if let closure = AUIRoomContext.shared.displayLogClosure {
        closure("\(visibleTag) \(message)")
        return
    }
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    print("\(timeString)[\(visibleTag)] \(message)")
}

func aui_warn(_ message: String, tag: String? = nil) {
    aui_info(message, levelTag: "Warning", tag: tag)
}

func aui_error(_ message: String, tag: String? = nil) {
    aui_info(message, levelTag: "Error", tag: tag)
}

func aui_benchmark(_ text: String, cost: Double, tag: String? = nil) {
    aui_info("\(text): \(Int64(cost * 1000)) ms", levelTag: "Benchmark", tag: tag)
}
