//
//  ResourceError.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/8.
//

import Foundation


enum ResourceError: Int {
    case unknown = 1000  //未知错误
    case urlInvalide  //url异常
    case resourceNotFound  //本地找不到文件
    case resourceDownloadingAlready    //正在下载
    case md5Mismatch   //本地文件md5不匹配
    
    func toNSError(errorStr: String) -> NSError {
        return NSError(domain: "ResourceManager",
                       code: -self.rawValue,
                       userInfo: [NSLocalizedDescriptionKey: errorStr])
    }
    
    static func unknownError() -> NSError {
        return ResourceError.unknown.toNSError(errorStr: "unknown error")
    }
    
    static func urlInvalidError(url: String) -> NSError {
        return ResourceError.urlInvalide.toNSError(errorStr: "download url(\(url) invalide")
    }
    
    static func resourceNotFoundError(url: String) -> NSError {
        return ResourceError.urlInvalide.toNSError(errorStr: "resource[\(url)] not found")
    }
    
    static func resourceDownloadingAlreadyError(url: String) -> NSError {
        return ResourceError.resourceDownloadingAlready.toNSError(errorStr: "downloading url(\(url) already")
    }
    
    static func md5MismatchError(msg: String) -> NSError {
        return ResourceError.md5Mismatch.toNSError(errorStr: "md5 missmatch \(msg)")
    }
}
