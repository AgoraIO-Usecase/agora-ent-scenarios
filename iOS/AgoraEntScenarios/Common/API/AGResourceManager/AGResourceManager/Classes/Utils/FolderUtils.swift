//
//  FolderUtils.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/6.
//

import Foundation
import CryptoKit

#if DEBUG
public func cleanAllResorce() {
    guard let cacheDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first else { return }
    let subdirectoryURL = cacheDirectory.appendingPathComponent("resourceManager")
    cleanDirectory(atPath: subdirectoryURL.path)
}
#endif

func getResourceCachePath(relativePath: String) -> String? {
    if let cacheDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first {
        let subdirectoryURL = cacheDirectory.appendingPathComponent("resourceManager/\(relativePath)")
        
        do {
            try FileManager.default.createDirectory(at: subdirectoryURL, 
                                                    withIntermediateDirectories: true,
                                                    attributes: nil)
        return subdirectoryURL.path
        } catch {
            aui_error("Error creating subdirectory: \(error.localizedDescription)")
            return subdirectoryURL.path
        }
    }
    return nil
}

func cleanDirectory(atPath path: String, excludeFiles: [String] = []) {
    let fileManager = FileManager.default
    do {
        let files = try fileManager.contentsOfDirectory(atPath: path)
        for file in files {
            if excludeFiles.contains(file) {continue}
            let filePath = URL(fileURLWithPath: path).appendingPathComponent(file).path
            try fileManager.removeItem(atPath: filePath)
            aui_info("Deleted file: \(filePath)")
        }
        aui_info("All files in directory \(path) have been deleted.")
    } catch {
        aui_error("Error clearing directory: \(error.localizedDescription)")
    }
}

func calculateTotalSize(_ folderPath: String) -> UInt64 {
    let fileManager = FileManager.default
    var totalSize: UInt64 = 0
    
    guard let files = fileManager.enumerator(atPath: folderPath) else {
        return totalSize
    }
    
    for case let file as String in files {
        let filePath = URL(fileURLWithPath: folderPath).appendingPathComponent(file).path
        
        do {
            let attributes = try fileManager.attributesOfItem(atPath: filePath)
            if let fileSize = attributes[.size] as? UInt64 {
                totalSize += fileSize
            }
        } catch {
            print("Error calculating size of file \(filePath): \(error.localizedDescription)")
        }
    }
    
    return totalSize
}

func calculateMD5(forFileAt url: URL) -> String? {
    let bufferSize = 4096 // 定义缓冲区大小为4KB
    var fileHash = Insecure.MD5()

    do {
        let date = Date()
        let fileHandle = try FileHandle(forReadingFrom: url)

        defer {
            fileHandle.closeFile()
        }

        while autoreleasepool(invoking: {
            let data = fileHandle.readData(ofLength: bufferSize)
            if !data.isEmpty {
                fileHash.update(data: data)
                return true
            } else {
                return false
            }
        }) {}
        
        let digest = fileHash.finalize()
        let md5String = digest.map { String(format: "%02hhx", $0) }.joined()
        aui_benchmark("calculate file \(url.relativePath) md5[\(md5String)] success", cost: -date.timeIntervalSinceNow)
        
        return md5String
    } catch {
        print("Error while calculating MD5: \(error.localizedDescription)")
        return nil
    }
}

func fileSize(atPath path: String) -> UInt64 {
    var currentLength: UInt64 = 0
    if let attributes = try? FileManager.default.attributesOfItem(atPath: path) {
        currentLength = attributes[FileAttributeKey.size] as? UInt64 ?? 0
    }
    
    return currentLength
}

func folderSize(atPath path: String) -> UInt64 {
    let fileManager = FileManager.default
    var totalSize: UInt64 = 0
    
    // 使用fileManager来枚举指定路径下的所有内容
    if let enumerator = fileManager.enumerator(at: URL(fileURLWithPath: path), 
                                               includingPropertiesForKeys: [.fileSizeKey],
                                               options: [.skipsHiddenFiles],
                                               errorHandler: { (url, error) -> Bool in
        aui_error("'folderSize' enumerator fail: \(error) in \(url) ")
        return true
    }) {
        for case let fileURL as URL in enumerator {
            do {
                // 获取每个文件的属性，并累加文件大小
                let fileAttributes = try fileURL.resourceValues(forKeys: [.fileSizeKey])
                if let fileSize = fileAttributes.fileSize {
                    totalSize += UInt64(fileSize)
                }
            } catch {
                aui_error("'folderSize'  get file size fail: \(error) in \(fileURL)")
            }
        }
    }
    
    return totalSize
}
