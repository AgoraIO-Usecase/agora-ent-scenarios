//
//  VMANISSetTableViewCell.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import SnapKit
import UIKit

class SAANISSetTableViewCell: UITableViewCell {
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    private var titleLabel: UILabel = .init()
    private var highBtn: UIButton = .init()
    private var midBtn: UIButton = .init()
    private var offBtn: UIButton = .init()
    private var selBtn: UIButton!
    public var isTouchAble: Bool = false
    public var ains_state: SARtcType.AINS_STATE = .mid {
        didSet {
            switch ains_state {
            case .high:
                setBtnStateWith(highBtn)
            case .mid:
                setBtnStateWith(midBtn)
            case .off:
                setBtnStateWith(offBtn)
            }
        }
    }

    var selBlock: ((SARtcType.AINS_STATE) -> Void)?

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
        titleLabel.text = sceneLocalized("Your AINS")
        titleLabel.font = UIFont.systemFont(ofSize: 13)
        titleLabel.textColor = UIColor(hex: "0x3C4267")
        contentView.addSubview(titleLabel)

        offBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        offBtn.setTitle(" \(sceneLocalized("Off")) ", for: .normal)
        offBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        offBtn.font(UIFont.systemFont(ofSize: 11))
        offBtn.layer.cornerRadius = 3
        offBtn.layer.masksToBounds = true
        offBtn.tag = 100
        offBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        addSubview(offBtn)

        midBtn.backgroundColor = .white
        midBtn.setTitle(" \(sceneLocalized("Middle")) ", for: .normal)
        midBtn.setTitleColor(UIColor(hex: "0x0A7AFF"), for: .normal)
        midBtn.font(UIFont.systemFont(ofSize: 11))
        midBtn.backgroundColor = .white
        midBtn.layer.cornerRadius = 3
        midBtn.layer.masksToBounds = true
        midBtn.layer.borderColor = UIColor(hex: "0x0A7AFF")?.cgColor
        midBtn.layer.borderWidth = 1
        midBtn.tag = 101
        midBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        addSubview(midBtn)
        selBtn = midBtn

        highBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        highBtn.setTitle(" \(sceneLocalized("High")) ", for: .normal)
        highBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        highBtn.font(UIFont.systemFont(ofSize: 11))
        highBtn.layer.cornerRadius = 3
        highBtn.layer.masksToBounds = true
        highBtn.tag = 102
        highBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        addSubview(highBtn)

        titleLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().offset(20)
        }

        offBtn.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-20)
            make.centerY.equalToSuperview()
            make.height.equalTo(24)
        }

        midBtn.snp.makeConstraints { make in
            make.right.equalTo(offBtn.snp.left).offset(-6)
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

        guard let selBlock = selBlock else {
            return
        }
        var state: SARtcType.AINS_STATE = .mid
        if selBtn == highBtn {
            state = .high
        } else if selBtn == midBtn {
            state = .mid
        } else {
            state = .off
        }
        selBlock(state)
    }

    private func setBtnStateWith(_ btn: UIButton) {
        if selBtn == btn { return }
        btn.backgroundColor = .white
        btn.layer.borderColor = UIColor(hex: "0x0A7AFF")?.cgColor
        btn.setTitleColor(UIColor(hex: "0x0A7AFF"), for: .normal)
        btn.layer.borderWidth = 1

        selBtn.backgroundColor = UIColor(red: 236 / 255.0, green: 236 / 255.0, blue: 236 / 255.0, alpha: 1)
        selBtn.setTitleColor(UIColor(red: 151 / 255.0, green: 156 / 255.0, blue: 187 / 255.0, alpha: 1), for: .normal)
        selBtn.layer.borderColor = UIColor.clear.cgColor
        selBtn.layer.borderWidth = 0
        selBtn = btn
    }
}
