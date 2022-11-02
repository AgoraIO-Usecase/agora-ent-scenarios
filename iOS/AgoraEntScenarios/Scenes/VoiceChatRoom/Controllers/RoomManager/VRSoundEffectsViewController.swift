//
//  VRSoundEffectsViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib

public class VRSoundEffectsViewController: VRBaseViewController {
    var code = ""

    var name = ""

    var type = 0

    lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage("roomList")!)

    lazy var effects: VRSoundEffectsList = .init(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - CGFloat(ZBottombarHeight) - CGFloat(ZTabbarHeight)), style: .plain).separatorStyle(.none).tableFooterView(UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 120))).backgroundColor(.clear)

    lazy var done: UIImageView = .init(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - 70, width: ScreenWidth, height: 92)).image(UIImage("blur")!).isUserInteractionEnabled(true)

    lazy var createContainer: UIView = .init(frame: CGRect(x: 30, y: 15, width: ScreenWidth - 60, height: 50)).backgroundColor(.white)

    lazy var toLive: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 30, y: 15, width: ScreenWidth - 60, height: 50)).title(LanguageManager.localValue(key: "Go Live"), .normal).font(.systemFont(ofSize: 16, weight: .semibold)).setGradient([UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1), UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).cornerRadius(25).addTargetFor(self, action: #selector(VRSoundEffectsViewController.entryRoom), for: .touchUpInside)
    }()

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        view.addSubViews([background, effects, done])
        done.addSubViews([createContainer, toLive])
        view.bringSubviewToFront(navigation)
        navigation.title.text = LanguageManager.localValue(key: "Sound Selection")
        createContainer.layer.cornerRadius = 25
        createContainer.layer.shadowRadius = 8
        createContainer.layer.shadowOffset = CGSize(width: 0, height: 4)
        createContainer.layer.shadowColor = UIColor(red: 0, green: 0.55, blue: 0.98, alpha: 0.2).cgColor
        createContainer.layer.shadowOpacity = 1
    }

    @objc func goLive() {
        if name.isEmpty || effects.type.isEmpty {
            view.makeToast("No Room Name".localized(), point: view.center, title: nil, image: nil, completion: nil)
        }
        Throttler.throttle {
            self.createRoom()
        }
    }

    private func createRoom() {
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .createRoom(()), params: ["name": name, "is_private":!code.isEmpty, "password": code, "type": type, "sound_effect": effects.type, "allow_free_join_mic": false], classType: VRRoomInfo.self) { info, error in
            if error == nil, info != nil {
                self.view.makeToast("Room Created".localized(), point: self.view.center, title: nil, image: nil, completion: nil)
                let vc = VoiceRoomViewController(info: info!)
                self.navigationController?.pushViewController(vc, animated: true)
            } else {
                self.view.makeToast("Create failed!".localized(), point: self.view.center, title: nil, image: nil, completion: nil)
            }
        }
    }

    @objc private func entryRoom() {
        SVProgressHUD.show(withStatus: "Loading".localized())
        VoiceRoomIMManager.shared?.loginIM(userName: VoiceRoomUserInfo.shared.user?.chat_uid ?? "", token: VoiceRoomUserInfo.shared.user?.im_token ?? "", completion: { userName, error in
            SVProgressHUD.dismiss()
            if error == nil {
                self.goLive()
            } else {
                self.view.makeToast("Login failed!", point: self.view.center, title: nil, image: nil, completion: nil)
            }
        })
    }
}
