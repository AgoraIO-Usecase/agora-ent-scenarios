//
//  AgoraEntLog.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/1/3.
//

import Foundation
import SwiftyBeaver
import SSZipArchive

@objc public class AgoraEntLogConfig: NSObject {
    var sceneName: String = ""
    var logFileMaxSize: Int = (1 * 1024 * 1024)
    
    public init(sceneName: String, logFileMaxSize: Int = 1 * 1024 * 1024) {
        super.init()
        self.sceneName = sceneName
        self.logFileMaxSize = logFileMaxSize
    }
}

public func agoraDoMainThreadTask(_ task: (()->())?) {
    if Thread.current.isMainThread == false {
        DispatchQueue.main.async {
            task?()
        }
    }else{
        task?()
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
        let logDir = logsDir()
        file.logFileURL = URL(fileURLWithPath: "\(logDir)/agora_ent_\(config.sceneName).log")
        
        // use custom format and set console output to short time, log level & message
        console.format = "[$DMM/dd/yy HH:mm:ss.SSS$d][Agora][$L][\(config.sceneName)][$X]: $M"
        file.format = console.format
        file.logFileMaxSize = config.logFileMaxSize
        file.logFileAmount = 2
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
        let logDir = "\(dir)/agora_ent_log"
        try? FileManager.default.createDirectory(at: URL(fileURLWithPath: logDir), withIntermediateDirectories: true)
        
        return logDir
    }
    
    @objc public static func allLogsUrls() -> [URL] {
        let dir = cacheDir()
        var urls = [URL(fileURLWithPath: logsDir())]
        
        let dirUrl = URL(fileURLWithPath: dir)
        guard let directoryContents = try? FileManager.default.contentsOfDirectory(at: dirUrl, includingPropertiesForKeys: nil, options: []) else {
            return urls
        }
        //查找dir里所有文件名包含'agoraapi'、'agorartmsdk'、'agorasdk'的三类文件
        for fileURL in directoryContents {
            // 过滤出文件名包含'agoraapi'、'agorartmsdk'、'agorasdk'的文件
            let fileName = fileURL.lastPathComponent.lowercased()
            if fileName.contains("agoraapi") || fileName.contains("agorartmsdk") || fileName.contains("agorasdk") {
                urls.append(fileURL)
            }
        }
        
        return urls
    }
    
    private static func zipSceneLog(scene: String, completion: @escaping (String?, Error?) -> Void) {
        var logFiles = [String]()
        let logDir = logsDir()
        if let dirs = try? FileManager.default.contentsOfDirectory(atPath: logDir) {
            for file in dirs {
                if file.contains(scene) {
                    let filePath = "\(logDir)/\(file)"
                    logFiles.append(filePath)
                }
            }
        }
        let cacheDir = cacheDir()
        if let dirs = try? FileManager.default.contentsOfDirectory(atPath: cacheDir) {
            for file in dirs {
                // 过滤出文件名包含'agoraapi'、'agorartmsdk'、'agorasdk'的文件
                if file.contains("agoraapi") || file.contains("agorartmsdk") || file.contains("agorasdk") {
                    let filePath = "\(cacheDir)/\(file)"
                    logFiles.append(filePath)
                }
            }
        }
        guard logFiles.isEmpty == false else {
            completion(nil, nil)
            return
        }
        let zipFile = NSTemporaryDirectory() + "/log_\(UUID().uuidString).zip"
        do {
            SSZipArchive.createZipFile(atPath: zipFile, withFilesAtPaths: logFiles)
            completion(zipFile, nil)
        } catch {
            // 异常处理，回调错误闭包
            completion(nil, error)
        }
    }
    
    @objc public static func autoUploadLog(scene: String) {
        let zipStart = DispatchTime.now()
        print("[AgoraEntLog] autoUploadLog: func start t:\(zipStart)")
        DispatchQueue.global().async {
            guard AppContext.shared.sceneConfig?.logUpload == 1 else {
                return
            }
            print("[AgoraEntLog] autoUploadLog: zip start t:\(DispatchTime.now())")
            AgoraEntLog.zipSceneLog(scene: scene, completion: { str, err in
                print("[AgoraEntLog] autoUploadLog: zip end cost: \(zipStart.distance(to: DispatchTime.now()))")
                guard let filePath = str, let data = try? Data.init(contentsOf: URL(fileURLWithPath: filePath)) else {
                    return
                }
                print("[AgoraEntLog] autoUploadLog: upload start t:\(DispatchTime.now())")
                let req = AUIUploadNetworkModel()
                req.interfaceName = "/api-login/upload/log"
                req.fileData = data
                req.name = "file"
                req.mimeType = "application/zip"
                req.fileName = URL(fileURLWithPath: filePath).lastPathComponent
                req.upload { progress in
                    
                } completion: { err, content in
                    print("[AgoraEntLog] autoUploadLog: upload end cost:\(zipStart.distance(to: DispatchTime.now())) e: \(err?.localizedDescription)")
                    if let e = err {
                    }
                }
            })
        }
    }
}
