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
    func onClickSettingBtnAction()
    func onClickStartBtnAction()
}

class ShowCreateLiveView: UIView {

    weak var delegate: ShowCreateLiveViewDelegate?
    var hideBottomViews = false {
        didSet {
            self.coverView.isHidden = hideBottomViews
        }
    }
    
    let roomNo: String = "\(arc4random_uniform(899999) + 100000)"
    let roomBg: String = "\(Int.random(in: 1...3))"
    var roomName: String? {
        get{
            return nameTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        }
    }

    private var roomBgImgView: UIImageView!
    private var nameTextField: UITextField!
    private var roomIdLabel: UILabel!
    private var coverView: UIImageView!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
//        backgroundColor = .white
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
        roomBgImgView.image = UIImage.show_sceneImage(name: "show_room_bg_\(roomBg)")
        roomInfoCoverVeiw.addSubview(roomBgImgView)
        roomBgImgView.snp.makeConstraints { make in
            make.left.equalTo(10)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(60)
        }
        
        // 名称
        nameTextField = UITextField()
        roomInfoCoverVeiw.addSubview(nameTextField)
        nameTextField.placeholder = "create_name_text_field_placeholder".show_localized
        nameTextField.font = .show_M_15
        nameTextField.textColor = .show_main_text
        nameTextField.snp.makeConstraints { make in
            make.left.equalTo(roomBgImgView.snp.right).offset(10)
            make.top.equalTo(18)
            make.right.equalTo(-50)
        }
//        nameTextField.becomeFirstResponder()
        
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
        roomIdLabel.text = "ID: " + roomNo
        roomIdLabel.font = .show_R_10
        roomIdLabel.textColor = .show_main_text
        roomInfoCoverVeiw.addSubview(roomIdLabel)
        roomIdLabel.snp.makeConstraints { make in
            make.left.equalTo(nameTextField)
            make.bottom.equalTo(-19)
        }
        
        // 复制按钮
        let copyButton = UIButton(type: .custom)
        copyButton.isHidden = true
        roomInfoCoverVeiw.addSubview(copyButton)
        copyButton.setImage(UIImage.show_sceneImage(name: "show_create_copy"), for: .normal)
        copyButton.addTarget(self, action: #selector(didClickCopyButton), for: .touchUpInside)
        copyButton.snp.makeConstraints { make in
            make.left.equalTo(roomIdLabel.snp.right).offset(10)
            make.centerY.equalTo(roomIdLabel)
        }
        
        // 蒙版
        coverView = UIImageView()
        coverView.isUserInteractionEnabled = true
        coverView.image = UIImage.show_sceneImage(name: "show_list_cover")
        addSubview(coverView)
        coverView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(254)
        }
        
        // tips
        let tipsLabel = UILabel()
        let tipsText = "create_tips".show_localized
        var attachment = NSTextAttachment()
        if #available(iOS 13.0, *) {
            attachment = NSTextAttachment(image: UIImage.show_sceneImage(name: "show_create_tips")!)
        } else {
            attachment.image = UIImage.show_sceneImage(name: "show_create_tips")
        }
        attachment.bounds = CGRect(x: 0, y: -2, width: 11, height: 11)
        let attriTipsImg = NSAttributedString(attachment: attachment)
        let attriTips = NSMutableAttributedString(attributedString: attriTipsImg)
        attriTips.append(NSAttributedString(string: tipsText))
        tipsLabel.attributedText = attriTips
        tipsLabel.font = .show_R_11
        tipsLabel.textColor = .show_main_text
        coverView.addSubview(tipsLabel)
        tipsLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(-134)
        }
        
        // 开始直播
        let btnHeight: CGFloat = 48
        let startButton = UIButton(type: .custom)
        startButton.setTitle("create_start_live".show_localized, for: .normal)
        startButton.backgroundColor = .show_btn_bg
        startButton.titleLabel?.font = .show_btn_title
        startButton.layer.cornerRadius = btnHeight * 0.5
        startButton.layer.masksToBounds = true
        coverView.addSubview(startButton)
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
        imageView.contentMode = .center
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
        coverView.addSubview(button)
        return button
    }
    
    private func layoutButtonArray(){
        // 翻转摄像头
        let cameraButton = createButton(imgName: "show_create_camera", title: "create_button_switch".show_localized)
        cameraButton.addTarget(self, action: #selector(didClickCameraButton), for: .touchUpInside)
        
        // 美化
        let beautyButton = createButton(imgName: "show_create_beauty", title: "create_button_beauty".show_localized)
        beautyButton.addTarget(self, action: #selector(didClickBeautyButton), for: .touchUpInside)
        
        // 画质
//        let qualityButton = createButton(imgName: "show_create_quality", title: "create_button_quality".show_localized)
//        qualityButton.addTarget(self, action: #selector(didClickQualityButton), for: .touchUpInside)
        
        // 设置
        let settingButton = createButton(imgName: "show_setting", title: "create_button_settings".show_localized)
        settingButton.addTarget(self, action: #selector(didClickSettingButton), for: .touchUpInside)
        
        let buttonArray = [cameraButton, beautyButton, settingButton]
        let count = buttonArray.count
        let itemSpace: CGFloat = 40
        let itemWidth: CGFloat = 40
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
        ToastView.show(text: "create_toast_copy_to_paste_borad".show_localized)
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
    
    // 点击设置按钮
    @objc private func didClickSettingButton(){
        delegate?.onClickSettingBtnAction()
    }
    
    // 点击开始直播按钮
    @objc private func didClickStartButton(){
        delegate?.onClickStartBtnAction()
    }
}
