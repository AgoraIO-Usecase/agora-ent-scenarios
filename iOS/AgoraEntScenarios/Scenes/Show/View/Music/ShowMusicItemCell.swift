//
//  ShowMusicItemCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/9.
//

import UIKit

 class ShowMusicItemCell: UICollectionViewCell {
    
    // 图
    var imageView: UIImageView!
    // 名称
    var nameLabel: UILabel!
    // 选中标识
    fileprivate var indicatorImgView: UIImageView!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
        show_layoutImageTitle()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        // 选中标识
        indicatorImgView = UIImageView()
        indicatorImgView.isHidden = true
        indicatorImgView.image = UIImage.show_sceneImage(name: "show_music_set_select_indicator")
        contentView.addSubview(indicatorImgView)
        indicatorImgView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
            make.width.height.equalTo(58)
        }
        
        // 图
        imageView = UIImageView()
//        imageView.image = UIImage.show_sceneImage(name: "show_beauty_none")
        contentView.addSubview(imageView)
        
        // 蒙版
        let coverView = UIView()
        coverView.layer.cornerRadius = 8
        coverView.layer.masksToBounds = true
        contentView.addSubview(coverView)
        coverView.backgroundColor = .show_music_item_bg
        coverView.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets(top: 2, left: 2, bottom: 2, right: 2))
        }
        
        // 名称
        nameLabel = UILabel()
        nameLabel.font = .show_R_11
        nameLabel.textColor = .show_beauty_deselect
        nameLabel.numberOfLines = 2
        contentView.addSubview(nameLabel)
    }
    
    func show_layoutImageTitle() {
        indicatorImgView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
            make.width.height.equalTo(48)
        }
        
        imageView.snp.makeConstraints { make in
            make.center.equalTo(indicatorImgView)
        }
        
        nameLabel.snp.makeConstraints { make in
            make.centerX.equalTo(indicatorImgView)
            make.bottom.equalToSuperview()
        }
    }
    
     func setImage(_ image: String, name: String, isSelected: Bool = false) {
        imageView.image = UIImage.show_sceneImage(name: image)
        nameLabel.text = name
        indicatorImgView.isHidden = !isSelected
        nameLabel.textColor = isSelected ? .show_main_text : .show_beauty_deselect
    }
}


class ShowMusicImageTopItemCell: ShowMusicItemCell {
    
    override func show_layoutImageTitle() {
        
        imageView.snp.makeConstraints { make in
            make.centerX.equalTo(indicatorImgView)
            make.top.equalTo(indicatorImgView).offset(5)
        }
        
        nameLabel.snp.makeConstraints { make in
            make.centerX.equalTo(indicatorImgView)
            make.bottom.equalToSuperview().offset(-5)
        }
    }
}

class ShowMusicImageBackgroundItemCell: ShowMusicItemCell {
    
    override func show_layoutImageTitle() {
        
        imageView.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets(top: 2, left: 2, bottom: 2, right: 2))
        }
        
        nameLabel.snp.makeConstraints { make in
            make.center.equalTo(indicatorImgView)
        }
    }
}

class ShowMusicImageOnlyItemCell: ShowMusicItemCell {
    
    override func show_layoutImageTitle() {
      
        imageView.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
        
        nameLabel.isHidden = true
    }
}

