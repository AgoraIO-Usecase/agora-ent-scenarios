//
//  GameSection.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/29.
//

import Foundation

class GameMixedSectionView: SectionView {
    lazy var gameAdView: GameAdView = {
       let adView = GameAdView()
        return adView
    }()
    
    required init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(gameAdView)
        self.backgroundColor = .clear
        
        gameAdView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.bottom.equalTo(0)
        }
        
        titleLabel.isHidden = true
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

class SectionView: UICollectionReusableView {
    let titleLabel = UILabel()
    
    lazy var innerHeaderView: GameAdView = {
        let view = GameAdView()
        return view;
    }()
    
    required override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.left.equalTo(34)
            make.top.bottom.equalTo(self)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
