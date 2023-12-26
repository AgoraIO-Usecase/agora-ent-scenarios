//
//  SBGScoreTitleCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/18.
//

import UIKit

class SBGScoreTitleCell: UITableViewCell {

    private var indexLabel: UILabel!
    private var playerLabel: UILabel!
    private var sbgCountLabel: UILabel!
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
        indexLabel.text = getLocalizeString(with: "sbg_rank_list")
        indexLabel.textAlignment = .center
        indexLabel.textColor = .white
        indexLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(indexLabel)
        
        playerLabel = UILabel()
        playerLabel.text = getLocalizeString(with: "sbg_rank_player")
        playerLabel.textAlignment = .center
        playerLabel.textColor = .white
        playerLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(playerLabel)
        
        sbgCountLabel = UILabel()
        sbgCountLabel.text = getLocalizeString(with: "sbg_success")
        sbgCountLabel.textAlignment = .center
        sbgCountLabel.textColor = .white
        sbgCountLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(sbgCountLabel)
        
        scoreLabel = UILabel()
        scoreLabel.text = getLocalizeString(with: "sbg_total_score")
        scoreLabel.textAlignment = .center
        scoreLabel.textColor = .white
        scoreLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(scoreLabel)
        
        self.backgroundColor = .clear
    }
    
    private func getLocalizeString(with key: String) -> String {
        return Bundle.localizedString(key, bundleName: "sbgResource")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        indexLabel.frame = CGRect(x: 0, y: 0, width: self.bounds.size.width / 5.0, height: 28)
        playerLabel.frame = CGRect(x: indexLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0 * 2, height: 28)
        sbgCountLabel.frame = CGRect(x: playerLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0, height: 28)
        scoreLabel.frame = CGRect(x: sbgCountLabel.frame.maxX, y: 0, width: self.bounds.size.width / 5.0, height: 28)
    }
    
    
}
