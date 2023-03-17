//
//  AboutAgoraHeader.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/8.
//

import UIKit
import ZSwiftBaseLib

protocol AboutAgoraHeaderDelegate: NSObjectProtocol {
    
    func enterDebugMode()
}

final class AboutAgoraHeader: UIView {
    
    var delegate: AboutAgoraHeaderDelegate?
    
    private lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: self.frame.width/2.0-37, y: 42, width: 74, height: 74)).backgroundColor(.white).image(UIImage(named: "app_icon")!)
    }()
    
    private lazy var appName: UILabel = {
        UILabel(frame: CGRect(x: 30, y: self.icon.frame.maxY+10, width: self.frame.width-60, height: 20)).font(.systemFont(ofSize: 17, weight: .medium)).textColor(UIColor(0x040925)).textAlignment(.center).backgroundColor(.white)
    }()
    
    private lazy var version: UILabel = {
        let label = UILabel(frame: CGRect(x: 20, y: self.appName.frame.maxY+5, width: ScreenWidth-40, height: 20))
            .font(.systemFont(ofSize: 13, weight: .regular))
            .textColor(UIColor(0x979CBB))
            .textAlignment(.center)
            .backgroundColor(.white)
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(onTapVersionLabel(_:)))
        tap.numberOfTapsRequired = 5;
        label.addGestureRecognizer(tap)
        label.isUserInteractionEnabled = true
        return label
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    convenience init(frame: CGRect,name: String,versionText: String) {
        self.init(frame: frame)
        self.addSubViews([self.icon,self.appName,self.version])
        self.appName.text = name
        self.version.text = versionText
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func onTapVersionLabel(_ sender: UITapGestureRecognizer) {
        delegate?.enterDebugMode()
    }
    
}
