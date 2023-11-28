//
//  AgoraChatRoomBaseUserCollectionViewCell.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/29.
//

import UIKit

class SABaseUserCollectionViewCell: UICollectionViewCell {
    private var rtcUserView: SABaseRtcUserView = .init()

    public var cellType: SABaseUserCellType = .AgoraChatRoomBaseUserCellTypeAdd {
        didSet {
            rtcUserView.cellType = cellType
        }
    }

    var clickBlock: ((Int) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    fileprivate func layoutUI() {
        rtcUserView.clickBlock = { [weak self] in
            guard let clickBlock = self?.clickBlock else {
                return
            }
            clickBlock(self!.tag)
        }
        contentView.addSubview(rtcUserView)

        rtcUserView.snp.makeConstraints { make in
            make.left.right.bottom.top.equalTo(self.contentView)
        }
    }

    public func refreshUser(with mic: SARoomMic) {
        let status = mic.status
        switch status {
        case -1:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.isHidden = false
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
        case 0:
            rtcUserView.iconView.isHidden = false
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.on)
            rtcUserView.bgIconView.isHidden = true
            rtcUserView.nameBtn.setImage(UIImage.sceneImage(name: "", bundleName: "VoiceChatRoomResource"), for: .normal)
        case 1:
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.off)
        case 2:
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "sa_ic_seat_empty", bundleName: "SpatialAudioResource")
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.off)
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
        default:
            break
        }

        rtcUserView.iconView.isHidden = mic.member == nil
        rtcUserView.iconView.sd_setImage(with: URL(string: mic.member?.portrait ?? ""), placeholderImage: nil)
        rtcUserView.nameBtn.setImage(UIImage.sceneImage(name:mic.mic_index == 0 ? "Landlord" : ""), for: .normal)
        rtcUserView.nameBtn.setTitle(mic.member?.name ?? "\(mic.mic_index)", for: .normal)
    }

    public func refreshVolume(vol: Int) {
        rtcUserView.volume = vol
    }
}
