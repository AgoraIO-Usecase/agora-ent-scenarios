//
//  ShowPresettingCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

class ShowPresettingCell: UITableViewCell {

    private lazy var bgImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.backgroundColor = .show_preset_bg
        return imgView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_zi02
        label.font = .show_R_14
        label.textColor = .show_Ellipse7
        return label
    }()
    
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse5
        label.font = .show_R_12
        label.numberOfLines = 0
        return label
    }()
    
    private lazy var whiteBgView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 10
        view.layer.borderColor = UIColor.show_zi02.cgColor
        view.layer.borderWidth = 0
        view.layer.masksToBounds = true
        return view
    }()
    
    // 选中标识
    private lazy var indicatorView: UIImageView = {
        let indicatorImgView = UIImageView()
        indicatorImgView.isHidden = true
        indicatorImgView.image = UIImage.show_sceneImage(name: "show_preset_select_indicator")
        return indicatorImgView
    }()
    
    // 苹果标识
    private lazy var appleIconImgView: UIImageView = {
        let imgView = UIImageView()
//        imgView.isHidden = true
        imgView.image = UIImage.show_sceneImage(name: "show_preset_apple_icon")
        return imgView
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        indicatorView.isHidden = !selected
        whiteBgView.layer.borderWidth = selected ? 1 : 0
    }
    
    func createSubviews(){
        
        contentView.addSubview(bgImgView)
        bgImgView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.bottom.equalToSuperview()
        }
        
        contentView.addSubview(whiteBgView)
        whiteBgView.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets(top: 5, left: 40, bottom: 5, right: 40))
        }
        
        contentView.addSubview(indicatorView)
        indicatorView.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.right.equalTo(-54)
        }
        
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalTo(55)
        }
        
        contentView.addSubview(appleIconImgView)
        appleIconImgView.snp.makeConstraints { make in
            make.left.equalTo(120)
            make.centerY.equalToSuperview()
        }
        
        contentView.addSubview(descLabel)
        descLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalTo(appleIconImgView.snp.right).offset(8)
        }
        
    }
    
    func setTitle(_ title: String, desc: String) {
        titleLabel.text = title
        descLabel.text = desc
    }
}
