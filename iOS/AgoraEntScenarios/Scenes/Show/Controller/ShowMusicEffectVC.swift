//
//  ShowMusicEffectVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/9.
//

import UIKit

class ShowMusicEffectVC: UIViewController {
    
    struct MusicConfigData {
        let title: String
        let dataArray: [ShowMusicEffectCell.CellData]
    }
    
    private var dataArray = [MusicConfigData]()
    
    private let headerView = ShowMusicTableHeaderView()
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.backgroundColor = .show_dark_cover_bg
        tableView.separatorStyle = .none
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = 128
        tableView.showsVerticalScrollIndicator = false
        tableView.tableHeaderView = headerView
        tableView.isScrollEnabled = false
        headerView.frame = CGRect(x: 0, y: 0, width: Screen.width, height: 58)
        return tableView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        configData()
    }
    
    private func setupUI(){
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(487)
        }
    }
    
    private func configData(){
        var musicBgDataArray = [ShowMusicEffectCell.CellData]()
        for title in ["欢快","浪漫", "欢快2"] {
            let data = ShowMusicEffectCell.CellData(image: "", title: title, style: .imageTop)
            musicBgDataArray.append(data)
        }
        let musicBg = MusicConfigData(title: "背景音乐", dataArray: musicBgDataArray)
        
        var beautyVoiceDataArray = [ShowMusicEffectCell.CellData]()
        for title in ["原声","甜美", "中性","稳重", "魔幻"] {
            let data = ShowMusicEffectCell.CellData(image: "", title: title, style: .imageBackground)
            beautyVoiceDataArray.append(data)
        }
        let beautyVoice = MusicConfigData(title: "美声", dataArray: beautyVoiceDataArray)
        
        var mixVoiceDataArray = [ShowMusicEffectCell.CellData]()
        for title in ["","KTV", "演唱会","录音棚", "空旷"] {
            let data = ShowMusicEffectCell.CellData(image: "", title: title, style: .imageBackground)
            mixVoiceDataArray.append(data)
        }
        let mixVoice = MusicConfigData(title: "混响", dataArray: beautyVoiceDataArray)
        
        dataArray = [musicBg, beautyVoice,mixVoice]
    }

}

extension ShowMusicEffectVC {
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        tableView.setRoundingCorners([.topLeft,.topRight], radius: 20)
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
    }
}

extension ShowMusicEffectVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellID = "ShowMusicEffectCell"
        var cell = tableView.dequeueReusableCell(withIdentifier: cellID) as? ShowMusicEffectCell
        if cell == nil {
            cell = ShowMusicEffectCell(style: .default, reuseIdentifier: cellID)
        }
        let data = dataArray[indexPath.row]
        cell?.setTitle(data.title, dataArray: data.dataArray)
        return cell!
    }
    
}
