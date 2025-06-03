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
    
        gameAdView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(0)
            make.height.equalTo(80)
        }
                
        titleLabel.snp.remakeConstraints { make in
            make.left.equalTo(34)
            make.right.equalTo(-34)
            make.top.equalTo(gameAdView.snp.bottom).offset(10)
            make.bottom.equalTo(0)
        }
}
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

class SectionView: UICollectionReusableView {
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.joy_segment_title_nor
        label.font = UIFont.joy_M_14
        return label
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
