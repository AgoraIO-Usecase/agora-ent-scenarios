//
//  SoundCardSwitchCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/12.
//

import UIKit

class SoundCardSwitchCell: UITableViewCell {
    var titleLabel: UILabel!
    var detailLabel: UILabel!
    var numLable: UITextField!
    var slider: UISlider!
    var valueBlock: ((Double)-> Void)?
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        titleLabel = UILabel()
        titleLabel.text = Bundle.localizedString("ktv_vol_gain", bundleName: "KtvResource")
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        self.contentView.addSubview(titleLabel)
        
        detailLabel = UILabel()
        detailLabel.text = Bundle.localizedString("ktv_vol_gain_level", bundleName: "KtvResource")
        detailLabel.font = UIFont.systemFont(ofSize: 12)
        detailLabel.textColor = .lightGray
        self.contentView.addSubview(detailLabel)
        
        slider = UISlider()
        slider.value = 0.5
        self.contentView.addSubview(slider)
        
        numLable = UITextField()
        numLable.font = UIFont.systemFont(ofSize: 13)
        numLable.text = "4.0"
        numLable.keyboardType = .decimalPad
        numLable.textColor = .gray
        numLable.textAlignment = .center
        self.contentView.addSubview(numLable)
        
        let toolbar = UIToolbar()
        toolbar.sizeToFit()
        toolbar.isUserInteractionEnabled = true
        let doneButton = UIBarButtonItem(barButtonSystemItem: .done, target: self, action: #selector(doneButtonTapped))
        toolbar.items = [doneButton]
        numLable.inputAccessoryView = toolbar
        
        slider.addTarget(self, action: #selector(gain), for: .valueChanged)
        slider.addTarget(self, action: #selector(gainSend), for: .touchUpInside)
        numLable.delegate = self
    }
    
    @objc func doneButtonTapped() {
       superview?.endEditing(true)
    }
    
    
    
    @objc func gain() {
        let gain = slider.value
        numLable.text = String(format: "%.1f", Double(calculateLevel(for: gain)) * 0.1)
    }

    @objc func gainSend() {
        let gain = slider.value
        let level = String(format: "%.1f", Double(calculateLevel(for: gain)) * 0.1)
        print("send lev:\(round(Double(calculateLevel(for: gain)) * 0.1, decimalPlaces: 1))")
        guard let valueBlock = valueBlock else {return}
        valueBlock(round(Double(calculateLevel(for: gain)) * 0.1, decimalPlaces: 1))
    }
    
    func round(_ value: Double, decimalPlaces: Int) -> Double {
        let multiplier = pow(10, Double(decimalPlaces))
        var newValue = value
        newValue = Darwin.round(value * multiplier) / multiplier
        return newValue
    }
    
    func calculateLevel(for value: Float) -> Int {
        let stepSize: Float = 1/40

        if value <= 0 {
            return 0
        } else if value >= 1 {
            return 40
        } else {
            let level = Int(value / stepSize)
            return level
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = CGRect(x: 20, y: 5, width: 80, height: 18)
        detailLabel.frame = CGRect(x: 20, y: 25, width: 180, height: 18)
        numLable.frame = CGRect(x: self.bounds.size.width - 60, y: 16, width: 60, height: 20)
        slider.frame = CGRect(x: self.bounds.size.width - 180, y: 11, width: 120, height: 30)
    }

}

extension SoundCardSwitchCell: UITextFieldDelegate {
    func textFieldDidEndEditing(_ textField: UITextField) {
            // 在此处执行输入结束后的操作
            if let text = textField.text {
                guard let num = Float(text) else {return}
                if num > 4.0 || num < 0 {
                    textField.text = "4.0"
                    //同时更新slider
                    slider.value = 1.0
                } else {
                    slider.value = num / 4.0
                }
                gainSend()
            }
    }
    
}
