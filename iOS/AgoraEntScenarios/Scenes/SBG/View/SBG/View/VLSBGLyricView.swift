//
//  VLSBGLyricView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/11.
//

import UIKit
import AgoraLyricsScore
import ScoreEffectUI

@objc protocol VLSBGLrcViewDelegate: NSObjectProtocol {
    func onKaraokeView(score: Int, totalScore: Int, lineScore: Int, lineIndex: Int)
    func didLrcViewActionChanged(state: SBGClickAction)
}

@objc public enum singingState: Int {
    case timeDownAudience
    case timeDownBroadcaster
    case sbgingOnSeat
    case sbgingOffSeat
    case singingAudience
    case singingBroadcaster
}

@objc class scoreModel: NSObject {
    @objc var index: Int = 0
    @objc var name: String = ""
    @objc var count: Int = 0
    @objc var score: Int = 0
    @objc var poster: String = ""
    @objc var userId: String = ""
}

class VLSBGLyricView: UIView {

    public var state: singingState = .singingAudience {
        didSet {
            if state == .timeDownAudience {//倒计时
//                songNameBtn.isHidden = false
//                songNameBtn.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 15, width: 100, height: 20)
                songNameView.isHidden = false
                songNameView.setName(with: songContent, isCenter: true)
                sbgBtn.isHidden = true
               // sbgBtn.frame = CGRect(x: self.bounds.width / 2.0 - 55, y: self.bounds.height - 54, width: 88, height: 34)
                sbgBtn.isEnabled = false
                lrcView.scoringEnabled = false
                incentiveView.isHidden = true
                lineScoreView.isHidden = true
                gradeView.isHidden = true
                nextBtn.isHidden = true
                effectBtn.isHidden = true
                originBtn.isHidden = true
            } else if state == .timeDownBroadcaster {//倒计时
//                songNameBtn.isHidden = false
//                songNameBtn.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 15, width: 100, height: 20)
                songNameView.isHidden = false
                songNameView.setName(with: songContent, isCenter: true)
                sbgBtn.isEnabled = false
                sbgBtn.isHidden = false
               // sbgBtn.frame = CGRect(x: self.bounds.width / 2.0 - 55, y: self.bounds.height - 54, width: 88, height: 34)
                lrcView.scoringEnabled = false
                incentiveView.isHidden = true
                lineScoreView.isHidden = true
                gradeView.isHidden = true
                nextBtn.isHidden = true
                effectBtn.isHidden = true
                originBtn.isHidden = true
            } else if state == .sbgingOnSeat {//麦上
//                songNameBtn.isHidden = false
//                songNameBtn.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 15, width: 100, height: 20)
                songNameView.isHidden = false
                songNameView.setName(with: songContent, isCenter: true)
                sbgBtn.setTitle("", for: .normal)
                sbgBtn.isHidden = false
              //  sbgBtn.frame = CGRect(x: self.bounds.width / 2.0 - 55, y: self.bounds.height - 54, width: 88, height: 34)
                sbgBtn.isEnabled = true
                gradeView.isHidden = true
                lrcView.scoringEnabled = false
                incentiveView.isHidden = true
                lineScoreView.isHidden = true
                gradeView.isHidden = true
                nextBtn.isHidden = true
                effectBtn.isHidden = true
                originBtn.isHidden = true
            } else if state == .sbgingOffSeat {//麦下
//                songNameBtn.isHidden = false
//                songNameBtn.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 15, width: 100, height: 20)
                songNameView.isHidden = false
                songNameView.setName(with: songContent, isCenter: true)
                sbgBtn.isHidden = true
              //  sbgBtn.frame = CGRect(x: self.bounds.width / 2.0 - 55, y: self.bounds.height - 54, width: 88, height: 34)
                sbgBtn.isEnabled = true
                gradeView.isHidden = true
                lrcView.scoringEnabled = false
                incentiveView.isHidden = true
                lineScoreView.isHidden = true
                gradeView.isHidden = true
                nextBtn.isHidden = true
                effectBtn.isHidden = true
                originBtn.isHidden = true
                sbgBtn.setTitle("", for: .normal)
            } else if state == .singingAudience {//观众
//                songNameBtn.isHidden = false
//                songNameBtn.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 15, width: 100, height: 20)
                songNameView.isHidden = false
                songNameView.setName(with: songContent, isCenter: true)
                sbgBtn.isHidden = true
                incentiveView.isHidden = false
                lineScoreView.isHidden = false
                lrcView.scoringEnabled = true
                gradeView.isHidden = false
                nextBtn.isHidden = true
                effectBtn.isHidden = true
                originBtn.isHidden = true
            } else if state == .singingBroadcaster {//主唱
//                songNameBtn.isHidden = false
//                songNameBtn.frame = CGRect(x: 12, y: 15, width: 100, height: 20)
                songNameView.isHidden = false
                songNameView.setName(with: songContent, isCenter: false)
                sbgBtn.isHidden = true
                lrcView.scoringEnabled = true
                incentiveView.isHidden = false
                lineScoreView.isHidden = false
                gradeView.isHidden = false
                nextBtn.isHidden = false
                effectBtn.isHidden = false
                originBtn.isHidden = false
            }
        }
    }

    private var songNameBtn: UIButton = {
        let btn = UIButton()
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 11)
        btn.imageView?.contentMode = .center
        btn.setImage(UIImage.sceneImage(name: "ktv_bigMusic_icon"), for: .normal)
        btn.setTitleColor(.white, for: .normal)
        return btn
    }()
    
    private var songNameView: SBGLrcNameView = {
        let view = SBGLrcNameView()
        return view
    }()
    
    var lrcView: KaraokeView!
    var incentiveView: IncentiveView!
    var lineScoreView: LineScoreView!
    var gradeView: GradeView!
    private var currentLoadLrcPath: String?
    private var totalLines: Int = 0
    private var localTotalScore: Int = 0
    private var totalCount: Int = 0
    private var progress: Int = 0
    private var highStartTime: Int = 0
    private var highEndTime: Int = 0
    @objc public var finalScore: Int = 0
    @objc weak var delegate: VLSBGLrcViewDelegate?
    private var model: LyricModel?
    private var isTaped: Bool = false
    private var songContent: String = ""
    private var downloadManager = AgoraDownLoadManager()
    private var lyricModel: LyricModel? = nil
    private var bgImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "ktv_mv_tempBg")
        return imgView
    }()
    
    private lazy var nextBtn: UIButton = {
        let btn = UIButton()
        btn.setTitle("切歌", for: .normal)
        btn.setTitleColor(.white, for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 11)
        btn.setImage(UIImage.sceneImage(name: "ktv_playNext_icon"), for: .normal)
        btn.adjustImageTitlePosition(.top)
        btn.addTarget(self, action: #selector(nextSong), for: .touchUpInside)
        btn.layoutIfNeeded()
        return btn
    }()
    
    private lazy var effectBtn: UIButton = {
        let btn = UIButton()
        btn.setTitle("调音", for: .normal)
        btn.setTitleColor(.white, for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 11)
        btn.setImage(UIImage.sceneImage(name: "ktv_subtitle_icon"), for: .normal)
        btn.adjustImageTitlePosition(.top)
        btn.addTarget(self, action: #selector(effect), for: .touchUpInside)
        btn.layoutIfNeeded()
        return btn
    }()
    
    private lazy var originBtn: UIButton = {
        let btn = UIButton()
        btn.setTitle("原唱", for: .normal)
        btn.setTitle("原唱", for: .selected)
        btn.setTitleColor(.white, for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 11)
        btn.setImage(UIImage.sceneImage(name: "original"), for: .normal)
        btn.setImage(UIImage.sceneImage(name: "acc"), for: .selected)
        btn.adjustImageTitlePosition(.top)
        btn.addTarget(self, action: #selector(trackChoose), for: .touchUpInside)
        btn.layoutIfNeeded()
        return btn
    }()
    
    private lazy var sbgBtn: UIButton = {
        let btn = UIButton()
        btn.setBackgroundImage(UIImage.sceneImage(name: "sbg-btn-disabled"), for: .disabled)
        btn.setBackgroundImage(UIImage.sceneImage(name: "sbg-btn-qiang"), for: .normal)
        btn.setTitleColor(.white, for: .normal)
        btn.addTargetFor(self, action: #selector(sbg), for: .touchUpInside)
        btn.isEnabled = false
        return btn
    }()
    
    public func setCountTime(time: Int) {
        sbgBtn.setTitle("\(time)", for: .normal)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
        downloadManager.delegate = self
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        
        addSubview(bgImgView)
        
        gradeView = GradeView()
        
        addSubview(gradeView!)
        
        lrcView = KaraokeView(frame: .zero, loggers: [FileLogger()])
        lrcView.scoringView.viewHeight = 60
        lrcView.scoringView.topSpaces = 5
        lrcView.lyricsView.inactiveLineTextColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.5)
        lrcView.lyricsView.activeLinePlayedTextColor = UIColor(hex: "#FF8AB4")
        lrcView.lyricsView.lyricLineSpacing = 6
        lrcView.lyricsView.draggable = false
        lrcView.delegate = self
        addSubview(lrcView!)

        incentiveView = IncentiveView()
        lrcView.addSubview(incentiveView!)

        lineScoreView = LineScoreView()
        addSubview(self.lineScoreView!)
        
        //addSubview(songNameBtn)
       // addSubview(songNameLabel)
        addSubview(songNameView)
        
        addSubview(nextBtn)
        addSubview(effectBtn)
        addSubview(originBtn)
        
        addSubview(sbgBtn)
        lrcView.isHidden = false
        incentiveView.isHidden = true
        lineScoreView.isHidden = true
        gradeView.isHidden = true
        
    }
    
    @objc func nextSong(){
        guard let delegate = delegate else {return}
        delegate.didLrcViewActionChanged(state: .nextSong)
    }
    
    @objc func effect(){
        guard let delegate = delegate else {return}
        delegate.didLrcViewActionChanged(state: .effect)
    }
    
    @objc func trackChoose(btn: UIButton){
        btn.isSelected = !btn.isSelected
        guard let delegate = delegate else {return}
        delegate.didLrcViewActionChanged(state: btn.isSelected ? .aac : .origin)
    }

    @objc public func setAudioTrack(index: Int){
        originBtn.isSelected = index == 1
    }
    
    @objc func sbg() {
        if isTaped {//防止暴力点击
            return
        }
        guard let delegate = delegate else {return}
        delegate.didLrcViewActionChanged(state: .sbg)
        isTaped = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.isTaped = false
        }
    }
    
    @objc public func updateScore(with lineScore: Int, cumulativeScore: Int, totalScore: Int) {
        if localTotalScore == 0 {return}
        lineScoreView.showScoreView(score: lineScore)
        gradeView.setScore(cumulativeScore: cumulativeScore, totalScore: localTotalScore)
        incentiveView.show(score: lineScore)
    }
    
    @objc public func resetScore() {
        gradeView.reset()
        incentiveView.reset()
        lrcView?.reset()
        self.songContent = ""
        self.lyricModel = nil
        self.songNameView.setName(with: self.songContent, isCenter: true)
        self.currentLoadLrcPath = nil
    }
    
    @objc public func resetLrc() {
        DispatchQueue.main.async {
            self.gradeView.reset()
            self.incentiveView.reset()
            self.lrcView?.reset()
            self.songContent = ""
            self.lyricModel = nil
            self.songNameView.setName(with: self.songContent, isCenter: true)
            self.currentLoadLrcPath = nil
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImgView.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: self.bounds.height - 16)
        gradeView.frame = CGRect(x: 15, y: 15, width: Int(self.bounds.width) - 15 * 2, height: 15 * 2)
        lrcView.frame = CGRect(x: 0, y: 30, width: self.bounds.width, height: self.bounds.height - 50)
        lineScoreView.frame = CGRect(x: lrcView?.scoringView.defaultPitchCursorX ?? 0, y: (lrcView?.scoringView.topSpaces ?? 0) + (lrcView?.bounds.minY ?? 0), width: 50, height: lrcView.scoringView.viewHeight)
        incentiveView.frame = CGRect(x: 15, y: 55, width: 192, height: 45)
        nextBtn.frame = CGRect(x: 20, y: self.bounds.height - 70, width: 30, height: 50)
        originBtn.frame = CGRect(x: self.bounds.width - 50, y: self.bounds.height - 70, width: 30, height: 50)
        effectBtn.frame = CGRect(x: self.bounds.width - 100, y: self.bounds.height - 70, width: 30, height: 50)
        
        //songNameBtn.frame = CGRect(x: state == .singingBroadcaster ? 20 : (self.bounds.width / 2.0 - 50) , y: 8, width: 100, height: 20)
        songNameView.frame = CGRect(x: 20, y:8, width:self.bounds.width - 40 , height: 20)
        sbgBtn.frame = CGRect(x: self.bounds.width / 2.0 - 60, y: self.bounds.height - 70, width: 110, height: 50)
        self.bgImgView.layer.cornerRadius = 10
        self.bgImgView.layer.masksToBounds = true
        self.bgImgView.layer.borderWidth = 1
        self.bgImgView.layer.borderColor = UIColor.white.cgColor
    }
    
}

extension VLSBGLyricView: KaraokeDelegate {
    public func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
        //歌词打分
        totalLines = lineCount
        guard let delegate = self.delegate else {
            return
        }
        delegate.onKaraokeView(score: cumulativeScore, totalScore: localTotalScore, lineScore: score, lineIndex: lineIndex)
        if localTotalScore == 0 {return}
        finalScore = Int(Double(cumulativeScore) / Double(localTotalScore) * 100)
       // updateScore(with: score, cumulativeScore: cumulativeScore, totalScore: localTotalScore)
    }
}

extension VLSBGLyricView: SBGLrcViewDelegate {
    
    func onHighPartTime(highStartTime: Int, highEndTime: Int) {
        self.highEndTime = highEndTime
        self.highStartTime = highStartTime
        //如果model为空 说明顺序对的就不用管，如果model不为空 表示歌词先到，需要处理
        guard let lyricModel = self.lyricModel else {return}
        DispatchQueue.main.async {
            self.dealWithModel(lyricModel)
        }
    }
    
    public func onUpdatePitch(pitch: Float) {
        //pitch 更新
        DispatchQueue.main.async {
            self.lrcView?.setPitch(pitch: Double(pitch))
        }
    }
    
    public func onUpdateProgress(progress: Int) {
        self.progress = progress
        //进度更新
        print("progress:\(progress)")
        lrcView?.setProgress(progress: progress)
    }
    
    func onDownloadLrcData(url: String) {
        //开始歌词下载
        startDownloadLrc(with: url) {[weak self] url in
            guard let self = self, let url = url else {return}
            self.resetLrcData(with: url)
        }
    }
        
    func startDownloadLrc(with url: String, callBack: @escaping LyricCallback) {
        var path: String? = nil
        downloadManager.downloadLrcFile(urlString: url) { lrcurl in
            defer {
                callBack(path)
            }
            guard let lrcurl = lrcurl else {
                print("downloadLrcFile fail, lrcurl is nil")
                return
            }

            let curSong = URL(string: url)?.lastPathComponent.components(separatedBy: ".").first
            let loadSong = URL(string: lrcurl)?.lastPathComponent.components(separatedBy: ".").first
            guard curSong == loadSong else {
                print("downloadLrcFile fail, missmatch, cur:\(curSong ?? "") load:\(loadSong ?? "")")
                return
            }
            path = lrcurl
        } failure: {
            callBack(nil)
            print("歌词解析失败")
        }
    }
        
    func resetLrcData(with url: String) {
        guard currentLoadLrcPath != url else {
            return
        }
        let musicUrl = URL(fileURLWithPath: url)
        guard let data = try? Data(contentsOf: musicUrl),
              let model = KaraokeView.parseLyricData(data: data) else {
            return
        }
        currentLoadLrcPath = url
        self.lyricModel = model
        if self.highEndTime != 0 {
            dealWithModel(model)
        }
    }
    
    func dealWithModel(_ model: LyricModel) {
        let lines = model.lines.map({
            LyricsCutter.Line(beginTime: $0.beginTime, duration: $0.duration)
        })
        
        if let res = LyricsCutter.handleFixTime(startTime: self.highStartTime, endTime: self.highEndTime, lines: lines) {
            self.highStartTime = res.0
            self.highEndTime = res.1
        }
        let realModel = LyricsCutter.cut(model:model, startTime: self.highStartTime, endTime: self.highEndTime)
        totalCount = realModel.lines.count
        totalLines = 0
        lrcView.reset()
        localTotalScore = realModel.lines.count * 100
        print("totalScore:\(localTotalScore), start:\(highStartTime), end:\(highEndTime), title:\(realModel.name), lineCount:\(realModel.lines.count)")
        songContent = "\(realModel.name.trimmingCharacters(in: .whitespacesAndNewlines))-\(realModel.singer)"
        songNameView.isHidden = false
        songNameView.setName(with: songContent, isCenter: true)
        lrcView?.setLyricData(data: realModel)
        self.lyricModel = nil
        self.highStartTime = 0
        self.highEndTime = 0
    }
}

extension VLSBGLyricView: AgoraLrcDownloadDelegate {
    func downloadLrcFinished(url: String) {
        print("download lrc finished \(url)")
    }
    
    func downloadLrcError(url: String, error: Error?) {
        print("download lrc fail \(url): \(String(describing: error))")
    }
}

class AtomicInteger {
    private var value: Int

    init(_ value: Int = 0) {
        self.value = value
    }

    func get() -> Int {
        return value
    }

    func set(_ newValue: Int) {
        value = newValue
    }

    @discardableResult
    func getAndIncrement() -> Int {
        defer { value += 1 }
        return value
    }
}

extension UIButton {
    /// 逆时针方向🔄
    enum ImgPosition { case top, left, bottom, right }
    
    /// 重置图片image与标题title位置(默认间距为0)
    func adjustImageTitlePosition(_ position: ImgPosition, spacing: CGFloat = 0 ) {
        self.sizeToFit()
        
        let imageWidth = self.imageView?.image?.size.width
        let imageHeight = self.imageView?.image?.size.height
        
        let labelWidth = self.titleLabel?.frame.size.width
        let labelHeight = self.titleLabel?.frame.size.height
        
        switch position {
        case .top:
            imageEdgeInsets = UIEdgeInsets(top: -labelHeight! - spacing / 2, left: 0, bottom: 0, right: -labelWidth!)
            titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageWidth!, bottom: -imageHeight! - spacing / 2, right: 0)
            break
            
        case .left:
            imageEdgeInsets = UIEdgeInsets(top: 0, left: -spacing / 2, bottom: 0, right: 0)
            titleEdgeInsets = UIEdgeInsets(top: 0, left: spacing * 1.5, bottom: 0, right: 0)
            break
            
        case .bottom:
            imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: -labelHeight! - spacing / 2, right: -labelWidth!)
            titleEdgeInsets = UIEdgeInsets(top: -imageHeight! - spacing / 2, left: -imageWidth!, bottom: 0, right: 0)
            break
            
        case .right:
            imageEdgeInsets = UIEdgeInsets(top: 0, left: labelWidth! + spacing / 2, bottom: 0, right: -labelWidth! - spacing / 2)
            titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageWidth! - spacing / 2, bottom: 0, right: imageWidth! + spacing / 2)
            break
        }
    }
}
