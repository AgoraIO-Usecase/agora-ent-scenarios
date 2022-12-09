//
//  ShowSettingSwitchCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

class ShowSettingSwitchCell: ShowSettingBaseCell {
    
    private var valueChangedAction: ((_ isOn: Bool)->())?
    private var clickDetailButonAction: (()->())?
    
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
    
    private lazy var detailButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.show_sceneImage(name: "show_setting_detail"), for: .normal)
        button.addTarget(self, action: #selector(didClickDetailButton), for: .touchUpInside)
        return button
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
        
        contentView.addSubview(detailButton)
        detailButton.snp.makeConstraints { make in
            make.centerY.equalTo(titleLabel)
            make.left.equalTo(titleLabel.snp.right).offset(6)
        }
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
    
    @objc private func didClickDetailButton(){
        self.clickDetailButonAction?()
    }
    
}
