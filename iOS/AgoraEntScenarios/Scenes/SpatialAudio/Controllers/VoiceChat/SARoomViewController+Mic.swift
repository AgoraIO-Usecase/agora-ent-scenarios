//
//  VoiceRoomViewController+Mic.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2022/12/6.
//

import Foundation

// MARK: - about mic 
extension SARoomViewController {
    
    // 禁言指定麦位
    func mute(with index: Int) {
        AppContext.saServiceImp().forbidMic(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.sRtcView.updateUser(mic)
            }
        }
    }

    // 取消禁言指定麦位
    func unMute(with index: Int) {
        if let user = roomInfo?.mic_info?[index] {
            if user.status == 1 && index != 0 && isOwner { return }
        }
        AppContext.saServiceImp().unForbidMic(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil {
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
                if let mic = mic {
                    self.roomInfo?.mic_info?[index] = mic
                    self.sRtcView.updateUser(mic)
                }
            }
        }
    }

    // 踢用户下麦
    func kickoff(with index: Int) {
        AppContext.saServiceImp().kickOff(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.sRtcView.updateUser(mic)
            }
        }
    }

    // 锁麦
    func lock(with index: Int) {
        AppContext.saServiceImp().lockMic(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.sRtcView.updateUser(mic)
            }
        }

    }

    // 取消锁麦
    func unLock(with index: Int) {
        AppContext.saServiceImp().unLockMic(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.sRtcView.updateUser(mic)
            }
        }
    }

    // 下麦
    func leaveMic(with index: Int) {
        chatBar.refresh(event: .mic, state: .unSelected, asCreator: false)
        AppContext.saServiceImp().leaveMic(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.sRtcView.updateUser(mic)
                self.rtckit.setClientRole(role: .audience)
                self.local_index = nil
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: self.isOwner)
                self.chatBar.refresh(event: .mic, state: .unSelected, asCreator: self.isOwner)
            }
        }
        

    }

    // mute自己
    func muteLocal(with index: Int) {
        AppContext.saServiceImp().muteLocal(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.chatBar.refresh(event: .mic, state: .selected, asCreator: false)
                self.rtckit.muteLocalAudioStream(mute: true)
                self.sRtcView.updateUser(mic)
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
                view.makeToast("Banned".localized())
                return
            }
        }
        
        AppContext.saServiceImp().unmuteLocal(mic_index: index) {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.chatBar.refresh(event: .mic, state: .unSelected, asCreator: false)
                self.rtckit.muteLocalAudioStream(mute: false)
                self.sRtcView.updateUser(mic)
            }
        }

    }

    func changeMic(from: Int, to: Int) {
        if let mic: SARoomMic = roomInfo?.mic_info?[to] {
            if mic.status == 3 || mic.status == 4 {
                view.makeToast("Mic Closed".localized())
                return
            }
        }
        AppContext.saServiceImp().changeMic(old_index: from, new_index: to) {[weak self] error, micMap in
            guard let self = self else {return}
            if error == nil,let old_mic = micMap?[from],let new_mic = micMap?[to] {
                self.local_index = to
                self.roomInfo?.mic_info?[from] = old_mic
                self.roomInfo?.mic_info?[to] = new_mic
                self.sRtcView.updateUser(old_mic)
                self.sRtcView.updateUser(new_mic)
                    //TODO: remove as!
                guard let mic = AppContext.saTmpServiceImp().mics.first(where: {
                                    SAUserInfo.shared.user?.chat_uid ?? "" == $0.member?.chat_uid ?? ""
                                }) else { return }
                self.rtckit.setClientRole(role: mic.status == 0 ? .owner : .audience)
                self.rtckit.muteLocalAudioStream(mute: mic.status != 0)
            }
        }

    }


    func showMuteView(with index: Int) {
        let isHairScreen = SwiftyFitsize.isFullScreen
        let muteView = SAMuteView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: isHairScreen ? 264~ : 264~ - 34))
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
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: isHairScreen ? 264~ : 264~ - 34)), custom: muteView)
        sa_presentViewController(vc)
    }

    @objc func leaveRoom() {
        if self.isOwner {
            SAIMManager.shared?.userDestroyedChatroom()
        } else {
            AppContext.saServiceImp().leaveRoom(self.roomInfo?.room?.chatroom_id ?? "") { _, _ in }
        }
    }

    func refuse() {
        AppContext.saServiceImp().refuseInvite(chat_uid: self.roomInfo?.room?.owner?.uid ?? "") { _, _ in
            
        }
    }

    func agreeInvite() {
        AppContext.saServiceImp().acceptMicSeatInvitation(completion: {[weak self] error, mic in
            guard let self = self else {return}
            if error == nil,let mic = mic {
                self.sRtcView.updateUser(mic)
                self.local_index = mic.mic_index
                self.rtckit.setClientRole(role: .owner)
                self.chatBar.refresh(event: .handsUp, state: .disable, asCreator: self.isOwner)
                self.chatBar.refresh(event: .mic, state: .unSelected, asCreator: self.isOwner)
                self.rtckit.muteLocalAudioStream(mute: mic.status != 0)
            }
        })
    }

}
