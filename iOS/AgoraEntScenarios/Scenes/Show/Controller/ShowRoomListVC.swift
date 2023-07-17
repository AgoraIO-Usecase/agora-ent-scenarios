//
//  ShowRoomListViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit

private let kAudienceShowPresetType = "kAudienceShowPresetType"

class ShowRoomListVC: UIViewController {
    
    let backgroundView = UIImageView()
    
    private let collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        let itemWidth = (Screen.width - 15 - 20 * 2) * 0.5
        layout.itemSize = CGSize(width: itemWidth, height: 234.0 / 160.0 * itemWidth)
        return UICollectionView(frame: .zero, collectionViewLayout: layout)
    }()
        
    private lazy var refreshControl: UIRefreshControl = {
        let ctrl = UIRefreshControl()
        ctrl.addTarget(self, action: #selector(refreshControlValueChanged), for: .valueChanged)
        return ctrl
    }()
    
    private let emptyView = ShowEmptyView()
    
    private let createButton = UIButton(type: .custom)
    
    
    private var roomList = [ShowRoomListModel]() {
        didSet {
            collectionView.reloadData()
            emptyView.isHidden = roomList.count > 0
            preLoadVisibleItems()
        }
    }
    
    // 自定义导航栏
    private let naviBar = ShowNavigationBar()
    
    private var needUpdateAudiencePresetType = false
    
    deinit {
        AppContext.unloadShowServiceImp()
        ShowAgoraKitManager.shared.destoryEngine()
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
        createViews()
        createConstrains()
        // 提前启动rtc engine
        ShowAgoraKitManager.shared.prepareEngine()
        ShowRobotService.shared.startCloudPlayers()
        preGenerateToken()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        fetchRoomList()
    }
    
    // 点击创建按钮
    @objc private func didClickCreateButton(){
        let preVC = ShowCreateLiveVC()
        let preNC = UINavigationController(rootViewController: preVC)
        preNC.navigationBar.setBackgroundImage(UIImage(), for: .default)
        preNC.modalPresentationStyle = .fullScreen
        present(preNC, animated: true)
    }
    
    @objc private func refreshControlValueChanged() {
        self.fetchRoomList()
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
        present(vc, animated: true)
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
            vc.roomList = roomList.filter({ $0.ownerId != VLUserCenter.user.id })
            vc.focusIndex = vc.roomList?.firstIndex(where: { $0.roomId == room.roomId }) ?? 0
            self.present(nc, animated: true)
        }
    }
    
    private func fetchRoomList() {
        AppContext.showServiceImp("").getRoomList(page: 1) { [weak self] error, roomList in
            guard let self = self else {return}
            self.refreshControl.endRefreshing()
            if let error = error {
                ToastView.show(text: error.localizedDescription)
                return
            }
            let list = roomList ?? []
            self.roomList = list
        }
    }
    // 预先加载RTC
    private func preLoadVisibleItems() {
        guard let token = AppContext.shared.rtcToken else {
            return
        }
        collectionView.indexPathsForVisibleItems.forEach { indexPath in
            let room = roomList[indexPath.item]
            ShowAgoraKitManager.shared.engine?.preloadChannel(byToken: token,
                                                              channelId: room.roomId,
                                                              uid: UInt(VLUserCenter.user.id) ?? 0)
        }
    }
    // 预先获取万能token
    private func preGenerateToken() {
        AppContext.shared.rtcToken = nil
        NetworkManager.shared.generateToken(
            channelName: "",
            uid: "\(UserInfo.userId)",
            tokenType: .token007,
            type: .rtc,
            expire: 24 * 60 * 60
        ) { token in
            guard let rtcToken = token else {
                return
            }
            AppContext.shared.rtcToken = rtcToken
            self.preLoadVisibleItems()
        }
    }
    
}
// MARK: - UICollectionView Call Back
extension ShowRoomListVC: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return roomList.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: ShowRoomListCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(ShowRoomListCell.self), for: indexPath) as! ShowRoomListCell
        let room = roomList[indexPath.item]
        cell.setBgImge((room.thumbnailId?.isEmpty ?? true) ? "0" : room.thumbnailId ?? "0",
                       name: room.roomName,
                       id: room.roomId,
                       count: room.roomUserCount)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let room = roomList[indexPath.item]
        let value = UserDefaults.standard.integer(forKey: kAudienceShowPresetType)
        let audencePresetType = ShowPresetType(rawValue: value)
        // 如果是owner是自己 或者已经设置过观众模式
        if AppContext.shared.isDebugMode == false {
            if room.ownerId == VLUserCenter.user.id || audencePresetType != .unknown {
                joinRoom(room)
            }else{
                showPresettingVC { [weak self] type in
                    self?.needUpdateAudiencePresetType = true
                    UserDefaults.standard.set(type.rawValue, forKey: kAudienceShowPresetType)
                    self?.joinRoom(room)
                }
            }
        }else {
            joinRoom(room)
        }
    }
    
    func scrollViewDidEndScrollingAnimation(_ scrollView: UIScrollView) {
        preLoadVisibleItems()
    }
    
}

// MARK: - Creations
extension ShowRoomListVC {
    private func createViews(){
        // 背景图
        backgroundView.image = UIImage.show_sceneImage(name: "show_list_Bg")
        view.addSubview(backgroundView)
        
        collectionView.backgroundColor = .clear
        collectionView.register(ShowRoomListCell.self, forCellWithReuseIdentifier: NSStringFromClass(ShowRoomListCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.refreshControl = self.refreshControl
        view.addSubview(collectionView)
        
        // 空列表
        emptyView.isHidden = true
        collectionView.addSubview(emptyView)
        
        // 创建房间按钮
        createButton.setTitleColor(.white, for: .normal)
        createButton.setTitle("room_list_create_room".show_localized, for: .normal)
        createButton.setImage(UIImage.show_sceneImage(name: "show_create_add"), for: .normal)
        createButton.imageEdgeInsets(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 5))
        createButton.titleEdgeInsets(UIEdgeInsets(top: 0, left: 5, bottom: 0, right: 0))
        createButton.backgroundColor = .show_btn_bg
        createButton.titleLabel?.font = .show_btn_title
        createButton.layer.cornerRadius = 48 * 0.5
        createButton.layer.masksToBounds = true
        createButton.addTarget(self, action: #selector(didClickCreateButton), for: .touchUpInside)
        view.addSubview(createButton)
        
        navigationController?.isNavigationBarHidden = true
        naviBar.title = "navi_title_show_live".show_localized
        view.addSubview(naviBar)
        let saveButtonItem = ShowBarButtonItem(title: "room_list_audience_setting".show_localized, target: self, action: #selector(didClickSettingButton))
        naviBar.rightItems = [saveButtonItem]
    }
    
    func createConstrains() {
        backgroundView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        collectionView.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets(top:  Screen.safeAreaTopHeight() + 54, left: 0, bottom: 0, right: 0))
        }
        emptyView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(156)
        }
        createButton.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalToSuperview().offset(-max(Screen.safeAreaBottomHeight(), 10))
            make.height.equalTo(48)
            make.width.equalTo(195)
        }
    }
}

