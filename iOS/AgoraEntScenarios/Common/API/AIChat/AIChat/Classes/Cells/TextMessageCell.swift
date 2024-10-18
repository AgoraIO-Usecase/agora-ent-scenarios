//
//  TextMessageCell.swift
//  Pods
//
//  Created by 朱继超 on 2024/9/5.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage

class TextMessageCell: MessageCell {
    
    lazy var typingIndicator: UIImageView = {
        UIImageView(frame: .zero).backgroundColor(.clear).contentMode(.scaleAspectFit)
    }()
    
    lazy var content: UILabel = {
        UILabel(frame: .zero).backgroundColor(.clear).numberOfLines(0)
    }()
    
    lazy var separatorLine: UIView = {
        UIView(frame: .zero).backgroundColor(UIColor(0xb5adcc))
    }()
    
    lazy var playButton: UIButton = {
        UIButton(type: .custom).title(" 开始识别", .normal).title(" 正在播放", .selected).font(.systemFont(ofSize: 11, weight: .regular)).textColor(UIColor(0x3c4267), .normal).addTargetFor(self, action: #selector(playAudio), for: .touchUpInside)
    }()
    
    private var contentBottomConstraint: NSLayoutConstraint!
    
    public required init(towards: BubbleTowards, reuseIdentifier: String, chatType: AIChatType) {
        super.init(towards: towards, reuseIdentifier: reuseIdentifier, chatType: chatType)
        self.bubbleMultiCorners.addSubview(self.typingIndicator)
        self.bubbleMultiCorners.addSubview(self.content)
        self.bubbleMultiCorners.addSubview(self.separatorLine)
        self.bubbleMultiCorners.addSubview(self.playButton)
        self.typingIndicator.isHidden = true
        self.content.isHidden = true
        self.typingIndicator.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.typingIndicator.leadingAnchor.constraint(equalTo: self.bubbleMultiCorners.leadingAnchor, constant: 4),
            self.typingIndicator.topAnchor.constraint(equalTo: self.bubbleMultiCorners.topAnchor, constant: 2),
            self.typingIndicator.bottomAnchor.constraint(equalTo: self.bubbleMultiCorners.bottomAnchor, constant: -2),
            self.typingIndicator.trailingAnchor.constraint(equalTo: self.bubbleMultiCorners.trailingAnchor, constant: -4)
        ])
        self.content.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.content.leadingAnchor.constraint(equalTo: self.bubbleMultiCorners.leadingAnchor, constant: 14),
            self.content.trailingAnchor.constraint(equalTo: self.bubbleMultiCorners.trailingAnchor, constant: -14),
            self.content.topAnchor.constraint(equalTo: self.bubbleMultiCorners.topAnchor, constant: 10)
        ])
        self.contentBottomConstraint = self.content.bottomAnchor.constraint(equalTo: self.bubbleMultiCorners.bottomAnchor, constant: -10)
        self.contentBottomConstraint.isActive = true
        
        self.separatorLine.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.separatorLine.heightAnchor.constraint(equalToConstant: 0.5),
            self.separatorLine.leadingAnchor.constraint(equalTo: self.bubbleMultiCorners.leadingAnchor,constant: 14),
            self.separatorLine.trailingAnchor.constraint(equalTo: self.bubbleMultiCorners.trailingAnchor,constant: -14),
            self.separatorLine.topAnchor.constraint(equalTo: self.content.bottomAnchor,constant: 8)
        ])
        self.separatorLine.isHidden = true
        
        self.playButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.playButton.heightAnchor.constraint(equalToConstant: 16),
            self.playButton.widthAnchor.constraint(equalToConstant: 84),
            self.playButton.topAnchor.constraint(equalTo: self.separatorLine.bottomAnchor,constant: 8),
            self.playButton.leadingAnchor.constraint(equalTo: self.bubbleMultiCorners.leadingAnchor,constant: 14)
        ])
        self.playButton.isHidden = true
        self.playButton.setImage(UIImage(named: "play2", in: .chatAIBundle, with: nil), for: .normal)
        self.playButton.setImage(UIImage(named: "playing", in: .chatAIBundle, with: nil), for: .selected)
        self.playButton.contentHorizontalAlignment = .left
        SDImageCodersManager.shared.addCoder(SDImageAPNGCoder.shared)
    }
    
    @objc func playAudio() {
        self.playButton.imageView?.stopAnimating()
        self.playButton.imageView?.layer.removeAllAnimations()
        self.clickAction?(.bubble,self.entity)
        
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func addRotation() {
        let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
        rotationAnimation.toValue = NSNumber(value: Double.pi * 2)
        rotationAnimation.duration = 1
        rotationAnimation.repeatCount = 999
        rotationAnimation.isRemovedOnCompletion = false
        rotationAnimation.fillMode = CAMediaTimingFillMode.forwards
        
        self.playButton.imageView?.layer.add(rotationAnimation, forKey: "rotationAnimation")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    override func refresh(entity: MessageEntity) {
        super.refresh(entity: entity)
        self.separatorLine.isHidden = true
        self.playButton.isHidden = true
        if entity.message.direction == .receive {
            if entity.editState == .typing {
                self.typingIndicator.isHidden = false
                self.content.isHidden = true
                if let url = Bundle.chatAIBundle.url(forResource: "agents_msg_rply", withExtension: "apng") {
                    self.typingIndicator.sd_setImage(with: url)
                }
            } else {
                self.typingIndicator.isHidden = true
                self.typingIndicator.image = nil
                self.content.isHidden = false
            }
            if entity.editState == .end {
                self.contentBottomConstraint.constant = -42
                self.separatorLine.isHidden = false
                self.playButton.isHidden = false
                if !entity.message.existTTSFile {
                    if entity.downloading {
                        self.playButton.setTitle(" 正在识别", for: .normal)
                        self.playButton.setImage(UIImage(named: "voice_spinner", in: .chatAIBundle, with: nil), for: .normal)
                        self.addRotation()
                        self.entity.playing = false
                    } else {
                        self.playButton.imageView?.stopAnimating()
                        self.playButton.imageView?.layer.removeAllAnimations()
                        self.playButton.setImage(UIImage(named: "play2", in: .chatAIBundle, with: nil), for: .normal)
                        self.playButton.imageView?.layer.removeAllAnimations()
                        self.playButton.setTitle(" 转语音", for: .normal)
                        self.entity.playing = false
                    }
                } else {
                    if entity.playing {
                        self.playButton.imageView?.stopAnimating()
                        self.playButton.imageView?.layer.removeAllAnimations()
                        self.playButton.setTitle(" 正在播放", for: .normal)
                        self.playButton.setImage(UIImage(named: "playing", in: .chatAIBundle, with: nil), for: .normal)
                        self.entity.playing = true
                        
                    } else {
                        self.playButton.imageView?.stopAnimating()
                        self.playButton.imageView?.layer.removeAllAnimations()
                        self.playButton.setTitle(" 转语音", for: .normal)
                        self.playButton.setImage(UIImage(named: "play2", in: .chatAIBundle, with: nil), for: .normal)
                        self.entity.playing = false
                    }
                }
            } else {
                self.contentBottomConstraint.constant = -10
            }
        } else {
            self.typingIndicator.isHidden = true
            self.typingIndicator.stopAnimating()
            self.content.isHidden = false
            self.contentBottomConstraint.constant = -10
        }
        self.content.attributedText = entity.content
    }
}
