//
//  ShowRealTimeDataView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/9.
//
import UIKit
import Agora_Scene_Utils
import AgoraRtcKit
import AgoraCommon
class ShowRealTimeDataView: UIView {
    private lazy var leftInfoLabel: AGELabel = {
        let label = AGELabel(colorStyle: .white, fontStyle: .small)
        label.textAlignment = .left
        label.numberOfLines = 0
        label.text = nil
        return label
    }()
    private lazy var rightInfoLabel: AGELabel = {
        let label = AGELabel(colorStyle: .white, fontStyle: .small)
        label.textAlignment = .left
        label.numberOfLines = 0
        label.text = nil
        return label
    }()
    private lazy var closeButton: AGEButton = {
        let button = AGEButton(style: .systemImage(name: "xmark", imageColor: .white))
        button.addTarget(self, action: #selector(onTapCloseButton), for: .touchUpInside)
        return button
    }()
    
    
    init(isLocal: Bool) {
        super.init(frame: .zero)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func update(left: String, right: String) {
        leftInfoLabel.text = left
        rightInfoLabel.text = right
    }
    
    private func setupUI() {
        backgroundColor = UIColor(hex: "#151325", alpha: 0.8)
        layer.cornerRadius = 15
        layer.masksToBounds = true
        widthAnchor.constraint(equalToConstant: Screen.width - 30).isActive = true
        
        addSubview(leftInfoLabel)
        addSubview(rightInfoLabel)
        addSubview(closeButton)
        
        leftInfoLabel.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.top.equalTo(10)
            make.bottom.equalTo(-10)
        }
        rightInfoLabel.snp.makeConstraints { make in
            make.left.equalTo(self.snp.centerX)
            make.top.bottom.equalTo(leftInfoLabel)
        }
        closeButton.snp.makeConstraints { make in
            make.top.equalTo(12)
            make.right.equalTo(-15)
        }
    }
    
    @objc
    private func onTapCloseButton() {
        removeFromSuperview()
    }
}
