//
//  DHCScoreTitleCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/18.
//

import UIKit

class DHCScoreTitleCell: UITableViewCell {

    private var indexLabel: UILabel!
    private var playerLabel: UILabel!
    private var scoreLabel: UILabel!
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        indexLabel = UILabel()
        indexLabel.text = "ktv_rank_list".toSceneLocalization() as String
        indexLabel.textAlignment = .center
        indexLabel.textColor = .white
        indexLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(indexLabel)
        
        playerLabel = UILabel()
        playerLabel.text = "ktv_rank_player".toSceneLocalization() as String
        playerLabel.textAlignment = .center
        playerLabel.textColor = .white
        playerLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(playerLabel)
        
        scoreLabel = UILabel()
        scoreLabel.text = "ktv_per_score".toSceneLocalization() as String
        scoreLabel.textAlignment = .center
        scoreLabel.textColor = .white
        scoreLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(scoreLabel)
        
        self.backgroundColor = .clear
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        indexLabel.frame = CGRect(x: 0, y: 0, width: self.bounds.size.width / 5.0, height: 28)
        playerLabel.frame = CGRect(x: indexLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0 * 2, height: 28)
        scoreLabel.frame = CGRect(x: self.bounds.size.width * 4.0 / 5.0, y: 0, width: self.bounds.size.width / 5.0, height: 28)
    }
    
    
}
