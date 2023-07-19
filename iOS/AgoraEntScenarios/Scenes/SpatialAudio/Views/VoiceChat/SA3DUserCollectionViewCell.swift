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

    public var cellType: SABaseUserCellType = .AgoraChatRoomBaseUserCellTypeAdd {
        didSet {
            rtcUserView.cellType = cellType
        }
    }

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
        let user_mic_status = mic.member?.mic_status ?? .none
        if user_mic_status == .mute {
            status = 1
        }
        var bgIcon = ""
        switch status {
        case -1:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.isHidden = false
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "icons／solid／add", bundleName: "VoiceChatRoomResource")
        case 0:
            rtcUserView.iconView.isHidden = false
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.on)
            rtcUserView.bgIconView.isHidden = true
            rtcUserView.nameBtn.setImage(UIImage.sceneImage(name: "", bundleName: "VoiceChatRoomResource"), for: .normal)
        case 1:
            // 需要区分有用户还是没有用户
            bgIcon = mic.member == nil ? "icons／solid／mute" : ""
            if mic.member != nil {
                rtcUserView.micView.isHidden = false
                rtcUserView.micView.setState(.off)
            } else {
                rtcUserView.micView.isHidden = true
            }
            rtcUserView.bgIconView.image = UIImage.spatial_image(bgIcon)
            rtcUserView.bgIconView.isHidden = mic.member != nil
        case 2:
            bgIcon = mic.member == nil ? "icons／solid／mute" : ""
            if mic.member != nil {
                rtcUserView.micView.isHidden = false
                rtcUserView.micView.setState(.off)
            } else {
                rtcUserView.micView.isHidden = true
            }
            rtcUserView.bgIconView.image = UIImage.spatial_image(bgIcon)
            rtcUserView.bgIconView.isHidden = mic.member != nil
        case 3:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "icons／solid／lock", bundleName: "VoiceChatRoomResource")
            rtcUserView.bgIconView.isHidden = false
        case 4:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.forbidden)
            rtcUserView.bgIconView.image = UIImage.sceneImage(name: "icons／solid／lock", bundleName: "VoiceChatRoomResource")
            rtcUserView.bgIconView.isHidden = false
        case 5:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.coverView.isHidden = true
            rtcUserView.activeButton.isHidden = true
        case -2:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
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
        rtcUserView.nameBtn.setImage(UIImage.spatial_image((mic.mic_index == 0 || mic.mic_index == 3 || mic.mic_index == 6) ? "Landlord" : ""), for: .normal)
        let title = mic.status == -1 ? "\(mic.mic_index)" : (mic.member?.name ?? "\(mic.mic_index)")
        rtcUserView.nameBtn.setTitle(title, for: .normal)
    }

    public func refreshVolume(vol: Int) {
        rtcUserView.volume = vol
    }
    
    public func updateAlienMic( flag: Bool) {
        rtcUserView.micView.isHidden = !flag
    }
    
    public var clickBlock: ((Int) -> Void)?
    public var activeBlock: ((SABaseUserCellType) -> Void)?

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
