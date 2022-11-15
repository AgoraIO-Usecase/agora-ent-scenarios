//
//  ShowRoomMembersCountView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit

private let viewHeight: CGFloat = 32

class ShowRoomMembersCountView: UIView {
    
    var count = 1 {
        didSet{
            if count < 1 {
                count = 1
            }
            
            if count < 1000 {
                numberLabel.text = "\(count)"
            }else if count < 10000 {
                numberLabel.text = String(format: "%.1fK", Float(count) / 1000.0)
            }else{
                numberLabel.text = String(format: "%.1fW", Float(count) / 10000.0)
            }
        }
    }
    
    // 背景
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = .show_room_info_cover
        view.layer.cornerRadius = viewHeight * 0.5
        view.layer.masksToBounds = true
        return view
    }()
    
    // 头像
    private lazy var imgView: UIImageView = {
        let imgView = UIImageView()
        imgView.image = UIImage.show_sceneImage(name: "show_live_member")
        imgView.contentMode = .scaleAspectFill
        return imgView
    }()
    
    // 数量
    private lazy var numberLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_main_text
        label.alpha = 0.8
        label.font = .show_R_9
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
            make.width.height.equalTo(viewHeight)
        }
        
        addSubview(imgView)
        imgView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(4)
        }
        
        addSubview(numberLabel)
        numberLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(-4)
        }
    }
}
