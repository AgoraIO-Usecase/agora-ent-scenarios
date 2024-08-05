//
//  RoomListViewController.swift
//  Joy
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit
import YYCategories
import AgoraRtmKit
import AgoraCommon
import SnapKit
import RTMSyncManager

class RoomListViewController: UIViewController {
    private var userInfo: InteractiveJoyUserInfo!
    
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        bar.title = LanguageManager.localValue(key: "game_room_list_vc_title")
        return bar
    }()
    
    private lazy var backgroundView = UIImageView(frame: self.view.bounds)
    private lazy var emptyView = RoomListEmptyView(frame: self.view.bounds)
    private var roomList: [InteractiveJoyRoomInfo] = [] {
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
    
    private var service: JoyServiceProtocol!
    
    deinit {
        JoyLogger.info("deinit-- RoomListViewController")
    }
    
    required init(userInfo: InteractiveJoyUserInfo, service: JoyServiceProtocol) {
        super.init(nibName: nil, bundle: nil)
        self.userInfo = userInfo
        self.service = service
        JoyLogger.info("init-- RoomListViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.white
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        backgroundView.image = UIImage.sceneImage(name: "joy_list_Bg")
        view.addSubview(backgroundView)
        view.addSubview(emptyView)
        view.addSubview(listView)
        view.addSubview(naviBar)
                
        renewRTMTokens {[weak self] token in
            guard let self = self else {return}
            guard let token = token else {
                self.navigationController?.popViewController(animated: true)
                return
            }
            self._refreshAction()
        }
    }
    
    private func configUI() {
        self.title = "房间列表"
        view.addSubview(naviBar)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        _refreshAction()
    }
}

extension RoomListViewController {
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
                                            tokenTypes: [.rtm]) {[weak self] token in
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
    
    private func prepareGotoRoom(roomInfo: InteractiveJoyRoomInfo) {
        if roomInfo.isPrivate {
            let array = [LanguageManager.localValue(key: "query_button_cancel"), LanguageManager.localValue(key: "query_button_confirm")]
            let title = LanguageManager.localValue(key: "game_private_room_title")
            VLAlert.shared().show(withFrame: UIScreen.main.bounds, title: title, message: "", placeHolder: title, type: .ALERTYPETEXTFIELD, buttonTitles: array) { [weak self] flag, text in
                if text == roomInfo.password {
                    roomInfo.password = text
                    self?.gotoRoom(roomInfo: roomInfo)
                } else {
                    VLToast.toast(LanguageManager.localValue(key: "game_room_password_error"))
                }
                
                VLAlert.shared().dismiss()
            }
        } else {
            gotoRoom(roomInfo: roomInfo)
        }
    }
    
    private func gotoRoom(roomInfo: InteractiveJoyRoomInfo) {
        service.joinRoom(roomInfo: roomInfo, completion: {[weak self] err in
            guard let self = self else { return }
            if let err = err {
                AUIToast.show(text: "\(err)")
                return
            }
            
            let gameVC = PlayGameViewController(userInfo: self.userInfo, service: self.service, roomInfo: roomInfo)
            gameVC.hidesBottomBarWhenPushed = true
            self.navigationController?.pushViewController(gameVC, animated: true)
        })
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
                       isPrivate: room.isPrivate)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let info = roomList[indexPath.item]
        self.prepareGotoRoom(roomInfo: info)
    }
}
