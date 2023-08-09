//
//  ShowTo1v1RoomBottomBar.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//
import UIKit

protocol RoomBottomBarDelegate: NSObjectProtocol {
    func onClickSettingButton()
}

class RoomBottomBar: UIView {
    weak var delegate: RoomBottomBarDelegate?
    
    // 设置
    private lazy var settingButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "live_setting"), for: .normal)
        button.addTarget(self, action: #selector(didClickSettingButton), for: .touchUpInside)
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
        buttonArray = [settingButton]
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
}
