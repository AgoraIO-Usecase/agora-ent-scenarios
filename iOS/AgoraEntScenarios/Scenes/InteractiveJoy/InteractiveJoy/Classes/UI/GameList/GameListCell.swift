//
//  GameListCell.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import UIKit

class GameListCell: UICollectionViewCell {
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.cornerRadius = 15
        imageView.layer.masksToBounds = true
        imageView.backgroundColor = .white
        return imageView
    }()
    
    private lazy var gameName : UILabel = {
        let label = UILabel()
        label.font = UIFont.joy_R_12
        label.textColor = UIColor.joy_title_text
        label.textAlignment = .center
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        contentView.addSubview(imageView)
        contentView.addSubview(gameName)
        
        imageView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
            make.width.equalToSuperview()
            make.height.equalTo(imageView.snp.width)
        }
        
        gameName.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom).offset(5)
            make.centerX.equalToSuperview()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(with game: Game) {
        imageView.image = UIImage.sceneImage(name: game.gamePic)
        gameName.text = game.gameName
    }
}
