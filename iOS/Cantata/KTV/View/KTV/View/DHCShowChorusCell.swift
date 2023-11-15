//
//  DHCShowChorusCell.swift
//  Cantata
//
//  Created by CP on 2023/9/19.
//

import UIKit
import SDWebImage
class DHCShowChorusCell: UITableViewCell {
    @IBOutlet weak var levelImgview: UIImageView!
    @IBOutlet weak var gradeLabel: UILabel!
    @IBOutlet weak var nameLabel: UIButton!
    @IBOutlet weak var headImgView: UIImageView!
    @IBOutlet weak var levelLabel: UILabel!
    @IBOutlet weak var leaveBtn: UIButton!
    private var model: ChorusShowModel?
    var leaveBlock:((String)->Void)?
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        nameLabel.titleLabel?.font = UIFont.systemFont(ofSize: 15)
        leaveBtn.setImage(UIImage.sceneImage(name: "leaveChorus", bundleName: "DHCResource"), for: .normal)
        leaveBtn.addTarget(self, action: #selector(leaveChorus), for: .touchUpInside)
    }
    
    @objc private func leaveChorus() {
        guard let leaveBlock = self.leaveBlock else {return}
        leaveBlock(model?.userNo ?? "")
    }
    
    public func setModel(with model: ChorusShowModel) {
        self.model = model
        switch model.level {
        case 0:
            levelImgview.isHidden = false
            levelLabel.isHidden = true
            levelImgview.image = UIImage.sceneImage(name: "sbg-rank1", bundleName: "DHCResource")
        case 1:
            levelImgview.isHidden = false
            levelLabel.isHidden = true
            levelImgview.image = UIImage.sceneImage(name: "sbg-rank2", bundleName: "DHCResource")
        case 2:
            levelImgview.isHidden = false
            levelLabel.isHidden = true
            levelImgview.image = UIImage.sceneImage(name: "sbg-rank3", bundleName: "DHCResource")
        default:
            levelImgview.isHidden = true
            levelLabel.isHidden = false
        }
        leaveBtn.isHidden = !model.isRoomOwner || model.isMaster
        levelLabel.text = "\(model.level)"
        gradeLabel.text = "当前 \(model.num) 分"
        nameLabel.setTitle("\(model.name)", for: .normal)
        nameLabel.setImage(model.isMaster ? UIImage.sceneImage(name: "dhc_mainSinger", bundleName: "DHCResource") : nil, for: .normal)
        headImgView.sd_setImage(with: NSURL(string: model.headIcon) as URL?)
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
