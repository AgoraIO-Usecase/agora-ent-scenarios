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
            tableView.registerCell(DHCScoreTitleCell.self, forCellReuseIdentifier: "title")
            tableView.registerCell(DHCScoreCell.self, forCellReuseIdentifier: "score")
            tableView.dataSource = self
            tableView.delegate = self
            tableView.backgroundColor = .clear
            return tableView
    }()
    
    @objc public var dataSource: [SubRankModel]? {
        didSet {
            
        }
    }
    
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

extension DHCResultView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (dataSource?.count ?? 3) + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.row == 0 {
            let cell : DHCScoreTitleCell = tableView.dequeueReusableCell(withIdentifier: "title") as! DHCScoreTitleCell
            return cell
        } else {
            let cell : DHCScoreCell = tableView.dequeueReusableCell(withIdentifier: "score") as! DHCScoreCell
            if let model: SubRankModel = dataSource?[indexPath.row - 1] {
                cell.score = model
            }
            return cell
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return indexPath.row == 0 ? 28 : 38
    }
}
