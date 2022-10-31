//
//  VMSoundView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/10/9.
//

import Foundation
import UIKit

class VMSoundView: UIView {
    
    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56~)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1),UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
    }()
    
    private var bgView: UIView = UIView()
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width - 40~
    private var typeLabel: UILabel = UILabel()
    private var detailLabel: UILabel = UILabel()
    private var usageLabel: UILabel = UILabel()
    private var yallaView: UIImageView = UIImageView()
    private var soulView: UIImageView = UIImageView()
    private var selView: UIImageView = UIImageView()
    private var iconBgView: UIView = UIView()
    private var lineImgView: UIImageView = UIImageView()
    
    private var soundEffect: String = ""
    private var typeStr: String = ""
    private var detailStr: String = ""
    private var images = [["wangyi","momo","pipi","yinyu"],["wangyi","jiamian","yinyu","paipaivoice","wanba","qingtian","skr","soul"],["yalla-ludo","jiamian"],["qingmang","cowLive","yuwan","weibo"]]
    private var iconImgs:[String]?
    
    public init(frame: CGRect, soundEffect: String) {
        super.init(frame: frame)
        setSoundEffect(soundEffect)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public var cellHeight: CGFloat = 0
    
    private func layoutUI() {
        
        bgView.backgroundColor = .white
        self.addSubview(bgView)
        
        let path: UIBezierPath = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer: CAShapeLayer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer

        bgView.addSubview(cover)

        lineImgView.image = UIImage("pop_indicator")
        bgView.addSubview(lineImgView)

        typeLabel.text = typeStr
        typeLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        typeLabel.textColor = UIColor(red: 0.016, green: 0.035, blue: 0.145, alpha: 1)
        typeLabel.textAlignment = .center
        bgView.addSubview(typeLabel)

        detailLabel.text = detailStr
        detailLabel.textAlignment = .left
        detailLabel.numberOfLines = 0
        detailLabel.textColor = UIColor(red: 0.235, green: 0.257, blue: 0.403, alpha: 1)
        detailLabel.font = UIFont.systemFont(ofSize: 13)
        detailLabel.lineBreakMode = .byCharWrapping
        bgView.addSubview(detailLabel)
        
        iconBgView.backgroundColor = UIColor(red: 241/255.0, green: 243/255.0, blue: 248/255.0, alpha: 1)
        iconBgView.layer.cornerRadius = 10
        iconBgView.layer.masksToBounds = true
        bgView.addSubview(iconBgView)

        usageLabel.text = "CUS Use".localized()
        usageLabel.font = UIFont.systemFont(ofSize: 12)
        usageLabel.textColor = UIColor(red: 0.593, green: 0.612, blue: 0.732, alpha: 1)
        bgView.addSubview(usageLabel)
        
        guard let iconImgs = iconImgs else {
            return
        }
        var basetag = 0
        switch soundEffect {
        case "Social Chat":
            basetag = 110
        case "Karaoke":
            basetag = 120
        case "Gaming Buddy":
            basetag = 130
        default:
            basetag = 140
        }
        for (index, value) in iconImgs.enumerated() {
            let imgView: UIImageView = UIImageView()
            imgView.image = UIImage(value)
            imgView.tag = basetag + index
            self.addSubview(imgView)
        }

    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgView.frame = CGRect(x: 0~, y: 0, width: self.bounds.size.width, height: self.bounds.size.height)
        lineImgView.frame = CGRect(x: self.bounds.size.width / 2.0 - 20~, y: 8, width: 40~, height: 4)
        typeLabel.frame = CGRect(x: 20~, y: 32, width: self.bounds.size.width - 40~, height: 18)
        detailLabel.frame = CGRect(x: 20~, y: 60, width: self.bounds.size.width - 40~, height: cellHeight)
        iconBgView.frame = CGRect(x: 20~, y: self.bounds.size.height - 94, width: self.bounds.size.width - 40~, height: 60)
        yallaView.frame = CGRect(x: 30~, y: self.bounds.size.height - 62, width: 20~, height: 20)
        soulView.frame = CGRect(x: 60~, y: self.bounds.size.height - 62, width: 20~, height: 20)
        usageLabel.frame = CGRect(x: 30~, y: self.bounds.size.height - 84, width: 300~, height: 12)
        for view in self.subviews {
            if view.isKind(of: UIImageView.self) {
                if view.tag >= 110 && view.tag <= 150 {
                    let index = view.tag % 10
                    view.frame = CGRect(x: 30~ + 30~ * CGFloat(index), y: self.bounds.size.height - 65~, width: 20~, height: 20~)
                }
            }
        }
    }
    
    func textHeight(text: String, fontSize: CGFloat, width: CGFloat) -> CGFloat {
        return text.boundingRect(with:CGSize(width: width, height:CGFloat(MAXFLOAT)), options: .usesLineFragmentOrigin, attributes: [.font:UIFont.systemFont(ofSize: fontSize)], context:nil).size.height+5
        
    }
    
    private func setSoundEffect(_ effect: String) {
        self.soundEffect = effect
        typeStr = effect.localized()
        switch effect {
        case "Social Chat":
            detailStr = "This sound effect focuses on solving the voice call problem of the Social Chat scene, including noise cancellation and echo suppression of the anchor's voice. It can enable users of different network environments and models to enjoy ultra-low delay and clear and beautiful voice in multi-person chat.".localized()
            iconImgs = images[0]
        case "Karaoke":
            detailStr = "This sound effect focuses on solving all kinds of problems in the Karaoke scene of single-person or multi-person singing, including the balance processing of accompaniment and voice, the beautification of sound melody and voice line, the volume balance and real-time synchronization of multi-person chorus, etc. It can make the scenes of Karaoke more realistic and the singers' songs more beautiful.".localized()
            iconImgs = images[1]
        case "Gaming Buddy":
            detailStr = "This sound effect focuses on solving all kinds of problems in the game scene where the anchor plays with him, including the collaborative reverberation processing of voice and game sound, the melody of sound and the beautification of sound lines. It can make the voice of the accompanying anchor more attractive and ensure the scene feeling of the game voice. ".localized()
            iconImgs = images[2]
        default:
            detailStr = "This sound effect focuses on solving the problems of poor sound quality of mono anchors and compatibility with mainstream external sound cards. The sound network stereo collection and high sound quality technology can greatly improve the sound quality of anchors using sound cards and enhance the attraction of live broadcasting rooms. At present, it has been adapted to mainstream sound cards in the market. ".localized()
            iconImgs = images[3]
        }
        cellHeight = textHeight(text: detailStr, fontSize: 13, width: self.bounds.size.width - 40~)
    }
}
