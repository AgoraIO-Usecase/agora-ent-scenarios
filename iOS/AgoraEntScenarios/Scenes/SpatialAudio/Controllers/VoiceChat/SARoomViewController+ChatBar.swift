//
//  VoiceRoomViewController+ChatBar.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/10/11.
//

import Foundation
import SVGAPlayer
import KakaJSON
//import ZSwiftBaseLib

extension SARoomViewController {
    
    func showEQView() {
        let isOpenSpatial = roomInfo?.robotInfo.use_robot == true
        let volumn = Double((roomInfo?.robotInfo.robot_volume ?? 45)) / 100.0

        let actionView = ActionSheetManager()
        actionView
            .section(section: 2)
            .rows(rows: [2, 1])
            .title(title: "Audio Settings".spatial_localized())
            .sectionHeader(title: "Bot Settings".spatial_localized(), desc: "Host Only".spatial_localized())
            .switchCell(iconName: "icons／set／jiqi", title: "Agora Blue&Agora Red".spatial_localized(), isOn: isOpenSpatial, isEnabel: isOwner)
            .sliderCell(iconName: "icons／set／laba", title: "Robot volume".spatial_localized(), value: volumn, isEnable: isOwner)
            .sectionHeader(iconName: "new", title: "Spatial Audio".spatial_localized(), desc: nil)
            .textCell(iconName: "icons／set／3D", title: "Spatial Audio".spatial_localized(), desc: nil, isShowArrow: true)
            .config()
        actionView.didCellItemClosure = { [weak self] indexPath in
            guard indexPath.section == 1 && indexPath.row == 0 else { return }
            self?.showSpatialAudioView()
        }
        actionView.didSwitchValueChangeClosure = { [weak self] _, isOn in
            guard let self = self else { return }
            self.roomInfo?.robotInfo.use_robot = isOn
            self.activeAlien(isOn)
        }
        actionView.didSliderValueChangeClosure = { [weak self] _, value in
            self?.rtckit.updatePlayerVolume(value: Double(value * 100))
            self?.updateVolume(Int(value * 100.0))
        }
        actionView.show()
    }
    
    func showSpatialAudioView() {
        guard let micInfos = sRtcView.micInfos else { return }
        let robotInfo = self.roomInfo?.robotInfo ?? SARobotAudioInfo()
        
        let red = micInfos[6]
        let blue = micInfos[3]
        
        let actionView = ActionSheetManager()
        actionView
            .section(section: 2)
            .rows(rows: [2, 2])
            .title(title: "Spatial Audio".spatial_localized())
            .sectionHeader(iconName: "new", title: "Agora Blue Bot".spatial_localized(), desc: "Host Only".spatial_localized())
            .sliderCell(title: "Attenuation factor volume".spatial_localized(), value: robotInfo.blue_robot_attenuation, isEnable: isOwner)
            .switchCell(title: "Air Absorb".spatial_localized(), isOn: robotInfo.blue_robot_absorb, isEnabel: isOwner)
//            .switchCell(title: "Voice Blur".localized_spatial(), isOn: robotInfo.blue_robot_blur, isEnabel: isOwner)
            .sectionHeader(iconName: "new", title: "Agora Red Bot".spatial_localized(), desc: "Host Only".spatial_localized())
            .sliderCell(title: "Attenuation factor volume".spatial_localized(), value: robotInfo.red_robot_attenuation, isEnable: isOwner)
            .switchCell(title: "Air Absorb".spatial_localized(), isOn: robotInfo.red_robot_absorb, isEnabel: isOwner)
//            .switchCell(title: "Voice Blur".localized_spatial(), isOn: robotInfo.red_robot_blur, isEnabel: isOwner)
            .config()
        actionView.didSwitchValueChangeClosure = { [weak self] indexPath, isOn in
            guard let self = self else { return }
            let robotInfo = self.roomInfo?.robotInfo ?? SARobotAudioInfo()
            if indexPath.section == 0 {
                switch indexPath.row {
                case 1:
                    blue.airAbsorb = isOn
                    robotInfo.blue_robot_absorb = isOn
                case 2:
                    blue.voiceBlur = isOn
                    robotInfo.blue_robot_blur = isOn
                    
                default: break
                }
            } else {
                switch indexPath.row {
                case 1:
                    red.airAbsorb = isOn
                    robotInfo.red_robot_absorb = isOn
                    
                case 2:
                    red.voiceBlur = isOn
                    robotInfo.red_robot_blur = isOn
                    
                default: break
                }
            }
            self.sRtcView.micInfos?[3] = blue
            self.sRtcView.micInfos?[6] = red
            AppContext.saServiceImp().updateRobotInfo(info: robotInfo) { error in }
        }
        actionView.didSliderValueChangeClosure = { [weak self] indexPath, value in
            guard let self = self, let micInfos = self.sRtcView.micInfos else { return }
            let robotInfo = self.roomInfo?.robotInfo ?? SARobotAudioInfo()
            let red = micInfos[6]
            let blue = micInfos[3]
            if indexPath.section == 0 {
                blue.attenuation = value
                robotInfo.blue_robot_attenuation = value
                
            } else {
                red.attenuation = value
                robotInfo.red_robot_attenuation = value
            }
            self.sRtcView.micInfos?[3] = blue
            self.sRtcView.micInfos?[6] = red
            AppContext.saServiceImp().updateRobotInfo(info: robotInfo) { error in }
        }
        actionView.show()
    }
    
    func applyMembersAlert(position: SASwitchBarDirection, index: Int) {
        let apply = SAApplyUsersViewController(roomId: roomInfo?.room?.room_id ?? "")
        //apply.agreeApply = {
           // self.rtcView.updateUser($0)
        //}
        let invite = SAInviteUsersController(roomId: roomInfo?.room?.room_id ?? "", mic_index:index)
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
                        SAUserInfo.shared.user?.chat_uid = VLUserCenter.user.id
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
            applyMembersAlert(position: .left, index: -1)
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
        if chatBar.micState == true {
            AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self)
        }
        guard let idx = local_index else {
            view.makeToast("you have no wheat slots!".spatial_localized(), point: view.center, title: nil, image: nil, completion: nil)
            return
        }
        if !isOwner, idx == 0 {
            view.makeToast("you have no wheat slots!".spatial_localized(), point: view.center, title: nil, image: nil, completion: nil)
            return
        }
        if let mic = roomInfo?.mic_info?[idx], mic.status == 2 && isOwner == false  {
            view.makeToast("Banned".spatial_localized())
            return
        }
        chatBar.micState = !chatBar.micState
       // chatBar.refresh(event: .mic, state: chatBar.micState ? .selected : .unSelected, asCreator: false)
        // 需要根据麦位特殊处理
        chatBar.micState == true ? muteLocal(with: idx) : unmuteLocal(with: idx)
        if let index = AppContext.saTmpServiceImp().mics.firstIndex(where: { $0.member?.uid == VLUserCenter.user.id }) {
            let model = AppContext.saTmpServiceImp().mics[index]
            model.status = chatBar.micState == true ? 1 : 0
            AppContext.saTmpServiceImp().mics[index] = model
        }
        
        rtckit.muteLocalAudioStream(mute: chatBar.micState)
    }

    func showUsers() {
        let contributes = SAUserView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 420), controllers: [SAGiftersViewController(roomId: roomInfo?.room?.room_id ?? "")], titles: [sceneLocalized( "Contribution List")], position: .left).cornerRadius(20, [.topLeft, .topRight], .white, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 420)), custom: contributes)
        sa_presentViewController(vc)
    }

    func showApplyAlert(_ index: Int) {
        let isHairScreen =  Screen.isFullScreen
        let manageView = SAVMManagerView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: isHairScreen ? 264 : 264 - 34))
        //TODO: remove as!
        let mic_info = AppContext.saTmpServiceImp().mics[safe: index]
        manageView.micInfo = mic_info
        manageView.resBlock = { [weak self] state, flag in
            self?.dismiss(animated: true)
            if state == .invite {
                if flag {
                    self?.applyMembersAlert(position: .right, index: index)
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
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: isHairScreen ? 264 : 264 - 34)), custom: manageView)
        sa_presentViewController(vc)
    }

    func userApplyAlert(_ index: Int?) {
        if chatBar.handsState == .selected {
            view.makeToast("Request Wait".spatial_localized())
            return
        }

        if let mic_index = index {
            if let mic: SARoomMic = roomInfo?.mic_info?[mic_index] {
                if mic.status == 3 || mic.status == 4 {
                    view.makeToast("Mic Closed".spatial_localized())
                    return
                }
            }
        }

        let applyAlert = SAApplyAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (205 / 375.0) * ScreenWidth), content: "Request to Speak?", cancel: "Cancel", confirm: "Confirm", position: .bottom).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .clear, 0)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (205 / 375.0) * ScreenWidth)), custom: applyAlert)
        applyAlert.actionEvents = { [weak self] in
            guard let self = self else { return }
            if $0 == 31 {
                AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self) { granted in
                    guard granted else { return }
                    self.requestSpeak(index: index)
                }
            }
            vc.dismiss(animated: true)
        }
        sa_presentViewController(vc)
    }

    func requestSpeak(index: Int?) {
        AppContext.saServiceImp().startMicSeatApply(index: index) {[weak self] error, flag in
            guard let self = self else {return}
            if error == nil {
                if flag {
                    self.chatBar.refresh(event: .handsUp, state: .selected, asCreator: false)
                    self.view.makeToast("Apply success!".spatial_localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                } else {
                    self.view.makeToast("Apply failed!".spatial_localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                }
            } else {
                
            }
        }
    }

    func cancelRequestSpeak(index: Int?) {
        AppContext.saServiceImp().cancelMicSeatApply(chat_uid: VLUserCenter.user.id) {[weak self] error, flag in
            guard let self = self else {return}
            if error == nil {
                self.view.makeToast("Cancel apply success!".spatial_localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
                self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
            } else {
                self.view.makeToast("Cancel apply failed!".spatial_localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
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
        let confirmView = SAConfirmView(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 40, height: 220), type: .leave)
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 40, height: 220))
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
        entity.content = "Welcome to the voice chat room! Pornography, gambling or violence is strictly prohibited in the room.".spatial_localized()
        entity.attributeContent = entity.attributeContent
        entity.uid = roomInfo?.room?.owner?.uid
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }

    @objc func resignKeyboard() { }
}
