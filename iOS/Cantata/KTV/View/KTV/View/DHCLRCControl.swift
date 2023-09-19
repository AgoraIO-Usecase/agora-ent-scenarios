//
//  DHCLRCControl.swift
//  Cantata
//
//  Created by CP on 2023/9/4.
//

import UIKit
import AgoraLyricsScore

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
}

public enum DHCGameState: Int {
    case noSong
    case ownerSing //房主主唱
    case chorusSing //非房主主唱
    case ownerChorus //房主加入合唱
    case joinChorus //加入合唱
    case beforeJoinChorus //加入合唱loading的时候
    case nextSong //下一首 结算
    case nextGame //下一轮 结算
}

public protocol DHCGameDelegate: NSObjectProtocol {
    func didGameEventChanged(with event: DHCGameEvent)
}

class DHCLRCControl: UIView {
    @objc public var lrcView: KaraokeView!
    private var musicNameBtn: UIButton!
    private var pauseBtn: UIButton!
    private var nextBtn: UIButton!
    private var originBtn: UIButton! //原唱/伴奏
    private var effectBtn: UIButton! //调音
    private var joinChorusBtn: UIButton! //加入合唱
    private var leaveChorusBtn: UIButton! //离开合唱
    private var resultView: UIView! //结算界面
    private var noSongLabel: UILabel!
    private var chorusNumBtn: UIButton!
    weak var delegate: DHCGameDelegate?
    private var downloadManager = AgoraDownLoadManager()
    public var controlState: DHCGameState = .noSong {
        didSet {
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
            case .ownerChorus:
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
            case .joinChorus:
                pauseBtn.isHidden = true
                nextBtn.isHidden = true
                originBtn.isHidden = true
                effectBtn.isHidden = true
                joinChorusBtn.isHidden = false
                leaveChorusBtn.isHidden = true
                joinChorusBtn.isSelected = false
                musicNameBtn.isHidden = false
                noSongLabel.isHidden = true
                lrcView.isHidden = false
                chorusNumBtn.isHidden = false
            case .beforeJoinChorus:
                pauseBtn.isHidden = true
                nextBtn.isHidden = true
                originBtn.isHidden = true
                effectBtn.isHidden = true
                joinChorusBtn.isHidden = false
                joinChorusBtn.isSelected = true
                leaveChorusBtn.isHidden = true
                musicNameBtn.isHidden = false
                noSongLabel.isHidden = true
                lrcView.isHidden = false
                chorusNumBtn.isHidden = false
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
            case .nextGame:
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
        musicNameBtn.contentMode = .scaleAspectFit
        musicNameBtn.setImage(UIImage.sceneImage(name: "ktv_currentPlay_icon", bundleName: "DHCResource"), for: .normal)
        musicNameBtn.contentHorizontalAlignment = .left
        let spacing: CGFloat = 10
        musicNameBtn.imageEdgeInsets = UIEdgeInsets(top: 0, left: -spacing, bottom: 0, right: 0)
        musicNameBtn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
        addSubview(musicNameBtn)
        musicNameBtn.isHidden = true
        
        chorusNumBtn = UIButton(frame: CGRect(x: self.bounds.width - 80, y: 0, width: 60, height: 30))
        chorusNumBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        chorusNumBtn.addTarget(self, action: #selector(showChorus), for: .touchUpInside)
        addSubview(chorusNumBtn)
        
        noSongLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 200, height: 50))
        noSongLabel.center = self.center
        noSongLabel.text = "当前无人点歌，\n快选择歌曲一起合唱吧!"
        noSongLabel.textColor = .white
        noSongLabel.font = UIFont.systemFont(ofSize: 13)
        noSongLabel.textAlignment = .center
        noSongLabel.numberOfLines = 0
        addSubview(noSongLabel)
        noSongLabel.isHidden = true
        
        lrcView = KaraokeView(frame: CGRectMake(0, 30, self.bounds.width, 110), loggers: [FileLogger()])
        lrcView.scoringEnabled = false
        lrcView.lyricsView.textNormalColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.5)
        lrcView.lyricsView.textHighlightedColor = UIColor(hexString: "#EEFF25")!
        lrcView.lyricsView.lyricLineSpacing = 6
        lrcView.lyricsView.draggable = false
        lrcView.delegate = self
        addSubview(lrcView!)

        pauseBtn = UIButton(frame: CGRect(x: 20, y: self.bounds.maxY - 50, width: 34, height: 40))
        pauseBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        pauseBtn.setVerticalLayoutWithCenterAlignment(title: "暂停", image: UIImage.sceneImage(name: "ktv_pause_icon", bundleName: "DHCResource")!, spacing: 0, for: .selected)
        pauseBtn.setVerticalLayoutWithCenterAlignment(title: "播放", image: UIImage.sceneImage(name: "ktv_pause_icon", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        pauseBtn.addTarget(self, action: #selector(pause), for: .touchUpInside)
        addSubview(pauseBtn)
        
        nextBtn = UIButton(frame: CGRect(x: 74, y: self.bounds.maxY - 50, width: 34, height: 40))
        nextBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        pauseBtn.addTarget(self, action: #selector(nextSong), for: .touchUpInside)
        nextBtn.setVerticalLayoutWithCenterAlignment(title: "切歌", image: UIImage.sceneImage(name: "ktv_playNext_icon", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(nextBtn)
        
        originBtn = UIButton(frame: CGRect(x: self.bounds.width - 54, y: self.bounds.maxY - 50, width: 34, height: 40))
        originBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        pauseBtn.addTarget(self, action: #selector(trackChange), for: .touchUpInside)
        originBtn.setVerticalLayoutWithCenterAlignment(title: "原唱", image: UIImage.sceneImage(name: "original", bundleName: "DHCResource")!, spacing: 0, for: .selected)
        originBtn.setVerticalLayoutWithCenterAlignment(title: "伴奏", image: UIImage.sceneImage(name: "acc", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(originBtn)
        
        effectBtn = UIButton(frame: CGRect(x: self.bounds.width - 108, y: self.bounds.maxY - 50, width: 34, height: 40))
        effectBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        pauseBtn.addTarget(self, action: #selector(effectChange), for: .touchUpInside)
        effectBtn.setVerticalLayoutWithCenterAlignment(title: "调音", image: UIImage.sceneImage(name: "ktv_subtitle_icon", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(effectBtn)
        
        leaveChorusBtn = UIButton(frame: CGRect(x: 20, y: self.bounds.maxY - 50, width: 50, height: 40))
        leaveChorusBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        pauseBtn.addTarget(self, action: #selector(leaveChorus), for: .touchUpInside)
        leaveChorusBtn.setVerticalLayoutWithCenterAlignment(title: "退出合唱", image: UIImage.sceneImage(name: "ic_leave_chorus", bundleName: "DHCResource")!, spacing: 0, for: .normal)
        addSubview(leaveChorusBtn)
        leaveChorusBtn.isHidden = true
        
        joinChorusBtn = UIButton(frame: CGRect(x: centerX - 61, y: self.bounds.maxY - 50 , width: 122, height: 38))
        joinChorusBtn.addTarget(self, action: #selector(joinChorus), for: .touchUpInside)
        joinChorusBtn.setImage(UIImage.sceneImage(name: "join", bundleName: "DHCResource"), for: .normal)
        joinChorusBtn.setImage(UIImage.sceneImage(name: "ic_join_chorus_loading", bundleName: "DHCResource"), for: .selected)
        addSubview(joinChorusBtn)
    }
    
    @objc private func pause( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        btn.isSelected = !btn.isSelected
        delegate.didGameEventChanged(with: btn.isSelected ? .play : .pause)
    }
    
    @objc private func nextSong( btn: UIButton) {
        guard let delegate = self.delegate else {return}
        delegate.didGameEventChanged(with: .next)
    }
    
    @objc private func trackChange( btn: UIButton) {
        guard let delegate = self.delegate else {return}
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
}

extension DHCLRCControl: KaraokeDelegate {
    func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
        
    }
}

extension DHCLRCControl: KTVLrcViewDelegate {
    func onUpdatePitch(pitch: Float) {
        lrcView.setPitch(pitch: Double(pitch))
    }
    
    func onUpdateProgress(progress: Int) {
        lrcView.setProgress(progress: progress)
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
        let musicUrl = URL(fileURLWithPath: url)
        guard let data = try? Data(contentsOf: musicUrl),
              let model = KaraokeView.parseLyricData(data: data) else {
            return
        }
        lrcView?.setLyricData(data: model)
        musicNameBtn.setTitle("\(model.name)-\(model.singer)", for: .normal)
        musicNameBtn.isHidden = false
    }
    
    func onHighPartTime(highStartTime: Int, highEndTime: Int) {
        
    }
    
    public func setChoursNum(with count: Int) {
        chorusNumBtn.setTitle("\(count)人合唱中", for: .normal)
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
