//
//  DHCResultView.swift
//  Cantata
//
//  Created by CP on 2023/9/4.
//

import UIKit

class DHCResultView: UIView {
    
    private lazy var resultTitleLabel: UILabel = { //本轮评分
        let label = UILabel()
        label.text = "本轮总分"
        label.textAlignment = .center
        label.textColor = .white
        return label
     }()
    
    private lazy var totalScoreLabel: UILabel = { //本轮评分
        let label = UILabel()
        label.text = "10000"
        label.textAlignment = .center
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 35)
        return label
     }()
    
    private lazy var tableView: UITableView = { //分数的tableView
            let tableView = UITableView()
            tableView.registerCell(SBGScoreTitleCell.self, forCellReuseIdentifier: "title")
            tableView.registerCell(SBGScoreCell.self, forCellReuseIdentifier: "score")
            tableView.dataSource = self
            tableView.delegate = self
            tableView.backgroundColor = .clear
            return tableView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        
    }
}
