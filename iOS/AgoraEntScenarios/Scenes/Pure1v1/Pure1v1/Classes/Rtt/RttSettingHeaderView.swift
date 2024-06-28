//
//  RttSettingHeaderView.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/26.
//

import UIKit

class RttSettingHeaderView: UIView {
    
    var title: String? {
        didSet {
            titleLabel.text = title
        }
    }
    
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse7
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    
    private lazy var bgView: UIImageView = {
        let bgView = UIImageView()
        bgView.image = UIImage.scene1v1Image(name: "show_action_sheet_header")
        return bgView
    }()


    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        backgroundColor = .white
        
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
    }
}
