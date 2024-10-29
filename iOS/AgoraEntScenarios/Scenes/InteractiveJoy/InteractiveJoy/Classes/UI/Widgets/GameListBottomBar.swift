//
//  BottomBar.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import UIKit

enum BottomBarClickType {
    case left, right
}

class GameListBottomBar: UIView {
    var gameSupplierCallback: ((BottomBarClickType) -> ())? = nil
    
    private lazy var line: UIView = {
        let l = UIView()
        l.backgroundColor = UIColor.gray
        l.alpha = 0.25
        return l
    }()
    
    var currentSelectText: String = LanguageManager.localValue(key: "game_list_huran_title")
    
    private lazy var leftButton: UIButton = {
        let b = UIButton(type: .custom)
        b.setTitle(LanguageManager.localValue(key: "game_list_vender_title"), for: .normal)
        b.setTitleColor(UIColor.joy_zi05, for: .normal)
        b.titleLabel?.font = UIFont.joy_M_15
        b.addTargetFor(self, action: #selector(leftAction), for: .touchUpInside)
        b.titleLabel?.textAlignment = .center
        b.setImage(UIImage.sceneImage(name: "arrow_ic"), for: .normal)
        b.imageView?.contentMode = .scaleAspectFit
        b.semanticContentAttribute = .forceRightToLeft
        b.imageEdgeInsets(UIEdgeInsets(top: 0, left: -7, bottom: 0, right: 7))
        b.titleEdgeInsets(UIEdgeInsets(top: 0, left: 7, bottom: 0, right: -7))
        return b
    }()
    
    private lazy var rightButton: UIButton = {
        let b = UIButton(type: .custom)
        b.setTitle("房间列表", for: .normal)
        b.setTitleColor(UIColor.joy_zi05, for: .normal)
        b.titleLabel?.font = UIFont.joy_M_15
        b.addTarget(self, action: #selector(rightAction), for: .touchUpInside)
        b.titleLabel?.textAlignment = .center
        return b
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = UIColor.white
        addSubview(line)
        addSubview(leftButton)
        addSubview(rightButton)
        
        line.snp.makeConstraints { make in
            make.top.right.left.equalTo(0)
            make.height.equalTo(1)
        }
        
        leftButton.snp.makeConstraints { make in
            make.left.equalTo(0)
            make.top.equalTo(15)
            make.height.equalTo(21)
        }
        
        rightButton.snp.makeConstraints { make in
            make.right.equalTo(0)
            make.top.equalTo(15)
            make.left.equalTo(leftButton.snp_right)
            make.width.equalTo(leftButton)
            make.height.equalTo(leftButton)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func leftAction() {
        guard let callback = gameSupplierCallback else {return}
        callback(.left)
    }
    
    @objc private func rightAction() {
        guard let callback = gameSupplierCallback else {return}
        callback(.right)
    }
}
