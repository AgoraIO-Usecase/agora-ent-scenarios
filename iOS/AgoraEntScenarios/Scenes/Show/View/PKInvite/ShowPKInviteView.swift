//
//  PKInviteView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/8.
//

import UIKit
import Agora_Scene_Utils

class ShowPKInviteView: UIView {
    var roomId: String!
    var pkUserInvitationList: [ShowPKUserInfo]? {
        didSet {
            tableView.dataArray = pkUserInvitationList ?? []
        }
    }
    var createPKInvitationMap: [String: ShowPKInvitation]? {
        didSet {
            tableView.reloadData()
        }
    }
    var interactionList: [ShowInteractionInfo]? {
        didSet {
            let pkInfo = interactionList?.filter({ $0.interactStatus == .pking }).first
            let pkTipsVisible = pkInfo == nil ? false : true
            _showTipsView(show: pkTipsVisible)
            pkTipsLabel.text = String(format: "show_pking_with_broadcastor".show_localized, pkInfo?.userName ?? "")
        }
    }
    private lazy var titleLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .large)
        label.text = "show_pking_request_message".show_localized
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
        label.text = ""
        return label
    }()
    private lazy var endButton: AGEButton = {
        let button = AGEButton()
        button.setTitle("show_stop_pking".show_localized, for: .normal)
        button.setTitleColor(UIColor(hex: "#684BF2"), for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "show_live_end"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.addTarget(self, action: #selector(onTapEndButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var tableView: AGETableView = {
        let view = AGETableView()
        view.rowHeight = 67
        view.emptyTitle = "show_empty_online_boardcaster".show_localized
        view.emptyTitleColor = UIColor(hex: "#989DBA")
        view.emptyImage = UIImage.show_sceneImage(name: "show_pkInviteViewEmpty")
        view.delegate = self
        view.register(ShowPKInviteViewCell.self,
                      forCellWithReuseIdentifier: ShowPKInviteViewCell.description())
        return view
    }()
    private var pkTipsViewHeightCons: NSLayoutConstraint?
    
    
    init(roomId: String) {
        self.roomId = roomId
        super.init(frame: .zero)
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
        
        pkTipsViewHeightCons = pkTipsContainerView.heightAnchor.constraint(equalToConstant: 0)
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
        _showTipsView(show: false)
        
        guard let pkInfo = interactionList?.filter({ $0.interactStatus == .pking }).first else {
            return
        }
        
        AppContext.showServiceImp(roomId)?.stopInteraction(interaction: pkInfo) { error in
        }
    }
    
    private func _showTipsView(show: Bool) {
        pkTipsViewHeightCons?.constant = show ? 40 : 0
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
        cell.roomId = roomId
        cell.pkUser = self.pkUserInvitationList?[indexPath.row]
        cell.pkInvitation = self.createPKInvitationMap?[cell.pkUser?.roomId ?? ""]
        return cell
    }
}
