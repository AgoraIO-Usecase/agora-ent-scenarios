//
//  HomeMenuCell.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2022/10/27.
//

import UIKit

final class HomeMenuCell: UICollectionViewCell {
    lazy var itemView: VLHomeItemView = .init(frame: self.contentView.frame)

    private var isClicked = false // 点击状态
        
    @objc func handleClick() {
        guard !isClicked else {
            return
        }
        // 执行点击事件
        isClicked = true
    }
    
    override func prepareForReuse() {
        isClicked = false // 重用前将状态重置
    }
    
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
