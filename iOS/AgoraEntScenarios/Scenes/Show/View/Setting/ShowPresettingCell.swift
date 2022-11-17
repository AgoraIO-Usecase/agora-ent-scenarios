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
        label.font = .show_navi_title
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
        indicatorImgView.image = UIImage.show_sceneImage(name: "show_presetting_selected_indicator")?.resizableImage(withCapInsets: UIEdgeInsets(top: 30, left: 30, bottom: 30, right: 30), resizingMode: .stretch)
        return indicatorImgView
    }()
    
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
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
            make.left.equalTo(40)
            make.top.equalTo(14)
        }
        
        contentView.addSubview(descLabel)
        descLabel.snp.makeConstraints { make in
            make.left.equalTo(40)
            make.top.equalTo(42)
            make.right.equalTo(-40)
        }
        
    }
    
    func setTitle(_ title: String, desc: String, selected: Bool) {
        titleLabel.text = title
        descLabel.text = desc
        indicatorView.isHidden = !selected
    }
}
