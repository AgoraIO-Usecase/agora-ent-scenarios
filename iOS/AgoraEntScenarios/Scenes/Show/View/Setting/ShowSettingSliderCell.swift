//
//  ShowSettingSliderCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

class ShowSettingSliderCell: ShowSettingBaseCell {
    
    var sliderValueChangingAction: ((_ value: Float)->())?   // 正在变化
    var sliderValueChangedAction: ((_ value: Float)->())?    // 变化结束
    
    private var currentValue: Float = 0
    
    private let aSwitch = UISwitch()
    
    private let valueLabel = UILabel()
    
    private let slider = UISlider()
    
    override func createSubviews(){
        super.createSubviews()
        
        createViews()
        createConstrians()
    }
    
    func setTitle(_ title: String, value: Float, minValue: Float, maxValue: Float,sliderValueChangingAction: ((_ value: Float)->())?,sliderValueChangedAction: ((_ value: Float)->())?) {
        titleLabel.text = title
        slider.minimumValue = minValue
        slider.maximumValue = maxValue
        slider.value = value
        valueLabel.text = String(format: "%.0f", slider.value)
        currentValue = value
        self.sliderValueChangedAction = sliderValueChangedAction
        self.sliderValueChangingAction = sliderValueChangingAction
    }
    
    private func setSliderEnabled(isEnabled: Bool) {
        slider.isEnabled = isEnabled
        if (isEnabled) {
            slider.value = currentValue
            valueLabel.text = String(format: "%.0f", currentValue)
        } else {
            slider.value = slider.minimumValue
            valueLabel.text = "0"
        }
    }
    
    @objc private func sliderValueDidChanged() {
        currentValue = slider.value
        valueLabel.text = String(format: "%.0f", slider.value)
        self.sliderValueChangingAction?(slider.value)
    }
    
    @objc private func sliderDidTouchUp() {
        currentValue = slider.value
        valueLabel.text = String(format: "%.0f", slider.value)
        self.sliderValueChangedAction?(slider.value)
    }
    
    @objc private func switchValueChanged() {
        setSliderEnabled(isEnabled: !aSwitch.isOn)
    }
}

private extension ShowSettingSliderCell {
    
    func createViews() {
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
        aSwitch.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.top.equalTo(10)
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


