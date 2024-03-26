//
//  IAGDownloadManager.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/8.
//

import Foundation

public protocol IAGDownloadManager: NSObjectProtocol {
    
    
    /// 取消下载
    /// - Parameter url: <#url description#>
    func cancelDownloadFile(withURL url: URL)
    
    /// 下载文件，并校验md5
    /// - Parameters:
    ///   - url: <#url description#>
    ///   - md5: <#md5 description#>
    ///   - destinationPath: <#destinationPath description#>
    ///   - progressHandler: <#progressHandler description#>
    ///   - completionHandler: <#completionHandler description#>
    func startDownloadFile(withURL url: URL,
                           md5: String?,
                           destinationPath: String,
                           progressHandler: @escaping (Double) -> Void,
                           completionHandler: @escaping (URL?, NSError?) -> Void)
    
    
    /// 下载zip文件，下载完成解压到指定目录
    /// - Parameters:
    ///   - url: <#url description#>
    ///   - md5: <#md5 description#>
    ///   - destinationFolderPath: <#destinationFolderPath description#>
    ///   - progressHandler: <#progressHandler description#>
    ///   - completionHandler: <#completionHandler description#>
    func startDownloadZip(withURL url: URL,
                          md5: String,
                          destinationFolderPath: String,
                          progressHandler: @escaping (Double) -> Void,
                          completionHandler: @escaping (URL?, NSError?) -> Void)
    
    
    
    /// 检查资源是否需要下载
    /// - Parameters:
    ///   - url: <#url description#>
    ///   - completionHandler: <#completionHandler description#>
    func checkResource(destinationPath: String, 
                       md5: String?,
                       completionHandler: @escaping (NSError?) -> Void)
    
    /// 取消下载任务
    /// - Parameter url: <#url description#>
    func cancelDownload(forURL url: URL)
    
    
    /// 是否正在下载
    /// - Parameter url: <#url description#>
    /// - Returns: <#description#>
    func isDownloading(forUrl url: URL) -> Bool
}
