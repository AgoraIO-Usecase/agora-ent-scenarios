//
//  VoiceRoomGiftersController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import KakaJSON
import UIKit
import ZSwiftBaseLib

public class SAGiftersViewController: UITableViewController {
    private var room_id = ""

   // private var dataSource = VoiceRoomContributions()
    private var dataSource: [SAUser]?
    override public func viewDidLoad() {
        super.viewDidLoad()
        tableView.tableFooterView(UIView()).registerCell(SAGifterCell.self, forCellReuseIdentifier: "VoiceRoomGifterCell").rowHeight(73).backgroundColor(.white).separatorInset(edge: UIEdgeInsets(top: 72, left: 15, bottom: 0, right: 15)).separatorColor(UIColor(0xF2F2F2)).showsVerticalScrollIndicator(false)
        tableView.refreshControl = UIRefreshControl()
        tableView.refreshControl?.addTarget(self, action: #selector(refresh), for: .valueChanged)
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }

    @objc public convenience init(roomId: String) {
        self.init()
        room_id = roomId
    }

    // MARK: - Table view data source

    override public func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        //dataSource.ranking_list?.count ?? 0
        dataSource?.count ?? 0
    }

    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomGifterCell") as? SAGifterCell
        if cell == nil {
            cell = SAGifterCell(style: .default, reuseIdentifier: "VoiceRoomGifterCell")
        }
        cell?.refresh(item: dataSource?[safe: indexPath.row])
        cell?.index = indexPath.row
        // Configure the cell...
        cell?.selectionStyle = .none
        return cell ?? SAGifterCell()
    }
}

extension SAGiftersViewController {
    @objc func refresh() {
        fetchList()
    }

    @objc private func fetchList() {
        AppContext.saServiceImp().fetchGiftContribute {[weak self] error, users in
            guard let self = self else {return}
            if error == nil, users != nil {
                self.tableView.refreshControl?.endRefreshing()
                if users?.count ?? 0 > 0 {
                    self.dataSource = users
                    self.tableView.reloadData()
                }
            }
        }
    }
}

public class SAContributions: NSObject, Convertible {
    var ranking_list: [SAUser]?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}
