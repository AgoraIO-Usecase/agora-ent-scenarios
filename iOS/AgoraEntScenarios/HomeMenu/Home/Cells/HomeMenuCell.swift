//
//  HomeMenuCell.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2022/10/27.
//

import UIKit

final class HomeMenuCell: UICollectionViewCell {
    lazy var itemView: VLHomeItemView = .init(frame: self.contentView.frame)

    override init(frame: CGRect) {
        super.init(frame: frame)
        contentView.addSubview(itemView)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc func refresh(item: VLHomeItemModel) {
        itemView.itemModel = item
    }
}
