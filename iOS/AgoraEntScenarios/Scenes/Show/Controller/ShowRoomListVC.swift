//
//  ShowRoomListViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit
import MJRefresh

private let kAudienceShowPresetType = "kAudienceShowPresetType"

class ShowRoomListVC: UIViewController {

    private var roomListView: ShowRoomListView!
    private var roomList: [ShowRoomListModel]?
    
    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    private var needUpdateAudiencePresetType = false
    
    deinit {
        AppContext.unloadShowServiceImp()
        showLogger.info("deinit-- ShowRoomListVC")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        hidesBottomBarWhenPushed = true
        showLogger.info("init-- ShowRoomListVC")
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
        if AppContext.shared.isDebugMode {
            showDebugSetVC()
        }else {
            showPresettingVC {[weak self] type in
                let value = UserDefaults.standard.integer(forKey: kAudienceShowPresetType)
                let audencePresetType = ShowPresetType(rawValue: value)
                if audencePresetType != .unknown {
                    UserDefaults.standard.set(type.rawValue, forKey: kAudienceShowPresetType)
                }
                self?.needUpdateAudiencePresetType = true
            }
        }
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
            guard let wSelf = self else { return }
            let value = UserDefaults.standard.integer(forKey: kAudienceShowPresetType)
            let audencePresetType = ShowPresetType(rawValue: value)
            // 如果是owner是自己 或者已经设置过观众模式
            if AppContext.shared.isDebugMode == false {
                if room.ownerId == VLUserCenter.user.id || audencePresetType != .unknown {
                    wSelf.joinRoom(room)
                }else{
                    wSelf.showPresettingVC { [weak self] type in
                        self?.needUpdateAudiencePresetType = true
                        UserDefaults.standard.set(type.rawValue, forKey: kAudienceShowPresetType)
                        self?.joinRoom(room)
                    }
                }
            }else {
                wSelf.joinRoom(room)
            }
        }
        view.addSubview(roomListView)
        roomListView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func showDebugSetVC(){
        let vc = ShowDebugSettingVC()
        vc.isBroadcastor = false
        navigationController?.pushViewController(vc, animated: true)
    }
    
    private func showPresettingVC(selected:((_ type: ShowPresetType)->())? = nil) {
        let vc = ShowPresettingVC()
        vc.isBroadcaster = false
        vc.didSelectedPresetType = { type, modeName in
            selected?(type)
        }
//        let value = UserDefaults.standard.integer(forKey: kAudienceShowPresetType)
//        let audencePresetType = ShowPresetType(rawValue: value)
//        vc.selectedType = audencePresetType
        present(vc, animated: true)
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
        let preNC = UINavigationController(rootViewController: preVC)
        preNC.navigationBar.setBackgroundImage(UIImage(), for: .default)
        preNC.modalPresentationStyle = .fullScreen
        present(preNC, animated: true)
    }
    
    // 加入房间
    private func joinRoom(_ room: ShowRoomListModel){
        let vc = ShowLivePagesViewController()
        let audencePresetType = UserDefaults.standard.integer(forKey: kAudienceShowPresetType)
        vc.audiencePresetType = ShowPresetType(rawValue: audencePresetType)
        vc.needUpdateAudiencePresetType = needUpdateAudiencePresetType
        let nc = UINavigationController(rootViewController: vc)
        nc.modalPresentationStyle = .fullScreen
        if room.ownerId == VLUserCenter.user.id {
            AppContext.showServiceImp(room.roomId).joinRoom(room: room) {[weak self] error, model in
                if let error = error {
                    ToastView.show(text: error.localizedDescription)
                    return
                }
                vc.roomList = [room]
                vc.focusIndex = 0
                self?.present(nc, animated: true)
            }
        } else {
            vc.roomList = roomList?.filter({ $0.ownerId != VLUserCenter.user.id })
            vc.focusIndex = vc.roomList?.firstIndex(where: { $0.roomId == room.roomId }) ?? 0
            self.present(nc, animated: true)
        }
    }
    
    private func getRoomList() {
        AppContext.showServiceImp("").getRoomList(page: 1) { [weak self] error, roomList in
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

