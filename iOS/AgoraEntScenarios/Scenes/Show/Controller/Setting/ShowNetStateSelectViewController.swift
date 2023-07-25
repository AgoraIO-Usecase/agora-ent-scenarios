//
//  ShowNetStateSelectViewController.swift
//  AgoraEntScenarios
//
//  Created by Jonathan on 2023/7/20.
//

import Foundation

private let TableHeaderHeight: CGFloat = 58
private let ShowPresettingCellID = "ShowPresettingCellID"
private let ShowPresettingHeaderViewID = "ShowPresettingHeaderViewID"
class ShowNetStateSelectViewController: UIViewController {
    
    public static func showInViewController(_ viewController: UIViewController) {
        let vc = ShowNetStateSelectViewController()
        vc.modalPresentationStyle = .fullScreen
        viewController.present(vc, animated: true)
    }
        
    private var netConditions: [ShowAgoraKitManager.NetCondition] = [.good, .bad]
    
    private var performances: [ShowAgoraKitManager.PerformanceMode] = [.smooth, .fluent]
    
    private var aNetCondition: ShowAgoraKitManager.NetCondition = .good
    
    private var aPerformance: ShowAgoraKitManager.PerformanceMode = .smooth
    
    private let topBar = ShowNavigationBar()

    private let tableView: UITableView = UITableView(frame: .zero, style: .grouped)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        createViews()
        createConstrains()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    @objc private func onClickSubmit() {
        ShowAgoraKitManager.shared.netCondition = aNetCondition
        ShowAgoraKitManager.shared.performanceMode = aPerformance
        ShowAgoraKitManager.shared.updateVideoProfileForMode(.single)
        dismiss(animated: true)
    }
}

// MARK: - Callback UITableView
extension ShowNetStateSelectViewController: UITableViewDelegate, UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section == 0) {
            return netConditions.count
        } else {
            return performances.count
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowPresettingCellID, for: indexPath) as! ShowPresettingCell
        if indexPath.section == 0 {
            let a = netConditions[indexPath.row]
            switch a {
            case .good:
                cell.setTitle("show_presetting_net_good".show_localized, desc: "show_presetting_net_good_detail".show_localized)
            case .bad:
                cell.setTitle("show_presetting_net_bad".show_localized, desc: "show_presetting_net_bad_detail".show_localized)
            }
            cell.aSelected = (aNetCondition == a)
        } else {
            let a = performances[indexPath.row]
            switch a {
            case .smooth:
                cell.setTitle("show_presetting_performances_smooth".show_localized, desc: "show_presetting_performances_smooth_detail".show_localized)
            case .fluent:
                cell.setTitle("show_presetting_performances_fluent".show_localized, desc: "show_presetting_performances_fluent_detail".show_localized)
            }
            cell.aSelected = (aPerformance == a)
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let headerView = tableView.dequeueReusableHeaderFooterView(withIdentifier: ShowPresettingHeaderViewID) as! ShowPresettingHeaderView
        if (section == 0) {
            headerView.setTitle("show_presetting_net_title".show_localized, desc: "show_presetting_net_title_detail".show_localized, type: .douyin)
        } else {
            headerView.setTitle("show_presetting_performances_title".show_localized, desc: "show_presetting_performances_title_detail".show_localized, type: .douyin)
        }
        return headerView
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return ShowPresettingFooterView()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.section == 0 {
            aNetCondition = netConditions[indexPath.row]
        } else {
            aPerformance = performances[indexPath.row]
        }
        tableView.reloadData()
    }
}
// MARK: - Creations
extension ShowNetStateSelectViewController {
    
    func createViews() {
        view.backgroundColor = .white
        
        tableView.backgroundColor = .white
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 60
        tableView.separatorStyle = .none
        tableView.sectionHeaderHeight = UITableView.automaticDimension
        tableView.sectionFooterHeight = 15
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
