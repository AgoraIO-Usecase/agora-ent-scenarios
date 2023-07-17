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

@objc public final class SARoomsViewController: SABaseViewController {
    private var index: Int = 0 {
        didSet {
            DispatchQueue.main.async {
                self.container.index = self.index
            }
        }
    }
    private let normal = SANormalRoomsViewController()

    private var currentUser: VLLoginModel?

    private lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage.sceneImage(name: "roomList", bundleName: "VoiceChatRoomResource")!)

    private lazy var container: SAPageContainer = {
        SAPageContainer(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - ZNavgationHeight - 10 - CGFloat(ZBottombarHeight) - 30), viewControllers: [self.normal]).backgroundColor(.clear)
    }()

    private lazy var create: SARoomCreateView = .init(frame: CGRect(x: 0, y: self.container.frame.maxY - 50, width: ScreenWidth, height: 72)).image(UIImage.sceneImage(name: "blur", bundleName: "VoiceChatRoomResource")!).backgroundColor(.clear)
    
    private var initialError: AgoraChatError?
    private var isDestory: Bool = false
    
    @objc convenience init(user: VLLoginModel) {
        AppContext.shared.sceneImageBundleName = "SpatialAudioResource"
        AppContext.shared.sceneLocalizeBundleName = "SpatialAudioResource"
        self.init()
        currentUser = user
        if SAIMManager.shared == nil {
            SAIMManager.shared = SAIMManager()
        }
        self.initialError = SAIMManager.shared?.configIM(appkey: KeyCenter.IMAppKey ?? "")
        mapUser(user: user)
        self.showContent()
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        navigation.title.text = sceneLocalized( "Agora Chat Room")
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        isDestory = true
    }
    
    public override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if isDestory {
            destory()
        }
    }

    private func showContent() {
        view.addSubViews([background, container, create])
        view.bringSubviewToFront(navigation)
        viewsAction()
        childViewControllersEvent()
    }
    
    func destory() {
        SAIMManager.shared?.logoutIM()
        SAIMManager.shared = nil
        SAUserInfo.shared.user = nil
        SAUserInfo.shared.currentRoomOwner = nil
    }
    
    deinit {
        print("\(self.swiftClassName ?? "") is destroyed!")
    }
}

extension SARoomsViewController {
    private func mapUser(user: VLLoginModel?) {
        let current = SAUser()
        current.chat_uid = user?.chat_uid
        current.rtc_uid = user?.id
        current.channel_id = user?.channel_id
        current.uid = user?.id
        current.name = user?.name
        current.portrait = user?.headUrl
        SAUserInfo.shared.user = current
    }


    private func viewsAction() {
        create.action = { [weak self] in
            self?.isDestory = false
            self?.navigationController?.pushViewController(SACreateRoomViewController(), animated: true)
        }
    }

    private func entryRoom(room: SARoomEntity) {
        if room.is_private ?? false {
            self.normal.roomList.isUserInteractionEnabled = true
            let width = ScreenWidth - 75
            let height = (ScreenWidth - 63 - 3 * 16) / 4.0 + 177
            var component = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                         height: ScreenHeight))
            component.destination = .center
            component.canPanDismiss = false
            
            let alert = SAPasswordAlert(frame: CGRect(x: 37.5,
                                                      y: 168,
                                                      width: width,
                                                      height: height)).backgroundColor(.white).cornerRadius(16)
            let vc = SAAlertViewController(compent: component, custom: alert)
            sa_presentViewController(vc)
            alert.actionEvents = {
                if $0 == 31 {
                    if room.roomPassword == alert.code {
                        self.loginIMThenPush(room: room)
                    } else {
                        self.view.makeToast("Incorrect Password".spatial_localized())
                    }
                }
                vc.dismiss(animated: true)
            }
        } else {
            loginIMThenPush(room: room)
        }
    }

    private func loginIMThenPush(room: SARoomEntity) {
        SVProgressHUD.show(withStatus: "Loading".spatial_localized())
        AppContext.saServiceImp().joinRoom(room.room_id ?? "") { [weak self] error, room_entity, robot in
            SVProgressHUD.dismiss()
            guard let self = self else {return}
            if error == nil {
                self.mapUser(user: VLUserCenter.user)
                let info: SARoomInfo = SARoomInfo()
                info.room = room
                info.robotInfo = robot ?? SARobotAudioInfo()
                info.mic_info = nil
                self.isDestory = false
                let vc = SARoomViewController(info: info)
                self.navigationController?.pushViewController(vc, animated: true)
            } else {
                SVProgressHUD.showError(withStatus: "join room failed!")
            }
            
            self.normal.roomList.isUserInteractionEnabled = true
        }
    }

    private func childViewControllersEvent() {
        normal.didSelected = { [weak self] room in
            SAThrottler.throttle(queue: .main,delay: .seconds(1.5),shouldRunLatest: true) {
                self?.normal.roomList.isUserInteractionEnabled = false
                self?.entryRoom(room: room)
            }
        }
    }
}
