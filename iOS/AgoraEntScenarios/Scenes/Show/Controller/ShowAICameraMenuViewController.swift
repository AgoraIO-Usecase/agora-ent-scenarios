//
//  ShowMetaMenuViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2024/3/7.
//

import UIKit
import AGResourceManager
import AgoraCommon

private let cellHeight: CGFloat = 60
private let lineSpacing: CGFloat = 48

class ShowAICameraMenuViewController: UIViewController {
    
    var onSelectedItem: ((_ item: AICameraMenuItem?)->())?
    var onDeSelectedItem: ((_ item: AICameraMenuItem?)->())?
    var dismissed: (()->())?
    var defalutSelectIndex =  0
    var currentItem: AICameraMenuItem?{
        didSet{
            if currentItem == oldValue {
                let isSelected = !(currentItem?.isSelected ?? false)
                currentItem?.isSelected = isSelected
                if isSelected {
                    didSelectedItem(currentItem)
                    
                }else{
                    didDeSelectedItem(currentItem)
                }
            }else{
                oldValue?.isSelected = false
                currentItem?.isSelected = true
                didDeSelectedItem(oldValue)
                didSelectedItem(currentItem)
            }
            collectionView.reloadData()
        }
    }
    
    private var observer: NSObjectProtocol?

    private let dataArray  = [
        AICameraMenuItem(id: .rhythm_portrait),
        AICameraMenuItem(id: .face_border_light),
        AICameraMenuItem(id: .rhythm_faceLock_L),
        AICameraMenuItem(id: .ad_light),
        AICameraMenuItem(id: .ai_3d_light),
        AICameraMenuItem(id: .ai_3d_light_virtual_bg),
        AICameraMenuItem(id: .polar_light),
    ]
    
    // 背景
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .show_dark_cover_bg
        return bgView
    }()
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        let marginLeft: CGFloat = 20
        let interSpacing: CGFloat = 15
        let countFowRow: CGFloat = 5
        let cellWidth: CGFloat = (Screen.width - marginLeft * 2  - (countFowRow - 1) * interSpacing) / countFowRow - 2
        layout.minimumInteritemSpacing = interSpacing
        layout.minimumLineSpacing = lineSpacing
        layout.sectionInset = UIEdgeInsets(top: 0, left: marginLeft, bottom: 0, right: marginLeft)
        layout.itemSize = CGSize(width: cellWidth, height: cellHeight)
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.register(ShowAICameraMenuCell.self, forCellWithReuseIdentifier: NSStringFromClass(ShowAICameraMenuCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        return collectionView
    }()
    
    deinit {
        currentItem = nil
        removeObserver()
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overCurrentContext
//        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configDefaultSelect()
        ShowAgoraKitManager.shared.initializeMeta()
        refreshItems()
        addObserver()
    }
    
    private func setUpUI(){
        
        view.addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(240)
        }
        
        // 列表
        bgView.addSubview(collectionView)
        collectionView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(30)
            make.height.equalTo(lineSpacing + 2 * cellHeight)
        }
      
    }
    
    private func addObserver(){
        NotificationCenter.default.addObserver(forName: ShowAgoraKitManager.disableVirtualBg360NotificaitonName, object: nil, queue: nil) { [weak self]_ in
            if let item = self?.dataArray.first(where: {$0.id == .ai_3d_light_virtual_bg}) {
                item.isSelected = false
                self?.collectionView.reloadData()
            }
        }
    }
    
    private func removeObserver() {
        if let observer = observer {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    private func configDefaultSelect(){
        // 默认选中
        CATransaction.begin()
        CATransaction.setCompletionBlock {
            let indexPath = IndexPath(item: self.defalutSelectIndex, section: 0)
            if self.collectionView.numberOfItems(inSection: 0)  > self.defalutSelectIndex {
                self.collectionView.selectItem(at: indexPath, animated: false, scrollPosition: .left)
            }
        }
        collectionView.reloadData()
        CATransaction.commit()
    }

}

extension ShowAICameraMenuViewController {
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        bgView.setRoundingCorners([.topLeft, .topRight], radius: 20)
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
        dismissed?()
    }
}

extension ShowAICameraMenuViewController: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: ShowAICameraMenuCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(ShowAICameraMenuCell.self), for: indexPath) as! ShowAICameraMenuCell
        let model = dataArray[indexPath.item]
        cell.menuItem = model
        cell.onClickDownloadButton = { [weak self] state in
            self?.downloadBaseEffectResources()
            if model.id == .ai_3d_light_virtual_bg {
                self?.downloadEffectBgImage(item: model)
            }
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let item = dataArray[indexPath.item]
        if item.state != .done {return}
        currentItem = item
    }
}

extension ShowAICameraMenuViewController {
    func didSelectedItem(_ item: AICameraMenuItem?) {
        guard let item = item else { return }
        switch item.id {
        case .rhythm_portrait:
            ShowAgoraKitManager.shared.swithRhythm(mode: .faceLock_L)
        case .face_border_light:
            ShowAgoraKitManager.shared.setOnEffect3D(type: .face_border_light)
        case .rhythm_faceLock_L:
            ShowAgoraKitManager.shared.swithRhythm(mode: .portrait)
        case .ad_light:
            ShowAgoraKitManager.shared.setOnEffect3D(type: .ad_light)
        case .ai_3d_light:
            ShowAgoraKitManager.shared.setOnEffect3D(type: .ai_3d_light)
        case .ai_3d_light_virtual_bg:
            trySetOffVirtualBg()
            ShowAgoraKitManager.shared.setOnEffect3D(type: .ai_3d_light)
            ShowAgoraKitManager.shared.setupBackground360(enabled: true)
        case .polar_light:
            ShowAgoraKitManager.shared.setOnEffect3D(type: .polar_light)
        }
        onSelectedItem?(item)
    }
    
    func didDeSelectedItem(_ item: AICameraMenuItem?) {
        guard let item = item else { return }
        switch item.id {
        case .rhythm_faceLock_L , .rhythm_portrait:
            ShowAgoraKitManager.shared.enableRhythm(false)
        case .face_border_light:
            ShowAgoraKitManager.shared.setOffEffect3D(type: .face_border_light)
        case .ad_light:
            ShowAgoraKitManager.shared.setOffEffect3D(type: .ad_light)
        case .ai_3d_light:
            ShowAgoraKitManager.shared.setOffEffect3D(type: .ai_3d_light)
        case .ai_3d_light_virtual_bg:
            ShowAgoraKitManager.shared.setOffEffect3D(type: .ai_3d_light)
            ShowAgoraKitManager.shared.setupBackground360(enabled: false)
        case .polar_light:
            ShowAgoraKitManager.shared.setOffEffect3D(type: .polar_light)
        }
        onDeSelectedItem?(item)
    }
}

extension ShowAICameraMenuViewController {
    
    private func trySetOffVirtualBg(){
        if ShowAgoraKitManager.shared.enableVirtualBg {
            // 自动关闭虚拟背景
            ShowAgoraKitManager.shared.enableVirtualBackground(isOn: false,greenCapacity: 0)
            ShowAgoraKitManager.shared.seVirtualtBackgoundImage(imagePath: nil, isOn: false)
            ToastView.show(text: "show_disable_virturalBg_toast".show_localized)
            NotificationCenter.default.post(name: ShowAgoraKitManager.disableVirtualBgNotificaitonName, object: nil)
        }
    }
    
    private func refreshItems(){
        self.dataArray.forEach { item in
            if item.id == .ai_3d_light_virtual_bg {
                if ShowAgoraKitManager.shared.baseResourceIsLoaded && ShowAgoraKitManager.shared.effectImageIsLoaded {
                    item.updateState(.done)
                }
            }else{
                if ShowAgoraKitManager.shared.baseResourceIsLoaded {
                    item.updateState(.done)
                }
            }
        }
        self.collectionView.reloadData()
    }
    
    private func downloadBaseEffectResources(){
        self.dataArray.forEach { item in
            if item.id == .ai_3d_light_virtual_bg {
                if ShowAgoraKitManager.shared.effectImageIsLoaded {
                    item.updateState(.loading)
                }
            }else{
                item.updateState(.loading)
            }
        }
        collectionView.reloadData()
        ShowAgoraKitManager.shared.downloadBaseEffectResources {[weak self] error, bgImageIsLoaded in
            if error == nil {
                self?.dataArray.forEach { item in
                    if item.id == .ai_3d_light_virtual_bg {
                        if bgImageIsLoaded {
                            item.updateState(.done)
                        }
                    }else{
                        item.updateState(.done)
                    }
                }
                self?.collectionView.reloadData()
            }
        }
    }
    
    private func downloadEffectBgImage(item: AICameraMenuItem){
        if item.id != .ai_3d_light_virtual_bg { return }
        item.updateState(.loading)
        collectionView.reloadData()
        ShowAgoraKitManager.shared.downloadEffectBgImage {[weak self] error, baseReourceIsLoaded in
            if error == nil ,baseReourceIsLoaded {
                item.updateState(.done)
                self?.collectionView.reloadData()
            }
        }
    }
}
