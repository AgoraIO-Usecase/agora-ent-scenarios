//
//  RoomListViewController.swift
//  Joy
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit
import YYCategories
import AgoraRtcKit


class RoomListViewController: UIViewController {
    private var userInfo: JoyUserInfo!
    private lazy var rtcEngine: AgoraRtcEngineKit = _createRtcEngine()
    private lazy var service: JoyServiceProtocol = JoyServiceImp(appId: joyAppId, user: userInfo)
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        return bar
    }()
    private lazy var backgroundView = UIImageView(frame: self.view.bounds)
    private lazy var emptyView = RoomListEmptyView(frame: self.view.bounds)
    private var roomList: [JoyRoomInfo] = [] {
        didSet {
            emptyView.isHidden = !roomList.isEmpty
            listView.reloadData()
        }
    }
    private lazy var refreshControl: UIRefreshControl = {
        let ctrl = UIRefreshControl()
        ctrl.addTarget(self, action: #selector(_refreshAction), for: .valueChanged)
        return ctrl
    }()
    
    private lazy var listView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.sectionInset = UIEdgeInsets(top: UIDevice.current.aui_SafeDistanceTop + 44, left: 20, bottom: 0, right: 20)
        let itemWidth = (self.view.width - 15 - 20 * 2) * 0.5
        layout.itemSize = CGSize(width: itemWidth, height: itemWidth)
        let collectionView = UICollectionView(frame: self.view.bounds, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.register(RoomListCell.self, forCellWithReuseIdentifier: NSStringFromClass(RoomListCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.refreshControl = self.refreshControl
        return collectionView
    }()
    
    private lazy var createButton: UIButton = {
        let button = UIButton(type: .custom)
        button.frame = CGRect(x: (self.view.aui_width - 175) / 2,
                              y: self.view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 42 - 19,
                              width: 175,
                              height: 42)
        button.backgroundColor = UIColor(hexString: "#345dff")
        button.setCornerRadius(21)
        button.setTitle("user_list_create_room".joyLocalization(), for: .normal)
        button.setImage(UIImage.sceneImage(name: "icon_add"), for: .normal)
        button.setBackgroundImage(UIImage.sceneImage(name: "room_create_button_bg1"), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.setTitleColor(.white, for: .normal)
        button.adjustHorizonAlign(spacing: 10)
        button.addTarget(self, action: #selector(_createAction), for: .touchUpInside)
        return button
    }()
    
    deinit {
        joyPrint("deinit-- RoomListViewController")
    }
    
    required init(userInfo: JoyUserInfo) {
        super.init(nibName: nil, bundle: nil)
        self.userInfo = userInfo
        joyPrint("init-- RoomListViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        backgroundView.image = UIImage.sceneImage(name: "joy_list_Bg")
        view.addSubview(backgroundView)
        view.addSubview(emptyView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        view.addSubview(createButton)
        _refreshAction()
        
        renewRTMTokens {[weak self] token in
            guard let self = self else {return}
            guard let token = token else {
                self.navigationController?.popViewController(animated: true)
                return
            }
            let config = CloudBarrageConfig(appId: joyAppId, engine: self.rtcEngine, rtmToken: token)
            CloudBarrageAPI.shared.setup(apiConfig: config)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
}

extension RoomListViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return roomList.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: RoomListCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(RoomListCell.self), for: indexPath) as! RoomListCell
        let room = roomList[indexPath.item]
        cell.setBgImge("\(indexPath.item % 5)",
                       name: room.roomName,
                       id: room.roomId,
                       badge: room.badgeTitle,
                       count: room.roomUserCount,
                       avatarUrl: room.ownerAvatar,
                       isPrivate: false)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let info = roomList[indexPath.item]
        self.gotoRoom(roomInfo: info)
    }
}

extension RoomListViewController {
    private func renewRTMTokens(completion: ((String?)->Void)?) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            joyError("renewTokens fail,userInfo == nil")
            completion?(nil)
            return
        }
        joyPrint("renewRTMTokens")
        NetworkManager.shared.generateTokens(appId: joyAppId,
                                             appCertificate: joyAppCertificate,
                                             channelName: ""/*tokenConfig.roomId*/,
                                             uid: "\(userInfo.userId)",
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtm]) {[weak self] tokens in
            guard let self = self else {return}
            guard let rtmToken = tokens[AgoraTokenType.rtm.rawValue] else {
                joyWarn("renewRTMTokens fail")
                completion?(nil)
                return
            }
            joyPrint("renewRTMTokens success")
            completion?(rtmToken)
        }
    }
    
    private func _createRtcEngine() ->AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = joyAppId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: self)
        
//        engine.setClientRole(.broadcaster)
        return engine
    }
    
    @objc func _backAction() {
        self.navigationController?.popViewController(animated: true)
    }
    
    @objc func _refreshAction() {
        service.getRoomList {[weak self] list in
            guard let self = self else {return}
            self.roomList = list
            self.refreshControl.endRefreshing()
            AUIToast.show(text: "room_list_refresh_tips".joyLocalization())
        }
    }
    
    @objc private func _createAction() {
        guard let userInfo = userInfo else {return}
        
        let vc = CreateRoomViewController(currentUserInfo: userInfo, service: service)
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    private func gotoRoom(roomInfo: JoyRoomInfo) {
        service.joinRoom(roomInfo: roomInfo, completion: {[weak self] err in
            guard let self = self else { return }
            if let err = err {
                AUIToast.show(text: err.localizedDescription)
                return
            }
            
            let vc = RoomViewController(roomInfo: roomInfo, currentUserInfo: self.userInfo, service: self.service)
            self.navigationController?.pushViewController(vc, animated: true)
        })
    }
}

extension RoomListViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        joyPrint("tokenPrivilegeWillExpire")
//        renewTokens {[weak self, weak engine] success in
//            guard let self = self, let engine = engine else {return}
//            guard success else {
//                self.rtcEngine(engine, tokenPrivilegeWillExpire: token)
//                return
//            }
//            
//            //renew callapi
//            self.callApi.renewToken(with: self.tokenConfig)
//            
//            //renew videoloader
//            self.videoLoaderApi.getConnectionMap().forEach { (channelId, connection) in
//                let mediaOptions = AgoraRtcChannelMediaOptions()
//                mediaOptions.token = self.tokenConfig.rtcToken
//                let ret = self.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
//                showTo1v1Print("renew token tokenPrivilegeWillExpire: \(channelId) \(ret)")
//            }
//        }
    }
}
