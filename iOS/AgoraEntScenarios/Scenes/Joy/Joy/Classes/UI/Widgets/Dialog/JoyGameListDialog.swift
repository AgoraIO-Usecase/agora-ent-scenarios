//
//  JoyGameListDialog.swift
//  Joy
//
//  Created by wushengtao on 2023/11/30.
//

import UIKit
import SDWebImage

class JoyGameListCell: UICollectionViewCell {
    override var isSelected: Bool {
        didSet {
            if isSelected {
                layer.borderWidth = 2
                layer.borderColor = UIColor.joy_btn_bg.cgColor
                backgroundColor = .joy_btn_bg
                nameLabel.textColor = .joy_main_text
            } else {
                layer.borderWidth = 2
                layer.borderColor = UIColor.clear.cgColor
                backgroundColor = .clear
                nameLabel.textColor = .joy_title_text
            }
        }
    }
    var gameInfo: CloudGameInfo? {
        didSet {
            imageView.sd_setImage(with: URL(string: gameInfo?.thumbnail ?? ""),
                                  placeholderImage: UIImage.sceneImage(name: "game_placeholder"))
            nameLabel.text = gameInfo?.name ?? ""
        }
    }
    // 背景图
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.cornerRadius = 10
        imageView.layer.masksToBounds = true
        imageView.backgroundColor = .gray
        imageView.contentMode = .scaleAspectFill
        return imageView
    }()
    // 房间名称
    private lazy var nameLabel: UILabel = {
        let nameLabel = UILabel()
        nameLabel.font = .joy_M_12
        nameLabel.textAlignment = .center
        return nameLabel
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews() {
        layer.cornerRadius = 15
        clipsToBounds = true
        contentView.addSubview(imageView)
        contentView.addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.bottom.equalTo(-8)
            make.height.equalTo(17)
        }
        
        imageView.snp.makeConstraints { make in
            make.left.right.top.equalToSuperview()
            make.bottom.equalTo(nameLabel.snp.top).offset(-4)
        }
    }
}

class JoyGameListDialog: JoyBaseDialog {
    var onSelectedGame: ((CloudGameInfo)->())?
    var gameList: [CloudGameInfo] = [] {
        didSet {
            listView.reloadData()
            if let game = gameList.first {
                selectedGame = game
                self.listView.selectItem(at: IndexPath(row: 0, section: 0), animated: false, scrollPosition: .centeredHorizontally)
            }
        }
    }
    private var selectedGame: CloudGameInfo? {
        didSet {
            button.isEnabled = selectedGame == nil ? false : true
        }
    }
    private lazy var listView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        layout.minimumInteritemSpacing = 8
        let itemWidth = (self.width - 8.0 * 3.0 - 20 * 2) / 4.0
        layout.itemSize = CGSize(width: floor(itemWidth), height: 115)
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
        collectionView.showsVerticalScrollIndicator = false
        collectionView.backgroundColor = .clear
        collectionView.register(JoyGameListCell.self, forCellWithReuseIdentifier: NSStringFromClass(JoyGameListCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        return collectionView
    }()
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.width, height: 268)
    }
    
    override func loadCustomContentView(contentView: UIView) {
        contentView.addSubview(listView)
        listView.snp.makeConstraints { make in
            make.edges.equalTo(contentView)
        }
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        // 检查是否有子视图响应触摸事件
        if let superViewHitTestView = super.hitTest(point, with: event), superViewHitTestView != self {
            return superViewHitTestView
        } else {
            // 如果没有子视图响应，则返回父视图，使触摸事件穿透到下一层视图
            return nil
        }
    }
    
    override func labelTitle() -> String {
        return "dialog_title_gamelist".joyLocalization()
    }
    
    override func buttonTitle() -> String {
        return "gamelist_selected_confirm".joyLocalization()
    }
    
    override func onClickButton() {
        guard let selectedGame = selectedGame else {return}
        onSelectedGame?(selectedGame)
    }
}

extension JoyGameListDialog: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: JoyGameListCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(JoyGameListCell.self), for: indexPath) as! JoyGameListCell
        let game = gameList[indexPath.item]
        cell.gameInfo = game
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        selectedGame = gameList[indexPath.item]
        if let cell = collectionView.cellForItem(at: indexPath) {
            cell.isSelected = true
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return gameList.count
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
}
