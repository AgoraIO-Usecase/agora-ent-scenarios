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
    private var currentLoadLrcPath: String?

    @objc init(lrcView: KaraokeView) {
        self.lrcView = lrcView
        super.init()
        lrcView.delegate = self
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

extension SBGLrcControl: SBGLrcViewDelegate {
    
    func onHighPartTime(highStartTime: Int, highEndTime: Int) {
        
    }

    func onUpdatePitch(pitch: Float) {
        lrcView?.setPitch(pitch: Double(pitch))
    }

    func onUpdateProgress(progress: Int) {
        self.progress = progress
        lrcView?.setProgress(progress: progress)
    }

    func onDownloadLrcData(url: String) {
        guard currentLoadLrcPath != url else {
            return
        }
        let musicUrl = URL(fileURLWithPath: url)
        guard let data = try? Data(contentsOf: musicUrl),
              let model = KaraokeView.parseLyricData(data: data) else {
            return
        }
        currentLoadLrcPath = url
        lyricModel = model
        totalCount = model.lines.count
        totalLines = 0
        totalScore = 0
        lrcView?.setLyricData(data: model)
    }
}
