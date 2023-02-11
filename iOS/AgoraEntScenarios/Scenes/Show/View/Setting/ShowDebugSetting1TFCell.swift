//
//  ShowDebugSetting1TFCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/11.
//

import UIKit

class ShowDebugSetting1TFCell: ShowSettingBaseCell {
    
    private var beginEditing: (()->())?
    private var tfDidEndEditing: ((_ textField: UITextField)->())?
    
    private lazy var valueTextfield: UITextField = {
        return createTF()
    }()
    
    private lazy var unitLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse5
        label.font = .show_R_14
        return label
    }()
    
    override func createSubviews(){
        super.createSubviews()
    
        contentView.addSubview(valueTextfield)
        valueTextfield.snp.makeConstraints { make in
            make.right.equalTo(-60)
            make.centerY.equalToSuperview()
            make.width.equalTo(60)
            make.height.equalTo(40)
        }
        
        contentView.addSubview(unitLabel)
        unitLabel.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalToSuperview()
        }
        detailButton.isHidden = true
    }
    
    func setTitle(_ title: String?, value: String?, unit: String?, tfDidEndEditing: ((_ textField: UITextField)->())?,beginEditing: (()->())?) {
        titleLabel.text = title
        valueTextfield.text = value
        unitLabel.text = unit
        self.tfDidEndEditing = tfDidEndEditing
        self.beginEditing = beginEditing
    }
}

extension ShowDebugSetting1TFCell: UITextFieldDelegate {
    private func createTF() -> UITextField {
        let tf = UITextField()
        tf.borderStyle = .roundedRect
        tf.font = .show_R_14
        tf.textColor = .show_Ellipse5
        tf.delegate = self
        tf.keyboardType = .decimalPad
        return tf
    }
    
    func textFieldDidBeginEditing(_ textField: UITextField) {
        self.beginEditing?()
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        self.tfDidEndEditing?(textField)
    }
}
