//
//  RoomListCell.swift
//  Joy
//
//  Created by wushengtao on 2023/7/28.
//

import Foundation
import SDWebImage
import SnapKit

class RoomListCell: UICollectionViewCell {
    lazy var badgeLabel: UILabel = {
        let label = GradientLabel()
        label.textColor = .joy_main_text
        label.font = .joy_M_10
        label.textAlignment = .center
        label.setGradientBackground(colors: [
            UIColor(red: 1, green: 0.467, blue: 0.851, alpha: 1),
            UIColor(red: 0.801, green: 0.379, blue: 1, alpha: 1)
            ])

        return label
    }()
    
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.cornerRadius = 16
        imageView.layer.masksToBounds = true
        imageView.contentMode = .scaleAspectFill
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "show_default_avatar"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.layer.cornerRadius = 32
        imageView.clipsToBounds = true
        return imageView
    }()
    private lazy var privateImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "show_private_room_icon"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.isHidden = true
        return imageView
    }()
    private lazy var nameLabel: UILabel = {
        let nameLabel = UILabel()
        nameLabel.font = .systemFont(ofSize: 14, weight: .medium)
        nameLabel.textColor = UIColor(hexString: "#000000")
        nameLabel.numberOfLines = 2
        nameLabel.textAlignment = .center
        nameLabel.text = "Chat with Eve tonight and merry Christmas"
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        return nameLabel
    }()
    private lazy var idLablel: UILabel = {
        let idLablel = UILabel()
        idLablel.font = .joy_R_10
        idLablel.textColor = UIColor(hexString: "#303553")
        idLablel.translatesAutoresizingMaskIntoConstraints = false
        return idLablel
    }()
    private lazy var numberLabel: UILabel = {
        let numberLabel = UILabel()
        numberLabel.font = .joy_R_10
        numberLabel.textColor = UIColor(hexString: "#303553")
        numberLabel.translatesAutoresizingMaskIntoConstraints = false
        return numberLabel
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setBgImge(_ img: String, 
                   name: String?,
                   id: String?,
                   badge: String?,
                   count: Int,
                   avatarUrl: String?,
                   isPrivate: Bool) {
        imageView.image = UIImage.sceneImage(name: "room_bg_\(img)")
        nameLabel.text = name
        idLablel.text = "ID: \(id ?? "0")"
        var attachment = NSTextAttachment()
        let image = UIImage.sceneImage(name: "room_person")?.withTintColor(UIColor(hexString: "#303553")!, renderingMode: .alwaysOriginal)
        if #available(iOS 13.0, *) {
            attachment = NSTextAttachment(image: image!)
        } else {
            attachment.image = image
        }
        attachment.bounds = CGRect(x: 0, y: 0, width: 10, height: 10)
        let attriTipsImg = NSAttributedString(attachment: attachment)
        let attriTips = NSMutableAttributedString(attributedString: attriTipsImg)
        attriTips.append(NSAttributedString(string: " \(count)\("joy_user_count".joyLocalization())"))
        numberLabel.attributedText = attriTips
        avatarImageView.sd_setImage(with: URL(string: avatarUrl ?? ""), placeholderImage: UIImage.sceneImage(name: "show_default_avatar"))
        privateImageView.isHidden = !isPrivate
        if let text = badge, !text.isEmpty {
            badgeLabel.text = "\(text ?? "")🔥"
            badgeLabel.isHidden = false
        } else {
            badgeLabel.isHidden = true
        }
        badgeLabel.sizeToFit()
        badgeLabel.size = CGSize(width: badgeLabel.width + 24, height: badgeLabel.height + 4)
        badgeLabel.setRoundedCorner(topLeft: badgeLabel.height, topRight: 0, bottomLeft: 0, bottomRight: badgeLabel.height)
    }
    
    private func createSubviews(){
        // 背景图
        contentView.addSubview(imageView)
        imageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        imageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor).isActive = true
        imageView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        imageView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        contentView.addSubview(avatarImageView)
        avatarImageView.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        avatarImageView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 16).isActive = true
        avatarImageView.widthAnchor.constraint(equalToConstant: 64).isActive = true
        avatarImageView.heightAnchor.constraint(equalToConstant: 64).isActive = true
        
        contentView.addSubview(privateImageView)
        privateImageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -10).isActive = true
        privateImageView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 9).isActive = true
        
        // 名称
        contentView.addSubview(nameLabel)
        nameLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 12).isActive = true
        nameLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -12).isActive = true
        nameLabel.topAnchor.constraint(equalTo: avatarImageView.bottomAnchor, constant: 7).isActive = true
        
        // id
        contentView.addSubview(idLablel)
        idLablel.topAnchor.constraint(equalTo: nameLabel.bottomAnchor, constant: 7).isActive = true
        idLablel.leadingAnchor.constraint(equalTo: nameLabel.leadingAnchor).isActive = true
        idLablel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -12).isActive = true
        
        // 人数
        contentView.addSubview(numberLabel)
        numberLabel.centerYAnchor.constraint(equalTo: idLablel.centerYAnchor).isActive = true
        numberLabel.trailingAnchor.constraint(equalTo: nameLabel.trailingAnchor).isActive = true
        
        //游戏名
        contentView.addSubview(badgeLabel)
        badgeLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        badgeLabel.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
    }
}
