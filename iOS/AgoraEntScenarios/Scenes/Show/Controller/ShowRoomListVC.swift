//
//  ShowRoomListViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit
import MJRefresh

class ShowRoomListVC: UIViewController {

    private var roomListView: ShowRoomListView!
    private var roomList: [ShowRoomListModel]?
    
    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    private lazy var agoraManager: ShowAgoraKitManager = {
        let manager = ShowAgoraKitManager()
        manager.defaultSetting()
        return manager
    }()
    
    deinit {
        AppContext.unloadShowServiceImp()
        print("---ShowRoomListVC 销毁了---")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        hidesBottomBarWhenPushed = true
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        setUpNaviBar()
        addRefresh()
    }
    
    @objc private func didClickSettingButton(){
        let vc = ShowPresettingVC()
        vc.isBroadcaster = false
        vc.didSelectedPresetType = {[weak self] type, modeName in
            self?.agoraManager.updatePresetForType(type, mode: .signle)
        }
        present(vc, animated: true)
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
    
    private func setUpNaviBar() {
        navigationController?.isNavigationBarHidden = true
        naviBar.title = "navi_title_show_live".show_localized
        view.addSubview(naviBar)
        let saveButtonItem = ShowBarButtonItem(title: "room_list_audience_setting".show_localized, target: self, action: #selector(didClickSettingButton))
        naviBar.rightItems = [saveButtonItem]
    }
    
    // 创建房间
    private func createRoom(){
        let preVC = ShowCreateLiveVC()
        preVC.agoraKitManager = agoraManager
        let preNC = UINavigationController(rootViewController: preVC)
        preNC.navigationBar.setBackgroundImage(UIImage(), for: .default)
        preNC.modalPresentationStyle = .fullScreen
        present(preNC, animated: true)
    }
    
    // 加入房间
    private func joinRoom(_ room: ShowRoomListModel){
        AppContext.showServiceImp.joinRoom(room: room) {[weak self] error, detailModel in
            if error == nil {
                guard let wSelf = self else { return }
                let vc = ShowLiveViewController()
                vc.agoraKitManager = wSelf.agoraManager
                vc.room = room
                let nc = UINavigationController(rootViewController: vc)
                nc.modalPresentationStyle = .fullScreen
                wSelf.present(nc, animated: true)
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

