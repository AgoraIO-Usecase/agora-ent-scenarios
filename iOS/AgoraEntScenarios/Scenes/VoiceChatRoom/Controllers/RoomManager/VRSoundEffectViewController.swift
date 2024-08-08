//
//  VRSoundEffectViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import Foundation

class VRSoundEffectViewController: UIViewController {
    
    public var soundcardPresenter: VirtualSoundcardPresenter? = nil
    
    deinit {
        soundcardPresenter?.removeDelegate(self)
    }
    
    var effectView: VRSoundCardEffectView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer
        
        effectView = VRSoundCardEffectView(frame: self.view.bounds)
        effectView.effectType =  self.soundcardPresenter?.presetSoundType ?? 0
        effectView.clickBlock = {[weak self] index in
            self?.soundcardPresenter?.setPresetSoundEffectType(index)
        }
        self.view.addSubview(effectView)
        
        let backBtn = UIButton(frame: CGRect(x: 20, y: 26, width: 30, height: 30))
        backBtn.setBackgroundImage(UIImage.sceneImage(name: "back", bundleName: "VoiceChatRoomResource"), for: .normal)
        backBtn.addTarget(self, action: #selector(back), for: .touchUpInside)
        self.view.addSubview(backBtn)
    }
    
    @objc private func back() {
        VoiceRoomPresentView.shared.pop()
    }
}

extension VRSoundEffectViewController: VirtualSoundcardPresenterDelegate {
    func onValueChanged(isEnabled: Bool, gainValue: Int, typeValue: Int, effectType: Int) {
        effectView.effectType = effectType
    }
}
