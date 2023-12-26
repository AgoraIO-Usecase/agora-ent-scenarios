//
//  SoundCardEffectCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/12.
///Users/cp/Desktop/AgoraEntScenarios 2023-10-13 11-35-47

import UIKit

class SoundCardMicTypeCell: UITableViewCell {

    var titleLabel: UILabel!
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func setIsSelected(_ selected: Bool) {
        titleLabel.textColor = selected ? .blue : UIColor(red: 48/255.0, green: 53/255.0, blue: 83/255.0, alpha: 1)
    }
    
    private func layoutUI() {
        
        titleLabel = UILabel()
        titleLabel.text = Bundle.localizedString("ktv_mic_type", bundleName: "KtvResource")
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        titleLabel.textAlignment = .center
        titleLabel.textColor = UIColor(red: 48/255.0, green: 53/255.0, blue: 83/255.0, alpha: 1)
        self.contentView.addSubview(titleLabel)

    }

    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: 48)
    }

}
