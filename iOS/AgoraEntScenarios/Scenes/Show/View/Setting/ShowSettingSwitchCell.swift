//
//  ShowSettingSwitchCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

class ShowSettingSwitchCell: ShowSettingBaseCell {
    
    private var valueChangedAction: ((_ isOn: Bool)->())?
    
    private lazy var aSwitch: UISwitch = {
        let aSwitch = UISwitch()
        aSwitch.addTarget(self, action: #selector(switchValueChanged), for: .touchUpInside)
        return aSwitch
    }()
    
    private lazy var stateLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse6
        label.font = .show_R_14
        return label
    }()

    override func createSubviews(){
        super.createSubviews()
        contentView.addSubview(aSwitch)
        aSwitch.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalTo(titleLabel)
        }
        
        contentView.addSubview(stateLabel)
        stateLabel.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalTo(titleLabel)
        }
        detailButton.isHidden = AppContext.shared.isDebugMode
    }
    
    func setTitle(_ title: String, enable: Bool = true, isOn: Bool, valueChangedAction: ((_ isOn: Bool)->())?, detailButtonAction: (()->())?) {
        titleLabel.text = title
        aSwitch.isOn = isOn
        stateLabel.text = isOn ? "已开启" : "已关闭"
        aSwitch.isHidden = !enable
        stateLabel.isHidden = enable
        self.valueChangedAction = valueChangedAction
        self.clickDetailButonAction = detailButtonAction
    }
    
    @objc private func switchValueChanged(){
        self.valueChangedAction?(aSwitch.isOn)
    }
    
}
