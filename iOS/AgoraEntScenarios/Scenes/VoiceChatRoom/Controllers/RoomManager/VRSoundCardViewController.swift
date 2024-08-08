//
//  VRSoundCardViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import UIKit

class VRSoundCardViewController: UIViewController {
    
    var settingView: VRSoundCardSettingView!
    
    var soundcardPresenter: VirtualSoundcardPresenter? = nil
    
    deinit {
        soundcardPresenter?.removeDelegate(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer
        
        soundcardPresenter?.addDelegate(self)
        settingView = VRSoundCardSettingView(frame: self.view.bounds)
        if let presenter = soundcardPresenter {
            settingView.effectType = presenter.presetSoundType
            settingView.typeValue = presenter.typeValue
            settingView.soundOpen = presenter.isEnabled
            settingView.gainValue = Float(presenter.gainValue)
        }
        
        settingView.clicKBlock = {[weak self] index in
            //弹出音效
            let effectVC = VRSoundEffectViewController()
            effectVC.soundcardPresenter = self?.soundcardPresenter
            VoiceRoomPresentView.shared.push(with: effectVC, frame: CGRect(x: 0, y: UIScreen.main.bounds.size.height - 400, width: UIScreen.main.bounds.size.width, height: 400), maxHeight: 600)
        }
        
        settingView.gainBlock = {[weak self] gain in
            self?.soundcardPresenter?.setGainValue(Int(gain))
        }
        
        settingView.typeBlock = {[weak self] type in
            self?.soundcardPresenter?.setTypeValue(type)
        }
        
        settingView.soundCardBlock = {[weak self] flag in
            self?.soundcardPresenter?.setSoundCardEnable(flag)
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

extension VRSoundCardViewController: VirtualSoundcardPresenterDelegate {
    func onValueChanged(isEnabled: Bool, gainValue: Int, typeValue: Int, effectType: Int) {
        settingView.soundOpen = isEnabled
        settingView.effectType = effectType
        settingView.typeValue = typeValue
        settingView.gainValue = Float(gainValue)
        settingView.tableView.reloadData()
    }
}
