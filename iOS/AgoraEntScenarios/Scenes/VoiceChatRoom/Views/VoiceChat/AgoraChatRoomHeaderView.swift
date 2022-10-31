//
//  AgoraChatRoomHeaderView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/29.
//

import UIKit
import SnapKit

public enum HEADER_ACTION {
    case back
    case notice
    case soundClick
    case rank
    case popBack
}

class AgoraChatRoomHeaderView: UIView {
    
    typealias resBlock = (HEADER_ACTION) -> Void
    
    private var backBtn: UIButton = UIButton()
    private var iconImgView: UIImageView = UIImageView()
    private var titleLabel: UILabel = UILabel()
    private var roomLabel: UILabel = UILabel()
    private var infoView: UIView = UIView()
    private var richView: UIView = UIView()
    private var totalCountLabel: UILabel = UILabel()
    private var giftBtn: UIButton = UIButton()
    private var lookBtn: UIButton = UIButton()
    private var noticeView: UIView = UIView()
    private var configView: UIView = UIView()
    private var soundSetLabel: UILabel = UILabel()
    private var soundClickBtn: UIButton = UIButton()
    
    private var rankFBtn: UIButton = UIButton() //榜一大哥
    private var rankSBtn: UIButton = UIButton() //榜二土豪
    private var rankTBtn: UIButton = UIButton() //榜三小弟
    
    var completeBlock: resBlock?
    
    var entity: VRRoomEntity = VRRoomEntity() {
        didSet {
            guard let user = VoiceRoomUserInfo.shared.user else {return}
            self.iconImgView.image = UIImage(user.portrait ?? "avatar1")
            self.titleLabel.text = user.name
            self.roomLabel.text = entity.name
            self.lookBtn.setTitle(" \(entity.click_count ?? 0)" , for: .normal)
            self.totalCountLabel.text = "\(entity.member_count ?? 0)"
            let gift_count = entity.gift_amount ?? 0
            let count = gift_count >= 1000 ? afterDecimals(value: gift_count) : "\(gift_count)"
            self.giftBtn.setTitle(" \(count)", for: .normal)
            self.giftBtn.snp.updateConstraints { make in
                make.width.greaterThanOrEqualTo(gift_count >= 100 ? 50 : 40)
            }
            self.soundSetLabel.text = entity.sound_effect?.localized()
            updateGiftList()
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        self.backBtn.setBackgroundImage(UIImage("icon／outline／left"), for: .normal)
        self.backBtn.addTarget(self, action: #selector(back), for: .touchUpInside)
        self.backBtn.vm_expandSize(size: 20)
        self.addSubview(self.backBtn)
        
        self.iconImgView.layer.cornerRadius = 16~;
        self.iconImgView.layer.masksToBounds = true;
        guard let user = VoiceRoomUserInfo.shared.user else {return}
        self.iconImgView.image = UIImage(user.portrait ?? "avatar1")
        self.addSubview(self.iconImgView)
        
        self.roomLabel.textColor = .white;
        self.roomLabel.text = entity.name
        self.roomLabel.font = UIFont.systemFont(ofSize: 14)
        self.addSubview(self.roomLabel)
        
        self.titleLabel.textColor = .white
        self.titleLabel.font = UIFont.systemFont(ofSize: 10)
        self.titleLabel.text = user.name
        self.titleLabel.alpha = 0.8
        self.addSubview(self.titleLabel)
        
        self.totalCountLabel.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
        self.totalCountLabel.layer.cornerRadius = 13~
        self.totalCountLabel.text = "\(entity.member_count ?? 0)"
        self.totalCountLabel.font = UIFont.systemFont(ofSize: 11)
        self.totalCountLabel.textColor = .white
        self.totalCountLabel.textAlignment = .center
        self.totalCountLabel.layer.masksToBounds = true;
        self.addSubview(self.totalCountLabel)

        self.rankFBtn.layer.cornerRadius = 13~
        self.rankFBtn.layer.masksToBounds = true
        self.rankFBtn.addTargetFor(self, action: #selector(rankClick), for: .touchUpInside)
        self.addSubview(self.rankFBtn)
        self.rankFBtn.isHidden = true
        
        self.rankSBtn.layer.cornerRadius = 13~
        self.rankSBtn.layer.masksToBounds = true
        self.rankSBtn.addTargetFor(self, action: #selector(rankClick), for: .touchUpInside)
        self.addSubview(self.rankSBtn)
        self.rankSBtn.isHidden = true
        
        self.rankTBtn.layer.cornerRadius = 13~
        self.rankTBtn.layer.masksToBounds = true
        self.rankTBtn.addTargetFor(self, action: #selector(rankClick), for: .touchUpInside)
        self.addSubview(self.rankTBtn)
        self.rankTBtn.isHidden = true
        
        self.configView.layer.cornerRadius = 11~;
        self.configView.layer.masksToBounds = true;
        self.configView.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1)
        self.addSubview(self.configView)

        let soundSetView = UIView()
        self.addSubview(soundSetView)
        
        self.soundSetLabel.text = entity.sound_effect ?? LanguageManager.localValue(key: "Social Chat")
        self.soundSetLabel.textColor = .white
        self.soundSetLabel.font = UIFont.systemFont(ofSize: 10)
        self.addSubview(self.soundSetLabel)
        
        let soundImgView = UIImageView()
        soundImgView.image = UIImage("icons／outlined／arrow_right")
        self.addSubview(soundImgView)
        
        self.soundClickBtn.backgroundColor = .clear
        self.soundClickBtn.addTargetFor(self, action: #selector(soundClick), for: .touchUpInside)
        self.addSubview(self.soundClickBtn)
        soundClickBtn.vm_expandSize(size: 20)
        
        self.giftBtn.layer.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1).cgColor
        self.giftBtn.layer.cornerRadius = 11~
        self.giftBtn.setImage(UIImage("liwu"), for: .normal)
        self.giftBtn.setTitle(" \(entity.gift_amount ?? 0)", for: .normal)
        self.giftBtn.titleLabel?.font = UIFont.systemFont(ofSize: 10)
        self.giftBtn.isUserInteractionEnabled = false
        self.addSubview(self.giftBtn)
        
        self.lookBtn.layer.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1).cgColor
        self.lookBtn.layer.cornerRadius = 11~
        self.lookBtn.titleLabel?.font = UIFont.systemFont(ofSize: 10)~
        self.lookBtn.setTitle(" \(entity.click_count ?? 0)", for: .normal)
        self.lookBtn.isUserInteractionEnabled = false
        self.lookBtn.setImage(UIImage(named:"guankan"), for: .normal)
        self.addSubview(self.lookBtn)
        
        self.noticeView.layer.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1).cgColor
        self.noticeView.layer.cornerRadius = 11~;
        self.addSubview(self.noticeView)
        
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(click))
        self.noticeView.addGestureRecognizer(tap)
        self.noticeView.isUserInteractionEnabled = true
        
        let imgView = UIImageView()
        imgView.image = UIImage("gonggao")
        self.noticeView.addSubview(imgView)
        
        let notiLabel = UILabel()
        notiLabel.text = LanguageManager.localValue(key: "Notice")
        notiLabel.font = UIFont.systemFont(ofSize: 12)
        notiLabel.textColor = .white
        self.noticeView.addSubview(notiLabel)
        
        let arrowImgView = UIImageView()
        arrowImgView.image = UIImage("icons／outlined／arrow_right")
        self.noticeView.addSubview(arrowImgView)
        
        let isHairScreen = SwiftyFitsize.isFullScreen
        self.backBtn.snp.makeConstraints { make in
            make.left.equalTo(12);
            make.top.equalTo(isHairScreen ? 54~ : 54~ - 25);
            make.width.height.equalTo(24~);
        }
        
        self.iconImgView.snp.makeConstraints { make in
            make.left.equalTo(self.backBtn.snp.right).offset(5);
            make.centerY.equalTo(self.backBtn);
            make.width.height.equalTo(32~);
        }
        
        self.roomLabel.snp.makeConstraints { make in
            make.left.equalTo(self.iconImgView.snp.right).offset(8);
            make.height.equalTo(20);
            make.width.lessThanOrEqualTo(150~);
            make.top.equalTo(self.iconImgView);
        }
        
        self.titleLabel.snp.makeConstraints { make in
            make.left.equalTo(self.iconImgView.snp.right).offset(8);
            make.height.equalTo(14);
            make.width.lessThanOrEqualTo(150~);
            make.bottom.equalTo(self.iconImgView);
        }
        
        self.totalCountLabel.snp.makeConstraints { make in
            make.right.equalTo(self.snp.right).offset(-16);
            make.centerY.equalTo(self.backBtn);
            make.width.height.equalTo(26~);
        }

        soundImgView.snp.makeConstraints { make in
            make.top.equalTo(isHairScreen ? 98~ : 98~ - 25);
            make.right.equalTo(self.snp.right).offset(-15);
            make.width.height.equalTo(10~);
        }
        
        self.soundSetLabel.snp.makeConstraints { make in
            make.right.equalTo(soundImgView.snp.left).offset(-2);
            make.centerY.equalTo(soundImgView);
        }
        
        self.configView.snp.makeConstraints { make in
            make.right.equalTo(self.snp.right).offset(19);
            make.height.equalTo(22~);
            make.left.equalTo(soundSetLabel.snp.left).offset(-9)
            make.centerY.equalTo(soundImgView)
        }
        
        self.soundClickBtn.snp.makeConstraints { make in
            make.top.right.bottom.left.equalTo(self.configView)
        }

        self.giftBtn.snp.makeConstraints { make in
            make.left.equalTo(self.snp.left).offset(15);
            make.centerY.equalTo(self.configView)
            make.width.greaterThanOrEqualTo(50)
            make.height.equalTo(22~);
        }
        
        self.lookBtn.snp.makeConstraints { make in
            make.left.equalTo(self.giftBtn.snp.right).offset(5);
            make.centerY.equalTo(self.configView)
            make.width.greaterThanOrEqualTo(40)
            make.height.equalTo(22~);
        }
        
        self.noticeView.snp.makeConstraints { make in
            make.left.equalTo(self.lookBtn.snp.right).offset(5);
            make.centerY.equalTo(self.configView);
            make.height.equalTo(22~);
        }
        
        imgView.snp.makeConstraints { make in
            make.left.equalTo(self.noticeView).offset(5);
            make.centerY.equalTo(self.noticeView);
            make.width.height.equalTo(15~);
        }
        
        arrowImgView.snp.makeConstraints { make in
            make.right.equalTo(self.noticeView).offset(-5~);
            make.centerY.equalTo(self.noticeView);
            make.width.height.equalTo(10~);
        }
        
        notiLabel.snp.makeConstraints { make in
            make.left.equalTo(imgView.snp.right).offset(5~);
            make.right.equalTo(arrowImgView.snp.left).offset(-5~);
            make.centerY.equalTo(self.noticeView);
        }
        
        self.rankFBtn.snp.makeConstraints { make in
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
            make.right.equalTo(self.totalCountLabel.snp.left).offset(-70)
        }
        self.rankSBtn.snp.makeConstraints { make in
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
            make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
        }
        self.rankTBtn.snp.makeConstraints { make in
            make.centerY.equalTo(self.backBtn)
            make.width.height.equalTo(26)
            make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
        }

    }
    
    func afterDecimals(value: Int) -> String {
        let intVal  = value / 1000
        let doubleVal = value % 1000
        let suffixValue = doubleVal / 100
        let newValue = "\(intVal)" + "." + "\(suffixValue)" + "K"
        return newValue
    }
    
    @objc private func back() {
        guard let block = completeBlock else {return}
        block(.back)
    }
    
    @objc private func click() {
        guard let block = completeBlock else {return}
        block(.notice)
    }
    
    @objc private func soundClick() {
        guard let block = completeBlock else {return}
        block(.soundClick)
    }
    
    @objc private func rankClick() {
        guard let block = completeBlock else {return}
        block(.rank)
    }
    
    private func updateGiftList() {
        //土豪榜展示逻辑
        if let rankList = entity.ranking_list {
            
            if rankList.count == 0 {return}
            
            if let fImg = rankList[0].portrait {
                self.rankFBtn.setImage(UIImage(fImg), for: .normal)
                self.rankFBtn.isHidden = false
                self.rankFBtn.snp.updateConstraints { make in
//                    make.centerY.equalTo(self.backBtn)
//                    make.width.height.equalTo(26)
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
                }
            }
            
            if rankList.count < 2 {return}
            if let sImg = rankList[1].portrait {
                self.rankSBtn.setImage(UIImage(sImg), for: .normal)
                self.rankFBtn.isHidden = false
                self.rankSBtn.isHidden = false
//                self.rankFBtn.snp.remakeConstraints { make in
//                    make.centerY.equalTo(self.backBtn)
//                    make.width.height.equalTo(26)
//                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
//                }
//                self.rankSBtn.snp.remakeConstraints { make in
//                    make.centerY.equalTo(self.backBtn)
//                    make.width.height.equalTo(26)
//                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
//                }
                self.rankFBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
                }
                self.rankSBtn.snp.updateConstraints { make in
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
                }
            }
            
            if rankList.count < 3 {return}
            if let tImg = rankList[2].portrait {
                self.rankTBtn.setImage(UIImage(tImg), for: .normal)
                self.rankFBtn.isHidden = false
                self.rankSBtn.isHidden = false
                self.rankTBtn.isHidden = false
                self.rankFBtn.snp.updateConstraints { make in
//                    make.centerY.equalTo(self.backBtn)
//                    make.width.height.equalTo(26)
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-70)
                }
                self.rankSBtn.snp.updateConstraints { make in
//                    make.centerY.equalTo(self.backBtn)
//                    make.width.height.equalTo(26)
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-40)
                }
                self.rankTBtn.snp.updateConstraints { make in
//                    make.centerY.equalTo(self.backBtn)
//                    make.width.height.equalTo(26)
                    make.right.equalTo(self.totalCountLabel.snp.left).offset(-10)
                }
            }
        } else {
            self.rankFBtn.isHidden = true
            self.rankSBtn.isHidden = true
            self.rankTBtn.isHidden = true
        }

    }
}
