//
//  VMANISSUPTableViewCell.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import UIKit
public enum SUP_CELL_TYPE {
    case normal
    case detail
}

public enum CELL_BTN_TYPE {
    case middle
    case off
    case none
}

class VMANISSUPTableViewCell: UITableViewCell {

    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    public var titleLabel: UILabel = UILabel()
    public var detailLabel: UILabel = UILabel()
    private var noneBtn: UIButton = UIButton()
    private var anisBtn: UIButton = UIButton()
    private var selBtn: UIButton!
    public var isTouchAble: Bool = false
    public var isAudience: Bool = false
    public var cellTag: Int = 1000 {
        didSet {
            noneBtn.tag = cellTag + 1
            anisBtn.tag = cellTag
        }
    }
    
    public var cellType: SUP_CELL_TYPE = .normal {
        didSet {
            if cellType == .normal {
                detailLabel.isHidden = true
                titleLabel.frame = CGRect(x: 20, y: 17, width: 200, height: 20)
            } else {
                detailLabel.isHidden = false
                titleLabel.frame = CGRect(x: 20, y: 10, width: 200, height: 20)
            }
        }
    }
    
    public var btn_state: CELL_BTN_TYPE = .none {
        didSet {
            if btn_state == .off {
                noneBtn.backgroundColor = .white
                noneBtn.layer.borderColor = UIColor.HexColor(hex: 0x0A7AFF, alpha: 1).cgColor
                noneBtn.setTitleColor(UIColor.HexColor(hex: 0x0A7AFF, alpha: 1), for: .normal)
                noneBtn.layer.borderWidth = 1
                
                anisBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
                anisBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
                anisBtn.layer.borderColor = UIColor.clear.cgColor
                anisBtn.layer.borderWidth = 0
                
                selBtn = noneBtn
            } else if btn_state == .middle {
                anisBtn.backgroundColor = .white
                anisBtn.layer.borderColor = UIColor.HexColor(hex: 0x0A7AFF, alpha: 1).cgColor
                anisBtn.setTitleColor(UIColor.HexColor(hex: 0x0A7AFF, alpha: 1), for: .normal)
                anisBtn.layer.borderWidth = 1
                
                noneBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
                noneBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
                noneBtn.layer.borderColor = UIColor.clear.cgColor
                noneBtn.layer.borderWidth = 0
                
                selBtn = anisBtn
            } else if btn_state == .none {
                anisBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
                anisBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
                anisBtn.layer.borderColor = UIColor.clear.cgColor
                anisBtn.layer.borderWidth = 0
                
                noneBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
                noneBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
                noneBtn.layer.borderColor = UIColor.clear.cgColor
                noneBtn.layer.borderWidth = 0
                selBtn = nil
            }
        }
    }
    
    public var resBlock:((Int) -> Void)?
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        titleLabel.frame = CGRect(x: 20, y: 10, width: 200, height: 20)
        titleLabel.text = "TV Sound"
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        titleLabel.textColor = UIColor.HexColor(hex: 0x3C4267, alpha: 1)
        self.contentView.addSubview(titleLabel)
        
        detailLabel.frame = CGRect(x: 20, y: 30, width: 150, height: 30)
        detailLabel.text = "Ex bird, car,subway sounds"
        detailLabel.font = UIFont.systemFont(ofSize: 11)
        detailLabel.numberOfLines = 0
        detailLabel.lineBreakMode = .byCharWrapping
        detailLabel.textColor = UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1)
        self.contentView.addSubview(detailLabel)
        detailLabel.isHidden = true
        
        noneBtn.frame = CGRect(x: screenWidth - 70, y: 12, width: 50, height: 30)
        noneBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
        noneBtn.setTitle("Without AINS".localized(), for: .normal)
        noneBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
        noneBtn.font(UIFont.systemFont(ofSize: 11))
        noneBtn.layer.cornerRadius = 3
        noneBtn.layer.masksToBounds = true
        noneBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(noneBtn)
        
        anisBtn.frame = CGRect(x: screenWidth - 130, y: 12, width: 50, height: 30)
        anisBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
        anisBtn.setTitle("With AINS".localized(), for: .normal)
        anisBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
        anisBtn.font(UIFont.systemFont(ofSize: 11))
        anisBtn.layer.cornerRadius = 3
        anisBtn.layer.masksToBounds = true
        anisBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(anisBtn)
        
    }
    
    @objc private func click(sender: UIButton) {
        if sender == selBtn {return}
        
        guard let resBlock = resBlock else {return}
        resBlock(sender.tag)
        
        if (!isTouchAble || isAudience) {return}
        
        sender.backgroundColor = .white
        sender.layer.borderColor = UIColor.HexColor(hex: 0x0A7AFF, alpha: 1).cgColor
        sender.setTitleColor(UIColor.HexColor(hex: 0x0A7AFF, alpha: 1), for: .normal)
        sender.layer.borderWidth = 1
        if selBtn != nil {
            selBtn.backgroundColor = UIColor(red: 236/255.0, green: 236/255.0, blue: 236/255.0, alpha: 1)
            selBtn.setTitleColor(UIColor(red: 151/255.0, green: 156/255.0, blue: 187/255.0, alpha: 1), for: .normal)
            selBtn.layer.borderColor = UIColor.clear.cgColor
            selBtn.layer.borderWidth = 0
        }
        selBtn = sender
    }


}
