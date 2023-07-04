//
//  SoundCardSettingViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/3.
//

import UIKit

class SoundCardSettingViewController: UIViewController {

    @IBOutlet weak var deviceLabel: UILabel!
    @IBOutlet weak var gainLabel: UILabel!
    @IBOutlet weak var volGainSlider: UISlider!
    @IBOutlet weak var warningView: UIView!
    @IBOutlet weak var soundSetView: UIView!
    @IBOutlet weak var soundSwitch: UISwitch!
    @IBOutlet weak var iconView: UIImageView!
    @IBOutlet weak var descLabel: UILabel!
    @IBOutlet weak var typeLabel: UILabel!
    @IBOutlet weak var micTypeLabel: UILabel!
    @IBOutlet weak var micTypeSlider: UISlider!
    var soundOpen: Bool = false
    var gainValue: Double = 1.0
    var typeValue: Int = 2
    var dropdownMenu: DropdownMenu?
    var effectType: Int = 0
    var effectBlock: ((Int)-> Void)?
    var soundBlock: ((Bool)-> Void)?
    var typeBlock: ((Int)-> Void)?
    var gainBlock: ((Double)-> Void)?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        iconView.layer.cornerRadius = 30
        iconView.layer.masksToBounds = true
        
        let flag = HeadSetUtil.hasSoundCard()
        warningView.isHidden = flag
        HeadSetUtil.addSoundCardObserver {[weak self] flag in
            self?.warningView.isHidden = flag
        }
        volGainSlider.addTarget(self, action: #selector(gain), for: .valueChanged)
        volGainSlider.addTarget(self, action: #selector(gainSend), for: .touchUpInside)
        micTypeSlider.addTarget(self, action: #selector(micTypeChange), for: .valueChanged)
        micTypeSlider.addTarget(self, action: #selector(typeSend), for: .touchUpInside)
        soundSwitch.addTarget(self, action: #selector(change), for: .valueChanged)
        
        soundSwitch.isOn = soundOpen
        volGainSlider.value = Float(0.5 * gainValue)
        gainLabel.text = String(format: "%.1ff",gainValue)
        
        micTypeSlider.value = Float(0.25 * Double(typeValue))
        typeLabel.text = "\(typeValue)"
        
        setEffectDescWith(index: effectType)
    }

    @IBAction func showDropMenu(_ sender: UIButton) {
        sender.layoutIfNeeded()
        let dropdownFrame = CGRect(x: sender.frame.minX - 30, y: sender.frame.maxY + 10, width: 110, height: 180)
        dropdownMenu = DropdownMenu(frame: dropdownFrame, items: ["性感欧巴", "温柔御姐", "阳光正太", "甜美嗲气"], selectIndex: self.effectType)
        dropdownMenu?.delegate = self
        self.view.addSubview(dropdownMenu!)
    }
    
    @IBAction func back(_ sender: UIButton) {
        self.navigationController?.popViewController(animated: false)
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        dropdownMenu?.removeFromSuperview()
    }
    
    @objc func change( swich: UISwitch) {
        print("switch \(swich.isOn)")
        guard let changeBlock = soundBlock else {return}
        changeBlock(swich.isOn)
    }
    
    @objc func gain() {
        let gain = volGainSlider.value
        gainLabel.text = String(format: "%.1ff", Double(calculateLevel(for: gain)) * 0.1)
    }
    
    @objc func micTypeChange() {
        let typeValue = micTypeSlider.value
        micTypeLabel.text = "\(calculateType(for: typeValue))"
    }
    
    @objc func gainSend() {
        let gain = volGainSlider.value
        let level = String(format: "%.1ff", Double(calculateLevel(for: gain)) * 0.1)
        let levNum = Double(level)
        print("send lev:\(round(Double(calculateLevel(for: gain)) * 0.1, decimalPlaces: 1))")
        guard let gainBlock = gainBlock else {return}
        gainBlock(round(Double(calculateLevel(for: gain)) * 0.1, decimalPlaces: 1))
    }
    
    func round(_ value: Double, decimalPlaces: Int) -> Double {
        let multiplier = pow(10, Double(decimalPlaces))
        var newValue = value
        newValue = Darwin.round(value * multiplier) / multiplier
        return newValue
    }
    
    @objc func typeSend() {
        let typeValue = micTypeSlider.value
        let type = calculateType(for: typeValue)
        print("send type:\(type)")
        guard let typeBlock = typeBlock else {return}
        typeBlock(type)
    }
    
    func calculateLevel(for value: Float) -> Int {
        let stepSize: Float = 0.05

        if value <= 0 {
            return 0
        } else if value >= 1 {
            return 20
        } else {
            let level = Int(value / stepSize)
            return level
        }
    }
    
    func calculateType(for value: Float) -> Int {
        let stepSize: Float = 0.25

        if value <= 0 {
            return 0
        } else if value >= 1 {
            return 4
        } else {
            let level = Int(value / stepSize)
            return level
        }
    }
}

extension SoundCardSettingViewController: DropdownMenuDelegate {
    func didSelectItemAtIndex(index: Int) {
        print("index: \(index)")
        self.effectType = index
        guard let block = effectBlock else {return}
        block(index)
        self.setEffectDescWith(index: index)
    }
    
    func setEffectDescWith(index: Int) {
        gainLabel.text = "1.0"
        volGainSlider.value = 0.5
        micTypeLabel.text = "4"
        micTypeSlider.value = 1
        switch index {
        case 0:
            iconView.image = UIImage(named: "shuaige")
            typeLabel.text = "性感欧巴"
            descLabel.text = "悦耳 | 磁性"
        case 1:
            iconView.image = UIImage(named: "meinv")
            typeLabel.text = "温柔御姐"
            descLabel.text = "柔美 | 磁性"
        case 2:
            iconView.image = UIImage(named: "zhengtai")
            typeLabel.text = "阳光正太"
            descLabel.text = "洪亮 | 饱满"
        case 3:
            iconView.image = UIImage(named: "girl")
            typeLabel.text = "甜美嗲气"
            descLabel.text = "夹子音 | 萝莉"
        default:
            break
        }
    }
}

