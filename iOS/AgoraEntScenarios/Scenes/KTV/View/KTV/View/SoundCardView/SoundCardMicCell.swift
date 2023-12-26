//
//  SoundCardMicCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/12.
//

import UIKit

class SoundCardMicCell: UITableViewCell {
    var titleLabel: UILabel!
    var detailLabel: UILabel!
    var numLable: UILabel!
    var slider: UISlider!
    var valueBlock: ((Int)-> Void)?
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        accessoryType = .none
        selectionStyle = .none
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupValue(_ v: Int) {
        slider.value = Float(v)
        preset()
    }
    
    private func layoutUI() {
        titleLabel = UILabel()
        titleLabel.text = Bundle.localizedString("ktv_mic_type", bundleName: "KtvResource")
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        self.contentView.addSubview(titleLabel)
        
        detailLabel = UILabel()
        detailLabel.text = Bundle.localizedString("ktv_choose_sc_params", bundleName: "KtvResource")
        detailLabel.font = UIFont.systemFont(ofSize: 12)
        detailLabel.textColor = .lightGray
        self.contentView.addSubview(detailLabel)
        
        numLable = UILabel()
        numLable.font = UIFont.systemFont(ofSize: 12)
        numLable.text = Bundle.localizedString("ktv_close_aec", bundleName: "KtvResource")
        numLable.textColor = .gray
        numLable.textAlignment = .center
        self.contentView.addSubview(numLable)
        
        slider = UISlider()
        slider.value = 4
        slider.maximumValue = 15
        slider.minimumValue = -1
        self.contentView.addSubview(slider)
        
        slider.addTarget(self, action: #selector(preset), for: .valueChanged)
        slider.addTarget(self, action: #selector(presetSend), for: .touchUpInside)
    }
    
    @objc func preset() {
        let preset = Int(slider.value)
        if (preset == -1) {
            numLable.text = Bundle.localizedString("ktv_close_aec", bundleName: "KtvResource")
        } else {
            numLable.text = String(preset)
        }
    }

    @objc func presetSend() {
        let preset = Int(slider.value)
        guard let valueBlock = valueBlock else {return}
        valueBlock(preset)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = CGRect(x: 20, y: 5, width: 80, height: 18)
        detailLabel.frame = CGRect(x: 20, y: 25, width: 200, height: 18)
        numLable.frame = CGRect(x: self.bounds.size.width - 60, y: 16, width: 60, height: 20)
        slider.frame = CGRect(x: self.bounds.size.width - 180, y: 11, width: 120, height: 30)
    }
}
