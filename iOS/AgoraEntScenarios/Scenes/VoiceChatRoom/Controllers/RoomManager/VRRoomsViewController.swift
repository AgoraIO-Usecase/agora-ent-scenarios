//
//  VRRoomsViewController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib
import AgoraChat

let bottomSafeHeight = safeAreaExist ? 33 : 0
let page_size = 15

@objc final class VRRoomsViewController: VRBaseViewController {
    private var index: Int = 0 {
        didSet {
            DispatchQueue.main.async {
                self.container.index = self.index
            }
        }
    }

//    private let all = VRAllRoomsViewController()
    private let normal = VRNormalRoomsViewController()
//    private let spatialSound = VRSpatialSoundViewController()

    private var currentUser: VLLoginModel?

    private lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage.sceneImage(name: "roomList", bundleName: "VoiceChatRoomResource")!)

    private lazy var container: VoiceRoomPageContainer = {
        VoiceRoomPageContainer(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - ZNavgationHeight - 10 - CGFloat(ZBottombarHeight) - 30), viewControllers: [self.normal]).backgroundColor(.clear)
    }()

    private lazy var create: VRRoomCreateView = .init(frame: CGRect(x: 0, y: self.container.frame.maxY - 50, width: ScreenWidth, height: 72)).image(UIImage.sceneImage(name: "blur", bundleName: "VoiceChatRoomResource")!).backgroundColor(.clear)
    
    private var initialError: AgoraChatError?
    
    private var loginError: AgoraChatError?
    private var isDestory: Bool = false
    
    @objc convenience init(user: VLLoginModel) {
        self.init()
        AppContext.shared.sceneImageBundleName = "VoiceChatRoomResource"
        currentUser = user
        if VoiceRoomIMManager.shared == nil {
            VoiceRoomIMManager.shared = VoiceRoomIMManager()
        }
        self.initialError = VoiceRoomIMManager.shared?.configIM(appkey: KeyCenter.IMAppKey ?? "")
        mapUser(user: user)
        self.showContent()
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        navigation.title.text = LanguageManager.localValue(key: "voice_app_name")
    }
    
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        guard let imKey = KeyCenter.IMAppKey, !imKey.isEmpty else {
            navigationController?.popViewController(animated: true)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                SVProgressHUD.showError(withStatus: "voice_im_key_empty_error".voice_localized())
            }
            return
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        isDestory = true
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if isDestory {
            destory()
        }
    }

    private func showContent() {
        view.addSubViews([background, container, create])
        view.bringSubviewToFront(navigation)
        viewsAction()
        self.fetchIMConfig()
        childViewControllersEvent()
    }
    
    func destory() {
        VoiceRoomIMManager.shared = nil
        ChatRoomServiceImp._sharedInstance = nil
        VoiceRoomUserInfo.shared.user = nil
        VoiceRoomUserInfo.shared.currentRoomOwner = nil
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
    
    deinit {
        VoiceRoomIMManager.shared?.logoutIM()
        VoiceRoomIMManager.shared = nil
    }
}

extension VRRoomsViewController {
    
    private func fetchIMConfig(completion: ((Bool) -> ())? = nil) {
        SVProgressHUD.show()
        self.view.isUserInteractionEnabled = false
        NetworkManager.shared.generateIMConfig(type: 1, channelName: "", nickName: VLUserCenter.user.name, chatId: "", imUid: VLUserCenter.user.id, password: "12345678", uid:  VLUserCenter.user.id, sceneType: .voice) { [weak self] uid, room_id, token in
            self?.view.isUserInteractionEnabled = true
            VLUserCenter.user.chat_uid = uid ?? ""
            VLUserCenter.user.im_token = token ?? ""
            SVProgressHUD.dismiss()
            if let userId = uid,let im_token = token {
                if self?.initialError == nil {
                    self?.view.isUserInteractionEnabled = false
                    VoiceRoomIMManager.shared?.loginIM(userName: userId, token: im_token, completion: { userName, error in
                        self?.view.isUserInteractionEnabled = true
                        if error == nil {
                            if (completion != nil) {
                                completion!(error == nil)
                            }
                        } else {
                            self?.loginError = error
                            self?.view.makeToast("login failed!".voice_localized(), point: CGPoint(x: ScreenWidth/2.0, y: ScreenHeight/2.0), title: nil, image: nil, completion: nil)
                        }
                    })
                }
            }
        }
    }
    
    private func mapUser(user: VLLoginModel?) {
        let current = VRUser()
        current.chat_uid = user?.chat_uid
        current.rtc_uid = user?.id
        current.channel_id = user?.channel_id
        current.uid = user?.id
        current.name = user?.name
        current.portrait = user?.headUrl
        VoiceRoomUserInfo.shared.user = current
    }


    private func viewsAction() {
        create.action = { [weak self] in
            self?.isDestory = false
            self?.navigationController?.pushViewController(VRCreateRoomViewController(), animated: true)
        }
//        self.container.scrollClosure = { [weak self] in
//            let idx = IndexPath(row: $0, section: 0)
//            guard let self = self else { return }
//            self.menuBar.refreshSelected(indexPath: idx)
//        }
//        self.menuBar.selectClosure = { [weak self] in
//            self?.index = $0.row
//        }
    }

    private func entryRoom(room: VRRoomEntity) {
        if room.is_private ?? false {
            self.normal.roomList.isUserInteractionEnabled = true
            let alert = VoiceRoomPasswordAlert(frame: CGRect(x: 37.5, y: 168, width: ScreenWidth - 75, height: (ScreenWidth - 63 - 3 * 16) / 4.0 + 177)).cornerRadius(16).backgroundColor(.white)
            let vc = VoiceRoomAlertViewController(compent: component(), custom: alert)
            presentViewController(vc)
            alert.actionEvents = {
                if $0 == 31 {
                    if room.roomPassword == alert.code {
                        if self.loginError == nil {
                            self.loginIMThenPush(room: room)
                        } else {
                            self.fetchIMConfig { [weak self] success in
                                if success {
                                    self?.loginIMThenPush(room: room)
                                } else {
                                    self?.normal.roomList.isUserInteractionEnabled = false
                                }
                            }
                        }
                    } else {
                        self.view.makeToast("voice_incorrect_password".voice_localized())
                    }
                }
                vc.dismiss(animated: true)
            }
        } else {
            if self.loginError == nil {
                self.loginIMThenPush(room: room)
            } else {
                self.fetchIMConfig { success in
                    if success {
                        self.loginIMThenPush(room: room)
                    }
                }
            }
        }
    }

    private func component() -> PresentedViewComponent {
        var component = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: ScreenHeight))
        component.destination = .center
        component.canPanDismiss = false
        return component
    }

    private func loginIMThenPush(room: VRRoomEntity) {
        SVProgressHUD.show(withStatus: "voice_loading".voice_localized())
        NetworkManager.shared.generateToken(channelName: room.channel_id ?? "", uid: VLUserCenter.user.id, tokenType: .token007, type: .rtc) { token in
            VLUserCenter.user.agoraRTCToken = token ?? ""
            ChatRoomServiceImp.getSharedInstance().joinRoom(room.room_id ?? "") { error, room_entity in
                SVProgressHUD.dismiss()
                if VLUserCenter.user.chat_uid.isEmpty || VLUserCenter.user.im_token.isEmpty || self.initialError != nil {
                    SVProgressHUD.showError(withStatus: "Fetch IMconfig failed!")
                    return
                }
                self.mapUser(user: VLUserCenter.user)
                let info: VRRoomInfo = VRRoomInfo()
                info.room = room
                info.mic_info = nil
                self.isDestory = false
                let vc = VoiceRoomViewController(info: info)
                self.navigationController?.pushViewController(vc, animated: true)
                self.normal.roomList.isUserInteractionEnabled = true

            }
        }
    }

    private func childViewControllersEvent()  {
//        self.all.didSelected = { [weak self] in
//            self?.entryRoom(room: $0)
//        }
//        self.all.totalCountClosure = { [weak self] in
//            guard let self = self else { return }
//            self.menuBar.dataSource[0].detail = "(\($0))"
//            self.menuBar.menuList.reloadData()
//        }

        normal.didSelected = { [weak self] room in
            self?.normal.roomList.isUserInteractionEnabled = false
            Throttler.throttle(queue:.main,delay: .seconds(1.5)) {
                self?.entryRoom(room: room)
            }
        }
//        self.normal.totalCountClosure = { [weak self] in
//            guard let self = self else { return }
//            self.menuBar.dataSource[1].detail = "(\($0))"
//            self.menuBar.menuList.reloadData()
//        }
//
//        self.spatialSound.didSelected = { [weak self] in
//            self?.entryRoom(room: $0)
//        }
//        self.spatialSound.totalCountClosure = { [weak self] in
//            guard let self = self else { return }
//            self.menuBar.dataSource[2].detail = "(\($0))"
//            self.menuBar.menuList.reloadData()
//        }
    }
}
