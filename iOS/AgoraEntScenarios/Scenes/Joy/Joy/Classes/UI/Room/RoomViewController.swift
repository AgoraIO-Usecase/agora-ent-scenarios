//
//  RoomViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/11/28.
//

import UIKit
import AgoraRtcKit

class RoomViewController: UIViewController {
    private var roomInfo: JoyRoomInfo!
    private var currentUserInfo: JoyUserInfo!
    private var service: JoyServiceProtocol!
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
    private lazy var roomInfoView = RoomInfoView()
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
        let bar = ShowRoomBottomBar()
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
    
    private lazy var broadcasterCanvasView: UIView = UIView()
    private lazy var assistantCanvasView: UIView = UIView()
    
    required init(roomInfo: JoyRoomInfo, currentUserInfo: JoyUserInfo, service: JoyServiceProtocol) {
        super.init(nibName: nil, bundle: nil)
        self.roomInfo = roomInfo
        self.currentUserInfo = currentUserInfo
        self.service = service
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad(){
        super.viewDidLoad()
        view.backgroundColor = .darkGray
        
        view.addSubview(assistantCanvasView)
        assistantCanvasView.frame = view.bounds
        
        view.addSubview(roomInfoView)
        let top = UIDevice.current.aui_SafeDistanceTop
        roomInfoView.snp.makeConstraints { make in
            make.top.equalTo(max(top, 20))
            make.left.equalTo(15)
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
        
        view.addSubview(waittingLabel)
        waittingLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
        
        view.addSubview(broadcasterCanvasView)
        broadcasterCanvasView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.width.equalTo(120)
            make.height.equalTo(160)
            make.top.equalTo(roomInfoView.snp.bottom).offset(20)
        }

        view.addSubview(chatTableView)
        chatTableView.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.bottom.equalTo(-kTableViewBottomOffset)
            make.right.equalTo(-70)
            make.height.equalTo(168)
        }
        chatTableView.addObserver()
        
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
        
        guard let roomInfo = roomInfo, let currentUserInfo = currentUserInfo else {
            AUIToast.show(text: "room info error")
            leaveRoom()
            return
        }
        
        roomInfoView.setRoomInfo(avatar: roomInfo.ownerAvatar,
                                 name: roomInfo.roomName,
                                 id: roomInfo.roomId,
                                 time: roomInfo.createdAt)
        
        joinRTCChannel()
        service?.subscribeListener(listener: self)
        service.getStartGame(roomId: roomInfo.roomId) {[weak self] err, gameInfo in
            if let err = err {
                AUIToast.show(text: err.localizedDescription)
                return
            }
            
            self?.startGameInfo = gameInfo
            self?.startScene(roomInfo: roomInfo)
        }
    }
    
    private func startScene(roomInfo: JoyRoomInfo) {
        if roomInfo.ownerId == currentUserInfo.userId {
            let gameId = startGameInfo?.gameId ?? ""
            let taskId = startGameInfo?.taskId ?? ""
            let assistantUid = startGameInfo?.assistantUid ?? 0
            if gameId.isEmpty || taskId.isEmpty {
                CloudBarrageAPI.shared.getGameList { [weak self] err, list in
                    if let err = err {
                        AUIToast.show(text: err.localizedDescription)
                        return
                    }
                    let dialog: JoyGameListDialog? = JoyGameListDialog.show()
                    dialog?.onSelectedGame = { game in
                        guard let self = self else {return}
                        self.renewRTCTokens(roomId: roomInfo.roomId,
                                            userId: assistantUid) { token in
                            guard let token = token else {
                                AUIToast.show(text: "assistant token is empty")
                                return
                            }
                            self.startGame(gameInfo: game, assistantUid: assistantUid, assistantToken: token)
                        }
                    }
                    #if DEBUG
                    var aaa = [CloudGameInfo]()
                    for i in 0...100 {
                        aaa += list!
                    }
                    dialog?.gameList = aaa
                    #else
                    dialog?.gameList = list!
                    #endif
                }
            } else {
                CloudBarrageAPI.shared.getGameInfo(gameId: gameId) {[weak self] err, detail in
                    if let err = err {
                        AUIToast.show(text: err.localizedDescription)
                        return
                    }
                    
                    self?.gameInfo = detail
                    self?.taskId = taskId
                }
            }
        }
    }
}

//MARK: game handler
extension RoomViewController {
    private func startGame(gameInfo: CloudGameInfo, assistantUid: UInt, assistantToken: String) {
        guard let roomInfo = roomInfo, let currentUserInfo = currentUserInfo else {return}
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
        CloudBarrageAPI.shared.startGame(config: startConfig) {[weak self] err, taskId in
            if let err = err {
                AUIToast.show(text: err.localizedDescription)
                return
            }
            
            if let roomInfo = self?.roomInfo, let taskId = taskId {
                roomInfo.badgeTitle = gameInfo.name ?? ""
                self?.service?.updateRoom(roomInfo: roomInfo, completion: { err in
                })
                let startGame = JoyStartGameInfo()
                startGame.gameId = gameInfo.gameId ?? ""
                startGame.taskId = taskId
                startGame.gameName = gameInfo.name ?? ""
                startGame.assistantUid = roomInfo.ownerId + 1000000
                self?.service.updateStartGame(roomId: roomInfo.roomId,
                                              gameInfo: startGame,
                                              completion: { err in
                    
                })
            }
            
            CloudBarrageAPI.shared.getGameInfo(gameId: gameInfo.gameId!) { err, detail in
                if let err = err {
                    AUIToast.show(text: err.localizedDescription)
                    return
                }
                
                self?.gameInfo = detail
                self?.taskId = taskId
                
                self?.onIntroduceAction()
            }
            JoyGameListDialog.hiddenAnimation()
        }
    }
    
    private func stopGame() {
        guard let gameId = gameInfo?.gameId, let taskId = taskId else {return}
        CloudBarrageAPI.shared.endGame(gameId: gameId,
                                       taskId: taskId) { err in
        }
    }
}

extension RoomViewController {
    @objc func onCloseAction() {
        let alertController = UIAlertController(title: "query_title_exit".joyLocalization(),
                                                message: "query_subtitle_exit".joyLocalization(),
                                                preferredStyle: .alert)
        let action1 = UIAlertAction(title: "query_button_confirm".joyLocalization(), style: .default) { action in
            self.leaveRoom()
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
}

extension RoomViewController {
    private func joinRTCChannel() {
        guard let roomInfo = roomInfo,
              let currentUserInfo = currentUserInfo,
              let engine = CloudBarrageAPI.shared.apiConfig?.engine else {
            AUIToast.show(text: "room info error")
            onCloseAction()
            return
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.publishCameraTrack = roomInfo.ownerId == currentUserInfo.userId ? true : false
        mediaOptions.publishMicrophoneTrack = mediaOptions.publishCameraTrack
        mediaOptions.clientRoleType = mediaOptions.publishCameraTrack ? .broadcaster : .audience
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        renewRTCTokens(roomId: roomInfo.roomId,
                       userId: currentUserInfo.userId) { token in
            engine.joinChannel(byToken: token,
                               channelId: roomInfo.roomId,
                               uid: currentUserInfo.userId,
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
    
    private func leaveRoom() {
        JoyBaseDialog.hidden()
        guard let roomInfo = roomInfo else {
            self.navigationController?.popViewController(animated: true)
            return
        }
        service?.leaveRoom(roomInfo: roomInfo, completion: { err in
            self.navigationController?.popViewController(animated: true)
        })
        leaveRTCChannel()
        stopGame()
    }
}

extension RoomViewController: RoomBottomBarDelegate {
    func onClickSendButton() {
        chatInputView.isHidden = false
        chatInputView.textField.becomeFirstResponder()
    }
    
    func onClickGiftButton() {
        guard let bundlePath = Bundle.main.path(forResource: "Joy", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath),
              let path = bundle.path(forResource: "Image/gift", ofType: "json"),
              let jsonData = try? Data(contentsOf: URL(fileURLWithPath: path)),
              let jsonObj = try? JSONSerialization.jsonObject(with: jsonData) as? [[String: Any]],
              let giftList: [CloudGameGiftInfo] = self.decodeModelArray(jsonObj) else {
            assert(false, "gift is empty")
            joyWarn("show gift fail!")
            return
        }
        
        JoyGiftListDialog.hidden()
        let dialog: JoyGiftListDialog? = JoyGiftListDialog.show()
        dialog?.giftList = giftList
        dialog?.onSelectedGift = {[weak self] game in
            guard let self = self else {return}
            
        }
    }
    
    func onClickLikeButton() {
        guard let roomId = roomInfo?.roomId,
              let gameId = gameInfo?.gameId,
              let user = currentUserInfo else {
            return
        }
        let like = CloudGameLikeInfo(userId: "\(user.userId)", userAvatar: user.avatar, userName: user.userName)
        let config = CloudGameSendLikeConfig(roomId: roomId, gameId: gameId, likeList: [like])
        CloudBarrageAPI.shared.sendLike(likeConfig: config) { err in
            guard let err = err else {return}
            AUIToast.show(text: err.localizedDescription)
        }
    }
}

extension RoomViewController: ChatInputViewDelegate {
    func onEndEditing() {
        chatInputView.isHidden = true
        self.view.endEditing(true)
    }
    
    func onClickSendButton(text: String) {
        guard let roomId = roomInfo?.roomId,
              let gameId = gameInfo?.gameId,
              let user = currentUserInfo else {
            return
        }
        service?.sendChatMessage(roomId: roomId,
                                 message: text,
                                 completion: { err in
        })
        
        let comment = CloudGameCommentInfo(userId: "\(user.userId)",
                                           userAvatar: user.avatar,
                                           userName: user.userName,
                                           content: text)
        let config = CloudGameSendCommentConfig(roomId:roomId, gameId: gameId, commentList: [comment])
        CloudBarrageAPI.shared.sendComment(commentConfig: config) { err in
            guard let err = err else {return}
            AUIToast.show(text: err.localizedDescription)
        }
    }
}

extension RoomViewController: JoyServiceListenerProtocol {
    func onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo) {
        self.startGameInfo = startGameInfo
    }
    
    func onNetworkStatusChanged(status: JoyServiceNetworkStatus) {
        
    }
    
    func onUserListDidChanged(userList: [JoyUserInfo]) {
        roomInfo.roomUserCount = userList.count
        /*
         owner control prevents game information loss due to simultaneous conflicts
         between owner and audience members
         */
        guard roomInfo.ownerId == currentUserInfo.userId else {return}
        service?.updateRoom(roomInfo: roomInfo, completion: { err in
        })
    }
    
    func onMessageDidAdded(message: JoyMessage) {
        chatTableView.appendMessage(msg: message)
    }
    
    func onRoomDidChanged(roomInfo: JoyRoomInfo) {
        self.roomInfo = roomInfo
    }
    
    func onRoomDidDestroy(roomInfo: JoyRoomInfo) {
        leaveRoom()
    }
}
