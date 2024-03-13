//
//  AUITools.swift
//  FBSnapshotTestCase
//
//  Created by wushengtao on 2024/1/23.
//

import Foundation

let logTag = "SyncManager"
let formatter = DateFormatter()
func aui_info(_ message: String, tag: String = logTag) {
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
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
