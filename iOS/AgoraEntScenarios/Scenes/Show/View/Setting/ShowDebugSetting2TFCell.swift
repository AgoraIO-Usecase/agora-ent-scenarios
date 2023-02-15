//
//  ShowDebugSetting2TFCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/11.
//

import UIKit

class ShowDebugSetting2TFCell: ShowSettingBaseCell {
    
    private var beginEditing: (()->())?
    private var tf1DidEndEditing: ((_ textField: UITextField)->())?
    private var tf2DidEndEditing: ((_ textField: UITextField)->())?

    private lazy var value1Textfield: UITextField = {
        return createTF()
    }()
    
    private lazy var value2Textfield: UITextField = {
       return createTF()
    }()
    
    private lazy var separatorLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse5
        label.font = .show_R_16
        label.textAlignment = .center
        return label
    }()
    
    override func createSubviews(){
        super.createSubviews()
    
        contentView.addSubview(value2Textfield)
        value2Textfield.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalToSuperview()
            make.width.equalTo(60)
            make.height.equalTo(40)
        }
        
        contentView.addSubview(separatorLabel)
        separatorLabel.snp.makeConstraints { make in
            make.right.equalTo(value2Textfield.snp.left)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(30)
        }
        
        contentView.addSubview(value1Textfield)
        value1Textfield.snp.makeConstraints { make in
            make.right.equalTo(separatorLabel.snp.left)
            make.centerY.equalToSuperview()
            make.width.equalTo(60)
            make.height.equalTo(40)
        }
        
        detailButton.isHidden = true
    }
    
    func setTitle(_ title: String?, value1: String?, value2: String?, separator: String?,tf1DidEndEditing: ((_ textField: UITextField)->())?,tf2DidEndEditing: ((_ textField: UITextField)->())?,beginEditing: (()->())?) {
        titleLabel.text = title
        value1Textfield.text = value1
        value2Textfield.text = value2
        separatorLabel.text = separator
        self.tf1DidEndEditing = tf1DidEndEditing
        self.tf2DidEndEditing = tf2DidEndEditing
        self.beginEditing = beginEditing
    }
}

extension ShowDebugSetting2TFCell: UITextFieldDelegate {
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
        if textField == value1Textfield {
            self.tf1DidEndEditing?(textField)
        }
        if textField == value2Textfield {
            self.tf2DidEndEditing?(textField)
        }
    }
}
