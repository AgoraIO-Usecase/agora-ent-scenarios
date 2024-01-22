//
//  AgoraDownLoadManager.swift
//  AgoraKaraokeScore
//
//  Created by zhaoyongqiang on 2021/12/10.
//

import UIKit
import Zip


@objc(AgoraLrcDownloadDelegate)
public
protocol AgoraLrcDownloadDelegate {
    /// 开始下载
    @objc
    optional func beginDownloadLrc(url: String)
    /// 下载完成
    @objc
    optional func downloadLrcFinished(url: String)
    /// 下载进度
    @objc
    optional func downloadLrcProgress(url: String, progress: Double)
    /// 下载失败
    @objc
    optional func downloadLrcError(url: String, error: Error?)
    /// 下载取消
    @objc
    optional func downloadLrcCanceld(url: String)
    /// 开始解析歌词
    @objc
    optional func beginParseLrc()
    /// 解析歌词结束
    @objc
    optional func parseLrcFinished()
}

@objc class AgoraDownLoadManager: NSObject {
    
    typealias Completion = (String?) -> Void
    typealias Sunccess = (String?) -> Void
    typealias UnZipErrorClosure = () -> Void
    private lazy var request = AgoraRequestTask()
    private var urlString: String = ""
    private var retryCount: Int = 0
    private var completion: [String: Completion] = [:]
    private var success: [String: Sunccess] = [:]
    private var failure: [String: UnZipErrorClosure] = [:]

    @objc public weak var delegate: AgoraLrcDownloadDelegate?

    @objc func downloadLrcFile(urlString: String,
                         completion: @escaping Completion,
                         failure: @escaping UnZipErrorClosure)
    {

        delegate?.beginDownloadLrc?(url: urlString)
        self.urlString = urlString
        let fileName = urlString.fileName.components(separatedBy: ".").first ?? ""
        let xmlPath = AgoraCacheFileHandle.cacheFileExists(with: fileName + ".xml")
        let lrcPath = AgoraCacheFileHandle.cacheFileExists(with: urlString)
        if urlString.hasSuffix(".xml"), !urlString.hasPrefix("https:"), xmlPath == nil {
            parseXml(path: urlString, completion: completion)
            return
        }
        if xmlPath != nil {
            parseXml(path: xmlPath ?? "", completion: completion)
        } else if lrcPath == nil {
            if !urlString.hasPrefix("http") {
                parseLrc(path: urlString, completion: completion)
                return
            }
            guard let url = URL(string: urlString) else { return }
            delegate?.beginDownloadLrc?(url: urlString)
            request.delegate = self
            request.download(requestURL: url)
            self.completion[urlString] = completion
            self.failure[urlString] = failure
        } else {
            if urlString.hasSuffix("zip") {
                unzip(path: lrcPath ?? "", completion: completion, failure: failure)
            } else {
                parseLrc(path: lrcPath ?? "", completion: completion)
            }
        }
    }

    func downloadMP3(urlString: String, success: @escaping Sunccess) {
        self.urlString = urlString
        let cachePath = AgoraCacheFileHandle.cacheFileExists(with: urlString)
        if cachePath == nil {
            guard let url = URL(string: urlString) else { return }
            request.delegate = self
            request.download(requestURL: url)
            self.success[urlString] = success
        } else {
            success(cachePath)
        }
    }

    private func unzip(path: String, completion: @escaping Completion, failure: @escaping UnZipErrorClosure) {
        delegate?.beginParseLrc?()
        DispatchQueue.global().async {
            let zipFile = URL(fileURLWithPath: path)
            let unZipPath = String.cacheFolderPath()
            do {
                try Zip.unzipFile(zipFile, destination: URL(fileURLWithPath: unZipPath), overwrite: true, password: nil, fileOutputHandler: { url in
                    self.parseXml(path: url.path, completion: completion)
                    try? FileManager.default.removeItem(atPath: path)
                })
            } catch {
                guard self.retryCount < 3 else {
                    self.retryCount = 0
                    DispatchQueue.main.async {
                        self.delegate?.downloadLrcError?(url: self.urlString,
                                                         error: error)
                        failure()
                    }
                    return
                }
                try? FileManager.default.removeItem(atPath: path)
                DispatchQueue.main.async {
                    self.downloadLrcFile(urlString: self.urlString,
                                         completion: completion,
                                         failure: failure)
                }
                self.retryCount += 1
            }
        }
    }

    private func parseXml(path: String, completion: @escaping (String?) -> Void) {
        DispatchQueue.global().async {
            DispatchQueue.main.async {
                completion(path)
                self.delegate?.downloadLrcFinished?(url: path)
            }
        }
    }

    private func parseLrc(path: String, completion: @escaping (String?) -> Void) {
        DispatchQueue.global().async {
            DispatchQueue.main.async {
                completion(path)
                self.delegate?.downloadLrcFinished?(url: path)
            }
        }
    }
}

extension AgoraDownLoadManager: AgoraLrcDownloadDelegate {
    func beginDownloadLrc(url: String) {
        delegate?.beginDownloadLrc?(url: url)
    }

    func downloadLrcFinished(url: String) {
        let cacheFilePath = "\(String.cacheFolderPath())/\(url.fileName)"
        if url.fileName.hasSuffix(".mp3") {
            DispatchQueue.main.async {
                self.success[url]?(cacheFilePath)
            }
        } else if url.fileName.hasSuffix(".xml") {
            guard let completion = completion[url] else { return }
            parseXml(path: cacheFilePath, completion: completion)
        } else {
            guard let completion = completion[url], let failure = failure[url] else { return }
            if url.hasSuffix(".zip") {
                unzip(path: cacheFilePath, completion: completion, failure: failure)
            } else {
                parseLrc(path: cacheFilePath, completion: completion)
            }
        }
    }

    func downloadLrcProgress(url: String, progress: Double) {
        delegate?.downloadLrcProgress?(url: url, progress: progress)
    }

    func downloadLrcError(url: String, error: Error?) {
       // Log.errorText(text: "\(error?.localizedDescription ?? "nil") url")
        delegate?.downloadLrcError?(url: url, error: error)
    }

    func downloadLrcCanceld(url: String) {
        delegate?.downloadLrcCanceld?(url: url)
    }

    func beginParseLrc() {
        delegate?.beginParseLrc?()
    }

    func parseLrcFinished() {
        delegate?.parseLrcFinished?()
    }
}
