//
//  ShowCreateLiveView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit


class ShowCreateLiveView: UIView {
    
    private var roomBgImgView: UIImageView!
    private var nameTextField: UITextField!
    private var roomIdLabel: UILabel!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
       // 房间信息蒙版
        let roomInfoCoverVeiw = UIView()
        roomInfoCoverVeiw.backgroundColor = .show_cover
        roomInfoCoverVeiw.layer.cornerRadius = 10
        roomInfoCoverVeiw.layer.masksToBounds = true
        addSubview(roomInfoCoverVeiw)
        roomInfoCoverVeiw.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(103)
            make.width.equalTo(315)
            make.height.equalTo(84)
        }
        
        // 房间背景图
        roomBgImgView = UIImageView()
        roomBgImgView.contentMode = .scaleAspectFill
        roomBgImgView.clipsToBounds = true
        roomBgImgView.layer.cornerRadius = 10
        roomBgImgView.sd_setImage(with: URL(string: "https://img0.baidu.com/it/u=3375911127,635571288&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1667494800&t=87178e90d06a726eeb8df88b51a3dfe0"))
        roomInfoCoverVeiw.addSubview(roomBgImgView)
        roomBgImgView.snp.makeConstraints { make in
            make.left.equalTo(10)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(60)
        }
        
        // 名称
        nameTextField = UITextField()
        roomInfoCoverVeiw.addSubview(nameTextField)
        nameTextField.placeholder = "请输入房间名称"
        nameTextField.text = "Chat with Eve tonight and"
        nameTextField.font = .show_M_15
        nameTextField.textColor = .show_main_text
        nameTextField.snp.makeConstraints { make in
            make.left.equalTo(roomBgImgView.snp.right).offset(10)
            make.top.equalTo(18)
            make.right.equalTo(-50)
        }
        
        // 编辑按钮
        let editButton = UIButton(type: .custom)
        editButton.setImage(UIImage.show_sceneImage(name: "show_create_edit"), for: .normal)
        addSubview(editButton)
        editButton.snp.makeConstraints { make in
            make.left.equalTo(nameTextField.snp.right).offset(3)
            make.centerY.equalTo(nameTextField)
        }
        // 房间号
        roomIdLabel = UILabel()
        roomIdLabel.text = "ID: 1235414"
        roomIdLabel.font = .show_R_10
        roomIdLabel.textColor = .show_main_text
        roomInfoCoverVeiw.addSubview(roomIdLabel)
        roomIdLabel.snp.makeConstraints { make in
            make.left.equalTo(nameTextField)
            make.bottom.equalTo(-19)
        }
        
        // 复制按钮
        let copyButton = UIButton(type: .custom)
        roomInfoCoverVeiw.addSubview(copyButton)
        copyButton.setImage(UIImage.show_sceneImage(name: "show_create_copy"), for: .normal)
        copyButton.addTarget(self, action: #selector(didClickCopyButton), for: .touchUpInside)
        copyButton.snp.makeConstraints { make in
            make.left.equalTo(roomIdLabel.snp.right).offset(10)
            make.centerY.equalTo(roomIdLabel)
        }
        
        // 蒙版
        let coverView = UIImageView()
        coverView.image = UIImage.show_sceneImage(name: "show_list_cover")
        addSubview(coverView)
        coverView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(254)
        }
        
        // 翻转摄像头
        
        // 美化
        
        // 画质
        
        // tips
        let tipsLabel = UILabel()
        let tipsText = " 本产品仅用于功能体验，单次直播时长不超过20mins"
        let attachment = NSTextAttachment(image: UIImage.show_sceneImage(name: "show_create_tips")!)
        attachment.bounds = CGRect(x: 0, y: -2, width: 11, height: 11)
        let attriTipsImg = NSAttributedString(attachment: attachment)
        let attriTips = NSMutableAttributedString(attributedString: attriTipsImg)
        attriTips.append(NSAttributedString(string: tipsText))
        tipsLabel.attributedText = attriTips
        tipsLabel.font = .show_R_11
        tipsLabel.textColor = .show_main_text
        addSubview(tipsLabel)
        tipsLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(-134)
        }
        
        // 开始直播
        let btnHeight: CGFloat = 48
        let startButton = UIButton(type: .custom)
        startButton.setTitle("开始直播", for: .normal)
        startButton.backgroundColor = .show_btn_bg
        startButton.titleLabel?.font = .show_btn_title
        startButton.layer.cornerRadius = btnHeight * 0.5
        startButton.layer.masksToBounds = true
        addSubview(startButton)
        startButton.addTarget(self, action: #selector(didClickStartButton), for: .touchUpInside)
        startButton.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(-60)
            make.width.equalTo(230)
            make.height.equalTo(btnHeight)
        }
    }
    
    
}

extension ShowCreateLiveView {
    // 点击复制按钮
    @objc private func didClickCopyButton(){
        UIPasteboard.general.string = roomIdLabel.text
        ToastView.show(text: "已拷贝至剪切板")
    }
    
    // 点击开始直播按钮
    @objc private func didClickStartButton(){
        
    }
}
