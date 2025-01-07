//
//  KTVSoundLoader.swift
//  AgoraEntScenarios
//
//  Created by Jonathan on 2024/6/21.
//

import Foundation
import YYModel
import AFNetworking

@objc public class DHCSongModel: NSObject {
    @objc public var lyric: String = ""
    @objc public var music: String = ""
    @objc public var name: String = ""
    @objc public var singer: String = ""
    @objc public var songCode: String = ""
}

@objc class KTVSoundLoader: NSObject {
    
    private var downloadTask: URLSessionDownloadTask? = nil
    
    var sounds = [DHCSongModel]()
    
    func getLyricURL(songCode: Int) -> String? {
        let music = sounds.first(where: {Int($0.songCode) == songCode})
        return music?.lyric
    }
    
    func getMusicPath(songCode: Int) -> String? {
        let music = sounds.first(where: {Int($0.songCode) == songCode})
        guard let name = music?.music.sl_fileName else {
            return nil
        }
        return NSURL(fileURLWithPath: String.sl_cacheFolderPath() + "/" + name).path
    }
    
    private func getMusicURL(songCode: Int) -> String? {
        let url = sounds.first(where: {Int($0.songCode) == songCode})
        return url?.music
    }
    
    @objc func fetchSongList(complete: ((_ list: [DHCSongModel]) -> Void)?) {
        if sounds.count > 0 {
            complete?(sounds)
        } else {
            VLSongListNetworkModel().request { e, rsp in
                guard let rsp = rsp as? VLResponseData,
                      rsp.code == 0,
                      let data = rsp.data as? [String: Any],
                      let songs = data["songs"]
                else {
                    complete?(self.sounds)
                    return
                }
                if let s = NSArray.yy_modelArray(with: DHCSongModel.self, json: songs) as? [DHCSongModel] {
                    self.sounds = s
                }
                complete?(self.sounds)
            }
        }
    }
    
    func isSongLoading(songCode: Int) -> Bool {
        let url = getMusicURL(songCode: songCode)
        return downloadTask?.currentRequest?.url?.absoluteString == url
    }
    
    func cancelDownload() {
        if (downloadTask != nil) {
            downloadTask?.cancel()
            downloadTask = nil
        }
    }
    
    @objc func preloadMusic(songCode: Int, onProgress: @escaping((Double)->Void), onCompelete: @escaping(NSError?)->Void) {
        guard let musicURLStr = getMusicURL(songCode: songCode),
              let musicURL = URL(string: musicURLStr),
              let filePath = getMusicPath(songCode: songCode)
        else {
            return
        }
        if FileManager.default.fileExists(atPath: filePath) {
            onCompelete(nil)
        } else {
            guard let folderPath = NSURL(fileURLWithPath: String.cacheFolderPath()).path else {
                return
            }
            FileManager.createDirectoryIfNeeded(atPath: folderPath)
            let manager = AFHTTPSessionManager()
            downloadTask?.cancel()
            manager.responseSerializer = AFHTTPResponseSerializer()
            downloadTask = manager.downloadTask(with: URLRequest(url: musicURL), progress: { (progress) in
                onProgress(progress.fractionCompleted)
            }, destination: { (url, response) -> URL in
                return URL(fileURLWithPath: filePath)
            }, completionHandler: { (response, url, error) in
                if let error = error {
                    onCompelete(error as NSError)
                } else {
                    onCompelete(nil)
                }
            })
            downloadTask?.resume()
        }
    }
}


private class VLSongListNetworkModel: VLCommonNetworkModel {
        
    public override init() {
        super.init()
        host = AppContext.shared.baseServerUrl
        interfaceName = "toolbox/v1/ktv/songs"
        method = .get
    }
}

extension String {
    
    fileprivate static func sl_cacheFolderPath() -> String {
        return NSHomeDirectory().appending("/Library").appending("/KTVMusic")
    }
    
    fileprivate var sl_fileName: String {
        components(separatedBy: "/").last ?? ""
    }
}

extension FileManager {
    fileprivate static func createDirectoryIfNeeded(atPath path: String) {
        let fileManager = FileManager.default
        var isDirectory: ObjCBool = false
        if fileManager.fileExists(atPath: path, isDirectory: &isDirectory) {
            if !isDirectory.boolValue {
                return
            }
        } else {
            do {
                try fileManager.createDirectory(atPath: path, withIntermediateDirectories: true, attributes: nil)
            } catch {
            }
        }
    }
}
