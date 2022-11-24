//
//  ShowMusicEffectCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/9.
//

import UIKit


class ShowMusicEffectCell: UITableViewCell {
    
    typealias ShowMusicEffectCellSelectAction = ((_ index: Int)->())
    
    private var selectAction: ShowMusicEffectCellSelectAction?
    
    enum LayoutStyle: String {
        case imageTop   // 图片在上
        case imageBackground // 图片是背景
        case imageOnly  // 只有图片
    }
    
    class CellData {
        let image: String
        let title: String
        let style: LayoutStyle
        var isSelected: Bool
        
        init(image: String, title: String, style: LayoutStyle, isSelected: Bool) {
            self.image = image
            self.title = title
            self.style = style
            self.isSelected = isSelected
        }
    }
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_main_text
        label.font = .show_R_14
        label.numberOfLines = 1
        return label
    }()
    
    var dataArray = [CellData]()
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumInteritemSpacing = 12
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        layout.itemSize = CGSize(width: 58, height: 58)
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.register(ShowMusicImageTopItemCell.self, forCellWithReuseIdentifier: LayoutStyle.imageTop.rawValue)
        collectionView.register(ShowMusicImageBackgroundItemCell.self, forCellWithReuseIdentifier: LayoutStyle.imageBackground.rawValue)
        collectionView.register(ShowMusicImageOnlyItemCell.self, forCellWithReuseIdentifier: LayoutStyle.imageOnly.rawValue)

        collectionView.delegate = self
        collectionView.dataSource = self
        return collectionView
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        contentView.backgroundColor = .clear
        backgroundColor = .clear
        
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(18)
            make.left.equalTo(20)
        }
        
        contentView.addSubview(collectionView)
        collectionView.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(10)
            make.left.right.bottom.equalToSuperview()
        }
    }
    
    func setTitle(_ title: String, dataArray: [CellData], selectAction: @escaping ShowMusicEffectCellSelectAction) {
        titleLabel.text = title
        self.dataArray = dataArray
        collectionView.reloadData()
        self.selectAction = selectAction
    }
}


extension ShowMusicEffectCell: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let item = dataArray[indexPath.item]
        let cell: ShowMusicItemCell = collectionView.dequeueReusableCell(withReuseIdentifier: item.style.rawValue, for: indexPath) as! ShowMusicItemCell
        cell.setImage(item.image, name: item.title, isSelected: item.isSelected)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        selectAction?(indexPath.item)
    }
}
