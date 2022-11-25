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
    
    private var selectedType: ShowPresetType?
    private var modeName: String?
    
    private var dataArray: [ShowPresettingModel] = {
        
        let model1 = ShowPresettingModel(title: "秀场模式", desc: "倒垃圾来得及啊了解到拉进来的房间法律监督福利卡冷冻机房垃圾袋发来得及垃圾房间来得及发动机法拉三等奖爱讲道理",icon: "show_live_chat_bar_emoji_sel",optionsArray: [.show_low,.show_medium,.show_high])
        let model2 = ShowPresettingModel(title: "舞蹈/运动模式", desc: "倒垃圾来得及啊理解多拉点了法兰蝶阀拉杜拉拉的发放的拉链都发了法拉第发令肌肤垃圾袋拉进来的房间爱劳动快乐",icon: "show_live_chat_bar_emoji_sel",optionsArray: [.show_low,.show_medium,.show_high])
        return [model1, model2]
    }()
    
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
        tableView.sectionHeaderHeight = UITableView.automaticDimension
        tableView.register(ShowPresettingCell.self, forCellReuseIdentifier: ShowPresettingCellID)
        tableView.register(ShowPresettingHeaderView.self, forHeaderFooterViewReuseIdentifier: ShowPresettingHeaderViewID)
        tableView.contentInset = UIEdgeInsets(top: 20, left: 0, bottom: 0, right: 0)
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
    
    private func setUpUI(){
        view.backgroundColor = .white
        
        headerView.title = "show_advance_setting_presetting_title".show_localized
        view.addSubview(headerView)
        headerView.setLeftButtonTarget(self, action: #selector(didClickCloseButton), image: UIImage.show_sceneImage(name: "show_live_close"))
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
        headerView.setTitle(model.title, desc: model.desc, icon: model.icon)
        return headerView
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let model = dataArray[indexPath.section]
        selectedType = model.optionsArray[indexPath.row]
        modeName = model.title
    }
}
