//
//  HorizontalCardCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit

public class HorizontalCardCell: UICollectionViewCell {
    
    private (set) weak var embededView: UIView?

    func embedView(_ view: HorizontalCardView) {
        self.contentView.backgroundColor = .clear
        self.contentView.subviews.forEach { $0.removeFromSuperview() }
        self.contentView.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.leftAnchor.constraint(equalTo: self.contentView.leftAnchor, constant: 0).isActive = true
        view.rightAnchor.constraint(equalTo: self.contentView.rightAnchor, constant: 0).isActive = true
        view.topAnchor.constraint(equalTo: self.contentView.topAnchor, constant: 0).isActive = true
        view.bottomAnchor.constraint(equalTo: self.contentView.bottomAnchor, constant: 0).isActive = true
        self.embededView = view
    }
    
}
