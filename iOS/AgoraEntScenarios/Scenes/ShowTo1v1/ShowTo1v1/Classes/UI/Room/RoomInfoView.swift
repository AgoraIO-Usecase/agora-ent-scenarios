//
//  RoomInfoView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit
import SDWebImage
import SnapKit

private let bgViewHeight: CGFloat = 40
private let imgViewHeight: CGFloat = 32

class RoomInfoView: UIView {
    var timerCallBack: ((Int64)->())?
    private var startTime: Int64!
    
    private var timer: Timer?
    
    // 背景
    private lazy var bgView: UIView = {
        let view = UIView()
        view.backgroundColor = .black.withAlphaComponent(0.25)
        view.layer.cornerRadius = bgViewHeight * 0.5
        view.layer.masksToBounds = true
        return view
    }()
    
    // 头像
    private lazy var headImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.contentMode = .scaleAspectFill
        imgView.layer.cornerRadius = imgViewHeight * 0.5
        imgView.clipsToBounds = true
        return imgView
    }()
    
    // 名称
    private lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    
    // 房间号
    private lazy var idLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.alpha = 0.8
        label.font = UIFont.systemFont(ofSize: 12)
        return label
    }()
    
    // 直播标识
    private lazy var indicatorImageView: UIView = {
        let imageView = UIImageView()
        imageView.image = UIImage.sceneImage(name: "call_connect")
        return imageView
    }()
    
    // 时间
    private lazy var timeLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.alpha = 0.8
        label.font = UIFont.systemFont(ofSize: 10)
        return label
    }()
    
    deinit {
        print("------ShowRoomInfoView ----- 销毁-----")
//        timer.invalidate()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
            make.width.greaterThanOrEqualTo(202)
            make.height.equalTo(bgViewHeight)
        }
        
        addSubview(headImgView)
        headImgView.snp.makeConstraints { make in
            make.left.equalTo(4)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(imgViewHeight)
        }
        
        addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.left.equalTo(headImgView.snp.right).offset(8)
            make.top.equalTo(headImgView)
            make.right.equalTo(-20)
        }
        
        addSubview(idLabel)
        idLabel.snp.makeConstraints { make in
            make.left.equalTo(nameLabel)
            make.bottom.equalTo(-4)
        }
        
        addSubview(indicatorImageView)
        indicatorImageView.snp.makeConstraints { make in
            make.left.equalTo(120)
            make.centerY.equalTo(idLabel)
        }
        
        addSubview(timeLabel)
        timeLabel.snp.makeConstraints { make in
            make.bottom.equalTo(idLabel)
            make.left.equalTo(indicatorImageView.snp.right).offset(4)
        }
    }
    
    func setRoomInfo(avatar: String?, name: String?, id: String?) {
        headImgView.sd_setImage(with: URL(string: avatar ?? ""))
        nameLabel.text = name
        idLabel.text = id
    }
    
    func startTime(_ time: Int64) {
        stopTime()
        self.startTime = time
        updateTime()
        
        timer = Timer.scheduledTimer(withTimeInterval: 1, block: {[weak self] t in
            self?.updateTime()
        }, repeats: true)
        timer?.fire()
    }
    
    func stopTime() {
        timer?.invalidate()
    }
    
    private func updateTime(){
        let duration = Int64(Date().timeIntervalSince1970) - startTime / 1000
        let seconds = duration % 60
        let minutes = duration / 60 % 60
        let hours = duration / 3600
        let durationStr = String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        timeLabel.text = durationStr
        
        timerCallBack?(duration)
    }
}
