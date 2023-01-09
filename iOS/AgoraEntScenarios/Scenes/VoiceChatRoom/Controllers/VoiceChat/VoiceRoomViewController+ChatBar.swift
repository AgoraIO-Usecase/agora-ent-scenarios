//
//  VoiceRoomViewController+ChatBar.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/10/11.
//

import Foundation
import SVGAPlayer
import KakaJSON
import ZSwiftBaseLib

extension VoiceRoomViewController {
    func showEQView() {
        preView = VMPresentView(frame: CGRect(x: 0, y: ScreenHeight, width: ScreenWidth, height: 280~))
        preView.isAudience = !isOwner
        preView.roomInfo = roomInfo
        preView.ains_state = ains_state
        preView.isTouchAble = roomInfo?.room?.use_robot ?? false
        preView.selBlock = { [weak self] state in
            self?.ains_state = state
            self?.rtckit.setAINS(with: state)
            if self?.isOwner == false || self?.roomInfo?.room?.use_robot == false { return }
            if state == .high {
                self?.rtckit.playMusic(with: .ainsHigh)
            } else if state == .mid {
                self?.rtckit.playMusic(with: .ainsMid)
            } else {
                self?.rtckit.playMusic(with: .ainsOff)
            }
        }
        preView.useRobotBlock = { [weak self] flag in
            if self?.alienCanPlay == true && flag == true {
                self?.roomInfo?.room?.use_robot = true
                self?.rtckit.playMusic(with: .alien)
            }
//            if self?.alienCanPlay == true && flag == false {
//                self?.rtckit.stopPlayMusic()
//            }
//
            if flag == false {
                self?.roomInfo?.room?.use_robot = false
                self?.rtckit.stopPlayMusic()
            }
            self?.preView.isTouchAble = flag
            self?.activeAlien(flag)
        }
        preView.volBlock = { [weak self] vol in
            self?.updateVolume(vol)
        }
        preView.eqView.effectClickBlock = { [weak self] type in

            /**
             1.如果是观众，则toast 提示
             2.如果是主播先要判断是否开启机器人
             */
            if self!.isOwner == false {
                self?.view.makeToast("Host Sound".localized())
                return
            }
            if self?.roomInfo?.room?.use_robot == false {
                self?.view.makeToast("Active First".localized())
                return
            }

            if type == .none {
                // 如果选择的是其他音效。弹窗确认是否需要退出
                self?.showExitRoomView()
                return
            }
            self?.rtckit.playMusic(with: self?.getSceneType(self?.roomInfo?.room?.sound_effect ?? 1) ?? .social)
        }
        preView.eqView.soundBlock = { [weak self] index in
            if self?.isOwner == false {
                self?.view.makeToast("Host Bot".localized())
                return
            }
            if let use_robot = self?.roomInfo?.room?.use_robot {
                if use_robot == false {
                    self?.view.makeToast("Active First".localized())
                    return
                }
            }
            let count = (index - 1000) / 10
            let tag = (index - 1000) % 10
            self?.rtckit.playSound(with: count, type: tag == 1 ? .ainsOff : .ainsHigh)
            self?.rtcView.updateAlienMic(.blue)
        }
        preView.eqView.visitBlock = { [weak self] in
            let VC: VoiceRoomHelpViewController = .init()
            self?.navigationController?.pushViewController(VC, animated: true)
        }
        view.addSubview(preView)
        isShowPreSentView = true
        sRtcView.isUserInteractionEnabled = false
        rtcView.isUserInteractionEnabled = false
        headerView.isUserInteractionEnabled = false

        UIView.animate(withDuration: 0.5, animations: {
            self.preView.frame = CGRect(x: 0, y: ScreenHeight - 360~, width: ScreenWidth, height: 360~)
        }, completion: nil)
    }

    func applyMembersAlert(position: VoiceRoomSwitchBarDirection) {
        let apply = VoiceRoomApplyUsersViewController(roomId: roomInfo?.room?.room_id ?? "")
        apply.agreeApply = {
            self.rtcView.updateUser($0)
        }
        let invite = VoiceRoomInviteUsersController(roomId: roomInfo?.room?.room_id ?? "", mic_index:nil)
        let userAlert = VoiceRoomUserView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 420), controllers: [apply, invite], titles: [LanguageManager.localValue(key: "Raised Hands"), LanguageManager.localValue(key: "Invite On-Stage")], position: position).cornerRadius(20, [.topLeft, .topRight], .white, 0)
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 420)), custom: userAlert)
        presentViewController(vc)
    }

    func showGiftAlert() {
        let giftsAlert = VoiceRoomGiftsView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (110 / 84.0) * ((ScreenWidth - 30) / 4.0) + 180), gifts: gifts()).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (110 / 84.0) * ((ScreenWidth - 30) / 4.0) + 180)), custom: giftsAlert)
        giftsAlert.sendClosure = { [weak self] in
            self?.sendGift(gift: $0)
            if $0.gift_id == "VoiceRoomGift9" {
                vc.dismiss(animated: true)
                self?.rocketAnimation()
            }
        }
        presentViewController(vc)
    }

    func sendGift(gift: VoiceRoomGiftEntity) {
        gift.userName = VoiceRoomUserInfo.shared.user?.name ?? ""
        gift.portrait = VoiceRoomUserInfo.shared.user?.portrait ?? userAvatar
        if let chatroom_id = roomInfo?.room?.chatroom_id, let id = gift.gift_id, let name = gift.gift_name, let value = gift.gift_price, let count = gift.gift_count {
            VoiceRoomIMManager.shared?.sendCustomMessage(roomId: chatroom_id, event: VoiceRoomGift, customExt: ["gift_id": id, "gift_name": name, "gift_price": value, "gift_count": count, "userName": VoiceRoomUserInfo.shared.user?.name ?? "", "portrait": VoiceRoomUserInfo.shared.user?.portrait ?? self.userAvatar], completion: { [weak self] message, error in
                guard let self = self else { return }
                if error == nil, message != nil {
                    let amount = Int(gift.gift_price ?? "1")!*Int(gift.gift_count ?? "1")!
                    if var currentAmount = VoiceRoomUserInfo.shared.user?.amount {
                        currentAmount += amount
                        VoiceRoomUserInfo.shared.user?.amount = currentAmount
                    }
                    if var room_amount = self.roomInfo?.room?.gift_amount {
                        room_amount += amount
                        self.roomInfo?.room?.gift_amount = room_amount
                        if self.isOwner {
                            VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["gift_amount":"\(room_amount)"], completion: { error in })
                            
                        }
                    }
                    if self.roomInfo?.room?.ranking_list == nil {
                        self.roomInfo?.room?.ranking_list = [VRUser]()
                    }
                    if (VoiceRoomUserInfo.shared.user?.chat_uid ?? "").isEmpty {
                        VoiceRoomUserInfo.shared.user?.chat_uid = VLUserCenter.user.chat_uid
                    }
                    let ranker = self.roomInfo?.room?.ranking_list?.first(where: { $0.chat_uid ?? "" == VoiceRoomUserInfo.shared.user?.chat_uid ?? ""
                    })
                    if ranker == nil {
                        self.roomInfo?.room?.ranking_list?.append(VoiceRoomUserInfo.shared.user!)
                    } else {
                        ranker?.amount = VoiceRoomUserInfo.shared.user?.amount
                    }
                    VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["ranking_list":self.roomInfo?.room?.ranking_list?.kj.JSONString() ?? ""], completion: { error in
                        if error != nil {
                            self.view.makeToast("update ranking_list failed!\(error?.errorDescription ?? "")")
                        } else {
                            self.requestRankList()
                        }
                    })
                    var giftList: VoiceRoomGiftView? = self.view.viewWithTag(1111) as? VoiceRoomGiftView
                    if giftList == nil {
                        giftList = self.giftList()
                        self.view.addSubview(giftList!)
                    }
                    giftList?.gifts.append(gift)
                    giftList?.cellAnimation()
                    
                } else {
                    self.view.makeToast("Send failed \(error?.errorDescription ?? "")", point: self.toastPoint, title: nil, image: nil, completion: nil)
                }
            })
        }
    }

    func rocketAnimation() {
        let player = SVGAPlayer(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ScreenHeight))
        player.loops = 1
        player.clearsAfterStop = true
        player.contentMode = .scaleAspectFill
        player.delegate = self
        player.tag(199)
        view.addSubview(player)
        let parser = SVGAParser()
        guard let path = Bundle.voiceRoomBundle.path(forResource: "rocket", ofType: "svga") else { return }
        parser.parse(with: URL(fileURLWithPath: path)) { entity in
            player.videoItem = entity
            player.startAnimation()
        } failureBlock: { error in
            player.removeFromSuperview()
        }
    }

    func changeHandsUpState() {
        if isOwner {
            applyMembersAlert(position: .left)
            chatBar.refresh(event: .handsUp, state: .selected, asCreator: true)
        } else {
            if chatBar.handsState == .unSelected {
                userApplyAlert(nil)
            } else if chatBar.handsState == .selected {
                userCancelApplyAlert()
            }
        }
    }

    func changeMicState() {
        guard let idx = local_index else {
            view.makeToast("you have no wheat slots!".localized(), point: view.center, title: nil, image: nil, completion: nil)
            return
        }
        if !isOwner, idx == 0 {
            view.makeToast("you have no wheat slots!".localized(), point: view.center, title: nil, image: nil, completion: nil)
            return
        }
        chatBar.micState = !chatBar.micState
        chatBar.refresh(event: .mic, state: chatBar.micState ? .selected : .unSelected, asCreator: false)
        // 需要根据麦位特殊处理
        chatBar.micState == true ? muteLocal(with: idx) : unmuteLocal(with: idx)
        rtckit.muteLocalAudioStream(mute: chatBar.micState)
    }

    func showUsers() {
        let contributes = VoiceRoomUserView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 420), controllers: [VoiceRoomGiftersViewController(roomId: roomInfo?.room?.room_id ?? "")], titles: [LanguageManager.localValue(key: "Contribution List")], position: .left).cornerRadius(20, [.topLeft, .topRight], .white, 0)
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 420)), custom: contributes)
        presentViewController(vc)
    }

    func showApplyAlert(_ index: Int) {
        let isHairScreen = SwiftyFitsize.isFullScreen
        let manageView = VMManagerView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: isHairScreen ? 264~ : 264~ - 34))
        let mic_info = ChatRoomServiceImp.getSharedInstance().mics[safe: index]
        manageView.micInfo = mic_info
        manageView.resBlock = { [weak self] state, flag in
            self?.dismiss(animated: true)
            if state == .invite {
                if flag {
                    self?.applyMembersAlert(position: .right)
                } else {
                    self?.kickoff(with: index)
                }
            } else if state == .mute {
                if flag {
                    self?.mute(with: index)
                } else {
                    self?.unMute(with: index)
                }
            } else {
                if flag {
                    self?.lock(with: index)
                } else {
                    self?.unLock(with: index)
                }
            }
        }
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: isHairScreen ? 264~ : 264~ - 34)), custom: manageView)
        presentViewController(vc)
    }

    func userApplyAlert(_ index: Int?) {
        if chatBar.handsState == .selected {
            view.makeToast("Request Wait".localized())
            return
        }

        if let mic_index = index {
            if let mic: VRRoomMic = roomInfo?.mic_info?[mic_index] {
                if mic.status == 3 || mic.status == 4 {
                    view.makeToast("Mic Closed".localized())
                    return
                }
            }
        }

        let applyAlert = VoiceRoomApplyAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (205 / 375.0) * ScreenWidth), content: "Request to Speak?", cancel: "Cancel", confirm: "Confirm", position: .bottom).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)), custom: applyAlert)
        applyAlert.actionEvents = { [weak self] in
            if $0 == 31 {
                self?.requestSpeak(index: index)
            }
            vc.dismiss(animated: true)
        }
        presentViewController(vc)
    }

    func requestSpeak(index: Int?) {
        ChatRoomServiceImp.getSharedInstance().startMicSeatApply(index: index) { error, flag in
            if error == nil {
                if flag {
                    self.chatBar.refresh(event: .handsUp, state: .selected, asCreator: false)
                    self.view.makeToast("Apply success!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                } else {
                    self.view.makeToast("Apply failed!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                }
            } else {
                
            }
        }
    }

    func cancelRequestSpeak(index: Int?) {
        ChatRoomServiceImp.getSharedInstance().cancelMicSeatApply(chat_uid: self.roomInfo?.room?.owner?.chat_uid ?? "") { error, flag in
            if error == nil {
                self.view.makeToast("Cancel apply success!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
            } else {
                self.view.makeToast("Cancel apply failed!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        }
    }

    func userCancelApplyAlert() {
        let cancelAlert = VoiceRoomCancelAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)), custom: cancelAlert)
        cancelAlert.actionEvents = { [weak self] in
            if $0 == 30 {
                self?.cancelRequestSpeak(index: nil)
            }
            vc.dismiss(animated: true)
        }
        presentViewController(vc)
    }

    func showExitRoomView() {
        let confirmView = VMConfirmView(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 40~, height: 220~), type: .leave)
        var compent = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 40~, height: 220~))
        compent.destination = .center
        let vc = VoiceRoomAlertViewController(compent: compent, custom: confirmView)
        confirmView.resBlock = { [weak self] flag in
            self?.dismiss(animated: true)
            if flag {
                self?.didHeaderAction(with: .popBack, destroyed: false)
            }
        }
        presentViewController(vc)
    }
    
    func giftList() -> VoiceRoomGiftView {
        VoiceRoomGiftView(frame: CGRect(x: 10, y: chatView.frame.minY - (ScreenWidth / 9.0 * 2), width: ScreenWidth / 3.0 * 2 + 20, height: ScreenWidth / 9.0 * 1.8)).backgroundColor(.clear).tag(1111)
    }

    func startMessage() -> VoiceRoomChatEntity {
        VoiceRoomUserInfo.shared.currentRoomOwner = roomInfo?.room?.owner
        let entity = VoiceRoomChatEntity()
        entity.userName = roomInfo?.room?.owner?.name
        entity.content = "Welcome to the voice chat room! Pornography, gambling or violence is strictly prohibited in the room.".localized()
        entity.attributeContent = entity.attributeContent
        entity.uid = roomInfo?.room?.owner?.uid
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }

    @objc func resignKeyboard() {
        inputBar.hiddenInputBar()
    }
}
