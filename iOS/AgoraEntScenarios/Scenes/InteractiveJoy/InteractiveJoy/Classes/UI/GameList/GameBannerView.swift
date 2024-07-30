//
//  GameBannerView.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/29.
//

import UIKit

class GameBannerCell: UICollectionViewCell {
    lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.layer.cornerRadius = 16
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.addSubview(imageView)
        self.contentView.addSubview(titleLabel)
        
        imageView.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets.zero)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
        
class GameBannerView: UIView {
    private var collectionView: UICollectionView!
    private lazy var cornerSubView: GameAdView = {
        let cornerView = GameAdView()
        cornerView.imageView.image = UIImage.sceneImage(name: "game_hot_ic")
        cornerView.titleLabel.text = LanguageManager.localValue(key: "game_list_hot_section_title")
        cornerView.descriptionLabel.text = LanguageManager.localValue(key: "game_list_hot_section_des")
        cornerView.layer.cornerRadius = 24
        cornerView.layer.masksToBounds = true
        cornerView.backgroundColor = .white
        return cornerView
    }()
    
    private var banners: [BannerModel]?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = frame.size
        layout.scrollDirection = .horizontal
        
        collectionView = UICollectionView(frame: CGRect(origin: CGPointZero, size: frame.size), collectionViewLayout: layout)
        collectionView.register(GameBannerCell.self, forCellWithReuseIdentifier: "GameBannerCell")
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.showsHorizontalScrollIndicator = false;
        self.addSubview(collectionView)
        collectionView.snp_makeConstraints { make in
            make.top.equalTo(32)
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.height.equalTo(200)
        }
        
        self.addSubview(cornerSubView)
        cornerSubView.snp_makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(collectionView.snp_bottom).offset(22)
            make.height.equalTo(80)
        }
    }
    
    func setBanners(banners: [BannerModel]) {
        self.banners = banners
        collectionView.reloadData()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension GameBannerView: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        guard let banners = banners else {return 0}
        
        return banners.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "GameBannerCell", for: indexPath) as! GameBannerCell
        let bannerModel = banners![indexPath.item]
        cell.imageView.image = UIImage.sceneImage(name: bannerModel.image)
        cell.titleLabel.text = bannerModel.title
        return cell
    }
}
