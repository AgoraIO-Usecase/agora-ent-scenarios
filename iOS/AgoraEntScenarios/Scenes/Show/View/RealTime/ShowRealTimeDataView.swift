//
//  ShowRealTimeDataView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/9.
//
import UIKit
import Agora_Scene_Utils
import AgoraRtcKit

class ShowRealTimeDataView: UIView {
    private lazy var leftInfoLabel: AGELabel = {
        let label = AGELabel(colorStyle: .white, fontStyle: .small)
        label.textAlignment = .left
        label.numberOfLines = 0
        return label
    }()
    private lazy var rightInfoLabel: AGELabel = {
        let label = AGELabel(colorStyle: .white, fontStyle: .small)
        label.textAlignment = .right
        label.numberOfLines = 0
        return label
    }()
    private lazy var closeButton: AGEButton = {
        let button = AGEButton(style: .systemImage(name: "xmark", imageColor: .white))
        button.addTarget(self, action: #selector(onTapCloseButton), for: .touchUpInside)
        return button
    }()
    
    private var audioOnly: Bool = false
    
    var statsInfo: ShowStatisticsInfo? {
        didSet{
            leftInfoLabel.text = statsInfo?.description(audioOnly: audioOnly).0
            rightInfoLabel.text = statsInfo?.description(audioOnly: audioOnly).1
        }
    }
    
    init(audioOnly: Bool = false, isLocal: Bool) {
        super.init(frame: .zero)
        self.audioOnly = audioOnly
        if isLocal {
            statsInfo = ShowStatisticsInfo(type: .local(ShowStatisticsInfo.LocalInfo()))
        } else {
            statsInfo = ShowStatisticsInfo(type: .remote(ShowStatisticsInfo.RemoteInfo()))
        }
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        backgroundColor = UIColor(hex: "#151325", alpha: 0.8)
        layer.cornerRadius = 15
        layer.masksToBounds = true
        widthAnchor.constraint(equalToConstant: Screen.width - 30).isActive = true
        
        leftInfoLabel.translatesAutoresizingMaskIntoConstraints = false
        rightInfoLabel.translatesAutoresizingMaskIntoConstraints = false
        closeButton.translatesAutoresizingMaskIntoConstraints = false
        addSubview(leftInfoLabel)
        addSubview(rightInfoLabel)
        addSubview(closeButton)
        
        leftInfoLabel.leadingAnchor.constraint(equalTo: leadingAnchor,
                                               constant: 15).isActive = true
        leftInfoLabel.topAnchor.constraint(equalTo: topAnchor,
                                           constant: 20).isActive = true
        leftInfoLabel.bottomAnchor.constraint(equalTo: bottomAnchor,
                                              constant: -20).isActive = true
        
        rightInfoLabel.trailingAnchor.constraint(equalTo: trailingAnchor,
                                                 constant: -57).isActive = true
        rightInfoLabel.topAnchor.constraint(equalTo: leftInfoLabel.topAnchor).isActive = true
        rightInfoLabel.bottomAnchor.constraint(equalTo: leftInfoLabel.bottomAnchor).isActive = true
        
        closeButton.trailingAnchor.constraint(equalTo: trailingAnchor,
                                              constant: -19).isActive = true
        closeButton.topAnchor.constraint(equalTo: topAnchor,
                                         constant: 13).isActive = true
    }
    
    @objc
    private func onTapCloseButton() {
//        AlertManager.hiddenView()
        removeFromSuperview()
    }
}
