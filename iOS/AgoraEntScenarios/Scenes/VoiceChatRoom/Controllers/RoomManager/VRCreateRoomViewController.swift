//
//  VRCreateRoomViewController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib

public final class VRCreateRoomViewController: VRBaseViewController {
    lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage("roomList")!)

    lazy var container: VRCreateRoomView = .init(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - ZNavgationHeight))

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        view.addSubViews([background, container])
        view.bringSubviewToFront(navigation)
        navigation.title.text = LanguageManager.localValue(key: "Create Room")
        container.createAction = { [weak self] in
            guard let self = self else { return }
            print("idx:\(self.container.idx)")
            if self.container.idx <= 0 {
                self.settingSound()
            } else {
                self.goLive()
            }
        }
    }
}

extension VRCreateRoomViewController {
    private func settingSound() {
        let vc = VRSoundEffectsViewController()
        vc.code = container.roomInput.code
        vc.type = container.idx
        vc.name = container.roomInput.name
        navigationController?.pushViewController(vc, animated: true)
    }

    private func goLive() {
        if container.roomInput.name.isEmpty {
            view.makeToast("No Room Name".localized(), point: view.center, title: nil, image: nil, completion: nil)
        }
        SVProgressHUD.show()
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .createRoom(()), params: ["name": container.roomInput.name, "is_private": container.roomInput.code.isEmpty, "password": container.roomInput.code, "type": container.idx, "allow_free_join_mic": false, "sound_effect": "Social Chat"], classType: VRRoomInfo.self) { info, error in
            SVProgressHUD.dismiss()
            if error == nil, info != nil {
                self.view.makeToast("Room Created".localized(), point: self.view.center, title: nil, image: nil, completion: nil)
                let vc = VoiceRoomViewController(info: info!)
                self.navigationController?.pushViewController(vc, animated: true)
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")", point: self.view.center, title: nil, image: nil, completion: nil)
            }
        }
    }

    private func entryRoom() {
        SVProgressHUD.show(withStatus: "Loading".localized())
        VoiceRoomIMManager.shared?.loginIM(userName: VoiceRoomUserInfo.shared.user?.chat_uid ?? "", token: VoiceRoomUserInfo.shared.user?.im_token ?? "", completion: { userName, error in
            SVProgressHUD.dismiss()
            if error == nil {
                Throttler.throttle {
                    self.goLive()
                }
            } else {
                self.view.makeToast("AgoraChat Login failed!", point: self.view.center, title: nil, image: nil, completion: nil)
            }
        })
    }
}
