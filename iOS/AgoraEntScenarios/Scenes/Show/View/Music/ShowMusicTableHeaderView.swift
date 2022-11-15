//
//  ShowMusicTableHeaderView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/9.
//

import UIKit

class ShowMusicTableHeaderView: UIView {

    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_main_text
        label.font = .show_navi_title
        label.text = "create_live_music_title".show_localized
        return label
    }()
    
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = .show_main_text
        view.alpha = 0.1
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        backgroundColor = .clear
        addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.top.equalTo(25)
        }
        
        addSubview(lineView)
        lineView.snp.makeConstraints { make in
            make.left.equalTo(titleLabel)
            make.right.equalTo(-20)
            make.bottom.equalToSuperview()
            make.height.equalTo(1)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        setRoundingCorners([.topLeft, .topRight], radius: 20)
    }
}
