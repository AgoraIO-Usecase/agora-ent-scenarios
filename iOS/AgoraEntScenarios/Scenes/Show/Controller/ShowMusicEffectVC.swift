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
        let defaultSelectIndex: Int
    }
    
    // 背景音乐
    private lazy var musicBg: MusicConfigData = {
        var musicBgDataArray = [ShowMusicEffectCell.CellData]()
        let titles = [
            "show_music_setting_bg_happy".show_localized,
            "show_music_setting_bg_romantic".show_localized,
            "show_music_setting_bg_happy_2".show_localized,
        ]
        let images = [
            "show_music_set_bg",
            "show_music_set_bg",
            "show_music_set_bg",
        ]
        
        for i in 0 ..< titles.count {
            let data = ShowMusicEffectCell.CellData(image: images[i], title: titles[i], style: .imageTop)
            musicBgDataArray.append(data)
        }
        return MusicConfigData(title: "show_music_setting_bg_title".show_localized, dataArray: musicBgDataArray, defaultSelectIndex: 0)
    }()
    
    // 美声
    private lazy var beautyVoice: MusicConfigData = {
        var beautyVoiceDataArray = [ShowMusicEffectCell.CellData]()
        
        let titles = [
            "show_music_setting_beaty_yuansheng".show_localized,
            "show_music_setting_beaty_tianmei".show_localized,
            "show_music_setting_beaty_zhongxing".show_localized,
            "show_music_setting_beaty_wenzhong".show_localized,
            "show_music_setting_beaty_mohuan".show_localized,
        ]
        let images = [
            "show_music_beauty_yuanchang",
            "show_music_beauty_tianmei",
            "show_music_beauty_zhongxing",
            "show_music_beauty_wenzhong",
            "show_music_beauty_mohuan",
        ]
        for i in 0 ..< titles.count {
            let data = ShowMusicEffectCell.CellData(image: images[i], title: titles[i], style: .imageBackground)
            beautyVoiceDataArray.append(data)
        }
        return MusicConfigData(title:  "show_music_setting_beatuy_title".show_localized, dataArray: beautyVoiceDataArray, defaultSelectIndex: 1)
    }()
    
    // 混响
    private lazy var mixVoice: MusicConfigData = {
        var mixVoiceDataArray = [ShowMusicEffectCell.CellData]()
        let titles = [
            "show_music_setting_mix_none".show_localized,
            "show_music_setting_mix_ktv".show_localized,
            "show_music_setting_mix_concert".show_localized,
            "show_music_setting_mix_record".show_localized,
            "show_music_setting_mix_hollowness".show_localized,
        ]
        let images = [
            "show_music_mix_none",
            "show_music_mix_KTV",
            "show_music_mix_concert",
            "show_music_mix_record",
            "show_music_mix_hollowness",
        ]
        for i in 0 ..< titles.count {
            let style: ShowMusicEffectCell.LayoutStyle = i == 0 ? .imageOnly : .imageBackground
            let data = ShowMusicEffectCell.CellData(image: images[i], title: titles[i], style: style)
            mixVoiceDataArray.append(data)
        }
        return MusicConfigData(title: "show_music_setting_mix_title".show_localized, dataArray: mixVoiceDataArray, defaultSelectIndex: 0)
    }()
    
    private lazy var dataArray = {
        return [musicBg, beautyVoice,mixVoice]
    }()
    
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
    }
    
    private func setupUI(){
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(487)
        }
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
        cell?.setTitle(data.title, dataArray: data.dataArray,defaultSelectIndex: data.defaultSelectIndex)
        return cell!
    }
    
}
