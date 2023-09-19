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
}

class DHCShowChoruserView: UIView {
    
    private var tableView: UITableView!
    private var countLabel: UILabel!
    private var dataSource: [ChorusShowModel] = [] {
        didSet {
            countLabel.text = "正在合唱用户(\(dataSource.count))"
            tableView.reloadData()
        }
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .blue
        countLabel = UILabel(frame: CGRect(x: 0, y: 20, width: 200, height: 20))
        addSubview(countLabel)
        
        tableView = UITableView(frame: CGRect(x: 0, y: 60, width: self.bounds.width, height: self.bounds.height - 60))
        tableView.dataSource = self
        tableView.delegate = self
        addSubview(tableView)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension DHCShowChoruserView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataSource.count
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
    }
}
