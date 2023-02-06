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

extension SARoomViewController {
    
    func showEQView() {
        //更新为不等高弹窗视图 全都是控制器 方便业务更新
        let audioSetVC: SAAudioSettingViewController = SAAudioSettingViewController()
        audioSetVC.roomInfo = roomInfo
        audioSetVC.isAudience = !isOwner
        audioSetVC.ains_state = ains_state
        audioSetVC.isTouchAble = roomInfo?.room?.use_robot ?? false
        audioSetVC.useRobotBlock = { [weak self] flag in
            if self?.alienCanPlay == true && flag == true {
                self?.roomInfo?.room?.use_robot = true
                self?.rtckit.playMusic(with: .alien)
            }
            if flag == false {
                self?.roomInfo?.room?.use_robot = false
                self?.rtckit.stopPlayMusic()
            }
            self?.activeAlien(flag)
            self?.roomInfo?.room?.use_robot = flag
        }

        audioSetVC.volBlock = { [weak self] vol in
            self?.updateVolume(vol)
        }
        
        audioSetVC.selBlock = { [weak self] state in
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
        
        audioSetVC.turnAIAECBlock = {[weak self] flag in
            self?.rtckit.setAIAECOn(isOn: flag);
        }
        audioSetVC.turnAGCBlock = {[weak self] flag in
            self?.rtckit.setAGCOn(isOn: flag);
        }
        
        audioSetVC.effectClickBlock = { [weak self] type in

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
        audioSetVC.soundBlock = { [weak self] index in
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
        audioSetVC.visitBlock = { [weak self] in
            let VC: SAHelpViewController = .init()
            self?.navigationController?.pushViewController(VC, animated: true)
        }
        
        let presentView: SARoomPresentView = SARoomPresentView.shared
        presentView.showView(with: CGRect(x: 0, y: 0, width: ScreenWidth, height: 372~), vc: audioSetVC)
        view.addSubview(presentView)
        
    }

    func applyMembersAlert(position: SASwitchBarDirection) {
        let apply = SAApplyUsersViewController(roomId: roomInfo?.room?.room_id ?? "")
        apply.agreeApply = {
            self.rtcView.updateUser($0)
        }
        let invite = SAInviteUsersController(roomId: roomInfo?.room?.room_id ?? "", mic_index:nil)
        let userAlert = SAUserView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 420), controllers: [apply, invite], titles: [sceneLocalized( "Raised Hands"), sceneLocalized( "Invite On-Stage")], position: position).cornerRadius(20, [.topLeft, .topRight], .white, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 420)), custom: userAlert)
        sa_presentViewController(vc)
    }

    func showGiftAlert() {
        let giftsAlert = SAGiftsView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (110 / 84.0) * ((ScreenWidth - 30) / 4.0) + 180), gifts: gifts()).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (110 / 84.0) * ((ScreenWidth - 30) / 4.0) + 180)), custom: giftsAlert)
        giftsAlert.sendClosure = { [weak self] in
            self?.sendGift(gift: $0)
            if $0.gift_id == "VoiceRoomGift9" {
                vc.dismiss(animated: true)
                self?.rocketAnimation()
            }
        }
        sa_presentViewController(vc)
    }

    func sendGift(gift: SAGiftEntity) {
        gift.userName = SAUserInfo.shared.user?.name ?? ""
        gift.portrait = SAUserInfo.shared.user?.portrait ?? userAvatar
        if let chatroom_id = roomInfo?.room?.chatroom_id, let id = gift.gift_id, let name = gift.gift_name, let value = gift.gift_price, let count = gift.gift_count {
            SAIMManager.shared?.sendCustomMessage(roomId: chatroom_id, event: SARoomGift, customExt: ["gift_id": id, "gift_name": name, "gift_price": value, "gift_count": count, "userName": SAUserInfo.shared.user?.name ?? "", "portrait": SAUserInfo.shared.user?.portrait ?? self.userAvatar], completion: { [weak self] message, error in
                guard let self = self else { return }
                if error == nil, message != nil {
                    let amount = Int(gift.gift_price ?? "1")!*Int(gift.gift_count ?? "1")!
                    if var currentAmount = SAUserInfo.shared.user?.amount {
                        currentAmount += amount
                        SAUserInfo.shared.user?.amount = currentAmount
                    }
                    if var room_amount = self.roomInfo?.room?.gift_amount {
                        room_amount += amount
                        self.roomInfo?.room?.gift_amount = room_amount
                        if self.isOwner {
                            SAIMManager.shared?.setChatroomAttributes(attributes: ["gift_amount":"\(room_amount)"], completion: { error in })
                            
                        }
                    }
                    if self.roomInfo?.room?.ranking_list == nil {
                        self.roomInfo?.room?.ranking_list = [SAUser]()
                    }
                    if (SAUserInfo.shared.user?.chat_uid ?? "").isEmpty {
                        SAUserInfo.shared.user?.chat_uid = VLUserCenter.user.chat_uid
                    }
                    let ranker = self.roomInfo?.room?.ranking_list?.first(where: { $0.chat_uid ?? "" == SAUserInfo.shared.user?.chat_uid ?? ""
                    })
                    if ranker == nil {
                        self.roomInfo?.room?.ranking_list?.append(SAUserInfo.shared.user!)
                    } else {
                        ranker?.amount = SAUserInfo.shared.user?.amount
                    }
                    SAIMManager.shared?.setChatroomAttributes(attributes: ["ranking_list":self.roomInfo?.room?.ranking_list?.kj.JSONString() ?? ""], completion: { error in
                        if error != nil {
                            self.view.makeToast("update ranking_list failed!\(error?.errorDescription ?? "")")
                        } else {
                            self.requestRankList()
                        }
                    })
                    var giftList: SAGiftView? = self.view.viewWithTag(1111) as? SAGiftView
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
        let contributes = SAUserView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 420), controllers: [SAGiftersViewController(roomId: roomInfo?.room?.room_id ?? "")], titles: [sceneLocalized( "Contribution List")], position: .left).cornerRadius(20, [.topLeft, .topRight], .white, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 420)), custom: contributes)
        sa_presentViewController(vc)
    }

    func showApplyAlert(_ index: Int) {
        let isHairScreen = SwiftyFitsize.isFullScreen
        let manageView = SAVMManagerView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: isHairScreen ? 264~ : 264~ - 34))
        let mic_info = SpatialAudioServiceImp.getSharedInstance().mics[safe: index]
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
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: isHairScreen ? 264~ : 264~ - 34)), custom: manageView)
        sa_presentViewController(vc)
    }

    func userApplyAlert(_ index: Int?) {
        if chatBar.handsState == .selected {
            view.makeToast("Request Wait".localized())
            return
        }

        if let mic_index = index {
            if let mic: SARoomMic = roomInfo?.mic_info?[mic_index] {
                if mic.status == 3 || mic.status == 4 {
                    view.makeToast("Mic Closed".localized())
                    return
                }
            }
        }

        let applyAlert = SAApplyAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (205 / 375.0) * ScreenWidth), content: "Request to Speak?", cancel: "Cancel", confirm: "Confirm", position: .bottom).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)), custom: applyAlert)
        applyAlert.actionEvents = { [weak self] in
            if $0 == 31 {
                self?.requestSpeak(index: index)
            }
            vc.dismiss(animated: true)
        }
        sa_presentViewController(vc)
    }

    func requestSpeak(index: Int?) {
        SpatialAudioServiceImp.getSharedInstance().startMicSeatApply(index: index) { error, flag in
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
        SpatialAudioServiceImp.getSharedInstance().cancelMicSeatApply(chat_uid: self.roomInfo?.room?.owner?.chat_uid ?? "") { error, flag in
            if error == nil {
                self.view.makeToast("Cancel apply success!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
            } else {
                self.view.makeToast("Cancel apply failed!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        }
    }

    func userCancelApplyAlert() {
        let cancelAlert = SACancelAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)), custom: cancelAlert)
        cancelAlert.actionEvents = { [weak self] in
            if $0 == 30 {
                self?.cancelRequestSpeak(index: nil)
            }
            vc.dismiss(animated: true)
        }
        sa_presentViewController(vc)
    }

    func showExitRoomView() {
        let confirmView = SAConfirmView(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 40~, height: 220~), type: .leave)
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 40~, height: 220~))
        compent.destination = .center
        let vc = SAAlertViewController(compent: compent, custom: confirmView)
        confirmView.resBlock = { [weak self] flag in
            self?.dismiss(animated: true)
            if flag {
                self?.didHeaderAction(with: .popBack, destroyed: false)
            }
        }
        sa_presentViewController(vc)
    }
    
    func giftList() -> SAGiftView {
        SAGiftView()
    }

    func startMessage() -> SAChatEntity {
        SAUserInfo.shared.currentRoomOwner = roomInfo?.room?.owner
        let entity = SAChatEntity()
        entity.userName = roomInfo?.room?.owner?.name
        entity.content = "Welcome to the voice chat room! Pornography, gambling or violence is strictly prohibited in the room.".localized()
        entity.attributeContent = entity.attributeContent
        entity.uid = roomInfo?.room?.owner?.uid
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }

    @objc func resignKeyboard() { }
}
