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

    lazy var empty: VREmptyView = .init(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 360), title: "No audience yet", image: nil).backgroundColor(.white)

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

    override public func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        if apply?.cursor != nil, (apply?.members?.count ?? 0) - 2 == indexPath.row, (apply?.total ?? 0) >= (apply?.members?.count ?? 0) {
            fetchUsers()
        }
    }
}

extension VoiceRoomInviteUsersController {
    @objc func refresh() {
        apply?.members?.removeAll()
        apply = nil
        fetchUsers()
    }

    @objc private func fetchUsers() {
        VoiceRoomBusinessRequest.shared.sendGETRequest(api: .fetchRoomMembers(roomId: roomId ?? "", cursor: apply?.cursor ?? "", pageSize: 15), params: [:], classType: VoiceRoomAudiencesEntity.self) { model, error in
            self.tableView.refreshControl?.endRefreshing()
            if model != nil, error == nil {
                if self.apply == nil {
                    self.apply = model
                } else {
                    self.apply?.cursor = model?.cursor
                    self.apply?.members?.append(contentsOf: model?.members ?? [])
                }
                self.tableView.reloadData()
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
            self.empty.isHidden = (self.apply?.members?.count ?? 0 != 0)
        }
    }

    private func inviteUser(user: VRUser?) {
        SVProgressHUD.show()
        var params: [String: Any] = ["uid": user?.uid ?? ""]
        if idx > 0 {
            params = ["uid": user?.uid ?? "", "mic_index": idx]
        }
        VoiceRoomBusinessRequest.shared.sendPOSTRequest(api: .inviteUserToMic(roomId: roomId ?? ""), params: params) { dic, error in
            SVProgressHUD.dismiss()
            if dic != nil, error == nil, let result = dic?["result"] as? Bool {
                if result {
                    self.view.makeToast("Invitation sent!".localized())
                } else {
                    self.view.makeToast("Invited failed!".localized())
                }
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
    }
}
