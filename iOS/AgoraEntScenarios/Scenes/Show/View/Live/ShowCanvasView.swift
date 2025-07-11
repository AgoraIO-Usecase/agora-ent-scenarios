//
//  ShowCanvasView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/24.
//

import UIKit
import Agora_Scene_Utils
import AgoraCommon
enum ShowLiveCanvasType {
    case none
    case pk
    case joint_broadcasting // 连麦
}

protocol ShowCanvasViewDelegate: NSObjectProtocol {
    func onClickRemoteCanvas()
    func onPKDidTimeout()
    func getPKDuration() -> UInt64
}

class ShowCanvasView: UIView {
    weak var delegate: ShowCanvasViewDelegate?
    
    lazy var thumnbnailCanvasView: ShowThumnbnailCanvasView = {
        let view = ShowThumnbnailCanvasView(frame: self.bounds)
        view.isHidden = true
        return view
    }()
    
    lazy var localView = UIView()
    lazy var remoteView: UIView = {
        let view = UIView()
        view.isHidden = true
//        view.addTarget(self, action: #selector(onTapRemoteButton), for: .touchUpInside)
        let tap = UITapGestureRecognizer(target: self, action: #selector(onTapRemoteButton))
        view.addGestureRecognizer(tap)
        view.addSubview(remoteBgView)
        remoteBgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        return view
    }()
    
    private lazy var remoteBgView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "141650")
        return view
    }()
    
    private lazy var broadcastorImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.cornerRadius(42)
        return imgView
    }()
    
    private lazy var audienceImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.cornerRadius(25)
        return imgView
    }()
    
    private lazy var localUser: AGEButton = {
        let button = AGEButton(type: .custom)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.setTitle("user name1", for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "mic"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.setImage(UIImage.show_sceneImage(name: "unmic"),
                        for: .selected,
                        postion: .right,
                        spacing: 5)
        return button
    }()
    private lazy var remoteUser: AGEButton = {
        let button = AGEButton(type: .custom)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.setTitle("user name2", for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "mic"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.setImage(UIImage.show_sceneImage(name: "unmic"),
                        for: .selected,
                        postion: .right,
                        spacing: 5)
        return button
    }()
    private lazy var remoteUserLabel: UILabel = {
        let label = UILabel()
        
        return label
    }()
    private lazy var timerView: UIButton = {
        let button = UIButton()
        button.setBackgroundImage(UIImage.show_sceneImage(name: "show_pk_timer_bg"), for: .normal)
        button.setTitle("PK 02:00", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.isUserInteractionEnabled = false
        return button
    }()
    private lazy var pkView: UIImageView = {
        let imageView = UIImageView(image: UIImage.show_sceneImage(name: "show_pk_progress_bg"))
        return imageView
    }()
    private lazy var timer = GCDTimer()
    
    private var localCanvasWCons: NSLayoutConstraint?
    private var localCanvasHCons: NSLayoutConstraint?
    private var localCanvasTopCons: NSLayoutConstraint?
    private var remoteCanvasRCons: NSLayoutConstraint?
    private var remoteCanvasTCons: NSLayoutConstraint?
    private var remoteCanvasWCons: NSLayoutConstraint?
    private var remoteCanvasHCons: NSLayoutConstraint?
    private var localUserBottomCons: NSLayoutConstraint?
    
    var canvasType: ShowLiveCanvasType = .none {
        didSet {
            switch canvasType {
            case .none:
                localUser.isHidden = true
                remoteView.isHidden = true
                remoteUser.isHidden = true
                timerView.isHidden = true
                pkView.isHidden = true
                broadcastorImgView.isHidden = true
                audienceImgView.isHidden = true
                localCanvasHCons?.constant = Screen.height
                localCanvasWCons?.constant = Screen.width
                localCanvasTopCons?.constant = 0
                timer.destoryAllTimer()
                
            case .pk:
                localUser.isHidden = false
                remoteUser.isHidden = false
                remoteView.isHidden = false
                timerView.isHidden = false
                pkView.isHidden = false
                broadcastorImgView.isHidden = true
                audienceImgView.isHidden = true
                localCanvasHCons?.constant = 315
                localCanvasWCons?.constant = Screen.width * 0.5
                localCanvasTopCons?.constant = 76 + Screen.safeAreaTopHeight() + timerView.height + 15
                remoteCanvasRCons?.constant = 0
                remoteCanvasTCons?.constant = 15
                remoteCanvasHCons?.constant = 315
                remoteCanvasWCons?.constant = Screen.width * 0.5
                localUserBottomCons?.constant = -10
                remoteView.cornerRadius(0)
                remoteBgView.cornerRadius(0)
                timer.scheduledSecondsTimer(withName: "pk", timeInterval: 1, queue: .main) { [weak self] _, duration in
                    guard let self = self else { return }
                    let maxTime: TimeInterval = TimeInterval(AppContext.shared.sceneConfig?.showpk ?? 120)
                    var timeLeft = maxTime - TimeInterval((self.delegate?.getPKDuration() ?? 0) / 1000)
                    if timeLeft < 0 {
                        self.timer.destoryAllTimer()
                        self.delegate?.onPKDidTimeout()
                        timeLeft = 0
                    }
                    self.timerView.setTitle("PK "+"".timeFormat(secounds: timeLeft), for: .normal)
                }
                
            case .joint_broadcasting:
                remoteView.isHidden = false
                remoteUser.isHidden = false
                timerView.isHidden = true
                pkView.isHidden = true
                localUser.isHidden = true
                broadcastorImgView.isHidden = true
                audienceImgView.isHidden = true
                localCanvasHCons?.constant = Screen.height
                localCanvasWCons?.constant = Screen.width
                localCanvasTopCons?.constant = 0
                remoteCanvasRCons?.constant = -15
                remoteCanvasTCons?.constant = -30
                remoteCanvasHCons?.constant = 163
                remoteCanvasWCons?.constant = 109
//                localUserBottomCons?.constant = -468 / 812 * Screen.height
                remoteView.cornerRadius(20)
                remoteBgView.cornerRadius(20)
                timer.destoryAllTimer()
            }
            localCanvasHCons?.isActive = true
            localCanvasWCons?.isActive = true
            localCanvasTopCons?.isActive = true
            remoteCanvasRCons?.isActive = true
            remoteCanvasTCons?.isActive = true
            remoteCanvasHCons?.isActive = true
            remoteCanvasWCons?.isActive = true
        }
    }
    var isLocalMuteMic: Bool = false {
        didSet {
            localUser.isSelected = isLocalMuteMic
        }
    }
    var isRemoteMuteMic: Bool = false {
        didSet {
            remoteUser.isSelected = isRemoteMuteMic
        }
    }
    
    var isLocalVideoEnable: Bool = false {
        didSet {
            audienceImgView.isHidden = isLocalVideoEnable
            localUser.isHidden = isLocalVideoEnable
        }
    }
    
    var isRemoteVideoEnable: Bool = false {
        didSet {
            broadcastorImgView.isHidden = isRemoteVideoEnable
            remoteUser.isHidden = isRemoteVideoEnable
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        defer {
            canvasType = .none
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setLocalUserInfo(name: String, img: String? = nil) {
        localUser.setTitle(name, for: .normal)
        if let img = img {
            broadcastorImgView.sd_setImage(with: URL(string: img))
        }
    }
    
    func setRemoteUserInfo(name: String, img: String? = nil) {
        remoteUser.setTitle(name, for: .normal)
        if let img = img {
            audienceImgView.sd_setImage(with: URL(string: img))
        }
    }
    
    private func setupUI() {
        addSubview(localView)
        addSubview(thumnbnailCanvasView)
        thumnbnailCanvasView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        addSubview(remoteView)
        addSubview(timerView)
        addSubview(pkView)
        addSubview(localUser)
        addSubview(remoteUser)
        addSubview(broadcastorImgView)
        addSubview(audienceImgView)
        timerView.translatesAutoresizingMaskIntoConstraints = false
        localView.translatesAutoresizingMaskIntoConstraints = false
        remoteView.translatesAutoresizingMaskIntoConstraints = false
        pkView.translatesAutoresizingMaskIntoConstraints = false
        localUser.translatesAutoresizingMaskIntoConstraints = false
        remoteUser.translatesAutoresizingMaskIntoConstraints = false
        broadcastorImgView.translatesAutoresizingMaskIntoConstraints = false
        audienceImgView.translatesAutoresizingMaskIntoConstraints = false
                
        timerView.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        timerView.topAnchor.constraint(equalTo: topAnchor,
                                       constant: 76 + Screen.safeAreaTopHeight()).isActive = true
        
        localView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        localCanvasTopCons = localView.topAnchor.constraint(equalTo: topAnchor, constant: 0)
        localCanvasTopCons?.isActive = true
        localCanvasWCons = localView.widthAnchor.constraint(equalToConstant: Screen.width)
        localCanvasWCons?.isActive = true
        localCanvasHCons = localView.heightAnchor.constraint(equalToConstant: Screen.height)
        localCanvasHCons?.isActive = true
        
        remoteCanvasRCons = remoteView.trailingAnchor.constraint(equalTo: trailingAnchor)
        remoteCanvasRCons?.isActive = true
        remoteCanvasWCons = remoteView.widthAnchor.constraint(equalToConstant: Screen.width * 0.5)
        remoteCanvasWCons?.isActive = true
        remoteCanvasHCons = remoteView.heightAnchor.constraint(equalToConstant: 315)
        remoteCanvasHCons?.isActive = true
        remoteCanvasTCons = remoteView.topAnchor.constraint(equalTo: timerView.bottomAnchor, constant: 15)
        remoteCanvasTCons?.isActive = true
        
        localUser.centerXAnchor.constraint(equalTo: localView.centerXAnchor).isActive = true
        localUserBottomCons = localUser.bottomAnchor.constraint(equalTo: localView.bottomAnchor, constant: -10)
        localUserBottomCons?.priority = .defaultHigh
        localUserBottomCons?.isActive = true
        remoteUser.centerXAnchor.constraint(equalTo: remoteView.centerXAnchor).isActive = true
        remoteUser.bottomAnchor.constraint(equalTo: remoteView.bottomAnchor, constant: -10).isActive = true
        
        broadcastorImgView.centerXAnchor.constraint(equalTo: localView.centerXAnchor).isActive = true
        broadcastorImgView.bottomAnchor.constraint(equalTo: localUser.topAnchor, constant: -15).isActive = true
        broadcastorImgView.widthAnchor.constraint(equalToConstant: 84).isActive = true
        broadcastorImgView.heightAnchor.constraint(equalToConstant: 84).isActive = true
        
        audienceImgView.centerXAnchor.constraint(equalTo: remoteView.centerXAnchor).isActive = true
        audienceImgView.bottomAnchor.constraint(equalTo: remoteUser.topAnchor, constant: -10).isActive = true
        audienceImgView.widthAnchor.constraint(equalToConstant: 50).isActive = true
        audienceImgView.heightAnchor.constraint(equalToConstant: 50).isActive = true
        
        pkView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 15).isActive = true
        pkView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -15).isActive = true
        pkView.topAnchor.constraint(equalTo: remoteView.bottomAnchor, constant: 12).isActive = true
    }
    
    @objc
    private func onTapRemoteButton() {
        delegate?.onClickRemoteCanvas()
    }
}
