//
//  ShowApplyView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/8.
//

import UIKit
import Agora_Scene_Utils

enum ShowApplyAndInviteType: String, CaseIterable {
    case apply
    case invite
    
    var title: String {
        switch self {
        case .apply: return "申请消息"
        case .invite: return "连麦邀请"
        }
    }
}

class ShowApplyAndInviteView: UIView {
    var applyStatusClosure: ((ShowInteractionStatus) -> Void)?
    
    private lazy var segmentView: ShowSegmentView = {
        let segmentView = ShowSegmentView(frame: CGRect(x: 10,
                                                        y: 23,
                                                        width: Screen.width,
                                                        height: 44),
                                          segmentStyle: .init(),
                                          titles: ShowApplyAndInviteType.allCases.map({ $0.title }))
        segmentView.style.indicatorStyle = .line
        segmentView.style.indicatorHeight = 2
        segmentView.style.indicatorColor = UIColor(hex: "#7A59FB")
        segmentView.style.indicatorWidth = 64
        segmentView.selectedTitleColor = .black
        segmentView.titlePendingHorizontal = 50
        segmentView.normalTitleColor = UIColor(hex: "#6D7291")
        segmentView.valueChange = { [weak self] index in
            if index == 0 {
                self?.tableView.emptyTitle = "暂无上麦申请".show_localized
                self?.getApplyList()
            } else {
                self?.tableView.emptyTitle = "暂无用户".show_localized
                self?.getInviteList()
            }
        }
        return segmentView
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
        view.isHidden = true
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
        view.emptyTitle = "暂无上麦申请".show_localized
        view.emptyTitleColor = UIColor(hex: "#989DBA")
        view.emptyImage = UIImage.show_sceneImage(name: "show_pkInviteViewEmpty")
        view.delegate = self
        view.register(ShowPKInviteViewCell.self,
                      forCellWithReuseIdentifier: ShowPKInviteViewCell.description())
        return view
    }()
    private var tipsViewHeightCons: NSLayoutConstraint?
    private var roomId: String?
    private var type: ShowApplyAndInviteType = .apply
    
    init(roomId: String?) {
        super.init(frame: .zero)
        self.roomId = roomId
        setupUI()
        getApplyInfo()
        getApplyList()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func getApplyList() {
        AppContext.showServiceImp.getAllMicSeatApplyList { _, list in
            guard let list = list else { return }
            self.tableView.dataArray = list
        }
    }
    private func getInviteList() {
        AppContext.showServiceImp.getAllUserList { _, list in
            guard let list = list?.filter({$0.userId != VLUserCenter.user.id}) else { return }
            self.tableView.dataArray = list
        }
    }
    private func getApplyInfo() {
        AppContext.showServiceImp.getCurrentApplyUser(roomId: roomId) { roomModel in
            self.tipsContainerView.isHidden = roomModel == nil
            self.tipsLabel.text = "与主播\(roomModel?.ownerName ?? "")PK中"
        }
    }
    
    private func setupUI() {
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        backgroundColor = .white
        translatesAutoresizingMaskIntoConstraints = false
        tipsView.translatesAutoresizingMaskIntoConstraints = false
        tipsLabel.translatesAutoresizingMaskIntoConstraints = false
        endButton.translatesAutoresizingMaskIntoConstraints = false
        statckView.translatesAutoresizingMaskIntoConstraints = false
        
        addSubview(segmentView)
        tipsView.addSubview(tipsLabel)
        tipsView.addSubview(endButton)
        addSubview(statckView)
        tipsContainerView.addSubview(tipsView)
        statckView.addArrangedSubview(tipsContainerView)
        statckView.addArrangedSubview(tableView)
        
        widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
                
        statckView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        statckView.topAnchor.constraint(equalTo: segmentView.bottomAnchor,
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
        
        endButton.centerYAnchor.constraint(equalTo: tipsView.centerYAnchor).isActive = true
        endButton.trailingAnchor.constraint(equalTo: tipsView.trailingAnchor,
                                            constant: -13).isActive = true
    }
    
    @objc
    private func onTapEndButton(sender: AGEButton) {
        tipsViewHeightCons?.constant = 0
        tipsViewHeightCons?.isActive = true
        UIView.animate(withDuration: 0.25) {
            self.layoutIfNeeded()
        }
        applyStatusClosure?(.idle)
    }
}
extension ShowApplyAndInviteView: AGETableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowPKInviteViewCell.description(),
                                                 for: indexPath) as! ShowPKInviteViewCell
        let model = self.tableView.dataArray?[indexPath.row]
        
        cell.setupApplyAndInviteData(model: model)
        cell.refreshDataClosure = { [weak self] in
            guard let self = self else { return }
            if self.type == .apply {
                self.getInviteList()
                self.applyStatusClosure?(.onSeat)
            } else {
                self.getInviteList()
            }
        }
        return cell
    }
}
