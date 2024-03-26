//
//  DebugSettingViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2024/3/25.
//

import UIKit
import AgoraRtcKit
import AgoraCommon

struct DebugSettingInfo {
    var title: String = ""
    var details: [String] = []
    var defaultSelectedIdxs: [Int] = []
    func selectedIdx() -> Int {
        let idx = UserDefaults.standard.value(forKey: title) as? Int ?? (defaultSelectedIdxs.first ?? 0)
        return idx
    }
    
    func selectedValue() -> Int {
        return defaultSelectedIdxs[selectedIdx()]
    }
}

let kSelectedDumpParam = ["{\"rtc.debug.enable\": true}",
                      "{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"]
let kUnselectedDumpParam = ["{\"rtc.debug.enable\": false}"]

private let kCellWithIdentifier = "DebugSettingCellIdentifier"

let settingInfoList: [DebugSettingInfo] = [
    DebugSettingInfo(title: "audio_dump".pure1v1Localization(),
                     details: ["audio_dump_close".pure1v1Localization(), "audio_dump_open".pure1v1Localization()],
                     defaultSelectedIdxs: [0, 1])
]

class DebugSettingViewController: UITableViewController {
    private var engine: AgoraRtcEngineKit
    
    required init(engine: AgoraRtcEngineKit) {
        self.engine = engine
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        tableView.delegate = self
        tableView.dataSource = self
        
        let tableHeaderView = UIView(frame: CGRect(x: 0, y: 0, width: 100, height: 60))
        tableView.tableHeaderView = tableHeaderView
        
        let btn = UIButton(type: .close)
        btn.frame = CGRect(x: 10, y: 0, width: 40, height: 40)
        btn.addTarget(self, action: #selector(onBackAction), for: .touchUpInside)
        tableHeaderView.addSubview(btn)
    }
    
    @objc func onBackAction() {
        navigationController?.popViewController(animated: true)
    }
}

extension DebugSettingViewController {
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return settingInfoList.count
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: kCellWithIdentifier)
        if cell == nil {
            cell = UITableViewCell(style: .value1, reuseIdentifier: kCellWithIdentifier)
        }
        let info = settingInfoList[indexPath.row]
        cell?.backgroundColor = .white
        cell?.textLabel?.textColor = .black
        cell?.textLabel?.text = info.title
        cell?.detailTextLabel?.textColor = .gray
        cell?.detailTextLabel?.text = info.details[info.selectedIdx()]
        return cell!
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let info = settingInfoList[indexPath.row]
        let selectedIdx = (info.selectedIdx() + 1) % info.details.count
        UserDefaults.standard.setValue(selectedIdx, forKey: info.title)
        
        AppContext.shared.resetDebugConfig(engine: engine)
        tableView.reloadData()
    }
}
