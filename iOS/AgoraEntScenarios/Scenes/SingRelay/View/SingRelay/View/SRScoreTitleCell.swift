//
//  srScoreTitleCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/18.
//

import UIKit

class SRScoreTitleCell: UITableViewCell {

    private var indexLabel: UILabel!
    private var playerLabel: UILabel!
    private var srCountLabel: UILabel!
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
        indexLabel.text = getLocalizeString(with: "sr_rank_list")
        indexLabel.textAlignment = .center
        indexLabel.textColor = .white
        indexLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(indexLabel)
        
        playerLabel = UILabel()
        playerLabel.text = getLocalizeString(with: "sr_rank_player")
        playerLabel.textAlignment = .center
        playerLabel.textColor = .white
        playerLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(playerLabel)
        
        srCountLabel = UILabel()
        srCountLabel.text = getLocalizeString(with: "sr_success")
        srCountLabel.textAlignment = .center
        srCountLabel.textColor = .white
        srCountLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(srCountLabel)
        
        scoreLabel = UILabel()
        scoreLabel.text = getLocalizeString(with: "sr_total_score")
        scoreLabel.textAlignment = .center
        scoreLabel.textColor = .white
        scoreLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(scoreLabel)
        
        self.backgroundColor = .clear
    }
    
    private func getLocalizeString(with key: String) -> String {
        return Bundle.localizedString(key, bundleName: "SRResource")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        indexLabel.frame = CGRect(x: 0, y: 0, width: self.bounds.size.width / 5.0, height: 28)
        playerLabel.frame = CGRect(x: indexLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0 * 2, height: 28)
        srCountLabel.frame = CGRect(x: playerLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0, height: 28)
        scoreLabel.frame = CGRect(x: srCountLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0, height: 28)
    }
    
    
}
