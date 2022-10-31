//
//  VoiceRoomGiftersController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit
import ZSwiftBaseLib
import KakaJSON

public class VoiceRoomGiftersViewController: UITableViewController {
    
    private var room_id = ""
    
    private var dataSource = VoiceRoomContributions()

    public override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.tableFooterView(UIView()).registerCell(VoiceRoomGifterCell.self, forCellReuseIdentifier: "VoiceRoomGifterCell").rowHeight(73).backgroundColor(.white).separatorInset(edge: UIEdgeInsets(top: 72, left: 15, bottom: 0, right: 15)).separatorColor(UIColor(0xF2F2F2)).showsVerticalScrollIndicator(false)
        self.tableView.refreshControl = UIRefreshControl()
        self.tableView.refreshControl?.addTarget(self, action: #selector(refresh), for: .valueChanged)
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.refresh()
    }
    
    @objc public convenience init(roomId: String) {
        self.init()
        self.room_id = roomId
    }

    // MARK: - Table view data source
    public override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    public override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.dataSource.ranking_list?.count ?? 0
    }

    
    public override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomGifterCell") as? VoiceRoomGifterCell
        if cell == nil {
            cell = VoiceRoomGifterCell(style: .default, reuseIdentifier: "VoiceRoomGifterCell")
        }
        cell?.refresh(item: self.dataSource.ranking_list?[safe: indexPath.row])
        cell?.index = indexPath.row
        // Configure the cell...
        cell?.selectionStyle = .none
        return cell ?? VoiceRoomGifterCell()
    }
    


}

extension VoiceRoomGiftersViewController {
    
    @objc func refresh() {
        self.fetchList()
    }
    
    @objc private func fetchList() {
        VoiceRoomBusinessRequest.shared.sendGETRequest(api: .fetchGiftContribute(roomId: self.room_id), params: [:], classType: VoiceRoomContributions.self) { contributions, error in
            self.tableView.refreshControl?.endRefreshing()
            if error == nil,contributions != nil,contributions?.ranking_list?.count ?? 0 > 0 {
                self.dataSource = contributions!
                self.tableView.reloadData()
            }
        }
    }
}


public class VoiceRoomContributions: NSObject,Convertible {
    
    var ranking_list: [VRUser]?
    
    required public override init() {
        
    }
    
    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}
