//
//  SBGScoreCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/18.
//

import UIKit

class SBGScoreCell: UITableViewCell {
    var bgImgView: UIImageView!
    var iconView: UIImageView!
    var indexLabel: UILabel!
    var headIcon: UIImageView!
    var nameLabel: UILabel!
    var sbgCountLabel: UILabel!
    var gradeLabel: UILabel!
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var score: SubRankModel = SubRankModel() {
        didSet {
            indexLabel.text = "\(score.index)"
            nameLabel.text = score.userName == "" ? "暂无上榜" : "\(score.userName)"
            sbgCountLabel.text = score.songNum == 0 ? "-" : "\(score.songNum)首"
            gradeLabel.text = score.songNum == 0 ? "-" : "\(score.score)分"
            if(score.index == 1){
                iconView.image = UIImage.sceneImage(name: "sbg-rank1")
            } else if score.index == 2 {
                iconView.image = UIImage.sceneImage(name: "sbg-rank2")
            } else if score.index == 3 {
                iconView.image = UIImage.sceneImage(name: "sbg-rank3")
            }
            headIcon.isHidden = score.poster == ""
            headIcon.sd_setImage(with: URL(string: score.poster ?? ""))
            
            var bgUrl = ""
            switch score.index {
            case 1:
                bgUrl = "sbg-bg-rank1"
            case 2:
                bgUrl = "sbg-bg-rank2"
            case 3:
                bgUrl = "sbg-bg-rank3"
            default:
                bgUrl = "sbg-bg-rank"
            }
            bgImgView.image = UIImage.sceneImage(name: bgUrl)
        }
    }
    
    private func layoutUI() {

        bgImgView = UIImageView()
        self.contentView.addSubview(bgImgView)

        iconView = UIImageView()
        self.contentView.addSubview(iconView)

        indexLabel = UILabel()
        indexLabel.textColor = .white
        indexLabel.font = UIFont.systemFont(ofSize: 10)
        indexLabel.backgroundColor = .clear
        indexLabel.textAlignment = .center
        self.contentView.addSubview(indexLabel)

        headIcon = UIImageView()
        headIcon.image = UIImage(named: "")
        headIcon.backgroundColor = .clear
        self.contentView.addSubview(headIcon)

        nameLabel = UILabel()
        nameLabel.textColor = .white
        nameLabel.backgroundColor = .clear
        nameLabel.font = UIFont.systemFont(ofSize: 10)
        self.contentView.addSubview(nameLabel)

        sbgCountLabel = UILabel()
        sbgCountLabel.textColor = .white
        sbgCountLabel.backgroundColor = .clear
        sbgCountLabel.font = UIFont.systemFont(ofSize: 10)
        sbgCountLabel.textAlignment = .center
        self.contentView.addSubview(sbgCountLabel)
        
        gradeLabel = UILabel()
        gradeLabel.textColor = .white
        gradeLabel.backgroundColor = .clear
        gradeLabel.font = UIFont.systemFont(ofSize: 10)
        gradeLabel.textAlignment = .center
        self.contentView.addSubview(gradeLabel)
        
        self.backgroundColor = .clear
        
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImgView.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: 38)
        let basicWidth = self.bounds.size.width / 5.0
        iconView.frame = CGRect(x: basicWidth / 2.0 - 18, y: 5, width: 36, height: 28)
        indexLabel.frame = CGRect(x: basicWidth / 2.0 - 18, y: 5, width: 36, height: 28)
        headIcon.frame = CGRect(x: basicWidth + 20, y: 7, width: 24, height: 24)
        nameLabel.frame = CGRect(x:headIcon.frame.maxX + 12, y: 5, width: 60, height: 28)
        sbgCountLabel.frame = CGRect(x:basicWidth * 3, y: 5, width: basicWidth, height: 28)
        gradeLabel.frame = CGRect(x:basicWidth * 4, y: 5, width: basicWidth, height: 28)
    }

}
