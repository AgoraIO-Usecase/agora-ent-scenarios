//
//  ShowVideoSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit
import ZSwiftBaseLib

private let SwitchCellID = "SwitchCellID"
private let SegmentCellID = "SegmentCellID"
private let SliderCellID = "SliderCellID"
private let LabelCellID = "LabelCellID"

class ShowVideoSettingVC: UIViewController {
    
    private let transDelegate = ShowPresentTransitioningDelegate()
    
    var dataArray = [ShowSettingKey]()
    
    var musicManager: ShowMusicManager!
    
    var currentChannelId: String?
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = 47
        tableView.separatorStyle = .none
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
        if data.type == .aSwitch {
            let cell = tableView.dequeueReusableCell(withIdentifier: SwitchCellID, for: indexPath) as! ShowSettingSwitchCell
            cell.setTitle(data.title, enable: true, isOn: data.boolValue) {[weak self] isOn in
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
        } else if data.type == .label {
            let cell = tableView.dequeueReusableCell(withIdentifier: LabelCellID, for: indexPath) as! ShowSettingLabelCell
            let index = data.intValue % data.items.count
            let value = data.items[index]
            cell.setTitle(data.title, value: value) { [weak self] in
                let vc = ShowSettingActionSheetVC()
                vc.transitioningDelegate = self?.transDelegate
                vc.title = data.title
                vc.defaultSelectedIndex = data.intValue
                vc.dataArray = data.items
                vc.didSelectedIndex = { index in
                    data.writeValue(index)
                    ShowAgoraKitManager.shared.updateSettingForkey(data)
                    tableView.reloadData()
                }
                self?.present(vc, animated: true, completion: {
                    vc.showBgView()
                })
            } detailButtonAction: {[weak self] in
                self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
            }
            return cell
        } else {
            if data == .videoBitRate {
                let cell = tableView.dequeueReusableCell(withIdentifier: SliderCellID, for: indexPath) as! ShowSettingSliderCell
                cell.setTitle(data.title, value: data.floatValue, minValue: 200, maxValue: 4000)
                cell.delegate = self
                cell.clickDetailButonAction = { [weak self] in
                    self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
                }
                return cell
            }
        }
        return UITableViewCell()
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let data = dataArray[indexPath.row]
        if data == .videoBitRate {
            if ShowSettingKey.videoBitRate.floatValue == 0 {
                return 48
            } else {
                return 100
            }
        } else {
            return 48
        }
    }
}

extension ShowVideoSettingVC {
    func changeValue(_ value: Any, forSettingKey key: ShowSettingKey) {
        if ShowAgoraKitManager.shared.rtcParam.suggested {
            ShowAgoraKitManager.shared.rtcParam.suggested = false
            let alert = UIAlertController(title: "show_presetting_alert_will_change_value_title".show_localized, message: "show_presetting_alert_will_change_value_message".show_localized, preferredStyle: .alert)
            let submit = UIAlertAction(title: "show_alert_confirm_btn_title".show_localized, style: .default) { _ in
                key.writeValue(value)
                self.tableView.reloadData()
            }
            let cancel = UIAlertAction(title: "show_alert_cancel_btn_title".show_localized, style: .cancel) { _ in
                self.tableView.reloadData()
            }
            alert.addAction(submit)
            alert.addAction(cancel)
            present(alert, animated: true, completion: nil)
        } else {
            key.writeValue(value)
            if key == .musincVolume {
                musicManager.setMusicVolume(value as! Float)
            } else {
                ShowAgoraKitManager.shared.updateSettingForkey(key, currentChannelId: currentChannelId)
            }
            tableView.reloadData()
        }
    }
}
// MARK: - ShowSettingSliderCellDelegate
extension ShowVideoSettingVC: ShowSettingSliderCellDelegate {
    
    func onAutoBitRateChanged(isOn: Bool) {
        if (isOn) {
            self.changeValue(0, forSettingKey: ShowSettingKey.videoBitRate)
        } else {
            // 根据机型设置码率
            switch ShowAgoraKitManager.shared.deviceLevel {
            case .low:
                self.changeValue(1461, forSettingKey: ShowSettingKey.videoBitRate)
            case .medium:
                self.changeValue(1800, forSettingKey: ShowSettingKey.videoBitRate)
            case .high:
                self.changeValue(2099, forSettingKey: ShowSettingKey.videoBitRate)
            }
        }
        tableView.reloadData()
    }
    
    func onBitRateValueChanged(value: Float) {
        self.changeValue(value, forSettingKey: ShowSettingKey.videoBitRate)
    }
}
