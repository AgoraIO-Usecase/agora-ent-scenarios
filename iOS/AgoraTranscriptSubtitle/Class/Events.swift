//
//  Events.swift
//  NewApi
//
//  Created by ZYP on 2022/11/22.
//

import Foundation

/// 日志协议
@objc public protocol ILogger {
    /// 日志输出
    /// - Note: 在子线程执行
    /// - Parameters:
    ///   - content: 内容
    ///   - tag: 标签
    ///   - time: 时间
    ///   - level: 等级
    @objc func onLog(content: String, tag: String?, time: String, level: LoggerLevel)
}

@objc public enum LoggerLevel: UInt8, CustomStringConvertible {
    case debug, info, warning, error
    
    public var description: String {
        switch self {
        case .debug:
            return "D"
        case .info:
            return "I"
        case .warning:
            return "W"
        case .error:
            return "E"
        }
    }
}
