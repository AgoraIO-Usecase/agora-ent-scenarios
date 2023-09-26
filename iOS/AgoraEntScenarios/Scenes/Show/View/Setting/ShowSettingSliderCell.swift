//
//  ShowSettingSliderCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

protocol ShowSettingSliderCellDelegate: NSObjectProtocol {
        
    func onCellSliderValueChanged(value: Float, at index: IndexPath)
}

class ShowSettingSliderCell: ShowSettingBaseCell {
    
    weak var delegate: ShowSettingSliderCellDelegate?
    
    var indexPath: IndexPath?
    
    private var currentValue: Float = 0
    
    private let valueLabel = UILabel()
    
    private let slider = UISlider()
    
    override func createSubviews(){
        super.createSubviews()
        
        createViews()
        createConstrians()
    }
    
    func setTitle(_ title: String, value: Float, minValue: Float, maxValue: Float) {
        titleLabel.text = title
        slider.isHidden = false
        valueLabel.isHidden = false
        slider.minimumValue = minValue
        slider.maximumValue = maxValue
        slider.value = value
        valueLabel.text = String(format: "%.0f", slider.value)
        currentValue = value
    }
    
    @objc private func sliderValueDidChanged() {
        currentValue = slider.value
        valueLabel.text = String(format: "%.0f", slider.value)
    }
    
    @objc private func sliderDidTouchUp() {
        currentValue = slider.value
        valueLabel.text = String(format: "%.0f", slider.value)
        if let i = indexPath {
            delegate?.onCellSliderValueChanged(value: slider.value, at: i)
        }
    }
}

private extension ShowSettingSliderCell {
    
    func createViews() {
        detailButton.isHidden = true
        
        valueLabel.textColor = .show_Ellipse5
        valueLabel.font = .show_R_14
        contentView.addSubview(valueLabel)
        
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
        slider.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalToSuperview()
            make.width.equalTo(150)
            make.height.equalTo(30)
        }
        valueLabel.snp.makeConstraints { make in
            make.right.equalTo(slider.snp.left).offset(-15)
            make.centerY.equalToSuperview()
        }
    }
}


