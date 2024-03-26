//
//  AgoraEntLog.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/1/3.
//

import Foundation
import SwiftyBeaver
@objc public class AgoraEntLogConfig: NSObject {
    var sceneName: String = ""
    var logFileMaxSize: Int = (2 * 1024 * 1024)
    
    public init(sceneName: String, logFileMaxSize: Int = 2 * 1024 * 1024) {
        super.init()
        self.sceneName = sceneName
        self.logFileMaxSize = logFileMaxSize
    }
}

@objc public class AgoraEntLog: NSObject {
    
    private static var currentLogKey = ""
    
    public static func currentLogger(with defaultKey: String) -> SwiftyBeaver.Type {
        if currentLogKey.isEmpty {
            return getSceneLogger(with: defaultKey)
        }
        
        let logger = SwiftyBeaver.self
        return logger
    }
    
    public static func getSceneLogger(with key: String) -> SwiftyBeaver.Type {
        let logger = SwiftyBeaver.self
        if key == currentLogKey {
            return logger
        }
        logger.removeAllDestinations()
        createLog(config: AgoraEntLogConfig.init(sceneName: key))
        currentLogKey = key
        return logger
    }
    
    public static func createLog(config: AgoraEntLogConfig) -> SwiftyBeaver.Type {
        let log = SwiftyBeaver.self
        
        // add log destinations. at least one is needed!
        let console = ConsoleDestination()
         // log to Xcode Console
        let file = FileDestination()  // log to default swiftybeaver.log file
       // let dateString = NSDate().string(withFormat: "yyyy-MM-dd", timeZone: nil, locale: nil) ?? ""
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let dateString = dateFormatter.string(from: Date())
        let logDir = logsDir()
        file.logFileURL = URL(fileURLWithPath: "\(logDir)/agora_ent_\(config.sceneName)_ios_\(dateString)_log.log")
        
        // use custom format and set console output to short time, log level & message
        console.format = "$Dyyyy-MM-dd HH:mm:ss.SSS[Agora][$L][\(config.sceneName)][$X]$d $M"
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
    
    @objc public static func logsDir() ->String {
        let dir = cacheDir()
        let logDir = "\(dir)/agora_ent_logs"
        try? FileManager.default.createDirectory(at: URL(fileURLWithPath: logDir), withIntermediateDirectories: true)
        
        return logDir
    }
}
