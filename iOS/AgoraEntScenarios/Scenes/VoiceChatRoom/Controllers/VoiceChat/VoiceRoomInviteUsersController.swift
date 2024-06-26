//
//  VoiceRoomInviteUsersController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/11.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib

public class VoiceRoomInviteUsersController: UITableViewController {
    private var apply: VoiceRoomAudiencesEntity?

    private var roomId: String?

    private var idx = 0

    lazy var empty: VREmptyView = .init(frame: CGRect(x: 0, y: 84, width: ScreenWidth, height: 360), title: "voice_no_audience_yet", image: nil).backgroundColor(.white)

    public convenience init(roomId: String, mic_index: Int?) {
        self.init()
        if mic_index != nil {
            idx = mic_index ?? 0
        }
        self.roomId = roomId
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        view.insertSubview(empty, belowSubview: tableView)
        tableView.tableFooterView(UIView()).registerCell(VoiceRoomInviteCell.self, forCellReuseIdentifier: "VoiceRoomInviteCell").rowHeight(73).backgroundColor(.white).separatorInset(edge: UIEdgeInsets(top: 72, left: 15, bottom: 0, right: 15)).separatorColor(UIColor(0xF2F2F2)).showsVerticalScrollIndicator(false).backgroundColor(.clear)
        tableView.refreshControl = UIRefreshControl()
        tableView.refreshControl?.addTarget(self, action: #selector(refresh), for: .valueChanged)
    }

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }

    // MARK: - Table view data source

    override public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        apply?.members?.count ?? 0
    }

    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomInviteCell", for: indexPath) as? VoiceRoomInviteCell
        if cell == nil {
            cell = VoiceRoomInviteCell(style: .default, reuseIdentifier: "VoiceRoomInviteCell")
        }
        // Configure the cell...
        cell?.selectionStyle = .none
        cell?.refresh(item: apply?.members?[safe: indexPath.row])
        cell?.inviteClosure = { [weak self] in
            self?.inviteUser(user: $0)
            self?.apply?.members?[safe: indexPath.row]?.invited = true
            self?.tableView.reloadData()
        }
        return cell ?? VoiceRoomInviteCell()
    }

//    override public func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
//        if apply?.cursor != nil, (apply?.members?.count ?? 0) - 2 == indexPath.row, (apply?.total ?? 0) >= (apply?.members?.count ?? 0) {
//            fetchUsers()
//        }
//    }
}

extension VoiceRoomInviteUsersController {
    @objc func refresh() {
        apply?.members?.removeAll()
        apply = nil
        fetchUsers()
    }

    @objc private func fetchUsers() {
        ChatRoomServiceImp.getSharedInstance().fetchRoomMembers { error, users in
            self.tableView.refreshControl?.endRefreshing()
            if users != nil, error == nil {
                if self.apply == nil {
                    let model: VoiceRoomAudiencesEntity = VoiceRoomAudiencesEntity()
                    model.members = users?.filter({
                        $0.mic_index == -1
                    })
                    self.apply = model
                } else {
                    self.apply?.members?.append(contentsOf: users ?? [])
                }
                self.empty.isHidden = (self.apply?.members?.count ?? 0) > 0
                self.tableView.reloadData()
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
        
    }

    private func inviteUser(user: VRUser?) {
        SVProgressHUD.show()
        let chat_uid: String = user?.chat_uid ?? ""
        ChatRoomServiceImp.getSharedInstance().startMicSeatInvitation(chatUid: chat_uid, index: idx < 0 ? nil:idx) { error, flag in
            SVProgressHUD.dismiss()
            self.view.makeToast(flag == true ? "voice_invitation_sent".voice_localized() : "voice_invited_failed".voice_localized())
        }

    }
}
