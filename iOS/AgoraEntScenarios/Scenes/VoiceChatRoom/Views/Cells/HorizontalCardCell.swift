//
//  HorizontalCardCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit

public class HorizontalCardCell: UICollectionViewCell {
    private(set) weak var embededView: UIView?

    func embedView(_ view: HorizontalCardView) {
        contentView.backgroundColor = .clear
        contentView.subviews.forEach { $0.removeFromSuperview() }
        contentView.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.leftAnchor.constraint(equalTo: contentView.leftAnchor, constant: 0).isActive = true
        view.rightAnchor.constraint(equalTo: contentView.rightAnchor, constant: 0).isActive = true
        view.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 0).isActive = true
        view.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: 0).isActive = true
        embededView = view
    }
}
