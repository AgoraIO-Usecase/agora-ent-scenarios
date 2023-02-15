//
//  ShowDebugSettingVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/10.
//

import UIKit

private let SwitchCellID = "SwitchCellID"
private let SliderCellID = "SliderCellID"
private let LabelCellID = "LabelCellID"
private let Debug1TFCellID = "Debug1TFCellID"
private let Debug2TFCellID = "Debug2TFCellID"

class ShowDebugSettingVC: UIViewController {
    
    var isBroadcastor = true // 频道外
    var settingManager: ShowAgoraKitManager?
    
    private let transDelegate = ShowPresentTransitioningDelegate()
    private lazy var dataArray: [Any] = {
        isBroadcastor ? createBroadcastorDataArray() : createAudienceDataArray()
    }()
    
    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = 47
        tableView.separatorStyle = .none
        tableView.registerCell(ShowSettingSwitchCell.self, forCellReuseIdentifier: SwitchCellID)
        tableView.registerCell(ShowSettingSliderCell.self, forCellReuseIdentifier: SliderCellID)
        tableView.registerCell(ShowSettingLabelCell.self, forCellReuseIdentifier: LabelCellID)
        tableView.registerCell(ShowDebugSetting1TFCell.self, forCellReuseIdentifier: Debug1TFCellID)
        tableView.registerCell(ShowDebugSetting2TFCell.self, forCellReuseIdentifier: Debug2TFCellID)
        return tableView
    }()
    
    private lazy var coverView: UIView = {
        let view = UIView()
        let tap = UITapGestureRecognizer(target: self, action: #selector(didTapedCoverView))
        view.addGestureRecognizer(tap)
        return view
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        configCustomNaviBar()
        setUpUI()
        addObserver()
    }
    
    private func configCustomNaviBar(){
        // 标题
        naviBar.title = "开发者模式设置"
        naviBar.backgroundColor = .white
        view.addSubview(naviBar)
        
//        let saveButtonItem = ShowBarButtonItem(title: "show_advanced_setting_presetting_save".show_localized, target: self, action: #selector(didClickSaveButton))
//        naviBar.rightItems = [saveButtonItem]
    }
    
    @objc private func didClickSaveButton() {
        navigationController?.popViewController(animated: true)
    }
    
    private func setUpUI(){
        view.backgroundColor = .white
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
            make.top.equalTo(naviBar.snp.bottom)
        }
    }
    
    private func createBroadcastorDataArray() -> [Any] {
        guard let settingManager = settingManager else {
            return createAudienceDataArray()
        }
        return [
            settingManager.debug1TFModelForKey(.captureFrameRate),
            settingManager.debug2TFModelForKey(.captureVideoSize),
            settingManager.debug1TFModelForKey(.encodeFrameRate),
            settingManager.debug2TFModelForKey(.encodeVideoSize),
            settingManager.debug1TFModelForKey(.bitRate),
            ShowSettingKey.debugPVC,
            ShowSettingKey.focusFace,  // 人脸对焦
            settingManager.debug2TFModelForKey(.exposureRange),// 曝光区域
            settingManager.debug2TFModelForKey(.colorSpace), // 颜色空间
            ShowSettingKey.encode,
            ShowSettingKey.codeCType,
            ShowSettingKey.mirror,
            ShowSettingKey.renderMode,
            ShowSettingKey.colorEnhance,
            ShowSettingKey.lowlightEnhance,
            ShowSettingKey.videoDenoiser,
        ]
    }
    
    private func createAudienceDataArray() -> [Any] {
        [
            ShowSettingKey.debugSR,
            ShowSettingKey.debugSrType
        ]
    }
}

extension ShowDebugSettingVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let model = dataArray[indexPath.row]
        if let tf1Model = model as? ShowDebug1TFModel {
            let cell = tableView.dequeueReusableCell(withIdentifier: Debug1TFCellID, for: indexPath) as! ShowDebugSetting1TFCell
            cell.setTitle(tf1Model.title, value: tf1Model.tfText, unit: tf1Model.unitText) {[weak self] textField in
                tf1Model.tfText = textField.text
                self?.settingManager?.updateDebugProfileFor1TFMode(tf1Model)
            } beginEditing: {
                tableView.scrollToRow(at: indexPath, at: .top, animated: true)
            }
            return cell
        }
        
        if let tf2Model = model as? ShowDebug2TFModel {
            let cell = tableView.dequeueReusableCell(withIdentifier: Debug2TFCellID, for: indexPath) as! ShowDebugSetting2TFCell
            cell.setTitle(tf2Model.title, value1: tf2Model.tf1Text, value2: tf2Model.tf2Text, separator: tf2Model.separatorText) {[weak self] textField in
                tf2Model.tf1Text = textField.text
                self?.settingManager?.updateDebugProfileFor2TFModel(tf2Model)
            } tf2DidEndEditing: { [weak self] textField in
                tf2Model.tf2Text = textField.text
                self?.settingManager?.updateDebugProfileFor2TFModel(tf2Model)
            } beginEditing: {
                tableView.scrollToRow(at: indexPath, at: .top, animated: true)
            }
            return cell
        }
        
        let data:ShowSettingKey = dataArray[indexPath.row] as! ShowSettingKey
        var cell: UITableViewCell!
        if data.type == .aSwitch {
            let cell = tableView.dequeueReusableCell(withIdentifier: SwitchCellID, for: indexPath) as! ShowSettingSwitchCell
            cell.setTitle(data.title,enable:true, isOn: data.boolValue) {[weak self] isOn in
                self?.changeValue(isOn, forSettingKey: data)
            } detailButtonAction: {[weak self] in
                self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
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
                    self?.settingManager?.updateSettingForkey(data)
                    tableView.reloadData()
                }
                self?.present(vc, animated: true, completion: {
                    vc.showBgView()
                })
            } detailButtonAction: {[weak self] in
                self?.showAlert(title: data.title, message: data.tips, confirmTitle: "OK", cancelTitle: nil)
            }

            return cell
        }else {
            cell = UITableViewCell()
        }
        return cell
    }
}

extension ShowDebugSettingVC {
    func changeValue(_ value: Any, forSettingKey key: ShowSettingKey) {
        key.writeValue(value)
        settingManager?.updateSettingForkey(key)
        tableView.reloadData()
    }
}

extension ShowDebugSettingVC {
    
    private func addObserver(){
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillShowNotification, object: nil, queue: nil) { [weak self] notify in
            guard let self = self else {return}
            guard let keyboardRect = (notify.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
            guard let duration = notify.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else { return }
            let keyboradHeight = keyboardRect.size.height
            UIView.animate(withDuration: duration) {
                self.view.layoutIfNeeded()
            }
            
            self.view.addSubview(self.coverView)
            self.coverView.snp.makeConstraints { make in
                make.left.right.top.equalToSuperview()
                make.bottom.equalTo(-keyboradHeight)
            }
        }
        
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillHideNotification, object: nil, queue: nil) {[weak self] notify in
            self?.coverView.removeFromSuperview()
        }
    }
    
    @objc private func didTapedCoverView(){
        view.endEditing(true)
    }
}
