//
//  ShowRoomOwnerExpiredView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2024/1/25.
//

import UIKit

private let HeadImgViewHeight: CGFloat = 61
private let BackButtonHeight: CGFloat = 45

class ShowRoomOwnerExpiredView: UIView {

    var headImg: String? {
        didSet {
            if (headImg ?? "").hasPrefix("http") {
                headImgView.sd_setImage(with: URL(string: headImg ?? ""))
            } else {
                headImgView.image = UIImage(named: headImg ?? "")
            }
        }
    }
    
    var clickBackButtonAction: (()->())?
    
    // 背景图
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 20
        view.layer.masksToBounds = true
        return view
    }()
    
    // 主播头像
    private lazy var headImgView: UIImageView =  {
        let imgeView =  UIImageView()
        imgeView.layer.cornerRadius = HeadImgViewHeight * 0.5
        imgeView.contentMode = .scaleAspectFill
        imgeView.layer.masksToBounds = true
        return imgeView
    }()
    
    // 标题
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = .show_S_18
        label.text = "show_alert_live_owner_expired_title".show_localized
        return label
    }()
    
    // 返回房间列表
    private lazy var backButton: GradientButton = {
        let button = GradientButton(type: .custom)
        button.titleLabel?.font = .show_M_15
        button.setTitle("show_alert_live_owner_expired_back".show_localized, for: .normal)
        button.addTarget(self, action: #selector(didClickBackButton), for: .touchUpInside)
        return button
    }()

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
            make.center.equalTo(self)
            make.width.equalTo(280)
            make.height.equalTo(230)
        }
        
        bgView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(25)
        }
        
        bgView.addSubview(headImgView)
        headImgView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(titleLabel.snp.bottom).offset(20)
            make.width.height.equalTo(HeadImgViewHeight)
        }
        
        bgView.addSubview(backButton)
        backButton.layer.cornerRadius = BackButtonHeight * 0.5
        backButton.layer.masksToBounds = true
        backButton.snp.makeConstraints { make in
            make.centerX.equalTo(self.titleLabel)
            make.bottom.equalTo(-20)
            make.width.equalTo(200)
            make.height.equalTo(BackButtonHeight)
        }
    }

}

extension ShowRoomOwnerExpiredView {
    @objc private func didClickBackButton() {
        self.clickBackButtonAction?()
    }
}

class GradientButton: UIButton {
    
    private let gradientLayer = CAGradientLayer()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupButton()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupButton()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        gradientLayer.frame = bounds
    }
    
    private func setupButton() {
        // 设置渐变层
        gradientLayer.frame = bounds
        gradientLayer.colors = [UIColor(hex: "#DD7BFF").cgColor, UIColor(hex: "#5E5BFF").cgColor]
        gradientLayer.startPoint = CGPoint(x: 0.5, y: 0.0)
        gradientLayer.endPoint = CGPoint(x: 0.5, y: 1.0)
        layer.insertSublayer(gradientLayer, at: 0)
    }
    
    override var isHighlighted: Bool {
        didSet {
            if isHighlighted {
                gradientLayer.colors = [UIColor(hex: "#DD00FF").cgColor, UIColor(hex: "#5E5BFF").cgColor]
            } else {
                gradientLayer.colors = [UIColor(hex: "#DD7BFF").cgColor, UIColor(hex: "#5E5BFF").cgColor]
            }
        }
    }
    
}
