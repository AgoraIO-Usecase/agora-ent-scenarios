//
//  GameListCell.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import UIKit

class GameListCell: UICollectionViewCell {
    private let imageView = UIImageView()
    private let gameName = UILabel()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        imageView.backgroundColor = .white
        contentView.addSubview(imageView)
        contentView.addSubview(gameName)
        
        imageView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
            make.width.equalToSuperview()
            make.height.equalTo(imageView.snp.width)
        }
        
        gameName.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom).offset(20)
            make.centerX.equalToSuperview()
        }
        
        gameName.textAlignment = .center
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(with game: Game) {
        imageView.image = UIImage.sceneImage(name: game.gamePic)
        gameName.text = game.gameName
    }
}
