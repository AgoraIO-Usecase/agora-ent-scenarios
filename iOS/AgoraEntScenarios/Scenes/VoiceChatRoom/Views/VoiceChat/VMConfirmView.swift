//
//  VMConfirmView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/20.
//

import UIKit
import ZSwiftBaseLib

public enum PROMPT_TYPE {
    case addbot
    case leave
}

class VMConfirmView: UIView {
    
    private var lineImgView: UIImageView = UIImageView()
    private var canBtn: UIButton = UIButton()
    private var subBtn: UIButton = UIButton()
    private var titleLabel: UILabel = UILabel()
    private var contentLabel: UILabel = UILabel()
    private var type: PROMPT_TYPE = .addbot
    var resBlock: ((Bool) -> Void)?
    
    init(frame: CGRect, type: PROMPT_TYPE) {
        super.init(frame: frame)
        self.backgroundColor = .white
        self.type = type
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func layoutUI() {
        let path: UIBezierPath = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: [.topLeft, .topRight, .bottomLeft, .bottomRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer: CAShapeLayer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer
        
        titleLabel.frame = CGRect(x: self.bounds.size.width / 2.0 - 60~, y: 30~, width: 120~, height: 22~)
        titleLabel.textAlignment = .center
        titleLabel.text = "Prompt".localized()
        titleLabel.textColor = UIColor.HexColor(hex: 0x040925, alpha: 1)
        titleLabel.font = UIFont.systemFont(ofSize: 16)
        self.addSubview(titleLabel)
        
        contentLabel.frame = CGRect(x: self.bounds.size.width / 2.0 - 150~, y: 72~, width: 300~, height: 60~)
        contentLabel.textAlignment = .center
        contentLabel.text = self.type == .addbot ? "Add Bot".localized() : "Exit Room".localized()
        contentLabel.numberOfLines = 0
        contentLabel.lineBreakMode = .byCharWrapping
        contentLabel.textColor = UIColor.HexColor(hex: 0x6C7192, alpha: 1)
        contentLabel.font = UIFont.systemFont(ofSize: 14)
        self.addSubview(contentLabel)

        canBtn.frame = CGRect(x: 30~, y: 150~, width: 120~, height: 40~)
        canBtn.setTitle("Cancel".localized(), for: .normal)
        canBtn.setTitleColor(.black, for: .normal)
        canBtn.backgroundColor = UIColor(red: 239/255.0, green: 244/255.0, blue: 1, alpha: 1)
        canBtn.addTargetFor(self, action: #selector(can), for: .touchUpInside)
        canBtn.layer.cornerRadius = 20~
        canBtn.layer.masksToBounds = true
        self.addSubview(canBtn)

        subBtn.frame = CGRect(x: self.bounds.size.width - 150~, y: 150~, width: 120~, height: 40~)
        subBtn.setTitle("Submit".localized(), for: .normal)
        subBtn.addTargetFor(self, action: #selector(sub), for: .touchUpInside)
        subBtn.setTitleColor(.white, for: .normal)
        self.addSubview(subBtn)
        
        // gradient
        let gl: CAGradientLayer = CAGradientLayer()
        gl.startPoint = CGPoint(x: 0.18, y: 0)
        gl.endPoint = CGPoint(x: 0.66, y: 1)
        gl.colors = [UIColor(red: 33/255.0, green: 155/255.0, blue: 1, alpha: 1).cgColor, UIColor(red: 52/255.0, green: 93/255.0, blue: 1, alpha: 1).cgColor]
        gl.locations = [0, 1.0]
        subBtn.layer.cornerRadius = 20~;
        subBtn.layer.masksToBounds = true;
        gl.frame = subBtn.bounds;
        subBtn.layer.addSublayer(gl)
    }
    
    @objc private func can() {
         guard let block = resBlock else {return}
         block(false)
     }
     
    @objc private func sub() {
         guard let block = resBlock else {return}
         block(true)
     }

}

