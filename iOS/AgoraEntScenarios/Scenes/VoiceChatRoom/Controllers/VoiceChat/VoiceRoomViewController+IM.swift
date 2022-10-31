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

// MARK: - VoiceRoomIMDelegate

extension VoiceRoomViewController: VoiceRoomIMDelegate {
    func memberLeave(roomId: String, userName: String) {
        let info = roomInfo
        let count: Int = info?.room?.member_count ?? 0
        info?.room?.member_count = count - 1
        roomInfo = info
    }

    func voiceRoomUpdateRobotVolume(roomId: String, volume: String) {
        roomInfo?.room?.robot_volume = UInt(volume)
    }

    func chatTokenDidExpire(code: AgoraChatErrorCode) {
        if code == .tokenExpire {
            reLogin()
        }
    }

    func chatTokenWillExpire(code: AgoraChatErrorCode) {
        if code == .tokeWillExpire {
            reLogin()
        }
    }

    func receiveTextMessage(roomId: String, message: AgoraChatMessage) {
        showMessage(message: message)
    }

    func receiveGift(roomId: String, meta: [String: String]?) {
        guard let dic = meta else { return }
        var giftList = view.viewWithTag(1111) as? VoiceRoomGiftView
        if giftList == nil {
            giftList = self.giftList()
            view.addSubview(giftList!)
        }
        giftList?.gifts.append(model(from: dic, VoiceRoomGiftEntity.self))
        giftList?.cellAnimation()
        if let id = meta?["gift_id"], id == "VoiceRoomGift9" {
            rocketAnimation()
        }
        requestRankList()
    }

    func receiveApplySite(roomId: String, meta: [String: String]?) {
        if VoiceRoomUserInfo.shared.user?.uid ?? "" != roomInfo?.room?.owner?.uid ?? "" {
            return
        }
        chatBar.refresh(event: .handsUp, state: .unSelected, asCreator: true)
    }

    func receiveInviteSite(roomId: String, meta: [String: String]?) {
        guard let map = meta?["user"] else { return }
        let user = model(from: map, VRUser.self)
        if VoiceRoomUserInfo.shared.user?.uid ?? "" != user?.uid ?? "" {
            return
        }
        showInviteMicAlert()
    }

    func refuseInvite(roomId: String, meta: [String: String]?) {
        let user = model(from: meta ?? [:], VRUser.self)
        if VoiceRoomUserInfo.shared.user?.uid ?? "" != user.uid ?? "" {
            return
        }
        chatBar.refresh(event: .handsUp, state: .selected, asCreator: true)
        view.makeToast("User \(user.name ?? "")" + "rejected Invitation".localized(), point: toastPoint, title: nil, image: nil, completion: nil)
    }

    func userJoinedRoom(roomId: String, username: String, ext: [String: Any]?) {
        // 更新用户人数
        if ext?.keys.contains("member_count") == true {
            if let count: String = ext?["member_count"] as? String {
                let info = roomInfo
                info?.room?.member_count = Int(count)
                roomInfo = info
            }
        } else if ext?.keys.contains("click_count") == true {
            if let count: String = ext?["click_count"] as? String {
                let info = roomInfo
                info?.room?.click_count = Int(count)
                roomInfo = info
            }
        }
        convertShowText(userName: username, content: LanguageManager.localValue(key: "Joined"), joined: true)
    }

    func announcementChanged(roomId: String, content: String) {
        view.makeToast("Voice room announcement changed!", point: toastPoint, title: nil, image: nil, completion: nil)
        guard let _ = roomInfo?.room else { return }
        roomInfo?.room!.announcement = content
    }

    func userBeKicked(roomId: String, reason: AgoraChatroomBeKickedReason) {
        VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
        VoiceRoomIMManager.shared?.delegate = nil
        var message = ""
        switch reason {
        case .beRemoved: message = "you are removed by owner!"
        case .destroyed: message = "VoiceRoom was destroyed!"
        case .offline: message = "you are offline!"
        @unknown default:
            break
        }
        view.makeToast(message, point: toastPoint, title: nil, image: nil, completion: nil)
        if reason == .destroyed || reason == .beRemoved || reason == .offline {
            var destroyed = false
            if reason == .destroyed {
                destroyed = true
                NotificationCenter.default.post(name: NSNotification.Name("refreshList"), object: nil)
            }
            didHeaderAction(with: .back, destroyed: destroyed)
        }
    }

    func roomAttributesDidUpdated(roomId: String, attributeMap: [String: String]?, from fromId: String) {
        updateMic(attributeMap, fromId: fromId)
    }

    func roomAttributesDidRemoved(roomId: String, attributes: [String]?, from fromId: String) {}

    private func updateMic(_ map: [String: String]?, fromId: String) {
        guard let mic_info = map else { return }
        let keys = mic_info.keys.map { $0 }
        for key in keys {
            let value: String = mic_info[key] ?? ""
            let mic_dic: [String: Any] = value.z.jsonToDictionary()
            let mic: VRRoomMic = model(from: mic_dic, type: VRRoomMic.self) as! VRRoomMic

            let status = mic.status
            let mic_index = mic.mic_index
            if !isOwner {
                refreshHandsUp(status: status)
                if mic_index == local_index && (status == -1 || status == 3 || status == 4) {
                    local_index = nil
                }
            }

            /**
             如果房主踢用户下麦
             */
            if let host: VRUser = roomInfo?.room?.owner {
                if host.uid == fromId && status == -1 {
                    view.makeToast("Removed Stage".localized())
                }
            }

            if status == 5 || status == -2 {
                // 刷新机器人
                roomInfo?.mic_info?[mic_index] = mic
                rtcView.updateAlien(mic.status)
            } else {
                // 刷新普通用户
                if mic.member != nil {
                    mic.member?.mic_index = mic_index
                }
                roomInfo?.mic_info?[mic_index] = mic
                rtcView.updateUser(mic)
                if let user = mic.member {
                    if let uid = user.uid {
                        let local_uid = VoiceRoomUserInfo.shared.user?.uid
                        if uid == local_uid {
                            local_index = mic_index
                            if !isOwner {
                                rtckit.setClientRole(role: status == 0 ? .owner : .audience)
                            }
                            // 如果当前是0的状态  就设置成主播
                            rtckit.muteLocalAudioStream(mute: status != 0)
                        }
                    }
                } else {
                    if local_index == nil || mic_index == local_index {
                        rtckit.setClientRole(role: .audience)
                        rtckit.muteLocalAudioStream(mute: true)
                    }
                }
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
        inputBar.endEditing(true)
        inputBar.inputField.text = ""
        guard let roomId = roomInfo?.room?.chatroom_id else { return }
        guard let userName = VoiceRoomUserInfo.shared.user?.name else { return }
        showMessage(message: AgoraChatMessage(conversationID: roomId, body: AgoraChatTextMessageBody(text: text), ext: ["userName": VoiceRoomUserInfo.shared.user?.name ?? ""]))
        VoiceRoomIMManager.shared?.sendMessage(roomId: roomId, text: text, ext: ["userName": userName]) { message, error in
            if error == nil, message != nil {
            } else {
                self.view.makeToast("\(error?.errorDescription ?? "")", point: self.toastPoint, title: nil, image: nil, completion: nil)
            }
        }
    }

    func reLogin() {
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .login(()), params: ["deviceId": UIDevice.current.deviceUUID, "portrait": VoiceRoomUserInfo.shared.user?.portrait ?? userAvatar, "name": VoiceRoomUserInfo.shared.user?.name ?? ""], classType: VRUser.self) { [weak self] user, error in
            if error == nil {
                VoiceRoomUserInfo.shared.user = user
                VoiceRoomBusinessRequest.shared.userToken = user?.authorization ?? ""
                AgoraChatClient.shared().renewToken(user?.im_token ?? "")
            } else {
                self?.view.makeToast("\(error?.localizedDescription ?? "")", point: self?.toastPoint ?? .zero, title: nil, image: nil, completion: nil)
            }
        }
    }

    func showMessage(message: AgoraChatMessage) {
        if let body = message.body as? AgoraChatTextMessageBody, let userName = message.ext?["userName"] as? String {
            convertShowText(userName: userName, content: body.text, joined: false)
        }
    }

    func convertShowText(userName: String, content: String, joined: Bool) {
        let dic = ["userName": userName, "content": content]
        chatView.messages?.append(chatView.getItem(dic: dic, join: joined))
        DispatchQueue.main.async {
            self.refreshChatView()
        }
    }

    @objc func refreshChatView() {
        chatView.chatView.reloadData()
        let row = (chatView.messages?.count ?? 0) - 1
        chatView.chatView.scrollToRow(at: IndexPath(row: row, section: 0), at: .bottom, animated: true)
    }

    func gifts() -> [VoiceRoomGiftEntity] {
        var gifts = [VoiceRoomGiftEntity]()
        for dic in giftMap {
            gifts.append(model(from: dic, VoiceRoomGiftEntity.self))
        }
        return gifts
    }
}
