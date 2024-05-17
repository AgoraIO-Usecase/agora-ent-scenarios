//
//  VoiceMusicListView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/5/22.
//

import UIKit
import AgoraRtcKit
import AgoraCommon
enum VoiceMusicPlayStatus {
    case pause
    case download
    case playing
    case none
}

class VoiceMusicModel: NSObject {
    var name: String?
    var singer: String?
    var songCode: Int = 0
    var status: VoiceMusicPlayStatus = .none
}

class VoiceMusicListView: UIView {
    var backgroundMusicPlaying: ((VoiceMusicModel) -> Void)?
    var onClickAccompanyButtonClosure: ((Bool) -> Void)?
    private lazy var backButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "back", bundleName: "SpatialAudioResource"), for: .normal)
        button.addTargetFor(self, action: #selector(clickBackButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var lineView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "tchead", bundleName: "SpatialAudioResource"))
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "背景音乐".show_localized
        label.textColor = UIColor(hex: "#040925", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .plain)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.delegate = self
        tableView.dataSource = self
        tableView.separatorColor = .gray.withAlphaComponent(0.1)
        tableView.register(VoiceMusicListCell.self, forCellReuseIdentifier: "cell")
        tableView.translatesAutoresizingMaskIntoConstraints = false
        return tableView
    }()
    
    private lazy var musicToolView = VoiceMusicToolView()
    private var rtcKit: VoiceRoomRTCManager?
    private var currentMusic: VoiceMusicModel? {
        (musicList.isEmpty || currentIndex < 0) ? nil : musicList[currentIndex]
    }
    private var roomInfo: VRRoomInfo?
    private var musicList: [VoiceMusicModel] = []
    private var currentIndex: Int = -1
    private var isOrigin: Bool = true
    private var tableViewHCons: NSLayoutConstraint?
    private var downloadCaches: [VoiceMusicModel] = []
    
    init(rtcKit: VoiceRoomRTCManager?, currentMusic: VoiceMusicModel?, isOrigin: Bool, roomInfo: VRRoomInfo?) {
        super.init(frame: .zero)
        self.rtcKit = rtcKit
        self.currentIndex = musicList.firstIndex(where: { $0.songCode == currentMusic?.songCode }) ?? -1
        self.roomInfo = roomInfo
        self.isOrigin = isOrigin
        setupUI()
        getMusicList()
        musicToolView.roomInfo = roomInfo
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func show() {
        let height = 387 + 59 + 60 + Screen.safeAreaBottomHeight()
        let component = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                     height: height))
        let controller = VoiceRoomAlertViewController(compent: component, custom: self)
        VoiceRoomPresentView.shared.push(with: controller, frame: CGRect(x: 0,
                                                                         y: 0,
                                                                         width: ScreenWidth,
                                                                         height: height),
                                         maxHeight: Screen.height - 40)
        VoiceRoomPresentView.shared.panViewHeightClosure = { height in
            self.tableViewHCons?.constant = height - 59 - (60 + Screen.safeAreaBottomHeight())
            self.tableViewHCons?.isActive = true
            self.layoutIfNeeded()
        }
    }
    
    func show_present() {
        let height = 387 + 59 + 60 + Screen.safeAreaBottomHeight()
        let component = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                     height: height))
        let controller = VoiceRoomAlertViewController(compent: component, custom: self)
        let presentView: VoiceRoomPresentView = VoiceRoomPresentView.shared
        presentView.showView(with: CGRect(x: 0, y: 0, width: ScreenWidth, height: height), vc: controller, maxHeight: Screen.height - 40)
        let vc = UIViewController.cl_topViewController()
        vc?.view.addSubview(presentView)
        currentIndex = -1
        getMusicList()
        tableViewHCons?.constant = height - 59 - (60 + Screen.safeAreaBottomHeight())
        tableViewHCons?.isActive = true
        layoutIfNeeded()
        VoiceRoomPresentView.shared.panViewHeightClosure = { height in
            self.tableViewHCons?.constant = height - 59 - (60 + Screen.safeAreaBottomHeight())
            self.tableViewHCons?.isActive = true
            self.layoutIfNeeded()
        }
    }
    
    func updatePlayStatus(model: VoiceMusicModel?) {
        musicList.forEach({
            if $0.songCode == model?.songCode {
                $0.status = model?.status ?? .none
                musicToolView.setupMusicInfo(model: $0, isOrigin: roomInfo?.room?.musicIsOrigin ?? true)
                return
            }
        })
        tableView.reloadData()
    }
    
    private func getMusicList() {
        rtcKit?.fetchMusicList(musicListCallback: { [weak self] list in
            guard let self = self else { return }
            self.musicList = list.map({
                let model = VoiceMusicModel()
                model.name = $0.name
                model.singer = $0.singer
                model.songCode = $0.songCode
                if $0.songCode == self.currentMusic?.songCode ||
                    $0.songCode == self.roomInfo?.room?.backgroundMusic?.songCode {
                    let status = self.roomInfo?.room?.backgroundMusic?.status ?? .playing
                    model.status = status == .playing ? .playing : .download
                    self.musicToolView.setupMusicInfo(model: model, isOrigin: self.isOrigin)
                    self.backgroundMusicPlaying?(model)
                    self.currentIndex = list.firstIndex(where: { $0.songCode == model.songCode }) ?? -1
                }
                return model
            })
            self.titleLabel.text = "背景音乐".show_localized + "(\(list.count))"
            if !self.musicList.isEmpty && self.currentMusic == nil {
                self.musicList[0].status = .pause
                self.musicToolView.setupMusicInfo(model: self.musicList[0], isOrigin: self.isOrigin)
                self.currentIndex = 0
            }
            self.tableView.reloadData()
            self.tableView.scrollToRow(at: IndexPath(row: self.currentIndex,
                                                     section: 0), at: .middle,
                                       animated: true)
        })
    }
    
    private func eventHandler() {
        rtcKit?.backgroundMusicPlayingStatusClosure = { [weak self] state in
            guard let self = self else { return }
            DispatchQueue.main.async {
                if state == .playBackAllLoopsCompleted {
                    self.nextMusicHandler()
                } else if state == .paused {
                    self.playOrPauseHandler(isPlay: false)
                } else if state == .playing {
                    self.playOrPauseHandler(isPlay: true)
                    self.rtcKit?.selectPlayerTrackMode(isOrigin: self.roomInfo?.room?.musicIsOrigin ?? true)
                }
                self.musicToolView.updatePlayStatus(isPlaying: state == .playing)
            }
        }
        rtcKit?.downloadBackgroundMusicStatusClosure = { [weak self] songCode, progress, status in
            guard let self = self, self.currentMusic?.songCode == songCode else { return }
            let index = self.musicList.firstIndex(where: { $0.songCode == songCode }) ?? 0
            let model = self.musicList[index]
            model.status = status == .preloading ? .download : status == .OK ? .playing : .none
            guard progress <= 0 || progress == 100 else { return }
            DispatchQueue.main.async {
                let indexPath = IndexPath(row: index, section: 0)
                self.tableView.reloadRows(at: [indexPath], with: .none)
            }
        }
        musicToolView.onAdjustVolumnClosure = { [weak self] value in
            guard let self = self else { return }
            self.rtcKit?.adjustMusicVolume(volume: value)
        }
        musicToolView.onClickNextButtonClosure = { [weak self] in
            guard let self = self else { return }
            self.nextMusicHandler()
        }
        musicToolView.onClickPlayButtonClosure = { [weak self] isPlay in
            guard let self = self else { return }
            let code = self.playOrPauseHandler(isPlay: isPlay)
            if code != 0 {
                self.rtcKit?.playMusic(songCode: code)
            }
        }
        musicToolView.onClickAccompanyButtonClosure = { [weak self] isOrigin in
            guard let self = self, let model = self.currentMusic else { return }
            self.isOrigin = isOrigin
            self.onClickAccompanyButtonClosure?(isOrigin)
            ChatRoomServiceImp.getSharedInstance().updateRoomBGM(songName: model.name, singerName: model.singer, isOrigin: isOrigin)
        }
    }
    
    private func nextMusicHandler() {
        rtcKit?.stopMusic()
        musicList.forEach({ $0.status = .none })
        if self.currentIndex < 0, let model = self.musicList.first {
            model.status = .download
            rtcKit?.playMusic(songCode: model.songCode)
            tableView.reloadData()
            backgroundMusicPlaying?(model)
            musicToolView.setupMusicInfo(model: model, isOrigin: isOrigin)
            currentIndex += 1
        } else {
            currentIndex += 1
            currentIndex = currentIndex < musicList.count ? currentIndex : 0
            let model = musicList[currentIndex]
            model.status = .download
            rtcKit?.playMusic(songCode: model.songCode)
            tableView.reloadData()
            backgroundMusicPlaying?(model)
            musicToolView.setupMusicInfo(model: model, isOrigin: isOrigin)
            tableView.scrollToRow(at: IndexPath(row: currentIndex, section: 0), at: .middle, animated: true)
        }
    }
    
    @discardableResult
    private func playOrPauseHandler(isPlay: Bool) -> Int {
        if !musicList.isEmpty {
            let currentIndex = currentIndex < 0 ? 0 : currentIndex
            let currentModel = musicList[currentIndex]
            currentModel.status = isPlay ? .playing : .pause
            backgroundMusicPlaying?(currentModel)
            tableView.reloadData()
            return currentModel.songCode
        }
        return 0
    }
    
    private func setupUI() {
        backgroundColor = .white
        translatesAutoresizingMaskIntoConstraints = false
        widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        
        addSubview(lineView)
        lineView.addSubview(titleLabel)
        addSubview(backButton)
        backButton.translatesAutoresizingMaskIntoConstraints = false
        backButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        backButton.centerYAnchor.constraint(equalTo: titleLabel.centerYAnchor).isActive = true
        
        lineView.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        lineView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        lineView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 59).isActive = true
        lineView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: lineView.bottomAnchor, constant: -10).isActive = true
        
        addSubview(tableView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: lineView.bottomAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        tableViewHCons = tableView.heightAnchor.constraint(equalToConstant: 387)
        tableViewHCons?.isActive = true

        addSubview(musicToolView)
        musicToolView.translatesAutoresizingMaskIntoConstraints = false
        musicToolView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        musicToolView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        musicToolView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        musicToolView.topAnchor.constraint(equalTo: tableView.bottomAnchor).isActive = true
        musicToolView.heightAnchor.constraint(equalToConstant: 60 + Screen.safeAreaBottomHeight()).isActive = true
        
        eventHandler()
    }
    
    @objc
    private func clickBackButton(sender: UIButton) {
        VoiceRoomPresentView.shared.pop()  
    }
}

extension VoiceMusicListView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        musicList.count
    }
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! VoiceMusicListCell
        cell.setipMusicModel(model: musicList[indexPath.row])
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        musicList.forEach({ $0.status = .none })
        let model = musicList[indexPath.row]
        model.status = .download
        rtcKit?.stopMusic()
        rtcKit?.playMusic(songCode: model.songCode)
        currentIndex = indexPath.row
        musicToolView.setupMusicInfo(model: model, isOrigin: isOrigin)
        backgroundMusicPlaying?(model)
        tableView.reloadData()
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        40
    }
}

class VoiceMusicListCell: UITableViewCell {
    private lazy var songTitleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 16)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var singerLabeL: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 11)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var statusImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "voice_music_status"))
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        imageView.isHidden = true
        return imageView
    }()
    private lazy var indicatorView: UIActivityIndicatorView = {
        let view = UIActivityIndicatorView(style: .medium)
        view.isHidden = true
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    private lazy var gifData: Data = {
        let bundlePath = Bundle.main.path(forResource: AppContext.shared.sceneImageBundleName, ofType: "bundle")
        let bundle = Bundle(path: bundlePath ?? "")
        let path = bundle?.path(forResource: "play-24px", ofType: "gif") ?? ""
        let url = URL(fileURLWithPath: path)
        let gifData = try? Data(contentsOf: url)
        return gifData ?? Data()
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setipMusicModel(model: VoiceMusicModel) {
        songTitleLabel.text = model.name
        singerLabeL.text = model.singer
        
        statusImageView.isHidden = model.status == .download || model.status == .none
        model.status == .playing ? statusImageView.startAnimating() : statusImageView.stopAnimating()
        if model.status == .playing {
//            statusImageView.image = UIImage.sd_animatedGIF(with: gifData)
        } else if model.status == .pause {
            statusImageView.image = UIImage.sceneImage(name: "voice_music_play")
        }
        songTitleLabel.textColor = (model.status == .playing || model.status == .pause) ? UIColor(hexString: "#0A7AFF") : UIColor(hexString: "#3C4267")
        songTitleLabel.font = (model.status == .playing || model.status == .pause) ? .boldSystemFont(ofSize: 14) : .systemFont(ofSize: 14)
        singerLabeL.textColor = songTitleLabel.textColor
        indicatorView.isHidden = model.status != .download
        model.status == .download ? indicatorView.startAnimating() : indicatorView.stopAnimating()
    }
    
    private func setupUI() {
        contentView.addSubview(songTitleLabel)
        contentView.addSubview(singerLabeL)
        contentView.addSubview(statusImageView)
        contentView.addSubview(indicatorView)
        
        songTitleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        songTitleLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        singerLabeL.leadingAnchor.constraint(equalTo: songTitleLabel.trailingAnchor, constant: 12).isActive = true
        singerLabeL.centerYAnchor.constraint(equalTo: songTitleLabel.centerYAnchor).isActive = true
        
        statusImageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        statusImageView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        indicatorView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        indicatorView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
    }
}


class VoiceMusicToolView: UIView {
    var onAdjustVolumnClosure: ((Int) -> Void)?
    var onClickAccompanyButtonClosure: ((Bool) -> Void)?
    var onClickPlayButtonClosure: ((Bool) -> Void)?
    var onClickNextButtonClosure: (() -> Void)?
    
    var roomInfo: VRRoomInfo?
    
    private lazy var volumnButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "voice_volumn_icon"), for: .normal)
        button.setImage(UIImage.sceneImage(name: "voice_volumn_selected_icon"), for: .selected)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickVolumnButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 15)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        return label
    }()
    private lazy var singerLabeL: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 13)
        label.textAlignment = .left
        label.translatesAutoresizingMaskIntoConstraints = false
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        return label
    }()
    private lazy var accompanyButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "voice_accompany_on"), for: .normal)
        button.setImage(UIImage.sceneImage(name: "voice_accompany_off"), for: .selected)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickAccompanyButton(sender:)), for: .touchUpInside)
        button.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return button
    }()
    private lazy var playButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "voice_play"), for: .normal)
        button.setImage(UIImage.sceneImage(name: "voice_pause"), for: .selected)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickPlayButton(sender:)), for: .touchUpInside)
        button.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return button
    }()
    private lazy var nextButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "voice_next"), for: .normal)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickNextButton), for: .touchUpInside)
        button.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return button
    }()
    private lazy var sliderContaonerView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 8
        view.layer.shadowOffset = .zero
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOpacity = 0.3
        view.layer.shadowRadius = 8
        view.translatesAutoresizingMaskIntoConstraints = false
        view.alpha = 0
        return view
    }()
    private lazy var slider: UISlider = {
        let slider = UISlider()
        slider.value = 0.5
        slider.minimumTrackTintColor = UIColor(hex: "#009FFF", alpha: 1.0)
        slider.addTarget(self, action: #selector(onClickSlider(sender:)), for: .valueChanged)
        slider.addTarget(self, action: #selector(onClickSliderEnd(sender:)), for: .touchUpInside)
        slider.setContentHuggingPriority(.defaultLow, for: .horizontal)
        slider.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        slider.translatesAutoresizingMaskIntoConstraints = false
        slider.transform = CGAffineTransform(rotationAngle: -(.pi * 0.5))
        return slider
    }()
    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.backgroundColor = UIColor(hex: "#000000", alpha: 0.4)
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        label.layer.cornerRadius = 4
        label.layer.masksToBounds = true
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        label.alpha = 0
        return label
    }()
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updatePlayStatus(isPlaying: Bool) {
        playButton.isSelected = isPlaying
    }
    
    func setupMusicInfo(model: VoiceMusicModel, isOrigin: Bool) {
        titleLabel.text = model.name
        singerLabeL.text = model.singer
        playButton.isSelected = model.status == .playing
        accompanyButton.isSelected = isOrigin
    }
    
    private func setupUI() {
        backgroundColor = .white
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOpacity = 0.2
        layer.shadowRadius = 10
        layer.shadowOffset = CGSize(width: 0, height: 0)
        
        addSubview(volumnButton)
        addSubview(titleLabel)
        addSubview(singerLabeL)
        addSubview(accompanyButton)
        addSubview(playButton)
        addSubview(nextButton)
        
        volumnButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20).isActive = true
        volumnButton.topAnchor.constraint(equalTo: topAnchor, constant: 19).isActive = true
        volumnButton.widthAnchor.constraint(equalToConstant: 17).isActive = true
        
        titleLabel.leadingAnchor.constraint(equalTo: volumnButton.trailingAnchor, constant: 15).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: volumnButton.centerYAnchor).isActive = true
        
        singerLabeL.leadingAnchor.constraint(equalTo: titleLabel.trailingAnchor, constant: 12).isActive = true
        singerLabeL.centerYAnchor.constraint(equalTo: titleLabel.centerYAnchor).isActive = true
        singerLabeL.trailingAnchor.constraint(equalTo: accompanyButton.leadingAnchor, constant: -15).isActive = true
        
        nextButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20).isActive = true
        nextButton.centerYAnchor.constraint(equalTo: singerLabeL.centerYAnchor).isActive = true
        
        playButton.trailingAnchor.constraint(equalTo: nextButton.leadingAnchor, constant: -26).isActive = true
        playButton.centerYAnchor.constraint(equalTo: nextButton.centerYAnchor).isActive = true
        playButton.widthAnchor.constraint(equalToConstant: 24).isActive = true
        playButton.heightAnchor.constraint(equalToConstant: 24).isActive = true
        
        accompanyButton.trailingAnchor.constraint(equalTo: playButton.leadingAnchor, constant: -25).isActive = true
        accompanyButton.centerYAnchor.constraint(equalTo: playButton.centerYAnchor).isActive = true
    }
    
    override func didMoveToSuperview() {
        super.didMoveToSuperview()
        guard let superView = superview else { return }
        superView.addSubview(sliderContaonerView)
        sliderContaonerView.bottomAnchor.constraint(equalTo: topAnchor, constant: 8).isActive = true
        sliderContaonerView.centerXAnchor.constraint(equalTo: volumnButton.centerXAnchor).isActive = true
        sliderContaonerView.widthAnchor.constraint(equalToConstant: 34).isActive = true
        sliderContaonerView.heightAnchor.constraint(equalToConstant: 176).isActive = true
        
        sliderContaonerView.addSubview(slider)
        slider.centerXAnchor.constraint(equalTo: sliderContaonerView.centerXAnchor).isActive = true
        slider.centerYAnchor.constraint(equalTo: sliderContaonerView.centerYAnchor).isActive = true
        slider.widthAnchor.constraint(equalToConstant: 160).isActive = true
        
        superView.addSubview(valueLabel)
        valueLabel.leadingAnchor.constraint(equalTo: sliderContaonerView.trailingAnchor, constant: 4).isActive = true
        valueLabel.centerYAnchor.constraint(equalTo: sliderContaonerView.centerYAnchor).isActive = true
        valueLabel.widthAnchor.constraint(equalToConstant: 25).isActive = true
        valueLabel.heightAnchor.constraint(equalToConstant: 25).isActive = true
    }
    
    @objc
    private func onClickVolumnButton(sender: UIButton) {
        if roomInfo?.room?.owner?.uid != VLUserCenter.user.id {
            ToastView.show(text: "Host Music".voice_localized())
            return
        }
        sender.isSelected = !sender.isSelected
        UIView.animate(withDuration: 0.25) {
            self.sliderContaonerView.alpha = sender.isSelected ? 1.0 : 0.0
            if sender.isSelected == false {
                self.valueLabel.alpha = 0
            }
        }
    }
    @objc
    private func onClickAccompanyButton(sender: UIButton) {
        if roomInfo?.room?.owner?.uid != VLUserCenter.user.id {
            ToastView.show(text: "Host Music".voice_localized())
            return
        }
        sender.isSelected = !sender.isSelected
        onClickAccompanyButtonClosure?(sender.isSelected)
    }
    @objc
    private func onClickPlayButton(sender: UIButton) {
        if roomInfo?.room?.owner?.uid != VLUserCenter.user.id {
            ToastView.show(text: "Host Music".voice_localized())
            return
        }
        sender.isSelected = !sender.isSelected
        onClickPlayButtonClosure?(sender.isSelected)
    }
    @objc
    private func onClickNextButton() {
        if roomInfo?.room?.owner?.uid != VLUserCenter.user.id {
            ToastView.show(text: "Host Music".voice_localized())
            return
        }
        onClickNextButtonClosure?()
    }
    @objc
    private func onClickSlider(sender: UISlider) {
        let value = Int(sender.value * 100)
        valueLabel.text = "\(value)"
        if valueLabel.alpha != 1.0 {
            UIView.animate(withDuration: 0.25, animations: {
                self.valueLabel.alpha = 1.0
            })
        }
        onAdjustVolumnClosure?(value)
    }
    @objc
    private func onClickSliderEnd(sender: UISlider) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5, execute: {
            self.valueLabel.alpha = 0.0
        })
    }
}
