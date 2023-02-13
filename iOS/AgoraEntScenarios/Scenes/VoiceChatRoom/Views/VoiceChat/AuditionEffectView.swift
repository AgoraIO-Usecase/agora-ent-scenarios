//
//  AuditionEffectView.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/9.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage.SDWebImageCodersManager
import SDWebImageAPNGCoder
//162 height
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
        UILabel(frame: CGRect(x: 20, y: 28, width: self.whiteContainer.frame.width-40, height: 20)).font(.systemFont(ofSize: 13, weight: .semibold)).textColor(UIColor(0x3C4267)).text("Before")
    }()
    
    private lazy var beforeAnimation: UIImageView = {
        UIImageView(frame: CGRect(x: 88, y: 22, width: self.whiteContainer.frame.width-88-73, height: 34)).backgroundColor(.white)
    }()
    
    private lazy var beforePlay: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.whiteContainer.frame.width-44, y: 27, width: 24, height: 24)).tag(11).addTargetFor(self, action: #selector(playAnimation(sender:)), for: .touchUpInside)
    }()
    
    private lazy var beforeSeparaLine: UIView = {
        UIView(frame: CGRect(x: 20, y: self.beforeAnimation.frame.maxY+8, width: self.whiteContainer.frame.width-40, height: 1)).backgroundColor(UIColor(0xF8F5FA))
    }()
    
    private lazy var after: UILabel = {
        UILabel(frame: CGRect(x: 20, y: self.before.frame.maxY+45, width: self.whiteContainer.frame.width-40, height: 20)).font(.systemFont(ofSize: 13, weight: .semibold)).textColor(UIColor(0x3C4267)).text("After")
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
        SDWebImageCodersManager.sharedInstance().addCoder(SDWebImageAPNGCoder.shared())
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
            if self.afterPlay.isSelected == false {
                self.beforePlay.isSelected = false
                self.beforeAnimation.image = self.beforePlaceHolderImage
            }
            if self.beforePlay.isSelected == false {
                self.afterPlay.isSelected = false
                self.afterAnimation.image = self.afterPlaceHolderImage
            }
            
        }
    }
    
    private func initializeProperties() {
        var text = ""
        switch type {
        case .AIAEC:
            text = "AIAEC Audition"
            self.beforePlaceHolderImage = UIImage("AIAECbefore")!
            self.afterPlaceHolderImage = UIImage("AIAECafter")!
        case .AGC:
            text = "AGC Audition"
            self.beforePlaceHolderImage = UIImage("AGCbefore")!
            self.afterPlaceHolderImage = UIImage("AGCafter")!
        default:
            text = ""
        }
        self.beforeAnimation.image = self.beforePlaceHolderImage
        self.afterAnimation.image = self.afterPlaceHolderImage
        self.title.text = text
        self.beforePlay.setImage(UIImage("play2"), for: .normal)
        self.beforePlay.setImage(UIImage("zanting"), for: .selected)
        self.afterPlay.setImage(UIImage("play2"), for: .normal)
        self.afterPlay.setImage(UIImage("zanting"), for: .selected)
    }
    
    @objc private func playAnimation(sender: UIButton) {
        var resourceName = "2xAIAEC_"
        var wavName = self.title.text?.components(separatedBy: " ").first ?? ""
        var type = "m4a"
        if self.type == .AGC {
            resourceName = "2xAGC_"
            type = "wav"
        }
        if sender.tag == 11 {
            self.beforePlay.isSelected = !self.beforePlay.isSelected
            resourceName += "Before"
            wavName += "-Before"
        } else {
            self.afterPlay.isSelected = !self.afterPlay.isSelected
            resourceName += "After"
            wavName += "-After"
        }
        guard let path = Bundle.voiceRoomBundle.path(forResource: resourceName, ofType: "png") else { return }
        guard let wavPath = Bundle.voiceRoomBundle.path(forResource: wavName, ofType: type) else { return }
        VoiceRoomRTCManager.getSharedInstance().rtcKit.stopAudioMixing()
        if sender.tag == 11 {
            if self.beforePlay.isSelected == false {
                self.beforeAnimation.image = self.beforePlaceHolderImage
            } else {
                self.afterAnimation.image = self.afterPlaceHolderImage
                self.afterPlay.isSelected = false
                self.beforeAnimation.sd_setImage(with: URL(fileURLWithPath: path), placeholderImage: self.beforePlaceHolderImage)
                VoiceRoomRTCManager.getSharedInstance().rtcKit.startAudioMixing(wavPath, loopback: false, cycle: 1)
            }
        } else {
            if self.afterPlay.isSelected == false {
                self.afterAnimation.image = self.afterPlaceHolderImage
            } else {
                self.beforeAnimation.image = self.afterPlaceHolderImage
                self.beforePlay.isSelected = false
                self.afterAnimation.sd_setImage(with: URL(fileURLWithPath: path), placeholderImage: self.afterPlaceHolderImage)
                VoiceRoomRTCManager.getSharedInstance().rtcKit.startAudioMixing(wavPath, loopback: false, cycle: 1)
            }
        }
    }
    
}
