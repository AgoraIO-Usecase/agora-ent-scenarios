//
//  VoiceRoomViewController+Mic.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2022/12/6.
//

import Foundation

// MARK: - about mic 
extension VoiceRoomViewController {
    
    // 禁言指定麦位
    func mute(with index: Int) {
        ChatRoomServiceImp.getSharedInstance().forbidMic(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                self.rtcView.updateUser(mic)
            }
        }
    }

    // 取消禁言指定麦位
    func unMute(with index: Int) {
        if let user = roomInfo?.mic_info?[index] {
            if user.status == 1 && index != 0 && isOwner { return }
        }
        ChatRoomServiceImp.getSharedInstance().unForbidMic(mic_index: index) { error, mic in
            if error == nil {
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
                if let mic = mic {
                    self.rtcView.updateUser(mic)
                }
            }
        }
    }

    // 踢用户下麦
    func kickoff(with index: Int) {
        ChatRoomServiceImp.getSharedInstance().kickOff(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                self.rtcView.updateUser(mic)
            }
        }
    }

    // 锁麦
    func lock(with index: Int) {
        ChatRoomServiceImp.getSharedInstance().lockMic(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                self.rtcView.updateUser(mic)
            }
        }

    }

    // 取消锁麦
    func unLock(with index: Int) {
        ChatRoomServiceImp.getSharedInstance().unLockMic(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                self.rtcView.updateUser(mic)
            }
        }
    }

    // 下麦
    func leaveMic(with index: Int) {
        ChatRoomServiceImp.getSharedInstance().leaveMic(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                self.rtcView.updateUser(mic)
                self.local_index = nil
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: self.isOwner)
                self.chatBar.refresh(event: .mic, state: .unSelected, asCreator: self.isOwner)
            }
        }
        

    }

    // mute自己
    func muteLocal(with index: Int) {
        ChatRoomServiceImp.getSharedInstance().muteLocal(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                self.chatBar.refresh(event: .mic, state: .selected, asCreator: false)
                self.rtckit.muteLocalAudioStream(mute: true)
                self.rtcView.updateUser(mic)
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")",point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        }
    }

    // unmute自己
    func unmuteLocal(with index: Int) {
        /**
         1.如果房主禁言了用户，用户没办法解除禁言
         2.如果客户mute了自己，房主没办法打开用户
         */
        if let mic = roomInfo?.mic_info?[index] {
            if mic.status == 2 && isOwner == false {
                view.makeToast("voice_banned".voice_localized())
                return
            }
        }
        
        ChatRoomServiceImp.getSharedInstance().unmuteLocal(mic_index: index) { error, mic in
            if error == nil,let mic = mic {
                if mic.member?.micStatus ?? 0 == 1 {
                    self.chatBar.refresh(event: .mic, state: .unSelected, asCreator: false)
                }
                self.rtckit.muteLocalAudioStream(mute: false)
                self.rtcView.updateUser(mic)
            }
        }

    }

    func changeMic(from: Int, to: Int) {
        if let mic: VRRoomMic = roomInfo?.mic_info?[to] {
            if mic.status == 3 || mic.status == 4 {
                view.makeToast("voice_mic_closed".voice_localized())
                return
            }
        }
        
        ChatRoomServiceImp.getSharedInstance().changeMic(old_index: from, new_index: to) { error, micMap in
            if error == nil,let old_mic = micMap?[from],let new_mic = micMap?[to] {
                self.local_index = to
                self.roomInfo?.mic_info?[from] = old_mic
                self.roomInfo?.mic_info?[to] = new_mic
                self.rtcView.updateUser(old_mic)
                self.rtcView.updateUser(new_mic)
                guard let mic = ChatRoomServiceImp.getSharedInstance().mics.first(where: {
                                    VoiceRoomUserInfo.shared.user?.chat_uid ?? "" == $0.member?.chat_uid ?? ""
                                }) else { return }
                self.micMuteManager(mic: mic)
            }
        }
    }


    func showMuteView(with index: Int) {
        let isHairScreen =  Screen.isFullScreen
        let muteView = VMMuteView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: isHairScreen ? 264 : 264 - 34))
        guard let mic_info = roomInfo?.mic_info?[index] else { return }
        muteView.isOwner = isOwner
        muteView.micInfo = mic_info
        muteView.resBlock = { [weak self] state in
            self?.dismiss(animated: true)
            if state == .leave {
                self?.leaveMic(with: index)
            } else if state == .mute {
                self?.muteLocal(with: index)
            } else {
                self?.unmuteLocal(with: index)
            }
        }
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: isHairScreen ? 264 : 264 - 34)), custom: muteView)
        presentViewController(vc)
    }

    @objc func leaveRoom() {
        ChatRoomServiceImp.getSharedInstance().leaveRoom(self.roomInfo?.room?.room_id ?? "") { _, _ in
        }
    }

    func refuse() {
        ChatRoomServiceImp.getSharedInstance().refuseInvite(chat_uid: self.roomInfo?.room?.owner?.chat_uid ?? "") { _, _ in
            
        }
    }

    func agreeInvite(index: Int?) {
        var idx: Int?
        if index != -1, index != 0 {
            idx = index
        }
        ChatRoomServiceImp.getSharedInstance().acceptMicSeatInvitation(index: idx,completion: { error, mic in
            if error == nil,let mic = mic {
                self.rtcView.updateUser(mic)
                self.local_index = mic.mic_index
                self.chatBar.refresh(event: .handsUp, state: .disable, asCreator: self.isOwner)
                self.chatBar.refresh(event: .mic, state: .unSelected, asCreator: self.isOwner)
                self.rtckit.muteLocalAudioStream(mute: mic.status != 0)
                self.checkEnterSeatAudioAuthorized()
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")",point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        })
    }

}
