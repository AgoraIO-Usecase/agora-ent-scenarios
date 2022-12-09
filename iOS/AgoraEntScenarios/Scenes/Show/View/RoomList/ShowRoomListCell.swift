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
    
    func setBgImge(_ img: String, name: String?, id: String?, count: Int) {
        imageView.image = UIImage.show_sceneImage(name: "show_room_bg_\(img)")
        nameLabel.text = name
        idLablel.text = "ID: \(id ?? "0")"
        var attachment = NSTextAttachment()
        if #available(iOS 13.0, *) {
            attachment = NSTextAttachment(image: UIImage.show_sceneImage(name: "show_room_person")!)
        } else {
            attachment.image = UIImage.show_sceneImage(name: "show_room_person")
        }
        attachment.bounds = CGRect(x: 0, y: 0, width: 10, height: 10)
        let attriTipsImg = NSAttributedString(attachment: attachment)
        let attriTips = NSMutableAttributedString(attributedString: attriTipsImg)
        attriTips.append(NSAttributedString(string: "  \(count)人"))
        numberLabel.attributedText = attriTips
    }
    
    private func createSubviews(){
        // 背景图
        imageView = UIImageView()
        imageView.layer.cornerRadius = 10
        imageView.layer.masksToBounds = true
        imageView.contentMode = .scaleAspectFill
        contentView.addSubview(imageView)
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
            make.bottom.equalTo(coverImgView).offset(-30)
            make.right.equalTo(-10)
        }
        
        // id
        idLablel = UILabel()
        idLablel.font = .show_R_10
        idLablel.textColor = .show_main_text
        contentView.addSubview(idLablel)
        idLablel.snp.makeConstraints { make in
            make.left.equalTo(nameLabel)
            make.bottom.equalTo(-10)
        }
        
        // 人数
        numberLabel = UILabel()
        numberLabel.font = .show_R_10
        numberLabel.textColor = .show_main_text
        contentView.addSubview(numberLabel)
        numberLabel.snp.makeConstraints { make in
            make.centerY.equalTo(idLablel)
            make.left.equalTo(102)
            make.width.equalTo(60)
        }
    }
}
