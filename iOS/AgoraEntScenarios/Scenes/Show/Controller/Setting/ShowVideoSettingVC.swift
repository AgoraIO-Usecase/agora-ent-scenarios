//
//  ShowVideoSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

private let SwitchCellID = "SwitchCellID"
private let SegmentCellID = "SegmentCellID"
private let SliderCellID = "SliderCellID"
private let LabelCellID = "LabelCellID"

class ShowVideoSettingVC: UIViewController {
    
    private let transDelegate = ShowPresentTransitioningDelegate()
    
    var dataArray = [ShowSettingKey]()
    var settingManager: ShowAgoraKitManager!
    var willChangeSettingParams: ((_ key: ShowSettingKey, _ value: Any)->Bool)?
    
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
    
    func reloadData(){
        tableView.reloadData()
    }
}

extension ShowVideoSettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let data = dataArray[indexPath.row]
        var cell: UITableViewCell!
        if data.type == .aSwitch {
            let cell = tableView.dequeueReusableCell(withIdentifier: SwitchCellID, for: indexPath) as! ShowSettingSwitchCell
            cell.setTitle(data.title, isOn: data.boolValue) {[weak self] isOn in
                self?.changeValue(isOn, forSettingKey: data)
            } detailButtonAction: {[weak self] in
                self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
            }
            return cell
        }else if data.type == .segment {
            let cell = tableView.dequeueReusableCell(withIdentifier: SegmentCellID, for: indexPath) as! ShowSettingSegmentCell
            
            cell.setTitle(data.title, items: data.items, defaultSelectIndex: data.intValue) {[weak self] index in
                self?.changeValue(index, forSettingKey: data)
            }
            return cell
        }else if data.type == .slider {
            let cell = tableView.dequeueReusableCell(withIdentifier: SliderCellID, for: indexPath) as! ShowSettingSliderCell
            cell.setTitle(data.title, value: data.floatValue, minValue: data.sliderValueScope.0, maxValue: data.sliderValueScope.1) {value in
                
            } sliderValueChangedAction: {[weak self] value in
                self?.changeValue(value, forSettingKey: data)
            }

            return cell
        }else if data.type == .label {
            let cell = tableView.dequeueReusableCell(withIdentifier: LabelCellID, for: indexPath) as! ShowSettingLabelCell
            let index = data.intValue
            let value = data.items[index]
            cell.setTitle(data.title, value: value) { [weak self] in
                let vc = ShowSettingActionSheetVC()
                vc.transitioningDelegate = self?.transDelegate
                vc.title = data.title
                vc.defaultSelectedIndex = data.intValue
                vc.dataArray = data.items
                vc.didSelectedIndex = {[weak self] index in
                    data.writeValue(index)
                    self?.settingManager.updateSettingForkey(data)
                    tableView.reloadData()
                }
                self?.present(vc, animated: true, completion: {
                    vc.showBgView()
                })
            }
            return cell
        }else {
            cell = UITableViewCell()
        }
        return cell
    }
}

extension ShowVideoSettingVC {
    func changeValue(_ value: Any, forSettingKey key: ShowSettingKey) {
        if let willChange = willChangeSettingParams, willChange(key,value) == true {
            key.writeValue(value)
            settingManager.updateSettingForkey(key)
        }
        tableView.reloadData()
    }
}
