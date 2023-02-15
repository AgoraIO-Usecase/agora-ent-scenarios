//
//  ShowSettingActionSheetFooterView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/15.
//

import UIKit

class ShowSettingActionSheetFooterView: UIView {
    
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .white
        return bgView
    }()
    
    lazy var button: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("show_advance_setting_action_sheet_cancel".show_localized, for: .normal)
        button.setTitleColor(.show_Ellipse6, for: .normal)
        button.titleLabel?.font = .show_R_16
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
