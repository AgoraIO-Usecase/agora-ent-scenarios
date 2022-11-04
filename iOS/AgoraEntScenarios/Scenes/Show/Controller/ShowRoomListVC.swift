//
//  ShowRoomListViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit
import MJRefresh

class ShowRoomListVC: ShowBaseViewController {

    private var roomListView: ShowRoomListView!
    
    override func preferredNavigationBarHidden() -> Bool {
        return true
    }
    
    override func configNavigationBar(_ navigationBar: UINavigationBar) {
        setNaviTitleName("秀场直播".show_localized)
        setBackBtn()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        addRefresh()
    }
    
    private func setUpUI(){
      
        // 背景图
        let bgView = UIImageView()
        bgView.image = UIImage.show_sceneImage(name: "show_list_Bg")
        view.addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        roomListView = ShowRoomListView()
        roomListView.clickCreateButtonAction = { [weak self] in
            self?.createRoom()
        }
        view.addSubview(roomListView)
        roomListView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    // 创建房间
    private func createRoom(){
        let preVC = ShowCreateLiveVC()
        let preNC = UINavigationController(rootViewController: preVC)
        preNC.navigationBar.setBackgroundImage(UIImage(), for: .default)
        preNC.modalPresentationStyle = .overCurrentContext
        present(preNC, animated: true)
    }
    
    // 下拉刷新
    private func addRefresh(){
        roomListView.collectionView.mj_header = MJRefreshNormalHeader(refreshingBlock: {
            
        })
    }
}

