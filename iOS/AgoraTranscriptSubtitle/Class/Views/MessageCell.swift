//
//  MessageCell.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import UIKit

class MessageCell: UITableViewCell {
    private let logTag = "MessageCell"
    let label = UILabel()
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
        
        label.numberOfLines = 0
        bgView.layer.cornerRadius = 10
        bgView.layer.masksToBounds = true
        
        contentView.addSubview(bgView)
        contentView.addSubview(label)
        
        bgView.translatesAutoresizingMaskIntoConstraints = false
        label.translatesAutoresizingMaskIntoConstraints = false
        
        label.leftAnchor.constraint(equalTo: contentView.leftAnchor, constant: 10).isActive = true
        label.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 10).isActive = true
        label.rightAnchor.constraint(lessThanOrEqualTo: contentView.rightAnchor, constant: -10).isActive = true
        label.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -10).isActive = true
        
        bgView.leftAnchor.constraint(equalTo: label.leftAnchor, constant: -5).isActive = true
        bgView.rightAnchor.constraint(equalTo: label.rightAnchor, constant: 5).isActive = true
        bgView.topAnchor.constraint(equalTo: label.topAnchor, constant: -5).isActive = true
        bgView.bottomAnchor.constraint(equalTo: label.bottomAnchor, constant: 5).isActive = true
    }
}
