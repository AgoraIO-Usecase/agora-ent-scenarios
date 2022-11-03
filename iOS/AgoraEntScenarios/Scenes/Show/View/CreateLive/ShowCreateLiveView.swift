//
//  ShowCreateLiveView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit

protocol ShowCreateLiveViewDelegate: NSObjectProtocol {
    func onClickCameraBtnAction()
    func onClickBeautyBtnAction()
    func onClickQualityBtnAction()
    func onClickStartBtnAction()
}

class ShowCreateLiveView: UIView {

    weak var delegate: ShowCreateLiveViewDelegate?
    
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
        
        // 中间按钮
        layoutButtonArray()
    }
    
    private func createButton(imgName: String, title: String) ->UIButton {
        let button = UIButton(type: .custom)
        let imageView = UIImageView(image: UIImage.show_sceneImage(name: imgName))
        button.addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.left.top.right.equalToSuperview()
        }
        let label = UILabel()
        label.font = .show_M_12
        label.textColor = .show_main_text
        label.text = title
        label.textAlignment = .center
        button.addSubview(label)
        label.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.top.equalTo(imageView.snp.bottom).offset(3)
        }
        addSubview(button)
        return button
    }
    
    private func layoutButtonArray(){
        // 翻转摄像头
        let cameraButton = createButton(imgName: "show_create_camera", title: "翻转")
        cameraButton.addTarget(self, action: #selector(didClickCameraButton), for: .touchUpInside)
        
        // 美化
        let beautyButton = createButton(imgName: "show_create_beauty", title: "美化")
        beautyButton.addTarget(self, action: #selector(didClickBeautyButton), for: .touchUpInside)
        
        // 画质
        let qualityButton = createButton(imgName: "show_create_quality", title: "画质")
        qualityButton.addTarget(self, action: #selector(didClickQualityButton), for: .touchUpInside)
        
        let buttonArray = [cameraButton, beautyButton, qualityButton]
        let count = buttonArray.count
        let itemSpace: CGFloat = 50
        let itemWidth: CGFloat = 30
        let baseLeft: CGFloat = (Screen.width - itemSpace * CGFloat(count - 1) - itemWidth * CGFloat(count)) / 2
        var i = 0
        for button in buttonArray {
            button.snp.makeConstraints { make in
                make.left.equalTo(baseLeft + CGFloat(i) * (itemWidth + itemSpace))
                make.bottom.equalTo(-174)
                make.width.equalTo(itemWidth)
            }
            i += 1
        }
    }
}

extension ShowCreateLiveView {
    // 点击复制按钮
    @objc private func didClickCopyButton(){
        UIPasteboard.general.string = roomIdLabel.text
        ToastView.show(text: "已拷贝至剪切板")
    }
    
    // 点击翻转按钮
    @objc private func didClickCameraButton(){
        delegate?.onClickCameraBtnAction()
    }
    
    // 点击美化按钮
    @objc private func didClickBeautyButton(){
        delegate?.onClickBeautyBtnAction()
    }
    
    // 点击画质按钮
    @objc private func didClickQualityButton(){
        delegate?.onClickQualityBtnAction()
    }
    
    // 点击开始直播按钮
    @objc private func didClickStartButton(){
        delegate?.onClickStartBtnAction()
    }
}
