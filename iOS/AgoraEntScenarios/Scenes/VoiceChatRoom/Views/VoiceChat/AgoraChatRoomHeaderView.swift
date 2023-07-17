//
//  AgoraChatRoomHeaderView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/29.
//

import SnapKit
import UIKit

public enum HEADER_ACTION {
    case back
    case notice
    case soundClick
    case rank
    case popBack
    case members
    case more
}

class AgoraChatRoomHeaderView: UIView {
    typealias resBlock = (HEADER_ACTION) -> Void

    private var backBtn: UIButton = .init()
    private lazy var moreBtn: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "icon_live_more", bundleName: "VoiceChatRoomResource"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFit
        button.addTarget(self, action: #selector(clickMore), for: .touchUpInside)
        return button
    }()
    private var iconImgView: UIImageView = .init()
    private var titleLabel: UILabel = .init()
    private var roomLabel: UILabel = .init()
    private var infoView: UIView = .init()
    private var richView: UIView = .init()
    private var totalCountLabel: UILabel = .init()
    private var giftBtn: UIButton = .init()
    private var lookBtn: UIButton = .init()
    private var noticeView: UIView = .init()
    private var configView: UIView = .init()
    private var soundSetLabel: UILabel = .init()
    private var soundClickBtn: UIButton = .init()

    private var rankFBtn: UIButton = .init() // 榜一大哥
    private var rankSBtn: UIButton = .init() // 榜二土豪
    private var rankTBtn: UIButton = .init() // 榜三小弟

    var completeBlock: resBlock?
    
    func updateHeader(with room_entity: VRRoomEntity?) {
        guard let room = room_entity else {return}
        guard let owner = room.owner else { return }
        self.iconImgView.sd_setImage(with: URL(string: owner.portrait ?? ""), placeholderImage:UIImage.sceneImage(name: "", bundleName: "VoiceChatRoomResource"))
        self.titleLabel.text = owner.name
        self.roomLabel.text = room.name
        self.lookBtn.setTitle(" \(room.click_count ?? 0)", for: .normal)
        self.totalCountLabel.text = "\((room.member_list?.count ?? 0)+(room.owner?.chat_uid ?? "" == VoiceRoomUserInfo.shared.user?.chat_uid ?? "" ? 3:4))"
        let gift_count = room.gift_amount ?? 0
        let count = gift_count >= 1000 ? afterDecimals(value: gift_count) : "\(gift_count)"
        self.giftBtn.setTitle(" \(count)", for: .normal)
        self.giftBtn.snp.updateConstraints { make in
            make.width.greaterThanOrEqualTo(gift_count >= 100 ? 50 : 40)
        }
        self.soundSetLabel.text = getSoundType(with: room.sound_effect)
        updateGiftList(with: room)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func layoutUI() {
        backBtn.setBackgroundImage(UIImage(systemName: "xmark")?.withTintColor(.white, renderingMode: .alwaysOriginal),
                                   for: .normal)
        backBtn.addTarget(self, action: #selector(back), for: .touchUpInside)
        backBtn.vm_expandSize(size: 20)
        addSubview(backBtn)

        addSubview(moreBtn)

        iconImgView.layer.cornerRadius = 16
        iconImgView.layer.masksToBounds = true
        addSubview(iconImgView)

        roomLabel.textColor = .white
        roomLabel.text = ""
        roomLabel.font = UIFont.systemFont(ofSize: 14)
        addSubview(roomLabel)

        titleLabel.textColor = .white
        titleLabel.font = UIFont.systemFont(ofSize: 10)
        titleLabel.alpha = 0.8
        addSubview(titleLabel)

        totalCountLabel.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
        totalCountLabel.layer.cornerRadius = 13
        totalCountLabel.text = "0"
        totalCountLabel.font = UIFont.systemFont(ofSize: 11)
        totalCountLabel.textColor = .white
        totalCountLabel.textAlignment = .center
        totalCountLabel.layer.masksToBounds = true
        totalCountLabel.isUserInteractionEnabled = true
        totalCountLabel.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(members)))
        addSubview(totalCountLabel)

        rankFBtn.layer.cornerRadius = 13
        rankFBtn.layer.masksToBounds = true
        rankFBtn.addTargetFor(self, action: #selector(rankClick), for: .touchUpInside)
        addSubview(rankFBtn)
        rankFBtn.isHidden = true

        rankSBtn.layer.cornerRadius = 13
        rankSBtn.layer.masksToBounds = true
        rankSBtn.addTargetFor(self, action: #selector(rankClick), for: .touchUpInside)
        addSubview(rankSBtn)
        rankSBtn.isHidden = true

        rankTBtn.layer.cornerRadius = 13
        rankTBtn.layer.masksToBounds = true
        rankTBtn.addTargetFor(self, action: #selector(rankClick), for: .touchUpInside)
        addSubview(rankTBtn)
        rankTBtn.isHidden = true

        configView.layer.cornerRadius = 11
        configView.layer.masksToBounds = true
        configView.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1)
        addSubview(configView)

        let soundSetView = UIView()
        addSubview(soundSetView)

        soundSetLabel.text = ""
        soundSetLabel.textColor = .white
        soundSetLabel.font = UIFont.systemFont(ofSize: 10)
        addSubview(soundSetLabel)

        let soundImgView = UIImageView()
        soundImgView.image = UIImage.sceneImage(name: "arrow_right", bundleName: "VoiceChatRoomResource")
        addSubview(soundImgView)

        soundClickBtn.backgroundColor = .clear
        soundClickBtn.addTargetFor(self, action: #selector(soundClick), for: .touchUpInside)
        addSubview(soundClickBtn)
        soundClickBtn.vm_expandSize(size: 20)

        giftBtn.layer.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1).cgColor
        giftBtn.layer.cornerRadius = 11
        giftBtn.setImage(UIImage.sceneImage(name: "liwu", bundleName: "VoiceChatRoomResource"), for: .normal)
        giftBtn.setTitle(" 0", for: .normal)
        giftBtn.titleLabel?.font = UIFont.systemFont(ofSize: 10)
        giftBtn.isUserInteractionEnabled = false
        addSubview(giftBtn)

        lookBtn.layer.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1).cgColor
        lookBtn.layer.cornerRadius = 11
        lookBtn.titleLabel?.font = UIFont.systemFont(ofSize: 10)
        lookBtn.setTitle(" 0", for: .normal)
        lookBtn.isUserInteractionEnabled = false
        lookBtn.setImage(UIImage(named: "guankan"), for: .normal)
        addSubview(lookBtn)

        noticeView.layer.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1).cgColor
        noticeView.layer.cornerRadius = 11
        addSubview(noticeView)

        let tap = UITapGestureRecognizer(target: self, action: #selector(click))
        noticeView.addGestureRecognizer(tap)
        noticeView.isUserInteractionEnabled = true

        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "gonggao", bundleName: "VoiceChatRoomResource")
        noticeView.addSubview(imgView)

        let notiLabel = UILabel()
        notiLabel.text = LanguageManager.localValue(key: "Notice")
        notiLabel.font = UIFont.systemFont(ofSize: 12)
        notiLabel.textColor = .white
        noticeView.addSubview(notiLabel)

        let arrowImgView = UIImageView()
        arrowImgView.image = UIImage.sceneImage(name: "arrow_right", bundleName: "VoiceChatRoomResource")
        noticeView.addSubview(arrowImgView)

        let isHairScreen =  Screen.isFullScreen
        backBtn.snp.makeConstraints { make in
            make.trailing.equalTo(-15)
            make.top.equalTo(isHairScreen ? 54 : 54 - 25)
        }

        iconImgView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(12)
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(32)
        }

        roomLabel.snp.makeConstraints { make in
            make.left.equalTo(self.iconImgView.snp.right).offset(8)
            make.height.equalTo(20)
            make.width.lessThanOrEqualTo(150)
            make.top.equalTo(self.iconImgView)
        }

        titleLabel.snp.makeConstraints { make in
            make.left.equalTo(self.iconImgView.snp.right).offset(8)
            make.height.equalTo(14)
            make.width.lessThanOrEqualTo(150)
            make.bottom.equalTo(self.iconImgView)
        }
        
        moreBtn.snp.makeConstraints { make in
            make.trailing.equalTo(backBtn.snp_leadingMargin).offset(-18)
            make.centerY.equalTo(backBtn.snp.centerY)
            make.width.equalTo(24)
        }
        
        totalCountLabel.snp.makeConstraints { make in
            make.right.equalTo(moreBtn.snp.left).offset(-10)
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
        }

        soundImgView.snp.makeConstraints { make in
            make.top.equalTo(isHairScreen ? 98 : 98 - 25)
            make.right.equalTo(self.snp.right).offset(-15)
            make.width.height.equalTo(10)
        }

        soundSetLabel.snp.makeConstraints { make in
            make.right.equalTo(soundImgView.snp.left).offset(-2)
            make.centerY.equalTo(soundImgView)
        }

        configView.snp.makeConstraints { make in
            make.right.equalTo(self.snp.right).offset(19)
            make.height.equalTo(22)
            make.left.equalTo(soundSetLabel.snp.left).offset(-9)
            make.centerY.equalTo(soundImgView)
        }

        soundClickBtn.snp.makeConstraints { make in
            make.top.right.bottom.left.equalTo(self.configView)
        }

        giftBtn.snp.makeConstraints { make in
            make.left.equalTo(self.snp.left).offset(15)
            make.centerY.equalTo(self.configView)
            make.width.greaterThanOrEqualTo(50)
            make.height.equalTo(22)
        }

        lookBtn.snp.makeConstraints { make in
            make.left.equalTo(self.giftBtn.snp.right).offset(5)
            make.centerY.equalTo(self.configView)
            make.width.greaterThanOrEqualTo(40)
            make.height.equalTo(22)
        }

        noticeView.snp.makeConstraints { make in
            make.left.equalTo(self.lookBtn.snp.right).offset(5)
            make.centerY.equalTo(self.configView)
            make.height.equalTo(22)
        }

        imgView.snp.makeConstraints { make in
            make.left.equalTo(self.noticeView).offset(5)
            make.centerY.equalTo(self.noticeView)
            make.width.height.equalTo(15)
        }

        arrowImgView.snp.makeConstraints { make in
            make.right.equalTo(self.noticeView).offset(-5)
            make.centerY.equalTo(self.noticeView)
            make.width.height.equalTo(10)
        }

        notiLabel.snp.makeConstraints { make in
            make.left.equalTo(imgView.snp.right).offset(5)
            make.right.equalTo(arrowImgView.snp.left).offset(-5)
            make.centerY.equalTo(self.noticeView)
        }

        rankFBtn.snp.makeConstraints { make in
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
            make.right.equalTo(self.totalCountLabel.snp.left).offset(-70)
        }
        rankSBtn.snp.makeConstraints { make in
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
            make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
        }
        rankTBtn.snp.makeConstraints { make in
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
            make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
        }
    }

    func afterDecimals(value: Int) -> String {
        let intVal = value / 1000
        let doubleVal = value % 1000
        let suffixValue = doubleVal / 100
        let newValue = "\(intVal)" + "." + "\(suffixValue)" + "K"
        return newValue
    }
    
    @objc private func members() {
        guard let block = completeBlock else { return }
        block(.members)
    }

    @objc private func back() {
        guard let block = completeBlock else { return }
        block(.back)
    }

    @objc private func click() {
        guard let block = completeBlock else { return }
        block(.notice)
    }

    @objc private func soundClick() {
        guard let block = completeBlock else { return }
        block(.soundClick)
    }

    @objc private func rankClick() {
        guard let block = completeBlock else { return }
        block(.rank)
    }
    @objc
    private func clickMore() {
        guard let block = completeBlock else { return }
        block(.more)
    }

    private func updateGiftList(with room: VRRoomEntity) {
        // 土豪榜展示逻辑
        if let rankList = room.ranking_list {
            if rankList.count == 0 { return }

            if let fImg: String = rankList[0].portrait {
                
                rankFBtn.isHidden = false
                rankFBtn.snp.updateConstraints { make in
                    make.right.equalTo(totalCountLabel.snp.left).offset(-10)
                }
                var img_first: UIImage?
                getImage(with: fImg) { img in
                    img_first = img
                    DispatchQueue.main.async {[weak self] in
                        self?.rankFBtn.setImage(img_first, for: .normal)
                    }
                }
                
            }

            if rankList.count < 2 { return }
            if let sImg = rankList[1].portrait {
                
                rankFBtn.isHidden = false
                rankSBtn.isHidden = false
                rankFBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
                }
                rankSBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
                }
                
                var img_second: UIImage?
                getImage(with: sImg) { img in
                    img_second = img
                    DispatchQueue.main.async {[weak self] in
                        self?.rankSBtn.setImage(img_second, for: .normal)
                    }
                }

            }

            if rankList.count < 3 { return }
            if let tImg = rankList[2].portrait {
                
                rankFBtn.isHidden = false
                rankSBtn.isHidden = false
                rankTBtn.isHidden = false
                rankFBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-70)
                }
                rankSBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
                }
                rankTBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
                }
                
                var img_third: UIImage?
                getImage(with: tImg) { img in
                    img_third = img
                    DispatchQueue.main.async {[weak self] in
                        self?.rankTBtn.setImage(img_third, for: .normal)
                    }
                }
            }
        } else {
            rankFBtn.isHidden = true
            rankSBtn.isHidden = true
            rankTBtn.isHidden = true
        }
    }
    
    private func getSoundType(with index: Int) -> String {
        var soundType: String = "Social Chat".voice_localized()
        switch index {
        case 1:
            soundType = "Social Chat".voice_localized()
        case 2:
            soundType = "Karaoke".voice_localized()
        case 3:
            soundType = "Gaming Buddy".voice_localized()
        case 4:
            soundType = "Professional Podcaster".voice_localized()
        default:
            soundType = "Social Chat".voice_localized()
        }
        return soundType
    }
    
    func getImage(with url: String,  completion:(@escaping (UIImage?) -> Void)){
        DispatchQueue.global().async{
            if let img_url: URL = URL(string: url) {
                if let data: Data = try? Data(contentsOf: img_url) {
                    if let img: UIImage = UIImage(data: data) {
                        completion(img)
                    } else {
                        completion(nil)
                    }
                } else {
                    completion(nil)
                }
            } else {
                completion(nil)
            }
        }
    }
}
