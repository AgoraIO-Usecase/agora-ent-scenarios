//
//  AboutAgoraEntertainmentViewController.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/8.
//

import UIKit
import ZSwiftBaseLib
import AgoraRtcKit
import AgoraCommon
@objcMembers final class AboutAgoraEntertainmentViewController: VLBaseViewController {
    
    var infos = [
        ["contents":[["title": NSLocalizedString("app_about_customer_service", comment: ""),
                      "detail": "400-632-6626"],
                     ["title": NSLocalizedString("app_about_official_website", comment: ""),
                      "detail":"https://www.shengwang.cn"]],
         "sectionName": ""],
        ["contents": [
//            ["title": NSLocalizedString("app_voice_chat", comment: ""), "detail":"YL-4.1.200"],
//                      ["title": NSLocalizedString("app_about_chat_room_spatial", comment: ""), "detail":"YLKJ-4.1.200"],
//                      ["title": NSLocalizedString("app_about_karaoke", comment: ""),"detail":"KTV-4.3.0"],
//                      ["title": NSLocalizedString("app_about_hiSong", comment: ""),"detail":"QC-4.3.0"],
//                      ["title": NSLocalizedString("app_about_continuesinging", comment: ""),"detail":"JC-4.3.0"],
//                      ["title": NSLocalizedString("app_about_cantata", comment: ""),"detail":"DHC-4.3.0"],
//                      ["title": NSLocalizedString("app_about_show", comment: ""), "detail":"ZB-4.2.1"],
//                      ["title": NSLocalizedString("app_about_1v1", comment: ""), "detail":"SMF-4.2.1"],
                      ["title": NSLocalizedString("app_about_game_joy", comment: ""), "detail":"DMWF-4.10.1"],
                      ["title": NSLocalizedString("app_about_game_interactive_joy", comment: ""), "detail":"XXWF-4.10.2"]

        ],
         "sectionName": NSLocalizedString("app_about_scene_version", comment: "")]
    ]
    
    let tableHeader = AboutAgoraHeader(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 220),
                                       name: NSLocalizedString("app_about_name", comment: ""),
                                       versionText: NSLocalizedString("app_about_version", comment: "")+": "+"20240511-"+UIDevice.current.appVersion+"-\(AgoraRtcEngineKit.getSdkVersion())")
    
    lazy var infoList: UITableView = {
        UITableView(frame: .zero, style: .grouped)
            .registerCell(ContactInfoCell.self, forCellReuseIdentifier: "ContactInfoCell")
            .registerCell(UITableViewCell.self, forCellReuseIdentifier: "SceneVersionCell")
            .delegate(self)
            .dataSource(self)
            .tableFooterView(UIView())
            .tableHeaderView(tableHeader)
            .backgroundColor(.white)
            .separatorStyle(.none)
            .estimatedSectionFooterHeight(0)
            .estimatedSectionHeaderHeight(0)
            .estimatedRowHeight(0)
    }()
    
    lazy var debugModeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = .blue
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.setTitle(NSLocalizedString("app_debug_open", comment: ""), for: .normal)
        button.addTargetFor(self, action: #selector(onClickCloseDebugMode(_:)), for: .touchUpInside)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        view.backgroundColor = .white
        setNaviTitleName(NSLocalizedString("app_about_app", comment: ""))
        setBackBtn()
        hiddenBackgroundImage()
        
        tableHeader.delegate = self
        view.addSubview(infoList)
        infoList.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: 10, right: 0)
        
        debugModeButton.isHidden = !AppContext.shared.isDebugMode
        view.addSubview(debugModeButton)
        createConstrains()
    }
    
    private func createConstrains() {
        infoList.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
            make.top.equalToSuperview().offset(ZNavgationHeight)
        }
        debugModeButton.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(44)
        }
    }
    
    @objc func onClickCloseDebugMode(_ sender: UIButton){

    }
    
    @objc func onClickCloseDebugMode1(_ sender: UIButton) {
//        let alert = UIAlertController(title: NSLocalizedString("app_about_app", comment: "app_exit_debug"), message: NSLocalizedString("app_exit_debug_tip", comment: ""), preferredStyle: .alert)
//        let submit = UIAlertAction(title: NSLocalizedString("confirm", comment: ""), style: .default, handler: { action in
//            AppContext.shared.isDebugMode = false
//            self.debugModeButton.isHidden = true
//        })
//        let cancel = UIAlertAction(title: NSLocalizedString("cancel", comment: ""), style: .default)
//        alert.addAction(submit)
//        alert.addAction(cancel)
//        present(alert, animated: true)
        let alert = UIAlertController(title: NSLocalizedString("app_about_app", comment: "app_exit_debug"), message: NSLocalizedString("app_exit_debug_tip", comment: ""), preferredStyle: .alert)

        // 创建一个包含 segment 和 button 的自定义视图
        let customView = UIView(frame: CGRect(x: 0, y: 0, width: 300, height: 50))

        // 创建 segment 控件
        let segment = UISegmentedControl(items: ["Option 1", "Option 2"])
        segment.frame = CGRect(x: 10, y: 5, width: 150, height: 40)
        customView.addSubview(segment)

        // 创建 button 控件
        let button = UIButton(type: .system)
        button.setTitle("Button", for: .normal)
        button.frame = CGRect(x: 160, y: 5, width: 130, height: 40)
        customView.addSubview(button)

        // 添加自定义视图到 alert 上
        alert.view.addSubview(customView)

        let submit = UIAlertAction(title: NSLocalizedString("confirm", comment: ""), style: .default, handler: { action in
            AppContext.shared.isDebugMode = false
            self.debugModeButton.isHidden = true
        })
        let cancel = UIAlertAction(title: NSLocalizedString("cancel", comment: ""), style: .default)
        alert.addAction(submit)
        alert.addAction(cancel)

        present(alert, animated: true)

    }
}
// MARK: - AboutAgoraHeaderDelegate
extension AboutAgoraEntertainmentViewController: AboutAgoraHeaderDelegate {
    
    func enterDebugMode() {
        AppContext.shared.isDebugMode = true
        debugModeButton.isHidden = false
    }
}
// MARK: - Table View
extension AboutAgoraEntertainmentViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if let name = self.infos[safe: section]?["sectionName"] as? String,name == NSLocalizedString("app_about_scene_version", comment: "") {
            return 44
        }
        return 0
    }
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        0.1
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if indexPath.section == 1 {
            return 64
        }
        return 72
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if let name = self.infos[safe: section]?["sectionName"] as? String,name == NSLocalizedString("app_about_scene_version", comment: "") {
            return UIView {
                UIView(frame: CGRect(x: 0, y: 0, width: self.infoList.frame.width, height: 44)).backgroundColor(.white)
                UILabel(frame: CGRect(x: 20, y: 12, width: self.infoList.frame.width-40, height: 20)).font(.systemFont(ofSize: 13, weight: .regular)).textColor(UIColor(0x6C7192)).text(name)
            }
        }
        return nil
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        self.infos.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        guard let contents = self.infos[safe: section]?["contents"] as? [[String:String]] else { return 0 }
        return contents.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "ContactInfoCell")
        guard let contents = self.infos[safe: indexPath.section]?["contents"] as? [[String:String]] else { return UITableViewCell() }
        if let name = self.infos[safe: indexPath.section]?["sectionName"] as? String,name == NSLocalizedString("SceneVersion", comment: "") {
            cell = tableView.dequeueReusableCell(withIdentifier: "SceneVersionCell")
            return self.constructVersionCell(cell: cell, contents: contents, indexPath: indexPath) ?? SceneVersionCell()
        } else {
            return self.constructInfoCell(cell: cell, contents: contents, indexPath: indexPath) ?? ContactInfoCell()
        }
    }
    
    private func constructVersionCell(cell: UITableViewCell?,contents: [[String:String]],indexPath: IndexPath) -> SceneVersionCell? {
        var versionCell = cell as? SceneVersionCell
        if versionCell == nil {
            versionCell = SceneVersionCell(style: .value2, reuseIdentifier: "SceneVersionCell")
        }
        versionCell?.selectionStyle = .none
        versionCell?.accessoryType = .none
        versionCell?.refreshInfo(info: contents[indexPath.row])
        return versionCell
    }
    
    private func constructInfoCell(cell: UITableViewCell?,contents: [[String:String]],indexPath: IndexPath) -> ContactInfoCell? {
        var infoCell = cell as? ContactInfoCell
        if infoCell == nil {
            infoCell = ContactInfoCell(style: .default, reuseIdentifier: "ContactInfoCell")
        }
        infoCell?.selectionStyle = .none
        infoCell?.accessoryType = .disclosureIndicator
        infoCell?.refreshInfo(info: contents[indexPath.row])
        return infoCell
    }
    
}
