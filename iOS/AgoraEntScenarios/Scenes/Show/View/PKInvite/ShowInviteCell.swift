//
//  ShowInviteCell.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/30.
//

import Foundation
import Agora_Scene_Utils

enum ShowPKInviteStatus: CaseIterable {
    case invite
    case waitting
    case pking
    case refused
    
    var title: String {
        switch self {
        case .invite: return "邀请".show_localized
        case .waitting: return "邀请中".show_localized
        case .pking: return "PK中".show_localized
        case .refused: return "已拒绝".show_localized
        }
    }
    var titleColor: UIColor? {
        switch self {
        case .invite, .waitting: return .white
        default: return .black
        }
    }

    var bgImage: UIImage? {
        switch self {
        case .invite, .waitting: return UIImage.show_sceneImage(name: "show_invite_btn_bg")
        default: return nil
        }
    }
}

//base invite cell
class ShowInviteCell: UITableViewCell {
    var refreshDataClosure: (() -> Void)?
    fileprivate lazy var avatarImageView: AGEImageView = {
        let imageView = AGEImageView(type: .avatar)
//        imageView.image = UIImage.show_sceneImage(name: "show_default_avatar")
        imageView.cornerRadius = 22
        return imageView
    }()
    fileprivate lazy var nameLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .middle)
//        label.text = "Antonovich A"
        return label
    }()
    fileprivate lazy var statusButton: UIButton = {
        let button = UIButton()
        button.titleLabel?.font = .systemFont(ofSize: 14)
        button.addTargetFor(self, action: #selector(onTapStatusButton(sender:)), for: .touchUpInside)
        return button
    }()
    fileprivate lazy var lineView: AGEView = {
        let view = AGEView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        return view
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        contentView.addSubview(avatarImageView)
        contentView.addSubview(nameLabel)
        contentView.addSubview(statusButton)
        contentView.addSubview(lineView)
        
        avatarImageView.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        statusButton.translatesAutoresizingMaskIntoConstraints = false
        lineView.translatesAutoresizingMaskIntoConstraints = false
        
        avatarImageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        avatarImageView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        avatarImageView.widthAnchor.constraint(equalToConstant: 44).isActive = true
        avatarImageView.heightAnchor.constraint(equalToConstant: 44).isActive = true
        
        nameLabel.leadingAnchor.constraint(equalTo: avatarImageView.trailingAnchor, constant: 11).isActive = true
        nameLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        statusButton.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        statusButton.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        lineView.leadingAnchor.constraint(equalTo: avatarImageView.leadingAnchor).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.trailingAnchor.constraint(equalTo: statusButton.trailingAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
    
    @objc
    fileprivate func onTapStatusButton(sender: UIButton) {
        AlertManager.hiddenView()
    }
}

//pk invite cell
class ShowPKInviteViewCell: ShowInviteCell {
    var pkUser: ShowPKUserInfo? {
        didSet {
            defer {
                _refreshPKStatus()
            }
            
            guard let info = pkUser else { return }
            avatarImageView.sd_setImage(with: URL(string: info.ownerAvater ?? ""),
                                        placeholderImage: UIImage.show_sceneImage(name: "show_default_avatar"))
            nameLabel.text = info.ownerName
        }
    }
    var pkInvitation: ShowPKInvitation? {
        didSet {
            _refreshPKStatus()
        }
    }
    var pkStatus: ShowPKInviteStatus = .invite {
        didSet {
            statusButton.setTitle(pkStatus.title, for: .normal)
            statusButton.setTitleColor(pkStatus.titleColor, for: .normal)
            statusButton.setBackgroundImage(pkStatus.bgImage, for: .normal)
        }
    }
    
    private func _refreshPKStatus() {
        var stauts: ShowPKInviteStatus = pkUser?.interactStatus == .pking ? .pking : .invite
        if stauts == .invite {
            stauts = pkInvitation?.status == .waitting ? .waitting : .invite
        }
        pkStatus = stauts
    }
    
    @objc
    fileprivate override func onTapStatusButton(sender: UIButton) {
        super.onTapStatusButton(sender: sender)
        guard let invitation = pkUser else {
            return
        }

        AppContext.showServiceImp.createPKInvitation(room: invitation) {[weak self] error in
            if let err = error {
                ToastView.show(text: err.localizedDescription)
                return
            }
            self?.refreshDataClosure?()
        }
    }
}


//mic seat apply and invite cell
class ShowSeatApplyAndInviteViewCell: ShowInviteCell {
    private var seatApplyModel: ShowMicSeatApply?
    private var seatInvitationModel: ShowUser?

    func setupApplyAndInviteData(model: Any?, isLink: Bool) {
        statusButton.isHidden = isLink
        if let model = model as? ShowMicSeatApply {
            seatApplyModel = model
            nameLabel.text = model.userName
            statusButton.tag = 1
            avatarImageView.sd_setImage(with: URL(string: model.avatar ?? ""),
                                        placeholderImage: UIImage.show_sceneImage(name: "show_default_avatar"))
            switch model.status {
            case .accepted:
                statusButton.isUserInteractionEnabled = false
                statusButton.setTitle("已上麦".show_localized, for: .normal)
                statusButton.setTitleColor(.black, for: .normal)
                statusButton.setBackgroundImage(nil, for: .normal)
                
            case .waitting:
                statusButton.isUserInteractionEnabled = true
                statusButton.setTitle("同意".show_localized, for: .normal)
                statusButton.setBackgroundImage(UIImage.show_sceneImage(name: "show_invite_btn_bg"), for: .normal)
                statusButton.setTitleColor(.white, for: .normal)
                
            default: break
            }
            
        } else if let model = model as? ShowUser {
            seatInvitationModel = model
            nameLabel.text = model.userName
            statusButton.tag = 2
            avatarImageView.sd_setImage(with: URL(string: model.avatar ?? ""),
                                        placeholderImage: UIImage.show_sceneImage(name: "show_default_avatar"))

            switch model.status {
            case .waitting:
                statusButton.isUserInteractionEnabled = false
                statusButton.setTitle("等待中".show_localized, for: .normal)
                statusButton.setBackgroundImage(nil, for: .normal)
                statusButton.setTitleColor(.black, for: .normal)
                
            default:
                statusButton.setTitle("邀请".show_localized, for: .normal)
                statusButton.setBackgroundImage(UIImage.show_sceneImage(name: "show_invite_btn_bg"), for: .normal)
                statusButton.setTitleColor(.white, for: .normal)
                statusButton.isUserInteractionEnabled = true
            }
        }
    }
    
    private func setupUI() {
        contentView.addSubview(avatarImageView)
        contentView.addSubview(nameLabel)
        contentView.addSubview(statusButton)
        contentView.addSubview(lineView)
        
        avatarImageView.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        statusButton.translatesAutoresizingMaskIntoConstraints = false
        lineView.translatesAutoresizingMaskIntoConstraints = false
        
        avatarImageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        avatarImageView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        avatarImageView.widthAnchor.constraint(equalToConstant: 44).isActive = true
        avatarImageView.heightAnchor.constraint(equalToConstant: 44).isActive = true
        
        nameLabel.leadingAnchor.constraint(equalTo: avatarImageView.trailingAnchor, constant: 11).isActive = true
        nameLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        statusButton.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        statusButton.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        lineView.leadingAnchor.constraint(equalTo: avatarImageView.leadingAnchor).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.trailingAnchor.constraint(equalTo: statusButton.trailingAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
    
    @objc
    fileprivate override func onTapStatusButton(sender: UIButton) {
        super.onTapStatusButton(sender: sender)
        if let model = seatApplyModel, sender.tag == 1 {
            AppContext.showServiceImp.acceptMicSeatApply(apply: model) { _ in
                self.refreshDataClosure?()
            }
        } else if let model = seatInvitationModel {
            AppContext.showServiceImp.createMicSeatInvitation(user: model) { error in
                if let err = error {
                    ToastView.show(text: err.localizedDescription)
                    return
                }
                
                self.refreshDataClosure?()
            }
        }
    }
}
