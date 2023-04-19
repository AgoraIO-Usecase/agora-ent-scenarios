//
//  ShowReceivePKView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

private let ButtonHeight: CGFloat = 40

protocol ShowReceivePKViewDelegate: NSObjectProtocol {
    func onClickRefuseButton()
    func onClickAcceptButton()
}

class ShowReceivePKView: UIView {

    weak var delegate: ShowReceivePKViewDelegate?
    
    private var style: Style? = .pk
    
    var countDown: Int = 0 {
        didSet {
            let text = "show_alert_pk_refuse".show_localized + "(\(countDown)s)"
            refuseButton.setTitle(text, for: .normal)
        }
    }
    
    var name: String? {
        didSet {
            let attributedText = NSMutableAttributedString(string: "show_alert_pk_title_1".show_localized, attributes: [NSAttributedString.Key.foregroundColor : UIColor.show_Ellipse6])
            let attributedName = NSAttributedString(string: name ?? "", attributes: [NSAttributedString.Key.foregroundColor : UIColor.show_blue03])
            let styleText = style == .pk ? "show_alert_pk_title_2".show_localized : "show_alert_link_title_2".show_localized
            let tailText = NSAttributedString(string: styleText , attributes: [NSAttributedString.Key.foregroundColor : UIColor.show_Ellipse6])
            attributedText.append(attributedName)
            attributedText.append(tailText)
            titleLabel.attributedText = attributedText
        }
    }
    
    // 背景图
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 20
        view.layer.masksToBounds = true
        return view
    }()
    
    // PK
    private lazy var pkImgView: UIImageView =  {
        let imgeView =  UIImageView(image: UIImage.show_sceneImage(name: style == .pk ? "show_alert_pk" : "show_alert_mic"))
        return imgeView
    }()
    
    // 标题
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse7
        label.font = .show_R_16
        return label
    }()
    
    // 拒绝按钮
    private lazy var refuseButton: UIButton = {
        let button = UIButton(type: .custom)
        button.backgroundColor = .show_btn_bg_not_recommended
        button.layer.cornerRadius = ButtonHeight * 0.5
        button.layer.masksToBounds = true
        button.setTitle("show_alert_pk_refuse".show_localized, for: .normal)
        button.setTitleColor(.show_Ellipse5, for: .normal)
        button.titleLabel?.font = UIFont.show_M_14
        button.addTarget(self, action: #selector(didClickRefuseButton), for: .touchUpInside)
        return button
    }()
    
    // 接受按钮
    private lazy var acceptButton: UIButton = {
        let button = UIButton(type: .custom)
        button.backgroundColor = .show_zi03
        button.layer.cornerRadius = ButtonHeight * 0.5
        button.layer.masksToBounds = true
        button.setTitle("show_alert_pk_accept".show_localized, for: .normal)
        button.setTitleColor(.show_main_text, for: .normal)
        button.titleLabel?.font = UIFont.show_M_14
        button.addTarget(self, action: #selector(didClickAcceptButton), for: .touchUpInside)
        return button
    }()
    
    init(style: Style?) {
        self.style = style
        super.init(frame: .zero)
        createSubviews()
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        backgroundColor = .clear
        
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.equalTo(295)
            make.height.equalTo(225)
        }
        
        addSubview(pkImgView)
        pkImgView.snp.makeConstraints { make in
            make.left.right.equalTo(bgView)
            make.bottom.equalTo(bgView.snp.top).offset(63)
        }
        
        bgView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(-105)
        }
        
        bgView.addSubview(refuseButton)
        refuseButton.snp.makeConstraints { make in
            make.left.equalTo(30)
            make.bottom.equalTo(-40)
            make.height.equalTo(ButtonHeight)
            make.width.greaterThanOrEqualTo(108)
        }
        
        bgView.addSubview(acceptButton)
        acceptButton.snp.makeConstraints { make in
            make.right.equalTo(-30)
            make.bottom.equalTo(-40)
            make.height.equalTo(ButtonHeight)
            make.width.greaterThanOrEqualTo(108)
        }
        
    }
}

extension ShowReceivePKView {
    enum Style {
        case pk  // pk
        case mic // 连麦
    }
}

extension ShowReceivePKView {
    
    @objc private func didClickRefuseButton() {
        delegate?.onClickRefuseButton()
    }
    
    @objc private func didClickAcceptButton() {
        delegate?.onClickAcceptButton()
    }
}
