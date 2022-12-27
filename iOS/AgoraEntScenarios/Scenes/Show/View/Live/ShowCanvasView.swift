//
//  ShowCanvasView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/24.
//

import UIKit
import Agora_Scene_Utils

enum ShowLiveCanvasType {
    case none
    case pk
    case joint_broadcasting // 连麦
}

protocol ShowCanvasViewDelegate: NSObjectProtocol {
    func onClickRemoteCanvas()
    func onPKDidTimeout()
}

class ShowCanvasView: UIView {
    weak var delegate: ShowCanvasViewDelegate?
    
    lazy var localView = UIView()
    lazy var remoteView: UIView = {
        let view = UIView()
        view.isHidden = true
//        view.addTarget(self, action: #selector(onTapRemoteButton), for: .touchUpInside)
        let tap = UITapGestureRecognizer(target: self, action: #selector(onTapRemoteButton))
        view.addGestureRecognizer(tap)
        return view
    }()
    
    private lazy var localUser: AGEButton = {
        let button = AGEButton()
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.setTitle("user name1", for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "show_mic"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.setImage(UIImage.show_sceneImage(name: "show_mic_off"),
                        for: .selected,
                        postion: .right,
                        spacing: 5)
        return button
    }()
    private lazy var remoteUser: AGEButton = {
        let button = AGEButton()
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.setTitle("user name2", for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "show_mic"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.setImage(UIImage.show_sceneImage(name: "show_mic_off"),
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
    
    var canvasType: ShowLiveCanvasType = .none {
        didSet {
            switch canvasType {
            case .none:
                localUser.isHidden = true
                remoteView.isHidden = true
                remoteUser.isHidden = true
                timerView.isHidden = true
                pkView.isHidden = true
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
                localCanvasHCons?.constant = 315
                localCanvasWCons?.constant = Screen.width * 0.5
                localCanvasTopCons?.constant = 76 + Screen.safeAreaTopHeight() + timerView.height + 15
                remoteCanvasRCons?.constant = 0
                remoteCanvasTCons?.constant = 15
                remoteCanvasHCons?.constant = 315
                remoteCanvasWCons?.constant = Screen.width * 0.5
                remoteView.cornerRadius(0)
                timer.scheduledSecondsTimer(withName: "pk", timeInterval: 1, queue: .main) { [weak self] _, duration in
                    guard let self = self else { return }
                    var timeLeft = 120 - duration
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
                localCanvasHCons?.constant = Screen.height
                localCanvasWCons?.constant = Screen.width
                localCanvasTopCons?.constant = 0
                remoteCanvasRCons?.constant = -15
                remoteCanvasTCons?.constant = -30
                remoteCanvasHCons?.constant = 163
                remoteCanvasWCons?.constant = 109
                remoteView.cornerRadius(20)
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
    
    func setLocalUserInfo(name: String) {
        localUser.setTitle(name, for: .normal)
    }
    
    func setRemoteUserInfo(name: String) {
        remoteUser.setTitle(name, for: .normal)
    }
    
    private func setupUI() {
        addSubview(localView)
        addSubview(remoteView)
        addSubview(timerView)
        addSubview(pkView)
        addSubview(localUser)
        addSubview(remoteUser)
        timerView.translatesAutoresizingMaskIntoConstraints = false
        localView.translatesAutoresizingMaskIntoConstraints = false
        remoteView.translatesAutoresizingMaskIntoConstraints = false
        pkView.translatesAutoresizingMaskIntoConstraints = false
        localUser.translatesAutoresizingMaskIntoConstraints = false
        remoteUser.translatesAutoresizingMaskIntoConstraints = false
                
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
        localUser.bottomAnchor.constraint(equalTo: localView.bottomAnchor, constant: -10).isActive = true
        remoteUser.centerXAnchor.constraint(equalTo: remoteView.centerXAnchor).isActive = true
        remoteUser.bottomAnchor.constraint(equalTo: remoteView.bottomAnchor, constant: -10).isActive = true
        
        pkView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 15).isActive = true
        pkView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -15).isActive = true
        pkView.topAnchor.constraint(equalTo: remoteView.bottomAnchor, constant: 12).isActive = true
    }
    
    @objc
    private func onTapRemoteButton() {
        delegate?.onClickRemoteCanvas()
    }
}
