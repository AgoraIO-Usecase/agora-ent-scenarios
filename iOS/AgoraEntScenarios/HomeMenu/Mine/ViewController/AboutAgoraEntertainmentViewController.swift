//
//  AboutAgoraEntertainmentViewController.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/8.
//

import UIKit
import ZSwiftBaseLib
import AgoraRtcKit

@objcMembers final class AboutAgoraEntertainmentViewController: VRBaseViewController {
    
    var infos = [["contents":[["title":NSLocalizedString("ContactCustomerService", comment: ""),"detail":"400-632-6626"],["title":NSLocalizedString("OfficialWebsiteAddress", comment: ""),"detail":"https://www.shengwang.cn"]],"sectionName":""],["contents":[["title":"语聊房","detail":"YL-2.1.0"],["title":"语聊房-空间音频模式","detail":"YLKJ-2.1.0"],["title":"在线K歌房","detail":"KTV-2.1.0"]],"sectionName":NSLocalizedString("SceneVersion", comment: "")]]
    
    lazy var infoList: UITableView = {
        UITableView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ScreenHeight-ZNavgationHeight), style: .plain).registerCell(ContactInfoCell.self, forCellReuseIdentifier: "ContactInfoCell").registerCell(UITableViewCell.self, forCellReuseIdentifier: "SceneVersionCell").delegate(self).dataSource(self).tableFooterView(UIView()).tableHeaderView(AboutAgoraHeader(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 220), name: NSLocalizedString("AgoraEntertainment", comment: ""), versionText: NSLocalizedString("Version", comment: "")+": "+"20230110-"+UIDevice.current.appVersion+"-\(AgoraRtcEngineKit.getSdkVersion())")).backgroundColor(.white).separatorStyle(.none)
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.view.addSubview(self.infoList)
        self.view.bringSubviewToFront(navigation)
        self.navigation.title.text = NSLocalizedString("AboutAgoraEntertainment", comment: "")
    }
    

}

extension AboutAgoraEntertainmentViewController:UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if let name = self.infos[safe: section]?["sectionName"] as? String,name == NSLocalizedString("SceneVersion", comment: "") {
            return 44
        }
        return 0
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if indexPath.section == 1 {
            return 54
        }
        return 72
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if let name = self.infos[safe: section]?["sectionName"] as? String,name == NSLocalizedString("SceneVersion", comment: "") {
            let sectionHeader = UIView {
                UIView(frame: CGRect(x: 0, y: 0, width: self.infoList.frame.width, height: 44)).backgroundColor(.white)
                UILabel(frame: CGRect(x: 20, y: 12, width: self.infoList.frame.width-40, height: 20)).font(.systemFont(ofSize: 13, weight: .regular)).textColor(UIColor(0x6C7192)).text(name)
            }
            return sectionHeader
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
