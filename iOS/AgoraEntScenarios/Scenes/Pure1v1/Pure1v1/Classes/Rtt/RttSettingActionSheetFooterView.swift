//
//  RttSettingActionSheetFooterView.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/26.
//

import UIKit

class RttSettingActionSheetFooterView: UIView {
    
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .white
        return bgView
    }()
    
    lazy var button: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("rtt_cancel".pure1v1Localization(), for: .normal)
        button.setTitleColor(.show_Ellipse6, for: .normal)
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 16)
        return button
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        backgroundColor = .show_footer_separator
        
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.top.equalTo(8)
        }
        
        addSubview(button)
        button.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(28)
            make.width.equalToSuperview()
        }
    }
}
