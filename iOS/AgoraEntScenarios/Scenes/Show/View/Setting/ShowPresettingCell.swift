//
//  ShowPresettingCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

class ShowPresettingCell: UITableViewCell {

    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_zi02
        label.font = .show_M_12
        return label
    }()
    
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_chat_input_text
        label.font = .show_R_12
        label.numberOfLines = 0
        return label
    }()
    
    // 选中标识
    private lazy var indicatorView: UIImageView = {
        let indicatorImgView = UIImageView()
        indicatorImgView.isHidden = true
        indicatorImgView.image = UIImage.show_sceneImage(name: "show_presetting_selected_indicator")?.resizableImage(withCapInsets: UIEdgeInsets(top: 20, left: 30, bottom: 20, right: 30), resizingMode: .stretch)
        return indicatorImgView
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
    }
    
    func createSubviews(){
        
        contentView.addSubview(indicatorView)
        indicatorView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.bottom.equalToSuperview()
        }
        
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalTo(40)
        }
        
        contentView.addSubview(descLabel)
        descLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalTo(titleLabel.snp.right).offset(20)
        }
        
    }
    
    func setTitle(_ title: String, desc: String) {
        titleLabel.text = title
        descLabel.text = desc
    }
}
