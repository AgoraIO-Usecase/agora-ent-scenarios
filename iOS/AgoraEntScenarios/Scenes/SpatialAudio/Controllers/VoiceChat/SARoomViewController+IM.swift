//
//  VoiceRoomViewController+IM.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/10/11.
//

import AgoraChat
import Foundation
import KakaJSON
import UIKit
import ZSwiftBaseLib

// MARK: - ChatRoomServiceSubscribeDelegate
extension SARoomViewController: SpatialAudioServiceSubscribeDelegate {
    
    
    func chatTokenWillExpire() {
        AgoraChatClient.shared().renewToken(VLUserCenter.user.im_token)
    }
    
    func onReceiveGift(roomId: String, gift: SAGiftEntity) {
        var giftList = view.viewWithTag(1111) as? SAGiftView
        if giftList == nil {
            giftList = self.giftList()
            view.addSubview(giftList!)
        }
        giftList?.gifts.append(gift)
        giftList?.cellAnimation()
        if gift.gift_id ?? "" == "VoiceRoomGift9" {
            rocketAnimation()
        }
        if var gift_amount = self.roomInfo?.room?.gift_amount {
            gift_amount += Int(gift.gift_price ?? "1")!*Int(gift.gift_count ?? "1")!
            self.roomInfo?.room?.gift_amount = gift_amount
            if self.isOwner {
                SAIMManager.shared?.setChatroomAttributes(attributes: ["gift_amount":"\(gift_amount)"], completion: { error in })
            }
        }
        //刷新礼物贡献总数，头部
//        self.requestRankList()
    }
    
    func fetchGiftContribution() {
//        let seconds: [Double] = [0,1,2,3]
//        guard let refreshSeconds = seconds.randomElement() else { return }
//        Throttler.throttle(delay: .seconds(refreshSeconds)) {
//            self.requestRankList()
//        }
    }
    
    func onReceiveSeatRequest(roomId: String, applicant: SAApply) {
        self.chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: true)
    }
    
    func onReceiveSeatRequestRejected(roomId: String, chat_uid: String) {
        self.refreshApplicants(chat_uid: chat_uid)
    }
    
    /// Description 刷新申请人列表
    /// - Parameter chat_uid: 环信userName
    func refreshApplicants(chat_uid: String) {
        AppContext.saTmpServiceImp().micApplys = AppContext.saTmpServiceImp().micApplys.filter({
            ($0.member?.chat_uid ?? "") != chat_uid
        })
    }
    
    func onReceiveSeatInvitation(roomId: String, user: SAUser) {
        self.showInviteMicAlert()
    }
    
    func onReceiveCancelSeatInvitation(roomId: String, chat_uid: String) {
        AppContext.saTmpServiceImp().userList.first(where: { $0.chat_uid ?? "" == chat_uid
        })?.mic_index = -1
    }
    
    func onUserJoinedRoom(roomId: String, user: SAUser) {
        // 更新用户人数
        let info = roomInfo
        info?.room?.member_count = (info?.room?.member_count ?? 0) + 1
        info?.room?.click_count = (info?.room?.click_count ?? 0) + 1
        headerView.updateHeader(with: info?.room)
        self.roomInfo?.room?.member_list?.append(user)
        AppContext.saTmpServiceImp().userList = self.roomInfo?.room?.member_list ?? []
        self.convertShowText(userName: user.name ?? "", content: "Joined".localized(), joined: true)
    }
    
    func onAnnouncementChanged(roomId: String, content: String) {
        self.view.makeToast("Voice room announcement changed!", point: toastPoint, title: nil, image: nil, completion: nil)
        self.roomInfo?.room?.announcement = content
    }
    
    func onRobotVolumeUpdated(roomId: String, volume: String) {
        roomInfo?.room?.robot_volume = UInt(volume)
    }
    
    func onUserBeKicked(roomId: String, reason: SAServiceKickedReason) {
        AppContext.saServiceImp().unsubscribeEvent()
        var message = ""
        switch reason {
        case .removed: message = "you are removed by owner!"
        case .destroyed: message = "VoiceRoom was destroyed!"
        case .offLined: message = "you are offline!"
        @unknown default:
            break
        }
        self.view.makeToast(message, point: toastPoint, title: nil, image: nil, completion: nil)
        var destroyed = false
        if reason == .destroyed {
            destroyed = true
            NotificationCenter.default.post(name: NSNotification.Name("refreshList"), object: nil)
        }
        AppContext.saServiceImp().leaveRoom(roomId) { _, _ in }
        self.didHeaderAction(with: .back, destroyed: destroyed)
    }
    
    func onSeatUpdated(roomId: String, mics: [SARoomMic], from fromId: String) {
        self.updateMic(mics, fromId: fromId)
    }
    
    func onRobotSwitch(roomId: String, enable: Bool, from fromId: String) {
        guard let mic: SARoomMic = roomInfo?.mic_info![6] else { return }
        let mic_info = mic
        mic_info.status = enable ? 5 : -2
        self.roomInfo?.room?.use_robot = enable
        self.roomInfo?.mic_info![6] = mic_info
        //机器人开关涉及到两个同时开关
        
        let blue_micInfo = mic_info
        let red_micInfo = mic_info
        
        //更新红蓝机器人的信息
        let blue: SAUser = SAUser()
        blue.portrait = "blue"
        blue.name = "Agora Blue"
        blue_micInfo.member = blue
        self.sRtcView.updateUser(blue_micInfo)
        
        let red: SAUser = SAUser()
        red.portrait = "blue"
        red.name = "Agora Blue"
        red_micInfo.member = blue
        self.sRtcView.updateUser(red_micInfo)

    }
    
    func onRobotVolumeChanged(roomId: String, volume: UInt, from fromId: String) {
        roomInfo?.room?.robot_volume = volume
    }
    
    func onContributionListChanged(roomId: String, ranking_list: [SAUser], from fromId: String) {
        self.roomInfo?.room?.ranking_list = ranking_list
        self.headerView.updateHeader(with: self.roomInfo?.room)
    }
    
    func onUserLeftRoom(roomId: String, userName: String) {
        let info = roomInfo
        let count: Int = info?.room?.member_count ?? 0
        info?.room?.member_count = count - 1
        headerView.updateHeader(with: info?.room)
        if let micInfos = info?.mic_info {
            for mic in micInfos {
                if let user: SAUser = mic.member {
                    if user.rtc_uid == userName {
                        let memeber = mic
                        memeber.member = nil
                        memeber.status = -1
                        sRtcView.updateUser(memeber)
                        break
                    }
                }
            }
        }
        self.roomInfo?.room?.member_list = self.roomInfo?.room?.member_list?.filter({
            $0.chat_uid != userName
        })
        self.refreshApplicants(chat_uid: userName)
        AppContext.saTmpServiceImp().userList = self.roomInfo?.room?.member_list ?? []
        if isOwner {
            AppContext.saServiceImp().updateRoomMembers { error in
                if error != nil {
                    self.view.makeToast("\(error?.localizedDescription ?? "")")
                }
            }
        }
    }
    
    func receiveTextMessage(roomId: String, message: SAChatEntity) {
       
    }

    private func updateMic(_ mics: [SARoomMic], fromId: String) {
        for mic in mics {
            AppContext.saTmpServiceImp().mics[mic.mic_index] = mic
        }
        //如果有两个mic对象来自同一个人证明是换麦 否则是上下麦或者被禁用静音等
        if mics.count == 2,let first = mics.first,let last = mics.last {
            //TODO: remove as!
            AppContext.saTmpServiceImp().mics[first.mic_index] = first
            AppContext.saTmpServiceImp().mics[last.mic_index] = last
            roomInfo?.mic_info = AppContext.saTmpServiceImp().mics
            sRtcView.updateUser(first)
            sRtcView.updateUser(last)
        } else {
            if let first = mics.first {
                let status = first.status
                let mic_index = first.mic_index
                //刷新底部✋🏻状态
                if !isOwner {
                    refreshHandsUp(status: status)
                }
                //                if fromId != self.roomInfo?.room?.owner?.chat_uid ?? "",!isOwner {
                //                    refreshHandsUp(status: status)
                //                }
                //将userList中的上麦用户做标记，便于后续过滤
                var micUser = AppContext.saTmpServiceImp().userList.first(where: {
                    $0.chat_uid ?? "" == fromId
                })
                if status == -1 {
                    micUser?.mic_index = -1
                } else {
                    micUser = AppContext.saTmpServiceImp().userList.first(where: {
                        $0.chat_uid ?? "" == first.member?.chat_uid ?? ""
                    })
                    if micUser != nil {
                        micUser?.mic_index = mic_index
                    }
                }
                if !isOwner {
                    var state: SAChatBarState = .selected
                    if status == 0 || status != -1 {
                        state = .unSelected
                    }
                    self.chatBar.refresh(event: .mic, state: state, asCreator: isOwner)
                    if mic_index == local_index && (status == -1 || status == 3 || status == 4 || status == 2) {
                        local_index = nil
                    }
                }
                /**
                 如果房主踢用户下麦
                 */
                if let host: SAUser = roomInfo?.room?.owner {
                    if host.uid == fromId && status == -1 {
                        AppContext.saTmpServiceImp().userList.first(where: { $0.chat_uid ?? "" == fromId })?.mic_index = -1
                        view.makeToast("Removed Stage".localized())
                    }  else {
                        self.refreshApplicants(chat_uid: fromId)
                    }
                } else {
                    self.refreshApplicants(chat_uid: fromId)
                }

                // 刷新普通用户
                if first.member != nil {
                    first.member?.mic_index = mic_index
                }

                if let _ = first.member {
                    let local_uid: String = SAUserInfo.shared.user?.chat_uid ?? ""
                    let cp_uid: String = first.member?.chat_uid ?? ""
                    if local_uid == cp_uid {
                        local_index = mic_index
                        if !isOwner {
                            self.rtckit.setClientRole(role: status == 0 ? .owner : .audience)
                        }
                        //如果当前是0的状态  就设置成主播
                        self.rtckit.muteLocalAudioStream(mute: status != 0)
                    }
                } else {
                    if local_index == nil || mic_index == local_index {
                        rtckit.setClientRole(role: .audience)
                        rtckit.muteLocalAudioStream(mute: true)
                    }
                }
                
                AppContext.saTmpServiceImp().mics[first.mic_index] = first
                roomInfo?.mic_info = AppContext.saTmpServiceImp().mics
                sRtcView.updateUser(first)
                refreshApplicants(chat_uid: fromId)
            }
        }
    }

    func refreshHandsUp(status: Int) {
        if status == -1 {
            chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: false)
        } else {
            chatBar.refresh(event: .handsUp, state: .disable, asCreator: false)
        }
    }

    func sendTextMessage(text: String) {
        guard let roomId = roomInfo?.room?.chatroom_id else { return }
        guard let userName = SAUserInfo.shared.user?.name else { return }
        showMessage(message: AgoraChatMessage(conversationID: roomId,
                                              body: AgoraChatTextMessageBody(text: text),
                                              ext: ["userName": SAUserInfo.shared.user?.name ?? ""]))
        SAIMManager.shared?.sendMessage(roomId: roomId, text: text, ext: ["userName": userName]) { message, error in
            if error != nil,error?.code == .moderationFailed {
                self.view.makeToast("Content prohibited".localized(), point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        }
    }

    func showMessage(message: AgoraChatMessage) {
        if let body = message.body as? AgoraChatTextMessageBody, let userName = message.ext?["userName"] as? String {
            convertShowText(userName: userName, content: body.text, joined: false)
        }
    }

    func convertShowText(userName: String, content: String, joined: Bool) {
       
    }

    @objc func refreshChatView() {
        
    }

    func gifts() -> [SAGiftEntity] {
        var gifts = [SAGiftEntity]()
        for dic in sa_giftMap {
            gifts.append(model(from: dic, SAGiftEntity.self))
        }
        return gifts
    }
}
