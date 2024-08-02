import UIKit
import SnapKit
import SVProgressHUD
import AgoraCommon

class GameListViewController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    let cellReuseIdentifier = "GameListCell"
    let sessionReuseIdentifier = "GameSessionView"
    let mixedSessionReuseIdentifier = "GameMixedSectionView"
    private var userInfo: InteractiveJoyUserInfo!
    private var service: JoyServiceProtocol!
    private var gameApi: GameApiManager = GameApiManager()
    private var dataArray: [GameModel] = [GameModel]()
    private var gameListType: GameVendor = .huRan
    
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop , width: self.view.width, height: 44))
        return bar
    }()
    
    private lazy var bottomBar: GameListBottomBar = {
       let bar = GameListBottomBar()
        return bar
    }()
    
    private lazy var imageBackgroundView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage.sceneImage(name: "joy_list_Bg@3x.png")
        return imageView
    }()
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.sectionInset = UIEdgeInsets(top: 20, left: 34, bottom: 20, right: 34)
        layout.minimumInteritemSpacing = 20
        layout.minimumLineSpacing = 10
        
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.dataSource = self
        collectionView.delegate = self
        collectionView.backgroundColor = .clear
        collectionView.register(GameListCell.self, forCellWithReuseIdentifier: cellReuseIdentifier)
        collectionView.register(SectionView.self, forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader, withReuseIdentifier: sessionReuseIdentifier)
        collectionView.register(GameMixedSectionView.self, forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader, withReuseIdentifier: mixedSessionReuseIdentifier)
        return collectionView
    }()
    
    private lazy var backgroundView: RoundedBackgroundView = {
        let backgroundView = RoundedBackgroundView(frame: CGRect(x: 0, y: 0, width: CGRectGetWidth(view.bounds), height: CGRectGetHeight(view.bounds)))
        backgroundView.layer.zPosition = -1
        
        return backgroundView
    }()
    
    private lazy var headerView: GameBannerView = {
        let headerView = GameBannerView(frame: CGRect(x: 0, y: -346, width: CGRectGetWidth(view.bounds), height: 346))
        headerView.setBanners(banners: [BannerModel(image: "play_zone_banner", title: "")])
        
        return headerView
    }()
    
    required init(userInfo: InteractiveJoyUserInfo) {
        super.init(nibName: nil, bundle: nil)
        self.userInfo = userInfo
        JoyLogger.info("init-- RoomListViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        collectionView.removeObserver(self, forKeyPath: "contentSize")
        print("")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        view.backgroundColor = UIColor.white
        service = JoyServiceImpl(appId: joyAppId, host:joyHost, user: userInfo, rtmClient: nil)
            
        collectionView.addSubview(headerView)
        collectionView.addSubview(backgroundView)
        
        view.addSubview(imageBackgroundView)
        view.addSubview(naviBar)
        view.addSubview(collectionView)
        view.addSubview(bottomBar)
        
        let safeAreaBottom = UIDevice.current.aui_SafeDistanceBottom
        imageBackgroundView.snp_makeConstraints { make in
            make.left.right.top.bottom.equalTo(0)
        }
        
        bottomBar.snp_makeConstraints { make in
            make.left.right.bottom.equalTo(0)
            make.height.equalTo(safeAreaBottom + 50)
        }
        
        collectionView.snp.makeConstraints { make in
            make.right.left.equalTo(0)
            make.bottom.equalTo(bottomBar.top)
            make.top.equalTo(naviBar.bottom)
        }
        
        bottomBar.gameSupplierCallback = {[weak self] type in
            guard let self = self else {return}
            switch type {
            case .left:
                self.showActionSheet()
                break
            case.right:
                let vc = RoomListViewController(userInfo: self.userInfo, service: service)
                vc.hidesBottomBarWhenPushed = true
                self.navigationController?.pushViewController(vc, animated: true)
                break
            }
        }
        
        collectionView.contentInset = UIEdgeInsets(top: 346, left: 0, bottom: safeAreaBottom + 50, right: 0)
        collectionView.addObserver(self, forKeyPath: "contentSize", options: .new, context: nil)
        fetchGameList(vender: .huRan)
        service.getRoomList { _ in}
    }
    
    private func fetchGameList(vender: GameVendor) {
        self.gameApi.getGameList(vendor: vender) { [weak self] error, list in
            if let error = error {
                return
            }
            
            guard let list = list else {return}
            self?.gameListType = vender
            self?.dataArray = list
            self?.collectionView.reloadData()
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(500)) {
                self?.viewDidLayoutSubviews()
            }
        }
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return dataArray[section].games.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: cellReuseIdentifier, for: indexPath) as! GameListCell
        let game = dataArray[indexPath.section].games[indexPath.item]
        cell.configure(with: game)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        guard let layout = collectionViewLayout as? UICollectionViewFlowLayout else {
            return CGSize.zero
        }
        
        let totalSpacing = layout.minimumInteritemSpacing * 3 + layout.sectionInset.left + layout.sectionInset.right + 14 * 2
        let width = (collectionView.bounds.width - totalSpacing) / 4
        let height = (width * 91) / 62
        return CGSize(width: width, height: height)
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        if kind == UICollectionView.elementKindSectionHeader {
            if indexPath.section == 0 {
                let sectionView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: mixedSessionReuseIdentifier, for: indexPath) as! GameMixedSectionView
                sectionView.gameAdView.titleLabel.text = LanguageManager.localValue(key: "game_list_rex_section_title")
                sectionView.gameAdView.descriptionLabel.text = LanguageManager.localValue(key: "game_list_rex_section_des")
                sectionView.gameAdView.imageView.image = UIImage.sceneImage(name: "game_relaxation_ic")
                return sectionView
            }
            let sectionView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: sessionReuseIdentifier, for: indexPath) as! SectionView
            sectionView.titleLabel.text = dataArray[indexPath.section].gameSection
            return sectionView
        }
        
        return UICollectionReusableView()
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        if section == 0 {
            return CGSize(width: collectionView.bounds.width, height: 100)
        }
        return CGSize(width: collectionView.bounds.width, height: 50)
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let game = self.dataArray[indexPath.section].games[indexPath.item]
        let gameId = game.gameId
        let gameName = game.gameName
        let gameUrl = game.gameUrl
        if gameListType != .huRan {
            let webGameViewController = PlayWebGameViewController()
            webGameViewController.title = gameName
            webGameViewController.url = gameUrl
            self.navigationController?.pushViewController(webGameViewController, animated: true)
            return
        }
        
        let presentView = VRCreateRoomPresentView.shared
        let vc = VRCreateViewController()
        presentView.showView(with: CGRect(x: 0, y: (self.view.bounds.size.height ?? 0) - 343, width: self.view.bounds.width ?? 0, height: 343), vc: vc)
        self.view.addSubview(presentView)
        
        vc.createRoomBlock = { height in
            presentView.update(height)
        }
    
        vc.createRoomVCBlock = {[weak self] (name, pwd) in
            presentView.dismiss()
            guard let self = self else {return}
            SVProgressHUD.show()
            var gameRoomInfo = InteractiveJoyRoomInfo()
            gameRoomInfo.roomName = name
            gameRoomInfo.gameId = gameId
            gameRoomInfo.password = pwd
            gameRoomInfo.isPrivate = !pwd.isEmpty
            gameRoomInfo.roomId = "\(arc4random_uniform(899999) + 100000)"
            gameRoomInfo.badgeTitle = gameName
            gameRoomInfo.roomUserCount = 1
            gameRoomInfo.ownerAvatar = userInfo.avatar
            gameRoomInfo.ownerId = userInfo.userId
            service.createRoom(gameRoomInfo: gameRoomInfo) { [weak self] roomInfo, error in
                SVProgressHUD.dismiss()
                guard let self = self else { return }
                if error != nil {
                    VLToast.toast("\(error?.localizedDescription)")
                    return
                }
                
                guard let roomInfo = roomInfo else {return}
                
                let gameVC = PlayGameViewController(userInfo: self.userInfo, service: self.service, roomInfo: roomInfo)
                let gameId = self.dataArray[indexPath.section].games[indexPath.item].gameId
                gameVC.hidesBottomBarWhenPushed = true
                self.navigationController?.pushViewController(gameVC, animated: true)
            }
        }
    }
}

extension GameListViewController {
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "contentSize" {
            guard let contentSize = change?[.newKey] as? CGSize else {return}
            self.backgroundView.frame = CGRect(origin: CGPoint(x: 20, y: 0), size: CGSize(width: CGRectGetWidth(view.bounds) - 20 * 2, height: contentSize.height))
        }
    }
    
    func showActionSheet() {
        let customAlertController = CustomAlertController(title: LanguageManager.localValue(key: "game_list_vender_title"), message: nil)
        let actionTitle1 = LanguageManager.localValue(key: "game_list_huran_title")
        let actionTitle2 = LanguageManager.localValue(key: "game_list_yuanyou_title")
        let actionTitle3 = LanguageManager.localValue(key: "game_list_qunwan_title")
        let actionTitle4 = LanguageManager.localValue(key: "authorized_cancel")
        customAlertController.addAction(title: actionTitle1, style: .default) { _ in
            self.bottomBar.currentSelectText = actionTitle1
            self.fetchGameList(vender: .huRan)
        }
        customAlertController.addAction(title: actionTitle2, style: .default) { _ in
            self.bottomBar.currentSelectText = actionTitle2
            self.fetchGameList(vender: .yuanyou)
        }
        customAlertController.addAction(title: actionTitle3, style: .default) { _ in
            self.bottomBar.currentSelectText = actionTitle3
            self.fetchGameList(vender: .qunwan)
        }
        customAlertController.addAction(title: actionTitle4, style: .cancel) { _ in
            self.bottomBar.currentSelectText = actionTitle4
        }
        customAlertController.modalPresentationStyle = .overFullScreen
        customAlertController.modalTransitionStyle = .crossDissolve
        customAlertController.selectedTitle = bottomBar.currentSelectText
        self.present(customAlertController, animated: true, completion: nil)
    }
}

class RoundedBackgroundView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.layer.cornerRadius = 24.0
        self.layer.masksToBounds = true
        self.backgroundColor = .white
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        return nil
    }
    
}
