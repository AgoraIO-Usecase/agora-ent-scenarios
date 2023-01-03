//
//  AgoraEntLog.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/1/3.
//

import Foundation
import SwiftyBeaver

class AgoraEngLog {
    static func createLog(moduleName: String) -> SwiftyBeaver.Type {
        let log = SwiftyBeaver.self
        
        // add log destinations. at least one is needed!
        let console = ConsoleDestination()
         // log to Xcode Console
        let file = FileDestination()  // log to default swiftybeaver.log file
        let dateString = NSDate().string(withFormat: "yyyy-MM-dd", timeZone: nil, locale: nil) ?? ""
        let dir = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.cachesDirectory,
                                                      FileManager.SearchPathDomainMask.userDomainMask, true).first
        let logDir = "\(dir ?? "")/agora_ent_logs"
        try? FileManager.default.createDirectory(at: URL(fileURLWithPath: logDir), withIntermediateDirectories: true)
        file.logFileURL = URL(fileURLWithPath: "\(logDir)/agora_ent_\(moduleName)_ios_\(dateString)_log.txt")
        
        // use custom format and set console output to short time, log level & message
        console.format = "[Agora][$L][\(moduleName)][$X]$Dyyyy-MM-DD HH:mm:ss.SSS$d $M"
        file.format = console.format
        // or use this for JSON output: console.format = "$J"

        // add the destinations to SwiftyBeaver
        #if DEBUG
        log.addDestination(console)
        #endif
        log.addDestination(file)

        return log
    }
}
