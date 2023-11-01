//
//  AUILogger.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation
import SwiftyBeaver


let commonLogger = AgoraEntCommonLog.createLog(config: AgoraEntCommonLogConfig())
public func agoraEnt_info(_ text: String, tag: String = "AgoraEntCommon") {
    commonLogger.info(text, context: tag)
}

public func agoraEnt_warn(_ text: String, tag: String = "AgoraEntCommon") {
    commonLogger.warning(text, context: tag)
}

public func agoraEnt_error(_ text: String, tag: String = "AgoraEntCommon") {
    commonLogger.error(text, context: tag)
}

@objc class AgoraEntCommonLogConfig: NSObject {
    var logFileMaxSize: Int = (2 * 1024 * 1024)
}

@objc public class AgoraEntCommonLog: NSObject {
    static let formatter = DateFormatter()
    fileprivate static func _dateFormat() ->String {
        formatter.dateFormat = "yyyy-MM-dd"
        
        return formatter.string(from: Date())
        
    }
    
    static func createLog(config: AgoraEntCommonLogConfig) -> SwiftyBeaver.Type {
        let log = SwiftyBeaver.self
        
        // add log destinations. at least one is needed!
        let console = ConsoleDestination()
         // log to Xcode Console
        let file = FileDestination()  // log to default swiftybeaver.log file
        let dateString = _dateFormat()
        let logDir = logsDir()
        file.logFileURL = URL(fileURLWithPath: "\(logDir)/AgoraEntCommon_ios_\(dateString)_log.txt")
        
        // use custom format and set console output to short time, log level & message
        console.format = "[AgoraEntCommon][$L][$X]$Dyyyy-MM-dd HH:mm:ss.SSS$d $M"
        file.format = console.format
        file.logFileMaxSize = config.logFileMaxSize
        file.logFileAmount = 4
        // or use this for JSON output: console.format = "$J"

        // add the destinations to SwiftyBeaver
        #if DEBUG
        log.addDestination(console)
        #endif
        log.addDestination(file)

        return log
    }
    
    @objc public static func cacheDir() ->String {
        let dir = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.cachesDirectory,
                                                      FileManager.SearchPathDomainMask.userDomainMask, true).first
        return dir ?? ""
    }
    
    @objc static func logsDir() ->String {
        let dir = cacheDir()
        let logDir = "\(dir)/AgoraEntCommon_logs"
        try? FileManager.default.createDirectory(at: URL(fileURLWithPath: logDir), withIntermediateDirectories: true)
        
        return logDir
    }
}
