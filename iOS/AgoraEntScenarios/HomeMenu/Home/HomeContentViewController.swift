//
//  HomeContentViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/11.
//

import UIKit
import JXCategoryView
import Pure1v1;
import ShowTo1v1;

@objc
class HomeContentViewController: UIViewController {
    @objc var changeToNavigationBarAlpha: ((CGFloat) -> Void)?
    private lazy var flowLayout: UICollectionViewFlowLayout = {
       let flowLayout = UICollectionViewFlowLayout()
        flowLayout.itemSize = CGSize(width: (Screen.width - 40.fit - 15.fit) * 0.5, height: 80.fit)
        return flowLayout
    }()
    private lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(HomeContentViewCell.self, forCellWithReuseIdentifier: "HomeCell")
        collectionView.register(HomeContentSessionView.self,
                                forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader,
                                withReuseIdentifier: "sessionTitle")
        collectionView.backgroundColor = .clear
        return collectionView
    }()
    private var currentType: HomeType = .all
    private var dataArray: [HomeContentSesionModel]?
    
    init(type: HomeType) {
        super.init(nibName: nil, bundle: nil)
        currentType = type
        dataArray = HomeContentSesionModel.createData().filter({ type == .all ? $0.type != .all : $0.type == type })
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
    }
    
    @objc
    func getScrollToPostion() {
        var delta = collectionView.contentOffset.y / Screen.kNavHeight
        delta = CGFloat.maximum(delta, 0)
        changeToNavigationBarAlpha?(delta)
    }

    private func setupUI() {
        view.backgroundColor = .clear
        view.addSubview(collectionView)
        collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
}

extension HomeContentViewController: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        currentType == .all ? (dataArray?.count ?? 0) : 1
    }
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        dataArray?[section].contentModels?.count ?? 0
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "HomeCell", for: indexPath) as! HomeContentViewCell
        let model = dataArray?[indexPath.section].contentModels?[indexPath.item]
        cell.setupData(model: model)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let model = dataArray?[indexPath.section].contentModels?[indexPath.item] else { return }
        if model.isEnable == false { return }
        NetworkManager.shared.reportSceneClick(sceneName: model.type.sceneName)
        NetworkManager.shared.reportDeviceInfo(sceneName: model.type.sceneName)
        NetworkManager.shared.reportUserBehavior(sceneName: model.type.sceneName)
        
        switch model.type {
        case .solo:
            ToastView.show(text: "敬请期待")
            return
            
//            let vc = VLOnLineListVC()
//            navigationController?.pushViewController(vc, animated: true)
        
        case .chorus:
            break
//            let vc = VLOnLineListVC()
//            navigationController?.pushViewController(vc, animated: true)
            
        case .continue_singing:
            ToastView.show(text: "敬请期待")
            return
            
//            let vc = VLSROnLineListVC()
//            navigationController?.pushViewController(vc, animated: true)
            
        case .snatch_singing:
            ToastView.show(text: "敬请期待")
            return
            
//            let vc = VLSBGOnLineListVC()
//            navigationController?.pushViewController(vc, animated: true)
        case .voice_chat:
            let vc = VRRoomsViewController(user: VLUserCenter.user)
            navigationController?.pushViewController(vc, animated: true)
            
        case .spatial_voice:
            let vc = SARoomsViewController(user: VLUserCenter.user)
            navigationController?.pushViewController(vc, animated: true)
            
        case .show:
            let vc = ShowRoomListVC()
            navigationController?.pushViewController(vc, animated: true)
    
        case .one_v_one:
            let userInfo = Pure1v1UserInfo()
            userInfo.userId = VLUserCenter.user.id
            userInfo.userName = VLUserCenter.user.name
            userInfo.avatar = VLUserCenter.user.headUrl
            Pure1v1Context.showScene(viewController: self,
                                     appId: KeyCenter.AppId,
                                     appCertificate: KeyCenter.Certificate ?? "",
                                     userInfo: userInfo)
            
        case .multiple:
            break
            
        case .show_private_one_v_one:
            let userInfo = ShowTo1v1UserInfo()
            userInfo.uid = VLUserCenter.user.id
            userInfo.userName = VLUserCenter.user.name
            userInfo.avatar = VLUserCenter.user.headUrl
            ShowTo1v1Context.showScene(viewController: self,
                                       appId: KeyCenter.AppId,
                                       appCertificate: KeyCenter.Certificate ?? "",
                                       userInfo: userInfo)
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        CGSize(width: Screen.width, height: currentType == .all ? 40 : 0)
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        guard kind == UICollectionView.elementKindSectionHeader else { return UICollectionReusableView() }
        
        let view = collectionView.dequeueReusableSupplementaryView(ofKind: kind,
                                                                   withReuseIdentifier: "sessionTitle",
                                                                   for: indexPath) as! HomeContentSessionView
        view.setupTitle(title: dataArray?[indexPath.section].title)
        return view
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        UIEdgeInsets(top: 12.fit, left: 20.fit, bottom: (dataArray?.count ?? 0) - 1 == section ? 20.fit : 0, right: 20.fit)
    }
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        var delta = scrollView.contentOffset.y / Screen.kNavHeight
        delta = CGFloat.maximum(delta, 0)
        changeToNavigationBarAlpha?(delta)
    }
}

extension HomeContentViewController: JXCategoryListContentViewDelegate {
    func listView() -> UIView! {
        view
    }
}
