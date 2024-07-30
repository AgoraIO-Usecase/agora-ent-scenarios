import UIKit
import SnapKit
import SVProgressHUD

class GameListViewController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    let cellReuseIdentifier = "GameListCell"
    let sessionReuseIdentifier = "GameSessionView"
    let mixedSessionReuseIdentifier = "GameMixedSectionView"
    private var userInfo: InteractiveJoyUserInfo!
    private var service: JoyServiceProtocol!
    
    lazy var dataArray: [GameModel] = {
        return [
            GameModel(gameSection: "休闲娱乐", games: [
                Game(gameId: 1468434637562912769, gameName: "数字转轮", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "短道速滑", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "石头剪刀布", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "排雷兵", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "云蹦迪", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "美式8球", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "桌球", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "蛇梯", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "怪物消消", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "对战消消乐", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "连连看", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "跳一跳", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "欢乐大富翁", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "Carrom", gamePic: "ic_dhs.png"),
            ]),
            GameModel(gameSection: "射击格斗", games: [
                Game(gameId: 1468434637562912769, gameName: "超级玛丽", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽2", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽3", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽4", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽5", gamePic: "ic_dhs.png")
            ]),
            GameModel(gameSection: "冒险闯关", games: [
                Game(gameId: 1468434637562912769, gameName: "超级玛丽", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽2", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽3", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽4", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽5", gamePic: "ic_dhs.png")
            ]),
            GameModel(gameSection: "冒险闯关", games: [
                Game(gameId: 1468434637562912769, gameName: "超级玛丽", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽2", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽3", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽4", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽5", gamePic: "ic_dhs.png")
            ]),
            GameModel(gameSection: "冒险闯关", games: [
                Game(gameId: 1468434637562912769, gameName: "超级玛丽", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽2", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽3", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽4", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽5", gamePic: "ic_dhs.png")
            ]),
            GameModel(gameSection: "冒险闯关", games: [
                Game(gameId: 1468434637562912769, gameName: "超级玛丽", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽2", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽3", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽4", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽5", gamePic: "ic_dhs.png")
            ]),
            GameModel(gameSection: "冒险闯关", games: [
                Game(gameId: 1468434637562912769, gameName: "超级玛丽", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽2", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽3", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽4", gamePic: "ic_dhs.png"),
                Game(gameId: 1468434637562912769, gameName: "超级玛丽5", gamePic: "ic_dhs.png")
            ])
        ]
    }()
    
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        return bar
    }()
    
    private lazy var bottomBar: BottomBar = {
       let bar = BottomBar()
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
        headerView.setBanners(banners: [BannerModel(image: "", title: "体验弹幕互动新玩法")])
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
    
    override func viewDidLayoutSubviews() {
        let contentSize = self.collectionView.contentSize
        self.backgroundView.frame = CGRect(origin: CGPoint(x: 20, y: 0), size: CGSize(width: CGRectGetWidth(view.bounds) - 20 * 2, height: contentSize.height))
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
        return CGSize(width: width, height: width + 40)
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        if kind == UICollectionView.elementKindSectionHeader {
            if indexPath.section == 0 {
                let sectionView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: mixedSessionReuseIdentifier, for: indexPath) as! GameMixedSectionView
                sectionView.titleLabel.text = dataArray[indexPath.section].gameSection
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
        let presentView = VRCreateRoomPresentView.shared
        let vc = VRCreateViewController()
        let gameId = self.dataArray[indexPath.section].games[indexPath.item].gameId
        presentView.showView(with: CGRect(x: 0, y: (self.view.bounds.size.height ?? 0) - 343, width: self.view.bounds.width ?? 0, height: 343), vc: vc)
        self.view.addSubview(presentView)
        
        vc.createRoomBlock = { height in
            presentView.update(height)
        }
    
        vc.createRoomVCBlock = {[weak self] (name, pwd) in
            presentView.dismiss()
            guard let self = self else {return}
            SVProgressHUD.show()
            service.createRoom(roomName: name, gameId: gameId, password: pwd) { [weak self] roomInfo, error in
                SVProgressHUD.dismiss()
                guard let self = self, error == nil else {return}
                guard let roomInfo = roomInfo else {return}
                
                let gameVC = PlayGameController(userInfo: self.userInfo, service: self.service, roomInfo: roomInfo)
                let gameId = self.dataArray[indexPath.section].games[indexPath.item].gameId
                gameVC.hidesBottomBarWhenPushed = true
                self.navigationController?.pushViewController(gameVC, animated: true)
            }
        }
    }
}

extension GameListViewController {
    func showActionSheet() {
        let alertController = UIAlertController(title: "切换供应商", message: nil, preferredStyle: .actionSheet)
        let option1 = UIAlertAction(title: "忽然玩法", style: .default) { action in
            print("忽然玩法 selected")
        }
            
        let option2 = UIAlertAction(title: "元游玩法", style: .default) { action in
            print("元游玩法 selected")
        }
            
        let option3 = UIAlertAction(title: "群玩玩法", style: .default) { action in
            print("群玩玩法 selected")
        }
            
        let cancelAction = UIAlertAction(title: "取消", style: .cancel)
        
        alertController.addAction(option1)
        alertController.addAction(option2)
        alertController.addAction(option3)
        alertController.addAction(cancelAction)
        
        if let popoverController = alertController.popoverPresentationController {
            popoverController.sourceView = self.view
            popoverController.sourceRect = CGRect(x: self.view.bounds.midX, y: self.view.bounds.midY, width: 0, height: 0)
            popoverController.permittedArrowDirections = []
        }
        
        self.present(alertController, animated: true, completion: nil)
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
