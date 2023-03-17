//
//  KTVLrcControl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraLyricsScore

@objc public protocol KTVLrcControlDelegate: NSObjectProtocol {
    func didLrcViewDragTo( pos: Int)
    func didLrcViewFinishLine(with model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int)
}

@objc class KTVLrcControl: NSObject {
    @objc public var lrcView: KaraokeView
    private var skipBtn: KTVSkipView!
    @objc public var isMainSinger: Bool = false //是否为主唱
    private var lyricModel: LyricModel?
    private var hasShowPreludeEndPosition = false
    private var hasShowEndPosition = false
    public weak var delegate: KTVLrcControlDelegate?
    @objc public var skipCallBack: ((Int) -> Void)?
    private var progress: Int = 0
    private var totalLines: NSInteger = 0
    private var totalScore: NSInteger = 0
    private var totalCount: NSInteger = 0
    
    @objc init(lrcView: KaraokeView) {
        self.lrcView = lrcView
        super.init()
        
        skipBtn = KTVSkipView(frame: CGRect(x: self.lrcView.bounds.size.width / 2.0 - 44, y: self.lrcView.bounds.size.height - 34, width: 120, height: 34), completion: { type in
            if type == .down {
                self.skipBtn.isHidden = true
                guard let duration = self.lyricModel?.duration else {return}
                guard let preludeEndPosition = self.lyricModel?.preludeEndPosition else {return}
                let pos: Int = self.progress >= duration  ? duration - 500 : preludeEndPosition - 500
                self.skipCallBack?(pos)
            }
        })
        
        self.lrcView.addSubview(skipBtn)
        self.lrcView.delegate = self
        self.skipBtn.isHidden = true
    }
}

extension KTVLrcControl: KaraokeDelegate {
    func onKaraokeView(view: KaraokeView, didDragTo position: Int) {
        totalScore = view.scoringView.getCumulativeScore()
        guard let delegate = delegate else {return}
        delegate.didLrcViewDragTo(pos: position)
    }
    
    func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
        self.totalLines = lineCount
        self.totalScore = cumulativeScore
        guard let delegate = delegate else {return}
        delegate.didLrcViewFinishLine(with: model, score: score, cumulativeScore: cumulativeScore, lineIndex: lineIndex, lineCount: lineCount)
    }
}


extension KTVLrcControl: KTVLrcViewDelegate {
    func onUpdatePitch(pitch: Float) {
        lrcView.setPitch(pitch: Double(pitch))
    }
    
    func onUpdateProgress(progress: Int) {
        lrcView.setProgress(progress: progress)
        if progress > (lyricModel?.duration ?? 0) {return}
        if !isMainSinger {return}
        if progress > 10 && progress < 100 {
            skipBtn.isHidden = false
            hasShowEndPosition = false
            hasShowPreludeEndPosition = false
        }
        
        let preludeEndPosition: Int = (lyricModel?.preludeEndPosition ?? 0) - 500
        let duration: Int = (lyricModel?.duration ?? 0) - 500
        
        if (preludeEndPosition < progress && hasShowPreludeEndPosition == false) {
            skipBtn.isHidden = true
            hasShowPreludeEndPosition = true
        } else if (duration < progress && hasShowEndPosition == false) {
            skipBtn.isHidden = false
            hasShowEndPosition = true
        }
    }
    
    func onDownloadLrcData(url: String) {
        let musicUrl: URL = URL(fileURLWithPath: url)

        guard let data = try? Data(contentsOf: musicUrl) else {return}
        guard let model: LyricModel = KaraokeView.parseLyricData(data: data) else {return}
        self.lyricModel = model
        self.totalCount = model.lines.count
        self.totalLines = 0
        self.totalScore = 0
        lrcView.setLyricData(data: model)
    }
    
    
}
