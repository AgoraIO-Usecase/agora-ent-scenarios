//
//  ShowVideoSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit
import ZSwiftBaseLib

class ShowVideoSettingVC: UIViewController {
    
    private let kSwitchCell = "ShowSettingSwitchCell"
    private let kSegmentCell = "ShowSettingSegmentCell"
    private let kSliderCell = "ShowSettingSliderCell"
    private let kLabelCell = "ShowSettingLabelCell"
    private let kBitrateCell = "ShowSettingBitrateCell"

    private let transDelegate = ShowPresentTransitioningDelegate()
    
    var dataArray = [ShowSettingKey]()
    
    var musicManager: ShowMusicPresenter!
    
    var currentChannelId: String?
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = 47
        tableView.separatorStyle = .none
        tableView.registerCell(ShowSettingBitrateCell.self, forCellReuseIdentifier: kBitrateCell)
        tableView.registerCell(ShowSettingSwitchCell.self, forCellReuseIdentifier: kSwitchCell)
        tableView.registerCell(ShowSettingSegmentCell.self, forCellReuseIdentifier: kSegmentCell)
        tableView.registerCell(ShowSettingSliderCell.self, forCellReuseIdentifier: kSliderCell)
        tableView.registerCell(ShowSettingLabelCell.self, forCellReuseIdentifier: kLabelCell)
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
    
    private func barrierValueChange(complete: (() -> Void)?) {
        if ShowAgoraKitManager.shared.rtcParam.suggested {
            ShowAgoraKitManager.shared.rtcParam.suggested = false
            let alert = UIAlertController(
                title: "show_presetting_alert_will_change_value_title".show_localized,
                message: "show_presetting_alert_will_change_value_message".show_localized,
                preferredStyle: .alert)
            let submit = UIAlertAction(
                title: "show_alert_confirm_btn_title".show_localized,
                style: .default) { _ in
                    complete?()
                    self.tableView.reloadData()
                }
            let cancel = UIAlertAction(
                title: "show_alert_cancel_btn_title".show_localized,
                style: .cancel) { _ in
                    self.tableView.reloadData()
                }
            alert.addAction(submit)
            alert.addAction(cancel)
            present(alert, animated: true, completion: nil)
        } else {
            complete?()
            tableView.reloadData()
        }
    }
    
    private func onValueChanged(_ value: Any, forSettingKey key: ShowSettingKey) {
        barrierValueChange {
            key.writeValue(value)
            ShowAgoraKitManager.shared.updateSettingForkey(key, currentChannelId: self.currentChannelId)
        }
    }
}
// MARK: - TableView Call Back
extension ShowVideoSettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let data = dataArray[indexPath.row]
        if data.type == .aSwitch {
            let cell = tableView.dequeueReusableCell(withIdentifier: kSwitchCell, for: indexPath) as! ShowSettingSwitchCell
            var enable = true
            if data == .H265 || data == .PVC {
                enable = false
            }
            cell.setTitle(data.title,enable:enable, isOn: data.boolValue) {[weak self] isOn in
                self?.onValueChanged(isOn, forSettingKey: data)
            } detailButtonAction: {[weak self] in
                self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
            }
            return cell
        }else if data.type == .segment {
            let cell = tableView.dequeueReusableCell(withIdentifier: kSegmentCell, for: indexPath) as! ShowSettingSegmentCell
            
            cell.setTitle(data.title, items: data.items, defaultSelectIndex: data.intValue) {[weak self] index in
                self?.onValueChanged(index, forSettingKey: data)
            }
            return cell
        } else if data.type == .label {
            let cell = tableView.dequeueReusableCell(withIdentifier: kLabelCell, for: indexPath) as! ShowSettingLabelCell
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
                let cell = tableView.dequeueReusableCell(withIdentifier: kBitrateCell, for: indexPath) as! ShowSettingBitrateCell
                cell.setTitle(data.title, value: data.floatValue, minValue: 200, maxValue: 4000)
                cell.delegate = self
                cell.clickDetailButonAction = { [weak self] in
                    self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
                }
                return cell
            } else if data == .musicVolume {
                let cell = tableView.dequeueReusableCell(withIdentifier: kSliderCell, for: indexPath) as! ShowSettingSliderCell
                let value = ShowAgoraKitManager.shared.rtcParam.musicVolume
                cell.setTitle(data.title, value: Float(value), minValue: 0, maxValue: 100)
                cell.delegate = self
                cell.indexPath = indexPath
                return cell
            } else if data == .recordingSignalVolume {
                let cell = tableView.dequeueReusableCell(withIdentifier: kSliderCell, for: indexPath) as! ShowSettingSliderCell
                let value = ShowAgoraKitManager.shared.rtcParam.recordingSignalVolume
                cell.setTitle(data.title, value: Float(value), minValue: 0, maxValue: 100)
                cell.delegate = self
                cell.indexPath = indexPath
                return cell
            } else if data == .PVC {
                let cell = tableView.dequeueReusableCell(withIdentifier: kSwitchCell, for: indexPath) as! ShowSettingSwitchCell
                let value = ShowAgoraKitManager.shared.rtcParam.pvc
                cell.setTitle(data.title, enable:false, isOn: value) {[weak self] isOn in
                    self?.barrierValueChange(complete: {
                        ShowAgoraKitManager.shared.rtcParam.pvc = isOn
                        ShowAgoraKitManager.shared.updateSettingForkey(.PVC, currentChannelId: self?.currentChannelId)
                    })
                } detailButtonAction: {[weak self] in
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

// MARK: - ShowSettingSliderCellDelegate
extension ShowVideoSettingVC: ShowSettingBitrateCellDelegate {
    
    func onAutoBitRateChanged(isOn: Bool) {
        if (isOn) {
            self.onValueChanged(0, forSettingKey: ShowSettingKey.videoBitRate)
        } else {
            // 根据机型设置码率
            switch ShowAgoraKitManager.shared.deviceLevel {
            case .low:
                self.onValueChanged(1461, forSettingKey: ShowSettingKey.videoBitRate)
            case .medium:
                self.onValueChanged(1800, forSettingKey: ShowSettingKey.videoBitRate)
            case .high:
                self.onValueChanged(2099, forSettingKey: ShowSettingKey.videoBitRate)
            }
        }
    }
    
    func onBitRateValueChanged(value: Float) {
        onValueChanged(value, forSettingKey: ShowSettingKey.videoBitRate)
    }
}
// MARK: - ShowSettingSliderCellDelegate
extension ShowVideoSettingVC: ShowSettingSliderCellDelegate {
    
    func onCellSliderValueChanged(value: Float, at index: IndexPath) {
        let key = dataArray[index.row]
        barrierValueChange {
            if key == .musicVolume {
                let v = Int(value)
                ShowAgoraKitManager.shared.rtcParam.musicVolume = v
                self.musicManager.setMusicVolume(v)
            } else if key == .recordingSignalVolume {
                ShowAgoraKitManager.shared.rtcParam.recordingSignalVolume = Int(value)
                ShowAgoraKitManager.shared.updateSettingForkey(key, currentChannelId: self.currentChannelId)
            }
        }
    }
}
