//
//  ShowVideoSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit
import JXCategoryView

private let SwitchCellID = "SwitchCellID"
private let SegmentCellID = "SegmentCellID"
private let SliderCellID = "SliderCellID"
private let LabelCellID = "LabelCellID"

class ShowVideoSettingVC: UIViewController {
    
    var dataArray = [ShowSettingModel]()
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = 47
        tableView.registerCell(ShowSettingSwitchCell.self, forCellReuseIdentifier: SwitchCellID)
        return tableView
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        setUpData()
    }
    
    private func setUpUI(){
        return
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func setUpData(){
        let model = ShowSettingSwitchModel(title: "哈哈", isOn: true) { isOn in
            ToastView.show(text: "点击了开关的\(isOn)")
        } clickDetailButonAction: {
            ToastView.show(text: "点击了哈哈哈详情")
        }

        dataArray = [model,model,model,model]
    }
}

extension ShowVideoSettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let data = dataArray[indexPath.row]
        var cell: UITableViewCell!
        if data is ShowSettingSwitchModel {
            let model = data as! ShowSettingSwitchModel
            let cell = tableView.dequeueReusableCell(withIdentifier: SwitchCellID, for: indexPath) as! ShowSettingSwitchCell
            cell.setTitle(model.title, isOn: model.isOn, valueChangedAction: model.valueChangedAction, detailButtonAction: model.clickDetailButonAction)
            return cell
        }else {
            cell = UITableViewCell()
        }
        return cell
    }
}


extension ShowVideoSettingVC: JXCategoryListContentViewDelegate {
    func listView() -> UIView! {
        return view
    }
    
}
