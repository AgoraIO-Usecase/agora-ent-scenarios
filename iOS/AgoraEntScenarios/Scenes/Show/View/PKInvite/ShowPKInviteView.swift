//
//  PKInviteView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/8.
//

import UIKit
import Agora_Scene_Utils

class ShowPKInviteView: UIView {
    var pkUserInvitationList: [ShowPKUserInfo]? {
        didSet {
            tableView.dataArray = pkUserInvitationList ?? []
        }
    }
    private lazy var titleLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .large)
        label.text = "PK邀请".show_localized
        return label
    }()
    private lazy var statckView: UIStackView = {
        let stackView = UIStackView()
        stackView.alignment = .fill
        stackView.axis = .vertical
        stackView.distribution = .fill
        stackView.spacing = 0
        return stackView
    }()
    private lazy var pkTipsContainerView: AGEView = {
        let view = AGEView()
        return view
    }()
    private lazy var pkTipsView: AGEView = {
        let view = AGEView()
        view.backgroundColor = UIColor(hex: "#F4F6F9")
        view.cornerRadius(5)
        return view
    }()
    private lazy var pkTipsLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .middle)
        label.text = "与主播gdsklgjlgPK中"
        return label
    }()
    private lazy var endButton: AGEButton = {
        let button = AGEButton()
        button.setTitle("结束".show_localized, for: .normal)
        button.setTitleColor(UIColor(hex: "#684BF2"), for: .normal)
        let image = UIImage(systemName: "xmark.circle")?.withTintColor(UIColor(hex: "#684BF2"),
                                                                       renderingMode: .alwaysOriginal)
        button.setImage(image, for: .normal, postion: .right, spacing: 5)
        button.addTarget(self, action: #selector(onTapEndButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var tableView: AGETableView = {
        let view = AGETableView()
        view.rowHeight = 67
        view.emptyTitle = "暂无主播在线".show_localized
        view.emptyTitleColor = UIColor(hex: "#989DBA")
        view.emptyImage = UIImage.show_sceneImage(name: "show_pkInviteViewEmpty")
        view.delegate = self
        view.register(ShowPKInviteViewCell.self,
                      forCellWithReuseIdentifier: ShowPKInviteViewCell.description())
//        view.dataArray = (0...10).map({ $0 })
        return view
    }()
    private var pkTipsViewHeightCons: NSLayoutConstraint?
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        backgroundColor = .white
        translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        pkTipsView.translatesAutoresizingMaskIntoConstraints = false
        pkTipsLabel.translatesAutoresizingMaskIntoConstraints = false
        endButton.translatesAutoresizingMaskIntoConstraints = false
        statckView.translatesAutoresizingMaskIntoConstraints = false
        
        addSubview(titleLabel)
        pkTipsView.addSubview(pkTipsLabel)
        pkTipsView.addSubview(endButton)
        addSubview(statckView)
        pkTipsContainerView.addSubview(pkTipsView)
        statckView.addArrangedSubview(pkTipsContainerView)
        statckView.addArrangedSubview(tableView)
        
        widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 23).isActive = true
        
        statckView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        statckView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor,
                                        constant: 13).isActive = true
        statckView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        statckView.bottomAnchor.constraint(equalTo: bottomAnchor,
                                           constant: -Screen.safeAreaBottomHeight()).isActive = true
        statckView.heightAnchor.constraint(equalToConstant: 340).isActive = true
        
        pkTipsViewHeightCons = pkTipsContainerView.heightAnchor.constraint(equalToConstant: 40)
        pkTipsViewHeightCons?.isActive = true
        pkTipsView.leadingAnchor.constraint(equalTo: pkTipsContainerView.leadingAnchor,
                                            constant: 20).isActive = true
        pkTipsView.trailingAnchor.constraint(equalTo: pkTipsContainerView.trailingAnchor,
                                             constant: -20).isActive = true
        pkTipsView.topAnchor.constraint(equalTo: pkTipsContainerView.topAnchor).isActive = true
        pkTipsView.bottomAnchor.constraint(equalTo: pkTipsContainerView.bottomAnchor).isActive = true
        
        pkTipsLabel.leadingAnchor.constraint(equalTo: pkTipsView.leadingAnchor,
                                             constant: 10).isActive = true
        pkTipsLabel.centerYAnchor.constraint(equalTo: pkTipsView.centerYAnchor).isActive = true
        
        endButton.centerYAnchor.constraint(equalTo: pkTipsView.centerYAnchor).isActive = true
        endButton.trailingAnchor.constraint(equalTo: pkTipsView.trailingAnchor,
                                            constant: -13).isActive = true
    }
    
    @objc
    private func onTapEndButton(sender: AGEButton) {
        pkTipsViewHeightCons?.constant = 0
        pkTipsViewHeightCons?.isActive = true
        UIView.animate(withDuration: 0.25) {
            self.layoutIfNeeded()
        }
    }
}
extension ShowPKInviteView: AGETableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowPKInviteViewCell.description(),
                                                 for: indexPath) as! ShowPKInviteViewCell
        cell.pkUserInvitation = self.pkUserInvitationList?[indexPath.row]
        return cell
    }
}

enum ShowPKInviteStatus: CaseIterable {
    case invite
    case pking
    case refused
    
    var title: String {
        switch self {
        case .invite: return "邀请".show_localized
        case .pking: return "PK中".show_localized
        case .refused: return "已拒绝".show_localized
        }
    }
    var titleColor: UIColor? {
        switch self {
        case .invite: return .white
        default: return .black
        }
    }

    var bgImage: UIImage? {
        switch self {
        case .invite: return UIImage.show_sceneImage(name: "show_invite_btn_bg")
        default: return nil
        }
    }
}

class ShowPKInviteViewCell: UITableViewCell {
    var pkUserInvitation: ShowPKUserInfo? {
        didSet {
            guard let info = pkUserInvitation else { return }
            avatarImageView.sd_setImage(with: URL(string: info.ownerAvater ?? ""),
                                        placeholderImage: UIImage.show_sceneImage(name: "show_default_avatar"))
            nameLabel.text = info.ownerName
            pkStatus = info.interactStatus == .pking  ? .pking : .invite
        }
    }
    var pkStatus: ShowPKInviteStatus = .invite {
        didSet {
            statusButton.setTitle(pkStatus.title, for: .normal)
            statusButton.setTitleColor(pkStatus.titleColor, for: .normal)
            statusButton.setBackgroundImage(pkStatus.bgImage, for: .normal)
        }
    }
    private lazy var avatarImageView: AGEImageView = {
        let imageView = AGEImageView(type: .avatar)
//        imageView.image = UIImage.show_sceneImage(name: "show_default_avatar")
        imageView.cornerRadius = 22
        return imageView
    }()
    private lazy var nameLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .middle)
//        label.text = "Antonovich A"
        return label
    }()
    private lazy var statusButton: UIButton = {
        let button = UIButton()
        button.titleLabel?.font = .systemFont(ofSize: 14)
        button.addTargetFor(self, action: #selector(onTapStatusButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var lineView: AGEView = {
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
    private func onTapStatusButton(sender: UIButton) {
        print("sender == \(sender.titleLabel?.text ?? "")")
        guard let invitation = pkUserInvitation else { return }
        AppContext.showServiceImp.createPKInvitation(room: invitation) { error in
            
        }
    }
}
