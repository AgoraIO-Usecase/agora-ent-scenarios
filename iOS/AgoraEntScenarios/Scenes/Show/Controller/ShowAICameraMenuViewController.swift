//
//  ShowMetaMenuViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2024/3/7.
//

import UIKit

private let cellHeight: CGFloat = 60
private let lineSpacing: CGFloat = 48

class ShowAICameraMenuViewController: UIViewController {
    
    var onSelectedItem: ((_ item: AICameraMenuItem?)->())?
    var onDeSelectedItem: ((_ item: AICameraMenuItem?)->())?
    var dismissed: (()->())?
    var defalutSelectIndex =  0
    var currentItem: AICameraMenuItem?{
        didSet{
            guard let currentItem = currentItem else {return}
            if currentItem == oldValue {
                currentItem.isSelected = !currentItem.isSelected
                if currentItem.isSelected {
                    onSelectedItem?(currentItem)
                }else{
                    onDeSelectedItem?(currentItem)
                }
            }else{
                onSelectedItem?(currentItem)
                dataArray.forEach { item in
                    item.isSelected = item == currentItem
                }
            }
            collectionView.reloadData()
        }
    }
    
    private let dataArray  = [
        AICameraMenuItem(id: .avatar),
        AICameraMenuItem(id: .avatar),
        AICameraMenuItem(id: .avatar),
        AICameraMenuItem(id: .rhythm_heart),
        AICameraMenuItem(id: .rhythm_portrait),
        AICameraMenuItem(id: .rhythm_faceLock_L),
        AICameraMenuItem(id: .avatar),
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
        cell.onClickDownloadButton = {state in
            model.updateState(.loading)
            collectionView.reloadItems(at: [indexPath])
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                model.updateState(.done)
                collectionView.reloadItems(at: [indexPath])
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

