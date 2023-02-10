//
//  VoiceRoomApplyUsersViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/11.
//

import SVProgressHUD
import UIKit
import ZSwiftBaseLib

public class SAApplyUsersViewController: UITableViewController {
    
    private var apply: SAApplyEntity?

    private var roomId: String?
    
    var agreeApply:((SARoomMic) -> Void)?
    
    lazy var empty: SAEmptyView = .init(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 360), title: "No one raised hands yet", image: nil).backgroundColor(.white)

    public convenience init(roomId: String) {
        self.init()
        self.roomId = roomId
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        view.insertSubview(empty, belowSubview: tableView)
        tableView.tableFooterView(UIView()).registerCell(SAApplyCell.self, forCellReuseIdentifier: "VoiceRoomApplyCell").rowHeight(73).backgroundColor(.white).separatorInset(edge: UIEdgeInsets(top: 72, left: 15, bottom: 0, right: 15)).separatorColor(UIColor(0xF2F2F2)).showsVerticalScrollIndicator(false).backgroundColor(.clear)
        tableView.refreshControl = UIRefreshControl()
        tableView.refreshControl?.addTarget(self, action: #selector(refresh), for: .valueChanged)
    }

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }

    // MARK: - Table view data source

    override public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        AppContext.saTmpServiceImp().micApplys.count
    }

    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomApplyCell", for: indexPath) as? SAApplyCell
        if cell == nil {
            cell = SAApplyCell(style: .default, reuseIdentifier: "VoiceRoomApplyCell")
        }
        // Configure the cell...
        cell?.selectionStyle = .none
        //TODO: remove as!
        cell?.refresh(item: AppContext.saTmpServiceImp().micApplys[safe: indexPath.row])
        cell?.agreeClosure = { [weak self] in
            self?.agreeUserApply(user: $0)
            AppContext.saTmpServiceImp().micApplys[safe: indexPath.row]?.member?.invited = true
            self?.tableView.reloadData()
        }
        return cell ?? SAApplyCell()
    }

//    override public func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
//        if apply?.cursor != nil, (apply?.apply_list?.count ?? 0) - 2 == indexPath.row, (apply?.total ?? 0) >= (apply?.apply_list?.count ?? 0) {
//            fetchUsers()
//        }
//    }
}

extension SAApplyUsersViewController {
    @objc func refresh() {
        AppContext.saServiceImp().fetchApplicantsList { error, applicants in
            self.refreshEnd()
            guard let datas = applicants else {
                return
            }
            self.empty.isHidden = datas.count > 0
        }
    }
    
    @objc func refreshEnd() {
        self.tableView.refreshControl?.endRefreshing()
        self.tableView.reloadData()
    }

    private func agreeUserApply(user: SAApply?) {
        SVProgressHUD.show()
        guard let user1 = user?.member else { return }
        AppContext.saServiceImp().acceptMicSeatApply(chatUid: user1.chat_uid ?? "", completion: { error,mic  in
            SVProgressHUD.dismiss()
            if self.agreeApply != nil,let mic = mic {
                self.agreeApply!(mic)
            }
            self.tableView.reloadData()
            let warningMessage = (error == nil ? "Agree success!".localized():"Agree failed!".localized())
            self.view.makeToast(warningMessage)
        })
    }
}
