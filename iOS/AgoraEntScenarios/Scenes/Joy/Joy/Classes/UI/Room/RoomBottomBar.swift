//
//  RoomBottomBar.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit

private let dotWidth: CGFloat = 5

protocol RoomBottomBarDelegate: NSObjectProtocol {
    func onClickSendButton()
    func onClickGiftButton()
    func onClickLikeButton()
    func onClickDeployButton(isUp: Bool)
}


class ShowRoomBottomBar: UIView {
    
    weak var delegate: RoomBottomBarDelegate?
    
    private lazy var sendMessageButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("room_send_message".joyLocalization(), for: .normal)
        button.setTitleColor(.joy_main_text, for: .normal)
        button.titleLabel?.font = .joy_R_13
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickSendButton), for: .touchUpInside)
        return button
    }()
    
    // 礼物
    private lazy var giftButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "bar_gift"), for: .normal)
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickGiftButton), for: .touchUpInside)
        return button
    }()
    
    private lazy var likeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "bar_like"), for: .normal)
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickLikeButton), for: .touchUpInside)
        return button
    }()
    
    
    private lazy var deployButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "bar_deploy"), for: .normal)
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickDeployDownButton), for: .touchDown)
        button.addTarget(self, action: #selector(didClickDeployUpButton), for: .touchUpInside)
        button.addTarget(self, action: #selector(didClickDeployUpButton), for: .touchCancel)
        button.addTarget(self, action: #selector(didClickDeployUpButton), for: .touchUpOutside)
        return button
    }()
    
    private var buttonArray = [UIButton]()
    private var isBroadcastor = false
    
    init(isBroadcastor: Bool = false) {
        super.init(frame: .zero)
        self.isBroadcastor = isBroadcastor
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        if isBroadcastor {
            buttonArray = [likeButton, deployButton]
        }else{
            buttonArray = [giftButton, likeButton]
        }
        addSubview(sendMessageButton)
        sendMessageButton.sizeToFit()
        sendMessageButton.layer.cornerRadius = 18
        sendMessageButton.clipsToBounds = true
        let buttonWidth = sendMessageButton.width + 32
        sendMessageButton.snp.makeConstraints { make in
            make.height.equalTo(36)
            make.width.equalTo(buttonWidth)
            make.left.equalTo(12)
            make.centerY.equalToSuperview()
        }
        
        let btnWidth: CGFloat = 38
        let btnHeight: CGFloat = 38
        let spacing: CGFloat = 12
        var right: CGFloat = -spacing
        for button in buttonArray {
            addSubview(button)
            button.layer.cornerRadius = 19
            button.clipsToBounds = true
            button.snp.makeConstraints { make in
                make.right.equalToSuperview().offset(right)
                make.centerY.equalToSuperview()
                make.height.equalTo(btnHeight)
                make.width.equalTo(btnWidth)
            }
            right -= (btnWidth + spacing)
        }
    }
    
    @objc private func didClickSendButton() {
        delegate?.onClickSendButton()
    }
    
    @objc private func didClickGiftButton() {
        delegate?.onClickGiftButton()
    }
    
    @objc private func didClickLikeButton() {
        delegate?.onClickLikeButton()
    }
    
    @objc private func didClickDeployDownButton() {
        delegate?.onClickDeployButton(isUp: false)
    }
    
    @objc private func didClickDeployUpButton() {
        delegate?.onClickDeployButton(isUp: true)
    }
    
    public func setDeployBtnHidden(isHidden: Bool) {
        if buttonArray.contains(deployButton) {
            deployButton.isHidden = isHidden
        }
    }
    
    public func setDeployBtn(with url: String) {
        if let imgUrl = URL(string: url) {
            deployButton.imageView?.sd_setImage(with: imgUrl)
        }
    }
}
