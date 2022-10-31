//
//  VoiceRoomViewController+ChatBar.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/10/11.
//

import Foundation
import SVGAPlayer

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
            guard let effect = self?.roomInfo?.room?.sound_effect else { return }
            self?.rtckit.playMusic(with: self?.getSceneType(effect) ?? .social)
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
        let userAlert = VoiceRoomUserView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 420), controllers: [VoiceRoomApplyUsersViewController(roomId: roomInfo?.room?.room_id ?? ""), VoiceRoomInviteUsersController(roomId: roomInfo?.room?.room_id ?? "", mic_index: nil)], titles: [LanguageManager.localValue(key: "Raised Hands"), LanguageManager.localValue(key: "Invite On-Stage")], position: position).cornerRadius(20, [.topLeft, .topRight], .white, 0)
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
        if let chatroom_id = roomInfo?.room?.chatroom_id, let uid = roomInfo?.room?.owner?.uid, let id = gift.gift_id, let name = gift.gift_name, let value = gift.gift_price, let count = gift.gift_count {
            notifyServerGiftInfo(id: id, count: count, uid: uid) {
                VoiceRoomIMManager.shared?.sendCustomMessage(roomId: chatroom_id, event: VoiceRoomGift, customExt: ["gift_id": id, "gift_name": name, "gift_price": value, "gift_count": count, "userName": VoiceRoomUserInfo.shared.user?.name ?? "", "portrait": VoiceRoomUserInfo.shared.user?.portrait ?? self.userAvatar], completion: { [weak self] message, error in
                    guard let self = self else { return }
                    if error == nil, message != nil {
                        var giftList: VoiceRoomGiftView? = self.view.viewWithTag(1111) as? VoiceRoomGiftView
                        if giftList == nil {
                            giftList = self.giftList()
                            self.view.addSubview(giftList!)
                        }
                        giftList?.gifts.append(gift)
                        giftList?.cellAnimation()
                        if let c = Int(count), let v = Int(value), var amount = VoiceRoomUserInfo.shared.user?.amount {
                            amount += c * v
                            VoiceRoomUserInfo.shared.user?.amount = amount
                        }
                    } else {
                        self.view.makeToast("Send failed \(error?.errorDescription ?? "")", point: self.toastPoint, title: nil, image: nil, completion: nil)
                    }
                })
            }
        }
    }

    func notifyServerGiftInfo(id: String, count: String, uid: String, completion: @escaping () -> Void) {
        if let roomId = roomInfo?.room?.room_id {
            VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .giftTo(roomId: roomId), params: ["gift_id": id, "num": Int(count) ?? 1, "to_uid": uid]) { dic, error in
                if let result = dic?["result"] as? Bool, error == nil, result {
                    debugPrint("result:\(result)")
                    self.requestRankList()
                    completion()
                } else {
                    self.view.makeToast("Send failed!", point: self.toastPoint, title: nil, image: nil, completion: nil)
                }
            }
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
        parser.parse(withNamed: "rocket", in: .main) { entitiy in
            player.videoItem = entitiy
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
            view.makeToast("you have no wheat slots!", point: view.center, title: nil, image: nil, completion: nil)
            return
        }
        if !isOwner, idx == 0 {
            view.makeToast("you have no wheat slots!", point: view.center, title: nil, image: nil, completion: nil)
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
        guard let mic_info = roomInfo?.mic_info?[index] else { return }
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
        guard let roomId = roomInfo?.room?.room_id else { return }
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .submitApply(roomId: roomId), params: index != nil ? ["mic_index": index ?? 2] : [:]) { dic, error in
            if error == nil, dic != nil, let result = dic?["result"] as? Bool {
                if result {
                    self.chatBar.refresh(event: .handsUp, state: .selected, asCreator: false)
                    self.view.makeToast("Apply success!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                } else {
                    self.view.makeToast("Apply failed!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                }
            } else {
//                self.view.makeToast("\(error?.localizedDescription ?? "")", point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        }
    }

    func cancelRequestSpeak(index: Int?) {
        guard let roomId = roomInfo?.room?.room_id else { return }
        VoiceRoomBusinessRequest.shared.sendDELETERequest(api: .cancelApply(roomId: roomId), params: [:]) { dic, error in
            if error == nil, dic != nil, let result = dic?["result"] as? Bool {
                if result {
                    self.view.makeToast("Cancel apply success!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                    self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
                } else {
                    self.view.makeToast("Cancel apply failed!".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                }
            } else {
//                self.view.makeToast("\(error?.localizedDescription ?? "")",point: self.toastPoint, title: nil, image: nil, completion: nil)
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
}
