//
//  AgoraChatRoomBaseUserCollectionViewCell.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/29.
//

import UIKit



class AgoraChatRoomBaseUserCollectionViewCell: UICollectionViewCell {
    
    private var rtcUserView: AgoraChatRoomBaseRtcUserView = AgoraChatRoomBaseRtcUserView()
    
    public var cellType: AgoraChatRoomBaseUserCellType = .AgoraChatRoomBaseUserCellTypeAdd {
        didSet {
            rtcUserView.cellType = cellType
        }
    }
    
    var clickBlock: ((Int) -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func layoutUI() {
        
        rtcUserView.clickBlock = {[weak self] in
            guard let clickBlock = self?.clickBlock else {
                return
            }
            clickBlock(self!.tag)
        }
        self.contentView.addSubview(rtcUserView)
        
        rtcUserView.snp.makeConstraints { make in
            make.left.right.bottom.top.equalTo(self.contentView)
        }
        
    }
    
    public func refreshUser(with mic: VRRoomMic) {
        let status = mic.status
        var bgIcon: String = ""
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
            //需要区分有用户还是没有用户
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
        default:
            break
        }

        rtcUserView.iconView.isHidden = mic.member == nil
        rtcUserView.iconView.image = UIImage(mic.member?.portrait ?? "")
        rtcUserView.nameBtn.setImage(UIImage(mic.mic_index == 0 ? "Landlord" : ""), for: .normal)
        rtcUserView.nameBtn.setTitle(mic.member?.name ?? "\(mic.mic_index)", for: .normal)
    }
    
    public func refreshVolume(vol: Int) {
        rtcUserView.volume = vol
    }
}

