//
//  DHCShowChoruserView.swift
//  Cantata
//
//  Created by CP on 2023/9/19.
//

import UIKit

public struct ChorusShowModel {
    var headIcon: String
    var name: String
    var num: Int
    var isMaster: Bool
    var level: Int
    var userNo: String
    var isRoomOwner: Bool
}

class DHCShowChoruserView: UIViewController {
    
    private var tableView: UITableView!
    private var countLabel: UILabel!
    public var dataSource: [ChorusShowModel] = []
    var leaveBlock:((String)->Void)?
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor(red: 21/255.0, green: 32/255.0, blue: 100/255.0, alpha: 1)
        countLabel = UILabel(frame: CGRect(x: (ScreenWidth - 100)/2.0, y: 20, width: 200, height: 20))
        countLabel.textColor = .white
        view.addSubview(countLabel)
        
        tableView = UITableView(frame: CGRect(x: 0, y: 60, width: self.view.bounds.width, height: self.view.bounds.height - 60))
        tableView.dataSource = self
        tableView.delegate = self
        tableView.register(UINib(nibName: "DHCShowChorusCell", bundle: nil), forCellReuseIdentifier: "ShowCourse")
        view.addSubview(tableView)
        tableView.backgroundColor = .clear
        
        countLabel.text = "正在合唱用户(\(dataSource.count))"
    }
    
}

extension DHCShowChoruserView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataSource.count
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 80
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: DHCShowChorusCell = tableView.dequeueReusableCell(withIdentifier: "ShowCourse") as! DHCShowChorusCell
        let data = dataSource[indexPath.row]
        cell.setModel(with: data)
        cell.leaveBlock = {[weak self] userNo in
            guard let leaveBlock = self?.leaveBlock else {return}
            leaveBlock(userNo)
        }
        return cell
    }
}
