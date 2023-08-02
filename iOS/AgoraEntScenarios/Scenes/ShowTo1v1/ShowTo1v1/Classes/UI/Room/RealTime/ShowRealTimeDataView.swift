//
//  ShowRealTimeDataView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/9.
//
import UIKit
import AgoraRtcKit

class ShowRealTimeDataView: UIView {
    private lazy var leftInfoLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 10)
        label.textColor = .white
        label.textAlignment = .left
        label.numberOfLines = 0
        label.text = nil
        return label
    }()
    private lazy var rightInfoLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 10)
        label.textColor = .white
        label.textAlignment = .right
        label.numberOfLines = 0
        label.text = nil
        return label
    }()
    private lazy var closeButton: UIButton = {
        let button = UIButton(type: .custom)
        if #available(iOS 13.0, *) {
            let image = UIImage(systemName: "xmark")?.withTintColor(.white, renderingMode: .alwaysOriginal)
            button.setImage(image, for: .normal)
        } else {
            // Fallback on earlier versions
        }
        button.addTarget(self, action: #selector(onTapCloseButton), for: .touchUpInside)
        return button
    }()
    
    private var audioOnly: Bool = false
    
//    var statsInfo: ShowStatisticsInfo? {
//        didSet{
//            leftInfoLabel.text = statsInfo?.description(audioOnly: audioOnly).0
//            rightInfoLabel.text = statsInfo?.description(audioOnly: audioOnly).1
//        }
//    }
    
    var receiveStatsInfo: ShowStatisticsInfo? {
        didSet{
            updateStatistisInfo()
        }
    }
    
    var sendStatsInfo: ShowStatisticsInfo? {
        didSet{
            updateStatistisInfo()
        }
    }
    
    func cleanRemoteDescription(){
        let localLeftStr = sendStatsInfo?.description(audioOnly: audioOnly).0 ?? ""
        let localRightStr = sendStatsInfo?.description(audioOnly: audioOnly).1 ?? ""
        let remoteLeftStr = receiveStatsInfo?.cleanRemoteDescription().0 ?? ""
        let remoteRightStr = receiveStatsInfo?.cleanRemoteDescription().1 ?? ""
        leftInfoLabel.text = [localLeftStr, remoteLeftStr].joined(separator: "\n\n")
        rightInfoLabel.text = [localRightStr, remoteRightStr].joined(separator: "\n\n")
    }
    
    func cleanLocalDescription(){
        let localLeftStr = sendStatsInfo?.cleanLocalDescription().0 ?? ""
        let localRightStr = sendStatsInfo?.cleanLocalDescription().1 ?? ""
        let remoteLeftStr = receiveStatsInfo?.description(audioOnly: audioOnly).0 ?? ""
        let remoteRightStr = receiveStatsInfo?.description(audioOnly: audioOnly).1 ?? ""
        leftInfoLabel.text = [localLeftStr, remoteLeftStr].joined(separator: "\n\n")
        rightInfoLabel.text = [localRightStr, remoteRightStr].joined(separator: "\n\n")
    }
    
    private func updateStatistisInfo(){
        let localLeftStr = sendStatsInfo?.description(audioOnly: audioOnly).0 ?? ""
        let localRightStr = sendStatsInfo?.description(audioOnly: audioOnly).1 ?? ""
        let remoteLeftStr = receiveStatsInfo?.description(audioOnly: audioOnly).0 ?? ""
        let remoteRightStr = receiveStatsInfo?.description(audioOnly: audioOnly).1 ?? ""
        leftInfoLabel.text = [localLeftStr, remoteLeftStr].joined(separator: "\n\n")
        rightInfoLabel.text = [localRightStr, remoteRightStr].joined(separator: "\n\n")
    }
    
    init(audioOnly: Bool = false, isLocal: Bool) {
        super.init(frame: .zero)
        self.audioOnly = audioOnly
//        if isLocal {
//            statsInfo = ShowStatisticsInfo(type: .local(ShowStatisticsInfo.LocalInfo()))
//        } else {
//            statsInfo = ShowStatisticsInfo(type: .remote(ShowStatisticsInfo.RemoteInfo()))
//        }
        sendStatsInfo = ShowStatisticsInfo(type: .local(ShowStatisticsInfo.LocalInfo()))
        receiveStatsInfo = ShowStatisticsInfo(type: .remote(ShowStatisticsInfo.RemoteInfo()))
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        backgroundColor = UIColor(hexString: "#151325")?.withAlphaComponent(0.8)
        layer.cornerRadius = 15
        layer.masksToBounds = true
        widthAnchor.constraint(equalToConstant: UIScreen.main.bounds.width - 30).isActive = true
        
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
