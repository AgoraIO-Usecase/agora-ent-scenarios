////
////  VRSoundEffectsViewController.swift
////  VoiceRoomBaseUIKit
////
////  Created by 朱继超 on 2022/8/26.
////
//
//import SVProgressHUD
//import UIKit
//import ZSwiftBaseLib
//import AgoraChat
//
//public class SASoundEffectsViewController: SABaseViewController {
//    var code = ""
//
//    var name = ""
//
//    var type = 0
//
//    lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage.sceneImage(name: "roomList")!)
//
//    lazy var effects: SASoundEffectsList = .init(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - CGFloat(ZBottombarHeight) - CGFloat(ZTabbarHeight)), style: .plain).separatorStyle(.none).tableFooterView(UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 120))).backgroundColor(.clear)
//
//    lazy var done: UIImageView = .init(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - 70, width: ScreenWidth, height: 92)).image(UIImage.sceneImage(name: "blur")!).isUserInteractionEnabled(true)
//
//    lazy var createContainer: UIView = .init(frame: CGRect(x: 30, y: 15, width: ScreenWidth - 60, height: 50)).backgroundColor(.white)
//
//    lazy var toLive: UIButton = {
//        UIButton(type: .custom).frame(CGRect(x: 30, y: 15, width: ScreenWidth - 60, height: 50)).title(sceneLocalized("Go Live"), .normal).font(.systemFont(ofSize: 16, weight: .semibold)).setGradient([UIColor(red: 0.13, green: 0.608, blue: 1, alpha: 1), UIColor(red: 0.204, green: 0.366, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).cornerRadius(25).addTargetFor(self, action: #selector(SASoundEffectsViewController.goLive), for: .touchUpInside)
//    }()
//    
//    override public func viewDidLoad() {
//        super.viewDidLoad()
//        // Do any additional setup after loading the view.
//        view.addSubViews([background, effects, done])
//        done.addSubViews([createContainer, toLive])
//        view.bringSubviewToFront(navigation)
//        navigation.title.text = sceneLocalized("Sound Selection")
//        createContainer.layer.cornerRadius = 25
//        createContainer.layer.shadowRadius = 8
//        createContainer.layer.shadowOffset = CGSize(width: 0, height: 4)
//        createContainer.layer.shadowColor = UIColor(red: 0, green: 0.55, blue: 0.98, alpha: 0.2).cgColor
//        createContainer.layer.shadowOpacity = 1
//    }
//    
//    deinit {
//        SVProgressHUD.dismiss()
//    }
//
//    @objc func goLive() {
//        if name.isEmpty {
//            view.makeToast("No Room Name".localized(), point: view.center, title: nil, image: nil, completion: nil)
//        }
//        SAThrottler.throttle {
//            DispatchQueue.main.async {
//                self.entryRoom()
//            }
//        }
//    }
//
//    @objc private func entryRoom() {
//        AgoraChatClient.shared().logout(false)
//        SVProgressHUD.show(withStatus: "Loading".localized())
//        self.view.window?.isUserInteractionEnabled = false
//        let imId: String? = VLUserCenter.user.chat_uid.count > 0 ? VLUserCenter.user.chat_uid : nil
//        let entity = self.createEntity()
//        SpatialAudioServiceImp.getSharedInstance().initIM(with: entity.name ?? "", chatId: nil, channelId: entity.channel_id ?? "",  imUid: imId, pwd: "12345678") { im_token, uid, room_id in
//            entity.chatroom_id = room_id
//            entity.owner = SAUserInfo.shared.user
//            entity.owner?.chat_uid = uid
//            VLUserCenter.user.im_token = im_token
//            VLUserCenter.user.chat_uid = uid
//            if im_token.isEmpty || uid.isEmpty || room_id.isEmpty {
//                SVProgressHUD.dismiss()
//                var showMessage = "Fetch IMConfig failed!"
//                if room_id.isEmpty {
//                    showMessage = "Incorrect room name".localized()
//                }
//                SVProgressHUD.showError(withStatus: showMessage)
//                self.view.window?.isUserInteractionEnabled = true
//                return
//            }
//            let error = SAIMManager.shared?.configIM(appkey: KeyCenter.IMAppKey ?? "")
//            if error == nil,SAIMManager.shared != nil {
//                SAIMManager.shared?.loginIM(userName: uid , token: im_token , completion: { userName, error in
//                    SVProgressHUD.dismiss()
//                    if error == nil {
//                        AppContext.saServiceImp().createRoom(room: entity) {[weak self] error, room in
//                            SVProgressHUD.dismiss()
//                            guard let self = self else {return}
//                            self.view.window?.isUserInteractionEnabled = true
//                            if let room = room,error == nil {
//                                self.entryRoom(room: room)
//                            } else {
//                                SVProgressHUD.showError(withStatus: "Create failed!".localized())
//                            }
//                        }
//                    }else {
//                        self.view.window?.isUserInteractionEnabled = true
//                        SVProgressHUD.showError(withStatus: "LoginIM failed!".localized())
//                    }
//                    
//                })
//            }
//        }
//    }
//    
//    private func createEntity() -> SARoomEntity {
//        let entity: SARoomEntity = SARoomEntity()
//        entity.sound_effect = effects.type
//        entity.is_private = !code.isEmpty
//        entity.name = name
//        entity.roomPassword = code
//        entity.rtc_uid = Int(VLUserCenter.user.id)
//        let timeInterval: TimeInterval = Date().timeIntervalSince1970
//        let millisecond = CLongLong(round(timeInterval*1000))
//        entity.room_id = String(millisecond)
//        entity.channel_id = String(millisecond)
//        entity.created_at = UInt(millisecond)
//        entity.click_count = 3
//        entity.member_count = 3
//        return entity
//    }
//    
//    private func entryRoom(room: SARoomEntity) {
//        let info: SARoomInfo = SARoomInfo()
//        info.room = room
//        info.mic_info = nil
//        let vc = SARoomViewController(info: info)
//        self.navigationController?.pushViewController(vc, animated: true)
//    }
//}
