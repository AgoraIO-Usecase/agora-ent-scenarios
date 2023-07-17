//
//  VREQOperationView.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/1/19.
//

import UIKit
import ZSwiftBaseLib

enum VREQOperation: Int {
    case close = 1
    case other = 2
}

public class VREQOperationAlert: UIView {
    
    var actionClosure: ((VREQOperation) -> ())?
    
    private lazy var header: VoiceRoomAlertContainer = {
        VoiceRoomAlertContainer(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: 60))
    }()
    private lazy var agoraBlue: UIImageView = {
        UIImageView(frame: CGRect(x: self.frame.width/2.0-95, y: 36, width: 64, height: 64)).layerProperties(UIColor(0xDAD9E9), 1).cornerRadius(32).image(UIImage.sceneImage(name: "blue", bundleName: "VoiceChatRoomResource")!)
    }()
    
    private lazy var neckSymbol: UIImageView = {
        UIImageView(frame: CGRect(x: self.frame.width/2.0-9, y: self.agoraBlue.frame.maxY+15, width: 18, height: 18)).layerProperties(UIColor(0xDAD9E9), 1).cornerRadius(32).image(UIImage.sceneImage(name: "icons／solid／link", bundleName: "VoiceChatRoomResource")!)
    }()
    
    private lazy var agoraRed: UIImageView = {
        UIImageView(frame: CGRect(x: self.frame.width/2.0+31, y: 36, width: 64, height: 64)).layerProperties(UIColor(0xDAD9E9), 1).cornerRadius(32).image(UIImage.sceneImage(name: "red", bundleName: "VoiceChatRoomResource")!)
    }()
    
    private lazy var blueName: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.frame.width/2.0 - 115, y: self.agoraBlue.frame.maxY+10, width: 85, height: 20)).font(.systemFont(ofSize: 14, weight: .semibold)).title("Agora Blue".voice_localized(), .normal).isUserInteractionEnabled(false).textColor(.black, .normal)
    }()
    
    private lazy var redName: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.frame.width/2.0 + 18, y: self.agoraRed.frame.maxY+10, width: 85, height: 20)).font(.systemFont(ofSize: 14, weight: .semibold)).title("Agora Red".voice_localized(), .normal).isUserInteractionEnabled(false).textColor(.black, .normal)
    }()
    
    private lazy var separateLine: UIView = {
        UIView(frame: CGRect(x: 0, y: self.frame.height - 59 - CGFloat(ZBottombarHeight), width: self.frame.width, height: 1)).backgroundColor(UIColor(red: 0.592, green: 0.592, blue: 0.592, alpha: 0.12))
    }()
    
    private lazy var closeBot: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: self.separateLine.frame.maxY+20, width: self.frame.width/2.0, height: 20)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x156EF3), .normal).title("Close Bot".voice_localized(), .normal).tag(11).addTargetFor(self, action: #selector(action(sender:)), for: .touchUpInside)
    }()
    
    private lazy var otherSetting: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.frame.width/2.0, y: self.separateLine.frame.maxY+20, width: self.frame.width/2.0, height: 20)).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x156EF3), .normal).title("Other Settings".voice_localized(), .normal).tag(12).addTargetFor(self, action: #selector(action(sender:)), for: .touchUpInside)
    }()
    
    private lazy var verticalLine: UIView = {
        UIView(frame: CGRect(x: self.frame.width/2.0-1, y: self.separateLine.frame.maxY+21, width: 2, height: 12)).backgroundColor(UIColor(0x979797))
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.header,self.agoraBlue,self.neckSymbol,self.agoraRed,self.blueName,self.redName,self.separateLine,self.closeBot,self.otherSetting,self.verticalLine])
        self.blueName.set(image: UIImage.sceneImage(name: "guanfang", bundleName: "VoiceChatRoomResource"), title: "Agora Blue".voice_localized(), titlePosition: .right, additionalSpacing: 5, state: .normal)
        self.redName.set(image: UIImage.sceneImage(name: "guanfang", bundleName: "VoiceChatRoomResource"), title: "Agora Red".voice_localized(), titlePosition: .right, additionalSpacing: 5, state: .normal)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func action(sender: UIButton) {
        if self.actionClosure != nil {
            self.actionClosure!(VREQOperation(rawValue: sender.tag-10) ?? .close)
        }
    }
    
}
