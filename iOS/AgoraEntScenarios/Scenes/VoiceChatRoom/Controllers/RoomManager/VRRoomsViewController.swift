//
//  VRRoomsViewController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib

let bottomSafeHeight = safeAreaExist ? 33 : 0
let page_size = 15

@objc public final class VRRoomsViewController: VRBaseViewController {
    
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

    private lazy var background: UIImageView = {
        UIImageView(frame: self.view.frame).image(UIImage("roomList")!)
    }()

    private lazy var container: VoiceRoomPageContainer = {
        VoiceRoomPageContainer(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - ZNavgationHeight - 10 - CGFloat(ZBottombarHeight) - 30), viewControllers: [self.normal]).backgroundColor(.clear)
    }()

    private lazy var create: VRRoomCreateView = {
        VRRoomCreateView(frame: CGRect(x: 0, y: self.container.frame.maxY - 50, width: ScreenWidth, height: 72)).image(UIImage("blur")!).backgroundColor(.clear)
    }()

    @objc convenience init(user: VLLoginModel) {
        self.init()
        self.currentUser = user
        VoiceRoomIMManager.shared?.configIM(appkey: "52117440#955012")
        // MARK: - you can replace request host call this.
        VoiceRoomBusinessRequest.shared.changeHost(host: "http://a1-test-voiceroom.easemob.com")
        if user.hasVoiceRoomUserInfo {
            self.mapUser(user: user)
        } else {
            self.login()
        }
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.navigation.title.text = LanguageManager.localValue(key: "Agora Chat Room")
    }
    
    private func showContent() {
        self.view.addSubViews([self.background, self.container, self.create])
        self.view.bringSubviewToFront(self.navigation)
        self.viewsAction()
        self.childViewControllersEvent()
    }
}

extension VRRoomsViewController {
    private func mapUser(user: VLLoginModel?) {
        let current = VRUser()
        current.chat_uid = user?.chat_uid
        current.rtc_uid = user?.rtc_uid
        current.im_token = user?.im_token
        current.authorization = user?.authorization
        current.channel_id = user?.channel_id
        current.uid = user?.userNo
        current.name = user?.name
        current.portrait = user?.headUrl
        VoiceRoomUserInfo.shared.user = current
    }

    private func login() {
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .login(()), params: ["deviceId": self.currentUser?.userNo ?? "", "portrait": self.currentUser?.headUrl ?? "", "name": self.currentUser?.name ?? ""], classType: VRUser.self) { user, error in
            if error == nil, user != nil {
                self.currentUser?.hasVoiceRoomUserInfo = true
                self.currentUser?.chat_uid = user?.chat_uid ?? ""
                self.currentUser?.channel_id = user?.channel_id ?? ""
                self.currentUser?.rtc_uid = user?.rtc_uid ?? ""
                self.currentUser?.authorization = user?.authorization ?? ""
                self.currentUser?.im_token = user?.im_token ?? ""
                print("avatar url: \(self.currentUser?.headUrl ?? "")")
                VoiceRoomUserInfo.shared.user = user
                VoiceRoomBusinessRequest.shared.userToken = user?.authorization ?? ""
                self.showContent()
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
    }

    private func viewsAction() {
        self.create.action = { [weak self] in
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
            let alert = VoiceRoomPasswordAlert(frame: CGRect(x: 37.5, y: 168, width: ScreenWidth - 75, height: (ScreenWidth - 63 - 3 * 16) / 4.0 + 177)).cornerRadius(16).backgroundColor(.white)
            let vc = VoiceRoomAlertViewController(compent: component(), custom: alert)
            self.presentViewController(vc)
            alert.actionEvents = {
                if $0 == 31 {
                    room.roomPassword = alert.code
                    self.validatePassword(room: room, password: alert.code)
                }
                vc.dismiss(animated: true)
            }
        } else {
            self.loginIMThenPush(room: room)
        }
    }

    private func component() -> PresentedViewComponent {
        var component = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: ScreenHeight))
        component.destination = .center
        component.canPanDismiss = false
        return component
    }

    private func validatePassword(room: VRRoomEntity, password: String) {
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .validatePassWord(roomId: room.room_id ?? ""), params: ["password": password]) { dic, error in
            if error == nil, let result = dic?["result"] as? Bool, result {
                self.loginIMThenPush(room: room)
            } else {
                self.view.makeToast("Password wrong!")
            }
        }
    }

    private func loginIMThenPush(room: VRRoomEntity) {
        SVProgressHUD.show(withStatus: "Loading".localized())
        VoiceRoomIMManager.shared?.loginIM(userName: VoiceRoomUserInfo.shared.user?.chat_uid ?? "", token: VoiceRoomUserInfo.shared.user?.im_token ?? "", completion: { userName, error in
            SVProgressHUD.dismiss()
            if error == nil {
                let info = VRRoomInfo()
                info.room = room
                let vc = VoiceRoomViewController(info: info)
                self.navigationController?.pushViewController(vc, animated: true)
            } else {
                self.view.makeToast("Loading failed,please retry or install again!")
            }
        })
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

        self.normal.didSelected = { [weak self] in
            self?.entryRoom(room: $0)
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
