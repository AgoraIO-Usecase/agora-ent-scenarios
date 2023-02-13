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

//    private let all = VRAllRoomsViewController()
    private let normal = SANormalRoomsViewController()
//    private let spatialSound = VRSpatialSoundViewController()

    private var currentUser: VLLoginModel?

    private lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage("roomList")!)

    private lazy var container: SAPageContainer = {
        SAPageContainer(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - ZNavgationHeight - 10 - CGFloat(ZBottombarHeight) - 30), viewControllers: [self.normal]).backgroundColor(.clear)
    }()

    private lazy var create: SARoomCreateView = .init(frame: CGRect(x: 0, y: self.container.frame.maxY - 50, width: ScreenWidth, height: 72)).image(UIImage("blur")!).backgroundColor(.clear)
    
    private var initialError: AgoraChatError?
    
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

    private func showContent() {
        view.addSubViews([background, container, create])
        view.bringSubviewToFront(navigation)
        viewsAction()
        childViewControllersEvent()
    }
    
    deinit {
        print("\(self.swiftClassName ?? "") is destroyed!")
        SAIMManager.shared?.logoutIM()
        SAIMManager.shared = nil
        SpatialAudioServiceImp._sharedInstance = nil
        SAUserInfo.shared.user = nil
        SAUserInfo.shared.currentRoomOwner = nil
    }
}

extension SARoomsViewController {
    private func mapUser(user: VLLoginModel?) {
        let current = SAUser()
        current.chat_uid = user?.chat_uid
        current.rtc_uid = user?.id
        current.channel_id = user?.channel_id
        current.uid = user?.userNo
        current.name = user?.name
        current.portrait = user?.headUrl
        SAUserInfo.shared.user = current
    }


    private func viewsAction() {
        create.action = { [weak self] in
            self?.navigationController?.pushViewController(SACreateRoomViewController(), animated: true)
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

    private func entryRoom(room: SARoomEntity) {
        if room.is_private ?? false {
            let alert = SAPasswordAlert(frame: CGRect(x: 37.5, y: 168, width: ScreenWidth - 75, height: (ScreenWidth - 63 - 3 * 16) / 4.0 + 177)).cornerRadius(16).backgroundColor(.white)
            let vc = SAAlertViewController(compent: component(), custom: alert)
            sa_presentViewController(vc)
            alert.actionEvents = {
                if $0 == 31 {
                    if room.roomPassword == alert.code {
                        self.loginIMThenPush(room: room)
                    } else {
                        self.view.makeToast("Incorrect Password".localized())
                    }
                }
                vc.dismiss(animated: true)
            }
        } else {
            loginIMThenPush(room: room)
        }
    }

    private func component() -> SAPresentedViewComponent {
        var component = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: ScreenHeight))
        component.destination = .center
        component.canPanDismiss = false
        return component
    }

    private func loginIMThenPush(room: SARoomEntity) {
        SVProgressHUD.show(withStatus: "Loading".localized())
        AppContext.saServiceImp().joinRoom(room.room_id ?? "") { error, room_entity in
            SVProgressHUD.dismiss()
            if VLUserCenter.user.chat_uid.isEmpty || VLUserCenter.user.im_token.isEmpty || self.initialError != nil {
                SVProgressHUD.showError(withStatus: "Fetch IMconfig failed!")
                return
            }
            if error == nil, room_entity != nil {
                SAIMManager.shared?.loginIM(userName: VLUserCenter.user.chat_uid , token: VLUserCenter.user.im_token , completion: { userName, error in
                    if error == nil {
                        SVProgressHUD.showSuccess(withStatus: "IM login successful!")
                        self.mapUser(user: VLUserCenter.user)
                        let info: SARoomInfo = SARoomInfo()
                        info.room = room
                        info.mic_info = nil
                        let vc = SARoomViewController(info: info)
                        self.navigationController?.pushViewController(vc, animated: true)
                    } else {
                        SVProgressHUD.showError(withStatus: "IM login failed!")
                    }
                })
            } else {
                SVProgressHUD.showError(withStatus: "Members reach limit!")
            }
        }
    }

    private func childViewControllersEvent() {
//        self.all.didSelected = { [weak self] in
//            self?.entryRoom(room: $0)
//        }
//        self.all.totalCountClosure = { [weak self] in
//            guard let self = self else { return }
//            self.menuBar.dataSource[0].detail = "(\($0))"
//            self.menuBar.menuList.reloadData()
//        }

        normal.didSelected = { [weak self] room in
            SAThrottler.throttle(delay: .seconds(1)) {
                DispatchQueue.main.async {
                    self?.entryRoom(room: room)
                }
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
