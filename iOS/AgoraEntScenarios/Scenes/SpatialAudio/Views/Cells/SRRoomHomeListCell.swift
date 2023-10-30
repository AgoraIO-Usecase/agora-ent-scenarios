//
//  VRRoomHomeListCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/23.
//

import Foundation
import UIKit
import SDWebImage

class SRRoomHomeListCell: UICollectionViewCell {
    
    var bgImgView: UIImageView!
    var iconImgView: UIImageView!
    var titleLabel: UILabel!
    var onListIconView: UIImageView!
    var countLabel: UILabel!
    var lockImgView: UIImageView!
    var roomOwnerLabel: UILabel!
    var roomEntity: SARoomEntity? {
        didSet {
            if let avatarURL = URL(string: roomEntity?.owner?.portrait ?? "") {
                iconImgView.sd_setImage(with: avatarURL)
            }
            lockImgView.isHidden = !(roomEntity?.is_private ?? false)
            titleLabel.text = roomEntity?.name
            roomOwnerLabel.text = roomEntity?.owner?.name ?? ""
            countLabel.text = "\(roomEntity?.member_count ?? 0)人"
        }
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Setup View
    func setupView() {
        bgImgView = UIImageView(frame: CGRect(x: 0, y: 0, width: self.bounds.width, height: self.bounds.height))
        contentView.addSubview(bgImgView)
        
        iconImgView = UIImageView(frame: CGRect(x: (self.bounds.width - 64) * 0.5, y: 20, width: 64, height: 64))
        iconImgView.layer.cornerRadius = 20
        iconImgView.layer.masksToBounds = true
        iconImgView.isUserInteractionEnabled = true
        bgImgView.addSubview(iconImgView)
        
        lockImgView = UIImageView(frame: CGRect(x: self.bounds.width - 26, y: 10, width: 16, height: 16))
        lockImgView.image = UIImage(named: "suo")
        bgImgView.addSubview(lockImgView)
        
        titleLabel = UILabel(frame: CGRect(x: 10, y: iconImgView.frame.maxY + 10, width: self.bounds.width - 20, height: 40))
        titleLabel.font = UIFont.boldSystemFont(ofSize: 14)
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 0
        titleLabel.textColor = UIColor(hexString: "#040925")
        titleLabel.isUserInteractionEnabled = true
        bgImgView.addSubview(titleLabel)
        
        onListIconView = UIImageView(frame: CGRect(x: self.bounds.width - 55, y: self.bounds.height - 16 - 11, width: 11, height: 11))
        onListIconView.image = UIImage(named: "online_list_countIcon")
        contentView.addSubview(onListIconView)
        
        countLabel = UILabel(frame: CGRect(x: onListIconView.frame.maxX + 2, y: onListIconView.center.y - 7, width: 40, height: 14))
        countLabel.font = UIFont.systemFont(ofSize: 12)
        countLabel.textColor = UIColor(hexString: "#6C7192")
        contentView.addSubview(countLabel)
        
        roomOwnerLabel = UILabel(frame: CGRect(x: 10, y: onListIconView.center.y - 7, width: 60, height: 14))
        roomOwnerLabel.font = UIFont.boldSystemFont(ofSize: 12)
        roomOwnerLabel.textColor = UIColor(hexString: "#6C7192")
        roomOwnerLabel.isUserInteractionEnabled = true
        bgImgView.addSubview(roomOwnerLabel)
    }
    
}
