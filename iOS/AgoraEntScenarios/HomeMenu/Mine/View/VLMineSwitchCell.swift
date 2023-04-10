//
//  VLMineSwitchCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/6.
//

import UIKit

class VLMineSwitchCell: UITableViewCell {
    
    
    private var valueChangedAction: ((_ isOn: Bool)->())?
    
    private lazy var aSwitch: UISwitch = {
        let aSwitch = UISwitch()
        aSwitch.addTarget(self, action: #selector(switchValueChanged), for: .touchUpInside)
        return aSwitch
    }()
    
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .gray
        label.font = .systemFont(ofSize: 14)
        return label
    }()
    
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func createSubviews(){
        
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.centerY.equalToSuperview()
        }
        
        contentView.addSubview(aSwitch)
        aSwitch.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalTo(titleLabel)
        }
    }
    
    @objc func setTitle(_ title: String, isOn: Bool, valueChangedAction: ((_ isOn: Bool)->())?) {
        titleLabel.text = title
        aSwitch.isOn = isOn
        self.valueChangedAction = valueChangedAction
    }
    
    @objc private func switchValueChanged(){
        self.valueChangedAction?(aSwitch.isOn)
    }
}
