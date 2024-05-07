
import Foundation

public enum AGResourceStatus: Int {
    case invalid = 0      //本地不存在，远端资源异常
    case needDownload     //本地不存在，需要下载
    case needUpdate       //本地存在老版本，需要更新
    case downloading      //下载中
    case downloaded       //下载完成
}

public struct AGResource: Codable {
    public var url: String = ""               //zip 文件的下载链接
    public var uri: String = ""               //本地存放路径
    public var md5: String = ""               //文件一致性校验 和 文件更新检测
    public var size: Int64 = 0                //文件大小
    public var autodownload: Bool = true      //是否sdk启动时预先下载 默认true
    public var encrypt: Bool = false          //默认不加密，保证链路不能被第三方使用
    public var group: String = ""             //资源逻辑分组名
    
    enum CodingKeys: String, CodingKey {
        case url, uri, md5, size, autodownload, encrypt, group
    }
}

public struct AGManifest: Codable {
    public private(set) var files: [AGResource] = []
    var customMsg: String = ""    //user 自定义 custom 字符串的格式
    var timestamp: Int64 = 0      //配置文件生成时间
    
    enum CodingKeys: String, CodingKey {
        case files, customMsg, timestamp
    }
}


@objc public protocol IAGResourceManagerListener: NSObjectProtocol {
    func downloadProgress(url: URL, progress: Double)
    func downloadCompletion(url: URL, error: NSError?)
}

@objcMembers
public class AGResourceManager: NSObject {
    public static let shared: AGResourceManager = AGResourceManager()
    private var managerDelegates:NSHashTable<IAGResourceManagerListener> = NSHashTable<IAGResourceManagerListener>.weakObjects()
    private var manifestFileList: [AGResource] = []
    private var manifestMap: [String: AGManifest] = [:]
    private var resourceStatusMap: [String: AGResourceStatus] = [:]
    private lazy var downloadManager: IAGDownloadManager = DownloadManager()
    
    public func addDelegate(_ delegate: IAGResourceManagerListener) {
        managerDelegates.add(delegate)
    }
    
    public func removeDelegate(_ delegate: IAGResourceManagerListener) {
        managerDelegates.remove(delegate)
    }
    
    
    /// 下载manifeste 列表文件
    /// - Parameters:
    ///   - url: <#url description#>
    ///   - md5: <#md5 description#>
    ///   - progressHandler: <#progressHandler description#>
    ///   - completionHandler: <#completionHandler description#>
    public func downloadManifestList(url: String,
                                     md5: String? = nil,
                                     progressHandler: @escaping (Double) -> Void,
                                     completionHandler: @escaping ([AGResource]?, NSError?) -> Void) {
        guard let url = URL(string: url) else {
            DispatchQueue.main.async {
                completionHandler(nil, ResourceError.urlInvalidError(url: url))
            }
            return
        }
        let destinationPath = getResourceCachePath(relativePath: "manifest")!
        let targetFilePath = "\(destinationPath)/manifestList"
        let targetTempFilePath = "\(destinationPath)/manifestList.tmp"
        try? FileManager.default.removeItem(atPath: targetTempFilePath)
        downloadManager.startDownloadFile(withURL: url,
                                          md5: md5,
                                          destinationPath: targetTempFilePath,
                                          progressHandler: progressHandler) {[weak self] localUrl, err in
            guard let self = self else { return }
            
            func readManifestList(filePath: String) -> [AGResource]? {
                let jsonStr: String = (try? String(contentsOfFile: filePath)) ?? ""
                let jsonArray: [[String: Any]] = (decodeToJsonObj(jsonStr) as? [[String: Any]]) ?? []
                let fileList: [AGResource]? = decodeModelArray(jsonArray)
                
                return fileList
            }
            
            if let err = err {
                aui_warn("downloadManifestList fail, err: \(err.localizedDescription)")
                //如果失败，读取本地缓存
                if let list: [AGResource] = readManifestList(filePath: targetFilePath) {
                    aui_warn("downloadManifestList unsuccess, use cache")
                    self.manifestFileList = list
                    self.downloadAllManifestFiles(fileList: self.manifestFileList) {[weak self] error in
                        guard let self = self else { return }
                        completionHandler(self.manifestFileList, error)
                    }
                    completionHandler(self.manifestFileList, nil)
                } else {
                    completionHandler(self.manifestFileList, err)
                }
                return
            }
            
            try? FileManager.default.removeItem(atPath: targetFilePath)
            try? FileManager.default.moveItem(atPath: targetTempFilePath, toPath: targetFilePath)

            self.manifestFileList = readManifestList(filePath: targetFilePath) ?? []
            self.downloadAllManifestFiles(fileList: self.manifestFileList) {[weak self] error in
                guard let self = self else { return }
                completionHandler(self.manifestFileList, error)
            }
        }
    }
    
    
    /// 下载所有manifest文件
    /// - Parameters:
    ///   - fileList: <#fileList description#>
    ///   - completion: <#completion description#>
    public func downloadAllManifestFiles(fileList: [AGResource]?, completion: @escaping ((NSError?) -> Void)) {
        guard let fileList = fileList else {
            DispatchQueue.main.async {
                completion(nil)
            }
            return
        }
        
        let dispatchGroup = DispatchGroup()
        var error: NSError? = nil
        for manifest in fileList {
            dispatchGroup.enter()
            self.downloadManifest(manifest: manifest) { _ in
                
            } completionHandler: { _, err in
                //error handler
                if error == nil {
                    error = err
                }
                dispatchGroup.leave()
            }
        }
        
        dispatchGroup.notify(queue: DispatchQueue.main) {
            completion(error)
        }
    }
    
    /// 下载menifest
    /// - Parameters:
    ///   - manifest: <#manifest description#>
    ///   - progressHandler: <#progressHandler description#>
    ///   - completionHandler: <#completionHandler description#>
    public func downloadManifest(manifest: AGResource,
                                 progressHandler: @escaping (Double) -> Void,
                                 completionHandler: @escaping (AGManifest?, NSError?) -> Void) {
        guard let url = URL(string: manifest.url) else {
            DispatchQueue.main.async {
                completionHandler(nil, ResourceError.urlInvalidError(url: manifest.url))
            }
            return
        }
        let destinationPath = getPath(manifest: manifest)
        let uri = manifest.uri
        downloadManager.startDownloadFile(withURL: url,
                                          md5: manifest.md5,
                                          destinationPath: destinationPath,
                                          progressHandler: progressHandler) {[weak self] localUrl, err in
            guard let self = self else { return }
            
            func readManifest(filePath: String) -> AGManifest? {
                let jsonStr: String = (try? String(contentsOfFile: filePath)) ?? ""
                let jsonObj: [String: Any] = (decodeToJsonObj(jsonStr) as? [String: Any]) ?? [:]
                guard let manifest: AGManifest = decodeModel(jsonObj) else { return nil }
                self.manifestMap[uri] = manifest
                return manifest
            }
            
            if let err = err {
                aui_warn("downloadManifest fail, err: \(err.localizedDescription)")
                if let manifest: AGManifest = readManifest(filePath: destinationPath) {
                    aui_warn("downloadManifest unsuccess, use cache")
                    completionHandler(manifest, nil)
                } else {
                    completionHandler(nil, err)
                }
                return
            }
            
            guard let localUrl = localUrl else {
                aui_warn("downloadManifest success, url not found, use cache")
                let manifest: AGManifest? = readManifest(filePath: destinationPath)
                completionHandler(manifest, nil)
                return
            }
            
            if let manifest: AGManifest = readManifest(filePath: localUrl.path) {
                for resource in manifest.files {
                    if resource.autodownload == false {
                        self.checkResourceInvalid(resource: resource)
                        continue
                    }
                    //TODO: 增加mime type
                    self.downloadResource(resource: resource) { _ in
                        
                    } completionHandler: { path, er in
                        
                    }
                }
                completionHandler(manifest,  nil)
                return
            }
            completionHandler(nil,  nil)
        }
    }
    
    public func downloadResources(resources: [AGResource]?,
                                  progress: @escaping ((Double) -> Void),
                                  completion: @escaping ((NSError?) -> Void)) {
        guard let resources = resources, resources.count > 0 else {
            DispatchQueue.main.async {
                completion(nil)
            }
            return
        }
        
        let dispatchGroup = DispatchGroup()
        var error: NSError? = nil
        var downloadSizeMap: [String: Int64] = [:]
        var totalDownloadSize: Int64 = 0
        var totalSize: Int64 = 0
        for resource in resources {
            totalSize += resource.size
            dispatchGroup.enter()
            self.downloadResource(resource: resource) { precent in
                let downloadSize = Int64(Double(resource.size) * precent)
                downloadSizeMap[resource.uri] = downloadSize
                totalDownloadSize = 0
                downloadSizeMap.forEach { key, size in
                    totalDownloadSize += size
                }
                let totalPrecent = Double(totalDownloadSize) / Double(totalSize)
                progress(totalPrecent)
//                aui_debug("downloadResources: \(totalPrecent)")
            } completionHandler: { url, err in
                //error handler
                if error == nil {
                    error = err
                }
                dispatchGroup.leave()
            }
        }
        
        dispatchGroup.notify(queue: DispatchQueue.main) {
            completion(error)
        }
    }
    
    public func cancelDownloadResource(resource: AGResource) {
        guard let url = URL(string: resource.url) else {
            return
        }
        downloadManager.cancelDownload(forURL: url)
        if resourceStatusMap[resource.url] == .downloading {
            resourceStatusMap[resource.url] = .needDownload
        }
    }
    
    /// 下载资源
    /// - Parameters:
    ///   - resource: <#resource description#>
    ///   - progressHandler: <#progressHandler description#>
    ///   - completionHandler: <#completionHandler description#>
    public func downloadResource(resource: AGResource,
                                 progressHandler: @escaping (Double) -> Void,
                                 completionHandler: @escaping (String?, NSError?) -> Void) {
        guard let url = URL(string: resource.url) else {
            DispatchQueue.main.async {
                completionHandler(nil, ResourceError.urlInvalidError(url: resource.url))
            }
            return
        }
        let destinationFolderPath = getFolderPath(resource: resource)
        resourceStatusMap[resource.url] = .downloading
        downloadManager.startDownloadZip(withURL: url,
                                         md5: resource.md5,
                                         destinationFolderPath: destinationFolderPath,
                                         progressHandler: {[weak self] progress in
            progressHandler(progress)
            self?.notifyProgress(url: url, progress: progress)
        }, completionHandler: {[weak self] localUrl, err in
            guard let self = self else { return }
            defer {
                self.notifyCompletion(url: url, error: err)
            }
            if let err = err {
                self.downloadErrorHandler(url: url, error: err)
                completionHandler(nil, err)
                return
            }
            progressHandler(1)
            guard let path = localUrl?.path else {
                completionHandler(nil, nil)
                return
            }
            self.resourceStatusMap[resource.url] = .downloaded
            completionHandler(path,  nil)
            
            //下载完成，移除相同目录老的资源
            DispatchQueue.global(qos: .background).async {
                let cleanFolderPath = self.getFolderPath(resource: resource, includeMd5Folder: false)
                cleanDirectory(atPath: cleanFolderPath, excludeFiles: ["\(resource.md5)"])
            }
        })
    }
    
    
    /// 根据uri获取manifest
    /// - Parameter uri: <#uri description#>
    /// - Returns: <#description#>
    public func getManifest(uri: String) -> AGManifest? {
        let file = manifestFileList.first { $0.uri == uri }
        return manifestMap[file?.uri ?? ""]
    }
    
    
    //TODO: 是否增加key
    /// 根据uri获取资源对象
    /// - Parameter uri: <#uri description#>
    /// - Returns: <#description#>
    public func getResource(uri: String) -> AGResource? {
        for manifest in manifestMap.values {
            for resource in manifest.files {
                if resource.uri == uri {
                    return resource
                }
            }
        }
        
        return nil
    }
    
    
    /// 根据资源查询当前资源状态
    /// - Parameter resource: <#resource description#>
    /// - Returns: <#description#>
    public func getStatus(resource: AGResource) -> AGResourceStatus {
        if let status = resourceStatusMap[resource.url] {
            return status
        }
        
        return .invalid
    }
    
    
    /// 获取manifest的路径
    /// - Parameter manifest: <#manifest description#>
    /// - Returns: <#description#>
    public func getPath(manifest: AGResource) -> String {
        let filePath = getResourceCachePath(relativePath: "manifest/\(manifest.uri)")!
        return filePath
    }
    
    
    /// 获取资源目录
    /// - Parameter resource: <#resource description#>
    /// - Returns: <#description#>
    public func getFolderPath(resource: AGResource, includeMd5Folder: Bool = true) -> String {
        let extPath = includeMd5Folder ? "/\(resource.md5)" : ""
        let folderPath = getResourceCachePath(relativePath: "resource/\(resource.uri)\(extPath)")!
        return folderPath
    }
}


extension AGResourceManager {
    private func notifyProgress(url: URL, progress: Double) {
        asyncToMainThread {
            self.managerDelegates.allObjects.forEach { delegate in
                delegate.downloadProgress(url: url, progress: progress)
            }
        }
    }
    
    private func notifyCompletion(url: URL, error: NSError?) {
        asyncToMainThread {
            self.managerDelegates.allObjects.forEach { delegate in
                delegate.downloadCompletion(url: url, error: error)
            }
        }
    }
    
    private func checkResourceInvalid(resource: AGResource) {
        let destinationFolderPath = self.getFolderPath(resource: resource)
        downloadManager.checkResource(destinationPath: destinationFolderPath,
                                      md5: resource.md5) {[weak self] err in
            guard let self = self else {return}
            guard let error = err else {
                self.resourceStatusMap[resource.url] = .downloaded
                return
            }
            let errCode = ResourceError(rawValue: abs(error.code)) ?? .unknown
            
            if errCode == .resourceNotFound {
                self.resourceStatusMap[resource.url] = .needDownload
            } else if errCode == .md5Mismatch {
                self.resourceStatusMap[resource.url] = .needUpdate
            }
        }
    }
    
    private func downloadErrorHandler(url: URL, error: NSError) {
        let errorCode = ResourceError(rawValue: abs(error.code)) ?? .unknown
        let path = url.absoluteString
        switch errorCode {
        case .unknown:
            resourceStatusMap[path] = .invalid
        case .resourceNotFound:
            resourceStatusMap[path] = .needDownload
        case .md5Mismatch:
            break
        case .resourceDownloadingAlready:
            break
        case .urlInvalide:
            resourceStatusMap[path] = .invalid
        }
    }
}
