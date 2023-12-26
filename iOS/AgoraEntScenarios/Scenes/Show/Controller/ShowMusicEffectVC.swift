//
//  ShowMusicEffectVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/9.
//

import UIKit
import AgoraRtcKit

class ShowMusicEffectVC: UIViewController {
    
    var musicManager: ShowMusicPresenter?
    
    var currentChannelId: String?
    
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
    
    deinit {
        showLogger.info("deinit-- ShowMusicEffectVC ")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overCurrentContext
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
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
        return musicManager?.dataArray.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellID = "ShowMusicEffectCell"
        var cell = tableView.dequeueReusableCell(withIdentifier: cellID) as? ShowMusicEffectCell
        if cell == nil {
            cell = ShowMusicEffectCell(style: .default, reuseIdentifier: cellID)
        }
        guard let data = musicManager?.dataArray[indexPath.row] else {
            return cell!
        }
        cell?.setTitle(data.title, dataArray: data.dataArray, selectAction: {[weak self] index in
            data.selectedIndex = index
            switch data.type {
            case .resource:
                self?.musicManager?.setMusicIndex(data.selectedIndex)
            case .beauty:
                self?.musicManager?.setBeautyIndex(data.selectedIndex)
            case .mixture:
                self?.musicManager?.setMixIndex(data.selectedIndex)
            }
            if let channelId = self?.currentChannelId,
               let playerId = ShowAgoraKitManager.shared.mediaPlayer()?.getMediaPlayerId() {
                let options = AgoraRtcChannelMediaOptions()
                options.publishMediaPlayerAudioTrack = true
                options.publishMediaPlayerId = Int(playerId)
                ShowAgoraKitManager.shared.updateChannelEx(channelId: channelId, options: options)
            }
            tableView.reloadData()
        })
        return cell!
    }
}
