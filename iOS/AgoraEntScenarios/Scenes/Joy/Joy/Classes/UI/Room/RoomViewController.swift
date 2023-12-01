//
//  RoomViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/11/28.
//

import UIKit
import AgoraRtcKit

class RoomViewController: UIViewController {
    var roomInfo: JoyRoomInfo?
    var currentUserInfo: JoyUserInfo?
    var service: JoyServiceProtocol?
    
    private var gameInfo: CloudGameInfo?
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
    
    private lazy var broadcasterCanvasView: UIView = UIView()
    private lazy var assistantCanvasView: UIView = UIView()
    
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
        
        view.addSubview(broadcasterCanvasView)
        broadcasterCanvasView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.width.equalTo(120)
            make.height.equalTo(160)
            make.top.equalTo(roomInfoView.snp.bottom).offset(20)
        }
        
        guard let roomInfo = roomInfo, let currentUserInfo = currentUserInfo else {
            AUIToast.show(text: "room info error")
            onCloseAction()
            return
        }
        
        roomInfoView.setRoomInfo(avatar: roomInfo.ownerAvatar,
                                 name: roomInfo.roomName,
                                 id: roomInfo.roomId,
                                 time: roomInfo.createdAt)
        
        joinRTCChannel()
        if roomInfo.ownerId == currentUserInfo.userId {
            CloudBarrageAPI.shared.getGameList { [weak self] err, list in
                if let err = err {
                    AUIToast.show(text: err.localizedDescription)
                    return
                }
                let dialog: JoyGameListDialog? = JoyGameListDialog.show()
                dialog?.onSelectedGame = { game in
                    guard let self = self else {return}
                    self.renewRTCTokens(roomId: roomInfo.roomId,
                                        userId: roomInfo.assistantUid) { token in
                        guard let token = token else {
                            AUIToast.show(text: "assistant token is empty")
                            return
                        }
                        self.startGame(gameInfo: game, assistantToken: token)
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
            service?.joinRoom(roomInfo: roomInfo, completion: { err in
                if let err = err {
                    AUIToast.show(text: err.localizedDescription)
                    self.onCloseAction()
                    return
                }
            })
        }
    }
}

extension RoomViewController {
    private func startGame(gameInfo: CloudGameInfo, assistantToken: String) {
        guard let roomInfo = roomInfo, let currentUserInfo = currentUserInfo else {return}
        let rtcConfig = CloudGameRtcConfig(broadcastUid: roomInfo.ownerId,
                                           assistantUid: roomInfo.assistantUid,
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
            self?.gameInfo = gameInfo
            self?.taskId = taskId
            
            CloudBarrageAPI.shared.getGameInfo(gameId: gameInfo.gameId!) { err, detail in
    
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
    
    @objc func onMoreAction() {
        
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
        
        let assistantCanvas = AgoraRtcVideoCanvas()
        broadcasterCanvas.uid = roomInfo.assistantUid
        broadcasterCanvas.view = assistantCanvasView
        engine.setupRemoteVideo(broadcasterCanvas)
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
}
