//
//  ShowTo1v1RoomBottomBar.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//
import UIKit

@objc enum RoomBottomBarType: Int {
    case more = 1000
    case call
}

@objc protocol RoomBottomBarDelegate: NSObjectProtocol {
    func onClick(actionType: RoomBottomBarType)
}

class RoomBottomBar: UIView {
    weak var delegate: RoomBottomBarDelegate?
    var buttonTypes: [RoomBottomBarType] = [.more] {
        didSet {
            _reloadSubviews()
        }
    }
    
    // 设置
    private lazy var settingButton: UIButton = {
        let button = UIButton(type: .custom)
        button.aui_size = CGSize(width: 38, height: 38)
        button.tag = RoomBottomBarType.more.rawValue
        button.setImage(UIImage.sceneImage(name: "live_setting"), for: .normal)
        button.addTarget(self, action: #selector(didClick(buton: )), for: .touchUpInside)
        return button
    }()
    
    //call
    private lazy var callButton: TouchWaveTextButton = {
        let button = TouchWaveTextButton()
        button.tag = RoomBottomBarType.call.rawValue
        button.content = "call_broadcaster_private".showTo1v1Localization()
        button.sizeToFit()
        button.addTarget(self, action: #selector(didClick(buton: )), for: .touchUpInside)
        return button
    }()
    
    private var buttonArray = [UIControl]()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _reloadSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _reloadSubviews(){
        for button in buttonArray {
            button.removeFromSuperview()
        }
        buttonArray.removeAll()
        buttonTypes.forEach { type in
            switch type {
            case .more:
                self.buttonArray.append(settingButton)
            case .call:
                self.buttonArray.append(callButton)
            }
        }
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
            button.aui_right = right
            right = button.aui_left - spacing
        }
    }
    
    @objc private func didClick(buton:UIControl) {
        guard let type = RoomBottomBarType(rawValue: buton.tag) else {return}
        delegate?.onClick(actionType: type)
    }
}
