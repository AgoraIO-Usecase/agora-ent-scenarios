//
//  VoiceRoomAudienceTableViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit
import ZSwiftBaseLib

public final class VoiceRoomAudiencesViewController: UITableViewController {
    var datas: [VRUser]?

    lazy var empty: VREmptyView = .init(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30), title: "No audience yet", image: nil)

    override public func viewDidLoad() {
        super.viewDidLoad()
        view.insertSubview(empty, belowSubview: tableView)
        tableView.tableFooterView(UIView()).registerCell(VoiceRoomAudienceCell.self, forCellReuseIdentifier: "VoiceRoomAudienceCell").rowHeight(73).backgroundColor(.white).showsVerticalScrollIndicator(false).separatorInset(edge: UIEdgeInsets(top: 72, left: 15, bottom: 0, right: 15)).separatorColor(UIColor(0xF2F2F2))
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }

    // MARK: - Table view data source

    override public func numberOfSections(in tableView: UITableView) -> Int {
        1
    }

    override public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        10
//        self.datas?.count ?? 0
    }

    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomGifterCell") as? VoiceRoomAudienceCell
        if cell == nil {
            cell = VoiceRoomAudienceCell(style: .default, reuseIdentifier: "VoiceRoomAudienceCell")
        }
        cell?.selectionStyle = .none
        return cell ?? VoiceRoomAudienceCell()
    }
}
