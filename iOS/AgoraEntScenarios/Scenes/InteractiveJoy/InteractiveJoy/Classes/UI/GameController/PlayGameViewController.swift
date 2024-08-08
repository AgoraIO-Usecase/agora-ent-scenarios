//
//  PlayGameController.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import UIKit
import AgoraRtcKit
import AgoraCommon
import AUIIMKit

class PlayGameViewController: UIViewController {
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
    private lazy var rtcEngine: AgoraRtcEngineKit = createRtcEngine()
    
    private lazy var gameView: UIView = {
        let view = UIView()
        return view
    }()
    
    lazy var messageView: AUIChatListView = {
        let chatListHeight = 200.0
        let listView = AUIChatListView(frame: CGRect(x: 0, y:  self.view.frame.height - chatListHeight - 65, width: self.view.width, height: chatListHeight))
        return listView
    }()
    
    private lazy var chatInputView: ShowChatInputView = {
        let textField = ShowChatInputView()
        textField.isHidden = true
        textField.delegate = self
        textField.backgroundColor = .clear
        return textField
    }()
    
    lazy var chatBinder: AUIIMViewBinder = AUIIMViewBinder()
    
    private lazy var backgroundView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage.sceneImage(name: "play_zone_room_bg")
        return imageView
    }()
    
    private lazy var bottomBar: ShowRoomBottomBar = {
        let bar = ShowRoomBottomBar(isBroadcastor: isRoomOwner())
        bar.delegate = self
        bar.audioEnable = !self.isRoomOwner()
        return bar
    }()
    
    lazy var gameHandler: GameEventHandler = {
        let handler = GameEventHandler()
        handler.delegate = self
        handler.robotInfoList = self.service.getRobotList()
        handler.isOwner = isRoomOwner()
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
            let ensureAction = UIAlertAction(title: LanguageManager.localValue(key: "gamenotice_selected_confirm"), style: .default) { _ in
                self?.onBackAction()
            }
            self?.showAlert(title: LanguageManager.localValue(key: "game_room_timeout_title"), message: LanguageManager.localValue(key: "game_room_timeout_des"), actions: [ensureAction])
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
        self.view.addSubview(messageView)
        self.view.addSubview(navigationBar)
        self.view.addSubview(bottomBar)
        self.view.addSubview(chatInputView)
        
        imServiceBindView()
    
        navigationBar.moreActionCallback = { [weak self] in
            guard let self = self else { return }
            
            let dialog = AUiMoreDialog(frame: view.bounds)
            self.view.addSubview(dialog)
            dialog.show()
        }
        
        navigationBar.closeActionCallback = { [weak self] in
            guard let self = self else { return }
            
            var content = LanguageManager.localValue(key: "game_room_exit_des")
            if isRoomOwner() {
                content = LanguageManager.localValue(key: "game_room_owner_exit_des")
            }
            
            let confirmAction = UIAlertAction(title: LanguageManager.localValue(key: "query_button_cancel"), style: .cancel, handler: nil)
            let cancelAction = UIAlertAction(title: LanguageManager.localValue(key: "query_button_confirm"), style: .default) { _ in
                self.prepareClose()
                self.closePage()
            }
            self.showAlert(title: LanguageManager.localValue(key: "game_room_exit_title"), message: content, actions: [confirmAction, cancelAction])
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
        
        chatInputView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.height.equalTo(kChatInputViewHeight)
            make.bottom.equalToSuperview()
        }
        
        gameManager.registerGameEventHandler(gameHandler)
        
        if roomInfo.gameId > 0 {
            loadGame(gameId: roomInfo.gameId)
        }
        
        addKeyboardObserver()
        navigationBar.roomInfoView.setRoomInfo(avatar: userInfo?.avatar, name: roomInfo.roomName, id: roomInfo.roomId)
        renewRTMTokens { [weak self] token in
            guard let self = self else {return}
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.publishMicrophoneTrack = true
            mediaOption.publishCameraTrack = false
            mediaOption.autoSubscribeAudio = true
            mediaOption.autoSubscribeVideo = false
            mediaOption.clientRoleType = .broadcaster
        
            let result = self.rtcEngine.joinChannel(byToken: token, channelId: roomInfo.roomId, uid: self.userInfo?.userId ?? 0, mediaOptions: mediaOption)
            if result != 0 {
                JoyLogger.error("join channel fail")
            }
        }
        mockMessage()
    }
    
    private func mockMessage() {
        let systemMsg = LanguageManager.localValue(key: "game_room_im_system_message")
        let chatEntity = AUIChatEntity()
        chatEntity.content = systemMsg
        chatEntity.messageType = .system
        
        self.messageView.showNewMessage(entity: chatEntity)
    }
    
    private func addKeyboardObserver() {
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillShowNotification, object: nil, queue: nil) { [weak self] notify in
            guard let self = self else {return}
            guard let keyboardRect = (notify.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
            guard let duration = notify.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else { return }
            let keyboradHeight = keyboardRect.size.height
            self.chatInputView.snp.updateConstraints { make in
                make.bottom.equalToSuperview().offset(-keyboradHeight)
            }
        }
        
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillHideNotification, object: nil, queue: nil) {[weak self] notify in
            guard let self = self else {return}
            self.chatInputView.snp.updateConstraints { make in
                make.bottom.equalToSuperview()
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
        
    private func createRtcEngine() -> AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = joyAppId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .default
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
//        engine.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)
        engine.setAudioScenario(.gameStreaming)
        //当你在频道外使用 Unity 组件播放背景音乐时，系统的 AudioSession 为 active 状态。
        //在你加入频道或离开频道后，Unity SDK 会将系统的 AudioSession 改为 deactive 状态，所以你在加入频道或离开频道后无法听到背景音乐。
        //在加入频道前调用 mRtcEngine.SetParameters("{\"che.audio.keep.audiosession\":true}"); 接口，保证系统的 AudioSession 状态不被改变。之后，即使你多次进出频道，也都能听到背景音乐。
        engine.setParameters("{\"che.audio.keep.audiosession\":true}")
    
        return engine
    }
    
    private func loadGame(gameId: Int64) {
        let sudGameConfigModel = SudGameLoadConfigModel()
        sudGameConfigModel.appId = SUDMGP_APP_ID
        sudGameConfigModel.appKey = SUDMGP_APP_KEY
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
    
    private func showAlert(title: String?, message: String?, actions:[UIAlertAction]) {
        let alertContoller = UIAlertController(title: title, message: message, preferredStyle: .alert)
        for action in actions {
            alertContoller.addAction(action)
        }
        
        self.present(alertContoller, animated: true, completion: nil)
    }
    
    private func imServiceBindView() {
        guard let service = service as? JoyServiceImpl else { return }
        chatBinder.bind(chat: self.messageView, chatService: service.imService)
    }
        
    private func prepareClose() {
        handleExitGame()
        handleLeaveRoom()
        disableRtcEngine()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(500)) {
            self.gameManager.destroyGame()
        }
    }
    
    private func closePage() {
        self.navigationController?.popViewController(animated: true)
    }
    
    private func handleLeaveRoom() {
        service.leaveRoom(roomInfo: roomInfo) { error in
            if let error = error  {
                JoyLogger.info("leave room error:\(error)")
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
        prepareClose()
        closePage()
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
    func onRoomRobotDidLoad(robots: [PlayRobotInfo]) {
        gameHandler.robotInfoList = robots
    }
    
    func onNetworkStatusChanged(status: JoyServiceNetworkStatus) {}
    
    func onUserListDidChanged(userList: [InteractiveJoyUserInfo]) {
        roomInfo.roomUserCount = userList.count
        /*
         owner control prevents game information loss due to simultaneous conflicts
         between owner and audience members
         */
        guard isRoomOwner() else {return}
        service.updateRoom(roomInfo: roomInfo, completion: { err in
        })
    }
    
    func onRoomDidDestroy(roomInfo: InteractiveJoyRoomInfo) {
        prepareClose()
        
        let alertController = UIAlertController(
            title: "游戏结束",
            message: "房主已解散房间，请确认离开房间",
            preferredStyle: .alert
        )
        
        let confirmAction = UIAlertAction(title: "我知道了", style: .default) { _ in
            self.closePage()
        }
        
        alertController.addAction(confirmAction)
        self.present(alertController, animated: true, completion: nil)
    }
}

extension PlayGameViewController: RoomBottomBarDelegate {
    func onClickAudioButton(audioEnable: Bool) {
        rtcEngine.muteLocalAudioStream(!audioEnable)
    }
    
    func onClickSendButton() {
        chatInputView.isHidden = false
        bottomBar.isHidden = true
        chatInputView.textField.becomeFirstResponder()
    }
    
    func onClickRobotButton() {
        gameHandler.addRobot()
    }
}

extension PlayGameViewController: ShowChatInputViewDelegate {
    func onEndEditing() {
        bottomBar.isHidden = false
        chatInputView.isHidden = true
    }
    
    func onClickEmojiButton() { }
    
    func onClickSendButton(text: String) {
        self.service.sendMessage(roomId: roomInfo.chatId ?? "", text: text) { [weak self] error in
            if error == nil {
                let chatEntity = AUIChatEntity()
                chatEntity.user = AUIChatContext.shared.commonConfig?.owner ?? AUIChatUserInfo()
                chatEntity.content = text
                
                self?.messageView.showNewMessage(entity: chatEntity)
            } else {
                VLToast.toast("\(error)")
            }
        }
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
        
        self.bottomBar.robotEnable = currentUidIsCaptain && !service.getRobotList().isEmpty && gameHandler.supportRobots(gameId: roomInfo.gameId)
    }
}
