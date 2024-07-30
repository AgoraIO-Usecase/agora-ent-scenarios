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

class BottomBar: UIView {
    var gameSupplierCallback: ((BottomBarClickType) -> ())? = nil
    private lazy var line: UIView = {
        let l = UIView()
        l.backgroundColor = UIColor.gray
        return l
    }()
    
    private lazy var leftButton: UIButton = {
        let b = UIButton(type: .custom)
        b.setTitle("切换供应", for: .normal)
        b.setTitleColor(UIColor(hexString: "#040925")!, for: .normal)
        b.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 15)
        b.addTargetFor(self, action: #selector(leftAction), for: .touchUpInside)
        b.titleLabel?.textAlignment = .center
        return b
    }()
    
    private lazy var rightButton: UIButton = {
        let b = UIButton(type: .custom)
        b.setTitle("房间列表", for: .normal)
        b.setTitleColor(UIColor(hexString: "#040925")!, for: .normal)
        b.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 15)
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
