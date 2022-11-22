//
//  ShowPresettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

private let TableHeaderHeight: CGFloat = 58
private let TableRowHeight: CGFloat = 130
private let ShowPresettingCellID = "ShowPresettingCellID"

class ShowPresettingVC: UIViewController {
    
    var didSelectedIndex: ((_ index: Int)->())?
    
    private var selectedModel: ShowPresettingModel? {
        didSet {
            oldValue?.isSelected = false
            selectedModel?.isSelected = true
        }
    }
    
    private var dataArray: [ShowPresettingModel] = {
        let model1 = ShowPresettingModel(title: "预设1", desc: "倒垃圾来得及啊了解到拉进来的房间法律监督福利卡冷冻机房垃圾袋发来得及垃圾房间来得及发动机法拉三等奖爱讲道理")
        let model2 = ShowPresettingModel(title: "预设2", desc: "倒垃圾来得及啊理解多拉点了法兰蝶阀拉杜拉拉的发放的拉链都发了法拉第发令肌肤垃圾袋拉进来的房间爱劳动快乐")
        return [model1, model2]
    }()
    
    private lazy var headerView: ShowSettingHeaderView = {
        let headerView = ShowSettingHeaderView()
        return headerView
    }()
    
    private lazy var segmentControl: UISegmentedControl = {
        let segmentCtrl = UISegmentedControl(items: ["show_advance_setting_presetting_mode_0".show_localized,"show_advance_setting_presetting_mode_1".show_localized])
        segmentCtrl.selectedSegmentIndex = 0
        return segmentCtrl
    }()

    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.backgroundColor = .white
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = TableRowHeight
        tableView.isScrollEnabled = false
        tableView.register(ShowPresettingCell.self, forCellReuseIdentifier: ShowPresettingCellID)
        tableView.contentInset = UIEdgeInsets(top: 20, left: 0, bottom: 0, right: 0)
        return tableView
    }()
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overFullScreen
        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    private func setUpUI(){

        let bgView = UIView()
        bgView.backgroundColor = .show_cover
        view.addSubview(bgView)
        bgView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(didTapBlank)))
        bgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        headerView.title = "show_advance_setting_presetting_title".show_localized
        view.addSubview(headerView)
        headerView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(320)
            make.height.equalTo(TableHeaderHeight)
        }
        
        let whiteBgView = UIView()
        whiteBgView.backgroundColor = .white
        view.addSubview(whiteBgView)
        whiteBgView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(headerView.snp.bottom)
            make.height.equalTo(42)
        }
        
        view.addSubview(segmentControl)
        segmentControl.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.top.equalTo(headerView.snp.bottom)
            make.right.equalTo(-15)
            make.height.equalTo(42)
        }
        
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.top.equalTo(segmentControl.snp.bottom)
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        headerView.setRoundingCorners([.topLeft, .topRight], radius: 20)
    }
    
    @objc private func didTapBlank() {
        dismiss(animated: true)
    }
}


extension ShowPresettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let model = dataArray[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowPresettingCellID, for: indexPath) as! ShowPresettingCell
        cell.selectionStyle = .none
        cell.setTitle(model.title, desc: model.desc, selected: model.isSelected)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        selectedModel = dataArray[indexPath.row]
        tableView.reloadData()
        didSelectedIndex?(indexPath.row)
//        dismiss(animated: true)
    }
}
