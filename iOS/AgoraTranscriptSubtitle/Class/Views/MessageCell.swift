//
//  MessageCell.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import UIKit

class MessageCell: UITableViewCell {
    private let logTag = "MessageCell"
    let transcriptLabel = UILabel()
    let translateLabel = UILabel()
    let bgView = UIView()

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        backgroundColor = .clear
        contentView.layer.cornerRadius = 5
        contentView.layer.masksToBounds = true
        
        transcriptLabel.numberOfLines = 0
        translateLabel.numberOfLines = 0
        bgView.layer.cornerRadius = 5
        bgView.layer.masksToBounds = true
        
        contentView.addSubview(bgView)
        bgView.addSubview(transcriptLabel)
        bgView.addSubview(translateLabel)
        
        bgView.translatesAutoresizingMaskIntoConstraints = false
        transcriptLabel.translatesAutoresizingMaskIntoConstraints = false
        translateLabel.translatesAutoresizingMaskIntoConstraints = false
        
        bgView.leftAnchor.constraint(equalTo: contentView.leftAnchor).isActive = true
        bgView.rightAnchor.constraint(equalTo: contentView.rightAnchor).isActive = true
        bgView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        bgView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -10).isActive = true
        
        transcriptLabel.leftAnchor.constraint(equalTo: bgView.leftAnchor, constant: 5).isActive = true
        transcriptLabel.rightAnchor.constraint(equalTo: bgView.rightAnchor, constant: -5).isActive = true
        transcriptLabel.topAnchor.constraint(equalTo: bgView.topAnchor, constant: 5).isActive = true
        
        translateLabel.leftAnchor.constraint(equalTo: bgView.leftAnchor, constant: 5).isActive = true
        translateLabel.rightAnchor.constraint(equalTo: bgView.rightAnchor, constant: -5).isActive = true
        translateLabel.topAnchor.constraint(equalTo: transcriptLabel.bottomAnchor, constant: 5).isActive = true
        translateLabel.bottomAnchor.constraint(equalTo: bgView.bottomAnchor, constant: -5).isActive = true
    }
}
