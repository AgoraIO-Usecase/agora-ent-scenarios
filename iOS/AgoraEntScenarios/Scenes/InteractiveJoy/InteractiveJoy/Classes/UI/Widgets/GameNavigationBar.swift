//
//  GameNavigationBar.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/29.
//

import UIKit

class GameNavigationBar: UIView {
    var moreActionCallback: (() -> ())?
    var closeActionCallback: (() -> ())?
    
    lazy var roomInfoView: RoomInfoView = {
        let view = RoomInfoView()
        return view
    }()
    
    lazy var moreButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "icon_more"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.addTarget(self, action: #selector(onMoreAction), for: .touchUpInside)
        return button
    }()
    
    lazy var closeButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "icon_close@3x"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.addTarget(self, action: #selector(onCloseAction), for: .touchUpInside)
        return button
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(roomInfoView)
        self.addSubview(moreButton)
        self.addSubview(closeButton)
        
        roomInfoView.snp_makeConstraints { make in
            make.left.equalTo(15)
            make.width.equalTo(202)
            make.top.bottom.equalTo(0)
        }
        
        closeButton.snp_makeConstraints { make in
            make.right.equalTo(-8)
            make.height.width.equalTo(32)
            make.centerY.equalTo(self)
        }
        
        moreButton.snp_makeConstraints { make in
            make.right.equalTo(closeButton.snp.left).offset(-4)
            make.height.width.equalTo(32)
            make.centerY.equalTo(self)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func onMoreAction() {
        guard let callback = moreActionCallback else {return}
        
        callback()
    }
    
    @objc private func onCloseAction() {
        guard let callback = closeActionCallback else {return}
        
        callback()
    }
}
