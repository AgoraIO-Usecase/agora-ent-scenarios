//
//  ShowRoomListCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit
import SnapKit

class ShowRoomListCell: UICollectionViewCell {
    
    // 背景图
    var imageView: UIImageView!
    // 房间名称
    var nameLabel: UILabel!
    // id
    var idLablel: UILabel!
    // 人数
    var numberLabel: UILabel!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        // 背景图
        imageView = UIImageView()
        imageView.layer.cornerRadius = 10
        imageView.layer.masksToBounds = true
        imageView.contentMode = .scaleAspectFill
        contentView.addSubview(imageView)
        imageView.sd_setImage(with: URL(string: "https://img0.baidu.com/it/u=1824168527,1620830099&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1667494800&t=20221f64ed073069b5caf60756a53221"))
        imageView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        // 直播标识
        let indicatorImgView = UIImageView()
        indicatorImgView.image = UIImage.show_sceneImage(name: "show_live_indictor")
        contentView.addSubview(indicatorImgView)
        indicatorImgView.snp.makeConstraints { make in
            make.top.equalTo(5)
            make.right.equalTo(-5)
        }
        
        // 蒙版
        let coverImgView = UIImageView()
        coverImgView.image = UIImage.show_sceneImage(name: "show_list_cover")
        contentView.addSubview(coverImgView)
        coverImgView.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
        }
        
        // 名称
        nameLabel = UILabel()
        nameLabel.font = .show_M_12
        nameLabel.textColor = .show_main_text
        nameLabel.numberOfLines = 2
        nameLabel.text = "Chat with Eve tonight and merry Christmas"
        contentView.addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.left.equalTo(10)
            make.top.equalTo(coverImgView).offset(8)
            make.right.equalTo(-10)
        }
        
        // id
        idLablel = UILabel()
        idLablel.font = .show_R_10
        idLablel.textColor = .show_main_text
        idLablel.text = "ID: 1234123"
        contentView.addSubview(idLablel)
        idLablel.snp.makeConstraints { make in
            make.left.equalTo(nameLabel)
            make.bottom.equalTo(-10)
        }
        
        // 人数
        numberLabel = UILabel()
        numberLabel.font = .show_R_10
        numberLabel.textColor = .show_main_text
        numberLabel.text = "5人"
        contentView.addSubview(numberLabel)
        numberLabel.snp.makeConstraints { make in
            make.centerY.equalTo(idLablel)
            make.left.equalTo(102)
            make.width.equalTo(60)
        }
    }
}
