//
//  ShowPresettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

private let TableHeaderHeight: CGFloat = 58
private let TableRowHeight: CGFloat = 60
private let ShowPresettingCellID = "ShowPresettingCellID"
private let ShowPresettingHeaderViewID = "ShowPresettingHeaderViewID"

class ShowPresettingVC: UIViewController {
    
    // 选中预设
    var didSelectedPresetType: ((_ type: ShowPresetType, _ modeName: String)->())?
    var isBroadcaster = true
//    var selectedIndexPath: IndexPath?
    
    private var selectedType: ShowPresetType?
    private var modeName: String?
    
    private var dataArray: [ShowPresettingModel] {
        if isBroadcaster {
            // 秀场模式
            let showMode = ShowPresettingModel(title: "show_presetting_mode_show_title".show_localized, desc: "show_presetting_mode_show_desc".show_localized,standard: .douyin, optionsArray: [.show_low,.show_medium,.show_high])
            return [showMode]
        }else {
            // 画质增强模式
            let qualityMode = ShowPresettingModel(title: "show_presetting_mode_qulity_title".show_localized, desc: "show_presetting_mode_qulity_desc".show_localized,standard: .douyin, optionsArray: [.quality_low,.quality_medium,.quality_high])
            // 基础模式
            let baseMode = ShowPresettingModel(title: "show_presetting_mode_base_title".show_localized, desc: "show_presetting_mode_base_title".show_localized,standard: .douyin, optionsArray: [.base_low,.base_medium,.base_high])
            return [qualityMode,baseMode]
        }
    }
    
    private lazy var headerView: ShowNavigationBar = {
        let headerView = ShowNavigationBar()
        return headerView
    }()

    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .grouped)
        tableView.backgroundColor = .white
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = TableRowHeight
        tableView.separatorStyle = .none
        tableView.sectionHeaderHeight = UITableView.automaticDimension
        tableView.sectionFooterHeight = 15
        tableView.register(ShowPresettingCell.self, forCellReuseIdentifier: ShowPresettingCellID)
        tableView.register(ShowPresettingHeaderView.self, forHeaderFooterViewReuseIdentifier: ShowPresettingHeaderViewID)
        return tableView
    }()
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .fullScreen
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        /*
        if isBroadcaster == false, ShowPresettingVC.selectedIndexPath != nil {
            self.tableView.selectRow(at: ShowPresettingVC.selectedIndexPath, animated: false, scrollPosition: .none)
        }
         */
    }
    
    private func setUpUI(){
        view.backgroundColor = .white
        if isBroadcaster {
            headerView.title = "show_advance_setting_presetting_title".show_localized
        }else{
            headerView.title = "show_advance_setting_presetting_audience_title".show_localized
        }
        view.addSubview(headerView)
        headerView.setLeftButtonTarget(self, action: #selector(didClickCloseButton), image: UIImage.show_sceneImage(name: "show_preset_close"))
        let saveButtonItem = ShowBarButtonItem(title: "show_advanced_setting_presetting_save".show_localized, target: self, action: #selector(didClickSaveButton))
        headerView.rightItems = [saveButtonItem]
        
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.top.equalTo(headerView.snp.bottom)
        }
    }
    
    @objc private func didClickCloseButton() {
        dismiss(animated: true)
    }
    
    @objc private func didClickSaveButton() {
        guard let selectedType = selectedType,let modeName = modeName else {
            ToastView.show(text: "请选择预设模式")
            return
        }
        dismiss(animated: true) {[weak self] in
            self?.didSelectedPresetType?(selectedType,modeName)
        }
    }
}


extension ShowPresettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let model =  dataArray[section]
        return model.optionsArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let model = dataArray[indexPath.section]
        let type = model.optionsArray[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowPresettingCellID, for: indexPath) as! ShowPresettingCell
        cell.selectionStyle = .none
        cell.setTitle(type.title, desc: type.iosInfo)
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let headerView = tableView.dequeueReusableHeaderFooterView(withIdentifier: ShowPresettingHeaderViewID) as! ShowPresettingHeaderView
        let model = dataArray[section]
        headerView.setTitle(model.title, desc: model.desc, type: model.standard)
        return headerView
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return ShowPresettingFooterView()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let model = dataArray[indexPath.section]
        selectedType = model.optionsArray[indexPath.row]
        modeName = model.title
//        selectedIndexPath = indexPath
    }
}
