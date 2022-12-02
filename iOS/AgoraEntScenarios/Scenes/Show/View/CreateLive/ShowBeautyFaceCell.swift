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
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ByteBeautyModel) {
        nameLabel.text = model.name
        imageView.image = UIImage.show_byteBeautyImage(name: model.icon)
        indicatorImgView.isHidden = !model.isSelected
        nameLabel.font = model.isSelected ? .show_M_12 : .show_R_11
        nameLabel.textColor = model.isSelected ? .show_main_text : .show_beauty_deselect
    }
    
    private func createSubviews(){
        // 图
        imageView = UIImageView()
        imageView.image = UIImage.show_byteBeautyImage(name: "show_beauty_none")
        imageView.contentMode = .scaleAspectFit
        contentView.addSubview(imageView)
        
        // 选中标识
        indicatorImgView = UIImageView()
        indicatorImgView.isHidden = true
        indicatorImgView.image = UIImage.show_byteBeautyImage(name: "show_beauty_selected")
        contentView.addSubview(indicatorImgView)
        
        imageView.snp.makeConstraints { make in
            make.center.equalTo(indicatorImgView)
        }
        
        indicatorImgView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
            make.width.height.equalTo(52)
        }
        
        // 名称
        nameLabel = UILabel()
        nameLabel.font = .show_R_11
        nameLabel.textColor = .show_beauty_deselect
        nameLabel.numberOfLines = 2
        nameLabel.text = "美白".show_localized
        contentView.addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.centerX.equalTo(indicatorImgView)
            make.bottom.equalToSuperview()
        }
    }

}
