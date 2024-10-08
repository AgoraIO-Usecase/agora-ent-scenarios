//
//  SBGLrcControl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraLyricsScore


private func agoraPrint(_ message: String) {
    SBGLog.info(text: message, tag: "SBGLrcControl")
}

@objc public protocol SBGLrcControlDelegate: NSObjectProtocol {
    func didLrcViewDragedTo(pos: Int, score: Int, totalScore: Int)
    func didLrcViewScorllFinished(with score: Int, totalScore: Int, lineScore: Int, lineIndex:Int)
}

@objc class SBGLrcControl: NSObject {

    @objc public weak var lrcView: KaraokeView?
    @objc public weak var delegate: SBGLrcControlDelegate?
 
    private var lyricModel: LyricModel?
    private var progress: Int = 0
    private var totalLines: Int = 0
    private var totalScore: Int = 0
    private var totalCount: Int = 0
    private var startTime: Int = 0
    private var endTime: Int = 0
    private var currentLoadLrcPath: String?
  //  private var downloadManager: AgoraDownLoadManager = AgoraDownLoadManager()
    private let lrcDownload: LyricsFileDownloader = LyricsFileDownloader()
    @objc init(lrcView: KaraokeView) {
        self.lrcView = lrcView
        super.init()
        lrcView.delegate = self
       // downloadManager.delegate = self
        lrcDownload.delegate = self
    }

    @objc public func getAvgScore() -> Int {
        return totalLines > 0 ? totalScore / totalLines : 0
    }

    @objc public func resetLrc() {
        lrcView?.reset()
        currentLoadLrcPath = nil
    }
    
}

extension SBGLrcControl: KaraokeDelegate {

    func onKaraokeView(view: KaraokeView, didDragTo position: Int) {
        totalScore = view.scoringView.getCumulativeScore()
        guard let delegate = delegate else {
            return
        }
        delegate.didLrcViewDragedTo(pos: position,
                                    score: totalScore,
                                    totalScore: totalCount * 100)
    }

    func onKaraokeView(view: KaraokeView,
                       didFinishLineWith model: LyricLineModel,
                       score: Int,
                       cumulativeScore: Int,
                       lineIndex: Int,
                       lineCount: Int) {
        totalLines = lineCount
        totalScore += score
        guard let delegate = delegate else {
            return
        }
        delegate.didLrcViewScorllFinished(with: totalScore,
                                          totalScore: lineCount * 100,
                                          lineScore: score,
                                          lineIndex: lineIndex)
    }
}

extension SBGLrcControl: KTVLrcViewDelegate {
    
    func onHighPartTime(highStartTime: Int, highEndTime: Int) {
        self.startTime = highStartTime
        self.endTime = highEndTime
    }

    func onUpdatePitch(pitch: Float) {
        lrcView?.setPitch(pitch: Double(pitch))
    }

    func onUpdateProgress(progress: Int) {
        self.progress = progress
        lrcView?.setProgress(progress: progress)
    }

    func onDownloadLrcData(url: String) {
        //开始歌词下载
//        startDownloadLrc(with: url) {[weak self] url in
//            guard let self = self, let url = url else {return}
//            self.resetLrcData(with: url)
//        }
        let _ = self.lrcDownload.download(urlString: url)
    }
        
//    func startDownloadLrc(with url: String, callBack: @escaping LyricCallback) {
//        var path: String? = nil
//        downloadManager.downloadLrcFile(urlString: url) { lrcurl in
//            defer {
//                callBack(path)
//            }
//            guard let lrcurl = lrcurl else {
//                agoraPrint("downloadLrcFile fail, lrcurl is nil")
//                return
//            }
//
//            let curSong = URL(string: url)?.lastPathComponent.components(separatedBy: ".").first
//            let loadSong = URL(string: lrcurl)?.lastPathComponent.components(separatedBy: ".").first
//            guard curSong == loadSong else {
//                agoraPrint("downloadLrcFile fail, missmatch, cur:\(curSong ?? "") load:\(loadSong ?? "")")
//                return
//            }
//            path = lrcurl
//        } failure: {
//            callBack(nil)
//            agoraPrint("歌词解析失败")
//        }
//    }
//        
//    func resetLrcData(with url: String) {
//        guard currentLoadLrcPath != url else {
//            return
//        }
//        let musicUrl = URL(fileURLWithPath: url)
//        guard let data = try? Data(contentsOf: musicUrl),
//              let model = KaraokeView.parseLyricData(data: data) else {
//            return
//        }
//        currentLoadLrcPath = url
//        lyricModel = model
//        let lines = model.lines.map({
//            LyricsCutter.Line(beginTime: $0.beginTime, duration: $0.duration)
//        })
//        
//        if let res = LyricsCutter.handleFixTime(startTime: self.startTime, endTime: self.endTime, lines: lines) {
//            self.startTime = res.0
//            self.endTime = res.1
//        }
//        lyricModel = LyricsCutter.cut(model:model, startTime: self.startTime, endTime: self.endTime)
//        totalCount = model.lines.count
//        totalLines = 0
//        totalScore = 0
//        lrcView?.setLyricData(data: model)
//    }
}

extension SBGLrcControl: LyricsFileDownloaderDelegate {
    func onLyricsFileDownloadProgress(requestId: Int, progress: Float) {
        print("lrc pro:\(progress)")
    }
    
    func onLyricsFileDownloadCompleted(requestId: Int, fileData: Data?, error: AgoraLyricsScore.DownloadError?) {
        if let data = fileData, let model = KaraokeView.parseLyricData(data: data) {
            lyricModel = model
            let lines = model.lines.map({
                LyricsCutter.Line(beginTime: $0.beginTime, duration: $0.duration)
            })
    
            if let res = LyricsCutter.handleFixTime(startTime: self.startTime, endTime: self.endTime, lines: lines) {
                self.startTime = res.0
                self.endTime = res.1
            }
            lyricModel = LyricsCutter.cut(model:model, startTime: self.startTime, endTime: self.endTime)
            totalCount = model.lines.count
            totalLines = 0
            totalScore = 0
            lrcView?.setLyricData(data: model)
        }
    }
}
