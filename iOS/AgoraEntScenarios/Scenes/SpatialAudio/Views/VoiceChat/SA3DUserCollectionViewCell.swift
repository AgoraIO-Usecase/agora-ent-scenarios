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
                make.top.equalTo(self.contentView).offset(directionType == .AgoraChatRoom3DUserDirectionTypeUp ? 0~ : 40~)
            }
            contentView.layoutIfNeeded()
        }
    }

    public func refreshUser(with mic: SARoomMic) {
        let status = mic.status
        var bgIcon = ""
        switch status {
        case -1:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.isHidden = false
            rtcUserView.bgIconView.image = UIImage("icons／solid／add")
        case 0:
            rtcUserView.iconView.isHidden = false
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.on)
            rtcUserView.bgIconView.isHidden = true
            rtcUserView.nameBtn.setImage(UIImage(""), for: .normal)
        case 1:
            // 需要区分有用户还是没有用户
            bgIcon = mic.member == nil ? "icons／solid／mute" : ""
            if mic.member != nil {
                rtcUserView.micView.isHidden = false
                rtcUserView.micView.setState(.off)
            } else {
                rtcUserView.micView.isHidden = true
            }
            rtcUserView.bgIconView.image = UIImage(bgIcon)
            rtcUserView.bgIconView.isHidden = mic.member != nil
        case 2:
            bgIcon = mic.member == nil ? "icons／solid／mute" : ""
            if mic.member != nil {
                rtcUserView.micView.isHidden = false
                rtcUserView.micView.setState(.off)
            } else {
                rtcUserView.micView.isHidden = true
            }
            rtcUserView.bgIconView.image = UIImage(bgIcon)
            rtcUserView.bgIconView.isHidden = mic.member != nil
        case 3:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = true
            rtcUserView.bgIconView.image = UIImage("icons／solid／lock")
            rtcUserView.bgIconView.isHidden = false
        case 4:
            rtcUserView.iconView.isHidden = true
            rtcUserView.micView.isHidden = false
            rtcUserView.micView.setState(.forbidden)
            rtcUserView.bgIconView.image = UIImage("icons／solid／lock")
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
            rtcUserView.iconView.image = UIImage(mic.member?.portrait ?? "")
        }
        rtcUserView.nameBtn.setImage(UIImage((mic.mic_index == 1 || mic.mic_index == 3 || mic.mic_index == 6) ? "Landlord" : ""), for: .normal)
        rtcUserView.nameBtn.setTitle(mic.member?.name ?? "\(mic.mic_index)", for: .normal)
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
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
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
            make.top.equalTo(self.contentView).offset(-20~)
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
