//
//  VLBottomView.swift
//  Cantata
//
//  Created by CP on 2023/8/31.
//

import Foundation
import UIKit

@objc public protocol VLBottomViewDelegate: NSObjectProtocol {
    func didBottomViewAudioStateChangeTo(enable: Bool)
    func didBottomChooseSong()
}

class VLBottomView: UIView {
    
    var audioBtn: UIButton!
    var musicChooseBtn: UIButton!
    weak var delegate: VLBottomViewDelegate?
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        audioBtn = UIButton()
        audioBtn.setImage(UIImage.sceneImage(name: "ktv_audio_icon", bundleName: "DHCResource"), for: .normal)
        audioBtn.setImage(UIImage.sceneImage(name: "ktv_self_muteIcon", bundleName: "DHCResource"), for: .selected)
        audioBtn.addTarget(self, action: #selector(audioChange), for: .touchUpInside)
        
        musicChooseBtn = UIButton()
        musicChooseBtn.setImage(UIImage.sceneImage(name: "ktv_diange_icon", bundleName: "DHCResource"), for: .normal)
        musicChooseBtn.addTarget(self, action: #selector(chooseMusic), for: .touchUpInside)
        self.addSubview(audioBtn)
        self.addSubview(musicChooseBtn)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        audioBtn.frame = CGRect(x: 25, y: 13, width: 24, height: 24)
        musicChooseBtn.frame = CGRect(x: ScreenWidth - 80, y: 9, width: 70, height: 32)
    }
    
    @objc private func audioChange(btn: UIButton) {
        guard let delegate = self.delegate else {return}
        btn.isSelected = !btn.isSelected
        delegate.didBottomViewAudioStateChangeTo(enable: btn.isSelected)
    }
    
    @objc private func chooseMusic(btn: UIButton) {
        guard let delegate = self.delegate else {return}
        delegate.didBottomChooseSong()
    }
}
