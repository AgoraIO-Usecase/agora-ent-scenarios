//
//  ShowRoomListCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit
import SnapKit

class ShowRoomListCell: UICollectionViewCell {
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
        imageView.cornerRadius(32)
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
        nameLabel.textColor = UIColor(hex: "#000000", alpha: 1.0)
        nameLabel.numberOfLines = 2
        nameLabel.textAlignment = .center
        nameLabel.text = "Chat with Eve tonight and merry Christmas"
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        return nameLabel
    }()
    private lazy var idLablel: UILabel = {
        let idLablel = UILabel()
        idLablel.font = .show_R_10
        idLablel.textColor = UIColor(hex: "#303553", alpha: 1.0)
        idLablel.translatesAutoresizingMaskIntoConstraints = false
        return idLablel
    }()
    private lazy var numberLabel: UILabel = {
        let numberLabel = UILabel()
        numberLabel.font = .show_R_10
        numberLabel.textColor = UIColor(hex: "#303553", alpha: 1.0)
        numberLabel.translatesAutoresizingMaskIntoConstraints = false
        return numberLabel
    }()
    
    private lazy var coverLayer: CALayer = {
        let layer = CALayer()
        layer.backgroundColor = UIColor(hex: "#000000", alpha: 0.05).cgColor
        layer.isHidden = true
        layer.masksToBounds = true
        layer.cornerRadius = 16
        return layer
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setBgImge(_ img: String, name: String?, id: String?, count: Int, avatarUrl: String?, isPrivate: Bool) {
        imageView.image = UIImage.show_sceneImage(name: "show_room_bg_\(img)")
        nameLabel.text = name
        idLablel.text = "ID: \(id ?? "0")"
        var attachment = NSTextAttachment()
        let image = UIImage.show_sceneImage(name: "show_room_person")?.withTintColor(UIColor(hex: "#303553", alpha: 1.0), renderingMode: .alwaysOriginal)
        if #available(iOS 13.0, *) {
            attachment = NSTextAttachment(image: image!)
        } else {
            attachment.image = image
        }
        attachment.bounds = CGRect(x: 0, y: 0, width: 10, height: 10)
        let attriTipsImg = NSAttributedString(attachment: attachment)
        let attriTips = NSMutableAttributedString(attributedString: attriTipsImg)
        attriTips.append(NSAttributedString(string: " \(count)\("show_user_count".show_localized)"))
        numberLabel.attributedText = attriTips
        avatarImageView.sd_setImage(with: URL(string: avatarUrl ?? ""), placeholderImage: UIImage.sceneImage(name: "show_default_avatar"))
        privateImageView.isHidden = !isPrivate
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
        
        contentView.layer.addSublayer(coverLayer)
        coverLayer.frame = contentView.bounds
    }
    
    func showCoverView(){
        coverLayer.isHidden = false
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.coverLayer.isHidden = true
        }
    }
}
