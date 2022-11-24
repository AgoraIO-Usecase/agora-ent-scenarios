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
    private var roomList: [ShowRoomListModel]?
    
    override func preferredNavigationBarHidden() -> Bool {
        return true
    }
    
    override func configNavigationBar(_ navigationBar: UINavigationBar) {
        setNaviTitleName("navi_title_show_live".show_localized)
        setBackBtn()
    }
    
    deinit {
        AppContext.unloadShowServiceImp()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
//        getRoomList()
        addRefresh()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        getRoomList()
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
        roomListView.joinRoomAction = { [weak self] room in
            self?.joinRoom(room)
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
        preNC.modalPresentationStyle = .fullScreen
        present(preNC, animated: true)
    }
    
    // 加入房间
    private func joinRoom(_ room: ShowRoomListModel){
        AppContext.showServiceImp.joinRoom(room: room) {[weak self] error, detailModel in
            if error == nil {
                let vc = ShowLiveViewController()
                vc.room = room
                let nc = UINavigationController(rootViewController: vc)
                nc.modalPresentationStyle = .fullScreen
                self?.present(nc, animated: true)
            }
        }
    }
    
    private func getRoomList() {
        AppContext.showServiceImp.getRoomList(page: 1) { [weak self] error, roomList in
            if let list = roomList {
                self?.roomListView.roomList = list
                self?.roomList = list
            }
            self?.roomListView.collectionView.mj_header?.endRefreshing()
        }
    }
    
    // 下拉刷新
    private func addRefresh(){
        roomListView.collectionView.mj_header = MJRefreshNormalHeader(refreshingBlock: { [weak self] in
            self?.getRoomList()
        })
    }
}

