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
    func onClickAudioButton(audioEnable:Bool)
    func onClickRobotButton()
}

class ShowRoomBottomBar: UIView {
    weak var delegate: RoomBottomBarDelegate?
    var robotEnable = false {
        didSet {
            self.createSubviews()
        }
    }
    
    private lazy var sendMessageButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("room_send_message".joyLocalization(), for: .normal)
        button.setTitleColor(.joy_main_text, for: .normal)
        button.titleLabel?.font = .joy_R_13
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickSendButton), for: .touchUpInside)
        return button
    }()
    
    // 麦克风开启静音
    private lazy var audioButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "ic_mic_closed"), for: .selected)
        button.setImage(UIImage.sceneImage(name: "ic_mic_opened"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickAudioButton(sender:)), for: .touchUpInside)
        return button
    }()
    
    //机器人
    private lazy var robotButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "play_zone_robot"), for: .normal)
        button.backgroundColor = .joy_cover
        button.addTarget(self, action: #selector(didClickRobotButton), for: .touchUpInside)
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
        self.removeAllSubviews()
        
        if robotEnable {
            buttonArray = [robotButton, audioButton]
        } else {
            buttonArray = [audioButton]
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
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        // 获取当前视图下的子视图
        let subview = super.hitTest(point, with: event)
        
        // 如果子视图存在并且不是自己，则返回子视图
        if subview != self {
            return subview
        }
        
        // 否则，将点击事件传递给下层视图
        return nil
        
    }
    
    @objc private func didClickSendButton() {
        delegate?.onClickSendButton()
    }
    
    @objc private func didClickAudioButton(sender: UIButton) {
        sender.isSelected = !sender.isSelected
        delegate?.onClickAudioButton(audioEnable: sender.isSelected)
    }
    
    @objc private func didClickRobotButton() {
        delegate?.onClickRobotButton()
    }
    
}
