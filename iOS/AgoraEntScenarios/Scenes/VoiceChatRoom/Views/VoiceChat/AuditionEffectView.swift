//
//  AuditionEffectView.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/9.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage
//import SDWebImageAPNGCoder
//162 height

private let beforPlayTag = 11

final class AuditionEffectView: UIView {
        
    private var type: AUDIO_SETTING_TYPE = .AIAEC
    
    private var beforePlaceHolderImage = UIImage()
    
    private var afterPlaceHolderImage = UIImage()
        
    private lazy var title: UILabel = {
        UILabel(frame: CGRect(x: 20, y: 7, width: self.frame.width-40, height: 18)).textColor(UIColor(0x6C7192)).font(.systemFont(ofSize: 13, weight: .regular)).backgroundColor(.clear)
    }()
    
    private lazy var whiteContainer: UIView = {
        UIView(frame: CGRect(x: 0, y: 32, width: self.frame.width, height: self.frame.height)).backgroundColor(.white)
    }()
    
    private lazy var before: UILabel = {
        UILabel(frame: CGRect(x: 20, y: 28, width: self.whiteContainer.frame.width-40, height: 20)).font(.systemFont(ofSize: 13, weight: .semibold)).textColor(UIColor(0x3C4267)).text("voice_before".voice_localized())
    }()
    
    private lazy var beforeAnimation: UIImageView = {
        UIImageView(frame: CGRect(x: 88, y: 22, width: self.whiteContainer.frame.width-88-73, height: 34)).backgroundColor(.white)
    }()
    
    private lazy var beforePlay: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.whiteContainer.frame.width-44, y: 27, width: 24, height: 24)).tag(beforPlayTag).addTargetFor(self, action: #selector(playAnimation(sender:)), for: .touchUpInside)
    }()
    
    private lazy var beforeSeparaLine: UIView = {
        UIView(frame: CGRect(x: 20, y: self.beforeAnimation.frame.maxY+8, width: self.whiteContainer.frame.width-40, height: 1)).backgroundColor(UIColor(0xF8F5FA))
    }()
    
    private lazy var after: UILabel = {
        UILabel(frame: CGRect(x: 20, y: self.before.frame.maxY+45, width: self.whiteContainer.frame.width-40, height: 20)).font(.systemFont(ofSize: 13, weight: .semibold)).textColor(UIColor(0x3C4267)).text("voice_after".voice_localized())
    }()
    
    private lazy var afterAnimation: UIImageView = {
        UIImageView(frame: CGRect(x: 88, y: self.beforeAnimation.frame.maxY+28, width: self.whiteContainer.frame.width-88-73, height: 34)).backgroundColor(.white)
    }()
    
    private lazy var afterPlay: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.whiteContainer.frame.width-44, y: self.beforePlay.frame.maxY+41, width: 24, height: 24)).tag(12).addTargetFor(self, action: #selector(playAnimation(sender:)), for: .touchUpInside)
    }()
    
    private lazy var afterSeparaLine: UIView = {
        UIView(frame: CGRect(x: 20, y: self.afterAnimation.frame.maxY+8, width: self.whiteContainer.frame.width-40, height: 1)).backgroundColor(UIColor(0xF8F5FA))
    }()

    private override init(frame: CGRect) {
        super.init(frame: frame)
//        SDWebImageCodersManager.sharedInstance().addCoder(SDWebImageAPNGCoder.shared())
    }
    
    convenience init(frame: CGRect,type: AUDIO_SETTING_TYPE) {
        self.init(frame: frame)
        self.type = type
        self.addSubViews([self.title,self.whiteContainer])
        self.whiteContainer.addSubViews([self.before,self.beforeAnimation,self.beforePlay,self.beforeSeparaLine,self.after,self.afterAnimation,self.afterPlay,self.afterSeparaLine])
        self.initializeProperties()
        self.actionEvents()
    }
    
    deinit {
        VoiceRoomRTCManager.getSharedInstance().rtcKit.stopAudioMixing()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func actionEvents() {
        VoiceRoomRTCManager.getSharedInstance().stopMixingClosure = {
            self.beforeAnimation.image = self.beforePlaceHolderImage
            self.afterAnimation.image = self.afterPlaceHolderImage
            if self.afterPlay.isSelected {
                self.afterPlay.setImage(UIImage.sceneImage(name: "play2", bundleName: "VoiceChatRoomResource"), for: .normal)
            }
            if self.beforePlay.isSelected {
                self.beforePlay.setImage(UIImage.sceneImage(name: "play2", bundleName: "VoiceChatRoomResource"), for: .normal)
            }
        }
    }
    
    private func initializeProperties() {
        var text = ""
        switch type {
        case .AIAEC:
            text = "voice_AIAEC_audition".voice_localized()
            self.beforePlaceHolderImage = UIImage.sceneImage(name: "AIAECbefore", bundleName: "VoiceChatRoomResource")!
            self.afterPlaceHolderImage = UIImage.sceneImage(name: "AIAECafter", bundleName: "VoiceChatRoomResource")!
        case .AGC:
            text = "voice_AGC_audition".voice_localized()
            self.beforePlaceHolderImage = UIImage.sceneImage(name: "AGCbefore", bundleName: "VoiceChatRoomResource")!
            self.afterPlaceHolderImage = UIImage.sceneImage(name: "AGCafter", bundleName: "VoiceChatRoomResource")!
        default:
            text = ""
        }
        self.afterPlay.setImage(UIImage.sceneImage(name: "play2", bundleName: "VoiceChatRoomResource"), for: .normal)
        self.beforePlay.setImage(UIImage.sceneImage(name: "play2", bundleName: "VoiceChatRoomResource"), for: .normal)
        self.beforeAnimation.image = self.beforePlaceHolderImage
        self.afterAnimation.image = self.afterPlaceHolderImage
        self.title.text = text
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        
    }
    
    @objc private func playAnimation(sender: UIButton) {
        var resourceName = "2xAIAEC_"
        var wavName = "AIAEC"
        var type = "m4a"
        if self.type == .AGC {
            resourceName = "2xAGC_"
            type = "WAV"
            wavName = "AGC"
        }
        
        if sender.tag == beforPlayTag {
            self.beforePlay.isSelected = !self.beforePlay.isSelected
            if self.beforePlay.isSelected {
                self.afterPlay.isSelected = !self.beforePlay.isSelected
            }
            resourceName += "Before"
            wavName += "-Before"
        } else {
            self.afterPlay.isSelected = !self.afterPlay.isSelected
            if self.afterPlay.isSelected  {
                self.beforePlay.isSelected = !self.afterPlay.isSelected
            }
            resourceName += "After"
            wavName += "-After"
        }
        guard let path = Bundle.voiceRoomBundle.path(forResource: resourceName, ofType: "png") else { return }

        let statusStr = sender.tag == beforPlayTag ? "before" : "after"
        let resourceNameStr = resourceName.contains("AGC") ? "agc" : "aec"
        let wavPath = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/music/voice_sample_\(resourceNameStr)_\(statusStr).\(type)"

        VoiceRoomRTCManager.getSharedInstance().rtcKit.stopAudioMixing()
        self.afterPlay.setImage(UIImage.sceneImage(name: "play2"), for: .normal)
        self.beforePlay.setImage(UIImage.sceneImage(name: "play2"), for: .normal)
        if self.beforePlay.isSelected {
            self.afterPlay.setImage(UIImage.sceneImage(name: "play2"), for: .normal)
            self.beforePlay.setImage(UIImage.sceneImage(name: "zanting"), for: .normal)
            self.afterAnimation.image = self.afterPlaceHolderImage
            self.beforeAnimation.sd_setImage(with: URL(fileURLWithPath: path), placeholderImage: self.beforePlaceHolderImage)
            VoiceRoomRTCManager.getSharedInstance().rtcKit.startAudioMixing(wavPath, loopback: false, cycle: 1)
        } else {
            self.beforeAnimation.image = self.beforePlaceHolderImage
        }
        if self.afterPlay.isSelected {
            self.beforePlay.setImage(UIImage.sceneImage(name: "play2"), for: .normal)
            self.afterPlay.setImage(UIImage.sceneImage(name: "zanting"), for: .normal)
            self.beforeAnimation.image = self.afterPlaceHolderImage
            self.afterAnimation.sd_setImage(with: URL(fileURLWithPath: path), placeholderImage: self.afterPlaceHolderImage)
            VoiceRoomRTCManager.getSharedInstance().rtcKit.startAudioMixing(wavPath, loopback: false, cycle: 1)
        } else {
            self.afterAnimation.image = self.afterPlaceHolderImage
        }
    }
    
}
