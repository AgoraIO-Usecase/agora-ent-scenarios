//
//  JoyStepper.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/12/19.
//

import UIKit

class JoyStepper: UIView {
    var minValue: Int = 1 {
        didSet {
            resetUI()
        }
    }
    var maxValue: Int = 20 {
        didSet {
            resetUI()
        }
    }
    var currentValue: Int = 1 {
        didSet {
            resetUI()
        }
    }
    private lazy var plusButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("+", for: .normal)
        button.titleLabel?.font = .joy_R_16
        button.setTitleColor(.joy_stepper, for: .normal)
        button.setTitleColor(.joy_stepper_disable, for: .disabled)
        button.addTarget(self, action: #selector(plusAction), for: .touchUpInside)
        return button
    }()
    private lazy var minusButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("-", for: .normal)
        button.titleLabel?.font = .joy_R_16
        button.setTitleColor(.joy_stepper, for: .normal)
        button.setTitleColor(.joy_stepper_disable, for: .disabled)
        button.addTarget(self, action: #selector(minusAction), for: .touchUpInside)
        return button
    }()
    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.font = .joy_R_16
        label.textColor = .joy_stepper
        label.textAlignment = .center
        return label
    }()
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(plusButton)
        addSubview(minusButton)
        addSubview(valueLabel)
        resetUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let buttonW = 32.0
        layer.cornerRadius = aui_height / 2
        layer.borderWidth = 1
        layer.borderColor = UIColor.joy_stepper_disable.cgColor
        minusButton.frame = CGRect(x: 0, y: 0, width: buttonW, height: aui_height)
        plusButton.frame = CGRect(x: aui_width - 32, y: 0, width: buttonW, height: aui_height)
        valueLabel.frame = CGRect(x: buttonW, y: 0, width: aui_width - buttonW * 2, height: aui_height)
        valueLabel.layer.borderWidth = 1
        valueLabel.layer.borderColor = layer.borderColor
    }
    
    private func resetUI() {
        valueLabel.text = "\(currentValue)"
        plusButton.isEnabled = currentValue < maxValue ? true : false
        minusButton.isEnabled = currentValue > minValue ? true : false
    }
    
    @objc func plusAction() {
        currentValue += 1
    }
    
    @objc func minusAction() {
        currentValue -= 1
    }
}
