//
//  ShowSettingSliderCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

protocol ShowSettingBitrateCellDelegate: NSObjectProtocol {
    
    func onAutoBitRateChanged(isOn: Bool)
    
    func onBitRateValueChanged(value: Float)
}

class ShowSettingBitrateCell: ShowSettingBaseCell {
    
    weak var delegate: ShowSettingBitrateCellDelegate?
    
    private var currentValue: Float = 0
    
    private let aSwitch = UISwitch()
    
    
    private let valueLabel = UILabel()
    
    private let slider = UISlider()
    
    override func createSubviews(){
        super.createSubviews()
        
        createViews()
        createConstrians()
    }
    
    func setTitle(_ title: String, value: Float, minValue: Float, maxValue: Float) {
        titleLabel.text = title
        if (value == 0) {
            aSwitch.isOn = true
            slider.isHidden = true
            valueLabel.isHidden = true
        } else {
            aSwitch.isOn = false
            slider.isHidden = false
            valueLabel.isHidden = false
            slider.minimumValue = minValue
            slider.maximumValue = maxValue
            slider.value = value
            valueLabel.text = String(format: "%.0f kbps", slider.value)
            currentValue = value
        }
    }
    
    @objc private func sliderValueDidChanged() {
        currentValue = slider.value
        valueLabel.text = String(format: "%.0f kbps", slider.value)
    }
    
    @objc private func sliderDidTouchUp() {
        currentValue = slider.value
        valueLabel.text = String(format: "%.0f kbps", slider.value)
        delegate?.onBitRateValueChanged(value: slider.value)
    }
    
    @objc private func switchValueChanged() {
        delegate?.onAutoBitRateChanged(isOn: aSwitch.isOn)
    }
}

private extension ShowSettingBitrateCell {
    
    func createViews() {
        detailButton.isHidden = false
        
        valueLabel.textColor = .show_Ellipse5
        valueLabel.font = .show_R_14
        contentView.addSubview(valueLabel)
        
        aSwitch.addTarget(self, action: #selector(switchValueChanged), for: .touchUpInside)
        contentView.addSubview(aSwitch)
        
        slider.minimumTrackTintColor = .show_zi03
        slider.maximumTrackTintColor = .show_Ellipse2
        slider.addTarget(self, action: #selector(sliderValueDidChanged), for: .valueChanged)
        slider.addTarget(self, action: #selector(sliderDidTouchUp), for: [.touchUpInside, .touchUpOutside])
        contentView.addSubview(slider)
    }
    
    func createConstrians() {
        titleLabel.snp.remakeConstraints { make in
            make.top.equalTo(14)
            make.left.equalTo(20)
        }
        aSwitch.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalTo(titleLabel)
        }
        slider.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.top.equalTo(aSwitch.snp.bottom).offset(10)
            make.width.equalTo(150)
            make.height.equalTo(30)
        }
        valueLabel.snp.makeConstraints { make in
            make.right.equalTo(slider.snp.left).offset(-15)
            make.centerY.equalTo(slider)
        }
    }
}


