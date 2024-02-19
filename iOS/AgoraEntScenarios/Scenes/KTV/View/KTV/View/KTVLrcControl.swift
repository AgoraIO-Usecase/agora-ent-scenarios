//
//  KTVLrcControl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraLyricsScore

private func agoraPrint(_ message: String) {
    KTVLog.info(text: message, tag: "KTVLrcControl")
}

@objc public protocol KTVLrcControlDelegate: NSObjectProtocol {
    func didLrcViewDragedTo(pos: Int, score: Int, totalScore: Int)
    func didLrcViewScorllFinished(with score: Int, totalScore: Int, lineScore: Int, lineIndex:Int)
}

@objc class KTVLrcControl: NSObject {

    @objc public weak var lrcView: KaraokeView?
    @objc public weak var delegate: KTVLrcControlDelegate?
    @objc public var skipCallBack: ((Int, Bool) -> Void)?

    private var skipBtn: KTVSkipView!
    private var lyricModel: LyricModel?
    private var hasShowPreludeEndPosition = false
    private var hasShowEndPosition = false
    private var progress: Int = 0
    private var totalLines: Int = 0
    private var totalScore: Int = 0
    private var totalCount: Int = 0
    private var currentLoadLrcPath: String?
    private var hasShowOnce: Bool = false
    //private var downloadManager: AgoraDownLoadManager = AgoraDownLoadManager()
    private let lrcDownload: LyricsFileDownloader = LyricsFileDownloader()
    @objc public var isMainSinger: Bool = false {
        didSet {
            if isMainSinger {return}
            skipBtn.isHidden = true
        }
    }

    @objc init(lrcView: KaraokeView) {
        self.lrcView = lrcView
        super.init()
        setupSkipBtn()
        lrcView.delegate = self
        skipBtn.isHidden = true
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

    @objc public func hideSkipView(flag: Bool) {
        skipBtn.isHidden = flag
    }

    @objc public func showPreludeEnd() {
        if hasShowOnce {return}
        //显示跳过前奏
        skipBtn.setSkipType(.prelude)
        skipBtn.isHidden = false
        hasShowEndPosition = false
        hasShowPreludeEndPosition = false
    }
    
    @objc public func resetShowOnce() {
        hasShowOnce = false
    }

    private func setupSkipBtn() {
        let frame = CGRect(x: (lrcView?.bounds.size.width ?? 0) / 2.0 - 60,
                           y: (lrcView?.bounds.size.height ?? 0) - 20,
                           width: 120,
                           height: 34)
        skipBtn = KTVSkipView(frame: frame) { [weak self] type in
            guard let self = self,
                  let duration = self.lyricModel?.duration,
                  let preludeEndPosition = self.lyricModel?.preludeEndPosition else {
                return
            }
            var pos = preludeEndPosition - 2000
            if self.progress >= duration - 500 {
                pos = duration - 500
                self.skipCallBack?(pos, true)
            } else {
                self.skipCallBack?(pos, false)
            }
            self.hasShowOnce = true
            self.skipBtn.isHidden = true
        }
        lrcView?.addSubview(skipBtn)
    }
}

extension KTVLrcControl: KaraokeDelegate {

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

extension KTVLrcControl: KTVLrcViewDelegate {

    func onUpdatePitch(pitch: Float) {
        lrcView?.setPitch(pitch: Double(pitch))
    }

    func onUpdateProgress(progress: Int) {
        self.progress = progress
        lrcView?.setProgress(progress: progress)
        guard let model = lyricModel else {
            return
        }
        let preludeEndPosition = model.preludeEndPosition
        let duration = model.duration - 500
        if progress > model.duration {
            return
        }
        if !isMainSinger {
            return
        }
        if preludeEndPosition < progress && !hasShowPreludeEndPosition {
            skipBtn.isHidden = true
            hasShowPreludeEndPosition = true
            hasShowOnce = true
        } else if duration < progress && !hasShowEndPosition {
            skipBtn.setSkipType(.epilogue)
            skipBtn.isHidden = false
            hasShowEndPosition = true
        }
    }

    func onDownloadLrcData(url: String) {
        //开始歌词下载
//        startDownloadLrc(with: url) {[weak self] url in
//            guard let self = self, let url = url else {return}
//            self.resetLrcData(with: url)
//        }
        let _ = self.lrcDownload.download(urlString: url)
    }
    
    func onHighPartTime(highStartTime: Int, highEndTime: Int) {
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
//        totalCount = model.lines.count
//        totalLines = 0
//        totalScore = 0
//        lrcView?.setLyricData(data: model)
//    }
}

extension KTVLrcControl: LyricsFileDownloaderDelegate {
    func onLyricsFileDownloadProgress(requestId: Int, progress: Float) {
        print("lrc pro:\(progress)")
    }
    
    func onLyricsFileDownloadCompleted(requestId: Int, fileData: Data?, error: AgoraLyricsScore.DownloadError?) {
        if let data = fileData, let model = KaraokeView.parseLyricData(data: data) {
            lyricModel = model
            totalCount = model.lines.count
            totalLines = 0
            totalScore = 0
            lrcView?.setLyricData(data: model)
//            musicNameBtn.setTitle("\(model.name)", for: .normal)
//            musicNameBtn.isHidden = false
            skipBtn.setSkipType(.prelude)
        }
    }
}
