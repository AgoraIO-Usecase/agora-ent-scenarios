//
//  DownloadManager.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/6.
//

import Foundation
import CommonCrypto

func asyncToMainThread(closure: @escaping (() -> Void)) {
    if Thread.isMainThread {
        closure()
        return
    }
    
    DispatchQueue.main.async {
        closure()
    }
}

class URLSessionDownloader: NSObject {
    private(set) var task: URLSessionDataTask
    private(set) var temporaryPath: String
    private(set) var destinationPath: String
    private var progressHandler: ((Double) -> Void)?
    private var completionHandler: ((URL?, Error?) -> Void)?
    private var resumeRange: UInt64 = 0
    
    private lazy var fileHandle: FileHandle = {
        let fm = FileManager.default
        if fm.fileExists(atPath: temporaryPath) == false {
            fm.createFile(atPath: temporaryPath, contents: nil, attributes: nil)
        }
        let hander = FileHandle(forWritingAtPath: temporaryPath)!
        hander.seekToEndOfFile()
        return hander
    }()
    
    deinit {
        closeFile()
        cancelTask()
    }
    
    required init(task: URLSessionDataTask,
                  currentLength: UInt64,
                  temporaryPath: String,
                  destinationPath: String,
                  progressHandler: @escaping (Double) -> Void,
                  completionHandler: @escaping (URL?, Error?) -> Void) {
        self.task = task
        self.resumeRange = currentLength
        self.temporaryPath = temporaryPath
        self.destinationPath = destinationPath
        self.progressHandler = progressHandler
        self.completionHandler = completionHandler
        super.init()
    }
    
    func write(_ data: Data, countOfBytesExpectedToReceive: Int64) {
        fileHandle.write(data)
        
        // 更新进度等操作
        let totolDownloaded = Double(fileHandle.offsetInFile)
        let resumeRangeDouble = Double(resumeRange)
        let totalExpected = Double(countOfBytesExpectedToReceive)
        let progress = totolDownloaded / (totalExpected + resumeRangeDouble)
        progressHandler?(progress)
    }
    
    func cancelTask() {
        task.cancel()
    }
    
    func closeFile() {
        fileHandle.closeFile()
    }
    
    func markCompletion(error: Error?) {
        asyncToMainThread {
            if let error = error {
                aui_error("download fail: \(error.localizedDescription)")
                self.completionHandler?(nil, error)
            } else {
                self.completionHandler?(URL(fileURLWithPath: self.temporaryPath), nil)
            }
        }
        
    }
}

public class DownloadManager: NSObject {
    private var session: URLSession?
    private var downloaderMap: [URL: URLSessionDownloader] = [:]
    private var unzipOpMap: [URL: UnzipManager] = [:]
    
    override init() {
        super.init()
        let configuration = URLSessionConfiguration.default
        configuration.httpMaximumConnectionsPerHost = 3
        self.session = URLSession(configuration: configuration, delegate: self, delegateQueue: nil)
        if let urlCache = URLSession.shared.configuration.urlCache {
            // 删除所有缓存
            urlCache.removeAllCachedResponses()
            // 或者根据特定的 URL 请求删除缓存
            // urlCache.removeCachedResponse(for: URLRequest(url: yourURL))
        }
    }
}

extension DownloadManager: URLSessionDataDelegate {
    public func urlSession(_ session: URLSession,
                           dataTask: URLSessionDataTask,
                           didReceive response: URLResponse,
                           completionHandler: @escaping (URLSession.ResponseDisposition) -> Void) {
        aui_info("----开始----\(response.expectedContentLength)---\(response.mimeType ?? "")")
//        totalLength = Int(response.expectedContentLength) + currentLength
        completionHandler(.allow)
        //completionHandler(.cancel)
    }
        
    public func urlSession(_ session: URLSession,
                           dataTask: URLSessionDataTask,
                           didReceive data: Data) {
        guard let url = dataTask.currentRequest?.url, let downloader = downloaderMap[url] else {
            return
        }
        downloader.write(data, countOfBytesExpectedToReceive: dataTask.countOfBytesExpectedToReceive)
    }
    
    public func urlSession(_ session: URLSession,
                           task: URLSessionTask,
                           didCompleteWithError error: Error?) {
        guard let url = task.currentRequest?.url, let downloader = downloaderMap[url] else {
            return
        }
        
        downloader.markCompletion(error: error)
//        if error == nil {
//            print("----下载完成-----")
//        }
        // if task.state == .completed {
        //     NSURLSessionTask.State.running = 0,
        //     NSURLSessionTask.State.suspended = 1,
        //     NSURLSessionTask.State.canceling = 2,
        //     NSURLSessionTask.State.completed = 3,
        // }
    }
}

extension DownloadManager: IAGDownloadManager {
    public func checkResource(destinationPath: String,
                              fileSize: UInt64,
                              md5: String?,
                              completionHandler: @escaping (NSError?) -> Void) {
        let fm = FileManager.default
        
        //如果是文件夹，文件夹存在且zip不存在，暂时用来表示该md5文件解压正确且完成了
        let totalSize = calculateTotalSize(destinationPath)
        aui_info("calc fileSize: \(totalSize)/\(fileSize) \(destinationPath)")
        //需要不小于原始文件大小
        if totalSize >= fileSize {
            asyncToMainThread {
                completionHandler(nil)
            }
            return
        }
        
        //再检查是不是文件，是文件先查是不是存在
        if !fm.fileExists(atPath: destinationPath) {
            asyncToMainThread {
                completionHandler(ResourceError.resourceNotFoundError(url: destinationPath))
            }
            return
        }
        
        //文件存在，检查md5
        guard let md5 = md5 else {
            aui_info("startDownload completion, file exist & without md5")
            asyncToMainThread {
                completionHandler(nil)
            }
            return
        }
        let queue = DispatchQueue.global(qos: .background)
        queue.async {
            let tempFileMD5 = calculateMD5(forFileAt: URL(fileURLWithPath: destinationPath)) ?? ""
            aui_info("check md5: '\(tempFileMD5)'-'\(md5)' \(destinationPath)")
            if md5 == tempFileMD5 {
                //md5一致，直接完成
                asyncToMainThread {
                    completionHandler(nil)
                }
                return
            }
//            do {
                //文件md5不同，移除（是否等下载完成再移除）
//                try fm.removeItem(atPath: destinationPath)
//                    try fm.removeItem(atPath: temporaryPath)
//            } catch {
//                aui_error("remove exist file fail: \(destinationPath)")
//            }
            
            asyncToMainThread {
                completionHandler(ResourceError.md5MismatchError(msg: destinationPath))
            }
        }
    }
    
    public func cancelDownloadFile(withURL url: URL) {
        guard let downloader = downloaderMap[url] else {return}
        downloaderMap[url] = nil
    }
    
    public func startDownloadFile(withURL url: URL,
                                  md5: String?,
                                  destinationPath: String,
                                  progressHandler: @escaping (Double) -> Void,
                                  completionHandler: @escaping (URL?, NSError?) -> Void) {
        //如果正在下载，返回错误，后续是否支持一个task多个progressHandler/completionHandler
        if let _ = self.downloaderMap[url] {
            completionHandler(nil, ResourceError.resourceDownloadingAlreadyError(url: url.absoluteString))
            return
        }
        
        let fm = FileManager.default
        if fm.fileExists(atPath: destinationPath) {
            checkResource(destinationPath: destinationPath, 
                          fileSize: 1,
                          md5: md5) { err in
                guard let err = err else {
                    completionHandler(URL(string: destinationPath), nil)
                    return
                }
                
                if abs(err.code) == ResourceError.md5Mismatch.rawValue {
                    do {
//                        文件md5不同，移除（是否等下载完成再移除）
                        try fm.removeItem(atPath: destinationPath)
                    } catch {
                        aui_error("remove exist file fail: \(destinationPath)")
                    }
                }
                self.startDownloadFile(withURL: url,
                                       md5: md5,
                                       destinationPath: destinationPath,
                                       progressHandler: progressHandler,
                                       completionHandler: completionHandler)
            }
            return
        }
        
        let temporaryDirectoryURL = FileManager.default.temporaryDirectory
        let temporaryPath = "\(temporaryDirectoryURL.path)/\(md5 ?? NSUUID().uuidString)"
        let currentLength = fileSize(atPath: temporaryPath)
        
        var request = URLRequest(url: url)
        //临时文件存在，计算range断点续传
        if currentLength > 0 {
            let range = "bytes=\(currentLength)-"
            request.setValue(range, forHTTPHeaderField: "Range")
            aui_info("resume download from range[\(currentLength)] url: \(url.absoluteString)")
        }
        let task = session!.dataTask(with: request)
        task.resume()
        
        let downloader = URLSessionDownloader(task: task,
                                              currentLength: currentLength,
                                              temporaryPath: temporaryPath,
                                              destinationPath: destinationPath) { progress in
            
            aui_debug("download progress: \(Int(progress * 100))% url: \(url.absoluteString)")
            progressHandler(progress)
        } completionHandler: { [weak self] localUrl, err in
            guard let self = self else { return }
            self.downloaderMap.removeValue(forKey: url)
            let localPath = localUrl?.path ?? ""
            if let err = err {
                aui_error("post download error: \(err.localizedDescription) localPath: \(localPath)")
                completionHandler(nil, err as NSError)
                return
            }
            
            //后处理，比对md5，移动临时目录到正确目录
            self.postDownloadProcessing(tempFilePath: temporaryPath,
                                        targetFilePath: destinationPath,
                                        md5: md5) { err in
                if let err = err {
                    aui_error("post download error: \(err.localizedDescription) localPath: \(localPath)")
                    completionHandler(nil, err as NSError)
                } else {
                    aui_info("post download success, localPath: \(localPath)")
                    completionHandler(URL(string: destinationPath), nil)
                }
            }
        }

        downloaderMap[url] = downloader
    }
    
    public func startDownloadZip(withURL url: URL,
                                 fileSize: UInt64,
                                 md5: String,
                                 destinationFolderPath: String,
                                 progressHandler: @escaping (Double) -> Void,
                                 completionHandler: @escaping (URL?, NSError?) -> Void) {
        let temporaryDirectoryURL = FileManager.default.temporaryDirectory
        let destinationZipPath = "\(temporaryDirectoryURL.path)/\(md5).zip"
        
        //文件夹存在且zip不存在，并且解压的资源大小不小于解压前的大小，暂时用来表示该md5文件解压正确且完成了
        if calculateTotalSize(destinationFolderPath) >= fileSize,
           !FileManager.default.fileExists(atPath: destinationZipPath) {
            
            completionHandler(URL(string: destinationFolderPath), nil)
            return
        }
        
        //TODO: 进度暂时按照下载80%，解压20%分配
        startDownloadFile(withURL: url,
                          md5: md5,
                          destinationPath: destinationZipPath) { percent in
            progressHandler(percent * 0.8)
        } completionHandler: {[weak self] localUrl, err in
            guard let self = self else { return }
            if let err = err {
                completionHandler(nil, err)
                return
            }
            
            let manager = UnzipManager()
            self.unzipOpMap[url] = manager
            let queue = DispatchQueue.global(qos: .default)
            queue.async {
                //先清理目标目录内容
                cleanDirectory(atPath: destinationFolderPath)
                //先解压到目标路径，防止解压中途crash等异常中断
                let tempFolderPath = "\(destinationFolderPath)_temp"
                try? FileManager.default.removeItem(atPath: tempFolderPath)
                try? FileManager.default.moveItem(atPath: destinationFolderPath, toPath: tempFolderPath)
                
                let date = Date()
                let ret = manager.unzipFile(zipFilePath: destinationZipPath,
                                  destination: tempFolderPath) { percent in
                    aui_debug("unzip progress: \(percent) file: \(destinationFolderPath)")
                    progressHandler(percent * 0.2 + 0.8)
                }
                
                aui_benchmark("file: \(destinationFolderPath) unzip completion", cost: -date.timeIntervalSinceNow)
                aui_info("unzip comletion[\(ret)] folderPath: \(destinationFolderPath)")
                let err = ret ? nil : NSError(domain: "unzip fail", code: -1)
                if ret {
                    try? FileManager.default.moveItem(atPath: tempFolderPath, toPath: destinationFolderPath)
                    //解压完成移除zip文件
                    try? FileManager.default.removeItem(atPath: destinationZipPath)
                }
                asyncToMainThread {
                    self.unzipOpMap.removeValue(forKey: url)
                    completionHandler(URL(string: destinationFolderPath), err)
                }
            }
        }
    }
    
    public func cancelDownload(forURL url: URL) {
        if let downloader = downloaderMap[url] {
            downloader.cancelTask()
            downloaderMap.removeValue(forKey: url)
        }
        
        if let unzipOp = unzipOpMap[url] {
            unzipOp.cancelUnzip()
            unzipOpMap.removeValue(forKey: url)
        }
    }
    public func isDownloading(forUrl url: URL) -> Bool {
        return downloaderMap[url] == nil ? false : true
    }
}

extension DownloadManager {
    private func postDownloadProcessing(tempFilePath: String,
                                        targetFilePath: String,
                                        md5: String?,
                                        completion: @escaping (Error?) -> Void) {
        let queue = DispatchQueue.global(qos: .background)
        queue.async {
            let fileManager = FileManager.default
            
            do {
                // 计算临时文件的MD5
                var isValideFile = true
                if let md5 = md5 {
                    let tempFileMD5 = calculateMD5(forFileAt: URL(fileURLWithPath: tempFilePath)) ?? ""
                    aui_info("check md5: '\(tempFileMD5)'-'\(md5)' \(tempFilePath)")
                    isValideFile = tempFileMD5 == md5 ? true : false
                }
                
                // 检查MD5是否正确
                if isValideFile {
                    //临时路径和目标路径不一致
                    if tempFilePath != targetFilePath {
                        // 删除目标路径上已存在的文件
                        if fileManager.fileExists(atPath: targetFilePath) {
                            try fileManager.removeItem(atPath: targetFilePath)
                        }
                        
                        // 将临时文件移动到目标路径
                        try fileManager.moveItem(atPath: tempFilePath, toPath: targetFilePath)
                    }
                    
                    // 文件处理完成，返回成功结果
                    asyncToMainThread {
                        completion(nil)
                    }
                } else {
                    try fileManager.removeItem(atPath: tempFilePath)
                    // MD5 不匹配，返回错误结果
                    asyncToMainThread {
                        completion(ResourceError.md5MismatchError(msg: targetFilePath))
                    }
                }
            } catch {
                try? fileManager.removeItem(atPath: tempFilePath)
                // 文件处理过程中出现错误，返回错误结果
                asyncToMainThread {
                    completion(error)
                }
            }
        }
    }
}
