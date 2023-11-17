//
//  VRSoundCardViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import UIKit

class VRSoundCardViewController: UIViewController {
    
    //虚拟声卡的属性
    public var soundOpen: Bool = false
    public var gainValue: String = ""
    public var typeValue: Int = 0
    public var effectType: Int = 0
    
    @objc var clicKBlock:((Int) -> Void)?
    @objc var gainBlock:((Float) -> Void)?
    @objc var typeBlock:((Int) -> Void)?
    @objc var soundCardBlock:((Bool) -> Void)?
    
    var settingView: VRSoundCardSettingView!
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer

        settingView = VRSoundCardSettingView(frame: self.view.bounds)
        settingView.effectType = self.effectType
        settingView.typeValue = self.typeValue
        settingView.soundOpen = self.soundOpen
        if let gain = Float(self.gainValue) {
            settingView.gainValue = gain
        } else {
            settingView.gainValue = 0
        }
        settingView.clicKBlock = {[weak self] index in
            //弹出音效
            let effectVC = VRSoundEffectViewController()
            effectVC.effectType = self?.effectType ?? 0
            effectVC.clicKBlock = {[weak self] index in
                guard let clicKBlock = self?.clicKBlock else {return}
                self?.settingView.effectType = index
                self?.effectType = index
                self?.gainValue = "1.0"
                self?.settingView.tableView.reloadData()
                clicKBlock(index)
            }
            VoiceRoomPresentView.shared.push(with: effectVC, frame: CGRect(x: 0, y: UIScreen.main.bounds.size.height - 400, width: UIScreen.main.bounds.size.width, height: 400), maxHeight: 600)
        }
        
        settingView.gainBlock = {[weak self] gain in
            guard let gainBlock = self?.gainBlock else {return}
            self?.gainValue = "\(gain)"
            gainBlock(gain)
        }
        
        settingView.typeBlock = {[weak self] type in
            guard let typeBlock = self?.typeBlock else {return}
            self?.typeValue = type
            typeBlock(type)
        }
        
        settingView.soundCardBlock = {[weak self] flag in
            guard let soundCardBlock = self?.soundCardBlock else {return}
            self?.soundOpen = flag
            if flag == true {
                self?.gainValue = "1.0"
                self?.effectType = 0
                self?.settingView.effectType = 0
                self?.settingView.tableView.reloadData()
            }
            soundCardBlock(flag)
        }
        
        self.view.addSubview(settingView)
        
        let backBtn = UIButton(frame: CGRect(x: 20, y: 26, width: 30, height: 30))
        backBtn.setBackgroundImage(UIImage.sceneImage(name: "back", bundleName: "VoiceChatRoomResource"), for: .normal)
        backBtn.addTarget(self, action: #selector(back), for: .touchUpInside)
        self.view.addSubview(backBtn)
    }
    
    @objc private func back() {
        VoiceRoomPresentView.shared.pop()
    }
    
}
