//
//  BaseNavigationView.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
@_exported import ZSwiftBaseLib

public class BaseNavigationView: UIView {
    
    lazy var back: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 20, y:  ZStatusBarHeight + 7, width: 30, height: 30))
    }()
    
    lazy var title: UILabel = {
        UILabel(frame: CGRect(x: self.back.frame.maxX + 10, y: ZStatusBarHeight+10, width: ScreenWidth - 120, height: 20)).font(UIFont.systemFont(ofSize: 16, weight: .semibold)).backgroundColor(.clear).textAlignment(.center)
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.back,self.title])
        self.title.center = CGPoint(x: self.center.x, y: self.title.center.y)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}


