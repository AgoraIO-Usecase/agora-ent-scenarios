//
//  ShowBeautyFaceCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import UIKit

class ShowBeautyFaceCell: UICollectionViewCell {
    // 图
    var imageView: UIImageView!
    // 名称
    var nameLabel: UILabel!
    // 选中标识
    private var indicatorImgView: UIImageView!
    
    override var isSelected: Bool {
        didSet{
            indicatorImgView.isHidden = !isSelected
            nameLabel.font = isSelected ? .show_M_12 : .show_R_11
            nameLabel.textColor = isSelected ? .show_main_text : .show_beauty_deselect
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        // 选中标识
        indicatorImgView = UIImageView()
        indicatorImgView.isHidden = true
        indicatorImgView.image = UIImage.show_sceneImage(name: "show_beauty_selected")
        contentView.addSubview(indicatorImgView)
        indicatorImgView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
            make.width.height.equalTo(48)
        }
        
        // 图
        imageView = UIImageView()
        imageView.image = UIImage.show_sceneImage(name: "show_beauty_none")
        contentView.addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.center.equalTo(indicatorImgView)
        }
        
        // 名称
        nameLabel = UILabel()
        nameLabel.font = .show_R_11
        nameLabel.textColor = .show_main_text
        nameLabel.numberOfLines = 2
        nameLabel.text = "美白".show_localized
        contentView.addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.centerX.equalTo(indicatorImgView)
            make.bottom.equalToSuperview()
        }
    }

}
