//
//  VRSoundEffectViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import Foundation

class VRSoundEffectViewController: UIViewController {
    var effectType: Int = 0
    var effectView: VRSoundCardEffectView!
    @objc var clicKBlock:((Int) -> Void)?
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer
        
        effectView = VRSoundCardEffectView(frame: self.view.bounds)
        effectView.effectType = effectType
        effectView.clickBlock = {[weak self] index in
            guard let self = self, let block = self.clicKBlock else {return}
            self.effectType = index
            block(index)
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
