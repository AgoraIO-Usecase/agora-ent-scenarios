//
//  VMSoundSelTableViewCell.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/9.
//

import UIKit

public enum SOUND_TYPE {
    case chat
    case karaoke
    case game
    case anchor
    case none
}

class VMSoundSelTableViewCell: UITableViewCell {
    private var bgView: UIView = .init()
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width - 40
    private var typeLabel: UILabel = .init()
    private var iconView: UIImageView = .init()
    private var detailLabel: UILabel = .init()
    private var lineView: UIView = .init()
    private var usageLabel: UILabel = .init()
    private var yallaView: UIImageView = .init()
    private var soulView: UIImageView = .init()
    private var selView: UIImageView = .init()

    private var typeStr: String = ""
    private var detailStr: String = ""

    private var images = [["wangyi", "momo", "pipi", "yinyu"], ["wangyi", "jiamian", "yinyu", "paipaivoice", "wanba", "qingtian", "skr", "soul"], ["yalla-ludo", "jiamian"], ["qingmang", "cowLive", "yuwan", "weibo"]]
    private var iconImgs: [String]?
    var clickBlock: (() -> Void)?

    private var cellType: SOUND_TYPE = .chat
    private var cellHeight: CGFloat = 0

    public var isSel: Bool = false {
        didSet {
            if isSel {
                bgView.layer.borderWidth = 1
                bgView.layer.borderColor = UIColor(red: 0, green: 159 / 255.0, blue: 1, alpha: 1).cgColor
                selView.isHidden = false
                iconView.image = UIImage.sceneImage(name: "icons／Stock／listen", bundleName: "VoiceChatRoomResource")
            } else {
                bgView.layer.borderWidth = 1
                bgView.layer.borderColor = UIColor.lightGray.cgColor
                selView.isHidden = true
                iconView.image = UIImage.sceneImage(name: "", bundleName: "VoiceChatRoomResource")
            }
        }
    }

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        layoutUI()
    }

    init(style: UITableViewCell.CellStyle, reuseIdentifier: String?, cellType: SOUND_TYPE, cellHeight: CGFloat) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.cellHeight = cellHeight
        setCellType(with: cellType)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func layoutUI() {
        contentView.backgroundColor = .white
        selectionStyle = .none

        bgView.backgroundColor = .white
        bgView.layer.cornerRadius = 16
        bgView.layer.borderWidth = 1
        bgView.layer.borderColor = UIColor(red: 0, green: 159 / 255.0, blue: 1, alpha: 1).cgColor
        setShadow(view: bgView, sColor: .lightGray, offset: CGSize(width: 0, height: 0), opacity: 0.9, radius: 3)
        contentView.addSubview(bgView)

        typeLabel.text = typeStr
        typeLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        typeLabel.textColor = UIColor(red: 0, green: 159 / 255.0, blue: 1, alpha: 1)
        bgView.addSubview(typeLabel)

        iconView.image = UIImage.sceneImage(name: "icons／Stock／listen", bundleName: "VoiceChatRoomResource")
//        let tap = UITapGestureRecognizer(target: self, action: #selector(click))
//        iconView.addGestureRecognizer(tap)
//        iconView.isUserInteractionEnabled = true
        bgView.addSubview(iconView)

        detailLabel.text = detailStr
        detailLabel.textAlignment = .left
        detailLabel.numberOfLines = 0
        detailLabel.textColor = UIColor.HexColor(hex: 0x3C4267, alpha: 1)
        detailLabel.font = UIFont.systemFont(ofSize: 13)
        detailLabel.lineBreakMode = .byCharWrapping
        bgView.addSubview(detailLabel)

        selView.image = UIImage.sceneImage(name: "", bundleName: "VoiceChatRoomResource")
        addSubview(selView)

        usageLabel.text = LanguageManager.localValue(key: "voice_current_customer_usage")
        usageLabel.font = UIFont.systemFont(ofSize: 11)
        usageLabel.textColor = UIColor.HexColor(hex: 0x979CBB, alpha: 1)
        bgView.addSubview(usageLabel)

        lineView.backgroundColor = UIColor.HexColor(hex: 0xF6F6F6, alpha: 1)
        bgView.addSubview(lineView)

        guard let iconImgs = iconImgs else {
            return
        }
        var basetag = 0
        switch cellType {
        case .chat:
            basetag = 10
        case .karaoke:
            basetag = 20
        case .game:
            basetag = 30
        case .anchor:
            basetag = 40
        case .none:
            break
        }
        for (index, value) in iconImgs.enumerated() {
            let imgView = UIImageView()
            imgView.image = UIImage(value)
            imgView.tag = basetag + index
            addSubview(imgView)
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        bgView.frame = CGRect(x: 20, y: 0, width: bounds.size.width - 40, height: bounds.size.height - 20)
        typeLabel.frame = CGRect(x: 20, y: 15, width: 200, height: 17)
        iconView.frame = CGRect(x: screenWidth - 30, y: 15, width: 20, height: 20)
        detailLabel.frame = CGRect(x: 20, y: 40, width: bounds.size.width - 80, height: cellHeight)
        selView.frame = CGRect(x: screenWidth - 10, y: bounds.size.height - 50, width: 30, height: 30)
        usageLabel.frame = CGRect(x: 20, y: bounds.size.height - 74, width: 200, height: 12)
        lineView.frame = CGRect(x: 20, y: bounds.size.height - 82, width: bounds.size.width - 80, height: 1)

        for view in subviews {
            if view.isKind(of: UIImageView.self) {
                if view.tag >= 10 && view.tag <= 50 {
                    let index = view.tag % 10
                    view.frame = CGRect(x: 40 + 30 * CGFloat(index), y: bounds.size.height - 55, width: 20, height: 20)
                }
            }
        }
    }

    func setShadow(view: UIView, sColor: UIColor, offset: CGSize,
                   opacity: Float, radius: CGFloat)
    {
        // 设置阴影颜色
        view.layer.shadowColor = sColor.cgColor
        // 设置透明度
        view.layer.shadowOpacity = opacity
        // 设置阴影半径
        view.layer.shadowRadius = radius
        // 设置阴影偏移量
        view.layer.shadowOffset = offset
    }

//
//    @objc func click(){
//        guard let clickBlock = clickBlock else {
//            return
//        }
//        clickBlock()
//    }

    private func setCellType(with type: SOUND_TYPE) {
        cellType = type
        if type == .chat {
            typeStr = LanguageManager.localValue(key: "voice_social_chat")
            detailStr = LanguageManager.localValue(key: "voice_chatroom_social_chat_introduce")
            iconImgs = images[0]
        } else if type == .karaoke {
            typeStr = LanguageManager.localValue(key: "voice_karaoke")
            detailStr = LanguageManager.localValue(key: "voice_chatroom_karaoke_introduce")
            iconImgs = images[1]
        } else if type == .game {
            typeStr = LanguageManager.localValue(key: "voice_gaming_buddy")
            detailStr = LanguageManager.localValue(key: "voice_chatroom_gaming_buddy_introduce")
            iconImgs = images[2]
        } else if type == .anchor {
            typeStr = LanguageManager.localValue(key: "voice_professional_podcaster")
            detailStr = LanguageManager.localValue(key: "voice_chatroom_professional_broadcaster_introduce")
            iconImgs = images[3]
        }
    }
}
