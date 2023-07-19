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
        case .apply: return "show_linking_request_message".show_localized
        case .invite: return "show_onseat_invite".show_localized
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
            self?.tableView.dataArray = []
            if index == 0 {
                self?.tableView.emptyTitle = "show_empty_onseat_apply".show_localized
                self?.getApplyList()
            } else {
                self?.tableView.emptyTitle = "show_no_user_yet".show_localized
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
        button.setTitle("show_stop_pking".show_localized, for: .normal)
        button.setTitleColor(UIColor(hex: "#684BF2"), for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "show_live_close"),
                        for: .normal,
                        postion: .right,
                        spacing: 5)
        button.addTarget(self, action: #selector(onTapEndButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var tableView: AGETableView = {
        let view = AGETableView()
        view.rowHeight = 67
        view.emptyTitle = "show_empty_onseat_apply".show_localized
        view.emptyTitleColor = UIColor(hex: "#989DBA")
        view.emptyImage = UIImage.show_sceneImage(name: "show_pkInviteViewEmpty")
        view.delegate = self
        view.register(ShowSeatApplyAndInviteViewCell.self,
                      forCellWithReuseIdentifier: ShowSeatApplyAndInviteViewCell.description())
        return view
    }()
    private var tipsViewHeightCons: NSLayoutConstraint?
    private var roomId: String!
    private var type: ShowApplyAndInviteType = .apply
    var seatMicModel: ShowInteractionInfo? {
        didSet {
            if seatMicModel == oldValue {
                return
            }
            self.tipsContainerView.isHidden = seatMicModel == nil
            self.tipsLabel.text = String(format: "show_onseat_with_broadcastor".show_localized, seatMicModel?.userName ?? "")
            self.updateLayout(isHidden: seatMicModel == nil)
            self.tableView.reloadData()
        }
    }
    
    init(roomId: String) {
        self.roomId = roomId
        super.init(frame: .zero)
        setupUI()
        getApplyList()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func reloadData() {
        if segmentView.selectIndex == 0 {
            getApplyList()
            
        } else {
            getInviteList()
        }
    }
    
    private func getApplyList() {
        AppContext.showServiceImp(roomId).getAllMicSeatApplyList {[weak self] _, list in
            guard let list = list?.filterDuplicates({ $0.userId }) else { return }
            self?.tableView.dataArray = list.filter({ $0.status == .waitting })
        }
    }
    private func getInviteList() {
        AppContext.showServiceImp(roomId).getAllUserList {[weak self] _, list in
            guard let list = list?.filter({$0.userId != VLUserCenter.user.id}) else { return }
            self?.tableView.dataArray = list.filter({ $0.status != .accepted })
        }
    }
    
    private func getApplyPKInfo() {
        AppContext.showServiceImp(roomId).getCurrentApplyUser(roomId: roomId) {[weak self] roomModel in
            self?.tipsContainerView.isHidden = roomModel == nil
            self?.tipsLabel.text = String(format: "show_pking_with_broadcastor".show_localized, roomModel?.ownerName ?? "")
        }
    }
    private func getApplyLinkInfo() {
        
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
    
    private func updateLayout(isHidden: Bool) {
        tipsViewHeightCons?.constant = isHidden ? 0 : 40
        tipsViewHeightCons?.isActive = true
        UIView.animate(withDuration: 0.25) {
            self.layoutIfNeeded()
        }
    }
    
    @objc
    private func onTapEndButton(sender: AGEButton) {
        updateLayout(isHidden: true)
        if let model = seatMicModel {
            AppContext.showServiceImp(roomId).stopInteraction(interaction: model) { _ in }
        }
        applyStatusClosure?(.idle)
    }
}
extension ShowApplyAndInviteView: AGETableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowSeatApplyAndInviteViewCell.description(),
                                                 for: indexPath) as! ShowSeatApplyAndInviteViewCell
        cell.roomId = roomId
        let model = self.tableView.dataArray?[indexPath.row]
        
        cell.setupApplyAndInviteData(model: model, isLink: seatMicModel != nil)
        return cell
    }
}
