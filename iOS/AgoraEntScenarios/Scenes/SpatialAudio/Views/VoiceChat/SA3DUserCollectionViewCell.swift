//
//  AgoraChatRoom3DUserCollectionViewCell.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/31.
//

import UIKit

public enum SA3DUserDirectionType {
    case AgoraChatRoom3DUserDirectionTypeUp
    case AgoraChatRoom3DUserDirectionTypeDown
}

class SA3DUserCollectionViewCell: UICollectionViewCell {
    var rtcUserView: SABaseRtcUserView = .init()

    public var directionType: SA3DUserDirectionType = .AgoraChatRoom3DUserDirectionTypeDown {
        didSet {
            rtcUserView.snp.updateConstraints { make in
                make.top.equalTo(self.contentView).offset(directionType == .AgoraChatRoom3DUserDirectionTypeUp ? 0 : 40)
            }
            contentView.layoutIfNeeded()
        }
    }

    public func refreshUser(with mic: SARoomMic) {
        var status = mic.status
        // 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 5:机器人生效 -1:空闲 -2: 机器人失效
        switch status {
        case -1:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.isHidden = false
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
        case 0:
            rtcUserView.iconView.isHidden = false
            rtcUserView.micView.isHidden = false
            rtcUserView.bgIconView.isHidden = true
            rtcUserView.nameBtn.setImage(nil, for: .normal)
            if mic.member?.mic_status == .mute {
                rtcUserView.micView.setState(.off)
            } else {
                rtcUserView.micView.setState(.on)
            }
        case 1:
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.off)
        case 2:
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.off)
            rtcUserView.bgIconView.isHidden = false
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
        case 3:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_lock", bundleName: "SpatialAudioResource")
            rtcUserView.bgIconView.isHidden = false
        case 4:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.forbidden)
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_lock", bundleName: "SpatialAudioResource")
            rtcUserView.bgIconView.isHidden = false
        case 5:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.on)
            rtcUserView.coverView.isHidden = true
            rtcUserView.activeButton.isHidden = true
        case -2:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.micView.setState(.off)
            rtcUserView.coverView.isHidden = false
            rtcUserView.activeButton.isHidden = false
        default:
            break
        }
        
        rtcUserView.iconView.isHidden = mic.member == nil
        if status != 5 && status != -2 {
            rtcUserView.iconView.sd_setImage(with: URL(string: mic.member?.portrait ?? ""), placeholderImage: nil)
        } else {
            rtcUserView.iconView.image = UIImage.spatial_image(mic.member?.portrait ?? "")
        }
        rtcUserView.nameBtn.setImage((mic.mic_index == 3 || mic.mic_index == 6) ? UIImage.spatial_image("guanfang"): nil, for: .normal)
        let title = mic.status == -1 ? "\(mic.mic_index)" : (mic.member?.name ?? "\(mic.mic_index)")
        rtcUserView.nameBtn.setTitle(title, for: .normal)
    }

    public func refreshVolume(vol: Int) {
        rtcUserView.volume = vol
    }
    
    public var clickBlock: ((Int) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setArrowInfo(imageName: String, margin: CGFloat) {
        rtcUserView.arrowImgMargin = margin
        rtcUserView.arrowImgUrl = imageName
    }
    
    fileprivate func layoutUI() {
        contentView.addSubview(rtcUserView)

        rtcUserView.snp.makeConstraints { make in
            make.left.right.bottom.equalTo(self.contentView)
            make.top.equalTo(self.contentView).offset(-20)
        }
        layoutIfNeeded()
        
        rtcUserView.clickBlock = { [weak self] in
            guard let clickBlock = self?.clickBlock else {
                return
            }
            clickBlock(self!.tag)
        }
    }
}
