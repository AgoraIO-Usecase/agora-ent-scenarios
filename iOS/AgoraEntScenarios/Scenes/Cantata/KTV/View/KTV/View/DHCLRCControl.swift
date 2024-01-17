//
//  DHCLRCControl.swift
//  Cantata
//
//  Created by CP on 2023/9/4.
//

import UIKit
import AgoraLyricsScore
import AUIKitCore
@objc public protocol DHCLrcControlDelegate: NSObjectProtocol {
    func didLrcViewScorllFinished(with score: Int, totalScore: Int, lineScore: Int, lineIndex:Int)
    func didLrcViewDragedTo(pos: Int, score: Int, totalScore: Int)
}

class RankModel: NSObject {
    @objc var userName: String?
    @objc var poster: String?
    @objc var songNum: Int = 0
    @objc var score: Int = 0
}

class SubRankModel : RankModel {
    @objc var userId: String?
    @objc var index: Int = 0
    @objc var count: Int = 0
    @objc var segCount: Int = 0
}

public enum DHCGameEvent: Int {
    case pause
    case play
    case next
    case leave
    case join
    case effect
    case origin
    case acc
    case showChorus
    case resultNext
    case retryLrc
}

public enum DHCGameState: Int {
    case noSong
    case ownerSing //房主主唱
    case chorusSing //非房主主唱
    case ownerChorusSing //房主合唱
    case ownerChorus //房主加入合唱
    case joinChorus //加入合唱
    case beforeJoinChorus //加入合唱loading的时候
    case nextSong //下一首 结算
}

public protocol DHCGameDelegate: NSObjectProtocol {
    func didGameEventChanged(with event: DHCGameEvent)
}

class DHCLRCControl: UIView {
    @objc public var lrcView: KaraokeView!
    private var musicNameBtn: UIButton!
    private var scoreLabel: UILabel!
    private var pauseBtn: UIButton!
    private var nextBtn: UIButton!
    private var originBtn: UIButton! //原唱/伴奏
    private var effectBtn: UIButton! //调音
    private var joinChorusBtn: UIButton! //加入合唱
    private var leaveChorusBtn: UIButton! //离开合唱
    private var resultView: DHCResultView! //结算界面
    public var noSongLabel: UILabel!
    private var chorusNumBtn: UIButton!
    private var progress: Int = 0
    private var totalLines: Int = 0
    private var totalScore: Int = 0
    private var totalCount: Int = 0
    private var currentLoadLrcPath: String?
    
    public var retryBtn: UIButton!
    @objc public var skipCallBack: ((Int, Bool) -> Void)?

    public var skipBtn: KTVSkipView!
    private var lyricModel: LyricModel?
    private var hasShowPreludeEndPosition = false
    private var hasShowEndPosition = false
    private var hasShowOnce: Bool = false
    @objc public var isMainSinger: Bool = false {
        didSet {
            if isMainSinger {return}
            skipBtn.isHidden = true
        }
    }
    
    private var loadingView: AUIKaraokeLoadingView!
    @objc public weak var lrcDelegate: DHCLrcControlDelegate?
    weak var delegate: DHCGameDelegate?
    private var downloadManager = AgoraDownLoadManager()
    public var controlState: DHCGameState = .noSong {
        didSet {
            DispatchQueue.main.async {
                self.updateUI(with: self.controlState)
            }
        }
    }
    
    private func updateUI(with state: DHCGameState) {
        switch controlState {
        case .noSong:
            pauseBtn.isHidden = true
            nextBtn.isHidden = true
            originBtn.isHidden = true
            effectBtn.isHidden = true
            joinChorusBtn.isHidden = true
            leaveChorusBtn.isHidden = true
            musicNameBtn.isHidden = true
            noSongLabel.isHidden = false
            lrcView.isHidden = true
            chorusNumBtn.isHidden = true
            scoreLabel.isHidden = true
            resultView.isHidden = true
            skipBtn.isHidden = true
        case .ownerSing:
            pauseBtn.isHidden = false
            nextBtn.isHidden = false
            originBtn.isHidden = false
            effectBtn.isHidden = false
            joinChorusBtn.isHidden = true
            leaveChorusBtn.isHidden = true
            musicNameBtn.isHidden = false
            noSongLabel.isHidden = true
            lrcView.isHidden = false
            chorusNumBtn.isHidden = false
            scoreLabel.isHidden = false
            resultView.isHidden = true
            skipBtn.isHidden = true
        case .chorusSing:
            pauseBtn.isHidden = true
            nextBtn.isHidden = true
            originBtn.isHidden = false
            effectBtn.isHidden = false
            joinChorusBtn.isHidden = true
            leaveChorusBtn.isHidden = false
            musicNameBtn.isHidden = false
            noSongLabel.isHidden = true
            lrcView.isHidden = false
            chorusNumBtn.isHidden = false
            scoreLabel.isHidden = false
            resultView.isHidden = true
            skipBtn.isHidden = true
        case .ownerChorusSing:
            pauseBtn.isHidden = true
            nextBtn.isHidden = false
            originBtn.isHidden = false
            effectBtn.isHidden = false
            joinChorusBtn.isHidden = true
            leaveChorusBtn.isHidden = false
            musicNameBtn.isHidden = false
            noSongLabel.isHidden = true
            lrcView.isHidden = false
            chorusNumBtn.isHidden = false
            scoreLabel.isHidden = false
            resultView.isHidden = true
            skipBtn.isHidden = true
        case .ownerChorus:
            pauseBtn.isHidden = true
            nextBtn.isHidden = false
            originBtn.isHidden = true
            effectBtn.isHidden = true
            joinChorusBtn.isHidden = false
            joinChorusBtn.isSelected = false
            leaveChorusBtn.isHidden = true
            musicNameBtn.isHidden = false
            noSongLabel.isHidden = true
            lrcView.isHidden = false
            chorusNumBtn.isHidden = false
            scoreLabel.isHidden = false
            resultView.isHidden = true
            skipBtn.isHidden = true
        case .joinChorus:
            pauseBtn.isHidden = true
            nextBtn.isHidden = true
            originBtn.isHidden = true
            effectBtn.isHidden = true
            joinChorusBtn.isHidden = false
            joinChorusBtn.isSelected = false
            leaveChorusBtn.isHidden = true
            joinChorusBtn.isSelected = false
            musicNameBtn.isHidden = false
            noSongLabel.isHidden = true
            lrcView.isHidden = false
            chorusNumBtn.isHidden = false
            scoreLabel.isHidden = false
            resultView.isHidden = true
            skipBtn.isHidden = true
            originBtn.isSelected = false
        case .beforeJoinChorus:
            pauseBtn.isHidden = true
            nextBtn.isHidden = true
            originBtn.isHidden = true
            originBtn.isSelected = false
            effectBtn.isHidden = true
            joinChorusBtn.isHidden = false
            joinChorusBtn.isSelected = true
            leaveChorusBtn.isHidden = true
            musicNameBtn.isHidden = false
            noSongLabel.isHidden = true
            lrcView.isHidden = false
            chorusNumBtn.isHidden = false
            scoreLabel.isHidden = false
            resultView.isHidden = true
            skipBtn.isHidden = true
        case .nextSong:
            pauseBtn.isHidden = true
            nextBtn.isHidden = true
            originBtn.isHidden = true
            effectBtn.isHidden = true
            joinChorusBtn.isHidden = true
            leaveChorusBtn.isHidden = true
            musicNameBtn.isHidden = true
            noSongLabel.isHidden = true
            lrcView.isHidden = true
            chorusNumBtn.isHidden = true
            scoreLabel.isHidden = true
            resultView.isHidden = false
            skipBtn.isHidden = true
        }
        
        if self.leaveChorusBtn.isHidden == true {
            nextBtn.frame = CGRect(x: pauseBtn.isHidden ? 20 : 74, y: self.bounds.maxY - 50, width: 34, height: 40)
        } else {
            if controlState == .ownerChorusSing {
                nextBtn.frame = CGRect(x: 84, y: self.bounds.maxY - 50, width: 34, height: 40)
            }
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        musicNameBtn = UIButton(frame: CGRect(x: 20, y: 0, width: 300, height: 30))
        musicNameBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        musicNameBtn.contentMode = .scaleAspectFill
        musicNameBtn.setImage(UIImage.sceneImage(name: "ktv_currentPlay_icon", bundleName: "DHCResource"), for: .normal)
        musicNameBtn.contentHorizontalAlignment = .left
//        let spacing: CGFloat = 10
//        musicNameBtn.imageEdgeInsets = UIEdgeInsets(top: 0, left: -spacing, bottom: 0, right: 0)
        musicNameBtn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
        addSubview(musicNameBtn)
        musicNameBtn.isHidden = true
        
        chorusNumBtn = UIButton(frame: CGRect(x: self.bounds.width - 80, y: 0, width: 60, height: 30))
        chorusNumBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        chorusNumBtn.addTarget(self, action: #selector(showChorus), for: .touchUpInside)
        addSubview(chorusNumBtn)
        
        scoreLabel = UILabel(frame: CGRect(x: self.bounds.width - 190, y: 0, width: 100, height: 30))
        scoreLabel.textColor = .white
        scoreLabel.font = UIFont.systemFont(ofSize: 12)
        scoreLabel.textAlignment = .right
        addSubview(scoreLabel)
        setScore(with: 0)
        
        noSongLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 200, height: 50))
        noSongLabel.center = self.center
        noSongLabel.text = "当前无人点歌，\n快选择歌曲一起合唱吧!"
        noSongLabel.textColor = .white
        noSongLabel.font = UIFont.systemFont(ofSize: 13)
        noSongLabel.textAlignment = .center
        noSongLabel.numberOfLines = 0
        addSubview(noSongLabel)
        noSongLabel.isHidden = true
        
        lrcView = KaraokeView(frame: CGRectMake(0, 30, self.bounds.width, bounds.size.height - 30 - 50 - 340), loggers: [FileLogger()])
        lrcView.scoringEnabled = false
        lrcView.lyricsView.textNormalColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.5)
        lrcView.lyricsView.textHighlightedColor = UIColor(hexString: "#EEFF25")!
        lrcView.lyricsView.lyricLineSpacing = 6
        lrcView.lyricsView.draggable = false
        lrcView.delegate = self
        addSubview(lrcView!)
        
        //重试歌词按钮
        retryBtn = UIButton(frame: CGRect(x: 0, y: 0, width: 100, height: 40))
        retryBtn.center = lrcView.center
        retryBtn.setTitle("点击重试", for: .normal)
        retryBtn.layer.cornerRadius = 5
        retryBtn.layer.masksToBounds = true
        retryBtn.layer.borderColor = UIColor.white.cgColor
        retryBtn.layer.borderWidth = 1
        retryBtn.addTarget(self, action: #selector(retryLrc), for: .touchUpInside)
        lrcView.addSubview(retryBtn)
        retryBtn.isHidden = true

        pauseBtn = UIButton(frame: CGRect(x: 20, y: self.bounds.maxY - 50, width: 34, height: 40))
        pauseBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        pauseBtn.setVerticalLayoutWithCenterAlignment(title: "播放", image: UIImage.sceneImage(name: "ktv_pause_resumeicon", bundleName: "DHCResource")!, spacing: 0, for: .selected)
        pauseBtn.setVerticalLayoutWithCenterAlignment(title: "暂停", image: UIImage.sceneImage(name: "ktv_pause_icon", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        pauseBtn.addTarget(self, action: #selector(pause), for: .touchUpInside)
        addSubview(pauseBtn)
        
        nextBtn = UIButton(frame: CGRect(x: 74, y: self.bounds.maxY - 50, width: 34, height: 40))
        nextBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        nextBtn.addTarget(self, action: #selector(nextSong), for: .touchUpInside)
        nextBtn.setVerticalLayoutWithCenterAlignment(title: "切歌", image: UIImage.sceneImage(name: "ktv_playNext_icon", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(nextBtn)
        
        originBtn = UIButton(frame: CGRect(x: self.bounds.width - 54, y: self.bounds.maxY - 50, width: 34, height: 40))
        originBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        originBtn.addTarget(self, action: #selector(trackChange), for: .touchUpInside)
        originBtn.setVerticalLayoutWithCenterAlignment(title: "原唱", image: UIImage.sceneImage(name: "original", bundleName: "DHCResource")!, spacing: 0, for: .selected)
        originBtn.setVerticalLayoutWithCenterAlignment(title: "原唱", image: UIImage.sceneImage(name: "acc", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(originBtn)
        
        effectBtn = UIButton(frame: CGRect(x: self.bounds.width - 108, y: self.bounds.maxY - 50, width: 34, height: 40))
        effectBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        effectBtn.addTarget(self, action: #selector(effectChange), for: .touchUpInside)
        effectBtn.setVerticalLayoutWithCenterAlignment(title: "调音", image: UIImage.sceneImage(name: "ktv_subtitle_icon", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(effectBtn)
        
        leaveChorusBtn = UIButton(frame: CGRect(x: 20, y: self.bounds.maxY - 50, width: 50, height: 40))
        leaveChorusBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        leaveChorusBtn.addTarget(self, action: #selector(leaveChorus), for: .touchUpInside)
        leaveChorusBtn.setVerticalLayoutWithCenterAlignment(title: "退出合唱", image: UIImage.sceneImage(name: "ic_leave_chorus", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(leaveChorusBtn)
        leaveChorusBtn.isHidden = true
        
        joinChorusBtn = UIButton(frame: CGRect(x: centerX - 61, y: self.bounds.maxY - 50 , width: 122, height: 38))
        joinChorusBtn.addTarget(self, action: #selector(joinChorus), for: .touchUpInside)
        joinChorusBtn.setImage(UIImage.sceneImage(name: "join", bundleName: "DHCResource"), for: .normal)
        joinChorusBtn.setImage(UIImage.sceneImage(name: "ic_join_chorus_loading", bundleName: "DHCResource"), for: .selected)
        addSubview(joinChorusBtn)
        
        resultView = DHCResultView()
        resultView.frame = CGRect(x: 0, y: 0, width: ScreenWidth, height: 600)
        resultView.nextBlock = {[weak self] in
            guard let self = self, let delegate = self.delegate else {return}
            delegate.didGameEventChanged(with: .resultNext)
        }
        addSubview(resultView)

        loadingView = AUIKaraokeLoadingView(frame: CGRectMake(0, 30, self.bounds.width, 110))
        addSubview(loadingView)
        loadingView.isHidden = true
        
        setupSkipBtn()
    }
    
    public func hideBotView() {
        pauseBtn.isHidden = true
        nextBtn.isHidden = true
        effectBtn.isHidden = true
        originBtn.isHidden = true
        leaveChorusBtn.isHidden = true
        joinChorusBtn.isHidden = true
        resultView.isHidden = true
        lrcView.isHidden = true
    }
    
    private func setupSkipBtn() {
        let frame = CGRect(x: centerX - 61, y: self.bounds.maxY - 50 , width: 122, height: 38)
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
        addSubview(skipBtn)
        skipBtn.isHidden = true
    }
    
    @objc private func pause( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        btn.isSelected = !btn.isSelected
        delegate.didGameEventChanged(with: btn.isSelected ? .pause : .play)
    }
    
    @objc private func nextSong( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .next)
    }
    
    @objc private func trackChange( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        btn.isSelected = !btn.isSelected
        delegate.didGameEventChanged(with: btn.isSelected ? .origin : .acc)
    }
    
    @objc private func effectChange( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .effect)
    }
    
    @objc private func leaveChorus( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .leave)
    }
    
    @objc private func joinChorus( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .join)
    }
    
    @objc private func showChorus() {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .showChorus)
    }
    
    @objc private func retryLrc() {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .retryLrc)
    }
    
    public func resetStatus() {
        pauseBtn.isSelected = false
        originBtn.isSelected = false
        lrcView.reset()
        skipBtn.isHidden = true
        musicNameBtn.isHidden = true
        musicNameBtn.setTitle("", for: .normal)
        scoreLabel.text = "0总分"
        currentLoadLrcPath = nil
    }
    
    public func updateLoadingView(with progress: Int) {
        DispatchQueue.main.async {[weak self] in
            if progress == 100 {
                self?.loadingView.isHidden = true
            } else {
                self?.loadingView.isHidden = false
                self?.loadingView.setProgress(progress)
            }
        }
    }

    public func updateButtonLayout(button: UIButton, title: String, image: UIImage, imageInsets: UIEdgeInsets, x: CGFloat, y: CGFloat) {
        let titleAttributedString = NSAttributedString(string: title)

        let imageAttachment = NSTextAttachment()
        imageAttachment.image = image
        imageAttachment.bounds = CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height)
        let imageAttributedString = NSAttributedString(attachment: imageAttachment)

        let attributedString = NSMutableAttributedString()
        attributedString.append(imageAttributedString)
        attributedString.append(titleAttributedString)

        let size = attributedString.size()

        let buttonWidth = size.width + image.size.width + imageInsets.left + imageInsets.right
        let buttonHeight = max(size.height, image.size.height)

        button.frame = CGRect(x: x, y: y, width: buttonWidth, height: buttonHeight)
        button.setAttributedTitleWithImage(title: title, image: image, imageInsets: imageInsets)
    }
    
    @objc public func hideSkipView(flag: Bool) {
        DispatchQueue.main.async {
            self.skipBtn.isHidden = flag
        }
    }

    @objc public func showPreludeEnd() {
        if hasShowOnce {return}
        //显示跳过前奏
        DispatchQueue.main.async {
            self.skipBtn.setSkipType(.prelude)
            self.skipBtn.isHidden = false
            self.hasShowEndPosition = false
            self.hasShowPreludeEndPosition = false
        }
    }
    
    @objc public func resetShowOnce() {
        hasShowOnce = false
    }
}

extension DHCLRCControl: KaraokeDelegate {
    func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
        totalLines = lineCount
        totalScore += score
        guard let delegate = lrcDelegate else {
            return
        }
        delegate.didLrcViewScorllFinished(with: totalScore,
                                          totalScore: lineCount * 100,
                                          lineScore: score,
                                          lineIndex: lineIndex)
    }
    
    func onKaraokeView(view: KaraokeView, didDragTo position: Int) {
        totalScore = view.scoringView.getCumulativeScore()
        guard let delegate = lrcDelegate else {
            return
        }
        delegate.didLrcViewDragedTo(pos: position,
                                    score: totalScore,
                                    totalScore: totalCount * 100)
    }
}

extension DHCLRCControl: KTVLrcViewDelegate {
    func onUpdatePitch(pitch: Float) {
        lrcView.setPitch(pitch: Double(pitch))
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
        startDownloadLrc(with: url) {[weak self] url in
            guard let self = self, let url = url else {return}
            print("获取地址:\(url)")
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
            print("获取地址本地:\(path)")
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
        lyricModel = model
        totalCount = model.lines.count
        totalLines = 0
        totalScore = 0
        lrcView?.setLyricData(data: model)
        musicNameBtn.setTitle("\(model.name)", for: .normal)
      //  musicNameBtn.setTitle("\(model.name)-\(model.singer)", for: .normal)
        musicNameBtn.isHidden = false
        skipBtn.setSkipType(.prelude)
        print("获取地址解析:\(url)")
    }
    
    func onHighPartTime(highStartTime: Int, highEndTime: Int) {
        
    }
    
    public func setChoursNum(with count: Int) {
        chorusNumBtn.setTitle("\(count)人合唱中", for: .normal)
    }
    
    public func setScore(with score: Int) {
        DispatchQueue.main.async {
            self.scoreLabel.text = "\(score) 总分"
            let width = self.calculateLabelWidth(text: self.scoreLabel.text!, font: UIFont.systemFont(ofSize: 12))
            self.scoreLabel.frame = CGRect(x: self.bounds.width - width - 90, y: 0, width: width, height: 30)
            self.musicNameBtn.frame =  CGRect(x: 20, y: 0, width: self.bounds.width - width - 90 - 20 - 10, height: 30)
        }
    }

    public func setResultData(with totalScore: Int, models:[SubRankModel], musicStr: String, isRoomOwner: Bool) {
        resultView.setResultData(with: totalScore, models: models, musicStr: musicStr, isRoomOwner: isRoomOwner)
    }
    
    func calculateLabelWidth(text: String, font: UIFont) -> CGFloat {
        let label = UILabel()
        label.text = text
        label.font = font
        label.numberOfLines = 1 // 设置为单行显示
        let size = label.sizeThatFits(CGSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude))
        return size.width
    }
}

extension UIButton {
    func setAttributedTitleWithImage(title: String, image: UIImage, imageInsets: UIEdgeInsets) {
        let attributedString = NSMutableAttributedString()
        
        // 创建附带图片的文本附件
        let imageAttachment = NSTextAttachment()
        imageAttachment.image = image
        imageAttachment.bounds = CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height)
        let imageAttributedString = NSAttributedString(attachment: imageAttachment)
        attributedString.append(imageAttributedString)
        
        // 创建带有文本的富文本
        let titleAttributedString = NSAttributedString(string: title)
        attributedString.append(titleAttributedString)
        
        // 设置按钮的富文本标题和图片间距
        self.setAttributedTitle(attributedString, for: .normal)
        self.titleEdgeInsets = UIEdgeInsets(top: 0, left: image.size.width + imageInsets.left, bottom: 0, right: -image.size.width - imageInsets.right)
        self.imageEdgeInsets = UIEdgeInsets(top: imageInsets.top, left: -imageInsets.left, bottom: imageInsets.bottom, right: imageInsets.right)
    }
}

extension UIButton {
    func setVerticalLayoutWithCenterAlignment(title: String, image: UIImage, spacing: CGFloat, for state: UIControl.State) {
        self.setTitle(title, for: state)
        self.setImage(image, for: state)
        
        // 设置图片和标题的布局
        self.imageView?.contentMode = .scaleAspectFit
        self.titleLabel?.textAlignment = .center
        
        // 计算图片和标题的实际大小
        let imageSize = image.size
        let titleSize = (title as NSString).size(withAttributes: [NSAttributedString.Key.font: self.titleLabel!.font!])
        
        // 设置内边距以及垂直间距
        let imageInsets = UIEdgeInsets(top: -(titleSize.height + spacing), left: 0, bottom: 0, right: -titleSize.width)
        let titleInsets = UIEdgeInsets(top: 0, left: -imageSize.width, bottom: -(imageSize.height + spacing), right: 0)
        
        // 设置按钮的布局
        self.imageEdgeInsets = imageInsets
        self.titleEdgeInsets = titleInsets
        self.contentEdgeInsets = UIEdgeInsets(top: spacing/2, left: 0, bottom: spacing/2, right: 0)
    }
}
