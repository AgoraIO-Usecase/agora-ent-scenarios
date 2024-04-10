//
//  RoomViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/11/28.
//

import UIKit
import AgoraRtcKit
import SVProgressHUD

class TouchGameView: UIView {
    var touchEnd: (()->())?
    var isMouseEnable: Bool = true
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        guard let point = touches.first?.location(in: self), isMouseEnable else {return}
        CloudBarrageAPI.shared.sendMouseEvent(type: .mouseEventLbuttonDown, point: point, gameViewSize: frame.size)
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesMoved(touches, with: event)
        guard let point = touches.first?.location(in: self), isMouseEnable else {return}
        CloudBarrageAPI.shared.sendMouseEvent(type: .mouseEventMove, point: point, gameViewSize: frame.size)
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        touchEnd?()
        guard let point = touches.first?.location(in: self), isMouseEnable else {return}
        CloudBarrageAPI.shared.sendMouseEvent(type: .mouseEventLbuttonUp, point: point, gameViewSize: frame.size)
    }
}

class RoomViewController: UIViewController {
    private var roomInfo: JoyRoomInfo
    private var currentUserInfo: JoyUserInfo
    private var service: JoyServiceProtocol
    private var startGameInfo: JoyStartGameInfo? {
        didSet {
            joinAssistantChannel()
        }
    }
    private var gameInfo: CloudGameDetailInfo? {
        didSet {
            gameIntroduceButton.isHidden = gameInfo == nil ? true : false
        }
    }
    private var taskId: String? 
    private lazy var roomInfoView = {
        let infoView = RoomInfoView()
        infoView.onTimerCallback = {[weak self] ts in
            guard ts > 10 * 60 else {
                return
            }
            self?.onTimeoutAction()
        }
        return infoView
    }()
    private lazy var closeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "icon_close"), for: .normal)
        button.addTarget(self, action: #selector(onCloseAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var moreBtn: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "icon_live_more"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.addTarget(self, action: #selector(onMoreAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var gameIntroduceButton: UIButton = {
        let button = UIButton()
        button.titleLabel?.font = .joy_R_12
        button.setTitle("game_play_introduce".joyLocalization(), for: .normal)
        button.backgroundColor = UIColor(hexString: "#08062F4D")!.withAlphaComponent(0.3)
        button.contentEdgeInsets = UIEdgeInsets(top: 4, left: 7, bottom: 4, right: 7)
        button.sizeToFit()
        button.layer.cornerRadius = button.height / 2
        button.clipsToBounds = true
        button.isHidden = true
        button.addTarget(self, action: #selector(onIntroduceAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var waittingLabel: UILabel = {
        let label = UILabel()
        label.text = "room_waitting_for_startgame".joyLocalization()
        label.numberOfLines = 2
        label.textAlignment = .center
        label.textColor = .joy_main_text
        label.font = .joy_M_17
        label.sizeToFit()
        return label
    }()
    
    private lazy var bottomBar: ShowRoomBottomBar = {
        let bar = ShowRoomBottomBar(isBroadcastor: isRoomOwner())
        bar.delegate = self
        return bar
    }()
    
    private lazy var chatTableView: ChatTableView = {
        let tableView = ChatTableView()
        return tableView
    }()
    
    private lazy var chatInputView: ChatInputView = {
        let textField = ChatInputView()
        textField.isHidden = true
        textField.delegate = self
        return textField
    }()
    
    private lazy var backgroundImageView = {
        let imageView = UIImageView(frame: self.view.bounds)
        imageView.image = UIImage.sceneImage(name: "joy_room_bg")
        return imageView
    }()
    
    private lazy var broadcasterCanvasView: UIView = UIView()
    private lazy var assistantCanvasView: UIView = UIView()
    
    private lazy var touchView: TouchGameView = {
        let view = TouchGameView()
        view.touchEnd = { [weak self] in
            self?.view.endEditing(true)
        }
        return view
    }()
    
    required init(roomInfo: JoyRoomInfo, currentUserInfo: JoyUserInfo, service: JoyServiceProtocol) {
        self.roomInfo = roomInfo
        self.currentUserInfo = currentUserInfo
        self.service = service
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad(){
        super.viewDidLoad()
        view.backgroundColor = .darkGray
        
        view.addSubview(backgroundImageView)
        
        view.addSubview(waittingLabel)
        waittingLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
        
        view.addSubview(assistantCanvasView)
        assistantCanvasView.frame = view.bounds
        
        
        let top = max(UIDevice.current.aui_SafeDistanceTop, 20)
        view.addSubview(broadcasterCanvasView)
        broadcasterCanvasView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.width.equalTo(90)
            make.height.equalTo(120)
            make.top.equalTo(top)
        }
        
        view.addSubview(roomInfoView)
        roomInfoView.snp.makeConstraints { make in
            make.top.equalTo(top)
            make.left.equalTo(15)
        }

        view.addSubview(chatTableView)
        chatTableView.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.bottom.equalTo(-kTableViewBottomOffset)
            make.right.equalTo(-70)
            make.height.equalTo(168)
        }
        chatTableView.addObserver()
        
        view.addSubview(touchView)
        touchView.isMouseEnable = isRoomOwner()
        
        view.addSubview(bottomBar)
        bottomBar.snp.makeConstraints { make in
            make.bottom.equalToSuperview().offset(-UIDevice.current.aui_SafeDistanceBottom)
            make.left.right.equalToSuperview()
            make.height.equalTo(58)
        }
        
        view.addSubview(chatInputView)
        chatInputView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.height.equalTo(kChatInputViewHeight)
            make.bottom.equalToSuperview()
        }
        
        view.addSubview(closeButton)
        closeButton.snp.makeConstraints { make in
            make.right.equalTo(-15)
            make.centerY.equalTo(roomInfoView)
        }
        
        view.addSubview(moreBtn)
        moreBtn.snp.makeConstraints { make in
            make.trailing.equalTo(closeButton.snp_leadingMargin).offset(-18)
            make.centerY.equalTo(closeButton.snp.centerY)
            make.width.equalTo(24)
        }
        
        view.addSubview(gameIntroduceButton)
        gameIntroduceButton.snp.makeConstraints { make in
            make.top.equalTo(closeButton.snp.bottom).offset(15)
            make.right.equalTo(closeButton)
        }
        
        roomInfoView.setRoomInfo(avatar: roomInfo.ownerAvatar,
                                 name: roomInfo.roomName,
                                 id: roomInfo.roomId,
                                 time: roomInfo.createdAt)
        
        joinRTCChannel()
        service.subscribeListener(listener: self)
        service.getStartGame(roomId: roomInfo.roomId) {[weak self] err, gameInfo in
            guard let self = self else {return}
            if let _ = err {
                self.showToastFail(text: err?.joyErrorString())
                return
            }
            
            self.startGameInfo = gameInfo
            self.startScene(roomInfo: self.roomInfo)
        }
    }
     
    private func startScene(roomInfo: JoyRoomInfo) {
        let gameId = startGameInfo?.gameId ?? ""
        
        if isRoomOwner() {
            let taskId = startGameInfo?.taskId ?? ""
            var assistantUid = startGameInfo?.assistantUid ?? 0
            assistantUid = assistantUid == 0 ? (roomInfo.ownerId + 1000000) : assistantUid
            if gameId.isEmpty || taskId.isEmpty {
                CloudBarrageAPI.shared.getGameList { [weak self] err, list in
                    if let _ = err {
                        self?.showToastFail(text: err?.joyErrorString())
                        return
                    }
                    let dialog: JoyGameListDialog? = JoyGameListDialog.show()
                    dialog?.onSelectedGame = { game in
                        self?.startGame(gameInfo: game, assistantUid: assistantUid)
                    }
                    dialog?.gameList = list ?? []
                }
            } else {
                CloudBarrageAPI.shared.getGameStatus(gameId: gameId, taskId: taskId) { status in
                    joyPrint("getGameStatus: \(status?.rawValue ?? .none)")
                    guard status == .startFailed else {
                        return
                    }
                    
                    //restart game
                    CloudBarrageAPI.shared.getGameList { [weak self] err, list in
                        if let _ = err {
                            self?.showToastFail(text: err?.joyErrorString())
                            return
                        }
                        
                        guard let game = list?.first(where: { $0.gameId == gameId }) else {
                            joyError("restart game[\(gameId)] not found! game list count: \(list?.count ?? 0)")
                            return
                        }
                        self?.startGame(gameInfo: game, assistantUid: assistantUid)
                    }
                }
                getGameInfo(gameId: gameId) { [weak self] in
                    self?.taskId = taskId
                }
            }
        } else {
            if gameId.isEmpty { return }
            getGameInfo(gameId: gameId)
        }
    }
}

//MARK: game handler
extension RoomViewController {
    private func startGame(gameInfo: CloudGameInfo, assistantUid: UInt) {
        renewRTCTokens(roomId: roomInfo.roomId,
                            userId: assistantUid) {[weak self] token in
            guard let self = self else {return}
            guard let token = token else {
                self.showToastFail()
                return
            }
            self.startGame(gameInfo: gameInfo, assistantUid: assistantUid, assistantToken: token)
        }
    }
    
    private func startGame(gameInfo: CloudGameInfo, assistantUid: UInt, assistantToken: String) {
        let rtcConfig = CloudGameRtcConfig(broadcastUid: roomInfo.ownerId,
                                           assistantUid: assistantUid,
                                           assistantToken: assistantToken,
                                           channelName: roomInfo.roomId)
        var startConfig = CloudGameStartConfig(roomId: roomInfo.roomId ?? "",
                                               gameId: gameInfo.gameId,
                                               userId: "\(currentUserInfo.userId)",
                                               userAvatar: currentUserInfo.avatar,
                                               userName: currentUserInfo.userName,
                                               rtcConfig: rtcConfig)
        SVProgressHUD.show()
        CloudBarrageAPI.shared.startGame(config: startConfig) {[weak self] err, taskId in
            if let _ = err {
                SVProgressHUD.dismiss()
                self?.showToastFail(text: err?.joyErrorString())
                return
            }
            
            self?.taskId = taskId
            if let roomInfo = self?.roomInfo, let taskId = taskId {
                roomInfo.badgeTitle = gameInfo.name ?? ""
                self?.service.updateRoom(roomInfo: roomInfo, completion: { err in
                })
                let startGame = JoyStartGameInfo()
                startGame.gameId = gameInfo.gameId ?? ""
                startGame.taskId = taskId
                startGame.gameName = gameInfo.name ?? ""
                startGame.assistantUid = assistantUid
                startGame.objectId = ""
                self?.service.updateStartGame(roomId: roomInfo.roomId,
                                              gameInfo: startGame,
                                              completion: { err in
                    
                })
            }
            
            self?.getGameInfo(gameId: gameInfo.gameId ?? "") {
                SVProgressHUD.dismiss()
                self?.onIntroduceAction()
                if self?.gameInfo?.actions == nil {
                    self?.bottomBar.setDeployBtnHidden(isHidden: true)
                } else {
                    self?.bottomBar.setDeployBtn(with: self?.gameInfo?.actions?.first?.icon ?? "")
                }
            } fail: { _ in
                SVProgressHUD.dismiss()
            }
            JoyGameListDialog.hiddenAnimation()
        }
    }
    
    private func stopGame() {
        guard let gameId = gameInfo?.gameId,
              let taskId = taskId,
              isRoomOwner() else {return}
        CloudBarrageAPI.shared.endGame(gameId: gameId,
                                       taskId: taskId, 
                                       roomId: roomInfo.roomId,
                                       userId: "\(roomInfo.ownerId)") { err in
        }
    }
    
    private func getGameInfo(gameId: String, success: (()-> ())? = nil, fail: ((NSError?)-> ())? = nil) {
        CloudBarrageAPI.shared.getGameInfo(gameId: gameId) {[weak self] err, detail in
            if let _ = err {
                self?.showToastFail(text: err?.joyErrorString())
                fail?(err)
                return
            }
            
            if let detail = detail {
                self?.gameInfo = detail
            }
            success?()
        }
    }
}

extension RoomViewController {
    @objc func onCloseAction() {
        let title = (isRoomOwner() ? "query_title_exit" : "query_title_exit_guest").joyLocalization()
        let message = (isRoomOwner() ? "query_subtitle_exit" : "query_subtitle_exit_guest").joyLocalization()
        let alertController = UIAlertController(title: title,
                                                message: message,
                                                preferredStyle: .alert)
        let action1 = UIAlertAction(title: "query_button_confirm".joyLocalization(), style: .default) {[weak self] action in
            JoyGameListDialog.hiddenAnimation()
            self?.leaveRoom {
                self?.navigationController?.popViewController(animated: true)
            }
        }
        let action2 = UIAlertAction(title: "query_button_cancel".joyLocalization(), style: .default) { action in
        }
        alertController.addAction(action2)
        alertController.addAction(action1)
        present(alertController, animated: true, completion: nil)
        
    }
    
    @objc func onMoreAction() {
        
    }
    
    @objc func onIntroduceAction() {
        guard let introduce = gameInfo?.introduce, !introduce.isEmpty else {return}
        let dialog: JoyGameNoticeDialog? = JoyGameNoticeDialog.show()
        dialog?.text = introduce
    }
    
    @objc func onTimeoutAction() {
        leaveRoom {
        }
        roomInfoView.onTimerCallback = nil
        let title = (isRoomOwner() ? "query_title_timeout" : "query_title_timeout_guest").joyLocalization()
        let message = (isRoomOwner() ? "query_subtitle_timeout" : "query_subtitle_timeout_guest").joyLocalization()
        let alertController = UIAlertController(title: title,
                                                message: message,
                                                preferredStyle: .alert)
        let action = UIAlertAction(title: "query_button_confirm".joyLocalization(), style: .default) {[weak self] action in
            self?.navigationController?.popViewController(animated: true)
        }
        alertController.addAction(action)
        present(alertController, animated: true, completion: nil)
        JoyGameListDialog.hiddenAnimation()
    }
}

extension RoomViewController {
    private func isRoomOwner() -> Bool {
        return roomInfo.ownerId == currentUserInfo.userId
    }
    
    private func joinRTCChannel() {
        guard let engine = CloudBarrageAPI.shared.apiConfig?.engine else {
            AUIToast.show(text: "rtc engine is nil")
            onCloseAction()
            return
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.publishCameraTrack = isRoomOwner()
        mediaOptions.publishMicrophoneTrack = mediaOptions.publishCameraTrack
        mediaOptions.clientRoleType = mediaOptions.publishCameraTrack ? .broadcaster : .audience
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        let userId = currentUserInfo.userId
        let roomId = roomInfo.roomId
        renewRTCTokens(roomId: roomInfo.roomId,
                       userId: currentUserInfo.userId) { token in
            engine.joinChannel(byToken: token,
                               channelId: roomId,
                               uid: userId,
                               mediaOptions: mediaOptions) { channel, uid, elapsed in
                joyPrint("joinChannel[\(channel)][\(uid)] cost: \(elapsed)ms")
            }
        }
        
        let broadcasterCanvas = AgoraRtcVideoCanvas()
        broadcasterCanvas.uid = roomInfo.ownerId
        broadcasterCanvas.view = broadcasterCanvasView
        if mediaOptions.publishCameraTrack {
            engine.enableVideo()
            engine.enableAudio()
            engine.startPreview()
            engine.setupLocalVideo(broadcasterCanvas)
        } else {
            engine.setupRemoteVideo(broadcasterCanvas)
        }
        
        CloudBarrageAPI.shared.apiConfig?.engine?.addDelegate(self)
    }
    
    private func joinAssistantChannel() {
        guard let engine = CloudBarrageAPI.shared.apiConfig?.engine,
              let startGameInfo = startGameInfo,
              startGameInfo.assistantUid > 0 else {
            return
        }
        let assistantCanvas = AgoraRtcVideoCanvas()
        assistantCanvas.uid = startGameInfo.assistantUid
        assistantCanvas.view = assistantCanvasView
        engine.setupRemoteVideo(assistantCanvas)
    }
    
    private func leaveRTCChannel() {
        CloudBarrageAPI.shared.apiConfig?.engine?.leaveChannel()
        CloudBarrageAPI.shared.apiConfig?.engine?.stopPreview()
        CloudBarrageAPI.shared.apiConfig?.engine?.removeDelegate(self)
    }
    
    private func renewRTCTokens(roomId: String, userId: UInt, completion: ((String?)->Void)?) {
        joyPrint("renewRTCTokens[\(roomId)][\(userId)]")
        NetworkManager.shared.generateTokens(appId: joyAppId,
                                             appCertificate: joyAppCertificate,
                                             channelName: roomId,
                                             uid: "\(userId)",
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc]) {[weak self] tokens in
            guard let self = self else {return}
            guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue] else {
                joyWarn("renewRTCTokens[\(roomId)] fail")
                completion?(nil)
                return
            }
            joyPrint("renewRTCTokens[\(roomId)] success")
            completion?(rtcToken)
        }
    }
    
    private func leaveRoom(completion:@escaping ()->()) {
        JoyBaseDialog.hidden()
        service.leaveRoom(roomInfo: roomInfo, completion: { err in
            completion()
        })
        leaveRTCChannel()
        stopGame()
    }
}

extension RoomViewController: RoomBottomBarDelegate {
    func showToastSuccess(text: String) {
        AUIToast.show(text: text,
                      tagImage: UIImage.sceneImage(name: "toast_success_icon"))
        
    }
    
    func showToastFail(text: String? = nil) {
        AUIToast.show(text: text ?? "game_connect_fail".joyLocalization(),
                      tagImage: UIImage.sceneImage(name: "toast_fail_icon"))
        
    }
    
    func onClickSendButton() {
        chatInputView.isHidden = false
        chatInputView.textField.becomeFirstResponder()
    }
    
    func onClickDeployButton(isUp: Bool) {
        guard let actions: [CloudGameActions] = gameInfo?.actions, let action = actions.first, let cmds = action.command else {return}
        for cmd in cmds {
            CloudBarrageAPI.shared.sendKeyboardEvent(type: isUp ? .keyboardEventKeyUp : .keyboardEventKeyDown, key: cmd.first ?? "Z")
        }
    }
    
    func onClickGiftButton() {
        guard let gameId = gameInfo?.gameId, let giftList = gameInfo?.gifts else {
            joyWarn("show gift fail!")
            getGameInfo(gameId: gameInfo?.gameId ?? "")
            return
        }
        
        JoyGiftListDialog.hidden()
        let dialog: JoyGiftListDialog? = JoyGiftListDialog.show()
        dialog?.giftList = giftList
        dialog?.onSelectedGift = {[weak self] gift, count in
            guard let self = self else {return}
            let sendGift = CloudGameSendGiftInfo(userId: "\(currentUserInfo.userId)",
                                                 userAvatar: currentUserInfo.avatar,
                                                 userName: currentUserInfo.userName,
                                                 vendorGiftId: gift.vendorGiftId,
                                                 giftNum: count,
                                                 giftValue: gift.price * count)
            let sendConfig = CloudGameSendGiftConfig(roomId: roomInfo.roomId, gameId: gameId, giftList: [sendGift])
            CloudBarrageAPI.shared.sendGift(giftConfig: sendConfig) { err in
                if let _ = err {
                    self.showToastFail(text: "game_send_gift_fail".joyLocalization())
                    return
                }
                self.showToastSuccess(text: "game_send_gift_success".joyLocalization())
            }
        }
    }
    
    func onClickLikeButton() {
        guard let gameId = gameInfo?.gameId else {
            return
        }
        let roomId = roomInfo.roomId
        let like = CloudGameLikeInfo(userId: "\(currentUserInfo.userId)",
                                     userAvatar: currentUserInfo.avatar,
                                     userName: currentUserInfo.userName)
        let config = CloudGameSendLikeConfig(roomId: roomId, gameId: gameId, likeList: [like])
        CloudBarrageAPI.shared.sendLike(likeConfig: config) { err in
            if let _ = err {
                self.showToastFail(text: "game_send_like_fail".joyLocalization())
                return
            }
            self.showToastSuccess(text: "game_send_like_success".joyLocalization())
        }
    }
}

extension RoomViewController: ChatInputViewDelegate {
    func onEndEditing() {
        chatInputView.isHidden = true
        self.view.endEditing(true)
    }
    
    func onClickSendButton(text: String) {
        guard let gameId = gameInfo?.gameId else {
            return
        }
        let roomId = roomInfo.roomId
        service.sendChatMessage(roomId: roomId,
                                message: text,
                                completion: { err in
       })
        
        let comment = CloudGameCommentInfo(userId: "\(currentUserInfo.userId)",
                                           userAvatar: currentUserInfo.avatar,
                                           userName: currentUserInfo.userName,
                                           content: text)
        let config = CloudGameSendCommentConfig(roomId:roomId, gameId: gameId, commentList: [comment])
        CloudBarrageAPI.shared.sendComment(commentConfig: config) { err in
            if let _ = err {
                self.showToastFail(text: "game_send_msg_fail".joyLocalization())
                return
            }
            self.showToastSuccess(text: "game_send_msg_success".joyLocalization())
        }
    }
}

extension RoomViewController: JoyServiceListenerProtocol {
    func onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo) {
        self.startGameInfo = startGameInfo
        startScene(roomInfo: roomInfo)
    }
    
    func onNetworkStatusChanged(status: JoyServiceNetworkStatus) {
        
    }
    
    func onUserListDidChanged(userList: [JoyUserInfo]) {
        roomInfo.roomUserCount = userList.count
        /*
         owner control prevents game information loss due to simultaneous conflicts
         between owner and audience members
         */
        guard isRoomOwner() else {return}
        service.updateRoom(roomInfo: roomInfo, completion: { err in
        })
    }
    
    func onMessageDidAdded(message: JoyMessage) {
        chatTableView.appendMessage(msg: message)
    }
    
    func onRoomDidChanged(roomInfo: JoyRoomInfo) {
        self.roomInfo = roomInfo
    }
    
    func onRoomDidDestroy(roomInfo: JoyRoomInfo) {
        if isRoomOwner() { return }
        leaveRoom {
        }
        let title = "query_title_destroy".joyLocalization()
        let message = "query_subtitle_timeout_guest".joyLocalization()
        let alertController = UIAlertController(title: title,
                                                message: message,
                                                preferredStyle: .alert)
        let action = UIAlertAction(title: "query_button_confirm".joyLocalization(), style: .default) { action in
            self.navigationController?.popViewController(animated: true)
        }
        alertController.addAction(action)
        present(alertController, animated: true, completion: nil)
    }
}


extension RoomViewController: AgoraRtcEngineDelegate {
    private func resetAssistantCanvasView(size: CGSize) {
        assistantCanvasView.frame = view.bounds.size.fitRect(imageSize: size)
        touchView.frame = assistantCanvasView.frame
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoFrameOfUid uid: UInt, size: CGSize, elapsed: Int) {
        if uid == startGameInfo?.assistantUid ?? 0 {
//            joyPrint("size = \(size)")
            resetAssistantCanvasView(size: size)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
//        gameViewHeightCon.constant = CGFloat(stats.height) / CGFloat(stats.width) * view.bounds.width
        let width: CGFloat = CGFloat(stats.width)
        let height: CGFloat = CGFloat(stats.height)
        resetAssistantCanvasView(size: CGSize(width: width, height: height))
    }
}
