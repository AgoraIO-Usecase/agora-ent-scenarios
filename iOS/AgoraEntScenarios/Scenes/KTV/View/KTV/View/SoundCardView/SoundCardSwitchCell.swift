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
    var numLable: UILabel!
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
        titleLabel.text = "增益调节"
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        self.contentView.addSubview(titleLabel)
        
        detailLabel = UILabel()
        detailLabel.text = "调节声音信号的增益倍数"
        detailLabel.font = UIFont.systemFont(ofSize: 12)
        detailLabel.textColor = .lightGray
        self.contentView.addSubview(detailLabel)
        
        slider = UISlider()
        slider.value = 0.5
        self.contentView.addSubview(slider)
        
        numLable = UILabel()
        numLable.font = UIFont.systemFont(ofSize: 13)
        numLable.text = "3.0f"
        numLable.textColor = .gray
        numLable.textAlignment = .right
        self.contentView.addSubview(numLable)
        
        slider.addTarget(self, action: #selector(gain), for: .valueChanged)
        slider.addTarget(self, action: #selector(gainSend), for: .touchUpInside)
    }
    
    @objc func gain() {
        let gain = slider.value
        numLable.text = String(format: "%.1ff", Double(calculateLevel(for: gain)) * 0.1)
    }

    @objc func gainSend() {
        let gain = slider.value
        let level = String(format: "%.1ff", Double(calculateLevel(for: gain)) * 0.1)
        let levNum = Double(level)
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
        let stepSize: Float = 1/30

        if value <= 0 {
            return 0
        } else if value >= 1 {
            return 30
        } else {
            let level = Int(value / stepSize)
            return level
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = CGRect(x: 20, y: 5, width: 80, height: 18)
        detailLabel.frame = CGRect(x: 20, y: 25, width: 180, height: 18)
        numLable.frame = CGRect(x: self.bounds.size.width - 50, y: 16, width: 30, height: 20)
        slider.frame = CGRect(x: self.bounds.size.width - 180, y: 11, width: 130, height: 30)
    }

}
