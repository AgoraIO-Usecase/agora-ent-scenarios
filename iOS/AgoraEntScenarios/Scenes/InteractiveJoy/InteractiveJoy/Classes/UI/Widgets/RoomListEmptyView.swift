//
//  RoomListEmptyView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit

class RoomListEmptyView: UIView {
    
    private let imageView: UIImageView = UIImageView()
    private var descLabel: UILabel!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews() {
        imageView.image = UIImage.sceneImage(name: "joy_list_empty")
        addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.height.equalTo(178)
            make.width.equalTo(254)
            make.bottom.equalTo(self.snp.centerY)
        }
        
        descLabel = UILabel()
        descLabel.numberOfLines = 0
        descLabel.textAlignment = .center
        descLabel.textColor = .joy_empty_desc
        descLabel.font = .joy_R_14
        descLabel.text = "room_list_empty_desc".joyLocalization()
        addSubview(descLabel)
        descLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(imageView.snp.bottom).offset(40)
        }
    }
}
