//
//  AIChatAudioRecorderView.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/4.
//

import UIKit
import AgoraCommon
import AgoraRtcKit
import ZSwiftBaseLib
import SDWebImage

class AIChatAudioRecorderView: UIImageView {
    
    public private(set) lazy var recorderView: UIImageView = {
        UIImageView(frame: .zero).contentMode(.scaleAspectFill).cornerRadius(16)
    }()
    
    public private(set) lazy var volumeIndicator: UIImageView = {
        UIImageView(frame: .zero).contentMode(.scaleAspectFill)
    }()
    
    public private(set) lazy var operationIndicator: UILabel = {
        UILabel(frame: .zero).textColor(.white).font(.systemFont(ofSize: 12, weight: .regular)).textAlignment(.center).backgroundColor(.clear)
    }()
    
    private var playAPNG = false

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.image = UIImage(named: "audio_recoder_bg", in: .chatAIBundle, compatibleWith: nil)
        self.contentMode = .scaleAspectFit
        self.addSubview(self.recorderView)
        self.addSubview(self.operationIndicator)
        self.recorderView.addSubview(self.volumeIndicator)
        self.setConstraints()
        let APNGCoder = SDImageAPNGCoder.shared
        SDImageCodersManager.shared.addCoder(APNGCoder)
    }
    
    func setConstraints() {
        self.recorderView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            self.recorderView.heightAnchor.constraint(equalToConstant: 58),
            self.recorderView.leadingAnchor.constraint(equalTo: self.leadingAnchor,constant: 20),
            self.recorderView.trailingAnchor.constraint(equalTo: self.trailingAnchor,constant: -20),
            self.recorderView.bottomAnchor.constraint(equalTo: self.safeAreaLayoutGuide.bottomAnchor)
        ])
        
        self.volumeIndicator.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.volumeIndicator.heightAnchor.constraint(equalToConstant: 24),
            self.volumeIndicator.widthAnchor.constraint(equalToConstant: 242),
            self.volumeIndicator.centerXAnchor.constraint(equalTo: self.recorderView.centerXAnchor),
            self.volumeIndicator.centerYAnchor.constraint(equalTo: self.recorderView.centerYAnchor)
        ])
        
        self.operationIndicator.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.operationIndicator.heightAnchor.constraint(equalToConstant: 16),
            self.operationIndicator.leadingAnchor.constraint(equalTo: self.recorderView.leadingAnchor),
            self.operationIndicator.trailingAnchor.constraint(equalTo: self.recorderView.trailingAnchor),
            self.operationIndicator.bottomAnchor.constraint(equalTo: self.recorderView.topAnchor,constant: -10)
        ])
        self.volumeIndicator.sd_setImage(with: nil, placeholderImage: UIImage(named: "No sound", in: .chatAIBundle, compatibleWith: nil), options: .retryFailed, context: nil)
        self.refreshBackground(with: .start)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updateIndicatorImage(volume: Int) {
//        aichatPrint("updateIndicatorImage: \(volume)")
        switch volume {
            case 0...10:
            self.volumeIndicator.sd_setImage(with: nil, placeholderImage: UIImage(named: "No sound", in: .chatAIBundle, compatibleWith: nil), options: .retryFailed, context: nil)
            default:
            if let url = Bundle.chatAIBundle.url(forResource: "with_sound", withExtension: "apng"),!self.playAPNG {
                self.volumeIndicator.sd_setImage(with: url)
                self.playAPNG = true
            }
        }
    }
    
    func refreshBackground(with state: LongPressButton.State) {
        
        switch state {
        case .start:
            self.operationIndicator.text = "松手发送，左滑取消"
            self.recorderView.image = UIImage(named: "recording_bg", in: .chatAIBundle, compatibleWith: nil)
        case .cancel:
            self.operationIndicator.text = "松手取消"
            self.recorderView.image = UIImage(named: "recording_bg_waring", in: .chatAIBundle, compatibleWith: nil)
        case .end:
            self.removeFromSuperview()
        }
    }
    
}

