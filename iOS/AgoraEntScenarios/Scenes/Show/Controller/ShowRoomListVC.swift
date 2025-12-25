//
//  ShowRoomListViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/1.
//

import UIKit
import VideoLoaderAPI
import AgoraCommon

class ShowRoomListVC: UIViewController {
    let backgroundView = UIImageView()
    
    private lazy var stateManager: AppStateManager = {
        let manager = AppStateManager()
        manager.appStateChangeHandler = { [weak self] isInBackground in
            if isInBackground { return }
            self?.checkTokenValid()
        }
        
        manager.networkStatusChangeHandler = { [weak self] isAvailable in
            guard isAvailable else { return }
            self?.checkTokenValid()
        }
        
        return manager
    }()
    
    private lazy var delegateHandler: ShowCollectionLoadingDelegateHandler = {
        let handler = ShowCollectionLoadingDelegateHandler(localUid: UInt(UserInfo.userId)!)
//        handler.didSelected = {[weak self] room in
//            self?.joinRoom(room)
//        }
        return handler
    }()
    
    private let collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        let itemWidth = (Screen.width - 15 - 20 * 2) * 0.5
        layout.itemSize = CGSize(width: itemWidth, height: itemWidth)
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
            delegateHandler.roomList = AGRoomArray(roomList: roomList)
            collectionView.reloadData()
            emptyView.isHidden = roomList.count > 0
        }
    }
    
    private let naviBar = ShowNavigationBar()
    
    private var needUpdateAudiencePresetType = false
    
    private lazy var modelSelectorButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle(getModelTypeDisplayText(), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5)
        button.layer.cornerRadius = 4
        button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 12)
        button.addTarget(self, action: #selector(didClickModelSelector), for: .touchUpInside)
        return button
    }()
    
    deinit {
        ShowLogger.info("deinit-- ShowRoomListVC")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        ShowLogger.info("init-- ShowRoomListVC")
        VideoLoaderApiImpl.shared.printClosure = { msg in
            ShowLogger.info(msg, context: "VideoLoaderApi")
        }
        VideoLoaderApiImpl.shared.warningClosure = { msg in
            ShowLogger.warn(msg, context: "VideoLoaderApi")
        }
        VideoLoaderApiImpl.shared.errorClosure = { msg in
            ShowLogger.error(msg, context: "VideoLoaderApi")
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        _ = stateManager
        AppContext.shared.sceneImageBundleName = "showResource"
        createViews()
        createConstrains()
        ShowAgoraKitManager.shared.prepareEngine()
        ShowRobotService.shared.startCloudPlayers()
        preGenerateToken()
        checkDevice()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        // cell恢复隐藏coverlayer
        collectionView.reloadData()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        checkTokenValid()
        autoRefreshing()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if isMovingFromParent {
            destroyService()
        }
    }
    
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
    
    @objc private func didClickModelSelector() {
        let currentModelType = AgoraBeautyManager.getCurrentModelType()
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        
        // Adaptive option
        let adaptiveAction = UIAlertAction(title: getModelTypeText(.adaptive), style: .default) { [weak self] _ in
            self?.switchModelType(.adaptive)
        }
        if currentModelType == .adaptive {
            adaptiveAction.setValue(true, forKey: "checked")
        }
        alertController.addAction(adaptiveAction)
        
        // Large model option
        let largeAction = UIAlertAction(title: getModelTypeText(.large), style: .default) { [weak self] _ in
            self?.switchModelType(.large)
        }
        if currentModelType == .large {
            largeAction.setValue(true, forKey: "checked")
        }
        alertController.addAction(largeAction)
        
        // Small model option
        let smallAction = UIAlertAction(title: getModelTypeText(.small), style: .default) { [weak self] _ in
            self?.switchModelType(.small)
        }
        if currentModelType == .small {
            smallAction.setValue(true, forKey: "checked")
        }
        alertController.addAction(smallAction)
        
        // Cancel action
        alertController.addAction(UIAlertAction(title: "show_alert_cancel_btn_title".show_localized, style: .cancel))
        
        // For iPad
        if let popover = alertController.popoverPresentationController {
            popover.sourceView = modelSelectorButton
            popover.sourceRect = modelSelectorButton.bounds
        }
        
        present(alertController, animated: true)
    }
    
    private func getModelTypeDisplayText() -> String {
        return getModelTypeText(AgoraBeautyManager.getCurrentModelType())
    }
    
    private func getModelTypeText(_ modelType: AgoraBeautyModelType) -> String {
        switch modelType {
        case .adaptive:
            // TODO: Add to Localizable.strings: "show_model_selector_adaptive" = "自适应"
            return "自适应"
        case .large:
            // TODO: Add to Localizable.strings: "show_model_selector_large" = "大模型"
            return "大模型"
        case .small:
            // TODO: Add to Localizable.strings: "show_model_selector_small" = "小模型"
            return "小模型"
        }
    }
    
    private func switchModelType(_ modelType: AgoraBeautyModelType) {
        // Save model type
        AgoraBeautyManager.saveModelType(modelType)
        
        // Update button text
        modelSelectorButton.setTitle(getModelTypeDisplayText(), for: .normal)
        
        // Destroy and recreate RTC Engine
        ShowAgoraKitManager.shared.destoryEngine()
        
        // Delay to ensure engine is fully destroyed
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            ShowAgoraKitManager.shared.prepareEngine()
            // Check device settings (corresponds to Android's initVideoSettings() in initRtc())
            self?.checkDevice()
        }
    }
    
    private func checkDevice() {
        let score = ShowAgoraKitManager.shared.engine?.queryDeviceScore() ?? 0
        if (score < 85) {// (0, 85)
            ShowAgoraKitManager.shared.deviceLevel = .low
        } else if (score < 90) {// [85, 90)
            ShowAgoraKitManager.shared.deviceLevel = .medium
        } else {// (> 90)
            ShowAgoraKitManager.shared.deviceLevel = .high
        }
        ShowAgoraKitManager.shared.deviceScore = Int(score)
   }
    
    private func joinRoom(_ room: ShowRoomListModel){
        ShowAgoraKitManager.shared.setupAudienceProfile()
        ShowAgoraKitManager.shared.updateLoadingType(roomId: room.roomId, channelId: room.roomId, playState: .joinedWithAudioVideo)
        
        if room.ownerId == VLUserCenter.user.id {
            ToastView.show(text: "show_join_own_room_error".show_localized)
        } else {
            let vc = ShowLivePagesViewController()
            let nc = UINavigationController(rootViewController: vc)
            nc.modalPresentationStyle = .fullScreen
            vc.roomList = roomList.filter({ $0.ownerId != VLUserCenter.user.id })
            vc.focusIndex = vc.roomList?.firstIndex(where: { $0.roomId == room.roomId }) ?? 0
            self.present(nc, animated: true)
        }
    }
    
    private func fetchRoomList() {
        AppContext.showServiceImp()?.getRoomList(page: 1) { [weak self] error, roomList in
            self?.refreshControl.endRefreshing()
            guard let self = self, let roomList = roomList else {return}
            if let error = error {
                ShowLogger.error(error.localizedDescription)
                return
            }
            self.roomList = roomList
        }
    }
    
    private func autoRefreshing(){
        if !refreshControl.isRefreshing {
            collectionView.setContentOffset(CGPoint(x: 0, y: -refreshControl.frame.size.height), animated: true)
            refreshControl.beginRefreshing()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                self.refreshControl.sendActions(for: .valueChanged)
            }
        }
    }

    private func preGenerateToken() {
        ShowAgoraKitManager.shared.preGenerateToken {[weak self] token in
            guard let self = self, let rtcToken = token, rtcToken.count > 0 else {
                return
            }
            self.delegateHandler.preLoadVisibleItems(scrollView: self.collectionView)
        }
    }
    
    private func checkTokenValid() {
        if AppContext.shared.rtcToken?.count ?? 0 > 0, let date = AppContext.shared.tokenDate, Int64(-date.timeIntervalSinceNow) < 20 * 60 * 60 {
            return
        }
        preGenerateToken()
    }
    
    private func destroyService() {
        AppContext.unloadShowServiceImp()
        VideoLoaderApiImpl.shared.cleanCache()
        ShowAgoraKitManager.shared.destoryEngine()
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
        cell.setBgImge("\(indexPath.item % 5)",
                       name: room.roomName,
                       id: room.roomId,
                       count: room.roomUserCount,
                       avatarUrl: room.ownerAvatar,
                       isPrivate: false)
        cell.ag_addPreloadTap(roomInfo: room, localUid: delegateHandler.localUid) {[weak self] state in
            if AppContext.shared.rtcToken?.count ?? 0 == 0 {
                if state == .began {
                    self?.preGenerateToken()
                } else if state == .ended {
                    ToastView.show(text: "Token is not exit, try again!")
                }
                return false
            }
            
            return true
        } onRequireRenderVideo: { _, _ in
            return nil
        } completion: { [weak self] in
            self?.joinRoom(room)
            cell.showCoverView()
        }

        return cell
    }
}

// MARK: - Creations
extension ShowRoomListVC {
    private func createViews(){
        backgroundView.image = UIImage.show_sceneImage(name: "show_list_Bg")
        view.addSubview(backgroundView)
        
        collectionView.backgroundColor = .clear
        collectionView.register(ShowRoomListCell.self, forCellWithReuseIdentifier: NSStringFromClass(ShowRoomListCell.self))
        collectionView.delegate = delegateHandler
        collectionView.dataSource = self
        collectionView.refreshControl = self.refreshControl
        view.addSubview(collectionView)
        
        emptyView.isHidden = true
        collectionView.addSubview(emptyView)
        
        createButton.setBackgroundImage(UIImage.show_sceneImage(name: "show_create_add_bg"), for: .normal)
        createButton.addTarget(self, action: #selector(didClickCreateButton), for: .touchUpInside)
        view.addSubview(createButton)
        
        view.addSubview(modelSelectorButton)
        
        navigationController?.isNavigationBarHidden = true
        naviBar.title = "navi_title_show_live".show_localized
        view.addSubview(naviBar)
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
        }
        
        modelSelectorButton.snp.makeConstraints { make in
            make.trailing.equalToSuperview().offset(-20)
            make.bottom.equalToSuperview().offset(-max(Screen.safeAreaBottomHeight() + 100, 110))
        }
    }
}

class ShowCollectionLoadingDelegateHandler: AGCollectionLoadingDelegateHandler {
    var didSelected: ((ShowRoomListModel) -> Void)?
    
    open func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let item = roomList?[indexPath.row] as? ShowRoomListModel else {return}
        didSelected?(item)
    }
}
