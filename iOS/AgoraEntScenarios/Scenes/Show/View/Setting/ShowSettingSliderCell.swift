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
    
    private var currentValue: Float = 0 {
        didSet {
            var limitValue = min(currentValue, slider.maximumValue)
            limitValue = max(limitValue, slider.maximumValue)
            valueLabel.text = String(format: "%.0f", slider.value)
        }
    }
    
    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse5
        label.font = .show_R_14
        return label
    }()

    private lazy var slider: UISlider = {
        let slider = UISlider()
        slider.minimumTrackTintColor = .show_zi03
        slider.maximumTrackTintColor = .show_Ellipse2
        slider.addTarget(self, action: #selector(sliderValueDidChanged), for: .valueChanged)
        slider.addTarget(self, action: #selector(sliderDidTouchUp), for: [.touchUpInside, .touchUpOutside, .touchDragExit])
        return slider
    }()
    
    override func createSubviews(){
        super.createSubviews()
        
        contentView.addSubview(slider)
        slider.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalTo(titleLabel)
            make.width.equalTo(150)
            make.height.equalTo(30)
        }
        
        contentView.addSubview(valueLabel)
        valueLabel.snp.makeConstraints { make in
            make.right.equalTo(slider.snp.left).offset(-15)
            make.centerY.equalTo(titleLabel)
        }
    }
    
    func setTitle(_ title: String, value: Float, minValue: Float, maxValue: Float,sliderValueChangingAction: ((_ value: Float)->())?,sliderValueChangedAction: ((_ value: Float)->())?) {
        titleLabel.text = title
        slider.minimumValue = minValue
        slider.maximumValue = maxValue
        slider.value = value
        currentValue = value
        self.sliderValueChangedAction = sliderValueChangedAction
        self.sliderValueChangingAction = sliderValueChangingAction
    }
}

extension ShowSettingSliderCell {
    
    @objc private func sliderValueDidChanged() {
        currentValue = slider.value
        self.sliderValueChangingAction?(slider.value)
    }
    
    @objc private func sliderDidTouchUp() {
        currentValue = slider.value
        self.sliderValueChangedAction?(slider.value)
    }
}


