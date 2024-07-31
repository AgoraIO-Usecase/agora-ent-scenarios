//
//  PlayGameController.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import UIKit
import AgoraRtcKit
import AgoraCommon

class PlayGameViewController: UIViewController {
    static let SUDMGP_APP_ID = ""
    static let SUDMGP_APP_KEY = ""
    
    private lazy var navigationBar: GameNavigationBar = {
        let bar = GameNavigationBar()
        return bar
    }()
    
    private var userInfo: InteractiveJoyUserInfo? {
        didSet {
            navigationBar.roomInfoView.startTime(Int64(Date().timeIntervalSince1970 * 1000))
        }
    }
    private var service: JoyServiceProtocol!
    private var roomInfo: InteractiveJoyRoomInfo!
    private lazy var rtcEngine: AgoraRtcEngineKit = _createRtcEngine()
    
    private lazy var gameView: UIView = {
        let view = UIView()
        return view
    }()
    
    private lazy var backgroundView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage.sceneImage(name: "play_zone_room_bg")
        return imageView
    }()
    
    private lazy var bottomBar: ShowRoomBottomBar = {
        let bar = ShowRoomBottomBar(isBroadcastor: isRoomOwner())
        bar.delegate = self
        return bar
    }()
    
    private func _createRtcEngine() -> AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = joyAppId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let logConfig = AgoraLogConfig()
        logConfig.filePath = AgoraEntLog.sdkLogPath()
        config.logConfig = logConfig
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: self)
        engine.disableVideo()
        engine.enableAudio()
        engine.setVideoEncoderConfiguration(AgoraVideoEncoderConfiguration(size: CGSize(width: 320, height: 240),
                                                                           frameRate: .fps15,
                                                                             bitrate: AgoraVideoBitrateStandard,
                                                                           orientationMode: .fixedPortrait,
                                                                             mirrorMode: .auto))
        
        // set audio profile
        engine.setAudioProfile(.default)
        
        // Set audio route to speaker
        engine.setDefaultAudioRouteToSpeakerphone(true)
        
        // enable volume indicator
        engine.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)
        return engine
    }
    
    lazy var gameHandler: GameEventHandler = {
        let handler = GameEventHandler()
        handler.delegate = self
        handler.robotInfoList = self.service.getRobotList()
        return handler
    }()
    
    let gameManager: SudGameManager = SudGameManager()
    
    required init(userInfo: InteractiveJoyUserInfo, service: JoyServiceProtocol, roomInfo: InteractiveJoyRoomInfo) {
        super.init(nibName: nil, bundle: nil)
        self.userInfo = userInfo
        self.service = service
        self.roomInfo = roomInfo
        self.service.subscribeListener(listener: self)
        let createdAt = roomInfo.createdAt ?? 0
        navigationBar.roomInfoView.startTime(Int64(createdAt > 0 ? createdAt : Int64(Date().timeIntervalSince1970) * 1000))
        navigationBar.roomInfoView.timerCallBack = {[weak self] duration in
            if duration < 60 * 10 {
                return
            }
            self?.navigationBar.roomInfoView.stopTime()
            self?.onBackAction()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.white
        self.view.addSubview(backgroundView)
        self.view.addSubview(gameView)
        self.view.addSubview(navigationBar)
        self.view.addSubview(bottomBar)
        
        navigationBar.moreActionCallback = { [weak self] in
            guard let self = self else { return }
            
            let dialog = AUiMoreDialog(frame: view.bounds)
            self.view.addSubview(dialog)
            dialog.show()
        }
        
        navigationBar.closeActionCallback = { [weak self] in
            self?.showEndGameAlert()
        }
        
        backgroundView.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets.zero)
        }
        
        gameView.snp.makeConstraints { make in
            make.top.left.right.bottom.equalTo(0)
        }
        
        navigationBar.snp.makeConstraints { make in
            make.left.right.equalTo(0)
            make.height.equalTo(40)
            make.top.equalTo(UIDevice.current.aui_SafeDistanceTop)
        }
        
        bottomBar.snp.makeConstraints { make in
            make.bottom.equalToSuperview().offset(-UIDevice.current.aui_SafeDistanceBottom)
            make.left.right.equalToSuperview()
            make.height.equalTo(58)
        }
        
        gameManager.registerGameEventHandler(gameHandler)
        
        if roomInfo.gameId > 0 {
            loadGame(gameId: roomInfo.gameId)
        }
        
        navigationBar.roomInfoView.setRoomInfo(avatar: userInfo?.avatar, name: roomInfo.roomName, id: roomInfo.roomId)
        renewRTMTokens { [weak self] token in
            guard let self = self else {return}
            
            let option = AgoraRtcChannelMediaOptions()
            option.publishCameraTrack = true
            option.publishMicrophoneTrack = true
            option.clientRoleType = self.roomInfo.ownerId == userInfo!.userId ? .broadcaster : .audience
            let result = self.rtcEngine.joinChannel(byToken: token, channelId: roomInfo.roomId, uid: self.userInfo?.userId ?? 0, mediaOptions: option)
            if result != 0 {
                JoyLogger.error("join channel fail")
            }
        }
    }
    
    private func renewRTMTokens(completion: ((String?)->Void)?) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            JoyLogger.error("renewTokens fail,userInfo == nil")
            completion?(nil)
            return
        }
        JoyLogger.info("renewRTMTokens")
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: "\(userInfo.userId)",
                                            tokenTypes: [.rtc]) {[weak self] token in
            guard let self = self else {return}
            guard let rtmToken = token else {
                JoyLogger.warn("renewRTMTokens fail")
                completion?(nil)
                return
            }
            JoyLogger.info("renewRTMTokens success")
            completion?(rtmToken)
        }
    }
    
    private func loadGame(gameId: Int64) {
        let sudGameConfigModel = SudGameLoadConfigModel()
        sudGameConfigModel.appId = PlayGameViewController.SUDMGP_APP_ID
        sudGameConfigModel.appKey = PlayGameViewController.SUDMGP_APP_KEY
        sudGameConfigModel.isTestEnv = true
        sudGameConfigModel.gameId = gameId
        sudGameConfigModel.roomId = roomInfo.roomId
        sudGameConfigModel.language = "zh-CN"
        sudGameConfigModel.gameView = gameView
        sudGameConfigModel.userId = "\(userInfo?.userId ?? 0)"
     
        gameManager.loadGame(sudGameConfigModel)
    }
    
    private func isRoomOwner() -> Bool {
        guard let currentUid = userInfo?.userId else {
            return false
        }
        
        return roomInfo.ownerId == currentUid
    }
    
    private func showEndGameAlert() {
        let alertController = UIAlertController(
            title: "结束玩法",
            message: "退出房间后将关闭",
            preferredStyle: .alert
        )
        
        let confirmAction = UIAlertAction(title: "取消", style: .cancel) { _ in
            
        }
        
        let cancelAction = UIAlertAction(title: "确认", style: .default) { _ in
            self.prepareClose()
        }
        
        alertController.addAction(cancelAction)
        alertController.addAction(confirmAction)

        self.present(alertController, animated: true, completion: nil)
    }
    
    private func prepareClose() {
        handleExitGame()
        service.leaveRoom(roomInfo: roomInfo) { error in
            if let error = error  {
                JoyLogger.info("leave room error:\(error)")
                return
            }
            
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(500)) {
                self.gameManager.destroyGame()
                self.disableRtcEngine()
                self.navigationController?.popViewController(animated: true)
            }
        }
    }
    
    private func handleExitGame() {
        guard let userId = userInfo?.userId else {return}
        let currentUserId = "\(userId)"
        if gameHandler.sudFSMMGDecorator.isPlayer(in: currentUserId) {
            // 用户正在游戏中，先退出本局游戏，再退出游戏
            // The user is in the game, first quit the game, and then quit the game
            if gameHandler.sudFSMMGDecorator.isPlayerIsPlaying(currentUserId) {
                gameHandler.sudFSTAPPDecorator.notifyAppComonSelfPlaying(false, reportGameInfoExtras: "")
            }
        } else if gameHandler.sudFSMMGDecorator.isPlayerIsReady(currentUserId) {
            // 准备时，先退出准备
            // When preparing, exit preparation first
            gameHandler.sudFSTAPPDecorator.notifyAppCommonSelfReady(false)
        }
        
        gameHandler.sudFSTAPPDecorator.notifyAppComonSelf(in: false, seatIndex: -1, isSeatRandom: true, teamId: 1)
    }
    
    private func disableRtcEngine() {
        rtcEngine.disableAudio()
        rtcEngine.disableVideo()
        rtcEngine.stopPreview()
        rtcEngine.leaveChannel { (stats) -> Void in
            JoyLogger.info("left channel, duration: \(stats.duration)")
        }
    }
    
    private func onBackAction() {
        self.prepareClose()
    }
    
}

extension PlayGameViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        JoyLogger.info("tokenPrivilegeWillExpire")
    }
}

extension PlayGameViewController: JoyServiceListenerProtocol {
    func onNetworkStatusChanged(status: JoyServiceNetworkStatus) {}
    
    func onUserListDidChanged(userList: [InteractiveJoyUserInfo]) {}
    
    func onRoomDidDestroy(roomInfo: InteractiveJoyRoomInfo) {
        let alertController = UIAlertController(
            title: "游戏结束",
            message: "房主已解散房间，请确认离开房间",
            preferredStyle: .alert
        )
        
        let confirmAction = UIAlertAction(title: "我知道了", style: .default) { _ in
            self.prepareClose()
        }
        
        alertController.addAction(confirmAction)

        self.present(alertController, animated: true, completion: nil)
    }
}

extension PlayGameViewController: RoomBottomBarDelegate {
    func onClickAudioButton(audioEnable: Bool) {
        if audioEnable {
            rtcEngine.adjustRecordingSignalVolume(100)
        } else {
            rtcEngine.adjustRecordingSignalVolume(0)
        }
    }
    
    func onClickSendButton() {
        
    }
    
    func onClickRobotButton() {
        gameHandler.addRobot()
    }
}

extension PlayGameViewController: GameEventHandlerDelegate {
    func onPlayerCaptainChanged(userId: String, model: MGCommonPlayerCaptainModel) {
        var currentUidIsCaptain = false
        guard let callbackUid = UInt(userId) else { return }
        guard let currentUid = userInfo?.userId else { return }
        
        if model.isCaptain {
            currentUidIsCaptain = currentUid == callbackUid
        } else {
            currentUidIsCaptain = !(currentUid == callbackUid)
        }
        
        self.bottomBar.robotEnable = currentUidIsCaptain
    }
}
