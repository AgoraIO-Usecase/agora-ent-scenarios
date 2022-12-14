//
//  ShowApplyView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/8.
//

import UIKit
import Agora_Scene_Utils

class ShowApplyView: UIView {
    private lazy var titleLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .large)
        label.text = "连麦申请".show_localized
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
    private lazy var tipsContainerView: AGEView = {
        let view = AGEView()
        return view
    }()
    private lazy var tipsView: AGEView = {
        let view = AGEView()
        view.backgroundColor = UIColor(hex: "#F4F6F9")
        view.cornerRadius(5)
        return view
    }()
    private lazy var tipsLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .middle)
        let text = " 正在等待"
        let attrs = NSMutableAttributedString(string: text)
        let attr = NSAttributedString(string: "0人", attributes: [.font: UIFont.systemFont(ofSize: 14, weight: .bold)])
        attrs.insert(attr, at: 0)
        label.attributedText = attrs
        return label
    }()
    private lazy var revokeutton: AGEButton = {
        let button = AGEButton()
        button.setTitle("撤回申请".show_localized, for: .normal)
        button.setTitleColor(UIColor(hex: "#684BF2"), for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "show_live_withdraw"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.addTarget(self, action: #selector(onTapRevokeButton(sender:)), for: .touchUpInside)
        button.isHidden = true
        return button
    }()
    private lazy var tableView: AGETableView = {
        let view = AGETableView(frame: .zero, style: .plain)
        view.rowHeight = 67
        view.emptyTitle = "暂无用户排麦".show_localized
        view.emptyTitleColor = UIColor(hex: "#989DBA")
        view.emptyImage = UIImage.show_sceneImage(name: "show_pkInviteViewEmpty")
        view.delegate = self
        view.register(ShowApplyViewCell.self,
                      forCellWithReuseIdentifier: ShowApplyViewCell.description())
        return view
    }()
    private var tipsViewHeightCons: NSLayoutConstraint?
    private var interactionModel: ShowInteractionInfo?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func getAllMicSeatList(autoApply: Bool) {
        AppContext.showServiceImp.getAllInterationList { _, list in
            guard let list = list?.filterDuplicates({ $0.userId }) else { return }
            self.interactionModel = list.filter({ $0.interactStatus == .onSeat }).first
            if self.interactionModel?.userId == VLUserCenter.user.id {
                self.revokeutton.setTitle("结束".show_localized, for: .normal)
                self.revokeutton.setImage(UIImage.show_sceneImage(name: "show_live_end"),
                                          for: .normal,
                                          postion: .right,
                                          spacing: 5)
                self.revokeutton.tag = 1
                self.revokeutton.isHidden = false
            }
            self.getMicSeatList(autoApply: autoApply)
        }
    }
    
    private func getMicSeatList(autoApply: Bool) {
        AppContext.showServiceImp.getAllMicSeatApplyList { _, list in
            guard let list = list?.filter({ $0.userId != self.interactionModel?.userId }) else { return }
            let seatUserModel = list.filter({ $0.userId == VLUserCenter.user.id }).first
            if seatUserModel == nil, autoApply, self.interactionModel?.userId != VLUserCenter.user.id {
                AppContext.showServiceImp.createMicSeatApply { _ in
                    DispatchQueue.global().asyncAfter(deadline: .now() + 0.5) {
                        self.getAllMicSeatList(autoApply: autoApply)
                    } 
                }
                self.revokeutton.setTitle("撤回申请".show_localized, for: .normal)
                self.revokeutton.setImage(UIImage.show_sceneImage(name: "show_live_withdraw"),
                                          for: .normal,
                                          postion: .right,
                                          spacing: 5)
                self.revokeutton.tag = 0
                self.revokeutton.isHidden = false
            }
            self.setupTipsInfo(count: list.count)
            self.tableView.dataArray = list
        }
    }
    
    private func setupTipsInfo(count: Int) {
        let text = " "+"正在等待".show_localized
        let attrs = NSMutableAttributedString(string: text)
        let attr = NSAttributedString(string: "\(count)"+"人".show_localized,
                                      attributes: [.font: UIFont.systemFont(ofSize: 14, weight: .bold)])
        attrs.insert(attr, at: 0)
        self.tipsLabel.attributedText = attrs
    }
    
    private func setupUI() {
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        backgroundColor = .white
        translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        tipsView.translatesAutoresizingMaskIntoConstraints = false
        tipsLabel.translatesAutoresizingMaskIntoConstraints = false
        revokeutton.translatesAutoresizingMaskIntoConstraints = false
        statckView.translatesAutoresizingMaskIntoConstraints = false
        
        addSubview(titleLabel)
        tipsView.addSubview(tipsLabel)
        tipsView.addSubview(revokeutton)
        addSubview(statckView)
        tipsContainerView.addSubview(tipsView)
        statckView.addArrangedSubview(tipsContainerView)
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
        
        tipsViewHeightCons = tipsContainerView.heightAnchor.constraint(equalToConstant: 40)
        tipsViewHeightCons?.isActive = true
        tipsView.leadingAnchor.constraint(equalTo: tipsContainerView.leadingAnchor,
                                            constant: 20).isActive = true
        tipsView.trailingAnchor.constraint(equalTo: tipsContainerView.trailingAnchor,
                                             constant: -20).isActive = true
        tipsView.topAnchor.constraint(equalTo: tipsContainerView.topAnchor).isActive = true
        tipsView.bottomAnchor.constraint(equalTo: tipsContainerView.bottomAnchor).isActive = true
        
        tipsLabel.leadingAnchor.constraint(equalTo: tipsView.leadingAnchor,
                                             constant: 10).isActive = true
        tipsLabel.centerYAnchor.constraint(equalTo: tipsView.centerYAnchor).isActive = true
        
        revokeutton.centerYAnchor.constraint(equalTo: tipsView.centerYAnchor).isActive = true
        revokeutton.trailingAnchor.constraint(equalTo: tipsView.trailingAnchor,
                                            constant: -13).isActive = true
    }
    
    @objc
    private func onTapRevokeButton(sender: AGEButton) {
        if sender.tag == 0, let dataArray = tableView.dataArray, dataArray.count > 0 {
            revokeutton.isHidden = true
            AppContext.showServiceImp.cancelMicSeatApply { _ in }
            let index = tableView.dataArray?.firstIndex(where: { ($0 as? ShowMicSeatApply)?.userId == VLUserCenter.user.id }) ?? 0
            tableView.dataArray?.remove(at: index)
            setupTipsInfo(count: dataArray.count)
        } else if let interactionModel = interactionModel {
            AppContext.showServiceImp.stopInteraction(interaction: interactionModel) { _ in }
            AlertManager.hiddenView()
        }
    }
}

extension ShowApplyView: AGETableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowApplyViewCell.description(),
                                                 for: indexPath) as! ShowApplyViewCell
        if let model = self.tableView.dataArray?[indexPath.row] as? ShowMicSeatApply {
            cell.setupApplyData(model: model, index: indexPath.row)
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = UIView()
        view.backgroundColor = .white
        let titleLabel = AGELabel(colorStyle: .black, fontStyle: .middle)
        titleLabel.text = "待上麦队列".show_localized
        view.addSubview(titleLabel)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -10).isActive = true
        return view
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        (self.tableView.dataArray?.isEmpty ?? true) ? 0 : 30
    }
}


class ShowApplyViewCell: UITableViewCell {
    private lazy var sortLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .middle)
        label.text = "1"
        return label
    }()
    private lazy var avatarImageView: AGEImageView = {
        let imageView = AGEImageView(type: .avatar)
        imageView.image = UIImage.show_sceneImage(name: "show_default_avatar")
        imageView.cornerRadius = 22
        return imageView
    }()
    private lazy var nameLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .middle)
        label.text = "Antonovich A"
        return label
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
    
    func setupApplyData(model: ShowMicSeatApply, index: Int) {
        sortLabel.text = "\(index + 1)"
        avatarImageView.sd_setImage(with: URL(string: model.avatar ?? ""),
                                    placeholderImage: UIImage.show_sceneImage(name: "show_default_avatar"))
        nameLabel.text = model.userName
        if model.userId == VLUserCenter.user.id {
            nameLabel.textColor = .show_zi01
        } else {
            nameLabel.textColor = .black
        }
    }
    
    private func setupUI() {
        contentView.addSubview(sortLabel)
        contentView.addSubview(avatarImageView)
        contentView.addSubview(nameLabel)
        contentView.addSubview(lineView)
        
        sortLabel.translatesAutoresizingMaskIntoConstraints = false
        avatarImageView.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        lineView.translatesAutoresizingMaskIntoConstraints = false
        
        sortLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        sortLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        avatarImageView.leadingAnchor.constraint(equalTo: sortLabel.trailingAnchor, constant: 25).isActive = true
        avatarImageView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        avatarImageView.widthAnchor.constraint(equalToConstant: 44).isActive = true
        avatarImageView.heightAnchor.constraint(equalToConstant: 44).isActive = true
        
        nameLabel.leadingAnchor.constraint(equalTo: avatarImageView.trailingAnchor, constant: 11).isActive = true
        nameLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        lineView.leadingAnchor.constraint(equalTo: sortLabel.leadingAnchor).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
}
