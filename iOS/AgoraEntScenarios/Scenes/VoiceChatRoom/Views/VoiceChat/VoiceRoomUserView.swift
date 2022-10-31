//
//  VoiceRoomUserView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomUserView: UIView {
    
    private var controllers = [UIViewController]()
    
    private var titles = [String]()
    
    lazy var header: VoiceRoomAlertContainer = {
        VoiceRoomAlertContainer(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56))
    }()
    
    lazy var switchBar: VoiceRoomSwitchBar = {
        VoiceRoomSwitchBar(frame: CGRect(x: 0, y: 16, width: ScreenWidth, height: 40), titles: self.titles)
    }()
    
    lazy var container: VoiceRoomPageContainer = {
        VoiceRoomPageContainer(frame: CGRect(x: 0, y: self.header.frame.maxY, width: ScreenWidth, height: self.frame.height-self.header.frame.height), viewControllers: self.controllers).backgroundColor(.white)
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    convenience init(frame: CGRect,controllers: [UIViewController],titles:[String],position: VoiceRoomSwitchBarDirection) {
        self.init(frame: frame)
        self.controllers = controllers
        self.titles = titles
        self.addSubViews([self.header,self.switchBar,self.container])
        self.container.scrollClosure = { [weak self] in
            self?.switchBar.moveTo(direction: $0 > 0 ? .right:.left)
        }
        self.switchBar.selectAction = { [weak self] in
            self?.container.index = $0-11
        }
        if position == .right {
            self.switchBar.moveTo(direction: .right)
            self.container.index = 1
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}
