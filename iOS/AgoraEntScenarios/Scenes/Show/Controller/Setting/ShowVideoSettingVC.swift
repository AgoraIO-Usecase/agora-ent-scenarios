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
    
    var dataArray = [ShowSettingKey]()
    var settingManager: ShowSettingManager!
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = 47
        tableView.registerCell(ShowSettingSwitchCell.self, forCellReuseIdentifier: SwitchCellID)
        tableView.registerCell(ShowSettingSegmentCell.self, forCellReuseIdentifier: SegmentCellID)
        tableView.registerCell(ShowSettingSliderCell.self, forCellReuseIdentifier: SliderCellID)
        tableView.registerCell(ShowSettingLabelCell.self, forCellReuseIdentifier: LabelCellID)
        return tableView
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    private func setUpUI(){
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
}

extension ShowVideoSettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let data = dataArray[indexPath.row]
        var cell: UITableViewCell!
        if data.type() == .aSwitch {
            let cell = tableView.dequeueReusableCell(withIdentifier: SwitchCellID, for: indexPath) as! ShowSettingSwitchCell
            cell.setTitle(data.title(), isOn: data.boolValue()) { isOn in
                data.writeValue(isOn)
                self.settingManager.updateSettingForkey(data)
            } detailButtonAction: {
                
            }
            return cell
        }else if data.type() == .segment {
            let cell = tableView.dequeueReusableCell(withIdentifier: SegmentCellID, for: indexPath) as! ShowSettingSegmentCell
            
            cell.setTitle(data.title(), items: data.items(), defaultSelectIndex: data.intValue()) { index in
                data.writeValue(index)
                self.settingManager.updateSettingForkey(data)
            }
            return cell
        }else if data.type() == .slider {
            let cell = tableView.dequeueReusableCell(withIdentifier: SliderCellID, for: indexPath) as! ShowSettingSliderCell
            cell.setTitle(data.title(), value: data.floatValue(), minValue: 100, maxValue: 1000) { value in
                
            } sliderValueChangedAction: { value in
                data.writeValue(value)
                self.settingManager.updateSettingForkey(data)
            }

            return cell
        }else if data.type() == .label {
            let cell = tableView.dequeueReusableCell(withIdentifier: LabelCellID, for: indexPath) as! ShowSettingLabelCell
            let index = data.intValue()
            let value = data.items()[index]
            cell.setTitle(data.title(), value: value) {
                let vc = ShowSettingActionSheetVC()
                vc.title = data.title()
                vc.dataArray = data.items()
                vc.didSelectedIndex = { index in
                    data.writeValue(index)
                    self.settingManager.updateSettingForkey(data)
                    tableView.reloadData()
                }
                self.present(vc, animated: true)
            }
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
