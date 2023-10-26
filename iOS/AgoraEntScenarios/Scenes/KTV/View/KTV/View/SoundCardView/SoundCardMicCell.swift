//
//  SoundCardMicCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/12.
//

import UIKit

class SoundCardMicCell: UITableViewCell {
    var titleLabel: UILabel!
    var detailLabel: UILabel!
    var numLable: UILabel!
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        titleLabel = UILabel()
        titleLabel.text = "麦克风类型"
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        self.contentView.addSubview(titleLabel)
        
        detailLabel = UILabel()
        detailLabel.text = "选择更适合当前麦克风的预设参数"
        detailLabel.font = UIFont.systemFont(ofSize: 12)
        detailLabel.textColor = .lightGray
        self.contentView.addSubview(detailLabel)
        
        numLable = UILabel()
        numLable.font = UIFont.systemFont(ofSize: 13)
        numLable.text = "类型A"
        numLable.textColor = .gray
        numLable.textAlignment = .right
        self.contentView.addSubview(numLable)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = CGRect(x: 20, y: 5, width: 80, height: 18)
        detailLabel.frame = CGRect(x: 20, y: 25, width: 200, height: 18)
        numLable.frame = CGRect(x: self.bounds.size.width - 130, y: 16, width: 100, height: 20)
    }
}
