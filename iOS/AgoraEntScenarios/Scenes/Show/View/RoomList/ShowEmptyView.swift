//
//  ShowEmptyView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit

class ShowEmptyView: UIView {
    
    var imageView: UIImageView!
    var descLabel: UILabel!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        imageView = UIImageView()
        imageView.image = UIImage.show_sceneImage(name: "show_list_empty")
        addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalToSuperview()
        }
        
        descLabel = UILabel()
        descLabel.textColor = .show_empty_desc
        descLabel.font = .show_R_14
        descLabel.text = "room_list_empty_desc".show_localized
        addSubview(descLabel)
        descLabel.snp.makeConstraints { make in
            make.bottom.equalToSuperview()
            make.centerX.equalToSuperview()
            make.top.equalTo(imageView.snp.bottom).offset(40)
        }
    }
}
