//
//  ShowReceiveFinishView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

private let HeadImgViewHeight: CGFloat = 61
private let BackButtonHeight: CGFloat = 40

protocol ShowReceiveFinishViewDelegate: NSObjectProtocol {
    func onClickBackButton()
}


class ShowReceiveFinishView: UIView {

    var headImg: String? {
        didSet {
            headImgView.sd_setImage(with: URL(string: headImg ?? ""))
        }
    }
    
    weak var delegate: ShowReceiveFinishViewDelegate?
    
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
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse7
        label.font = .show_R_16
        label.text = "show_alert_live_finish_title".show_localized
        return label
    }()
    
    // 返回房间列表
    private lazy var backButton: UIButton = {
        let button = UIButton(type: .custom)
        button.backgroundColor = .show_zi03
        button.layer.cornerRadius = BackButtonHeight * 0.5
        button.layer.masksToBounds = true
        button.setTitle("show_alert_live_finish_back".show_localized, for: .normal)
        button.setTitleColor(.show_main_text, for: .normal)
        button.titleLabel?.font = UIFont.show_M_14
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
            make.center.equalToSuperview()
            make.width.equalTo(295)
            make.height.equalTo(242)
        }
        
        bgView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(30)
        }
        
        bgView.addSubview(headImgView)
        headImgView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(67)
            make.width.height.equalTo(HeadImgViewHeight)
        }
        
        bgView.addSubview(backButton)
        backButton.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(-31)
            make.height.equalTo(BackButtonHeight)
            make.width.greaterThanOrEqualTo(177)
        }
        
    }

}

extension ShowReceiveFinishView {
    
    @objc private func didClickBackButton() {
        delegate?.onClickBackButton()
    }
}
