//
//  ShowRoomInfoView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit
import SnapKit

private let bgViewHeight: CGFloat = 40

class ShowRoomInfoView: UIView {
    
    // 背景
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = .show_room_info_cover
        view.layer.cornerRadius = bgViewHeight * 0.5
        view.layer.masksToBounds = true
        return view
    }()
    
    // 头像
    private lazy var headImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.contentMode = .scaleAspectFill
        return imgView
    }()
    
    // 名称
    private lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_main_text
        label.font = .show_M_14
        return label
    }()
    
    // 房间号
    private lazy var idLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_main_text
        label.alpha = 0.8
        label.font = .show_R_10
        return label
    }()
    
    
    // 时间
    private lazy var timeLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_main_text
        label.alpha = 0.8
        label.font = .show_R_10
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
            make.width.greaterThanOrEqualTo(202)
            make.height.equalTo(bgViewHeight)
        }
        
        addSubview(headImgView)
        headImgView.snp.makeConstraints { make in
            make.top.equalTo(4)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(32)
        }
        
        addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.left.equalTo(headImgView.snp.right).offset(8)
            make.top.equalTo(headImgView)
            make.right.equalTo(-20)
        }
        
        addSubview(idLabel)
        idLabel.snp.makeConstraints { make in
            make.left.equalTo(nameLabel)
            make.bottom.equalTo(-4)
        }
        
        addSubview(timeLabel)
        timeLabel.snp.makeConstraints { make in
            make.bottom.equalTo(idLabel)
            make.right.equalTo(-28)
        }
    }
    
    func setRoomInfo(avatar: String?, name: String?, id: String?, time: String?) {
        headImgView.image = UIImage.show_sceneImage(name: "show_room_bg_0")
        nameLabel.text = name
        idLabel.text = id
        let attachment = NSTextAttachment(image: UIImage.show_sceneImage(name: "show_live_duration")!)
        attachment.bounds = CGRect(x: -4, y: 0, width: 6, height: 6)
        let attriTipsImg = NSAttributedString(attachment: attachment)
        let attriTips = NSMutableAttributedString(attributedString: attriTipsImg)
        attriTips.append(NSAttributedString(string: "00:00:00"))
        timeLabel.attributedText = attriTips
    }
}
