//
//  Pure1v1BottomBar.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/25.
//
import UIKit

protocol Pure1v1RoomBottomBarDelegate: NSObjectProtocol {
    func onClickSettingButton()
    func onClickRttButton()
}

class Pure1v1RoomBottomBar: UIView {
    weak var delegate: Pure1v1RoomBottomBarDelegate?
    
    // 设置
    private lazy var settingButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.scene1v1Image(name: "live_setting"), for: .normal)
        button.addTarget(self, action: #selector(didClickSettingButton), for: .touchUpInside)
        return button
    }()
    
    // RTT
    private lazy var rttButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.scene1v1Image(name: "live_rtt"), for: .normal)
        button.addTarget(self, action: #selector(didClickRttButton), for: .touchUpInside)
        return button
    }()
    
    private var buttonArray = [UIButton]()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubviews(){
        buttonArray = [rttButton, settingButton]
        for button in buttonArray {
            addSubview(button)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let spacing: CGFloat = 15
        var right: CGFloat = aui_width - spacing
        for button in buttonArray.reversed() {
            addSubview(button)
            button.aui_size = CGSize(width: aui_height, height: aui_height)
            button.aui_right = right
            right = button.aui_left - spacing
        }
    }
    
    @objc private func didClickSettingButton() {
        delegate?.onClickSettingButton()
    }
    
    @objc private func didClickRttButton() {
        delegate?.onClickRttButton()
    }
    
    func setRttButtonView(enable: Bool) {
        if (enable) {
            rttButton.setImage(UIImage.scene1v1Image(name: "live_rtt_start"), for: .normal)
        } else {
            rttButton.setImage(UIImage.scene1v1Image(name: "live_rtt"), for: .normal)
        }
    }
}
