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
        tableView.registerCell(ShowSettingSegmentCell.self, forCellReuseIdentifier: SegmentCellID)
        tableView.registerCell(ShowSettingSliderCell.self, forCellReuseIdentifier: SliderCellID)
        tableView.registerCell(ShowSettingLabelCell.self, forCellReuseIdentifier: LabelCellID)
        return tableView
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        setUpData()
    }
    
    private func setUpUI(){
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func setUpData(){
        /*
        let model = ShowSettingSwitchModel(title: "哈哈", isOn: true) { isOn in
            ToastView.show(text: "点击了开关的\(isOn)")
            
        } clickDetailButonAction: {
            ToastView.show(text: "点击了哈哈哈详情")
        }
        
        let model2 = ShowSettingSwitchModel(title: "哈哈", isOn: true) { isOn in
            ToastView.show(text: "点击了开关的\(isOn)")
            
        } clickDetailButonAction: {
            ToastView.show(text: "点击了哈哈哈详情")
        }
        
        let model3 = ShowSettingSwitchModel(title: "哈哈", isOn: true) { isOn in
            ToastView.show(text: "点击了开关的\(isOn)")
            
        } clickDetailButonAction: {
            ToastView.show(text: "点击了哈哈哈详情")
        }
        
        let model4 = ShowSettingSwitchModel(title: "哈哈", isOn: true) { isOn in
            ToastView.show(text: "点击了开关的\(isOn)")
            
        } clickDetailButonAction: {
            ToastView.show(text: "点击了哈哈哈详情")
        }
        
        let segmentModel = ShowSettingSegmentModel(title: "人像增强", selectedIndex: 0, items: ["无","低","中","高"]) { index in
            
        }
        
        let segmentModel2 = ShowSettingSegmentModel(title: "延迟", selectedIndex: 2, items: ["低","中","高"]) { index in
            
        }
        
        let sliderModel = ShowSettingSliderModel(title: "码率", value: 400, minValue: 200, maxValue: 1000) { value in
            
        }
        
        let sliderModel2 = ShowSettingSliderModel(title: "码率2", value: 400, minValue: 200, maxValue: 1000) { value in
            
        }
        
        let labelModel = ShowSettingLabelModel(title: "分辨率", value: "360 * 640") { index in
           
        }
        
        let labelModel2 = ShowSettingLabelModel(title: "帧率", value: "30 fps") { index in
            
        }
        
        dataArray = [model,model2,model4,model3,segmentModel, segmentModel2,sliderModel,sliderModel2, labelModel, labelModel2]
         */
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
            cell.setTitle(model.title, isOn: model.isOn) { isOn in
                model.isOn = isOn
                model.valueChangedAction?(isOn)
            } detailButtonAction: {
                model.clickDetailButonAction?()
            }
            return cell
        }else if data is ShowSettingSegmentModel {
            let model = data as! ShowSettingSegmentModel
            let cell = tableView.dequeueReusableCell(withIdentifier: SegmentCellID, for: indexPath) as! ShowSettingSegmentCell
            cell.setTitle(model.title, items: model.items, defaultSelectIndex: model.selectedIndex) { index in
                model.selectedIndex = index
                model.selectedIndexChangedAction?(index)
            }
            return cell
        }else if data is ShowSettingSliderModel {
            let model = data as! ShowSettingSliderModel
            let cell = tableView.dequeueReusableCell(withIdentifier: SliderCellID, for: indexPath) as! ShowSettingSliderCell
            cell.setTitle(model.title, value: model.value, minValue: model.minValue, maxValue: model.maxValue) { value in
                model.value = value
            } sliderValueChangedAction: { value in
                model.value = value
                model.sliderValueChangedAction?(value)
            }

            return cell
        }else if data is ShowSettingLabelModel {
            let model = data as! ShowSettingLabelModel
            let cell = tableView.dequeueReusableCell(withIdentifier: LabelCellID, for: indexPath) as! ShowSettingLabelCell
            cell.setTitle(model.title, value: model.value) {
                /*
                let vc = ShowSettingActionSheetVC()
                let dataArray = ["320x240","320x2401","320x2402","320x2403","320x2405","320x2406","320x2407"]
                vc.title = "分辨率"
                vc.dataArray = dataArray
                vc.didSelectedIndex = { index in
                    let value = dataArray[index]
                    model.value = value
                    model.cellDidSelectedAction?(index)
                    tableView.reloadData()
                }
                self.present(vc, animated: true)
                 */
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
