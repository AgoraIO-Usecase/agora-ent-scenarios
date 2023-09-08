//
//  ShowNetStateSelectViewController.swift
//  AgoraEntScenarios
//
//  Created by Jonathan on 2023/7/20.
//

import Foundation

private let ShowPresettingCellID = "ShowPresettingCellID"
private let ShowPresettingHeaderViewID = "ShowPresettingHeaderViewID"
class ShowNetStateSelectViewController: UIViewController {
    
    private enum SectionType {
        case deviceLevel
        case netCondition
        case performance
    }
    
    public static func showInViewController(_ viewController: UIViewController) {
        let vc = ShowNetStateSelectViewController()
        vc.modalPresentationStyle = .fullScreen
        viewController.present(vc, animated: true)
    }
    
    private var sections: [SectionType] = [.performance, .netCondition]
    
    private var deviceLevels: [ShowAgoraKitManager.DeviceLevel] = [.high, .medium, .low]
    
    private var netConditions: [ShowAgoraKitManager.NetCondition] = [.good, .bad]
    
    private var performances: [ShowAgoraKitManager.PerformanceMode] = [.fluent, .smooth]
    
    private var aDeviceLevel: ShowAgoraKitManager.DeviceLevel = .high
    
    private var aNetCondition: ShowAgoraKitManager.NetCondition = .good
    
    private var aPerformance: ShowAgoraKitManager.PerformanceMode = .fluent
    
    private let topBar = ShowNavigationBar()
    
    private let footerView = ShowNetStateFooterView(frame: CGRect(x: 0, y: 0, width: 100, height: 160))

    private let tableView: UITableView = UITableView(frame: .zero, style: .grouped)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        aDeviceLevel = ShowAgoraKitManager.shared.deviceLevel
        
        createViews()
        createConstrains()
        updateSections()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    @objc private func onClickSubmit() {
        ShowAgoraKitManager.shared.netCondition = aNetCondition
        ShowAgoraKitManager.shared.performanceMode = aPerformance
        ShowAgoraKitManager.shared.deviceLevel = aDeviceLevel
        ShowAgoraKitManager.shared.setupBroadcasterProfile()
        dismiss(animated: true)
    }
    
    private func updateSections() {
        var temp: [SectionType] = []
        if AppContext.shared.isDebugMode {
            temp.append(.deviceLevel)
        }
        temp.append(.performance)
        if aPerformance == .fluent {
            temp.append(.netCondition)
        }
        sections = temp
        let deviceStr = aDeviceLevel.description() + "（\(ShowAgoraKitManager.shared.deviceScore)）"
        footerView.setDeviceLevel(text: deviceStr)
        tableView.reloadData()
    }
}

// MARK: - Callback UITableView
extension ShowNetStateSelectViewController: UITableViewDelegate, UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let sectionType = sections[section]
        if (sectionType == .deviceLevel) {
            return deviceLevels.count
        } else if (sectionType == .performance) {
            return performances.count
        } else if (sectionType == .netCondition) {
            return netConditions.count
        } else {
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowPresettingCellID, for: indexPath) as! ShowPresettingCell
        let sectionType = sections[indexPath.section]
        if (sectionType == .deviceLevel) {
            let a = deviceLevels[indexPath.row]
            switch a {
            case .high:
                cell.setTitle("show_presetting_device_level_high_title".show_localized,
                              desc: "show_presetting_device_level_high_desc".show_localized)
            case .medium:
                cell.setTitle("show_presetting_device_level_medium_title".show_localized,
                              desc: "show_presetting_device_level_medium_desc".show_localized)
            case .low:
                cell.setTitle("show_presetting_device_level_low_title".show_localized,
                              desc: "show_presetting_device_level_low_desc".show_localized)
            }
            cell.aSelected = (aDeviceLevel == a)
        } else if (sectionType == .performance) {
            let a = performances[indexPath.row]
            switch a {
            case .smooth:
                cell.setTitle("show_presetting_performances_smooth".show_localized,
                              desc: "show_presetting_performances_smooth".show_localized)
            case .fluent:
                cell.setTitle("show_presetting_performances_fluent".show_localized,
                              desc: "show_presetting_performances_fluent".show_localized)
            }
            cell.aSelected = (aPerformance == a)
        } else {
            let a = netConditions[indexPath.row]
            switch a {
            case .good:
                cell.setTitle("show_presetting_net_good".show_localized,
                              desc: "show_presetting_net_good_detail".show_localized)
            case .bad:
                cell.setTitle("show_presetting_net_bad".show_localized,
                              desc: "show_presetting_net_bad_detail".show_localized)
            }
            cell.aSelected = (aNetCondition == a)
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let headerView = tableView.dequeueReusableHeaderFooterView(withIdentifier: ShowPresettingHeaderViewID) as! ShowPresettingHeaderView
        let sectionType = sections[section]
        if (sectionType == .deviceLevel) {
            headerView.setTitle("show_presetting_mode_show_title".show_localized,
                                desc: "show_presetting_mode_show_desc".show_localized)
        } else if (sectionType == .performance) {
            headerView.setTitle("show_presetting_performances_title".show_localized,
                                desc: "show_presetting_performances_title_detail".show_localized)
        } else if (sectionType == .netCondition) {
            headerView.setTitle("show_presetting_net_title".show_localized,
                                desc: "show_presetting_net_title_detail".show_localized)
        }
        return headerView
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return ShowPresettingFooterView()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let sectionType = sections[indexPath.section]
        if (sectionType == .deviceLevel) {
            aDeviceLevel = deviceLevels[indexPath.row]
        } else if (sectionType == .performance) {
            aPerformance = performances[indexPath.row]
        } else if (sectionType == .netCondition) {
            aNetCondition = netConditions[indexPath.row]
        }
        updateSections()
    }
}
// MARK: - Creations
extension ShowNetStateSelectViewController {
    
    func createViews() {
        view.backgroundColor = .white
        let deviceStr = aDeviceLevel.description() + "（\(ShowAgoraKitManager.shared.deviceScore)）"
        footerView.setDeviceLevel(text: deviceStr)
        
        tableView.backgroundColor = .white
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 60
        tableView.separatorStyle = .none
        tableView.sectionHeaderHeight = UITableView.automaticDimension
        tableView.sectionFooterHeight = 16
        tableView.tableFooterView = footerView
        tableView.register(ShowPresettingCell.self, forCellReuseIdentifier: ShowPresettingCellID)
        tableView.register(ShowPresettingHeaderView.self, forHeaderFooterViewReuseIdentifier: ShowPresettingHeaderViewID)
        view.addSubview(tableView)
        
        topBar.title = "show_advance_setting_presetting_title".show_localized
        topBar.leftItems = nil;
        let saveButtonItem = ShowBarButtonItem(title: "show_advanced_setting_presetting_save".show_localized, target: self, action: #selector(onClickSubmit))
        topBar.rightItems = [saveButtonItem]
        view.addSubview(topBar)
    }
    
    func createConstrains() {
        tableView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.top.equalTo(topBar.snp.bottom)
        }
    }
}
// MARK: - ShowNetStateFooterView
fileprivate class ShowNetStateFooterView: UITableViewHeaderFooterView {
    
    private let radiusView = UIView()
    
    public let infoLabel = UILabel()
    
    private let suggestLabel = UILabel()
    
    private let suggestInfoLabel = UILabel()
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        createViews()
        createConstrains()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setDeviceLevel(text: String) {
        infoLabel.text = "show_presetting_info_device_level".show_localized + text
    }
    
    private func createViews(){
        radiusView.backgroundColor = .show_preset_bg
        radiusView.layer.cornerRadius = 16
        radiusView.clipsToBounds = true
        contentView.addSubview(radiusView)
        
        infoLabel.font = UIFont.systemFont(ofSize: 16)
        infoLabel.textColor = .show_chat_input_text
        infoLabel.font = .show_R_14
        infoLabel.numberOfLines = 0
        infoLabel.textAlignment = .left
        contentView.addSubview(infoLabel)
        
        suggestLabel.font = UIFont.systemFont(ofSize: 16)
        suggestLabel.textColor = .show_chat_input_text
        suggestLabel.font = .show_R_14
        suggestLabel.numberOfLines = 1
        suggestLabel.textAlignment = .left
        suggestLabel.text = "show_presetting_info_suggest".show_localized
        contentView.addSubview(suggestLabel)
        
        suggestInfoLabel.font = UIFont.systemFont(ofSize: 16)
        suggestInfoLabel.textColor = .show_chat_input_text
        suggestInfoLabel.font = .show_R_14
        suggestInfoLabel.numberOfLines = 0
        suggestInfoLabel.textAlignment = .left
        suggestInfoLabel.text = "show_presetting_info_suggest_detail".show_localized
        contentView.addSubview(suggestInfoLabel)
    }
    
    private func createConstrains() {
        radiusView.snp.makeConstraints { make in
            make.top.equalTo(20)
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.bottom.equalTo(-20)
        }
        infoLabel.snp.makeConstraints { make in
            make.top.equalTo(radiusView).offset(20)
            make.left.equalTo(radiusView).offset(20)
            make.right.equalTo(radiusView).offset(-20)
        }
        suggestLabel.snp.makeConstraints { make in
            make.top.equalTo(infoLabel.snp.bottom).offset(15)
            make.left.equalTo(radiusView).offset(20)
        }
        suggestInfoLabel.snp.makeConstraints { make in
            make.top.equalTo(suggestLabel)
            make.left.equalTo(suggestLabel.snp.right)
            make.right.equalTo(radiusView).offset(-20)
            make.bottom.equalTo(radiusView).offset(-20)
        }
    }
}
