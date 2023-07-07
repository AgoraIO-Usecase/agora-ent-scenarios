//
//  SoundCardSettingViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/3.
//

import UIKit

@objc protocol SoundCardDelegate: NSObjectProtocol {
    func didUpdateEffectValue(_ value: Int)
    func didUpdateSoundSetting(_ isEnabled: Bool)
    func didUpdateTypeValue(_ value: Int)
    func didUpdateGainValue(_ value: Double)
}

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
    @objc var soundOpen: Bool = false
    @objc var gainValue: Double = 1.0
    @objc var typeValue: Int = 2
    var dropdownMenu: DropdownMenu?
    @objc var effectType: Int = 0
    @objc weak var delegate: SoundCardDelegate?
    
    @IBOutlet weak var dropBtn: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()
        iconView.layer.cornerRadius = 30
        iconView.layer.masksToBounds = true
        
//        let flag = HeadSetUtil.hasSoundCard()
//        warningView.isHidden = flag
//        HeadSetUtil.addSoundCardObserver {[weak self] flag in
//            self?.warningView.isHidden = flag
//            guard let soundBlock = self?.soundBlock else {
//                return
//            }
//            soundBlock(flag)
//        }
        
        volGainSlider.addTarget(self, action: #selector(gain), for: .valueChanged)
        volGainSlider.addTarget(self, action: #selector(gainSend), for: .touchUpInside)
        micTypeSlider.addTarget(self, action: #selector(micTypeChange), for: .valueChanged)
        micTypeSlider.addTarget(self, action: #selector(typeSend), for: .touchUpInside)
        soundSwitch.addTarget(self, action: #selector(change), for: .valueChanged)
 
        soundSwitch.isOn = soundOpen
        volGainSlider.value = Float(1/3.0 * gainValue)
        gainLabel.text = String(format: "%.1ff",gainValue)
        
        micTypeSlider.value = Float(0.25 * Double(typeValue))
        micTypeLabel.text = "\(typeValue)"
        
        switch effectType {
            case 0:
                iconView.image = UIImage.sceneImage(name: "shuaige")
                typeLabel.text = "成熟大叔"
                descLabel.text = "低沉 | 高混响"
            case 1:
                iconView.image = UIImage.sceneImage(name: "meinv")
                typeLabel.text = "女播音员"
                descLabel.text = "厚重 | 高混响"
            default:
                break
        }
        
        volGainSlider.isUserInteractionEnabled = soundOpen
        micTypeSlider.isUserInteractionEnabled = soundOpen
        dropBtn.isUserInteractionEnabled = soundOpen
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
    }
    
    deinit {
        print("sound card deinit")
    }
    
    @IBAction func showDropMenu(_ sender: UIButton) {
        sender.layoutIfNeeded()
        let dropdownFrame = CGRect(x: sender.frame.minX - 30, y: sender.frame.maxY + 10, width: 110, height: 90)
        dropdownMenu = DropdownMenu(frame: dropdownFrame, items: ["成熟大叔", "女播音员"], selectIndex: self.effectType)
        dropdownMenu?.delegate = self
        self.view.addSubview(dropdownMenu!)
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        dropdownMenu?.removeFromSuperview()
    }
    
    @objc func change( swich: UISwitch) {
        print("switch \(swich.isOn)")
        if swich.isOn {
            iconView.image = UIImage.sceneImage(name: "shuaige")
            typeLabel.text = "成熟大叔"
            descLabel.text = "低沉 | 高混响"
            gainLabel.text = "1.0"
            volGainSlider.value = 1/3.0
            micTypeLabel.text = "4"
            micTypeSlider.value = 1
        }
        volGainSlider.isUserInteractionEnabled = swich.isOn
        micTypeSlider.isUserInteractionEnabled = swich.isOn
        dropBtn.isUserInteractionEnabled = swich.isOn
        guard let delegate = delegate else {return}
        delegate.didUpdateSoundSetting(swich.isOn)
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
        guard let delegate = delegate else {return}
        delegate.didUpdateGainValue(round(Double(calculateLevel(for: gain)) * 0.1, decimalPlaces: 1))
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
        micTypeLabel.text = "\(type)"
        print("send type:\(type)")
        guard let delegate = delegate else {return}
        delegate.didUpdateTypeValue(type)
    }
    
    func calculateLevel(for value: Float) -> Int {
        let stepSize: Float = 1/30

        if value <= 0 {
            return 0
        } else if value >= 1 {
            return 30
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
        guard let delegate = delegate else {return}
        delegate.didUpdateEffectValue(index)
        self.setEffectDescWith(index: index)
    }
    
    func setEffectDescWith(index: Int) {
        gainLabel.text = "1.0"
        volGainSlider.value = 1/3.0
        micTypeLabel.text = "4"
        micTypeSlider.value = 1
        switch index {
        case 0:
            iconView.image = UIImage.sceneImage(name: "shuaige")
            typeLabel.text = "成熟大叔"
            descLabel.text = "低沉 | 高混响"
        case 1:
            iconView.image = UIImage.sceneImage(name: "meinv")
            typeLabel.text = "女播音员"
            descLabel.text = "厚重 | 高混响"
        default:
            break
        }
    }
}

