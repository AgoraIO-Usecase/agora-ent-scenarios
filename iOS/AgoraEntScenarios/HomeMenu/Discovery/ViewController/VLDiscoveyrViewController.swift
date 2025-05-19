//
//  VLDiscoveyrController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/20.
//

import UIKit

@objc
class VLDiscoveyrViewController: VLBaseViewController {
    private lazy var flowLayout: UICollectionViewFlowLayout = {
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .vertical
        return flowLayout
    }()
    private lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.backgroundColor = .clear
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(VLDiscoverCell.self, forCellWithReuseIdentifier: "discoveryCell")
        collectionView.register(VLDiscoveryHeaderView.self,
                                forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader,
                                withReuseIdentifier: "headerView")
        collectionView.register(VLDiscoverySessionView.self,
                                forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader,
                                withReuseIdentifier: "sessionView")
        return collectionView
    }()
    
    private let dataArray: [VLDiscoveryModel] = VLDiscoveryModel.createData()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        hiddenBackgroundImage()
        setupUI()
    }
    
    private func setupUI() {
        view.addSubview(collectionView)
        collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
}

extension VLDiscoveyrViewController: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        dataArray.count
    }
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        dataArray[section].items?.count ?? 0
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "discoveryCell", for: indexPath) as! VLDiscoverCell
        let model = dataArray[indexPath.section].items?[indexPath.item]
        cell.setupModel(model: model)
        cell.updateLayout(layoutType: model?.layoutType ?? .full)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let model = dataArray[indexPath.section].items?[indexPath.item]
        let webViewVC = VLDiscoveryWebViewController()
        webViewVC.urlString = "\(model?.schemeUrl ?? "")?token=\(VLUserCenter.user.token)"
        navigationController?.pushViewController(webViewVC, animated: true)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        guard let model = dataArray[indexPath.section].items?[indexPath.item] else { return .zero }
        switch model.layoutType {
        case .full, .side: return CGSize(width: (Screen.width - 40.fit), height: 100.fit)
        case .half: return CGSize(width: (Screen.width - 40.fit - 16.fit) * 0.5, height: 100.fit)
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        UIEdgeInsets(top: 12.fit, left: 20.fit, bottom: section == 0 ? 0 : 12.fit, right: 20.fit)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        13.fit
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        16.fit
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        section == 0 ? CGSize(width: Screen.width, height: 225.fit) : CGSize(width: Screen.width, height: 17.fit)
    }
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        if kind == UICollectionView.elementKindSectionHeader {
            let model = dataArray[indexPath.section]
            if model.title == nil {
                let headerView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: "headerView", for: indexPath) as! VLDiscoveryHeaderView
                return headerView
            } else {
                let sesstionView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: "sessionView", for: indexPath) as! VLDiscoverySessionView
                sesstionView.setupTitle(title: model.title)
                return sesstionView
            }
        }
        return UICollectionReusableView()
    }
}
