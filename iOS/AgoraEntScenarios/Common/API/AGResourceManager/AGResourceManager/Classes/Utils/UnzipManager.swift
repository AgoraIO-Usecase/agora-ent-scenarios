//
//  UnzipManager.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/20.
//

import SSZipArchive

class UnzipManager: NSObject {
    private var isCancel: Bool = false
    private var unzipProgress: ((Double)->Void)?
    
    func unzipFile(zipFilePath: String,
                   destination: String,
                   progress: ((Double)->Void)?) -> Bool {
        self.unzipProgress = progress
        let ret = SSZipArchive.unzipFile(atPath: zipFilePath, toDestination: destination, delegate: self)
        aui_info("unzipFile ret: \(ret) \(destination)")
        return ret
    }
    
    func cancelUnzip() {
        isCancel = true
    }
}

extension UnzipManager: SSZipArchiveDelegate {
    func zipArchiveProgressEvent(_ loaded: UInt64, total: UInt64) {
        let progress = Double(loaded) / Double(total)
        unzipProgress?(progress)
    }
    
    func zipArchiveShouldUnzipFile(at fileIndex: Int, totalFiles: Int, archivePath: String, fileInfo: unz_file_info) -> Bool {
        return isCancel ? false : true
    }
}
