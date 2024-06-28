//
//  Logger.swift
//  AgoraLyricsScore
//
//  Created by ZYP on 2023/2/3.
//

import Foundation
import AgoraComponetLog

// MARK:- ConsoleLogger

public class ConsoleLogger: NSObject, ILogger {
    let consoleFileLogger: AgoraComponetConsoleLogger!
    let domainName = "ATS"
    
    @objc public override init() {
        self.consoleFileLogger = AgoraComponetConsoleLogger(domainName: domainName)
        super.init()
    }
    
    @objc public func onLog(content: String,
                            tag: String?,
                            time: String,
                            level: LoggerLevel) {
        let newLevel = AgoraComponetLoggerLevel(rawValue: UInt(level.rawValue))!
        consoleFileLogger.onLog(withContent: content, tag: tag, time: time, level: newLevel)
    }
}

// MARK:- FileLogger

public class AGFileLogger: NSObject, ILogger {
    let componetFileLogger: AgoraComponetFileLogger!
    let filePrefixName = "agora.AgoraTranscriptSubtitle"
    let maxFileSizeOfBytes: UInt64 = 1024 * 1024 * 1
    let maxFileCount: UInt = 4
    let domainName = "ATS"
    
    @objc public override init() {
        self.componetFileLogger = AgoraComponetFileLogger(logFilePath: nil,
                                                          filePrefixName: filePrefixName,
                                                          maxFileSizeOfBytes: maxFileSizeOfBytes,
                                                          maxFileCount: maxFileCount,
                                                          domainName: domainName)
        super.init()
    }
    
    /// init
    /// - Parameter logFilePath: custom log file path.
    @objc public init(logFilePath: String) {
        componetFileLogger = AgoraComponetFileLogger(logFilePath: logFilePath,
                                                     filePrefixName: filePrefixName,
                                                     maxFileSizeOfBytes: maxFileSizeOfBytes,
                                                     maxFileCount: maxFileCount,
                                          
                                                     domainName: domainName)
    }
    
    @objc public func onLog(content: String,
                            tag: String?,
                            time: String,
                            level: LoggerLevel) {
        let newLevel = AgoraComponetLoggerLevel(rawValue: UInt(level.rawValue))!
        componetFileLogger.onLog(withContent: content, tag: tag, time: time, level: newLevel)
    }
}
