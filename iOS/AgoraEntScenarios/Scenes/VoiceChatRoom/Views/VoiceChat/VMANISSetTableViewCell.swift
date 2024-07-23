//
//  VMANISSetTableViewCell.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import SnapKit
import UIKit

public enum AINSSetCellType {
    case ns //降噪类型cell
    case music //音乐保护
    case voice //人声保护
}

class VMANISSetTableViewCell: UITableViewCell {
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    private var titleLabel: UILabel = .init()
    private var highBtn: UIButton = .init()
    private var midBtn: UIButton = .init()
    private var aiHighBtn: UIButton = .init()
    private var aiMidBtn: UIButton = .init()
    private var cusBtn: UIButton = .init()
    private var offBtn: UIButton = .init()
    private var selBtn: UIButton = UIButton()
    public var isTouchAble: Bool = false
    public var ains_state: AINS_STATE = .mid {
        didSet {
            switch ains_state {
            case .high:
                setBtnStateWith(highBtn)
            case .mid:
                setBtnStateWith(midBtn)
            case .off:
                setBtnStateWith(offBtn)
            case .aiMid:
                setBtnStateWith(aiMidBtn)
            case .aiHigh:
                setBtnStateWith(aiHighBtn)
            case .custom:
                setBtnStateWith(cusBtn)
            }
        }
    }

    public var aed_state: AED_STATE = .off {
        didSet {
            switch aed_state {
            case .high:
                setBtnStateWith(aiHighBtn)
            case .low:
                setBtnStateWith(aiMidBtn)
            case .off:
                setBtnStateWith(offBtn)
            case .custom:
                setBtnStateWith(cusBtn)
            }
        }
    }
    
    public var aspt_state: ASPT_STATE = .off {
        didSet {
            switch aspt_state {
            case .high:
                setBtnStateWith(aiHighBtn)
            case .low:
                setBtnStateWith(aiMidBtn)
            case .off:
                setBtnStateWith(offBtn)
            case .custom:
                setBtnStateWith(cusBtn)
            }
        }
    }
    
    public var cellType: AINSSetCellType = .ns {
        didSet {
            highBtn.isHidden = cellType != .ns
            midBtn.isHidden = cellType != .ns
            if cellType == .ns {
                titleLabel.text = "降噪"
            } else if cellType == .music {
                titleLabel.text = "音乐保护"
            } else if cellType == .voice {
                titleLabel.text = "人声保护"
            }
        }
    }

    var selBlock: ((AINS_STATE) -> Void)?
    var aedBlock: ((AED_STATE) -> Void)?
    var asptBlock:((ASPT_STATE) -> Void)?

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)

        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func layoutUI() {
        // titleLabel.frame = CGRect(x: 20~, y: 17~, width: 200~, height: 20~)
        titleLabel.text = "降噪"
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        titleLabel.textColor = UIColor(hex: 0x3C4267, alpha: 1)
        contentView.addSubview(titleLabel)

        offBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        offBtn.setTitle(" \("voice_ains_off".voice_localized()) ", for: .normal)
        offBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        offBtn.font(UIFont.systemFont(ofSize: 11))
        offBtn.layer.cornerRadius = 3
        offBtn.layer.masksToBounds = true
        offBtn.tag = 100
        offBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        offBtn.accessibilityIdentifier = "voice_chat_ains_setting_self_off"
        addSubview(offBtn)
      //  selBtn = offBtn
        
        aiMidBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        aiMidBtn.setTitle(" \("voice_ains_ai_low".voice_localized()) ", for: .normal)
        aiMidBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        aiMidBtn.font(UIFont.systemFont(ofSize: 11))
        aiMidBtn.layer.cornerRadius = 3
        aiMidBtn.layer.masksToBounds = true
        aiMidBtn.tag = 103
        aiMidBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        aiMidBtn.accessibilityIdentifier = "voice_chat_ains_setting_self_off"
        addSubview(aiMidBtn)

        aiHighBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        aiHighBtn.setTitle(" \("voice_ains_ai_high".voice_localized()) ", for: .normal)
        aiHighBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        aiHighBtn.font(UIFont.systemFont(ofSize: 11))
        aiHighBtn.layer.cornerRadius = 3
        aiHighBtn.layer.masksToBounds = true
        aiHighBtn.tag = 104
        aiHighBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        aiHighBtn.accessibilityIdentifier = "voice_chat_ains_setting_self_high"
        addSubview(aiHighBtn)
        
        midBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        midBtn.setTitle(" \("voice_ains_legacy_low".voice_localized()) ", for: .normal)
        midBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        midBtn.font(UIFont.systemFont(ofSize: 11))
        midBtn.layer.cornerRadius = 3
        midBtn.layer.masksToBounds = true
        midBtn.tag = 101
        midBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        midBtn.accessibilityIdentifier = "voice_chat_ains_setting_self_high"
        addSubview(midBtn)

        highBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        highBtn.setTitle(" \("voice_ains_legacy_high".voice_localized()) ", for: .normal)
        highBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        highBtn.font(UIFont.systemFont(ofSize: 11))
        highBtn.layer.cornerRadius = 3
        highBtn.layer.masksToBounds = true
        highBtn.tag = 102
        highBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        highBtn.accessibilityIdentifier = "voice_chat_ains_setting_self_high"
        addSubview(highBtn)
        
        cusBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        cusBtn.setTitle(" \("voice_ains_custom".voice_localized()) ", for: .normal)
        cusBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        cusBtn.font(UIFont.systemFont(ofSize: 11))
        cusBtn.layer.cornerRadius = 3
        cusBtn.layer.masksToBounds = true
        cusBtn.tag = 105
        cusBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        cusBtn.accessibilityIdentifier = "voice_chat_ains_setting_self_off"
        addSubview(cusBtn)

        titleLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().offset(20)
        }

        offBtn.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-10)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }
        
        cusBtn.snp.makeConstraints { make in
            make.right.equalTo(offBtn.snp.left).offset(-6)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }
        
        aiMidBtn.snp.makeConstraints { make in
            make.right.equalTo(cusBtn.snp.left).offset(-6)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }
        
        aiHighBtn.snp.makeConstraints { make in
            make.right.equalTo(aiMidBtn.snp.left).offset(-6)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }

        midBtn.snp.makeConstraints { make in
            make.right.equalTo(aiHighBtn.snp.left).offset(-6)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }

        highBtn.snp.makeConstraints { make in
            make.right.equalTo(midBtn.snp.left).offset(-6)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }
    }

    @objc private func click(sender: UIButton) {
        setBtnStateWith(sender)
        if cellType == .ns {
            
            guard let selBlock = selBlock else {
                return
            }
            
            var state: AINS_STATE = .off
            if selBtn == highBtn {
                state = .high
            } else if selBtn == midBtn {
                state = .mid
            } else if selBtn == offBtn {
                state = .off
            }  else if selBtn == aiHighBtn {
                state = .aiHigh
            }  else if selBtn == aiMidBtn {
                state = .aiMid
            }  else if selBtn == cusBtn {
                state = .custom
            }
            UserDefaults.standard.setValue(state == .custom ? true : false, forKey: "AINSCUSTOM")
            selBlock(state)
        } else if cellType == .music {
            guard let selBlock = aedBlock else {
                return
            }
            
            var state: AED_STATE = .off
            if selBtn == offBtn {
                state = .off
            }  else if selBtn == aiHighBtn {
                state = .high
            }  else if selBtn == aiMidBtn {
                state = .low
            }  else if selBtn == cusBtn {
                state = .custom
            }
            UserDefaults.standard.setValue(state == .custom ? true : false, forKey: "AEDCUSTOM")
            selBlock(state)
        } else {
            guard let selBlock = asptBlock else {
                return
            }
            
            var state: ASPT_STATE = .off
            if selBtn == offBtn {
                state = .off
            }  else if selBtn == aiHighBtn {
                state = .high
            }  else if selBtn == aiMidBtn {
                state = .low
            }  else if selBtn == cusBtn {
                state = .custom
            }
            UserDefaults.standard.setValue(state == .custom ? true : false, forKey: "ASPTCUSTOM")
            selBlock(state)
        }
        UserDefaults.standard.synchronize()
    }

    private func setBtnStateWith(_ btn: UIButton) {
        if selBtn == btn {return}
        btn.backgroundColor = .white
        btn.layer.borderColor = UIColor(hex: 0x0A7AFF, alpha: 1).cgColor
        btn.setTitleColor(UIColor(hex: 0x0A7AFF, alpha: 1), for: .normal)
        btn.layer.borderWidth = 1

        selBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        selBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        selBtn.layer.borderColor = UIColor.clear.cgColor
        selBtn.layer.borderWidth = 0
        selBtn = btn
    }
}
