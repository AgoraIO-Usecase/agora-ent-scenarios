//
//  ShowPresettingHeaderView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/25.
//

import UIKit

class ShowPresettingHeaderView: UITableViewHeaderFooterView {
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse7
        label.font = .show_S_18
        return label
    }()
    
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_chat_input_text
        label.font = .show_R_13
        label.numberOfLines = 0
        return label
    }()
    
    private lazy var bgImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.backgroundColor = .show_preset_bg
        return imgView
    }()
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    
    private func createSubviews(){
        
        contentView.addSubview(bgImgView)
        bgImgView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(15)
            make.bottom.equalTo(0).priority(.high)
        }
        
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.left.equalTo(40)
            make.top.equalTo(35)
        }
        
        contentView.addSubview(descLabel)
        descLabel.snp.makeConstraints { make in
            make.left.equalTo(40)
            make.top.equalTo(70)
            make.right.equalTo(-40)
            make.bottom.equalTo(-20).priority(.high)
        }
    }
    
    func setTitle(_ title: String, desc: String) {
        titleLabel.text = title
        descLabel.text = desc
    }
}
