//
//  VRCreateRoomViewController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib
import AgoraChat

public final class SACreateRoomViewController: SABaseViewController {
    lazy var background: UIImageView = .init(frame: self.view.frame).image(UIImage.sceneImage(name: "roomList", bundleName: "VoiceChatRoomResource")!)

    lazy var container: SACreateRoomView = .init(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight - ZNavgationHeight))

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        view.addSubViews([background, container])
        view.bringSubviewToFront(navigation)
        navigation.title.text = "spatial_voice_create_room".localized_spatial()
        container.createAction = { [weak self] in
            guard let self = self else { return }
            print("idx:\(self.container.idx)")
            Throttler.throttle(queue:.main,delay: 1,shouldRunLatest: true) {
                self.goLive()
            }
        }
    }
    
    deinit {
        SVProgressHUD.dismiss()
    }
}

extension SACreateRoomViewController {
//    private func settingSound() {
//        let vc = SASoundEffectsViewController()
//        vc.code = container.roomInput.code
//        vc.type = container.idx
//        vc.name = container.roomInput.name
//        navigationController?.pushViewController(vc, animated: true)
//    }
    
    
    private func entryRoom(room: SARoomEntity) {
        let info: SARoomInfo = SARoomInfo()
        info.room = room
        info.mic_info = nil
        let vc = SARoomViewController(info: info)
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    private func createEntity() -> SARoomEntity {
        let code = container.roomInput.code
//        let type = container.idx
        let name = container.roomInput.name
        
        let entity: SARoomEntity = SARoomEntity()
        entity.sound_effect = 1
        entity.is_private = !code.isEmpty
        entity.name = name
        entity.roomPassword = code
        entity.rtc_uid = Int(VLUserCenter.user.id)
        let timeInterval: TimeInterval = Date().timeIntervalSince1970
        let millisecond = CLongLong(round(timeInterval*1000))
        entity.room_id = String(millisecond)
        entity.channel_id = String(millisecond)
        entity.created_at = UInt(millisecond)
        entity.click_count = 3
        entity.member_count = 3
        return entity
    }
    
    private func entryRoom() {
//        AgoraChatClient.shared().logout(false)
        SVProgressHUD.show(withStatus: "spatial_voice_loading".localized_spatial())
        self.view.window?.isUserInteractionEnabled = false
//        let imId: String? = VLUserCenter.user.chat_uid.count > 0 ? VLUserCenter.user.chat_uid : nil
        let entity = self.createEntity()
//        SpatialAudioServiceImp.getSharedInstance().initIM(with: entity.name ?? "", chatId: nil, channelId: entity.channel_id ?? "",  imUid: imId, pwd: "12345678") { im_token, uid, room_id in
        entity.chatroom_id = entity.room_id
            entity.owner = SAUserInfo.shared.user
            entity.owner?.chat_uid = SAUserInfo.shared.user?.rtc_uid
//            VLUserCenter.user.im_token = im_token
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
                        AppContext.saServiceImp().createRoom(room: entity) {[weak self] error, room in
                            SVProgressHUD.dismiss()
                            guard let self = self else {return}
                            self.view.window?.isUserInteractionEnabled = true
                            if let room = room,error == nil {
                                self.entryRoom(room: room)
                            } else {
                                SVProgressHUD.showError(withStatus: "spatial_voice_create_failed".localized_spatial())
                            }
                        }
//                    }else {
//                        self.view.window?.isUserInteractionEnabled = true
//                        SVProgressHUD.showError(withStatus: "LoginIM failed!".localized())
//                    }
                    
//                })
//            }
//        }
    }

    private func goLive() {
        if container.roomInput.name.isEmpty {
            view.makeToast("spatial_voice_no_room_name".localized_spatial(), point: view.center, title: nil, image: nil, completion: nil)
        }
        entryRoom()
    }
}
